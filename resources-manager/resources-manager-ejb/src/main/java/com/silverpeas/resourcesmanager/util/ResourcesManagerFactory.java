/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of
 * the text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.silverpeas.resourcesmanager.util;

import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBm;
import com.silverpeas.resourcesmanager.control.ejb.ResourcesManagerBmHome;
import com.silverpeas.resourcesmanager.model.ResourcesManagerRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 *
 * @author ehugonnet
 */
public class ResourcesManagerFactory {

  public static ResourcesManagerBm getResourcesManagerBm() {
    try {
      ResourcesManagerBmHome resourceManagerBmHome = EJBUtilitaire.getEJBObjectRef(
              "ejb/ResourcesManagerBm", ResourcesManagerBmHome.class);
      return resourceManagerBmHome.create();
    } catch (Exception e) {
      throw new ResourcesManagerRuntimeException(
              "ResourcesManagerSessionController.getResourceManagerBm()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  private ResourcesManagerFactory() {
  }
}
