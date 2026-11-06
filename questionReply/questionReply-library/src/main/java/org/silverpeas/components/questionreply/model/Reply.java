/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.questionreply.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import java.text.ParseException;
import java.util.Date;

/**
 * A reply to a question of a FAQ. The reply belongs to the FAQ of its question.
 */
@Entity
@Table(name = "SC_QuestionReply_Reply")
@NamedQuery(name = "Reply.findById",
    query = "SELECT r FROM Reply r WHERE r.id.id = :id AND r.question.instanceId = :instanceId")
@NamedQuery(name = "Reply.findByIdsOfQuestion",
    query = "SELECT r FROM Reply r WHERE r.id.id IN :ids AND r.question.id.id = :questionId " +
        "ORDER BY r.id.id")
@NamedQuery(name = "Reply.findByQuestion",
    query = "SELECT r FROM Reply r WHERE r.question.id.id = :questionId " +
        "AND r.question.instanceId = :instanceId ORDER BY r.id.id")
@NamedQuery(name = "Reply.findPublicByQuestion",
    query = "SELECT r FROM Reply r WHERE r.question.id.id = :questionId " +
        "AND r.question.instanceId = :instanceId AND r.publicReply = 1 ORDER BY r.id.id")
@NamedQuery(name = "Reply.findPrivateByQuestion",
    query = "SELECT r FROM Reply r WHERE r.question.id.id = :questionId " +
        "AND r.question.instanceId = :instanceId AND r.privateReply = 1 ORDER BY r.id.id")
@NamedQuery(name = "Reply.countByQuestion",
    query = "SELECT COUNT(r) FROM Reply r WHERE r.question.id.id = :questionId")
@NamedQuery(name = "Reply.countPublicByQuestion",
    query = "SELECT COUNT(r) FROM Reply r WHERE r.question.id.id = :questionId " +
        "AND r.publicReply = 1")
@NamedQuery(name = "Reply.countPrivateByQuestion",
    query = "SELECT COUNT(r) FROM Reply r WHERE r.question.id.id = :questionId " +
        "AND r.privateReply = 1")
public class Reply extends BasicJpaEntity<Reply, UniqueIntegerIdentifier>
    implements Contribution, WithAttachment {

  private static final long serialVersionUID = 5638699228049557540L;
  @ManyToOne(optional = false)
  @JoinColumn(name = "questionId", nullable = false)
  private Question question;
  @Column(nullable = false)
  private String title;
  @Column
  private String content;
  @Column(nullable = false)
  private String creatorId;
  @Column(nullable = false)
  private String creationDate;
  @Column(nullable = false)
  private int publicReply = 0;
  @Column(nullable = false)
  private int privateReply = 1;
  @Transient
  private String wysiwygContent;

  public Reply() {
    // required by JPA
  }

  public Reply(String creatorId) {
    this.creatorId = creatorId;
    this.creationDate = DateUtil.date2SQLDate(new Date());
  }

  public Reply(Question question, String creatorId) {
    this(creatorId);
    this.question = question;
  }

  /**
   * Sets the unique identifier of this reply. It is for web entities representing an existing
   * reply.
   * @param id the unique identifier of the reply.
   * @return itself.
   */
  @Override
  public Reply setId(String id) {
    return super.setId(id);
  }

  /**
   * Gets the unique identifier of this reply as a long value.
   * @return the unique identifier of this reply.
   */
  public long getIdAsLong() {
    return getNativeId().getId();
  }

  /**
   * Gets the question this reply answers.
   * @return the question of the reply.
   */
  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public long getQuestionId() {
    return question.getIdAsLong();
  }

  /**
   * Gets the unique identifier of the FAQ this reply belongs to, that is to say the one of its
   * question.
   * @return the unique identifier of a QuestionReply instance.
   */
  public String getInstanceId() {
    return question.getInstanceId();
  }

  /**
   * Gets a reference to this reply, for example to get the documents attached to it.
   * @return a reference to this reply.
   */
  public ResourceReference toResourceReference() {
    return new ResourceReference(getId(), getInstanceId());
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    return false;
  }

  public String getContent() {
    return content;
  }

  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public ContributionIdentifier getIdentifier() {
    return ContributionIdentifier.from(toResourceReference());
  }

  @Override
  public User getCreator() {
    return User.getById(getCreatorId());
  }

  @Override
  public Date getCreationDate() {
    try {
      return DateUtil.parse(this.getCreationDateAsString());
    } catch (ParseException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public User getLastUpdater() {
    return getCreator();
  }

  @Override
  public Date getLastUpdateDate() {
    return getCreationDate();
  }

  public String getCreationDateAsString() {
    return creationDate;
  }

  public int getPublicReply() {
    return publicReply;
  }

  public int getPrivateReply() {
    return privateReply;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  @SuppressWarnings("unused")
  public void setCreationDate() {
    setCreationDate(new Date());
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = DateUtil.date2SQLDate(creationDate);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setPublicReply(int publicReply) {
    this.publicReply = publicReply;
  }

  public void setPrivateReply(int privateReply) {
    this.privateReply = privateReply;
  }

  public String readCreatorName() {
    String creatorName = null;
    UserDetail userDetail = readAuthor();
    if (userDetail != null) {
      creatorName = userDetail.getDisplayedName();
    }
    return creatorName;
  }

  public UserDetail readAuthor() {
    return UserDetail.getById(getCreatorId());
  }

  public String loadWysiwygContent() {
    this.wysiwygContent =
        WysiwygController.load(getInstanceId(), getId(), I18NHelper.getDefaultLanguage());
    return wysiwygContent;
  }

  public String readCurrentWysiwygContent() {
    return this.wysiwygContent;
  }

  public void writeWysiwygContent(String wysiwygContent) {
    this.wysiwygContent = wysiwygContent;
  }

  @Override
  public boolean isIndexable() {
    return this.getPublicReply() == 1;
  }

  @Override
  public String toString() {
    return "Reply{" + "id=" + getId() + ", questionId=" +
        (question == null ? null : question.getId()) + ", title=" + title + ", content=" +
        content + ", creatorId=" + creatorId + ", creationDate=" + creationDate +
        ", publicReply=" + publicReply + ", privateReply=" + privateReply + '}';
  }
}
