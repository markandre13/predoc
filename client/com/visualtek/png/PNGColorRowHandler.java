/*
 * $Id: PNGColorRowHandler.java,v 1.2 1998/07/15 03:37:09 verylong Exp $
 *
 * $Log: PNGColorRowHandler.java,v $
 * Revision 1.2  1998/07/15 03:37:09  verylong
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
 * Revision 1.1  1998/07/14 05:31:32  verylong
 * Classes subtree PNGRowHandler was created. transformRow method moved
 * to appropriate subclasses. Image now stored in byte arrays instead
 * array of integer. It allows to save width*height bytes memory because
 * we don't support alpha channel now. It seems working but full testing
 * didn't completed.
 *
 */
package com.visualtek.png;

/**
 *
 * @author Vadym Tymchenko <verylong@visyaltek.com>
 */
class PNGColorRowHandler extends PNGRowHandler
{
    PNGInfo info;
    int mask;

    private PNGColorRowHandler() { }

    PNGColorRowHandler(PNGInfo png_info)
        throws PNGException
    {
        info = png_info;
        mask = (1 << info.bit_depth) - 1;
        switch (info.bit_depth)
        {
        case 1 :            // 2 colors
        case 2 :            // 4 colors
        case 4 :            // 16 colors
        case 8 :            // 256 colors
            break;
        default:
            throw new PNGException("Not supported number of bit per pixel = " + info.bit_depth);
        }
    }

    void transformRow(byte[] row_buf, int row_index, int pixels[], int index)
        throws PNGException
    {
        int red, green, blue;
        int ibuf = row_index;

        try
        {
            while (ibuf < row_buf.length)
            {
                int v = row_buf[ibuf] & 0xff;
                for (int shift = 8 - info.bit_depth; shift >= 0;
                     shift -= info.bit_depth)
                {
                    int i = (v >> shift) & mask;
                    red   = info.palette[i].getRed  ();
                    green = info.palette[i].getGreen();
                    blue  = info.palette[i].getBlue ();
                    pixels[index++] = 0xff << 24 |  red << 16 | green << 8 | blue;
                }
                ibuf ++;
            }
        }
            catch (IndexOutOfBoundsException e) {}
    }

    void transformRowBytes(byte[] row_buf,
                           byte pixels_red[], byte pixels_green[], 
                           byte pixels_blue[], byte pixels_alpha[], int index)
        throws PNGException
    {
        int ibuf   = 0;

        try
        {

            while (ibuf < row_buf.length)
            {
                int v = row_buf[ibuf] & 0xff;
                for (int shift = 8 - info.bit_depth; shift >= 0;
                     shift -= info.bit_depth)
                {
                    int i = (v >> shift) & mask;
                    //
                    // Alpha channel in this case always 0xff.
                    //
                    pixels_red[index]   = (byte)info.palette[i].getRed();
                    pixels_green[index] = (byte)info.palette[i].getGreen();
                    pixels_blue[index]  = (byte)info.palette[i].getBlue();
                    index++;
                }
                ibuf ++;
            }
        }
        catch (IndexOutOfBoundsException e) {}
    }
}
