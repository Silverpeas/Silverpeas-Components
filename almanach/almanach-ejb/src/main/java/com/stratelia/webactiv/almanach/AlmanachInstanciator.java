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
package com.stratelia.webactiv.almanach;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.attachment.AttachmentInstanciator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class AlmanachInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  public AlmanachInstanciator() {
    super("com.stratelia.webactiv.almanach");
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("almanach", "AlmanachInstanciator.create()",
        "almanach.MSG_CREATE_WITH_SPACE_AND_COMPONENT",
        "space : " + spaceId + "component : " + componentId);

    AttachmentInstanciator ai = new AttachmentInstanciator("com.stratelia.webactiv.almanach");
    ai.create(con, spaceId, componentId, userId);
    SilverTrace.info("almanach", "AlmanachInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("almanach", "AlmanachInstanciator.delete()", "almanach.MSG_DELETE_WITH_SPACE",
        "spaceId : " + spaceId);
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "Event");
    AttachmentInstanciator ai = new AttachmentInstanciator("com.stratelia.webactiv.almanach");
    ai.delete(con, spaceId, componentId, userId);
    SilverTrace.info("almanach", "AlmanachInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

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
          "AlmanachInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR,
          "almanach.EX_DELETE_DATA_OF_INSTANCE_FAIL",
          "componentId : " + componentId + "delete query = " + deleteQuery, se);
      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        InstanciationException ie = new InstanciationException(
            "AlmanachInstanciator.deleteDataOfInstance()", SilverpeasException.ERROR,
            "almanach.EX_CLOSE_STATEMENT_FAIL", null, err_closeStatement);
        throw ie;
      }
    }

  }
}
