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

import org.silverpeas.util.ResourceLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the result of a dragNDrop of folders and path in SilverCrawler.
 * @author Ludovic Bertin
 */
public class UploadReport {
  File repositoryPath = null;
  List<UploadItem> items = new ArrayList<>();
  boolean conflictous = false;
  boolean failed = false;
  ResourceLocator resources =
      new ResourceLocator("com.silverpeas.silvercrawler.multilang.silverCrawlerBundle");
  public int nbCopied = 0;
  public int nbIgnored = 0;
  public int nbReplaced = 0;
  boolean forbiddenFolderDetected = false;

  /**
   * @return the forbiddenFolderDetected
   */
  public boolean isForbiddenFolderDetected() {
    return forbiddenFolderDetected;
  }

  /**
   * @param forbiddenFolderDetected the forbiddenFolderDetected to set
   */
  public void setForbiddenFolderDetected(boolean forbiddenFolderDetected) {
    this.forbiddenFolderDetected = forbiddenFolderDetected;
  }

  public boolean isFailed() {
    return failed;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public boolean isConflictous() {
    return conflictous;
  }

  public void setConflictous(boolean conflictous) {
    this.conflictous = conflictous;
  }

  public File getRepositoryPath() {
    return repositoryPath;
  }

  public void setRepositoryPath(File repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  public List<UploadItem> getItems() {
    return items;
  }

  public void addItem(UploadItem item) {
    item.setId(items.size());
    items.add(item);
  }

  public String displayErrors() {
    StringBuilder errorMessage = new StringBuilder();

    if (failed) {
      errorMessage.append(resources.getString("silverCrawler.followingErrorsOccured"))
          .append(" :<br/><br/>");
      for (UploadItem item : items) {
        if (item.isCopyFailed()) {
          errorMessage.append(resources.getString("silverCrawler.copyFailed")).append(" : ")
              .append(item.getRelativePath().getPath()).append(" - ")
              .append(item.getCopyFailedException().getMessage()).append("<br/>");
        }
      }
    }
    return errorMessage.toString();
  }

  public String displaySuccess() {
    StringBuilder successMessage = new StringBuilder();

    if (!failed) {
      successMessage.append(resources.getString("silverCrawler.dragNDropSucceeded"))
          .append(" :<br/><br/>");
      successMessage.append(nbCopied).append(" ")
          .append(resources.getString("silverCrawler.filesCopied")).append("<br/>");
      successMessage.append(nbReplaced).append(" ")
          .append(resources.getString("silverCrawler.filesReplaced")).append("<br/>");
      successMessage.append(nbIgnored).append(" ")
          .append(resources.getString("silverCrawler.filesIgnored")).append("<br/>");
    }
    return successMessage.toString();
  }
}

