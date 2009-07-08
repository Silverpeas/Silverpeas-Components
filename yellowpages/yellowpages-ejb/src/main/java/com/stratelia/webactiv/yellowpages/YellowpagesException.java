package com.stratelia.webactiv.yellowpages;

import com.stratelia.webactiv.util.exception.*;

public class YellowpagesException extends SilverpeasException
{
    /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public YellowpagesException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public YellowpagesException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public YellowpagesException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public YellowpagesException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**--------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule() {
       return "yellowpages";
    }

}
