/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Prepares for the spawned WhitePages instance a publication template from which contact cards
 * will be created.
 * @author mmoquillon
 */
@Technical
@Bean
@Named
public class WhitePagesInstancePostConstruction implements ComponentInstancePostConstruction {

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    try {
      String template = AdministrationServiceProvider.getAdminService()
          .getComponentParameterValue(componentInstanceId, "cardTemplate");
      PublicationTemplateManager.getInstance()
          .addDynamicPublicationTemplate(componentInstanceId, template);
    } catch (PublicationTemplateException ex) {
      throw new SilverpeasRuntimeException(ex.getMessage(), ex);
    }
  }
}
