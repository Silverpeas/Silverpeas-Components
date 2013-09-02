/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.export;

import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.kmelia.model.KmeliaPublication;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.silverpeas.core.admin.OrganisationController;

import java.util.List;
import java.util.UUID;
import static com.silverpeas.util.StringUtil.*;

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
    List<SpaceInst> listSpaces = getSpacePathOf(publication);
    for (SpaceInst space :
        listSpaces) {
      fileName.append(space.getName(language)).append('-');
    }
    // add component name to filename
    fileName.append(getComponentLabelOf(publication.getPk().getInstanceId()));

    fileName.append('-').append(publication.getDetail().getTitle()).append('-');
    fileName.append(publication.getPk().getId()).append('-').append(UUID.randomUUID().hashCode());
    return fileName.toString();
  }

  private List<SpaceInst> getSpacePathOf(final KmeliaPublication publication) {
    List<SpaceInst> spacePath;
    PublicationPK id = publication.getPk();
    if (isDefined(id.getSpaceId())) {
      spacePath = getOrganisationController().getSpacePath(id.getSpaceId());
    } else {
      spacePath = getOrganisationController().getSpacePathToComponent(id.getInstanceId());
    }
    return spacePath;
  }

  private String getComponentLabelOf(String componentId) {
    return getOrganisationController().getComponentInst(componentId).getLabel();
  }

  private OrganisationController getOrganisationController() {
    return new OrganizationController();
  }
}
