/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.components.kmelia;

import org.silverpeas.core.contribution.contentcontainer.content.AbstractContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;

import javax.inject.Singleton;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The kmelia implementation of ContentInterface.
 */
@Singleton
public class KmeliaContentManager extends AbstractContentInterface implements Serializable {
  private static final long serialVersionUID = 3525407153404515235L;

  private static final String CONTENT_ICON_FILE_NAME = "kmeliaSmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected KmeliaContentManager() {
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
  protected List<Contribution> getAccessibleContributions(final List<String> resourceIds,
      final String componentInstanceId, final String currentUserId) {
    final KmeliaAuthorization security = new KmeliaAuthorization();
    final boolean checkRights = security.isRightsOnTopicsEnabled(componentInstanceId);

    final List<PublicationPK> ids =
        resourceIds.stream().map(i -> new PublicationPK(i, componentInstanceId))
            .collect(Collectors.toList());
    Stream<PublicationDetail> publications = getPublicationService().getPublications(ids).stream();
    if (checkRights) {
      publications =
          publications.filter(p -> security.isPublicationAvailable(p.getPK(), currentUserId));
    }
    return publications.collect(Collectors.toList());
  }

  /**
   * delete a content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PublicationPK pubPK)
      throws ContentManagerException {
    deleteSilverContent(con, pubPK.getId(), pubPK.getComponentName());
  }

  @Override
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      final T contribution) {
    final PublicationDetail pubDetail = (PublicationDetail) contribution;
    return new SilverContentVisibility(pubDetail.getBeginDate(), pubDetail.getEndDate(),
        isVisible(pubDetail));
  }

  private boolean isVisible(PublicationDetail pubDetail) {
    return PublicationDetail.VALID_STATUS.equals(pubDetail.getStatus());
  }

  private PublicationService getPublicationService() {
    return PublicationService.get();
  }
}