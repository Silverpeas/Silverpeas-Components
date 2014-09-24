/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.model;

import org.silverpeas.util.i18n.I18NHelper;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.util.DateUtil;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reply extends SilverpeasBean {

  Logger logger = LoggerFactory.getLogger("questionReply");
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

  public String getContent() {
    return content;
  }

  public String getCreatorId() {
    return creatorId;
  }

  public String getCreationDate() {
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

  public void setCreationDate() {
    this.creationDate = DateUtil.date2SQLDate(new Date());
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
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
    return OrganisationControllerFactory.getOrganisationController().getUserDetail(String.valueOf(
        getCreatorId()));
  }

  public String loadWysiwygContent() {
   this.wysiwygContent = WysiwygController.load(getPK().getInstanceId(), getPK().getId(),
          I18NHelper.defaultLanguage);
    return wysiwygContent;
  }

  public String readCurrentWysiwygContent() {
    return this.wysiwygContent;
  }

  public void writeWysiwygContent(String wysiwygContent) {
    this.wysiwygContent = wysiwygContent;
  }

  @Override
  public String _getTableName() {
    return "SC_QuestionReply_Reply";
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  @Override
  public String toString() {
    return "Reply{" + "questionId=" + questionId + ", title=" + title + ", content=" + content
        + ", creatorId=" + creatorId + ", creationDate=" + creationDate + ", publicReply="
        + publicReply + ", privateReply=" + privateReply + '}';
  }
}
