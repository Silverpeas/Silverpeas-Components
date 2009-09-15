/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.newsEdito;

import com.stratelia.webactiv.util.exception.SilverpeasException;


/**
 * Non runtime exception management for this jobPeas.
 * @author Sébastien Antonio
 */
public class NewsEditoException extends SilverpeasException
{

    // constructors

    /**
     * Constructor which calls the super constructor
     * @param callingClass (String) the name of the module which catchs the Exception
     * @param errorLevel (int) the level error of the exception
     * @param message (String) the level of the exception label
     * @param extraParams (String) the generic exception message
     * @param nested (Exception) the exception catched
     */
    public NewsEditoException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
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
    public NewsEditoException(String callingClass, int errorLevel, String message, String extraParams)
    {
        this(callingClass, errorLevel, message, extraParams, null);
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
    public NewsEditoException(String callingClass, int errorLevel, String message, Exception nested)
    {
        this(callingClass, errorLevel, message, "", nested);
    }

    /**
     * Constructor declaration
     * 
     * 
     * @param callingClass
     * @param errorLevel
     * @param message
     * 
     * @see
     */
    public NewsEditoException(String callingClass, int errorLevel, String message)
    {
        this(callingClass, errorLevel, message, "", null);
    }


    // 
    // public methods
    // 

    /**
     * Returns the name of this jobPeas
     * @return the name of this module
     */
    public String getModule()
    {
        return "NewsEdito";
    }

}
