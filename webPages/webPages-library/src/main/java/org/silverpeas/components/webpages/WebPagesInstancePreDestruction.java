/*
 * Copyright (C) 2000 - 2022 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.webpages;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Named;

/**
 * Deletes all the web pages managed by the WebPages instance that is being deleted.
 * @author mmoquillon
 */
@Named
public class WebPagesInstancePreDestruction implements ComponentInstancePreDestruction {
  /**
   * Performs pre destruction tasks in the behalf of the specified component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  @Override
  public void preDestroy(final String componentInstanceId) {
    OrganizationController organizationController =
        OrganizationControllerProvider.getOrganisationController();
    String xmlFormName =
        organizationController.getComponentParameterValue(componentInstanceId, "xmlTemplate");
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlShortName =
          xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
      try {
        PublicationTemplateManager.getInstance()
            .removePublicationTemplate(componentInstanceId + ":" + xmlShortName);
      } catch (PublicationTemplateException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }
}
