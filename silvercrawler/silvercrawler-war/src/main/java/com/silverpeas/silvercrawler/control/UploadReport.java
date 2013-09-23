package com.silverpeas.silvercrawler.control;

import com.stratelia.webactiv.util.ResourceLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the result of a dragNDrop of folders and path in SilverCrawler.
 * @author Ludovic Bertin
 */
public class UploadReport {
  File repositoryPath = null;
  List<UploadItem> items = new ArrayList<UploadItem>();
  boolean conflictous = false;
  boolean failed = false;
  String language = null;
  ResourceLocator resources =
      new ResourceLocator("com.silverpeas.silvercrawler.multilang.silverCrawlerBundle", language);
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
    StringBuffer errorMessage = new StringBuffer();

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
    StringBuffer successMessage = new StringBuffer();

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

