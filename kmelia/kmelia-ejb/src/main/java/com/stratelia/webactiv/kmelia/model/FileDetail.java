/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.stratelia.webactiv.kmelia.model;

import org.silverpeas.util.FileServerUtils;
import org.silverpeas.util.FileUtil;
import org.silverpeas.util.FileRepositoryManager;

public class FileDetail extends Object implements java.io.Serializable {
  private static final long serialVersionUID = -9137458562237749139L;
  private String name;
  private String path;
  private long size;
  private boolean isDirectory;

  public FileDetail(String name, String path, long size, boolean isDirectory) {
    this.name = name;
    this.path = path;
    this.size = size;
    this.isDirectory = isDirectory;
  }

  public String getFileDownloadEstimation() {
    return FileRepositoryManager.getFileDownloadTime(size);
  }

  public String getFileSize() {
    return FileRepositoryManager.formatFileSize(size);
  }

  public String getFileIcon() {
    String icon = "";
    int pointIndex = name.lastIndexOf(".");
    int theLength = name.length();

    if ((pointIndex >= 0) && ((pointIndex + 1) < theLength)) {
      String fileType = name.substring(pointIndex + 1, theLength);
      icon = FileRepositoryManager.getFileIcon(fileType);
    } else {
      icon = FileRepositoryManager.getFileIcon("html");
    }
    return icon;
  }

  public String getFileURL() {
    return FileServerUtils.getUrl(name, path, getMimeType());
  }

  public boolean isIsDirectory() {
    return isDirectory;
  }

  public String getName() {
    return name;
  }

  public String getMimeType() {
    return FileUtil.getMimeType(name);
  }

  public String getPath() {
    return path;
  }

  public long getSize() {
    return size;
  }

}