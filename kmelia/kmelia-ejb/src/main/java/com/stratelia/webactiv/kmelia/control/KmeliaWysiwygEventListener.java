/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.kmelia.control;

import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
import com.stratelia.webactiv.publication.model.PublicationPK;
import org.silverpeas.contribution.model.ContributionIdentifier;
import org.silverpeas.notification.JMSResourceEventListener;
import org.silverpeas.wysiwyg.control.WysiwygContent;
import org.silverpeas.wysiwyg.notification.WysiwygEvent;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 * @author mmoquillon
 */
@MessageDriven(name = "KmeliaWysiwygEventListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
        propertyValue = "topic/wysiwyg"),
    @ActivationConfigProperty(propertyName = "destinationType",
        propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode",
        propertyValue = "Auto-acknowledge")})
public class KmeliaWysiwygEventListener extends JMSResourceEventListener<WysiwygEvent> {

  @Inject
  private KmeliaBm kmeliaBm;

  @Override
  protected Class<WysiwygEvent> getResourceEventClass() {
    return WysiwygEvent.class;
  }

  @Override
  public void onUpdate(final WysiwygEvent event) throws Exception {
    WysiwygContent content = event.getTransition().getAfter();
    if (content != null) {
      anExternalPublicationElementHaveChanged(content);
    }
  }

  @Override
  public void onCreation(final WysiwygEvent event) throws Exception {
    WysiwygContent content = event.getTransition().getAfter();
    if (content != null) {
      anExternalPublicationElementHaveChanged(content);
    }
  }

  private void anExternalPublicationElementHaveChanged(WysiwygContent content) {
    if (isAboutKmeliaPublication(content)) {
      ContributionIdentifier id = content.getContributionId();
      PublicationPK pubPK =
          new PublicationPK(id.getLocalId(), id.getComponentInstanceId());
      kmeliaBm.externalElementsOfPublicationHaveChanged(pubPK, content.getAuthorId());
    }
  }

  private boolean isAboutKmeliaPublication(WysiwygContent content) {
    return !content.getContributionId().getLocalId().startsWith("Node") && (
        content.getContributionId().getComponentInstanceId().startsWith("kmelia") ||
            content.getContributionId().getComponentInstanceId().startsWith("kmax") ||
            content.getContributionId().getComponentInstanceId().startsWith("kmelia") ||
            content.getContributionId().getComponentInstanceId().startsWith("toolbox"));
  }
}
