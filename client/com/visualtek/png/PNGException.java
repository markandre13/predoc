/*
 * $Id: PNGException.java,v 1.4 1998/05/07 18:36:09 vlad Exp $
 */

package com.visualtek.png;

import java.io.IOException;

/**
 * class PNGException
 * Thrown from PNGProducer in case of errors during decoding/encoding image
 */

public class PNGException extends IOException
{
    public  PNGException() {}
    
    public  PNGException(String what)
    {
        super(what);
    }

}

