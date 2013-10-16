/*
 *  Copyright (C) 2000 - 2013 Silverpeas
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.stratelia.webactiv.kmelia.control;


import javax.inject.Named;

import org.silverpeas.attachment.notification.AttachmentDeletionNotification;
import org.silverpeas.attachment.notification.AttachmentRef;

import com.silverpeas.notification.DefaultNotificationSubscriber;
import com.silverpeas.notification.NotificationTopic;
import com.silverpeas.notification.SilverpeasNotification;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

import static com.silverpeas.notification.NotificationTopic.onTopic;
import static com.silverpeas.notification.RegisteredTopics.ATTACHMENT_TOPIC;

/**
 * @author neysseri
 */
@Named
public class AttachmentKmeliaListener extends DefaultNotificationSubscriber {

  @Override
  public void subscribeOnTopics() {
    subscribeForNotifications(onTopic(ATTACHMENT_TOPIC.getTopicName()));
  }

  @Override
  public void unsubscribeOnTopics() {
    unsubscribeForNotifications(onTopic(ATTACHMENT_TOPIC.getTopicName()));
  }

  @Override
  public void onNotification(SilverpeasNotification notification, NotificationTopic onTopic) {
    if (ATTACHMENT_TOPIC.getTopicName().equals(onTopic.getName()) &&
        notification instanceof AttachmentDeletionNotification) {
      AttachmentDeletionNotification deletion = (AttachmentDeletionNotification) notification;
      AttachmentRef attachment = deletion.getAttachment();
      if (attachment != null) {
        PublicationPK pubPK =  new PublicationPK(attachment.getForeignId(), attachment.getInstanceId());
        anExternalPublicationElementHaveChanged(pubPK);

      }
    }
  }

  private void anExternalPublicationElementHaveChanged(PublicationPK pubPK) {
    try {
      getKmeliaBm().externalElementsOfPublicationHaveChanged(pubPK, null, -1);
    } catch (Exception e) {
      // if exception is throw, JMS will attempt to execute it again and again...
      SilverTrace.error("kmelia", "AttachmentKmeliaListener.onNotification",
          "kmelia.EX_IMPOSSIBLE_DE_MODIFIER_LA_PUBLICATION", "pubPK = " + pubPK.toString(), e);
    }
  }

  private KmeliaBm getKmeliaBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
    } catch (Exception e) {
      throw new KmeliaRuntimeException("AttachmentKmeliaListener.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }
}