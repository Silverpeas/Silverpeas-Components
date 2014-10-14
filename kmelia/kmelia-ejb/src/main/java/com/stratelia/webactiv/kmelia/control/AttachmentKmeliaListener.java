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


import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.attachment.notification.AttachmentEvent;
import org.silverpeas.attachment.notification.AttachmentRef;
import org.silverpeas.notification.JMSResourceEventListener;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 * @author neysseri
 */
@MessageDriven(name = "KmeliaAttachmentEventListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
        propertyValue = "topic/attachments"),
    @ActivationConfigProperty(propertyName = "destinationType",
        propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode",
        propertyValue = "Auto-acknowledge")})
public class AttachmentKmeliaListener extends JMSResourceEventListener<AttachmentEvent> {

  @Inject
  private KmeliaBm kmeliaBm;


  @Override
  protected Class<AttachmentEvent> getResourceEventClass() {
    return AttachmentEvent.class;
  }

  @Override
  public void onDeletion(final AttachmentEvent event) throws Exception {
    AttachmentRef attachment = event.getResource();
    if (attachment != null) {
      anExternalPublicationElementHaveChanged(attachment, null);
    }
  }

  @Override
  public void onUpdate(final AttachmentEvent event) throws Exception {
    AttachmentRef attachment = event.getResource();
    if (attachment != null) {
      anExternalPublicationElementHaveChanged(attachment, attachment.getUserId());
    }
  }

  @Override
  public void onCreation(final AttachmentEvent event) throws Exception {
    AttachmentRef attachment = event.getResource();
    if (attachment != null) {
      anExternalPublicationElementHaveChanged(attachment, attachment.getUserId());
    }
  }

  private void anExternalPublicationElementHaveChanged(AttachmentRef attachment, String userId) {
    if (isAboutKmeliaPublication(attachment)) {
      PublicationPK pubPK =
          new PublicationPK(attachment.getForeignId(), attachment.getInstanceId());
      kmeliaBm.externalElementsOfPublicationHaveChanged(pubPK, userId);
    }
  }

  private boolean isAboutKmeliaPublication(AttachmentRef attachment) {
    return !attachment.getForeignId().startsWith("Node") && (
        attachment.getInstanceId().startsWith("kmelia") ||
            attachment.getInstanceId().startsWith("kmax") ||
            attachment.getInstanceId().startsWith("kmelia") ||
            attachment.getInstanceId().startsWith("toolbox"));
  }
}