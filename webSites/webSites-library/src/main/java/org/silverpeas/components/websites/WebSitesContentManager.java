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
package org.silverpeas.components.websites;

import org.silverpeas.components.websites.service.WebSiteService;
import org.silverpeas.components.websites.model.SiteDetail;
import org.silverpeas.components.websites.model.SitePK;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractSilverpeasContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

/**
 * The webSites implementation of SilverpeasContentManager.
 */
@Service
public class WebSitesContentManager extends AbstractSilverpeasContentManager implements Serializable {
  private static final long serialVersionUID = -8992766242253326927L;
  private static final String WEB_SITES_CONTENT_ICON_FILE_NAME = "webSitesSmall.gif";
  private static final String BOOKMARK_CONTENT_ICON_FILE_NAME = "bookmarkSmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected WebSitesContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    if (componentInstanceId.startsWith("bookmark")) {
      return BOOKMARK_CONTENT_ICON_FILE_NAME;
    }
    return WEB_SITES_CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    return Optional.ofNullable(getWebSiteService().getWebSite(componentInstanceId, resourceId));
  }

  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    return resourceReferences.stream()
        .collect(groupingBy(ResourceReference::getComponentInstanceId,
                 mapping(ResourceReference::getLocalId, toList())))
        .entrySet().stream()
        .flatMap(e -> getWebSiteService().getWebSites(e.getKey(), e.getValue()).stream())
        .collect(toList());
  }

  @Override
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      final T contribution) {
    final SiteDetail siteDetail = (SiteDetail) contribution;
    return new SilverContentVisibility(isVisible(siteDetail));
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param sitePK the site identifier to delete
   * @throws ContentManagerException on technical error.
   */
  public void deleteSilverContent(Connection con, SitePK sitePK) throws ContentManagerException {
    deleteSilverContent(con, sitePK.getId(), sitePK.getComponentName());
  }

  private boolean isVisible(SiteDetail siteDetail) {
    return siteDetail.getState() == 1;
  }

  private WebSiteService getWebSiteService() {
    return WebSiteService.get();
  }
}
