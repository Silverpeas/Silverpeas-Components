package com.silverpeas.external.mailinglist.servlets;

import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.stratelia.webactiv.util.FileRepositoryManager;

public class DisplayableAttachment implements MailingListRoutage {
  private long size;

  private String fileName;

  private String icon;

  private String downloadTime;

  private String url;

  public DisplayableAttachment(String messageId, Attachment attachment) {
    this.size = attachment.getSize();
    this.fileName = attachment.getFileName();
    this.icon = FileRepositoryManager.getFileIcon(FileRepositoryManager
        .getFileExtension(this.fileName));
    this.downloadTime = FileRepositoryManager.getFileDownloadTime(this.size);
    this.url = '/' + DESTINATION_ATTACHMENT + '/' + attachment.getId() + '/'
        + DESTINATION_MESSAGE + '/' + messageId;
  }

  public String getDisplayableSize() {
    return FileRepositoryManager.formatFileSize(getSize());
  }

  public long getSize() {
    return size;
  }

  public String getFileName() {
    return fileName;
  }

  public String getIcon() {
    return icon;
  }

  public String getDownloadTime() {
    return downloadTime;
  }

  public String getUrl() {
    return url;
  }
}
