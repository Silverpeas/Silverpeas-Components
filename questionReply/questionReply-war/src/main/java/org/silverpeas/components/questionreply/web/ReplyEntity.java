/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.web;

import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.webapi.attachment.AttachmentEntity;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.WRITER;

/**
 * @author emmanuel.hugonnet@silverpeas.org
 */
@XmlRootElement
public class ReplyEntity implements WebEntity {

  private static final long serialVersionUID = -4930210623640108120L;
  @XmlElement()
  private URI uri;
  @XmlElement()
  private String id;
  @XmlElement()
  private long questionId;
  @XmlElement()
  private String title;
  @XmlElement()
  private String content;
  @XmlElement()
  private String creatorId;
  @XmlElement()
  private String creatorName;
  @XmlElement()
  private String creationDate;
  @XmlElement()
  private boolean publicReply = false;
  @XmlElement()
  private boolean privateReply = true;
  @XmlElement
  private boolean readOnly = true;
  @XmlElement
  private AttachmentEntity[] attachments;

  @XmlTransient
  private String language = DisplayI18NHelper.getDefaultLanguage();

  protected ReplyEntity() {
  }

  private ReplyEntity(Reply reply, String lang) {
    this.id = reply.getPK().getId();
    this.questionId = reply.getQuestionId();
    this.content = reply.readCurrentWysiwygContent();
    this.title = reply.getTitle();
    try {
      this.creationDate = DateUtil.getOutputDate(reply.getCreationDateAsString(), lang);
      this.language = lang;
    } catch (ParseException ex) {
      this.creationDate = reply.getCreationDateAsString();
    }
    this.creatorId = reply.getCreatorId();
    this.creatorName = reply.readCreatorName();
    this.publicReply = reply.getPublicReply() == 1;
    this.privateReply = reply.getPrivateReply() == 1;
    this.attachments = null;
  }

  public String getContent() {
    return content;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public String getId() {
    return id;
  }

  public boolean isPrivateReply() {
    return privateReply;
  }

  public boolean isPublicReply() {
    return publicReply;
  }

  public String getTitle() {
    return title;
  }

  public String getLanguage() {
    return this.language;
  }

  public AttachmentEntity[] getAttachments() {
    return this.attachments;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ReplyEntity other = (ReplyEntity) obj;
    if (this.id != null && other.id != null && this.id.equals(other.id)) {
      return true;
    }
    if (this.questionId != other.questionId) {
      return false;
    }
    if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
      return false;
    }
    if ((this.content == null) ? (other.content != null) : !this.content.equals(other.content)) {
      return false;
    }
    if ((this.creatorId == null) ? (other.creatorId != null) :
        !this.creatorId.equals(other.creatorId)) {
      return false;
    }
    if ((this.creatorName == null) ? (other.creatorName != null) :
        !this.creatorName.equals(other.creatorName)) {
      return false;
    }
    if ((this.creationDate == null) ? (other.creationDate != null) :
        !this.creationDate.equals(other.creationDate)) {
      return false;
    }
    if (this.publicReply != other.publicReply) {
      return false;
    }
    return this.privateReply == other.privateReply;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 79 * hash + (Long.valueOf(this.questionId)).intValue();
    hash = 79 * hash + (this.title != null ? this.title.hashCode() : 0);
    hash = 79 * hash + (this.content != null ? this.content.hashCode() : 0);
    hash = 79 * hash + (this.creatorId != null ? this.creatorId.hashCode() : 0);
    hash = 79 * hash + (this.creatorName != null ? this.creatorName.hashCode() : 0);
    hash = 79 * hash + (this.creationDate != null ? this.creationDate.hashCode() : 0);
    hash = 79 * hash + (this.publicReply ? 1 : 0);
    hash = 79 * hash + (this.privateReply ? 1 : 0);
    return hash;
  }

  /**
   * Creates a new reply entity from the specified reply.
   * @param reply the reply to entitify.
   * @return the entity representing the specified reply.
   */
  public static ReplyEntity fromReply(final Reply reply, String lang) {
    return new ReplyEntity(reply, lang);
  }

  /**
   * Creates several new reply entities from the specified list of replies.
   * @param replies the list of replies to entitify.
   * @return a list of entities representing each of then one of the specified replies.
   */
  public static List<ReplyEntity> fromReplies(final Iterable<Reply> replies, String lang) {
    List<ReplyEntity> entities = new ArrayList<>();
    for (Reply reply : replies) {
      entities.add(fromReply(reply, lang));
    }
    return entities;
  }

  /**
   * Gets the reply business objet this entity represent.
   * @return a reply instance.
   */
  public Reply toReply() {
    Reply reply = new Reply();
    reply.setPK(new IdPK(id));
    reply.setQuestionId(questionId);
    reply.setContent(content);
    reply.setCreationDate(creationDate);
    reply.setCreatorId(creatorId);
    reply.setPrivateReply(privateReply ? 1 : 0);
    reply.setPublicReply(publicReply ? 1 : 0);
    reply.setTitle(title);
    return reply;
  }

  /**
   * Sets a URI to this entity.
   * With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public ReplyEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Sets a URI to this entity.
   * With this URI, it can then be accessed through the Web.
   * @param attachmentDetails a collection of SimpleDocument
   * @return itself.
   */
  public ReplyEntity withAttachments(final Collection<SimpleDocument> attachmentDetails) {
    if (attachmentDetails != null && !attachmentDetails.isEmpty()) {
      List<AttachmentEntity> entities = new ArrayList<>(attachmentDetails.size());
      for (SimpleDocument attachment : attachmentDetails) {
        entities.add(AttachmentEntity.fromAttachment(attachment));
      }
      this.attachments = entities.toArray(new AttachmentEntity[entities.size()]);
    }
    return this;
  }

  /**
   * Sets the accessing user profile to this entity.
   * With this profile the status of this reply can be defined.
   * @param profile the profile of the accessing user.
   * @return itself.
   */
  public ReplyEntity withProfile(final SilverpeasRole profile) {
    this.readOnly = profile != ADMIN && profile != WRITER;
    return this;
  }

  @Override
  public String toString() {
    return "ReplyEntity{" + "id=" + id + ", questionId=" + questionId + ", title=" + title +
        ", content=" + content + ", creatorId=" + creatorId + ", creatorName=" + creatorName +
        ", creationDate=" + creationDate + ", publicReply=" + publicReply + ", privateReply=" +
        privateReply + '}';
  }
}
