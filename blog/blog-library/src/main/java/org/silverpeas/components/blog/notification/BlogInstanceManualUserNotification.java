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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.blog.notification;

import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.components.blog.service.BlogService;
import org.silverpeas.core.notification.user.AbstractComponentInstanceManualUserNotification;
import org.silverpeas.core.notification.user.NotificationContext;
import org.silverpeas.core.notification.user.UserNotification;

import javax.inject.Named;

/**
 * @author silveryocha
 */
@Named
public class BlogInstanceManualUserNotification extends
    AbstractComponentInstanceManualUserNotification {

  private static final String POST_KEY = "PostDetailKey";

  @Override
  protected boolean check(final NotificationContext context) {
    final String postId = context.getContributionId();
    final PostDetail post = getPost(postId);
    context.put(POST_KEY, post);
    return post.canBeAccessedBy(context.getSender());
  }

  @Override
  public UserNotification createUserNotification(final NotificationContext context) {
    final PostDetail post = context.getObject(POST_KEY);
    context.put(NotificationContext.PUBLICATION_ID, post.getPublication().getId());
    return new BlogUserAlertNotification(post, context.getSender()).build();
  }

  private PostDetail getPost(final String postId) {
    return BlogService.get().getContentById(postId);
  }
}
