/*
 * $Id: PNGTrueColorRowHandler.java,v 1.2 1998/07/15 03:37:12 verylong Exp $
 *
 * $Log: PNGTrueColorRowHandler.java,v $
 * Revision 1.2  1998/07/15 03:37:12  verylong
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
class PNGTrueColorRowHandler extends PNGRowHandler
{
    PNGInfo info;
    int size;
    boolean hasAlpha;

    private PNGTrueColorRowHandler() { }

    PNGTrueColorRowHandler(PNGInfo png_info)
        throws PNGException
    {
        info = png_info;
        hasAlpha = ((info.color_type & PNGData.PNG_COLOR_MASK_ALPHA)
                    == PNGData.PNG_COLOR_MASK_ALPHA);
        if (info.bit_depth == 8)       size = 1;
        else if (info.bit_depth == 16) size = 2;
        else 
            throw new PNGException("Not supported number of bit per pixel = " + info.bit_depth);
    }

    void transformRow(byte[] row_buf, int row_index, int pixels[], int index)
        throws PNGException
    {
        int red, green, blue;
        int ibuf   = row_index;
        
        try
        {
            while (ibuf < row_buf.length)
            {
                red   = row_buf[ibuf] & 0xff; ibuf += size;
                green = row_buf[ibuf] & 0xff; ibuf += size;
                blue  = row_buf[ibuf] & 0xff; ibuf += size;
                if (hasAlpha)
                {
                    ibuf += size;
                }
                pixels[index++] = 0xff << 24 |  red << 16 | green << 8 | blue;
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
                pixels_red[index]   = (byte)(row_buf[ibuf] & 0xff); ibuf += size;
                pixels_green[index] = (byte)(row_buf[ibuf] & 0xff); ibuf += size;
                pixels_blue[index]  = (byte)(row_buf[ibuf] & 0xff); ibuf += size;
                //
                // We doesn't support alpha channel now and it always is 0xff.
                //
                if (hasAlpha)
                {
                    ibuf += size;
                }
                index++;
            }
        }
        catch (IndexOutOfBoundsException e) {}
    }
}
