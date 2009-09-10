package com.silverpeas.gallery.model;

import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class GalleryRuntimeException extends SilverpeasRuntimeException
{ 
  /**--------------------------------------------------------------------------constructors
     * constructors
     */
    public GalleryRuntimeException (String callingClass, int errorLevel, String message)
    {
       super(callingClass, errorLevel, message);
    }

    public GalleryRuntimeException (String callingClass, int errorLevel, String message, String extraParams)
    {
        super(callingClass, errorLevel, message, extraParams);
    }

    public GalleryRuntimeException (String callingClass, int errorLevel, String message, Exception nested)
    {
        super(callingClass, errorLevel, message, nested);
    }

    public GalleryRuntimeException (String callingClass, int errorLevel, String message, String extraParams, Exception nested)
    {
        super(callingClass, errorLevel, message, extraParams, nested);
    }

    /**--------------------------------------------------------------------------getModule
     * getModule
     */
    public String getModule() {
       return "gallery";
    }

}
