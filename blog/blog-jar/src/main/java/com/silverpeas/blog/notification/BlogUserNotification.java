/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.blog.notification;

import java.util.Collection;

import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

/**
 * The centralization of the construction of the blog notifications
 * @author Yohann Chastagnier
 */
public class BlogUserNotification extends AbstractTemplateUserNotificationBuilder<PostDetail> {

  private final UserDetail userDetail;
  private final String componentInstanceId;
  private final Comment comment;
  private final String fileName;
  private final NotifAction action;
  private final String senderId;
  private final Collection<String> newSubscribers;

  public BlogUserNotification(final String componentInstanceId, final PostDetail postDetail,
      final UserDetail userDetail) {
    this(componentInstanceId, postDetail, null, null, userDetail.getId(), null, userDetail);
  }

  public BlogUserNotification(final String componentInstanceId, final PostDetail postDetail, final Comment comment,
      final String type, final String senderId, final Collection<String> newSubscribers) {
    this(componentInstanceId, postDetail, comment, type, senderId, newSubscribers, null);
  }

  private BlogUserNotification(final String componentInstanceId, final PostDetail postDetail, final Comment comment,
      final String type, final String senderId, final Collection<String> newSubscribers, final UserDetail userDetail) {
    super(postDetail, null, null);
    this.componentInstanceId = componentInstanceId;
    this.comment = comment;
    if ("create".equals(type)) {
      fileName = "blogNotificationSubscriptionCreate";
      action = NotifAction.CREATE;
    } else if ("update".equals(type)) {
      fileName = "blogNotificationSubscriptionUpdate";
      action = NotifAction.UPDATE;
    } else if ("commentCreate".equals(type)) {
      fileName = "blogNotificationSubscriptionCommentCreate";
      action = NotifAction.CREATE;
    } else if ("commentUpdate".equals(type)) {
      fileName = "blogNotificationSubscriptionCommentUpdate";
      action = NotifAction.UPDATE;
    } else {
      fileName = "blogNotification";
      action = NotifAction.REPORT;
    }
    this.senderId = senderId;
    this.newSubscribers = newSubscribers;
    this.userDetail = userDetail;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (action.equals(NotifAction.REPORT)) {
      return "blog.notifSubject";
    }
    return "blog.subjectSubscription";
  }

  @Override
  protected String getFileName() {
    return fileName;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return newSubscribers;
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return !NotifAction.REPORT.equals(action);
  }

  @Override
  protected void performTemplateData(final String language, final PostDetail resource,
      final SilverpeasTemplate template) {
    getNotificationMetaData().addLanguage(language, getBundle(language).getString(getBundleSubjectKey(), getTitle()), "");
    template.setAttribute("blog", resource);
    template.setAttribute("blogName", resource.getPublication().getName(language));
    template.setAttribute("blogDate", DateUtil.getOutputDate(resource.getDateEvent(), language));
    template.setAttribute("comment", comment);
    String commentMessage = null;
    if (comment != null) {
      commentMessage = comment.getMessage();
    }
    template.setAttribute("commentMessage", commentMessage);
    final Category categorie = resource.getCategory();
    String categorieName = null;
    if (categorie != null) {
      categorieName = categorie.getName(language);
    }
    template.setAttribute("blogCategorie", categorieName);
    template.setAttribute("senderName", (userDetail != null ? userDetail.getDisplayedName() : ""));
    template.setAttribute("silverpeasURL", getNotificationMetaData().getLink());
  }

  @Override
  protected void performNotificationResource(final String language, final PostDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getPublication().getName(language));
  }

  @Override
  protected String getTemplatePath() {
    return "blog";
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return componentInstanceId;
  }

  @Override
  protected String getSender() {
    return senderId;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "com.silverpeas.blog.multilang.blogBundle";
  }
}
