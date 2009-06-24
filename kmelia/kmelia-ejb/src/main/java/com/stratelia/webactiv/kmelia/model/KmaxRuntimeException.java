/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/*
 * CVS Informations
 *
 * $Id: KmaxRuntimeException.java,v 1.2 2007/04/20 14:31:14 neysseri Exp $
 *
 * $Log: KmaxRuntimeException.java,v $
 * Revision 1.2  2007/04/20 14:31:14  neysseri
 * no message
 *
 * Revision 1.1.2.1  2007/01/08 15:55:09  dlesimple
 * Fusion kmax
 *
 * Revision 1.1.1.1  2002/08/06 14:47:57  nchaix
 * no message
 *
 * Revision 1.3  2002/01/22 11:04:46  mguillem
 * Stabilisation Lot2
 * Réorganisation des Router et SessionController
 * Suppression dans les fichiers *Exception de 'implements FromModule'
 *
 */

/**
 * Class declaration
 *
 *
 * @author
 */
public class KmaxRuntimeException extends SilverpeasRuntimeException
{

    /**
     * --------------------------------------------------------------------------constructors
     * constructors
     */
    public KmaxRuntimeException(String callingClass, int errorLevel, String message)
    {
        super(callingClass, errorLevel, message);
    }

    /**
     * Constructor declaration
     *
     *
     * @param callingClass
     * @param errorLevel
     * @param message
     * @param extraParams
     *
     * @see
     */
    public KmaxRuntimeException(String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    /**
     * Constructor declaration
     *
     *
     * @param callingClass
     * @param errorLevel
     * @param message
     * @param nested
     *
     * @see
     */
    public KmaxRuntimeException(String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    /**
     * Constructor declaration
     *
     *
     * @param callingClass
     * @param errorLevel
     * @param message
     * @param extraParams
     * @param nested
     *
     * @see
     */
    public KmaxRuntimeException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String getModule()
    {
        return "kmax";
    }

}

