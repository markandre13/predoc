/*
 * $Id: PNGDataEncoder.java,v 1.11 1998/07/15 03:37:10 verylong Exp $
 *
 * $Log: PNGDataEncoder.java,v $
 * Revision 1.11  1998/07/15 03:37:10  verylong
 * 1. Fixed error in PNGImageEncoder.
 * 2. PNGRowInfo merged into PNGRowHandler.
 * 3. All data stored in 3 byte arrays inteas int arrays.
 * 4. InputStream in PNGDataDecoder replaced by DataInputStream.
 * 5. OutputStream in PNGDataEncoder replaced by DataOutputStream.
 * 6. Some additional optimisation completed.
 * 7. Maybe something else... I don't remember exactly...
 *
 * The version was tested on PngSutite and it works not worse then
 * previous one. It seems to me :-).
 *
 * Revision 1.10  1998/07/08 19:18:07  verylong
 * There was made big optimisation of time-eating procedures. Fixed
 * unnecessary buffers allocation inside loops and double buffering in
 * image writing.  After the optiomisation time of read/write image cycle
 * is 80% of unoptimized and I think significant memory optimisation was
 * reached too.
 *
 * Revision 1.9  1998/07/01 22:58:21  verylong
 * Method ubyte() was removed. All calls were modified to in-line
 * functionality of the method.
 *
 * Revision 1.8  1998/05/07 23:32:34  vlad
 * Some changes in PNG usage.
 *
 * Revision 1.7  1998/05/07 18:36:09  vlad
 * more brush up
 *
 * Revision 1.6  1998/05/06 22:45:50  vlad
 * png lib brush up
 *
 * Revision 1.5  1998/04/15 04:46:07  vb
 * Black row problem fixed
 *
 * Revision 1.4  1998/04/15 00:10:27  vlad
 * Brush up
 *
 * Revision 1.3  1998/03/03 00:01:02  vlad
 * Brush up
 *
 * Revision 1.2  1997/08/13 01:05:42  lord
 * move some classes from PNG to Cubes
 *
 * Revision 1.1  1997/08/05 17:53:31  lord
 * PNG code added. JDK-1.1.3 is now used
 *
 */

package com.visualtek.png;

import java.awt.Color;
import java.util.zip.*;
import java.io.*;


// PNG encoder
class PNGDataEncoder extends PNGData
{
    private DataOutputStream    ostream          = null;
    private Deflater            zstream          = null;

    private int                 zout_len         = 0;

