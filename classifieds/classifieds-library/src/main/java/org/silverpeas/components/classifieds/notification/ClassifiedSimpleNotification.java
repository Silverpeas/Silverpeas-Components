/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.classifieds.notification;

import org.silverpeas.components.classifieds.model.ClassifiedDetail;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.client.constant.NotifAction;

import java.util.Collection;
import java.util.Collections;

public class ClassifiedSimpleNotification extends AbstractClassifiedUserNotification {

  private final User sender;

  public ClassifiedSimpleNotification(final ClassifiedDetail resource, final User sender) {
    super(resource);
    this.sender = sender;
  }

  @Override
  protected String getTemplateFileName() {
    return "notification";
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return false;
  }

  @Override
  protected NotifAction getAction() {
    return NotifAction.REPORT;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    // Users to notify are not handled here.
    return Collections.emptyList();
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }
}