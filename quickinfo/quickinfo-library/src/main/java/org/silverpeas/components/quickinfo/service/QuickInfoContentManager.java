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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quickinfo.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractSilverpeasContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuickInfoContentManager extends AbstractSilverpeasContentManager {

  private static final String CONTENT_ICON_FILE_NAME = "quickinfoSmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected QuickInfoContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    return Optional.ofNullable(
        getPublicationService().getDetail(new PublicationPK(resourceId, componentInstanceId)));
  }

  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    List<PublicationPK> pks = resourceReferences.stream()
        .map(r -> new PublicationPK(r.getLocalId(), r.getComponentInstanceId()))
        .collect(Collectors.toList());
    return new ArrayList<>(getPublicationService().getPublications(pks));
  }

  @Override
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      final T contribution) {
    final PublicationDetail pubDetail = (PublicationDetail) contribution;
    return new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(), false);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PublicationPK pubPK)
      throws ContentManagerException {
    deleteSilverContent(con, pubPK.getId(), pubPK.getComponentName());
  }

  private PublicationService getPublicationService() {
    return PublicationService.get();
  }
}
