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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.notification;

import java.util.Collection;

import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

/**
 * @author Yohann Chastagnier
 */
public class ClassifiedSubscriptionUserNotification extends AbstractClassifiedUserNotification {

  private final Collection<String> usersToBeNotified;

  public ClassifiedSubscriptionUserNotification(final ClassifiedDetail resource, final Collection<String> usersToBeNotified) {
    super(resource);
    this.usersToBeNotified = usersToBeNotified;
  }

  @Override
  protected String getTemplateFileName() {
    return "subscription";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return usersToBeNotified;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "classifieds.mailNewPublicationSubscription";
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.CREATE;
  }
}
