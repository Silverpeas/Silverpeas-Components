package com.silverpeas.mailinglist.service.model.beans;

public class Attachment extends IdentifiedObject {
  private String path;

  private long size;

  private String fileName;

  private String md5Signature;

  private String contentType;

  public String getMd5Signature() {
    return md5Signature;
  }

  public void setMd5Signature(String md5Signature) {
    this.md5Signature = md5Signature;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + (int) (size ^ (size >>> 32));
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Attachment other = (Attachment) obj;
    if (fileName == null) {
      if (other.fileName != null)
        return false;
    } else if (!fileName.equals(other.fileName))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (size != other.size)
      return false;
    return true;
  }

}
