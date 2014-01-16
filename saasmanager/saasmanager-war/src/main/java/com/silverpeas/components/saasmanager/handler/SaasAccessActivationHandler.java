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

package com.silverpeas.components.saasmanager.handler;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.components.saasmanager.control.SaasDomainController;
import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.components.saasmanager.service.SaasAccessService;
import com.silverpeas.components.saasmanager.service.SaasComponentService;
import com.silverpeas.components.saasmanager.service.SaasMailService;
import com.silverpeas.components.saasmanager.service.SaasSpaceService;
import com.silverpeas.components.saasmanager.service.SaasUserService;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Handler which manages actions linked to a SAAS access activation.
 * @author ahedin
 */
public class SaasAccessActivationHandler extends Handler {

  private SaasAccessService accessService;
  private SaasDomainController domainController;
  private SaasUserService userService;
  private SaasMailService mailService;
  private SaasSpaceService spaceService;
  private SaasComponentService componentService;

  public SaasAccessActivationHandler() {
    accessService = new SaasAccessService();
    domainController = new SaasDomainController();
    userService = new SaasUserService();
    mailService = new SaasMailService();
    spaceService = new SaasSpaceService();
    componentService = new SaasComponentService();
  }

  @Override
  public String getPage(HttpServletRequest request) {
    String uid = request.getParameter("uid");
    try {
      SaasAccess access = accessService.getAccess(uid);
      activate(access);
    } catch (SaasManagerException e) {
      SilverTrace.error("SaasManager", "SaasAccessActivationHandler.getPage()",
        "saasmanager.EX_ACTIVATE_ACCESS", "uid=" + uid, e);
      return "Error";
    }
    return "Success";
  }

  /**
   * Activate the access
   * @param access the SAAS access
   * @throws SaasManagerException
   */
  private void activate(SaasAccess access)
  throws SaasManagerException {
    // Domain creation
    createDomain(access);

    // User creation
    createUser(access);

    // Space creation
    createSpace(access);

    // Components creation
    createComponents(access);

    // Update user's preferences
    updateUserPreferences(access, access.getUserId());

    // Send notification to user
    notifyUser(access);

    // Achieve access
    achieve(access);
  }

  /**
   * Creates the domain of the SAAS access.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void createDomain(SaasAccess access)
  throws SaasManagerException {
    if (!StringUtil.isDefined(access.getDomainId())) {
      // Access domain id is defined only if a previous activation has been launched but not ended
      // successfully
      String domainId = domainController.createDomain();
      accessService.updateAccessDomainId(access, domainId);
    }
  }

  /**
   * Creates the administrator user of the SAAS access.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void createUser(SaasAccess access)
  throws SaasManagerException {
    if (!StringUtil.isDefined(access.getUserId())) {
      // Access user id is defined only if a previous activation has been launched but not ended
      // successfully
      String login = userService.getLogin(access);
      String password = userService.getPassword();
      String userId = userService.createUser(access, login, password);
      accessService.updateAccessUser(access, userId, login, password);
    }
  }

  /**
   * Creates the space of the SAAS access.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void createSpace(SaasAccess access)
  throws SaasManagerException {
    if (!StringUtil.isDefined(access.getSpaceId())) {
      // Access space id is defined only if a previous activation has been launched but not ended
      // successfully
      String spaceId = spaceService.createSpace(access);
      accessService.updateAccessSpaceId(access, spaceId);
    }
  }

  /**
   * Creates the components of the SAAS access.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void createComponents(SaasAccess access)
  throws SaasManagerException {
    boolean componentsCreated = false;

    if (!StringUtil.isDefined(access.getComponentIds())) {
      // Access components ids are defined only if a previous activation has been launched but not
      // ended successfully
      ArrayList<String> componentIds = componentService.createComponents(access);
      accessService.updateAccessComponentIds(access, componentIds);
      componentsCreated = true;
    }

    if (!StringUtil.isDefined(access.getHomeComponentId())) {
      // Access home component id is defined only if a previous activation has been launched but not
      // ended successfully
      String homeComponentId = componentService.createHomeComponent(access);
      accessService.updateAccessHomeComponentId(access, homeComponentId);
      componentService.fillHomeComponent(access);
      spaceService.updateSpaceFirstPage(access);
      componentsCreated = true;
    }

    if (!StringUtil.isDefined(access.getManagementComponentId())) {
      // Access management component id is defined only if a previous activation has been launched
      // but not ended successfully
      String managementComponentId = componentService.createManagementComponent(access);
      accessService.updateAccessManagementComponentId(access, managementComponentId);
      componentsCreated = true;
    }

    if (componentsCreated) {
      componentService.orderComponents(access);
    }
  }

  /**
   * Updates the preferences of the user.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void updateUserPreferences(SaasAccess access, String userId) {
    userService.updatePreferences(access, userId);
  }

  /**
   * Notifies the user of the SAAS access by sending him his connection informations.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void notifyUser(SaasAccess access)
  throws SaasManagerException {
    mailService.sendLoginMail(access);
  }

  /**
   * Achieves the SAAS access.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  private void achieve(SaasAccess access)
    throws SaasManagerException {
    accessService.updateAccessAchievementDate(access);
  }

}
