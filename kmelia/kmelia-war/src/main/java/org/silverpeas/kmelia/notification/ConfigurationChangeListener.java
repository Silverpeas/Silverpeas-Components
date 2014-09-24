/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.kmelia.notification;

import javax.inject.Named;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.notification.jsondiff.Operation;

import com.silverpeas.admin.notification.ComponentJsonPatch;
import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;
import org.silverpeas.util.StringUtil;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.ADMIN_COMPONENT_TOPIC;
import static com.silverpeas.notification.RegisteredTopics.fromName;
import static com.silverpeas.notification.SilverpeasNotificationCause.UPDATE;
import static org.silverpeas.attachment.AttachmentService.VERSION_MODE;

/**
 *
 * @author ehugonnet
 */
@Named
public class ConfigurationChangeListener extends DefaultNotificationSubscriber {

  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(ADMIN_COMPONENT_TOPIC));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(ADMIN_COMPONENT_TOPIC));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    if (ADMIN_COMPONENT_TOPIC == fromName(onTopic.getName()) && notification.getCause() == UPDATE) {
      ComponentJsonPatch patch = (ComponentJsonPatch) notification.getObject();
      if ("kmelia".equalsIgnoreCase(patch.getComponentType())) {
        Operation operation = patch.getOperationByPath(VERSION_MODE);
        if (operation != null) {
          AttachmentServiceFactory.getAttachmentService().switchComponentBehaviour(notification
              .getSource().getComponentInstanceId(), StringUtil.getBooleanValue(operation
              .getValue()));
        }
      }
    }
  }
}
