/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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

/**
 * 
 * @author squere
 * @version update by the Sébastien Antonio - Externalisation of the SQL request
 */
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
