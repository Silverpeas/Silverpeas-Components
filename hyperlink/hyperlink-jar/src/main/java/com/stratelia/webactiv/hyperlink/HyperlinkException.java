package com.stratelia.webactiv.hyperlink;

import com.stratelia.webactiv.util.exception.*;

public class HyperlinkException extends SilverpeasException
{
	/**--------------------------------------------------------------------------constructors
	 * constructors
	 */
	public HyperlinkException (String callingClass, int errorLevel, String message)
	{
	   super(callingClass, errorLevel, message);
	}

	public HyperlinkException (String callingClass, int errorLevel, String message, String extraParams)
	{
		super(callingClass, errorLevel, message, extraParams);
	}

    public HyperlinkException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public HyperlinkException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

	/**--------------------------------------------------------------------------getModule
	 * getModule
	 */
	public String getModule() {
	   return "hyperlink";
	}

}
