/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.forums.instanciator;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.WysiwygInstanciator;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ForumsInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  /** Creates new ForumsInstanciator */
  public ForumsInstanciator() {
    super("com.stratelia.webactiv.forums");
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("forums", "ForumsInstanciator.create()",
        "forums.MSG_CREATE_WITH_SPACE_AND_COMPONENT",
        "space : " + spaceId + "component : " + componentId);
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
    SilverTrace.info("forums", "ForumsInstanciator.delete()", "forums.MSG_DELETE_WITH_SPACE",
        "spaceId : " + spaceId);

    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "Subscription");
    deleteDataOfInstance(con, componentId, "Rights");
    deleteDataOfInstance(con, componentId, "Message");
    deleteDataOfInstance(con, componentId, "Forum");
    // delete wysiwyg stuff
    WysiwygInstanciator wysiwygI = new WysiwygInstanciator("uselessButMandatory :)");
    wysiwygI.delete(con, spaceId, componentId, userId);

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
          "ForumsInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR,
          "root.DELETING_DATA_OF_INSTANCE_FAILED",
          "componentId = " + componentId + " deleteQuery = " + deleteQuery, se);
      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException exCloseStatement) {
        InstanciationException ie = new InstanciationException(
            "ForumsInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR,
            "root.EX_RESOURCE_CLOSE_FAILED",
            "componentId = " + componentId + " deleteQuery = " + deleteQuery, exCloseStatement);
        throw ie;
      }
    }

  }
}