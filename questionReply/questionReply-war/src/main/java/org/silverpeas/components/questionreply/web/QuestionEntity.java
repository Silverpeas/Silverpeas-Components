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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.web;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.web.rs.WebEntity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.PUBLISHER;

/**
 * @author emmanuel.hugonnet@silverpeas.org
 */
@XmlRootElement
public class QuestionEntity implements WebEntity {

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
  @XmlElement()
  private boolean replyable = false;
  @XmlElement()
  private boolean closeable = false;
  @XmlElement(required = true)
  private AuthorEntity creator;

  @XmlTransient
  private String language = DisplayI18NHelper.getDefaultLanguage();

  protected QuestionEntity() {
  }

  private QuestionEntity(Question question, String lang) {
    this.id = question.getPK().getId();
    this.instanceId = question.getInstanceId();
    this.categoryId = question.getCategoryId();
    this.title = question.getTitle();
    this.content = question.getContent();
    try {
      this.creationDate = DateUtil.getOutputDate(question.getCreationDate(), lang);
      this.language = lang;
    } catch (ParseException ex) {
      this.creationDate = question.getCreationDate();
    }
    this.creatorId = question.getCreatorId();
    this.status = question.getStatus();
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
    return new EqualsBuilder()
        .append(this.id, other.id)
        .append(this.title, other.title)
        .append(this.content, other.content)
        .append(this.creatorId, other.creatorId)
        .append(this.creatorName, other.creatorName)
        .append(this.creationDate, other.creationDate)
        .append(this.status, other.status)
        .append(this.publicReplyNumber, other.publicReplyNumber)
        .append(this.privateReplyNumber, other.privateReplyNumber)
        .append(replyNumber, other.replyNumber)
        .append(this.instanceId, other.instanceId)
        .append(this.categoryId, other.categoryId)
        .build();
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
   * @param lang the language of the current User.
   * @return the entity representing the specified question.
   */
  public static QuestionEntity fromQuestion(final Question question, final String lang) {
    return new QuestionEntity(question, lang);
  }

  /**
   * Creates several new reply entities from the specified list of questions.
   * @param questions the list of questions to entitify.
   * @param lang the language of the current User.
   * @return a list of entities representing each of then one of the specified questions.
   */
  public static List<QuestionEntity> fromQuestions(final Iterable<Question> questions,
      final String lang) {
    List<QuestionEntity> entities = new ArrayList<>();
    for (Question question : questions) {
      entities.add(fromQuestion(question, lang));
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
    try {
      question.setCreationDate(DateUtil.getInputDate(this.creationDate, language));
    } catch (ParseException ex) {
      question.setCreationDate(this.creationDate);
    }
    question.setCreatorId(creatorId);
    question.setInstanceId(instanceId);
    question.setPrivateReplyNumber(privateReplyNumber);
    question.setPublicReplyNumber(publicReplyNumber);
    question.setReplyNumber(replyNumber);
    question.setStatus(status);
    question.setTitle(title);
    return question;
  }

  public QuestionEntity withUser(User user, SilverpeasRole profile) {
    this.updatable = isQuestionUpdatable(user, profile);
    this.reopenable = isQuestionReopenable(profile);
    this.replyable = isQuestionReplyable(user, profile);
    this.closeable = isQuestionCloseable(user, profile);
    return this;
  }

  boolean isQuestionUpdatable(User user, SilverpeasRole profile) {
    return !((profile == PUBLISHER && !this.getCreatorId().equals(user.getId())) ||
        (profile == PUBLISHER && this.getStatus() != Question.NEW) ||
        (profile == SilverpeasRole.USER));
  }

  boolean isQuestionCloseable(User user, SilverpeasRole profile) {
    return PUBLISHER != profile && this.getStatus() == Question.WAITING &&
        isQuestionUpdatable(user, profile);
  }

  boolean isQuestionReplyable(User user, SilverpeasRole profile) {
    return PUBLISHER != profile && this.getStatus() != Question.CLOSED &&
        isQuestionUpdatable(user, profile);
  }

  boolean isQuestionReopenable(SilverpeasRole profile) {
    return this.getStatus() == Question.CLOSED && profile == ADMIN;
  }

  @Override
  public String toString() {
    return "QuestionEntity{" + "uri=" + uri + ", id=" + id + ", title=" + title + ", content=" +
        content + ", creatorId=" + creatorId + ", creatorName=" + creatorName + ", creationDate=" +
        creationDate + ", status=" + status + ", publicReplyNumber=" + publicReplyNumber +
        ", privateReplyNumber=" + privateReplyNumber + ", replyNumber=" + replyNumber +
        ", instanceId=" + instanceId + ", categoryId=" + categoryId + ", updatable=" + updatable +
        ", reopenable=" + reopenable + '}';
  }
}
