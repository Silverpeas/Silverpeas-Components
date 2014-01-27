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

package com.silverpeas.components.saasmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Date;

import com.silverpeas.components.saasmanager.model.SaasAccess;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

/**
 * SAAS access DAO.
 * @author ahedin
 */
public class SaasAccessDao {

  // Query to create a SAAS access.
  private static final String QUERY_INSERT_ACCESS =
      "INSERT INTO sc_saasmanager_access "
      + "(id, uid, lastName, firstName, email, phone, company, companyWebSite, lang, services, "
      + "usersCount, conditionsAgreement, remarks, remoteAddress, requestDate, achievementDate, "
      + "domainId, userId, userLogin, userPassword, spaceId, componentIds, homeComponentId, "
      + "managementComponentId) "
      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  // Query to retrieve a SAAS access by its UID.
  private static final String QUERY_GET_ACCESS =
      "SELECT id, uid, lastName, firstName, email, phone, company, companyWebSite, lang, services, "
          + "usersCount, conditionsAgreement, remarks, remoteAddress, requestDate, achievementDate, "
          + "domainId, userId, userLogin, userPassword, spaceId, componentIds, homeComponentId, "
          + "managementComponentId "
          + "FROM sc_saasmanager_access "
          + "WHERE uid = ?";

  // Query to update a specific data of a SAAS access.
  private static final String QUERY_UPDATE_ACCESS =
      "UPDATE sc_saasmanager_access SET {0} = ? where id = ?";

  /**
   * @param connection The database connection.
   * @param access The SAAS access.
   * @return The id of the created access.
   * @throws UtilException
   * @throws SQLException
   */
  public int createAccess(Connection connection, SaasAccess access)
  throws UtilException, SQLException {
    int id = DBUtil.getNextId("sc_saasmanager_access", "id");
    PreparedStatement stmt = null;
    try {
      int i = 1;
      stmt = connection.prepareStatement(QUERY_INSERT_ACCESS);
      stmt.setInt(i++, id);
      stmt.setString(i++, access.getUid());
      stmt.setString(i++, access.getLastName());
      stmt.setString(i++, access.getFirstName());
      stmt.setString(i++, access.getEmail());
      stmt.setString(i++, access.getPhone());
      stmt.setString(i++, access.getCompany());
      stmt.setString(i++, access.getCompanyWebSite());
      stmt.setString(i++, access.getLang());
      stmt.setString(i++, access.getServices());
      stmt.setInt(i++, access.getUsersCount());
      stmt.setInt(i++, (access.isConditionsAgreement() ? 1 : 0));
      stmt.setString(i++, access.getRemarks());
      stmt.setString(i++, access.getRemoteAddress());
      stmt.setTimestamp(i++, new Timestamp(access.getRequestDate().getTime()));
      if (access.getAchievementDate() != null) {
        stmt.setTimestamp(i++, new Timestamp(access.getAchievementDate().getTime()));
      } else {
        stmt.setNull(i++, Types.TIMESTAMP);
      }
      stmt.setString(i++, access.getDomainId());
      stmt.setString(i++, access.getUserId());
      stmt.setString(i++, access.getUserLogin());
      stmt.setString(i++, access.getUserPassword());
      stmt.setString(i++, access.getSpaceId());
      stmt.setString(i++, access.getComponentIds());
      stmt.setString(i++, access.getHomeComponentId());
      stmt.setString(i++, access.getManagementComponentId());
      stmt.executeUpdate();
    } finally {
      DBUtil.close(stmt);
    }
    return id;
  }

  /**
   * @param connection The database connection.
   * @param uid The UID of the searched SAAS access.
   * @return The searched SAAS access.
   * @throws SQLException
   */
  public SaasAccess getAccess(Connection connection, String uid)
  throws SQLException {
    SaasAccess access = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = connection.prepareStatement(QUERY_GET_ACCESS);
      stmt.setString(1, uid);
      rs = stmt.executeQuery();
      if (rs.next()) {
        access = getAccess(rs);
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return access;
  }

  /**
   * Updates a specific data of the SAAS access corresponding to the id.
   * @param connection The database connection.
   * @param id The id of the SAAS access to update.
   * @param column The name of the column containing the data to update.
   * @param value The value to update.
   * @throws SQLException
   */
  public void updateAccess(Connection connection, int id, String column, Object value)
  throws SQLException {
    String query = MessageFormat.format(QUERY_UPDATE_ACCESS, new Object[] { column });
    PreparedStatement stmt = null;
    try {
      stmt = connection.prepareStatement(query);
      if (value instanceof String) {
        stmt.setString(1, (String) value);
      } else if (value instanceof Integer) {
        stmt.setInt(1, ((Integer) value).intValue());
      } else if (value instanceof Date) {
        Date date = (Date) value;
        if (date != null) {
          stmt.setTimestamp(1, new Timestamp(date.getTime()));
        } else {
          stmt.setNull(1, Types.TIMESTAMP);
        }
      }
      stmt.setInt(2, id);
      stmt.execute();
    } finally {
      DBUtil.close(stmt);
    }
  }

  /**
   * @param rs The result set containing the data of a SAAS access.
   * @return The SAAS access corresponding to the data extracted from the result set.
   * @throws SQLException
   */
  private SaasAccess getAccess(ResultSet rs)
  throws SQLException {
    SaasAccess access = new SaasAccess();
    access.setId(rs.getInt("id"));
    access.setUid(rs.getString("uid"));
    access.setLastName(rs.getString("lastName"));
    access.setFirstName(rs.getString("firstName"));
    access.setEmail(rs.getString("email"));
    access.setPhone(rs.getString("phone"));
    access.setCompany(rs.getString("company"));
    access.setCompanyWebSite(rs.getString("companyWebSite"));
    access.setLang(rs.getString("lang"));
    access.setServices(rs.getString("services"));
    access.setUsersCount(rs.getInt("usersCount"));
    access.setConditionsAgreement(rs.getInt("conditionsAgreement") == 1);
    access.setRemarks(rs.getString("remarks"));
    access.setRemoteAddress(rs.getString("remoteAddress"));
    access.setRequestDate(new Date(rs.getTimestamp("requestDate").getTime()));
    Timestamp achievementTimestamp = rs.getTimestamp("achievementDate");
    if (achievementTimestamp != null) {
      access.setAchievementDate(new Date(achievementTimestamp.getTime()));
    }
    access.setDomainId(rs.getString("domainId"));
    access.setUserId(rs.getString("userId"));
    access.setUserLogin(rs.getString("userLogin"));
    access.setUserPassword(rs.getString("userPassword"));
    access.setSpaceId(rs.getString("spaceId"));
    access.setComponentIds(rs.getString("componentIds"));
    access.setManagementComponentId(rs.getString("homeComponentId"));
    access.setManagementComponentId(rs.getString("managementComponentId"));
    return access;
  }

}
