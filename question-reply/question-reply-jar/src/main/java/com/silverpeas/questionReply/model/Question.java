/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.questionReply.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.DateUtil;

public class Question extends SilverpeasBean {
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
  private Collection<Reply> replies = new ArrayList<Reply>();
  private Collection<Recipient> recipients = new ArrayList<Recipient>();

  private static OrganizationController organizationController = new OrganizationController();

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

  public Collection<Reply> readReplies() {
    return replies;
  }

  public Collection<Recipient> readRecipients() {
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
    this.replies = replies;
  }

  public void writeRecipients(Collection<Recipient> recipients) {
    this.recipients = recipients;
  }

  public void incPublicReplyNumber(int nb) {
    this.publicReplyNumber = this.publicReplyNumber + nb;
  }

  public void incPrivateReplyNumber(int nb) {
    this.privateReplyNumber = this.privateReplyNumber + nb;
  }

  public void decPublicReplyNumber(int nb) {
    this.publicReplyNumber = this.publicReplyNumber - nb;
  }

  public void decPrivateReplyNumber(int nb) {
    this.privateReplyNumber = this.privateReplyNumber - nb;
  }

  public void incReplyNumber(int nb) {
    this.replyNumber = this.replyNumber + nb;
  }

  public void decReplyNumber(int nb) {
    this.replyNumber = this.replyNumber - nb;
  }

  public String _getPermalink() {
    if (URLManager.displayUniversalLinks()) {
      return URLManager.getApplicationURL() + "/Question/" + getPK().getId();
    }

    return null;
  }

  public String _getURL() {
    return "searchResult?Type=Question&Id=" + getPK().getId();
  }

  public String readCreatorName() {
    String creatorName = null;
    UserDetail userDetail = organizationController.getUserDetail(String.valueOf(getCreatorId()));
    if (userDetail != null) {
      creatorName = userDetail.getDisplayedName();
    }
    return creatorName;
  }

  @Override
  public String _getTableName() {
    return "SC_QuestionReply_Question";
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

}
