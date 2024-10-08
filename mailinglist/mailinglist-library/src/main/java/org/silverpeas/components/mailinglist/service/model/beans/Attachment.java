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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "sc_mailinglist_attachment")
@NamedQuery(name = "findSomeAttachments", query =
    "select a from Attachment a where a.md5Signature = :md5 and a.size = :size and a.fileName = " +
        ":fileName")
@NamedQuery(name = "findSomeAttachmentsExcludingOne", query =
    "select a from Attachment a where a.md5Signature = :md5 and a.size = :size and a.fileName = " +
        ":fileName and a.id <> :id")
public class Attachment extends IdentifiableObject {

  @Column(name = "attachmentPath")
  private String path;
  @Column(name = "attachmentSize")
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + Long.hashCode(size);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Attachment other = (Attachment) obj;
    if (fileName == null) {
      if (other.fileName != null) {
        return false;
      }
    } else if (!fileName.equals(other.fileName)) {
      return false;
    }
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    return size == other.size;
  }
}
