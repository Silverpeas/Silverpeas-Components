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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.kmelia.export;

import org.silverpeas.components.kmelia.model.KmeliaPublication;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.contribution.publication.model.PublicationPK;

import java.util.List;
import java.util.UUID;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * A default export file name producer.
 * It computes the name of export file according to the name of the publication and of the space and
 * component instance the publication belongs to.
 */
public class DefaultExportFileNameProducer implements ExportFileNameProducer {

  public static DefaultExportFileNameProducer aDefaultExportFileNameProducer() {
    return new DefaultExportFileNameProducer();
  }

  @Override
  public String getPublicationExportFileName(KmeliaPublication publication, String language) {
    StringBuilder fileName = new StringBuilder(250);

    // add space path to filename
    List<SpaceInstLight> listSpaces = getSpacePathOf(publication);
    for (SpaceInstLight space : listSpaces) {
      fileName.append(space.getName(language)).append('-');
    }
    // add component name to filename
    fileName.append(getComponentLabelOf(publication.getPk().getInstanceId()));

    fileName.append('-').append(publication.getDetail().getTitle()).append('-');
    fileName.append(publication.getPk().getId()).append('-').append(UUID.randomUUID().hashCode());
    return fileName.toString();
  }

  private List<SpaceInstLight> getSpacePathOf(final KmeliaPublication publication) {
    List<SpaceInstLight> spacePath;
    PublicationPK id = publication.getPk();
    if (isDefined(id.getSpaceId())) {
      spacePath = getOrganisationController().getPathToSpace(id.getSpaceId());
    } else {
      spacePath = getOrganisationController().getPathToComponent(id.getInstanceId());
    }
    return spacePath;
  }

  private String getComponentLabelOf(String componentId) {
    return getOrganisationController().getComponentInst(componentId).getLabel();
  }

  private OrganizationController getOrganisationController() {
    return OrganizationControllerProvider.getOrganisationController();
  }
}
