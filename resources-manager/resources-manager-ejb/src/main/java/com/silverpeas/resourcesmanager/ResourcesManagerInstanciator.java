package com.silverpeas.resourcesmanager;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class ResourcesManagerInstanciator implements ComponentsInstanciatorIntf {

  public ResourcesManagerInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("resourcesManager",
        "ResourcesManagerInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId ="
            + userId);

    // insert your code here !

    SilverTrace.info("resourcesManager",
        "ResourcesManagerInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("resourcesManager",
        "ResourcesManagerInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId ="
            + userId);

    // insert your code here !

    SilverTrace.info("resourcesManager",
        "ResourcesManagerInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }
}