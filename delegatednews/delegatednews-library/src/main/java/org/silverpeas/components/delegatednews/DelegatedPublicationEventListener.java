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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.delegatednews;

import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.components.delegatednews.service.DelegatedNewsService;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.notification.PublicationEvent;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Inject;

import static org.silverpeas.components.delegatednews.service.DelegatedNewsService.Constants.DELEGATED_COMPONENT_PARAM;

/**
 * @author mmoquillon
 */
@Bean
public class DelegatedPublicationEventListener extends CDIResourceEventListener<PublicationEvent> {

  @Inject
  private DelegatedNewsService delegatedNewsService;

  @Override
  public void onUpdate(PublicationEvent event) {
    final PublicationDetail pubDetail = event.getTransition().getAfter();
    if (isDelegatedNewsActivated(pubDetail.getInstanceId())) {
      final DelegatedNews delegatedNews = delegatedNewsService.getDelegatedNews(pubDetail.getId());
      if (delegatedNews != null) {
        delegatedNewsService.updateDelegatedNews(pubDetail.getIdentifier(),
            pubDetail.getUpdaterId(), pubDetail.getVisibility().getSpecificPeriod().orElse(null));
      }
    }
  }

  @Override
  public void onDeletion(final PublicationEvent event) {
    final PublicationDetail pubDetail = event.getTransition().getBefore();
    if (isDelegatedNewsActivated(pubDetail.getInstanceId())) {
      delegatedNewsService.deleteDelegatedNews(pubDetail.getId());
    }
  }

  private boolean isDelegatedNewsActivated(String componentId) {
    final String paramValue = OrganizationControllerProvider.getOrganisationController()
        .getComponentParameterValue(componentId, DELEGATED_COMPONENT_PARAM);
    return StringUtil.getBooleanValue(paramValue);
  }
}
