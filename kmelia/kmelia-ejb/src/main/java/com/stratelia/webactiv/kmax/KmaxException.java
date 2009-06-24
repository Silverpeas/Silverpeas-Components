package com.stratelia.webactiv.kmax;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class KmaxException extends SilverpeasException
{
	/**--------------------------------------------------------------------------constructors
	 * constructors
	 */
	public KmaxException (String callingClass, int errorLevel, String message)
	{
	   super(callingClass, errorLevel, message);
	}

	public KmaxException (String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

    public KmaxException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public KmaxException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

	/**--------------------------------------------------------------------------getModule
	 * getModule
	 */
	public String getModule() {
	   return "kmax";
	}

}
