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
import org.silverpeas.components.community.notification.CommunityEventNotifier;
import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Once the Community application instance created, constructs an empty community of users for the
 * collaborative space in which the application instance has been spawned. The community application
 * instance becomes automatically the home page of the parent space and this parent space doesn't
 * inherit anymore the access rights defined in its own parent spaces.
 */
@Bean
@Named
public class CommunityInstancePostConstruction implements ComponentInstancePostConstruction {

  @Inject
  private Administration admin;

  @Inject
  private CommunityOfUsersRepository repository;

  @Inject
  private CommunityEventNotifier notifier;


  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    ComponentInst instance = getComponentInst(componentInstanceId);
    SpaceInst spaceInst = getSpaceInst(instance.getSpaceId());
    CommunityOfUsers community = new CommunityOfUsers(instance.getId(), spaceInst.getId());
    CommunityOfUsers savedCommunity = repository.save(community);

    if (!spaceInst.isCommunitySpace()) {
      spaceInst.setFirstPageExtraParam(componentInstanceId);
      spaceInst.setInheritanceBlocked(true);
      spaceInst.setCommunitySpace(true);
      spaceInst.setFirstPageType(SpaceHomePageType.COMPONENT_INST.ordinal());
    }
    spaceInst.setFirstPageExtraParam(componentInstanceId);
    spaceInst.setFirstPageType(SpaceHomePageType.COMPONENT_INST.ordinal());
    updateSpaceInst(spaceInst);

    notifier.notifyEventOn(ResourceEvent.Type.CREATION, savedCommunity);
  }

  private ComponentInst getComponentInst(final String instanceId) {
    try {
      return admin.getComponentInst(instanceId);
    } catch (AdminException e) {
      throw new IllegalStateException(
          "The Community application " + instanceId + " should be created!");
    }
  }

  private SpaceInst getSpaceInst(final String spaceId) {
    try {
      return admin.getSpaceInstById(spaceId);
    } catch (AdminException e) {
      throw new IllegalStateException(
          "The space " + spaceId + " should exist!");
    }
  }

  private void updateSpaceInst(final SpaceInst spaceInst) {
    try {
      admin.updateSpaceInst(spaceInst);
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e.getMessage());
    }
  }
}