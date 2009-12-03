/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
/*
 * InfoLetterInstanciator.java
 *
 */
package com.stratelia.silverpeas.infoLetter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.infoLetter.control.ServiceFactory;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;

public class InfoLetterInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  /** Creates new FileBoxPlusInstanciator */
  public InfoLetterInstanciator() {
    super("com.stratelia.silverpeas.infoLetter");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    InfoLetterDataInterface dataInterface = ServiceFactory.getInfoLetterData();
    InfoLetter il = dataInterface.createDefaultLetter(spaceId, componentId);
    FullIndexEntry indexEntry = new FullIndexEntry(componentId, "Lettre", il
        .getPK().getId());
    indexEntry.setTitle(il.getName());
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "ILLETTERS");
    deleteDataOfInstance(con, componentId, "ILPUBS");
    deleteDataOfInstance(con, componentId, "ILEXTS");
    deleteDataOfInstance(con, componentId, "ILINTS");
    deleteDataOfInstance(con, componentId, "ILPUBS");
  }

  /**
   * Delete all data of one website instance from the website table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component website.
   * @param suffixName (String) the suffixe of a website table
   */
  private void deleteDataOfInstance(Connection con, String componentId,
      String suffixName) throws InstanciationException {

    Statement stmt = null;

    // get the delete query from the external file
    String deleteQuery = getDeleteQuery(componentId, suffixName);

    // execute the delete query
    try {
      stmt = con.createStatement();
      stmt.executeUpdate(deleteQuery);
      stmt.close();
    } catch (SQLException se) {
      throw new InstanciationException(
          "InfoLetterInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        SilverTrace.error("infoLetter",
            "InfoLetterInstanciator.deleteDataOfInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
      }
    }

  }

}