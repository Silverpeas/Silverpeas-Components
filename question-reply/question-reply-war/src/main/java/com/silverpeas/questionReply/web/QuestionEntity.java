/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.web;

import com.silverpeas.questionReply.model.Question;
import com.silverpeas.rest.Exposable;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.IdPK;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


import static com.stratelia.webactiv.SilverpeasRole.*;

/**
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
@XmlRootElement
public class QuestionEntity implements Exposable {

  @XmlTransient
  private static final long serialVersionUID = -5078903273496009079L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true, defaultValue = "")
  private String title;
  @XmlElement(required = true, defaultValue = "")
  private String content;
  @XmlElement(required = true, defaultValue = "")
  private String creatorId;
  @XmlElement(defaultValue = "")
  private String creatorName;
  @XmlElement(defaultValue = "")
  private String creationDate;
  @XmlElement(defaultValue = "0")
  private int status = 0;
  @XmlElement(defaultValue = "0")
  private int publicReplyNumber = 0;
  @XmlElement(defaultValue = "0")
  private int privateReplyNumber = 0;
  @XmlElement(defaultValue = "0")
  private int replyNumber = 0;
  @XmlElement(required = true)
  private String instanceId;
  @XmlElement(defaultValue = "")
  private String categoryId = "";
  @XmlElement()
  private boolean updatable = true;
  @XmlElement()
  private boolean reopenable = false;
  @XmlElement(required = true)
  private AuthorEntity creator;


  protected QuestionEntity() {
  }

  private QuestionEntity(Question question) {
    this.id = question.getPK().getId();
    this.instanceId = question.getInstanceId();
    this.categoryId = question.getCategoryId();
    this.title = question.getTitle();
    this.content = question.getContent();
    this.creationDate = question.getCreationDate();
    this.creatorId = question.getCreatorId();
    this.privateReplyNumber = question.getPrivateReplyNumber();
    this.publicReplyNumber = question.getPublicReplyNumber();
    this.replyNumber = question.getReplyNumber();
  }

  /**
   * Sets a URI to this entity.
   * With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public QuestionEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * Gets the URI of this comment entity.
   * @return the URI with which this entity can be access through the Web.
   */
  @Override
  public URI getURI() {
    return uri;
  }

  public String getCategoryId() {
    return categoryId;
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

  public String getId() {
    return id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public int getPrivateReplyNumber() {
    return privateReplyNumber;
  }

  public int getPublicReplyNumber() {
    return publicReplyNumber;
  }

  public int getReplyNumber() {
    return replyNumber;
  }

  public int getStatus() {
    return status;
  }

  public String getTitle() {
    return title;
  }

  public AuthorEntity getCreator() {
    return creator;
  }

  void setCreator(AuthorEntity creator) {
    this.creator = creator;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final QuestionEntity other = (QuestionEntity) obj;
    if (this.id != null && other.id != null && this.id.equals(other.id)) {
      return true;
    }
    if ((this.title == null) ? (other.title != null) : !this.title.equals(other.title)) {
      return false;
    }
    if ((this.content == null) ? (other.content != null) : !this.content.equals(other.content)) {
      return false;
    }
    if ((this.creatorId == null) ? (other.creatorId != null) : !this.creatorId.equals(
        other.creatorId)) {
      return false;
    }
    if ((this.creatorName == null) ? (other.creatorName != null) : !this.creatorName.equals(
        other.creatorName)) {
      return false;
    }
    if ((this.creationDate == null) ? (other.creationDate != null) : !this.creationDate.equals(
        other.creationDate)) {
      return false;
    }
    if (this.status != other.status) {
      return false;
    }
    if (this.publicReplyNumber != other.publicReplyNumber) {
      return false;
    }
    if (this.privateReplyNumber != other.privateReplyNumber) {
      return false;
    }
    if (this.replyNumber != other.replyNumber) {
      return false;
    }
    if ((this.instanceId == null) ? (other.instanceId != null) : !this.instanceId.equals(
        other.instanceId)) {
      return false;
    }
    if ((this.categoryId == null) ? (other.categoryId != null) : !this.categoryId.equals(
        other.categoryId)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 59 * hash + (this.title != null ? this.title.hashCode() : 0);
    hash = 59 * hash + (this.content != null ? this.content.hashCode() : 0);
    hash = 59 * hash + (this.creatorId != null ? this.creatorId.hashCode() : 0);
    hash = 59 * hash + (this.creatorName != null ? this.creatorName.hashCode() : 0);
    hash = 59 * hash + (this.creationDate != null ? this.creationDate.hashCode() : 0);
    hash = 59 * hash + this.status;
    hash = 59 * hash + this.publicReplyNumber;
    hash = 59 * hash + this.privateReplyNumber;
    hash = 59 * hash + this.replyNumber;
    hash = 59 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    hash = 59 * hash + (this.categoryId != null ? this.categoryId.hashCode() : 0);
    return hash;
  }

  /**
   * Creates a new question entity from the specified question.
   * @param question the question to entitify.
   * @return the entity representing the specified question.
   */
  public static QuestionEntity fromQuestion(final Question question) {
    return new QuestionEntity(question);
  }

  /**
   * Creates several new question entities from the specified questions.
   * @param questions the questions to entitify.
   * @return a list of entities representing each of then one of the specified questions.
   */
  public static List<QuestionEntity> fromQuestions(final Question... questions) {
    return fromQuestions(questions);
  }

  /**
   * Creates several new reply entities from the specified list of questions.
   * @param questions the list of questions to entitify.
   * @return a list of entities representing each of then one of the specified questions.
   */
  public static List<QuestionEntity> fromQuestions(final Iterable<Question> questions) {
    List<QuestionEntity> entities = new ArrayList<QuestionEntity>();
    for (Question question : questions) {
      entities.add(fromQuestion(question));
    }
    return entities;
  }

  /**
   * Gets the reply business objet this entity represent.
   * @return a reply instance.
   */
  public Question toQuestion() {
    Question question = new Question();
    question.setPK(new IdPK(id));
    question.setCategoryId(categoryId);
    question.setContent(content);
    question.setCreationDate(creationDate);
    question.setCreatorId(creatorId);
    question.setInstanceId(instanceId);
    question.setPrivateReplyNumber(privateReplyNumber);
    question.setPublicReplyNumber(publicReplyNumber);
    question.setReplyNumber(replyNumber);
    question.setStatus(status);
    question.setTitle(title);
    return question;
  }

  public QuestionEntity withUser(UserDetail userDetail, SilverpeasRole profile) {
    this.updatable = isQuestionUpdatable(userDetail, profile);
    return this;
  }

  boolean isQuestionUpdatable(UserDetail userDetail, SilverpeasRole profile) {
    boolean questionUpdatable = true;
    if (profile == publisher && !this.getCreatorId().equals(userDetail.getId())) {
      questionUpdatable = false;
    } else if (profile == publisher) {
      if (this.getStatus() != Question.NEW) {
        questionUpdatable = false;
      }
    }
    if (profile == user) {
      questionUpdatable = false;
    }
    return questionUpdatable;
  }

  boolean isQuestionReopenable(UserDetail userDetail, SilverpeasRole profile) {
    return this.getStatus() == Question.CLOSED && profile == admin;
  }

  @Override
  public String toString() {
    return "QuestionEntity{" + "uri=" + uri + ", id=" + id + ", title=" + title + ", content="
        + content + ", creatorId=" + creatorId + ", creatorName=" + creatorName + ", creationDate="
        + creationDate + ", status=" + status + ", publicReplyNumber=" + publicReplyNumber
        + ", privateReplyNumber=" + privateReplyNumber + ", replyNumber=" + replyNumber
        + ", instanceId=" + instanceId + ", categoryId=" + categoryId + ", updatable="
        + updatable + ", reopenable=" + reopenable + '}';
  }
}
