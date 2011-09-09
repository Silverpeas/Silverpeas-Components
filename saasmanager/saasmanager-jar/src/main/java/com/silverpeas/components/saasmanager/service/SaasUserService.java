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
import java.util.Random;

import com.silverpeas.SilverpeasServiceProvider;
import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.silverpeas.components.saasmanager.vo.UserVO;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * SAAS user service
 * @author ahedin
 */
public class SaasUserService {

  private static final String USER_ACCESS = "U";
  private static final int PASSWORD_LENGTH = 8;

  private Admin admin = null;
  protected OrganizationController organizationController;

  public SaasUserService() {
    admin = new Admin();
    organizationController = new OrganizationController();
  }
  
  public String getUserDataCheckMessage(SaasAccess access, String id, String lastName,
    String firstName, String login, String email, String password)
  throws SaasManagerException {
    boolean creation = id.equals("-1");
    if (!StringUtil.isDefined(lastName)) {
      return "missingLastName";
    }
    if (creation && !StringUtil.isDefined(login)) {
      return "missingLogin";
    }
    if (!StringUtil.isDefined(email)) {
      return "missingEmail";
    }
    if (creation && !StringUtil.isDefined(password)) {
      return "missingPassword";
    }
    
    if (creation) {
      ArrayList<UserVO> users = getUsers(access);
      for (UserVO user : users) {
        if (user.getLastName().toLowerCase().equals(lastName.toLowerCase()) 
            && user.getFirstName().toLowerCase().equals(firstName.toLowerCase())) {
          return "existingName";
        }
        if (user.getLogin().equals(login)) {
          return "existingLogin";
        }
        if (user.getEmail().equals(email)) {
          return "existingEmail";
        }
      }
    }
    
    return null;
  }

  /**
   * Creates a user into the SAAS domain.
   * @param access The SAAS access.
   * @param login The user's login.
   * @param password The user's password.
   * @return The id of the new user.
   * @throws SaasManagerException
   */
  public String createUser(SaasAccess access, String login, String password)
  throws SaasManagerException {
    return createUser(String.valueOf(access.getDomainId()), access.getLastName(),
      access.getFirstName(), login, access.getEmail(), password, access.getCompany(),
      access.getPhone());
  }

  /**
   * Creates a user into the SAAS domain.
   * @param domainId The domain's id.
   * @param lastName The user's last name.
   * @param firstName The user's first name.
   * @param login The user's login.
   * @param email The user's email.
   * @param password The user's password.
   * @param company The user's company.
   * @param phone The user's phone number.
   * @return The id of the new user.
   * @throws SaasManagerException
   */
  public String createUser(String domainId, String lastName, String firstName, String login,
    String email, String password, String company, String phone)
  throws SaasManagerException {
    UserDetail user = new UserDetail();
    user.setId("-1");
    user.setDomainId(domainId);
    user.setLogin(login);
    user.setLastName(lastName);
    user.setFirstName(firstName);
    user.seteMail(email);
    user.setAccessLevel(USER_ACCESS);

    String userId;
    try {
      userId = admin.addUser(user);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasUserService.createUser()", SilverpeasException.ERROR,
        "saasmanager.EX_CREATE_USER", "domainId=" + domainId, e);
    }

    if (StringUtil.isDefined(userId)) {
      UserFull userFull = getUserFull(userId);
      if (userFull != null) {
        userFull.setPasswordValid(true);
        userFull.setPassword(password);
        userFull.setValue("company", company);
        userFull.setValue("phone", phone);

        try {
          userId = admin.updateUserFull(userFull);
        } catch (AdminException e) {
          throw new SaasManagerException("SaasUserService.createUser()", SilverpeasException.ERROR,
            "saasmanager.EX_UPDATE_USER", "userId=" + userId, e);
        }
      }
    }

