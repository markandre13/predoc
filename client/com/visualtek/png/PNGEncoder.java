/*
 * $Id: PNGEncoder.java,v 1.12 1998/07/15 03:37:11 verylong Exp $
 */

package com.visualtek.png;

import java.util.Hashtable;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *  PNG Encoder.
 *
 *  @author Vlad Karpinsky <vlad@visualtek.com>
 */
public class PNGEncoder implements ImageConsumer
{
    PNGDataEncoder Encoder;
    PNGInfo        Info;
    ImageProducer  Producer;  
    int            HintFlags;
    int            Width;
    int            Height;
    boolean        InProgress;
    PNGException   EncodeException;
    IOException    OutputException;
    OutputStream   Out;
    boolean        CloseOut;
    byte []        RawBuffer;
    
    /**
     * Constructs PNG empty encoder
     */
    public PNGEncoder()
    {
    }
    
    /**
     * Constructs PNG encoder
     *
     * @param Image image     - Image to encode 
     * @param String filename - name of file to write PNG image 
     * @exception IOException
     */
    public PNGEncoder(Image img, String filename)
        throws IOException
    {
        this(img.getSource(), new FileOutputStream(filename));
        CloseOut = true;
    }
    
   /**
     * Constructs PNG encoder
     *
     * @param Image image     - Image to encode 
     * @param OutputStrem out - output stream to write PNG image 
     */
    public PNGEncoder(Image img, OutputStream out)
    {
        this(img.getSource(), out);
    }
    
   /**
     * Constructs PNG encoder
     *
     * @param ImageProducer ip  - image producer of the image to encode 
     * @param OutputStrem   out - output stream to write PNG image 
     */
    public PNGEncoder(ImageProducer ip, OutputStream out)
    {
        setOutput(out);
        setImage(ip);
        InProgress = false;
    }
    
    /**
     * Sets image producer of the image to encode
     *
     * @param ImageProducer ip - image producer of the image to encode 
     */
    public void setImage(ImageProducer ip)
    {
        Producer = ip;
    }

    /**
     * Sets image to encode
     *
     * @param ImageProducer ip - image producer of the image to encode 
     */
    public void setImage(Image img)
    {
        if(img != null)
            Producer = img.getSource();
    }
    
    /**
     * Sets output stream for encoded image
     *
     * @param OutputStream out - output stream for encoded image 
     */
    public void setOutput(OutputStream out)
    {
        Out      = out;
        CloseOut = false;
    }

    /**
     * Strats encoding
     */
    public void encode()
        throws PNGException, IOException
    {
        if (Producer != null && Out != null)
        {
            EncodeException = null;
            OutputException = null;
            Encoder         = new PNGDataEncoder(Out);
            Info            = new PNGInfo();
            RawBuffer       = null;
            HintFlags       = 0;
            Width           = 0;
            Height          = 0;

            InProgress = true;
            
            Producer.startProduction(this);
            
            while(InProgress)
            {
                synchronized(this)
                {
                    try
                    {
                        wait();
                    }
                    catch(InterruptedException e){}
                }
            }
            if(EncodeException != null)
                throw EncodeException;
            if(OutputException != null)
                throw OutputException;
            
            if(Out != null && CloseOut)
            {
                Out.flush();
                Out.close();
            }
        }
    }
    /**
     * Sets width and height of this image
     * Implements java.awt.image.ImageConsumer.setDimensions() method
     */
    public void setDimensions(int width, int height)
    {
        Width  = width;
        Height = height;
    }

    /**
     * Sets the extensible list of properties associated with this image
     * Implements java.awt.image.ImageConsumer.setProperties() method
     */
    public void setProperties(Hashtable props)
    {
        // Ignore.
    }

    /**
     * Sets the color model for this image
     * Implements java.awt.image.ImageConsumer.setColorModel() method
     */
    public void setColorModel(ColorModel model)
    {
        // Ignore.
    }

    /**
     * Sets the hints for this image
     * Implements java.awt.image.ImageConsumer.setHints() method
     */
    public void setHints( int hintflags)
    {
        HintFlags = hintflags;
    }

    
    /**
     * Sets the pixels of this image. The pixels are all stored as bytes.
     * Implements java.awt.image.ImageConsumer.setPixels() method
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, byte[] pixels, int off, int scansize )
    {
        try
        {
            int row, row_off = off, index, col, pixel, pix_off;

            if (RawBuffer == null)
                RawBuffer = new byte[Width*Height*3];

            for (row = 0; row < h; row++)
            {
                index = x + (y + row) * Width * 3;
                pix_off = row_off;
                for (col = 0; col < w; col++)
                {
                    pixel = 0xff & pixels[pix_off++]; // ???
                    RawBuffer[index++] = (byte)model.getRed  (pixel);
                    RawBuffer[index++] = (byte)model.getGreen(pixel);
                    RawBuffer[index++] = (byte)model.getBlue (pixel);
                }
                row_off += scansize;
            }
        }
        catch(Exception e)
        {
            EncodeException = new PNGException(e.toString());
            encodeDone();
        }
    }   
    
    /**
     * Sets the pixels of this image. The pixels are all stored as ints.
     * Implements java.awt.image.ImageConsumer.setPixels() method
     */
    public void setPixels(int x, int y, int w, int h,
                          ColorModel model, int[] pixels, int off, int scansize)
    {
        int row, row_off = off, index, col, pixel, pix_off;

        try
        {
            if (RawBuffer == null)
                RawBuffer = new byte[Width*Height*3]; // whhere is alpha?

            for (row = 0; row < h; row++)
            {
                index = x + (y + row) * Width * 3;
                pix_off = row_off;
                for (col = 0; col < w; col++)
                {
                    pixel = pixels[pix_off++];
                    RawBuffer[index++] = (byte)model.getRed  (pixel);
                    RawBuffer[index++] = (byte)model.getGreen(pixel);
                    RawBuffer[index++] = (byte)model.getBlue (pixel);
                }
                row_off += scansize;
            }
        }
        catch(Exception e)
        {
            EncodeException = new PNGException(e.toString());
            encodeDone();
        }
    }
    
    /**
     * Sets the image status.
     * Implements java.awt.image.ImageConsumer.imageComplete() method
     */
    public void imageComplete(int status)
    {
        Producer.removeConsumer(this);
        if((status & ImageConsumer.IMAGEABORTED ) != 0)
        {
            OutputException = new IOException("Image transfer aborted");
        }
        else if (RawBuffer != null && Width > 0)
        {
            try
            {
              // Fill PNG header
                Info.width      = Width;
                Info.height     = Height;
                Info.bit_depth  = 8;
            
                // True color PNG settings
                Info.color_type = (byte)PNGData.PNG_COLOR_TYPE_RGB;
                Info.channels   = 3;
                Encoder.writeInfo(Info);
                
                // Writes data
                int row_buf_size = Width*3;
                for (int col = 0; col < Height; col++)
                {
                    Encoder.writeRow(RawBuffer, col*row_buf_size, row_buf_size);
                }
                Encoder.writeEnd(Info);
            }
            catch (PNGException e)
            {
                EncodeException = e;
            }
            catch(IOException e)
            {
                OutputException = e;
            }
            catch(Exception e)
            {
                EncodeException = new PNGException(e.toString());
            }
        }
        else
        {
            EncodeException = new PNGException("Null image");
        }
         
        encodeDone();
    }

    /**
     * Stops encoding proccess. For internal usage.
     */
    protected synchronized void encodeDone()
    {
        InProgress = false;
        notify();
    }
}
