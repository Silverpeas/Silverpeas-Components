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
package org.silverpeas.components.mailinglist.service.model.beans;

import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.persistence.*;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sc_mailinglist_message", uniqueConstraints = @UniqueConstraint(columnNames = {
    "mailId", "componentId"}))
@NamedQuery(name = "findMessage", query =
    "from Message where componentId = :componentId and messageId = :messageId")
@NamedQuery(name = "countOfMessages", query =
    "select count(m) from Message m where m.componentId = :componentId")
@NamedQuery(name = "countOfMessagesByModeration", query =
    "select count(m) from Message m where m.componentId = :componentId and m.moderated = :moderated")
@NamedQuery(name = "findActivitiesFromMessages", query =
    "select new org.silverpeas.components.mailinglist.service.model.beans.Activity(count(m), " +
        "m.year, m.month) from Message m where m.componentId = :componentId and " +
        "m.moderated = :moderated group by m.year, m.month")
public class Message extends IdentifiableObject {

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "messageId")
  private Set<Attachment> attachments = new HashSet<>();
  private String title;
  private String sender;
  private Instant sentDate;
  @Lob
  private String body;
  private String summary;
  private boolean moderated;
  @Column(name = "mailId", nullable = false)
  private String messageId;
  private String referenceId;
  @Column(name = "componentId", nullable = false)
  private String componentId;
  private String contentType;
  @Column(name = "messageYear")
  private int year;
  @Column(name = "messageMonth")
  private int month;

  public Set<Attachment> getAttachments() {
    return attachments;
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
    return Date.from(sentDate);
  }

  public void setSentDate(Date date) {
    if (date != null) {
      this.sentDate = date.toInstant();
      Calendar calend = Calendar.getInstance();
      calend.setTime(getSentDate());
      this.year = calend.get(Calendar.YEAR);
      this.month = calend.get(Calendar.MONTH);
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
    return this.year;
  }

  public int getMonth() {
    return this.month;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((messageId == null) ? 0 : messageId.hashCode());
    result = prime * result + ((componentId == null) ? 0 : componentId.hashCode());
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
    if (this.id != null && other.getId() != null &&
        (!this.id.equals(other.id) || version != other.version)) {
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
