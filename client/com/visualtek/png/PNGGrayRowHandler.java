/*
 * $Id: PNGGrayRowHandler.java,v 1.2 1998/07/15 03:37:11 verylong Exp $
 *
 * $Log: PNGGrayRowHandler.java,v $
 * Revision 1.2  1998/07/15 03:37:11  verylong
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
class PNGGrayRowHandler extends PNGRowHandler
{
    PNGInfo info;
    int size = 0;
    int mask;                   // we need to initialise these vars only 
    int color_off;              // if size != 0.

    private PNGGrayRowHandler () {}

    PNGGrayRowHandler(PNGInfo png_info)
        throws PNGException
    {
        info = png_info;
        if (info.bit_depth == 16 || info.bit_depth == 8)
            size = (info.bit_depth == 16) ? 2 : 1;
        else {
            mask = (1 << info.bit_depth) - 1;
            color_off = (info.bit_depth == 1) ? 255 :
                ((info.bit_depth == 2) ? 85 : 17);
        }
    }


    void transformRow(byte[] row_buf, int row_index, int pixels[], int index)
        throws PNGException
    {
        int ibuf   = row_index;

        try
        {
            if (size != 0)
            {
                while (ibuf < row_buf.length)
                {
                    int i = row_buf[ibuf] & 0xff;
                    ibuf += size;
                    if (info.color_type == PNGData.PNG_COLOR_TYPE_GRAY_ALPHA)
                        ibuf += size;
                    // 0xff << 24 | red << 16 | green << 8 | blue
                    pixels[index++] = 0xff << 24 | i << 16 | i << 8 | i;
                }
            }
            else
            {
                while (ibuf < row_buf.length)
                {
                    int v = row_buf[ibuf] & 0xff;
                    for (int shift = 8 - info.bit_depth; shift >= 0;
                         shift-=info.bit_depth)
                    {
                        int i = (((v >> shift) & mask) * color_off);
                        // 0xff << 24 | red << 16 | green << 8 | blue
                        pixels[index++] = 0xff << 24 |  i << 16 | i << 8 | i;
                    }
                    ibuf ++;
                }
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
            if (size != 0)
            {
                while (ibuf < row_buf.length)
                {
                    pixels_red[index] = row_buf[ibuf];
                    pixels_green[index] = row_buf[ibuf];
                    pixels_blue[index] = row_buf[ibuf];

                    // We skip alpha data in case of gray image. 
                    // It is always 0xff.

                    if (info.color_type == PNGData.PNG_COLOR_TYPE_GRAY_ALPHA)
                        ibuf += size;
                    ibuf += size;
                    index++;
                }
            }
            else
            {
                 while (ibuf < row_buf.length)
                 {
                     int v = row_buf[ibuf] & 0xff;
                     for (int shift = 8 - info.bit_depth; shift >= 0;
                          shift-=info.bit_depth)
                     {
                         byte i = (byte)((((v >> shift) & mask) * color_off) & 0xff);
                         // We skip alpha data in case of gray image. 
                         // It is always 0xff.
                         pixels_red[index] =   i;
                         pixels_green[index] = i;
                         pixels_blue[index] =  i;
                         index++;
                     }
                     ibuf++;
                 }
            }
        }
        catch (IndexOutOfBoundsException e) {}
    }
}
