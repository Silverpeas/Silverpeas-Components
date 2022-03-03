/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.kmelia.service;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.contribution.content.wysiwyg.notification.WysiwygEvent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;

/**
 * @author mmoquillon
 */
@Bean
public class KmeliaWysiwygEventListener extends CDIResourceEventListener<WysiwygEvent> {

  @Inject
  private KmeliaService kmeliaService;

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
      ContributionIdentifier id = content.getContribution().getContributionId();
      PublicationPK pubPK =
          new PublicationPK(id.getLocalId(), id.getComponentInstanceId());
      kmeliaService.externalElementsOfPublicationHaveChanged(pubPK, content.getAuthor().getId());
    }
  }

  private boolean isAboutKmeliaPublication(WysiwygContent content) {
    final Contribution contribution = content.getContribution();
    return !contribution.getContributionId().getLocalId().startsWith("Node") && (
        contribution.getContributionId().getComponentInstanceId().startsWith("kmax") ||
        contribution.getContributionId().getComponentInstanceId().startsWith("kmelia") ||
        contribution.getContributionId().getComponentInstanceId().startsWith("toolbox"));
  }
}
