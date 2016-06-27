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

import com.silverpeas.blog.model.Category;
import com.silverpeas.blog.model.PostDetail;
import com.silverpeas.comment.model.Comment;
import com.silverpeas.notification.builder.AbstractTemplateUserNotificationBuilder;
import com.silverpeas.notification.builder.UserSubscriptionNotificationBehavior;
import com.silverpeas.notification.model.NotificationResourceData;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;

import java.util.Collection;

/**
 * The centralization of the construction of the blog notifications
 * @author Yohann Chastagnier
 */
public class BlogUserSubscriptionNotification extends AbstractBlogUserNotification
    implements UserSubscriptionNotificationBehavior {

  private final String fileName;
  private final NotifAction action;
  private final Collection<String> newSubscribers;
  private final Comment comment;

  public BlogUserSubscriptionNotification(final PostDetail postDetail, final Comment comment,
      final String type, final String senderId, final Collection<String> newSubscribers) {
    super(postDetail, UserDetail.getById(senderId));
    if ("create".equals(type)) {
      fileName = "blogNotificationSubscriptionCreate";
      action = NotifAction.CREATE;
    } else {
      fileName = "blogNotificationSubscriptionUpdate";
      action = NotifAction.UPDATE;
    }
    this.newSubscribers = newSubscribers;
    this.comment = comment;
  }

  @Override
  protected String getBundleSubjectKey() {
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
  protected void performTemplateData(final String language, final PostDetail resource,
      final SilverpeasTemplate template) {
    super.performTemplateData(language, resource, template);
    template.setAttribute("comment", comment);
    String commentMessage = null;
    if (comment != null) {
      commentMessage = comment.getMessage();
    }
    template.setAttribute("commentMessage", commentMessage);
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return true;
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

}