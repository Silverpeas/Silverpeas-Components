/**
 * Copyright (C) 2000 - 2011 Silverpeas
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