    if (StringUtil.isDefined(userId)) {
      return userId;
    } else {
      throw new SaasManagerException("SaasUserService.createUser()", SilverpeasException.ERROR,
        "saasmanager.EX_CREATE_USER", "domainId=" + domainId);
    }
  }

  /**
   * Updates a user.
   * @param userId The user's id.
   * @param lastName The user's last name.
   * @param firstName The user's first name.
   * @param email The user's email.
   * @param password The user's password.
   * @param company The user's company.
   * @param phone The user's phone number.
   * @throws SaasManagerException
   */
  public void updateUser(String userId, String lastName, String firstName, String email,
    String password, String company, String phone)
  throws SaasManagerException {
    UserFull userFull = getUserFull(userId);
    if (userFull != null) {
      userFull.setLastName(lastName);
      userFull.setFirstName(firstName);
      userFull.seteMail(email);
      if (StringUtil.isDefined(password)) {
        userFull.setPasswordValid(true);
        userFull.setPassword(password);
      }
      userFull.setValue("company", company);
      userFull.setValue("phone", phone);

      try {
        admin.updateUserFull(userFull);
      } catch (AdminException e) {
        throw new SaasManagerException("SaasUserService.updateUser()", SilverpeasException.ERROR,
          "saasmanager.EX_UPDATE_USER", "userId=" + userId, e);
      }
    }
  }

  /**
   * Removes a user.
   * @param userId The user's id.
   * @throws SaasManagerException
   */
  public void removeUser(String userId)
  throws SaasManagerException {
    try {
      admin.deleteUser(userId);
    } catch (AdminException e) {
      throw new SaasManagerException("SaasUserService.removeUser()", SilverpeasException.ERROR,
        "saasmanager.EX_REMOVE_USER", "userId=" + userId, e);
    }
  }

  /**
   * @param userId The user's id.
   * @return The full detail of the user.
   */
  private UserFull getUserFull(String userId) {
    return organizationController.getUserFull(userId);
  }

  /**
   * @param userId The user's id.
   * @return A visual object containing user's data.
   * @throws SaasManagerException
   */
  public UserVO getUser(String userId)
  throws SaasManagerException {
    UserFull userFull = getUserFull(userId);
    return new UserVO(userFull.getId(), userFull.getLastName(), userFull.getFirstName(),
      userFull.getLogin(), userFull.geteMail(), userFull.getValue("company"),
      userFull.getValue("phone"));
  }

  /**
   * @param access The SAAS access.
   * @return The login of the SAAS user.
   */
  public String getLogin(SaasAccess access) {
    String firstName = access.getFirstName().toLowerCase();
    String lastName = access.getLastName().toLowerCase();
    return new StringBuilder()
      .append(lastName.length() >= 3 ? firstName.substring(0, 1) : firstName)
      .append(".")
      .append(lastName)
      .toString();
  }

  /**
   * @return A randomly generated password.
   */
  public String getPassword() {
    Random random = new Random();
    StringBuilder password = new StringBuilder();
    while (password.length() < PASSWORD_LENGTH) {
      password.append(Integer.toHexString(random.nextInt()));
    }
    return password.toString();
  }

  /**
   * Updates default preferences of the user.
   * @param access The SAAS access.
   * @param userId The user's id.
   */
  public void updatePreferences(SaasAccess access, String userId) {
    UserPreferences preferences = SilverpeasServiceProvider.getPersonalizationService()
      .getUserSettings(userId);
    preferences.setLanguage(access.getLang());
    preferences.setPersonalWorkSpaceId(access.getSpaceId());
    preferences.enableDragAndDrop(true);
    SilverpeasServiceProvider.getPersonalizationService().saveUserSettings(preferences);
  }

  /**
   * @param access The SAAS access.
   * @return The number of users of the SAAS domain.
   * @throws SaasManagerException
   */
  public int getUsersCount(SaasAccess access)
  throws SaasManagerException {
    try {
      return admin.getUsersNumberOfDomain(access.getDomainId());
    } catch (AdminException e) {
      throw new SaasManagerException("SaasUserService.getUsersCount()", SilverpeasException.ERROR,
        "saasmanager.EX_GET_USERS_COUNT", "uid=" + access.getUid()
        + "; domainId=" + access.getDomainId(), e);
    }
  }

  /**
   * @param access The SAAS access.
   * @return The list of users visual objects of the SAAS domain.
   * @throws SaasManagerException
   */
  public ArrayList<UserVO> getUsers(SaasAccess access)
  throws SaasManagerException {
    try {
      String[] userIds = admin.getUserIdsOfDomain(access.getDomainId());
      ArrayList<UserVO> users = new ArrayList<UserVO>();
      if (userIds != null) {
        for (String userId : userIds) {
          users.add(getUser(userId));
        }
      }
      return users;
    } catch (AdminException e) {
      throw new SaasManagerException("SaasUserService.getUsers()", SilverpeasException.ERROR,
        "saasmanager.EX_GET_USERS", "uid=" + access.getUid() + "; domainId=" + access.getDomainId(),
        e);
    }
  }

}
