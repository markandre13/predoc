/*
 * $Id: PNGRowHandler.java,v 1.2 1998/07/15 03:37:11 verylong Exp $
 *
 * $Log: PNGRowHandler.java,v $
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
abstract class PNGRowHandler
{
    public int      width       = 0;    /* width of row */
    public int      rowbytes    = 0;    /* number of bytes in row */
    public byte     color_type  = 0;    /* color type of row */
    public byte     bit_depth   = 0;    /* bit depth of row */
    public byte     channels    = 0;    /* number of channels (1, 2, 3, or 4) */
    public byte     pixel_depth = 0;    /* bits per pixel (depth * channels) */

    abstract void transformRow(byte[] row_buf, int row_index, int pixels[], int index)
        throws PNGException;
    abstract void transformRowBytes(byte[] row_buf, 
                                    byte pixels_red[], byte pixels_green[], 
                                    byte pixels_blue[], byte pixels_alpha[], int index)
        throws PNGException;
}
