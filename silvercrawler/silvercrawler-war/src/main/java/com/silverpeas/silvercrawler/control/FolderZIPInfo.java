package com.silverpeas.silvercrawler.control;

/**
 * Folder ZIP Information.
 *
 * @author Ludovic Bertin
 *
 */
public class FolderZIPInfo {
  String fileZip = null;
  long size = 0;
  long maxiSize = 0;
  String url = null;

  public String getFileZip() {
    return fileZip;
  }

  public void setFileZip(String fileZip) {
    this.fileZip = fileZip;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public long getMaxiSize() {
    return maxiSize;
  }

  public void setMaxiSize(long maxiSize) {
    this.maxiSize = maxiSize;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
