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
package org.silverpeas.components.kmelia.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaPendingValidationPublicationUserNotification extends AbstractKmeliaActionPublicationUserNotification {

  private final String[] usersToBeNotified;

  public KmeliaPendingValidationPublicationUserNotification(final PublicationDetail resource,
      final String[] usersToBeNotified) {
    super(null, resource, NotifAction.PENDING_VALIDATION);
    this.usersToBeNotified = usersToBeNotified;
  }

  @Override
  protected void perform(final PublicationDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return "ToValidateForNotif";
  }

  @Override
  protected String getTemplateFileName() {
    return "notificationToValidate";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    if (usersToBeNotified == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(Arrays.asList(usersToBeNotified));
  }

  protected String getSenderName() {
    return User.getById(getSender()).getDisplayedName();
  }

  @Override
  protected boolean isSendImmediately() {
    return true;
  }
}
