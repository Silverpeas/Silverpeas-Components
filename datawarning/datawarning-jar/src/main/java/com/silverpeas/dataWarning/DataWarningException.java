package com.silverpeas.dataWarning;

import com.stratelia.webactiv.util.exception.SilverpeasException;
 
public class DataWarningException extends SilverpeasException
{
    public DataWarningException(String callingClass, int errorLevel, String message)
    {
        super(callingClass, errorLevel, message);
    }

    public DataWarningException(String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public DataWarningException(String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public DataWarningException(String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    public String getModule()
    {
        return "DataWarning";
    }

}