    private final static int    PNG_NO_FILTERS   =  0x00;
    private final static int    PNG_FILTER_NONE  =  0x08;
    private final static int    PNG_FILTER_SUB   =  0x10;
    private final static int    PNG_FILTER_UP    =  0x20;
    private final static int    PNG_FILTER_AVG   =  0x40;
    private final static int    PNG_FILTER_PAETH =  0x80;
    private final static int    PNG_ALL_FILTERS  =  PNG_FILTER_NONE | PNG_FILTER_SUB | PNG_FILTER_UP |
    PNG_FILTER_AVG | PNG_FILTER_PAETH;

/* Constructor */
    public  PNGDataEncoder ( OutputStream o_stream )
    {
        super ();

        ostream = new DataOutputStream(o_stream);
        zstream = new Deflater ();

        /* initialize zbuf - compression buffer */
        zbuf_size = PNG_ZBUF_SIZE;
        zbuf = new byte [zbuf_size];
    }

/* Writing Info */
    void writeInfo ( PNGInfo info )  throws PNGException, IOException
    {
        /* write PNG signature */
       ostream.write(png_sig, 0, 8);

       if (info.color_type == PNGData.PNG_COLOR_TYPE_GRAY ||
           info.color_type == PNGData.PNG_COLOR_TYPE_GRAY_ALPHA)
           row_info = new PNGGrayRowHandler(info);
       else if ((info.color_type & PNGData.PNG_COLOR_TYPE_PALETTE) == PNGData.PNG_COLOR_TYPE_PALETTE)
           row_info = new PNGColorRowHandler(info);
       else if ((info.color_type & PNGData.PNG_COLOR_TYPE_RGB) == PNGData.PNG_COLOR_TYPE_RGB) 
           row_info = new PNGTrueColorRowHandler(info);
       else
           throw new PNGException("Not supported color type = " + info.color_type);

        /* write IHDR information. */
        writeIHDR(info.width, info.height,
                  info.bit_depth, info.color_type, info.compression_type,
                  info.filter_type, info.interlace_type);

        if ((info.valid & PNG_INFO_PLTE) != 0)
            writePLTE(info.palette, info.num_palette);
        else if (info.color_type == PNG_COLOR_TYPE_PALETTE)
            throw new PNGException("Valid palette required for paletted images");
    }

/* Writing row */
    void writeRow(byte[] row, int offset, int length)
        throws PNGException, IOException
    {
        /* initialize transformations and other stuff if first time */
        if (row_number == 0 && pass == 0)
        {
            writeStartRow();
        }

        /* set up row info for transformations */
        row_info.color_type = color_type;
        row_info.width = usr_width;
        row_info.channels = usr_channels;
        row_info.bit_depth = usr_bit_depth;
        row_info.pixel_depth = (byte)(row_info.bit_depth * row_info.channels);
        row_info.rowbytes = ((row_info.width * (int)row_info.pixel_depth + 7) >> 3);

        /* copy users row into buffer, leaving room for filter byte */
        System.arraycopy(row, offset, row_buf, 1, row_info.rowbytes);
        /* handle other transformations */
        /*!!! not implemented now
          if (transformations)
          doWriteTransformations();
        */

        /* find a filter if necessary, filter the row and write it out */
        writeFindFilter(row_info);
    }

/* End writing */
    void writeEnd ( PNGInfo info )  
        throws PNGException, IOException
    {
        if ((mode & PNG_HAVE_IDAT) == 0)
            throw new PNGException("No IDATs written into file");

        mode |= PNG_AFTER_IDAT;

        /* write end of png file */
        writeIEND();
    }

/* writes method */
    protected void writeIHDR ( int vwidth, int vheight, int vbit_depth,
                               int vcolor_type, int vcompression_type,
                               int vfilter_type, int vinterlace_type ) throws PNGException, IOException
    {
        byte[] buf = new byte [13]; /* buffer to store the IHDR info */

        /* Check that we have valid input data from the application info */
        switch (vcolor_type)
        {
        case 0:
            switch (vbit_depth)
            {
            case 1:
            case 2:
            case 4:
            case 8:
            case 16: 
                channels = 1; 
                break;
            default: 
                new PNGException("Invalid bit depth for grayscale image");
            }
            break;
        case 2:
            if (vbit_depth != 8 && vbit_depth != 16)
                new PNGException("Invalid bit depth for RGB image");
            channels = 3;
            break;
        case 3:
            switch (vbit_depth)
            {
            case 1:
            case 2:
            case 4:
            case 8: 
                channels = 1;
                break;
            default: 
                new PNGException("Invalid bit depth for paletted image");
            }
            break;
        case 4:
            if (vbit_depth != 8 && vbit_depth != 16)
                new PNGException("Invalid bit depth for grayscale+alpha image");
            channels = 2;
            break;
        case 6:
            if (vbit_depth != 8 && vbit_depth != 16)
                new PNGException("Invalid bit depth for RGBA image");
            channels = 4;
            break;
        default:
            new PNGException("Invalid image color type specified");
        }

        if (vcompression_type != 0)
        {
            warning("Invalid compression type specified");
            vcompression_type = 0;
        }

        if (vfilter_type != 0)
        {
            warning("Invalid filter type specified");
            vfilter_type = 0;
        }

        if (vinterlace_type != 0 && vinterlace_type != 1)
        {
            warning("Invalid interlace type specified");
            vinterlace_type = 1;
        }

        /* save off the relevent information */
        bit_depth = (byte)vbit_depth;
        color_type = (byte)vcolor_type;
        interlaced = (byte)vinterlace_type;
        width = vwidth;
        height = vheight;

        pixel_depth = (byte)(vbit_depth * channels);
        rowbytes = ((vwidth * (int)pixel_depth + 7) >> 3);
        /* set the usr info, so any transformations can modify it */
        usr_width = width;
        usr_bit_depth = bit_depth;
        usr_channels = channels;

        /* pack the header information into the buffer */
        saveInt32(buf, vwidth);
        saveInt32(buf, 4, vheight);
        buf[8] = (byte)vbit_depth;
        buf[9] = (byte)vcolor_type;
        buf[10] = (byte)vcompression_type;
        buf[11] = (byte)vfilter_type;
        buf[12] = (byte)vinterlace_type;

        /* write the chunk */
        writeChunk(png_IHDR, buf, 13);

        if (do_filter == 0)
        {
            if (color_type == 3 || bit_depth < 8)
                do_filter = PNG_FILTER_NONE;
            else
                do_filter = PNG_ALL_FILTERS;
        }
        if ((flags & PNG_FLAG_ZLIB_CUSTOM_STRATEGY) == 0)
        {
            if (do_filter != PNG_FILTER_NONE)
                zlib_strategy = Deflater.FILTERED;
            else
                zlib_strategy = Deflater.DEFAULT_STRATEGY;
        }
        if ((flags & PNG_FLAG_ZLIB_CUSTOM_LEVEL) == 0)
            zlib_level = Deflater.DEFAULT_COMPRESSION;
        if ((flags & PNG_FLAG_ZLIB_CUSTOM_MEM_LEVEL) == 0)
            zlib_mem_level = 8;
        if ((flags & PNG_FLAG_ZLIB_CUSTOM_WINDOW_BITS) == 0)
            zlib_window_bits = 15;
        if ((flags & PNG_FLAG_ZLIB_CUSTOM_METHOD) == 0)
            zlib_method = 8;

        zstream.setLevel    (zlib_level);
        zstream.setStrategy (zlib_strategy);
        mode = PNG_HAVE_IHDR;
    }

/* writes method */
    protected void writePLTE ( Color[] vpalette, int number )  throws PNGException, IOException
    {
        if (number == 0 || number > 256)
        {
            if (color_type == PNG_COLOR_TYPE_PALETTE)
            {
                new PNGException("Invalid number of colors in palette");
            }
            else
            {
                warning("Invalid number of colors in palette");
                return;
            }
        }

        byte[] buf = new byte [3];
        num_palette = (short)number;

        writeChunkStart(png_PLTE, number * 3);
        for (int i = 0; i < number; i++)
        {
            buf[0] = (byte)vpalette[i].getRed();
            buf[1] = (byte)vpalette[i].getGreen();
            buf[2] = (byte)vpalette[i].getBlue();
            writeChunkData(buf, 3);
        }
        writeChunkEnd();
        mode |= PNG_HAVE_PLTE;
    }

/* writes method */
    protected void writeIDAT ( byte[] data, int length )  throws IOException
    {
        writeChunk(png_IDAT, data, length);
        mode |= PNG_HAVE_IDAT;
    }

/* writes method */
    protected void writeIEND ()  throws IOException
    {
        writeChunk(png_IEND, null, 0);
        mode |= PNG_HAVE_IEND;
    }

/* writes method */
    protected void writeChunk ( byte[] vchunk_name, byte[] data, int length )  throws IOException
    {
        writeChunkStart(vchunk_name, length);
        writeChunkData(data, length);
        writeChunkEnd();
    }

/* writes method */
    protected void writeChunkStart ( byte[] vchunk_name, int length ) throws IOException
    {
        /* write the length */
        ostream.writeInt(length);
        /* write the chunk name */
        ostream.write(vchunk_name, 0, 4);
        /* reset the crc and run it over the chunk name */
        resetCRC();
        calculateCRC(vchunk_name, 4);
    }

/* writes method */
    protected void writeChunkData ( byte[] data, int length )  throws IOException
    {
        /* write the data, and run the crc over it */
        if (length != 0)
        {
            calculateCRC(data, length);
            ostream.write(data, 0, length);
        }
    }

/* writes method */
    protected void writeChunkEnd () throws IOException
    {
        ostream.writeInt(crc);
    }

/* writer row method */
    protected void writeStartRow ()  throws IOException
    {
        /* set up row buffer */
        row_buf = new byte [(((int)usr_channels *
                              (int)usr_bit_depth *
                              width + 7) >> 3) + 1];
        row_buf[0] = 0;

        /* set up filtering buffer, if using this filter */
        if ((do_filter & PNG_FILTER_SUB) != 0)
        {
            sub_row = new byte [rowbytes + 1];
            sub_row[0] = 1;  /* Set the row filter type */
        }

        /* We only need to keep the previous row if we are using one of these */
        if ((do_filter & (PNG_FILTER_AVG | PNG_FILTER_UP | PNG_FILTER_PAETH)) != 0)
        {
            /* set up previous row buffer */
            prev_row = new byte [(((int)usr_channels *
                                   (int)usr_bit_depth *
                                   width + 7) >> 3) + 1];
            memSet(prev_row, 0, (((int)usr_channels *
                                  (int)usr_bit_depth *
                                  width + 7) >> 3) + 1);

            if ((do_filter & PNG_FILTER_UP) != 0)
            {
                up_row = new byte [rowbytes + 1];
                up_row[0] = 2;  /* Set the row filter type */
            }

            if ((do_filter & PNG_FILTER_AVG) != 0)
            {
                avg_row = new byte [rowbytes + 1];
                avg_row[0] = 3;  /* Set the row filter type */
            }

            if ((do_filter & PNG_FILTER_PAETH) != 0)
            {
                paeth_row = new byte [rowbytes + 1];
                paeth_row[0] = 4;  /* Set the row filter type */
            }
        }

        /* if interlaced, we need to set up width and height of pass */
        if (interlaced != 0)
        {
            if ((transformations & PNG_INTERLACE) == 0)
            {
                num_rows = (height + png_pass_yinc[0] - 1 -
                            png_pass_ystart[0]) / png_pass_yinc[0];
                usr_width = (width +
                             png_pass_inc[0] - 1 -
                             png_pass_start[0]) /
                    png_pass_inc[0];
            }
            else
            {
                num_rows = height;
                usr_width = width;
            }
        }
        else
        {
            num_rows = height;
            usr_width = width;
        }
    }

/* writer row method */
    protected void writeFindFilter(PNGRowHandler vrow_info) 
        throws PNGException, IOException
    {
        int best_row = 0; /* !!! 0 - row_buf
                             1 - sub_row
                             2 - up_row
                             3 - avg_row
                             4 - paeth_row */

        /* find out how many bytes offset each pixel is */
        int bpp = (vrow_info.pixel_depth + 7) / 8;

        int vprev_row = 0;
        int vrow_buf = 0;
        int mins = 0xffffffff;

        /* the prediction method we use is to find which method provides
           the smallest value when summing the abs of the distances from
           zero using anything >= 128 as negitive numbers. */

        /* We don't need to test the 'no filter' case if this is the only filter
         * that has been chosen, as it doesn't actually do anything to the data. */
        if ((do_filter & PNG_FILTER_NONE) != 0 &&
            do_filter != PNG_FILTER_NONE)
        {
            int rp;
            int sum = 0;
            int v;

            for (rp = 1; rp <= vrow_info.rowbytes; rp++)
            {
                v = row_buf[rp] & 0xff;
                sum += (v < 128) ? v : 256 - v;
            }
            mins = sum;
        }


        /* sub filter */
        if ((do_filter & PNG_FILTER_SUB) != 0)
        {
            int rp, lp;
            int sum = 0;
            int v;

            for (rp = 1; rp <= bpp; rp++)
            {
                sub_row[rp] = row_buf[rp];
                v = sub_row[rp] & 0xff;

                sum += (v < 128) ? v : 256 - v;
            }
            for (lp = 1; rp <= vrow_info.rowbytes; rp++, lp++)
            {
                v = ((row_buf[rp] & 0xff ) - (row_buf[lp] & 0xff)) & 0xff;
                sub_row[rp] = (byte)v;

                sum += (v < 128) ? v : 256 - v;
            }
            if (sum < mins)
            {
                mins = sum;
                best_row = 1; //!!! sub_row;
            }
        }

        /* up filter */
        if ((do_filter & PNG_FILTER_UP) != 0)
        {
            int rp;
            int sum = 0;
            int v;

            for (rp = 1; rp <= vrow_info.rowbytes; rp++)
            {
                v = ((row_buf[rp] & 0xff) - (prev_row[rp] & 0xff)) & 0xff;
                up_row[rp] = (byte) v;

                sum += (v < 128) ? v : 256 - v;
            }
            if (sum < mins)
            {
                mins = sum;
                best_row = 2; //!!! up_row;
            }
        }

        /* avg filter */
        if ((do_filter & PNG_FILTER_AVG) != 0)
        {
            int rp, lp;
            int sum = 0;
            int v;

            for (rp = 1; rp <= bpp; rp++)
            {
                v = ((row_buf[rp] & 0xff) - ((prev_row[rp] & 0xff)/2)) & 0xff;
                avg_row[rp] = (byte) v;

                sum += (v < 128) ? v : 256 - v;
            }
            for (lp = 1; rp <= vrow_info.rowbytes; rp++, lp++)
            {
                v = ((row_buf[rp] & 0xff) - 
                     (((prev_row[rp] & 0xff) + (row_buf[lp] & 0xff))/2)) & 0xff;
                avg_row[rp] = (byte) v;

                sum += (v < 128) ? v : 256 - v;
            }
            if (sum < mins)
            {
                mins = sum;
                best_row = 3; //!!! avg_row;
            }
        }

        /* paeth filter */
        if ((do_filter & PNG_FILTER_PAETH) != 0)
        {
            int rp, lp;
            int sum = 0;
            int v;

            for (rp = 1; rp <= bpp; rp++)
            {
                v = ((row_buf[rp] & 0xff) - (prev_row[rp] & 0xff)) & 0xff;
                paeth_row[rp] = (byte) v;

                sum += (v < 128) ? v : 256 - v;
            }

            int a, b, c, pa, pb, pc, p; 
            for (lp = 1; rp <= vrow_info.rowbytes; rp++, lp++)
            {
                b = prev_row[rp] & 0xff;
                c = prev_row[lp] & 0xff;
                a = row_buf[lp] & 0xff;

                // Call of Math.abs() is very time-expensive for this place. 
                // So I'm trying to avoid it any way. Verylong.

                pa = b - c;           // Here is an old version...
                pb = a - c;           // p = a + b - c;
                pc = pa + pb;         // pa = Math.abs(p - a);
                if (pa < 0) pa = -pa; // pb = Math.abs(p - b);
                if (pb < 0) pb = -pb; // pc = Math.abs(p - c);
                if (pc < 0) pc = -pc;

                if (pa <= pb && pa <= pc)
                    p = a;
                else if (pb <= pc)
                    p = b;
                else
                    p = c;

                v = ((row_buf[rp] & 0xff) - p) & 0xff;
                paeth_row[rp] = (byte) v;

                sum += (v < 128) ? v : 256 - v;
            }
            if (sum < mins)
            {
                best_row = 4; //!!! paeth_row;
            }
        }

        /* Do the actual writing of the filtered row data from the chosen filter */
        if (best_row == 0)
            writeFilteredRow(row_buf);
        else if (best_row == 1)
            writeFilteredRow(sub_row);
        else if (best_row == 2)
            writeFilteredRow(up_row);
        else if (best_row == 3)
            writeFilteredRow(avg_row);
        else if (best_row == 4)
            writeFilteredRow(paeth_row);
    }

/* writer row method */
    protected void writeFilteredRow ( byte[] filtered_row ) throws PNGException, IOException
    {
        /* set up the zlib input buffer */
        zstream.setInput (filtered_row, 0, row_info.rowbytes + 1);

        /* repeat until we have compressed all the data */
        do
        {
            /* compress the data */
            int def_bytes = zstream.deflate (zbuf, zout_len, zbuf_size - zout_len);
            zout_len += def_bytes;
            /* see if it is time to write another IDAT */
            if (zstream.finished() || zout_len >= zbuf_size)
            {
                /* write the IDAT and reset the zlib output buffer */
                writeIDAT(zbuf, zbuf_size);
                zout_len = 0;
            }
            /* repeat until all data has been compressed */
        } while (!zstream.needsInput());

        /* swap the current and previous rows */
        if (prev_row != null)
        {
            byte[] tptr;

            tptr = prev_row;
            prev_row = row_buf;
            row_buf = tptr;
        }

        /* finish row - updates counters and flushes zlib if last row */
        writeFinishRow();
    }

/* writer row method */
    protected void writeFinishRow () throws PNGException, IOException
    {
        /* next row */
        row_number++;

        /* see if we are done */
        if (row_number < num_rows)
            return;

        /* if interlaced, go to next pass */
        if (interlaced != 0)
        {
            row_number = 0;
            if ((transformations & PNG_INTERLACE) != 0)
            {
                pass++;
            }
            else
            {
                /* loop until we find a non-zero width or height pass */
                do
                {
                    pass++;
                    if (pass >= 7)
                        break;
                    usr_width = (width + png_pass_inc[pass] - 1 -
                                 png_pass_start[pass]) / png_pass_inc[pass];
                    num_rows = (height + png_pass_yinc[pass] - 1 -
                                png_pass_ystart[pass]) / png_pass_yinc[pass];
                    if ((transformations & PNG_INTERLACE) != 0)
                        break;
                } while (usr_width == 0 || num_rows == 0);

            }

            /* reset the row above the image for the next pass */
            if (pass < 7)
            {
                if (prev_row != null)
                    memSet(prev_row, 0, ((int)usr_channels *
                                         (int)usr_bit_depth *
                                         width + 7) >> 3 + 1);
                return;
            }
        }

        /* if we get here, we've just written the last row, so we need
           to flush the compressor */
        zstream.finish();
//   zout_len = 0;
        do
        {
            /* tell the compressor we are done */
            int def_bytes = zstream.deflate (zbuf, zout_len, zbuf_size - zout_len);
            zout_len += def_bytes;
            /* check to see if we need more room */
            if (zout_len >= zbuf_size)
            {
                writeIDAT(zbuf, zbuf_size);
                zout_len = 0;
            }
        } while (!zstream.finished()/*zstream.needsInput()*/);

        /* write any extra space */
        if (zout_len != 0)
        {
            writeIDAT(zbuf, zout_len);
            zout_len = 0;
        }
        zstream.reset();
    }
};

