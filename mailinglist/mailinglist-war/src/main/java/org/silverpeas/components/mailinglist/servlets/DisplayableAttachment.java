/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.mailinglist.servlets;

import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.core.util.file.FileRepositoryManager;

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
