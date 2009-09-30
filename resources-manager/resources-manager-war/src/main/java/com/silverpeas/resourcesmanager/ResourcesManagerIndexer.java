package com.silverpeas.resourcesmanager;

import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBm;
import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBmHome;
import com.silverpeas.resourcesmanager.model.ResourcesManagerRuntimeException;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ResourcesManagerIndexer implements ComponentIndexerInterface {

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    SilverTrace.info("resourcesManager", "ResourcesManagerIndexer.index()",
        "root.MSG_GEN_PARAM_VALUE", "index, context.getCurrentComponentId() = "
            + context.getCurrentComponentId());
    getResourceManagerBm()
        .indexResourceManager(context.getCurrentComponentId());
  }

  private ResourcesManagerBm getResourceManagerBm() {
    ResourcesManagerBm resourceManagerBm = null;
    try {
      ResourcesManagerBmHome resourceManagerBmHome = (ResourcesManagerBmHome) EJBUtilitaire
          .getEJBObjectRef("ejb/ResourcesManagerBm",
              ResourcesManagerBmHome.class);
      resourceManagerBm = resourceManagerBmHome.create();
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
          "ResourcesManagerSessionController.getResourceManagerBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return resourceManagerBm;
  }
}