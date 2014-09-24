/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.connecteurJDBC.control;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import org.silverpeas.util.exception.SilverpeasException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Title: Connecteur JDBC Description: Ce composant a pour objet de permettre de recuperer
 * rapidement et simplement des donnees du systeme d'information de l'entreprise.
 * @author Eric BURGEL
 * @version 1.0
 */
public class ConnecteurJDBCInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  public ConnecteurJDBCInstanciator() {
    super("com.stratelia.silverpeas.connecteurJDBC");
  }

  @Override
  public void create(Connection connection, String spaceId, String componentId, String userId)
      throws InstanciationException {
  }

  /**
   * Delete some rows of an instance of a forum.
   * @param con (Connection) the connection to the data base
   * @param spaceId (String) the id of a the space where the component exist.
   * @param componentId (String) the instance id of the Silverpeas component forum.
   * @param userId (String) the owner of the component
   */
  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("connecteurJDBC", "ConnecteurJDBCInstanciator.delete()",
        "connecteurJDBC.MSG_DELETE_CALLED_FOR_SPACE_ID", "spaceId : " + spaceId);
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "connecteurJDBC");
  }

  /**
   * Delete all data of one forum instance from the forum table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component forum.
   * @param suffixName (String) the suffixe of a Forum table
   */
  private void deleteDataOfInstance(Connection con, String componentId, String suffixName) throws
      InstanciationException {
    Statement stmt = null;
    String deleteQuery = getDeleteQuery(componentId, suffixName);
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      InstanciationException ie = new InstanciationException(
          "connecteurJDBCInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR,
          "connecteurJDBC.EX_DELETE_DATA_OF_INSTANCE_FAIL", "componentId : "
          + componentId + "delete query = " + deleteQuery, se);
      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        InstanciationException ie = new InstanciationException(
            "connecteurJDBCInstanciator.deleteDataOfInstance()",
            SilverpeasException.ERROR, "connecteurJDBC.EX_CLOSE_STATEMENT_FAIL", "componentId : "
            + componentId + "delete query = " + deleteQuery, err_closeStatement);
        throw ie;
      }
    }

  }
}
