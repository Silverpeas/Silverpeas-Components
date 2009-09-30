/*
 * AlmanachInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.almanach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.attachment.AttachmentInstanciator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * 
 * @author squere
 * @version update by Sébastien Antonio - Externalisation of the SQL request
 */
public class AlmanachInstanciator extends SQLRequest implements
    ComponentsInstanciatorIntf {

  /** Creates new AlmanachInstanciator */
  public AlmanachInstanciator() {
    super("com.stratelia.webactiv.almanach");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("almanach", "AlmanachInstanciator.create()",
        "almanach.MSG_CREATE_WITH_SPACE_AND_COMPONENT", "space : " + spaceId
            + "component : " + componentId);

    // JCG
    AttachmentInstanciator ai = new AttachmentInstanciator(
        "com.stratelia.webactiv.almanach");
    ai.create(con, spaceId, componentId, userId);
    SilverTrace.info("almanach", "AlmanachInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("almanach", "AlmanachInstanciator.delete()",
        "almanach.MSG_DELETE_WITH_SPACE", "spaceId : " + spaceId);

    // read the property file which contains all SQL queries to delete rows
    setDeleteQueries();
    deleteDataOfInstance(con, componentId, "Event");

    // JCG
    AttachmentInstanciator ai = new AttachmentInstanciator(
        "com.stratelia.webactiv.almanach");
    ai.delete(con, spaceId, componentId, userId);

    SilverTrace.info("almanach", "AlmanachInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }

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
      InstanciationException ie = new InstanciationException(
          "AlmanachInstanciator.deleteDataOfInstance()",
          SilverpeasException.ERROR,
          "almanach.EX_DELETE_DATA_OF_INSTANCE_FAIL", "componentId : "
              + componentId + "delete query = " + deleteQuery, se);
      throw ie;
    } finally {
      try {
        stmt.close();
      } catch (SQLException err_closeStatement) {
        InstanciationException ie = new InstanciationException(
            "AlmanachInstanciator.deleteDataOfInstance()",
            SilverpeasException.ERROR, "almanach.EX_CLOSE_STATEMENT_FAIL",
            null, err_closeStatement);
        throw ie;
      }
    }

  }

}
