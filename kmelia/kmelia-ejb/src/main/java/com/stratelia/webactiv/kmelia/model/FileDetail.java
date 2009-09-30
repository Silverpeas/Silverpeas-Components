package com.stratelia.webactiv.kmelia.model;

import com.stratelia.webactiv.util.FileRepositoryManager;

public class FileDetail extends Object implements java.io.Serializable {
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
    return com.stratelia.webactiv.util.FileServerUtils.getUrl(name, path,
        getMimeType());
  }

  public boolean isIsDirectory() {
    return isDirectory;
  }

  public String getName() {
    return name;
  }

  public String getMimeType() {
    return com.stratelia.webactiv.util.attachment.control.AttachmentController
        .getMimeType(name);
  }

  public String getPath() {
    return path;
  }

  public long getSize() {
    return size;
  }

}