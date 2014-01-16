/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.components.saasmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.components.saasmanager.vo.UserVO;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * SAAS space service
 * @author ahedin
 *
 */
public class SaasSpaceService {

  private Admin admin;

  public SaasSpaceService() {
    admin = new Admin();
  }

  /**
   * Creates the space of the SAAS access.
   * @param access The SAAS access.
   * @return The id of the new space.
   * @throws SaasManagerException
   */
  public String createSpace(SaasAccess access)
  throws SaasManagerException {
    SpaceInst spaceInst = new SpaceInst();
    spaceInst.setName(getSpaceName(access));
    spaceInst.setDescription("");
    spaceInst.setLanguage(access.getLang());
    spaceInst.setCreatorUserId(access.getUserId());

    String spaceId = null;
    try {
      spaceId = admin.addSpaceInst(access.getUserId(), spaceInst);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.createSpace()",
        SilverpeasException.ERROR, "saasmanager.EX_CREATE_SPACE", e);
    }

    updateOrders(spaceId);

    addUserRole(spaceId, access.getUserId(), access.getUserId(), UserVO.ROLE_ADMIN);

    return spaceId;
  }

  /**
   * Updates orders of all root spaces.
   * @param spaceId The id of the space to put at the end.
   * @throws SaasManagerException
   */
  private void updateOrders(String spaceId)
  throws SaasManagerException {
    ArrayList<SpaceInst> rootSpaces = getRootSpaces(spaceId);
    try {
      int order = 0;
      for (SpaceInst space : rootSpaces) {
        if (space.getOrderNum() != order) {
          admin.updateSpaceOrderNum(space.getId(), order);
        }
        order++;
      }

      SpaceInst space = getSpace(spaceId);
      admin.updateSpaceOrderNum(space.getId(), order);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.updateOrders()",
        SilverpeasException.ERROR, "saasmanager.EX_ORDER_SPACES", e);
    }
  }

  /**
   * @param exceptedSpaceId The id of the space to not put into the result list.
   * @return A list of all root sorted spaces excepted the one which id is given as a parameter.
   * @throws SaasManagerException
   */
  private ArrayList<SpaceInst> getRootSpaces(String exceptedSpaceId)
  throws SaasManagerException {
    try {
      String[] spaceIds = admin.getAllRootSpaceIds();
      ArrayList<SpaceInst> rootSpaces = new ArrayList<SpaceInst>();
      for (int i = 0; i < spaceIds.length; i++) {
        SpaceInst space = getSpace(spaceIds[i]);
        if (!space.getId().equals(exceptedSpaceId)) {
          rootSpaces.add(space);
        }
      }

      Collections.sort(rootSpaces, new Comparator<SpaceInst>() {
        @Override
        public int compare(SpaceInst o1, SpaceInst o2) {
          return o1.getOrderNum() - o2.getOrderNum();
        }
      });

      return rootSpaces;
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.getRootSpaces)",
        SilverpeasException.ERROR, "saasmanager.EX_GET_ROOT_SPACES", e);
    }
  }

  /**
   * @param spaceId The id of the searched space.
   * @return The space corresponding to the id.
   * @throws SaasManagerException
   */
  private SpaceInst getSpace(String spaceId)
  throws SaasManagerException {
    try {
      return admin.getSpaceInstById(spaceId);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.getRootSpaces)",
        SilverpeasException.ERROR, "saasmanager.EX_GET_SPACE", e);
    }
  }

  /**
   * @param access The SAAS access.
   * @return The name to give to the space of the SAAS access.
   * @throws SaasManagerException
   */
  private String getSpaceName(SaasAccess access)
  throws SaasManagerException {
    String[] spaceNames = null;
    try {
      String[] spaceIds = admin.getAllSpaceIds();
      spaceNames = admin.getSpaceNames(spaceIds);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.getRootSpaces()",
          SilverpeasException.ERROR, "admin.MSG_ERR_CREATE_SPACE", e);
    }
    ArrayList<String> spaceNameList = new ArrayList<String>();
    for (String spaceName : spaceNames) {
      spaceNameList.add(spaceName.toLowerCase());
    }

    // Company's name if it is defined and available.
    String spaceName = access.getCompany();
    if (!StringUtil.isDefined(spaceName) || spaceNameList.contains(spaceName.toLowerCase())) {
      // Else first and last names if they are defined and available.
      spaceName = access.getFirstName() + " " + access.getLastName();
      if (!StringUtil.isDefined(spaceName) || spaceNameList.contains(spaceName.toLowerCase())) {
        // Else a default name containing the domain's id.
        spaceName = "Space of domain " + access.getDomainId();
      }
    }
    return spaceName;
  }

  /**
   * @param spaceId The space's id.
   * @param userId The user's id.
   * @return true if the user is administrator of the space.
   * @throws SaasManagerException
   */
  public boolean isAdmin(String spaceId, String userId)
  throws SaasManagerException {
    SpaceInst space = getSpace(spaceId);
    SpaceProfileInst spaceProfile = space.getSpaceProfileInst(UserVO.ROLE_ADMIN);
    ArrayList<String> userIds = spaceProfile.getAllUsers();
    return userIds.contains(userId);
  }

  /**
   * Updates the home page of the SAAS space.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  public void updateSpaceFirstPage(SaasAccess access)
  throws SaasManagerException {
    SpaceInst space = getSpace(access.getSpaceId());
    space.setFirstPageType(SpaceInst.FP_TYPE_COMPONENT_INST);
    space.setFirstPageExtraParam(access.getHomeComponentId());
    try {
      admin.updateSpaceInst(space);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasSpaceService.updateSpaceFirstPage()",
        SilverpeasException.ERROR, "saasmanager.EX_UPDATE_SPACE", e);
    }
  }

  /**
   * 
   * @param spaceId The space's id.
   * @param userId The user's id.
   * @return The role of the user on the space.
   * @throws SaasManagerException
   */
  public String getUserRole(String spaceId, String userId)
  throws SaasManagerException {
    SpaceInst space = getSpace(spaceId);
    ArrayList<String> profileUserIds;
    for (String role : UserVO.ROLES) {
      SpaceProfileInst spaceProfile = space.getSpaceProfileInst(role);
      if (spaceProfile != null) {
        profileUserIds = spaceProfile.getAllUsers();
        if (profileUserIds != null && profileUserIds.contains(userId)) {
          return role;
        }
      }
    }
    return UserVO.ROLE_READER;
  }

  /**
   * Adds the user's role to the space.
   * @param spaceId The space's id.
   * @param userId The administrator user's id.
   * @param id The updated user's id.
   * @param role The user's role.
   * @throws SaasManagerException
   */
  public void addUserRole(String spaceId, String userId, String id, String role)
  throws SaasManagerException {
    try {
      SpaceInst space = getSpace(spaceId);
      SpaceProfileInst spaceProfile = space.getSpaceProfileInst(role);
      if (spaceProfile != null) {
        spaceProfile.addUser(id);
        admin.updateSpaceProfileInst(spaceProfile, userId);
      } else {
        spaceProfile = new SpaceProfileInst();
        spaceProfile.setSpaceFatherId(spaceId);
        spaceProfile.setName(role);
        spaceProfile.addUser(id);
        admin.addSpaceProfileInst(spaceProfile, userId);
      }
    } catch (AdminException e) {
      throw new SaasManagerException("SaasUserService.addUserRole()", SilverpeasException.ERROR,
        "saasmanager.EX_ADD_USER_ROLE", "spaceId=" + spaceId + "; userId=" + userId
        + "; role=" + role, e);
    }
  }

  /**
   * Removes the user's role from the space.
   * @param spaceId The space's id.
   * @param userId The administrator user's id.
   * @param id The updated user's id.
   * @param role The user's role.
   * @throws SaasManagerException
   */
  public void removeUserRole(String spaceId, String userId, String id, String role)
  throws SaasManagerException {
    try {
      SpaceInst space = getSpace(spaceId);
      SpaceProfileInst spaceProfile = space.getSpaceProfileInst(role);
      if (spaceProfile != null) {
        spaceProfile.removeUser(id);
        admin.updateSpaceProfileInst(spaceProfile, userId);
      }
    } catch (AdminException e) {
      throw new SaasManagerException("SaasUserService.removeUserRole()", SilverpeasException.ERROR,
        "saasmanager.EX_REMOVE_USER_ROLE", "spaceId=" + spaceId + "; userId=" + userId
        + "; role=" + role, e);
    }
  }

  /**
   * 
   * @param spaceId The space's id.
   * @return The list of ids of users who are administrators of the space.
   * @throws SaasManagerException
   */
  public ArrayList<String> getAdminUserIds(String spaceId)
  throws SaasManagerException {
    SpaceInst space = getSpace(spaceId);
    SpaceProfileInst spaceProfile = space.getSpaceProfileInst(UserVO.ROLE_ADMIN);
    return spaceProfile.getAllUsers();
  }
  
}
