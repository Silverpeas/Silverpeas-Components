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

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class Question extends SilverpeasBean {

  public static final int CLOSED = 2;
  public static final int NEW = 0;
  public static final int WAITING = 1;
  private static final long serialVersionUID = 8690405914141003827L;
  private String title;
  private String content;
  private String creatorId;
  private String creationDate;
  private int status = 0;
  private int publicReplyNumber = 0;
  private int privateReplyNumber = 0;
  private int replyNumber = 0;
  private String instanceId;
  private String categoryId;
  private List<Reply> replies = new ArrayList<>();
  private List<Recipient> recipients = new ArrayList<>();

  public Question() {
  }

  public Question(String creatorId, String instanceId) {
    this.creatorId = creatorId;
    this.instanceId = instanceId;
    setCreationDate();
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public int getStatus() {
    return status;
  }

  public int getPublicReplyNumber() {
    return publicReplyNumber;
  }

  public int getPrivateReplyNumber() {
    return privateReplyNumber;
  }

  public int getReplyNumber() {
    return replyNumber;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public List<Reply> readReplies() {
    return replies;
  }

  public List<Recipient> readRecipients() {
    return recipients;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public final void setCreationDate() {
    this.creationDate = DateUtil.date2SQLDate(new Date());
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setPublicReplyNumber(int publicReplyNumber) {
    this.publicReplyNumber = publicReplyNumber;
  }

  public void setPrivateReplyNumber(int privateReplyNumber) {
    this.privateReplyNumber = privateReplyNumber;
  }

  public void setReplyNumber(int replyNumber) {
    this.replyNumber = replyNumber;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public void writeReplies(Collection<Reply> replies) {
    this.replies = new ArrayList<>(replies);
  }

  public void writeRecipients(Collection<Recipient> recipients) {
    this.recipients = new ArrayList<>(recipients);
  }

  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_QUESTION, getPK().getId(), false);
  }

  public String getURL() {
    return "searchResult?Type=Question&Id=" + getPK().getId();
  }

  public String readCreatorName() {
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    return readCreatorName(organizationController);
  }

  public String readCreatorName(OrganizationController organizationController) {
    String creatorName = null;
    UserDetail userDetail = readAuthor(organizationController);
    if (userDetail != null) {
      creatorName = userDetail.getDisplayedName();
    }
    return creatorName;
  }


  public UserDetail readAuthor(OrganizationController organizationController) {
    return organizationController.getUserDetail(getCreatorId());
  }

  @Override
  @NonNull
  protected String getTableName() {
    return "SC_QuestionReply_Question";
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  @SuppressWarnings("unused")
  public boolean hasWaitingStatus() {
    return this.status == WAITING;
  }

  public boolean hasClosedStatus() {
    return this.status == CLOSED;
  }

  public boolean hasNewStatus() {
    return this.status == NEW;
  }

  public void close() {
    this.status = CLOSED;
  }

  public void waitForAnswer() {
    this.status = WAITING;
  }

  @Override
  public String toString() {
    return "Question{" + "title=" + title + ", content=" + content + ", creatorId=" + creatorId +
        ", creationDate=" + creationDate + ", status=" + status + ", publicReplyNumber=" +
        publicReplyNumber + ", privateReplyNumber=" + privateReplyNumber + ", replyNumber=" +
        replyNumber + ", instanceId=" + instanceId + ", categoryId=" + categoryId + ", replies=" +
        replies + ", recipients=" + recipients + '}';
  }
}
