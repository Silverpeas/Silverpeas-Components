package com.silverpeas.mydb.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * MyDB runtime exception.
 * 
 * @author Antoine HEDIN
 */
public class MyDBRuntimeException
	extends SilverpeasRuntimeException
{ 
	
    public MyDBRuntimeException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public MyDBRuntimeException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public MyDBRuntimeException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public MyDBRuntimeException (String callingClass, int errorLevel, String message, String extraParams,
    	Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    public String getModule()
    {
       return "MyDB";
    }

}