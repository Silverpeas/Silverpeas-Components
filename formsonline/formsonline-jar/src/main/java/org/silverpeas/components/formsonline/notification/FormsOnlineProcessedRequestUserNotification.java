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
package org.silverpeas.components.formsonline.notification;

import com.silverpeas.formsonline.model.FormInstance;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;

import java.util.Collection;
import java.util.List;

/**
 * @author Nicolas EYSSERIC
 */
public class FormsOnlineProcessedRequestUserNotification
    extends FormsOnlineValidationRequestUserNotification {

  private final List<String> usersToBeNotified;

  public FormsOnlineProcessedRequestUserNotification(final FormInstance resource,
      NotifAction action, final List<String> usersToBeNotified) {
    super(resource, action);
    this.usersToBeNotified = usersToBeNotified;
  }

  @Override
  protected void perform(final FormInstance resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected String getBundleSubjectKey() {
    return "formsOnline.msgFormProcessed";
  }

  @Override
  protected String getFileName() {
    return "notificationProcessed";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    if (usersToBeNotified == null) {
      return null;
    }
    return usersToBeNotified;
  }
}