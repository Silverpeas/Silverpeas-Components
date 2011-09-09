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
import javax.servlet.http.HttpSession;

import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.components.saasmanager.service.SaasAccessService;
import com.silverpeas.components.saasmanager.service.SaasMailService;
import com.silverpeas.components.saasmanager.service.SaasSpaceService;
import com.silverpeas.components.saasmanager.service.SaasUserService;
import com.silverpeas.components.saasmanager.vo.UserVO;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Handler which manages actions linked to the management of a space and a domain created in a SAAS
 * context.
 * @author ahedin
 */
public class SaasManagementHandler extends Handler {

  private SaasAccessService accessService;
  private SaasSpaceService spaceService;
  private SaasUserService userService;
  private SaasMailService mailService;

  public SaasManagementHandler() {
    accessService = new SaasAccessService();
    spaceService = new SaasSpaceService();
    userService = new SaasUserService();
    mailService = new SaasMailService();
  }

  @Override
  public String getPage(HttpServletRequest request) {
    try {
      SaasAccess access = init(request);
      String function = request.getParameter("function");
      if ("editUser".equals(function)) {
        // User edition
        return getEditUserDestination(request, access);
      } else if ("removeUser".equals(function)) {
        // User deletion
        return getRemoveUserDestination(request, access);
      } else if ("saveUser".equals(function)) {
        // User update
        return getSaveUserDestination(request, access);
      } else {
        // Main page
        return getMainDestination(request, access);
      }
    } catch (Exception e) {
      SilverTrace.error("SaasManager", "SaasManagementHandler.getPage()",
        "saasmanager.EX_MANAGE_ACCESS", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

  /**
   * Initializes data required in the different pages of SAAS space and domain management : SAAS
   * access, multilanguage resources, language, user's id, if user is admin or not.
   * @param request The HTTP request.
   * @return The SAAS access corresponding to the UID coming from the request.
   * @throws SaasManagerException
   */
  private SaasAccess init(HttpServletRequest request)
      throws SaasManagerException {
    String uid = request.getParameter("uid");
    String userId = request.getParameter("userId");

    SaasAccess access = accessService.getAccess(uid);

    HttpSession session = request.getSession(true);
    MainSessionController sessionController = (MainSessionController) session.getAttribute(
      MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    String language = sessionController.getFavoriteLanguage();

    ResourcesWrapper resources = new ResourcesWrapper(
      new ResourceLocator("com.silverpeas.saasmanager.multilang.SaasManagerBundle", language),
      new ResourceLocator("com.silverpeas.saasmanager.settings.SaasManagerIcons", ""),
      new ResourceLocator("com.silverpeas.saasmanager.settings.SaasManagerSettings", ""),
      language);

    request.setAttribute("resources", resources.getMultilangBundle());
    request.setAttribute("language", language);
    request.setAttribute("access", access);
    request.setAttribute("userId", userId);
    request.setAttribute("admin", spaceService.isAdmin(access.getSpaceId(), userId));
    
    // Update last access time to avoid timeout.
    SessionManager.getInstance().setLastAccess(session);

    return access;
  }

  /**
   * Loads users of the domain corresponding to the SAAS access.
   * @param request The HTTP request.
   * @param access The SAAS access.
   * @return The main page of SAAS management.
   * @throws SaasManagerException
   */
  private String getMainDestination(HttpServletRequest request, SaasAccess access)
  throws SaasManagerException {
    ArrayList<UserVO> users = userService.getUsers(access);
    for (UserVO user : users) {
      user.setRole(spaceService.getUserRole(access.getSpaceId(), user.getId()));
    }
    request.setAttribute("users", users);
    return "management.jsp";
  }

  /**
   * Loads the user corresponding to the id coming from the request.
   * @param request The HTTP request.
   * @param access The SAAS access.
   * @return The user edition page of SAAS management.
   * @throws SaasManagerException
   */
  private String getEditUserDestination(HttpServletRequest request, SaasAccess access)
  throws SaasManagerException {
    String id = request.getParameter("id");
    UserVO user;
    if (id.equals("-1")) {
      // User creation
      int usersCount = userService.getUsersCount(access);
      if (access.getUsersCount() == 0 || usersCount < access.getUsersCount()) {
        user = new UserVO();
      } else {
        // Maximum of users reached
        return getMessageDestination(request, "maxUsersReached");
      }
    } else {
      // User update
      user = userService.getUser(id);
      user.setRole(spaceService.getUserRole(access.getSpaceId(), user.getId()));
    }
    request.setAttribute("user", user);
    return "editUser.jsp";
  }

  /**
   * Removes the user corresponding to the id coming from the request.
   * @param request The HTTP request.
   * @param access The SAAS access.
   * @return The main page of SAAS management if the user has been removed, an information page if
   *         the user cannot be removed or the login page if the current user has removed himself.
   * @throws SaasManagerException
   */
  private String getRemoveUserDestination(HttpServletRequest request, SaasAccess access)
  throws SaasManagerException {
    int usersCount = userService.getUsersCount(access);
    if (usersCount == 1) {
      // Deletion not allowed : the domain must contain at least one user.
      return getMessageDestination(request, "oneUserRequired");
    }

    String id = request.getParameter("id");
    ArrayList<String> adminUserIds = spaceService.getAdminUserIds(access.getSpaceId());
    if (adminUserIds.size() == 1 && adminUserIds.contains(id)) {
      // Deletion not allowed : the domain must contain at least one administrator user.
      return getMessageDestination(request, "oneAdminUserRequired");
    }

    userService.removeUser(id);

    if (id.equals(request.getParameter("userId"))) {
      // The user has removed his own account
      return "logout.jsp";
    } else {
      return getMainDestination(request, access);
    }
  }

  /**
   * Saves the data of the user corresponding to the id coming from the request.
   * @param request The HTTP request.
   * @param access The SAAS access.
   * @return The main page of SAAS management if the user has been updated, an information page if
   *         an error occurred.
   * @throws SaasManagerException
   */
  private String getSaveUserDestination(HttpServletRequest request, SaasAccess access)
  throws SaasManagerException {
    String id = request.getParameter("id");
    String lastName = request.getParameter("lastName").trim();
    String firstName = request.getParameter("firstName").trim();
    String login = request.getParameter("login");
    if (StringUtil.isDefined(login)) {
      login = login.trim();
    }
    String email = request.getParameter("email").trim();
    String password = request.getParameter("password").trim();
    String company = request.getParameter("company").trim();
    String phone = request.getParameter("phone").trim();
    String role = request.getParameter("role");
    
    String message = userService.getUserDataCheckMessage(
      access, id, lastName, firstName, login, email, password);
    if (StringUtil.isDefined(message)) {
      request.setAttribute("message", message);
      UserVO user = new UserVO(id, lastName, firstName, login, email, company, phone);
      user.setRole(role);
      request.setAttribute("user", user);
      return "editUser.jsp";
    }
    
    String userId = request.getParameter("userId");
    if (id.equals("-1")) {
      // User creation
      id = userService.createUser(
        access.getDomainId(), lastName, firstName, login, email, password, company, phone);
      userService.updatePreferences(access, id);
      spaceService.addUserRole(access.getSpaceId(), userId, id, role);
      mailService.sendInvitationMail(access, firstName, lastName, login, password);
    } else {
      // User update
      userService.updateUser(id, lastName, firstName, email, password, company, phone);
      UserVO user = userService.getUser(id);
      if (!user.getRole().equals(role)) {
        ArrayList<String> adminUserIds = spaceService.getAdminUserIds(access.getSpaceId());
        if (adminUserIds.size() == 1 && adminUserIds.contains(id)) {
          return getMessageDestination(request, "oneAdminUserRequired");
        }
        spaceService.removeUserRole(access.getSpaceId(), userId, id, user.getRole());
        spaceService.addUserRole(access.getSpaceId(), userId, id, role);
      }
    }
    return getMainDestination(request, access);
  }

  /**
   * @param request The HTTP request.
   * @param message The key of the message to display on the information page.
   * @return An information page.
   */
  private String getMessageDestination(HttpServletRequest request, String message) {
    request.setAttribute("message", message);
    return "message.jsp";
  }

}
