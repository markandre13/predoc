/*
 * $Id: PNGProducer.java,v 1.18 1998/07/15 03:37:11 verylong Exp $
 *
 * $Log: PNGProducer.java,v $
 * Revision 1.18  1998/07/15 03:37:11  verylong
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
 * Revision 1.17  1998/07/14 05:31:32  verylong
 * Classes subtree PNGRowHandler was created. transformRow method moved
 * to appropriate subclasses. Image now stored in byte arrays instead
 * array of integer. It allows to save width*height bytes memory because
 * we don't support alpha channel now. It seems working but full testing
 * didn't completed.
 *
 * Revision 1.16  1998/07/14 00:58:40  verylong
 * Some optimisation in image reading procedure. Warning! Method
 * combineRow() has been excluded since it was never used but probably it
 * will be back when the library functionality will be extended. Is seems
 * still work but it wasn't tested at full PngSuite...
 *
 * Revision 1.15  1998/07/01 22:58:21  verylong
 * Method ubyte() was removed. All calls were modified to in-line
 * functionality of the method.
 *
 * Revision 1.14  1998/06/24 02:32:15  vlad
 * 100% java sertification brush up
 *
 * Revision 1.13  1998/05/07 23:32:35  vlad
 * Some changes in PNG usage.
 *
 * Revision 1.12  1998/05/06 22:45:52  vlad
 * png lib brush up
 *
 * Revision 1.11  1998/04/15 00:10:29  vlad
 * Brush up
 *
 * Revision 1.10  1998/03/07 03:54:46  vlad
 * Some changes in ChatArea, Thicknes indicatior, URL parser, PNG library
 *
 * Revision 1.9  1998/02/21 01:03:21  vb
 * Supports grayscaled PNG
 *
 * Revision 1.8  1998/02/20 05:21:49  vb
 * Supports 4,16 color models
 *
 * Revision 1.7  1997/09/02 20:45:30  lord
 * workaround for IE3.0. Ditry hack
 *
 * Revision 1.6  1997/08/28 23:10:56  vlad
 * DataFormatException doesn't throw up from PNG any more
 *
 * Revision 1.5  1997/08/21 02:00:13  vlad
 * Some Pointer related bugs have been fixed
 *
 * Revision 1.4  1997/08/13 01:53:56  lord
 * PNGProducer moved back to PNG package.
 *
 * Revision 1.1  1997/08/13 01:05:44  lord
 * move some classes from PNG to Cubes
 *
 * Revision 1.1  1997/08/05 17:53:31  lord
 * PNG code added. JDK-1.1.3 is now used
 *
 */

package com.visualtek.png;

import java.awt.image.*;
import java.io.*;
import java.util.Hashtable;
import java.util.zip.*;

 /**
  *
  */
public class PNGProducer implements ImageProducer
{
    byte             pixels_red[];
    byte             pixels_green[];
    byte             pixels_blue[];
    byte             pixels_alpha[]; // Only if the image has alpha channel

    PNGInfo          info;
    PNGRowHandler    rowHandler;

    private ColorModel      colorModel = ColorModel.getRGBdefault();
    private ImageConsumer   theConsumer;
    private boolean initialised=false;

    /**
      * Constructs an unitialised ImageProducer object.
      */
    public PNGProducer () throws PNGException, IOException
    {
        initialised=false;
    }

    /**
      * Constructs an ImageProducer object from an PNG bitmap
      * to produce data for an Image object.
      * @param istream PNG input stream
      */
    public PNGProducer ( InputStream istream ) throws PNGException,
                                                      IOException
    {
        init(istream);
        initialised=true;;
    }

    public void init ( InputStream istream ) 
        throws PNGException, IOException
    {
        PNGDataDecoder png_decoder = new PNGDataDecoder (istream);
        info = png_decoder.readInfo ();

        if (info.interlace_type != 0)
            throw new PNGException ("Interlaced PNG image not supported.");

        // rows proccessing
        int pixels_num = info.width * info.height;

        pixels_red   = new byte[pixels_num];
        pixels_green = new byte[pixels_num];
        pixels_blue  = new byte[pixels_num];
//        At current time alpha channel is not supported and always is 0xff.
//        pixels_alpha  = new byte[pixels_num];

        byte[] row_buf = new byte [info.rowbytes];
        png_decoder.readStartRow();

        for (int index = 0; index < pixels_num; index += info.width)
        {
            png_decoder.readRow(row_buf, 0);
            ((PNGRowHandler)png_decoder.row_info).transformRowBytes(row_buf, pixels_red, pixels_green, pixels_blue, pixels_alpha, index);
        }
        PNGInfo png_end_info = png_decoder.readEnd();
    }

    /**
      * Adds an ImageConsumer to the list of consumers interested in
      * data for this image.
      * @see ImageConsumer
      */
    public synchronized void addConsumer(ImageConsumer ic)
    {
        theConsumer = ic;
        produce();
        theConsumer = null;
    }

    /**
      * Determine if an ImageConsumer is on the list of consumers currently
      * interested in data for this image.
      * @return true if the ImageConsumer is on the list; false otherwise
      * @see ImageConsumer
      */
    public synchronized boolean isConsumer(ImageConsumer ic)
    {
        return (ic == theConsumer);
    }

    /**
      * Remove an ImageConsumer from the list of consumers interested in
      * data for this image.
      * @see ImageConsumer
      */
    public synchronized void removeConsumer(ImageConsumer ic)
    {
        if (theConsumer == ic)
        {
            theConsumer = null;
        }
    }

    /**
      * Adds an ImageConsumer to the list of consumers interested in
      * data for this image, and immediately start delivery of the
      * image data through the ImageConsumer interface.
      * @see ImageConsumer
      */
    public void startProduction(ImageConsumer ic)
    {
        addConsumer(ic);
    }

    /**
      * Requests that a given ImageConsumer have the image data delivered
      * one more time in top-down, left-right order.
      * @see ImageConsumer
      */
    public void requestTopDownLeftRightResend(ImageConsumer ic)
    {
        // Noting to do.  The data is always in TDLR format.
    }

    /*
     * Produce Image data.
     * I.e set the data for the Image, using the ImageConsumer interface.
     */
    private void produce()
    {
        ImageConsumer consumer = theConsumer;
        
        if(consumer != null)
        {
            consumer.setDimensions(info.width, info.height);
            consumer.setProperties(new Hashtable());
            consumer.setColorModel(colorModel);
            consumer.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
                              ImageConsumer.COMPLETESCANLINES |
                              ImageConsumer.SINGLEPASS |
                              ImageConsumer.SINGLEFRAME);
            // Fill selected imageConsumer
             int row[] = new int[info.width]; 
             int index = 0;
             for (int row_num = 0; row_num < info.height; row_num++) 
                 {
                     for (int pix_num = 0; pix_num < info.width; pix_num++, index++)
                         row[pix_num] = (0xff << 24                        | 
                                         (pixels_red[index] & 0xff) << 16  | 
                                         (pixels_green[index] & 0xff) << 8 | 
                                         (pixels_blue[index] & 0xff));
            
                     consumer.setPixels(0, row_num, info.width, 1, colorModel,
                                        row, 0, info.width);
                 }
            consumer.imageComplete(ImageConsumer.STATICIMAGEDONE);
        }
    }
}
