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
package org.silverpeas.components.infoletter;

import org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC;
import org.silverpeas.components.infoletter.model.InfoLetterService;
import org.silverpeas.components.infoletter.service.InfoLetterServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.silverpeas.components.infoletter.model.InfoLetterPublication.PUBLICATION_VALIDEE;

/**
 * The infoletter implementation of ContentInterface.
 */
@Service
public class InfoLetterContentManager extends AbstractContentInterface {

  private static final String CONTENT_ICON_FILE_NAME = "infoLetterSmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected InfoLetterContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    return Optional.ofNullable(getInfoLetterPublicationPdC(resourceId, componentInstanceId));
  }

  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    return resourceReferences.stream()
        .map(r -> getInfoLetterPublicationPdC(r.getLocalId(), r.getComponentInstanceId()))
        .collect(Collectors.toList());
  }

  @Override
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      final T contribution) {
    final InfoLetterPublicationPdC ilPub = (InfoLetterPublicationPdC) contribution;
    return new SilverContentVisibility(isVisible(ilPub));
  }

  private boolean isVisible(InfoLetterPublicationPdC ilPub) {
    return ilPub.getPublicationState() == PUBLICATION_VALIDEE;
  }

  /**
   * Gets a publication from info letter.
   * @param resourceId a publication identifier
   * @param componentId the id of the instance
   * @return a list of publicationDetail
   */
  private Contribution getInfoLetterPublicationPdC(String resourceId, String componentId) {
    InfoLetterPublicationPdC ilPub =
        getDataInterface().getInfoLetterPublication(new IdPK(resourceId));
    ilPub.setInstanceId(componentId);
    return ilPub;
  }

  private InfoLetterService getDataInterface() {
    return InfoLetterServiceProvider.getInfoLetterData();
  }
}