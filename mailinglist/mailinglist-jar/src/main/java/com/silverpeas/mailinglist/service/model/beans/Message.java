/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist.service.model.beans;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.stratelia.webactiv.util.FileRepositoryManager;

public class Message extends IdentifiedObject {
  private Set<Attachment> attachments = new HashSet<Attachment>();
  private String title;
  private String sender;
  private Date sentDate;
  private String body;
  private String summary;
  private boolean moderated;
  private String messageId;
  private String referenceId;
  private String componentId;
  private String contentType;

  public Set<Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(Set<Attachment> attachments) {
    this.attachments = attachments;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public Date getSentDate() {
    if (sentDate == null) {
      return null;
    }
    return new Date(sentDate.getTime());
  }

  public void setSentDate(Date date) {
    if (date != null) {
      this.sentDate = new Date(date.getTime());
    }
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public boolean isModerated() {
    return moderated;
  }

  public void setModerated(boolean moderated) {
    this.moderated = moderated;
  }

  public void setAttachmentsSize(long size) {
    // do nothing
  }

  public long getAttachmentsSize() {
    long size = 0;
    for (Attachment attachment : attachments) {
      size += attachment.getSize();
    }
    return size;
  }

  public String getAttachmentsSizeToDisplay() {
    return FileRepositoryManager.formatFileSize(getAttachmentsSize());
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public int getYear() {
    if (this.sentDate != null) {
      Calendar calend = Calendar.getInstance();
      calend.setTime(this.sentDate);
      return calend.get(Calendar.YEAR);
    }
    return 0;
  }

  public void setYear(int year) {
  }

  public int getMonth() {
    if (this.sentDate != null) {
      Calendar calend = Calendar.getInstance();
      calend.setTime(this.sentDate);
      return calend.get(Calendar.MONTH);
    }
    return 0;
  }

  public void setMonth(int month) {
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
    result = prime * result
        + ((componentId == null) ? 0 : componentId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Message other = (Message) obj;
    if (this.id != null && other.getId() != null
        && (!this.id.equals(other.id) || version != other.version)) {
      return false;
    } else if (messageId == null) {
      if (other.messageId != null) {
        return false;
      }
    } else if (!messageId.equals(other.messageId)) {
      return false;
    } else if (componentId == null) {
      if (other.componentId != null) {
        return false;
      }
    } else if (!componentId.equals(other.componentId)) {
      return false;
    }
    return true;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
