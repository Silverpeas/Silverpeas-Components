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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.silverpeas.components.saasmanager.dao.SaasAccessDao;
import com.silverpeas.components.saasmanager.exception.SaasManagerException;
import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * SAAS access service
 * @author ahedin
 */
public class SaasAccessService {

  private SaasAccessDao dao;

  public SaasAccessService() {
    dao = new SaasAccessDao();
  }

  /**
   * @param access The SAAS access.
   * @return true if the SAAS access is allowed, else returns false.
   */
  public boolean isAccessAllowed(SaasAccess access) {
    // @TODO establish a functional rule to not allow any SAAS access to avoid, for instance, too
    // many accesses in a defined period from the same ip address
    return true;
  }

  /**
   * @param access The SAAS access.
   * @return
   * @throws SaasManagerException
   */
  public int addAccessRequest(SaasAccess access)
  throws SaasManagerException {
    Connection connection = null;
    try {
      connection = getConnection();
      return dao.createAccess(connection, access);
    } catch (Exception e) {
      throw new SaasManagerException("SaasAccessService.addAccessRequest()",
        SilverpeasException.ERROR, "saasmanager.EX_CREATE_ACCESS", "uid=" + access.getUid(), e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * @param uid The UID of the searched SAAS access.
   * @return The SAAS access corresponding to the UID.
   * @throws SaasManagerException
   */
  public SaasAccess getAccess(String uid)
  throws SaasManagerException {
    SaasAccess access;
    Connection connection = null;
    try {
      connection = getConnection();
      access = dao.getAccess(connection, uid);
    } catch (Exception e) {
      throw new SaasManagerException("SaasAccessService.getAccess()",
        SilverpeasException.ERROR, "saasmanager.EX_GET_ACCESS", "uid=" + uid, e);
    } finally {
      DBUtil.close(connection);
    }

    if (access == null) {
      throw new SaasManagerException("SaasAccessService.getAccess()",
        SilverpeasException.ERROR, "saasmanager.EX_NO_ACCESS_FOUND", "uid=" + uid);
    }
    return access;
  }

  /**
   * Updates in database and in the object itself the domain's id of the SAAS access.
   * @param access The SAAS access.
   * @param domainId The domain's id.
   * @throws SaasManagerException
   */
  public void updateAccessDomainId(SaasAccess access, String domainId)
  throws SaasManagerException {
    access.setDomainId(domainId);
    updateAccess(access.getId(), "domainId", domainId);
  }

  /**
   * Updates in database and in the object itself user data (id, login, password) of the SAAS access.
   * @param access The SAAS access.
   * @param id The user's id.
   * @param login The user's login.
   * @param password The user's password.
   * @throws SaasManagerException
   */
  public void updateAccessUser(SaasAccess access, String id, String login, String password)
      throws SaasManagerException {
    access.setUserId(id);
    access.setUserLogin(login);
    access.setUserPassword(password);
    updateAccess(access.getId(), "userId", id);
    updateAccess(access.getId(), "userLogin", login);
    updateAccess(access.getId(), "userPassword", password);
  }

  /**
   * Updates in database and in the object itself the space id of the SAAS access.
   * @param access The SAAS access.
   * @param spaceId The space's id.
   * @throws SaasManagerException
   */
  public void updateAccessSpaceId(SaasAccess access, String spaceId)
  throws SaasManagerException {
    access.setSpaceId(spaceId);
    updateAccess(access.getId(), "spaceId", spaceId);
  }

  /**
   * Updates in database and in the object itself the components' ids of the SAAS access.
   * @param access The SAAS access.
   * @param componentIds The components' ids.
   * @throws SaasManagerException
   */
  public void updateAccessComponentIds(SaasAccess access, ArrayList<String> componentIds)
  throws SaasManagerException {
    access.setComponentIds(componentIds);
    updateAccess(access.getId(), "componentIds", access.getComponentIds());
  }

  /**
   * Updates in database and in the object itself the achievement date of the SAAS access.
   * @param access The SAAS access.
   * @throws SaasManagerException
   */
  public void updateAccessAchievementDate(SaasAccess access)
  throws SaasManagerException {
    access.setAchievementDate(new Date());
    updateAccess(access.getId(), "achievementDate", access.getAchievementDate());
  }

  /**
   * Updates in database and in the object itself the home component's id of the SAAS access.
   * @param access The SAAS access.
   * @param homeComponentId The home component's id.
   * @throws SaasManagerException
   */
  public void updateAccessHomeComponentId(SaasAccess access, String homeComponentId)
  throws SaasManagerException {
    access.setHomeComponentId(homeComponentId);
    updateAccess(access.getId(), "homeComponentId", homeComponentId);
  }

  /**
   * Updates in database and in the object itself the management component's id of the SAAS access.
   * @param access The SAAS access.
   * @param managementComponentId The management component's id.
   * @throws SaasManagerException
   */
  public void updateAccessManagementComponentId(SaasAccess access, String managementComponentId)
  throws SaasManagerException {
    access.setManagementComponentId(managementComponentId);
    updateAccess(access.getId(), "managementComponentId", managementComponentId);
  }

  /**
   * Updates in database a specific data of the SAAS access.
   * @param id The id of the SAAS access.
   * @param column The database column of the value to update.
   * @param value The value to update.
   * @throws SaasManagerException
   */
  private void updateAccess(int id, String column, Object value)
  throws SaasManagerException {
    Connection connection = null;
    try {
      connection = getConnection();
      dao.updateAccess(connection, id, column, value);
    } catch (Exception e) {
      throw new SaasManagerException("SaasAccessService.updateAccess()", SilverpeasException.ERROR,
        "saasmanager.EX_UPDATE_ACCESS", "id=" + id + " ; column=" + column + " ; value=" + value, e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * @return A database connection.
   * @throws UtilException
   * @throws SQLException
   */
  private Connection getConnection()
  throws UtilException, SQLException {
    return DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
  }

}
