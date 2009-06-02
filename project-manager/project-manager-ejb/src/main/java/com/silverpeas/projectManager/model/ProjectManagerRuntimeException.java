package com.silverpeas.projectManager.model;

import com.stratelia.webactiv.util.exception.*;

public class ProjectManagerRuntimeException extends SilverpeasRuntimeException
{ 
  /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public ProjectManagerRuntimeException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public ProjectManagerRuntimeException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public ProjectManagerRuntimeException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public ProjectManagerRuntimeException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**--------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule() {
       return "projectManager";
    }

}