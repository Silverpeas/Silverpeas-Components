package com.silverpeas.mydb;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

/**
 * MyDB Instanciator.
 * 
 * @author Antoine HEDIN
 */
public class MyDBInstanciator implements ComponentsInstanciatorIntf {

  public MyDBInstanciator() {

  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("myDB", "MyDBInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space=" + spaceId + ", componentId="
            + componentId + ", userId=" + userId);

    SilverTrace.info("myDB", "MyDBInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("myDB", "MyDBInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space=" + spaceId + ", componentId="
            + componentId + ", userId=" + userId);

    SilverTrace.info("myDB", "MyDBInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }

}