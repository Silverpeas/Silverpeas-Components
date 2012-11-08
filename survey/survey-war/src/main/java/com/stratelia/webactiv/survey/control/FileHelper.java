/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.survey.control;

import com.silverpeas.util.web.servlet.FileUploadUtil;
import org.apache.commons.fileupload.FileItem;

/**
 * @author ehugonnet
 */
public class FileHelper {

  public static boolean isCorrectFile(FileItem filePart) {
    String fileName = FileUploadUtil.getFileName(filePart);
    boolean correctFile = false;
    if (fileName != null) {
      String logicalName = fileName.trim();
      if (logicalName != null) {
        if ((logicalName.length() >= 3) && (logicalName.indexOf('*') == -1)
            && (logicalName.indexOf('.') != -1)) {
          String type = logicalName.substring(logicalName.indexOf('.') + 1,
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
