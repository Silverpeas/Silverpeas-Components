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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
---*/

/*
 * NewsEditoInstanciator.java
 * 
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.newsEdito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.publication.PublicationInstanciator;

public class NewsEditoInstanciator extends SQLRequest implements
    ComponentsInstanciatorIntf {
  private static java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(
      "yyyy/MM/dd");

  /**
   * Creates new NewsEditoInstanciator
   */
  public NewsEditoInstanciator() {
    super("com.stratelia.webactiv.newsEdito");
  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * 
   * @throws InstanciationException
   * 
   * @see
   */
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    // create publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.newsEdito");

    pub.create(con, spaceId, componentId, userId);

    // create node component
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.newsEdito");

    node.create(con, spaceId, componentId, userId);

    // Insert the line corresponding to the Root node
    setInsertQueries();
    insertSpecialNode(con, componentId, userId);

  }

  /**
   * Method declaration
   * 
   * 
   * @param con
   * @param spaceId
   * @param componentId
   * @param userId
   * 
   * @throws InstanciationException
   * 
   * @see
   */
  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

    // delete publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.newsEdito");

    pub.delete(con, spaceId, componentId, userId);

    // delete node component
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.newsEdito");

    node.delete(con, spaceId, componentId, userId);

  }

  /**
   * Insert specific default data of the news Edito component into the database.
   * 
   * @param con
   *          (Connection) the connection to the database
   * @param componentId
   *          (String) the identification of the SilverPeas Component
   * @param userId
   *          (String) the user id.
   * @throws InstanciationException
   *           exception catched during the instanciation of the Silverpeas
   *           Component
   */
  private void insertSpecialNode(Connection con, String componentId,
      String userId) throws InstanciationException {

    String insertQuery = getInsertQuery(componentId, "Accueil");
    String creationDate = formatter.format(new Date());

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException err_insert) {
      InstanciationException ie = new InstanciationException(err_insert
          .getMessage());

      throw ie;
    }

  }

}
