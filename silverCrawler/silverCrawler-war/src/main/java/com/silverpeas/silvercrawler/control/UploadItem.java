/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.silvercrawler.control;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * This class is the result of a dragNDrop of folders and path in SilverCrawler.
 * @author Ludovic Bertin
 */
public class UploadItem {
  int id = -1;
  String fileName = null;
  File parentRelativePath = null;
  boolean itemAlreadyExists = false;
  boolean replace = false;
  private boolean copyFailed;
  private IOException copyFailedException;

  private String getFileName() {
    return fileName;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public File getRelativePath() {
    return FileUtils.getFile(parentRelativePath, getFileName());
  }

  public void setParentRelativePath(File parentRelativePath) {
    this.parentRelativePath = parentRelativePath;
  }

  public boolean isItemAlreadyExists() {
    return itemAlreadyExists;
  }

  public void setItemAlreadyExists(boolean itemAlreadyExists) {
    this.itemAlreadyExists = itemAlreadyExists;
  }

  public boolean isReplace() {
    return replace;
  }

  public void setReplace(boolean replace) {
    this.replace = replace;
  }

  public void setCopyFailed(IOException e) {
    this.copyFailed = true;
    this.copyFailedException = e;
  }

  public boolean isCopyFailed() {
    return copyFailed;
  }

  public IOException getCopyFailedException() {
    return copyFailedException;
  }

  public void setCopyFailedException(IOException copyFailedException) {
    this.copyFailedException = copyFailedException;
  }

}
