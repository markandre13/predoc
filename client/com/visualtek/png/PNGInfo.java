/*
 * $Id: PNGInfo.java,v 1.4 1998/05/07 18:36:09 vlad Exp $
 *
 * $Log: PNGInfo.java,v $
 * Revision 1.4  1998/05/07 18:36:09  vlad
 * more brush up
 *
 * Revision 1.3  1998/05/06 22:45:52  vlad
 * png lib brush up
 *
 * Revision 1.2  1997/08/13 01:05:43  lord
 * move some classes from PNG to Cubes
 *
 * Revision 1.1  1997/08/05 17:53:31  lord
 * PNG code added. JDK-1.1.3 is now used
 *
 */

package com.visualtek.png;

import java.awt.Color;

// PNG info
class PNGInfo
{
    // constructor
    public  PNGInfo  () {}
    
    /* the following are necessary for every png file */
    public int      width           = 0;    /* with of file */
    public int      height          = 0;    /* height of file */
    public int      valid           = 0;    /* the PNG_INFO_ defines, OR'd together */
    public int      rowbytes        = 0;    /* bytes needed for untransformed row */
    public Color[]  palette         = null; /* palette of file */
    public short    num_palette     = 0;    /* number of values in palette */
    public short    num_trans       = 0;    /* number of trans values */
    public byte     bit_depth       = 0;    /* 1, 2, 4, 8, or 16 */
    public byte     color_type      = 0;    /* use the PNG_COLOR_TYPE_ defines */
    public byte     compression_type= 0;    /* must be 0 */
    public byte     filter_type     = 0;    /* must be 0 */
    public byte     interlace_type  = 0;    /* 0 for non-interlaced, 1 for interlaced */
    
    /* the following is informational only on read, and not used on writes */
    public byte     channels        = 0;    /* number of channels of data per pixel */
    public byte     pixel_depth     = 0;    /* number of bits per pixel */
    public byte     spare_byte      = 0;    /* To align the data, and for future use */
    public byte[]   signature       = new byte [8]; /* Signature read from start of file */
    
    /*!!! optional information is absent now */
}

