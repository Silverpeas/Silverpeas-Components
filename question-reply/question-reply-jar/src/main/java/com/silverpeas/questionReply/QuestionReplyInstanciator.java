/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import org.silverpeas.util.exception.SilverpeasException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class QuestionReplyInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  /**
   * Creates new QuestionReplyInstanciator
   */
  public QuestionReplyInstanciator() {
    super("org.silverpeas.questionReply");
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "RECIPIENT");
    deleteDataOfInstance(con, componentId, "REPLY");
    deleteDataOfInstance(con, componentId, "QUESTION");
  }

  /**
   * Delete all data of one website instance from the website table.
   * @param con (Connection) the connection to the data base
   * @param componentId (String) the instance id of the Silverpeas component website.
   * @param suffixName (String) the suffixe of a website table
   */
  private void deleteDataOfInstance(Connection con, String componentId, String suffixName)
      throws InstanciationException {
    String deleteQuery = getDeleteQuery(componentId, suffixName);
    try (Statement stmt = con.createStatement()) {
      stmt.executeUpdate(deleteQuery);
    } catch (SQLException se) {
      throw new InstanciationException("QuestionReplyInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
    }
  }
}