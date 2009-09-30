/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.survey.control;

import com.silverpeas.util.web.servlet.FileUploadUtil;
import org.apache.commons.fileupload.FileItem;

/**
 * 
 * @author ehugonnet
 */
public class FileHelper {

  public static boolean isCorrectFile(FileItem filePart) {
    String fileName = FileUploadUtil.getFileName(filePart);
    boolean correctFile = false;
    if (fileName != null) {
      String logicalName = fileName.trim();
      if (logicalName != null) {
        if ((logicalName.length() >= 3) && (logicalName.indexOf("*") == -1)
            && (logicalName.indexOf(".") != -1)) {
          String type = logicalName.substring(logicalName.indexOf(".") + 1,
              logicalName.length());
          if (type.length() >= 3) {
            correctFile = true;
          }
        }
      }
    }
    return correctFile;
  }
}
