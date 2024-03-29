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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.forums.notification;

import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.components.forums.model.Message;
import org.silverpeas.components.forums.model.Moderator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Yohann Chastagnier
 * Date: 10/06/13
 */
public class ForumsMessagePendingValidationUserNotification
    extends AbstractForumsMessageUserNotification {

  /**
   * Default constructor.
   * @param resource
   */
  public ForumsMessagePendingValidationUserNotification(final Message resource) {
    super(resource);
  }

  @Override
  protected void perform(final Message resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return getNotificationBundleKeyPrefix() + "subject.toValidate";
  }

  @Override
  protected String getTemplateFileName() {
    return "messageToValidate";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.PENDING_VALIDATION;
  }

  @Override
  protected String getSender() {
    return getResource().getAuthor();
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    Collection<Moderator> moderators = getForumsService().getModerators(getResource().getForumId());
    Collection<String> moderatorIds = new ArrayList<>(moderators.size());
    for (Moderator moderator : moderators) {
      moderatorIds.add(moderator.getUserId());
    }
    return moderatorIds;
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }
}
