/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.gallery;

import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractSilverpeasContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.ServiceProvider;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

/**
 * The gallery implementation of SilverpeasContentManager.
 */
@Service
public class GalleryContentManager extends AbstractSilverpeasContentManager implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final String CONTENT_ICON_FILE_NAME = "gallerySmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected GalleryContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    return Optional.ofNullable(getGalleryService()
        .getMedia(new MediaPK(resourceId, componentInstanceId),
            MediaCriteria.VISIBILITY.FORCE_GET_ALL));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    return (List) resourceReferences.stream()
        .collect(groupingBy(ResourceReference::getComponentInstanceId,
                 mapping(ResourceReference::getLocalId, toList())))
        .entrySet().stream()
        .flatMap(e -> getGalleryService().getMedia(e.getValue(), e.getKey(), MediaCriteria.VISIBILITY.FORCE_GET_ALL).stream())
        .collect(toList());
  }

  @Override
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      final T contribution) {
    return null;
  }

  private GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }
}
