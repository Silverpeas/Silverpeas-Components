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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.classifieds;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import java.sql.Connection;

import com.silverpeas.classifieds.control.ejb.ClassifiedsBm;
import com.silverpeas.classifieds.control.ejb.ClassifiedsBmHome;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ClassifiedsInstanciator implements ComponentsInstanciatorIntf {

  public ClassifiedsInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) {
    SilverTrace.info("classifieds", "classifiedsInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    SilverTrace.info("classifieds", "classifiedsInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) {
    SilverTrace.info("classifieds", "classifiedsInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    try {
      getClassifiedsBm().deleteAllClassifieds(componentId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsInstanciator.delete()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    try {
      getClassifiedsBm().deleteAllSubscribes(componentId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsInstanciator.delete()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    SilverTrace.info("classifieds", "ClassifiedsInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private ClassifiedsBm getClassifiedsBm() {
    ClassifiedsBm classifiedsBm = null;
    try {
      ClassifiedsBmHome classifiedsBmHome =
          EJBUtilitaire.getEJBObjectRef(JNDINames.CLASSIFIEDSBM_EJBHOME,
          ClassifiedsBmHome.class);
      classifiedsBm = classifiedsBmHome.create();
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsInstanciator.getClassifiedsBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return classifiedsBm;
  }
}