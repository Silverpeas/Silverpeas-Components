/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.community;

import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Once the Community application instance created, constructs an empty community of users for the
 * resource in Silverpeas specified in the instance parameter.
 */
@Bean
public class CommunityPostConstruction implements ComponentInstancePostConstruction {

  @Inject
  private OrganizationController controller;

  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    ComponentInst instance = controller.getComponentInst(componentInstanceId);
    if (instance == null) {
      throw new IllegalStateException("The Community application " + componentInstanceId +
          " should be created!");
    }
    String resourceId = instance.getSpaceId();
    CommunityOfUsers community = new CommunityOfUsers(componentInstanceId, resourceId);
    community.save();
  }
}