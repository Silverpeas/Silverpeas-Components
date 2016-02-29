/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.quickinfo.notification;

import java.util.Collection;

import org.silverpeas.components.quickinfo.model.News;

import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class NewsManualUserNotification extends AbstractNewsUserNotification {

  private UserDetail sender;

  public NewsManualUserNotification(News resource, final UserDetail sender) {
    super(resource, NotifAction.REPORT);
    this.sender = sender;
  }

  @Override
  protected String getBundleSubjectKey() {
    return "GML.st.notification.from";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    // Users to notify are not handled here.
    return null;
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return false;
  }

  @Override
  protected String getFileName() {
    return "notification";
  }

  @Override
  protected String getSender() {
    return sender.getId();
  }
}
