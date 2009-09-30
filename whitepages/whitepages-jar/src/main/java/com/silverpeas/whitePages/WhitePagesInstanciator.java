/*
 * WhitePagesInstanciator.java
 *
 */
package com.silverpeas.whitePages;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class WhitePagesInstanciator extends SQLRequest implements
    ComponentsInstanciatorIntf {

  /** Creates new WhitePagesInstanciator */
  public WhitePagesInstanciator() {
    super("com.silverpeas.whitePages");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    try {

      Admin admin = new Admin();
      String template = admin.getComponentParameterValue(componentId,
          "cardTemplate");

      // PublicationTemplateManager.addPublicationTemplate(componentId,
      // template);
      PublicationTemplateManager.addDynamicPublicationTemplate(componentId,
          template);

    } catch (Exception e) {
      throw new InstanciationException("WhitePagesInstanciator.create()",
          SilverpeasException.ERROR, "whitePages.EX_CANT_ADD_TEMPLATE", e);
    }
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "WhitePages");

    try {
      PublicationTemplateManager.removePublicationTemplate(componentId);
    } catch (Exception e) {
      throw new InstanciationException("WhitePagesInstanciator.delete()",
          SilverpeasException.ERROR, "whitePages.EX_CANT_REMOVE_TEMPLATE", e);
    }

  }

  /**
   * Delete all data of one website instance from the website table.
   * 
   * @param con
   *          (Connection) the connection to the data base
   * @param componentId
   *          (String) the instance id of the Silverpeas component website.
   * @param suffixName
   *          (String) the suffixe of a website table
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
          "WhitePagesInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR, "root.EX_SQL_QUERY_FAILED", se);
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        SilverTrace.error("whitePages",
            "WhitePagesInstanciator.deleteDataOfInstance()",
            "root.EX_RESOURCE_CLOSE_FAILED", "", err_closeStatement);
      }
    }

  }

}