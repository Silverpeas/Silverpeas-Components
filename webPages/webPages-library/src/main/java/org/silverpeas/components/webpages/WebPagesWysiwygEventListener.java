/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.components.webpages;

import org.silverpeas.components.webpages.notification.WebPagesUserNotifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.contribution.content.wysiwyg.notification.WysiwygEvent;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.subscription.ResourceSubscriptionService;

import javax.inject.Inject;

/**
 * @author mmoquillon
 */
@Bean
public class WebPagesWysiwygEventListener extends CDIResourceEventListener<WysiwygEvent> {

  @Inject
  private OrganizationController organisationController;

  @Override
  public void onUpdate(final WysiwygEvent event) throws Exception {
    notifyUsersAboutChange(event.getTransition().getAfter());
  }

  @Override
  public void onCreation(final WysiwygEvent event) throws Exception {
    notifyUsersAboutChange(event.getTransition().getAfter());
  }

  private void notifyUsersAboutChange(WysiwygContent content) {
    String userId = content.getAuthor().getId();
    String componentId = content.getContribution().getContributionId().getComponentInstanceId();

    // If parameter useSubscription is used
    if ("yes".equals(organisationController.getComponentParameterValue(componentId,
        ResourceSubscriptionService.Constants.SUBSCRIPTION_PARAMETER))) {
      if (isAboutWebPage(content)) {
        WebPagesUserNotifier.notify(new NodePK("0", componentId), userId);
      }
    }
  }

  private boolean isAboutWebPage(WysiwygContent content) {
    final Contribution contribution = content.getContribution();
    return !contribution.getContributionId().getLocalId().startsWith("Node") &&
        contribution.getContributionId().getComponentInstanceId().startsWith("webPages");
  }
}
