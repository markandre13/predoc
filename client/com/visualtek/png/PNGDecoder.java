/*
 * $Id: PNGDecoder.java,v 1.5 1998/05/06 22:45:51 vlad Exp $
 */

package com.visualtek.png;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

/**
 *  PNG decoder.
 *
 *  @author Vlad Karpinsky <vlad@visualtek.com>
 */
public class PNGDecoder implements ImageObserver
{
    public static final int IMAGE_IS_NOT_READY = 0;
    public static final int IMAGE_OK           = 1;
    public static final int IMAGE_ERROR        = 2;
    
    private int         Flags;
    private PNGProducer Producer;
    private InputStream In;
    private boolean     CloseIn;
    
    /**
     *  Constructs PNG decoder
     *
     *  @param String filename - name of file with PNG image 
     */
    public PNGDecoder(String filename)
        throws IOException, FileNotFoundException
    {
        this(new FileInputStream(filename));
        CloseIn = true;
    }

    /**
     *  Constructs PNG encoder
     *
     *  @param URL url - URL with PNG image
     */
    public PNGDecoder(URL url)
        throws IOException
    {
        this(url.openStream());
        CloseIn = true;
    }

    /**
     *  Constructs PNG encoder
     *
     *  @param InputStream in -  input stream with PNG image
     */
    public PNGDecoder(InputStream in)
        throws IOException
    {
        In       = in;
        CloseIn  = false;
        Producer = new PNGProducer(in);
    }

    /**
     *  Reads and decodes PNG image
     *
     *  @return Image - created image
     */
    public Image decode()
        throws PNGException, IOException
    {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image   img     = toolkit.createImage(Producer);
        
        if(toolkit.prepareImage(img, -1, -1, this) == false)
        {
            while(status() == IMAGE_IS_NOT_READY)
            {
                try
                {
                    synchronized(this)
                    {
                        wait();
                    }
                }
                catch (InterruptedException e){}
            }
        }
        if(status() == IMAGE_ERROR)
            throw new PNGException("Error image loading");

        if(CloseIn && In != null)
            In.close();

        return img;
    }
    
    /**
     * Is called when previously requested information about an image becomes available.
     *
     * @see java.awt.image.ImageObserver
     */
    public boolean imageUpdate(Image img, int infoflags,
                               int  x, int  y, int  width, int height)
    {
        Flags             = infoflags;
        boolean not_ready = (status() == IMAGE_IS_NOT_READY);
        if(!not_ready)
        {
            synchronized(this)
            {
                notify();
            }
        }
        return not_ready;
    }
    
    /**
     * Returns status of the image.
     *
     */
    public int status()
    {
        return (((Flags & ERROR) != 0)? IMAGE_ERROR:
                (((Flags & ALLBITS) != 0)? IMAGE_OK: IMAGE_IS_NOT_READY));
    }
}
