package com.silverpeas.silvercrawler.model;

import com.silverpeas.silvercrawler.util.FileServerUtils;
import com.silverpeas.util.FileUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;

public class FileDetail extends Object implements java.io.Serializable {
  private String name;
  private String path;
  private long size;
  private boolean isDirectory;
  private boolean isIndexed;

  public FileDetail(String name, String path, long size, boolean isDirectory) {
    this.name = name;
    this.path = path;
    this.size = size;
    this.isDirectory = isDirectory;
  }

  public FileDetail(String name, String path, long size, boolean isDirectory,
      boolean isIndexed) {
    this.name = name;
    this.path = path;
    this.size = size;
    this.isDirectory = isDirectory;
    this.isIndexed = isIndexed;
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
      icon = FileRepositoryManager.getFileIcon(true, fileType);
    } else {
      icon = FileRepositoryManager.getFileIcon("html");
    }
    return icon;
  }

  public String getFileURL(String userId, String componentId) {
    return FileServerUtils.getUrl(name, path, getMimeType(), userId,
        componentId);
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

  public boolean isIsIndexed() {
    return isIndexed;
  }

}