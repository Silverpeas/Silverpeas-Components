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
package org.silverpeas.components.questionreply.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.annotation.NonNull;

import java.text.ParseException;
import java.util.Date;

@SuppressWarnings("deprecation")
public class Reply extends SilverpeasBean implements Contribution, WithAttachment {

  private static final long serialVersionUID = 5638699228049557540L;
  private long questionId;
  private String title;
  private String content;
  private String wysiwygContent;
  private String creatorId;
  private String creationDate;
  private int publicReply = 0;
  private int privateReply = 1;

  public Reply() {
  }

  public Reply(String creatorId) {
    this.creatorId = creatorId;
    this.creationDate = DateUtil.date2SQLDate(new Date());
  }

  public Reply(long questionId, String creatorId) {
    this(creatorId);
    this.questionId = questionId;
  }

  public long getQuestionId() {
    return questionId;
  }

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
    return ContributionIdentifier.from(new ResourceReference(getPK()));
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
      throw new RuntimeException(e);
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

  public void setQuestionId(long questionId) {
    this.questionId = questionId;
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
   this.wysiwygContent = WysiwygController.load(getPK().getInstanceId(), getPK().getId(),
          I18NHelper.DEFAULT_LANGUAGE);
    return wysiwygContent;
  }

  public String readCurrentWysiwygContent() {
    return this.wysiwygContent;
  }

  public void writeWysiwygContent(String wysiwygContent) {
    this.wysiwygContent = wysiwygContent;
  }

  @Override
  @NonNull
  protected String getTableName() {
    return "SC_QuestionReply_Reply";
  }

  @Override
  public boolean isIndexable() {
    return this.getPublicReply() == 1;
  }

  @Override
  public String toString() {
    return "Reply{" + "questionId=" + questionId + ", title=" + title + ", content=" + content
        + ", creatorId=" + creatorId + ", creationDate=" + creationDate + ", publicReply="
        + publicReply + ", privateReply=" + privateReply + '}';
  }
}
