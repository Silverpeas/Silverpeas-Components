/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.suggestionbox.mock;

import com.silverpeas.usernotification.builder.UserNotificationBuider;
import com.silverpeas.usernotification.builder.helper.UserNotificationManager;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.constant.NotifMediaType;
import org.mockito.Mockito;

/**
 * @author: Yohann Chastagnier
 */
public class UserNotificationManagerMockWrapper extends UserNotificationManager {

  private UserNotificationManager mock = Mockito.mock(UserNotificationManager.class);

  protected UserNotificationManagerMockWrapper() {
  }

  public UserNotificationManager getMock() {
    return mock;
  }

  @Override
  public void buildAndSend(final UserNotificationBuider notificationBuider) {
    mock.buildAndSend(notificationBuider);
  }

  @Override
  public void buildAndSend(final NotifMediaType mediaType,
      final UserNotificationBuider notificationBuider) {
    mock.buildAndSend(mediaType, notificationBuider);
  }

  @Override
  public NotificationMetaData build(final UserNotificationBuider notificationBuider) {
    return mock.build(notificationBuider);
  }
}
