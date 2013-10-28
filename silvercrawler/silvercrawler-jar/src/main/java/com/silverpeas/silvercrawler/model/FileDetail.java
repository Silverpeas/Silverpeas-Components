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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.silvercrawler.model;

import com.silverpeas.silvercrawler.util.FileServerUtils;
import com.stratelia.webactiv.util.FileRepositoryManager;

public class FileDetail implements java.io.Serializable {

  private static final long serialVersionUID = 4697608390797941792L;
  private String name;
  private String path;
  private String fullPath;
  private long size;
  private boolean isDirectory;
  private boolean isIndexed;

  public FileDetail(String name, String path, String fullPath, long size, boolean isDirectory) {
    this.name = name;
    this.path = path;
    this.fullPath = fullPath;
    this.size = size;
    this.isDirectory = isDirectory;
  }

  public FileDetail(String name, String path, String fullPath, long size, boolean isDirectory,
      boolean isIndexed) {
    this.name = name;
    this.path = path;
    this.fullPath = fullPath;
    this.size = size;
    this.isDirectory = isDirectory;
    this.isIndexed = isIndexed;
  }

  public String getFileSize() {
    return FileRepositoryManager.formatFileSize(size);
  }

  public String getFileIcon() {
    String fileType = FileRepositoryManager.getFileExtension(name);
    return FileRepositoryManager.getFileIcon(fileType);
  }

  public String getFileURL(String componentId) {
    return FileServerUtils.getSilverCrawlerUrl(name, path, componentId);
  }

  public boolean isIsDirectory() {
    return isDirectory;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public long getSize() {
    return size;
  }

  public boolean isIsIndexed() {
    return isIndexed;
  }
  
  public String getDirectURL() {
	  if (fullPath.startsWith("/")) {
		  return "file://"+fullPath;
	  }
	  return "file:///"+fullPath;
  }
}