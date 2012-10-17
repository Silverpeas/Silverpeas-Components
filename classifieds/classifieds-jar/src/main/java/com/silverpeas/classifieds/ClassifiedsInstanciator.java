/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import java.sql.Connection;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.classifieds.control.ClassifiedService;
import com.silverpeas.classifieds.control.ClassifiedServiceFactory;
import com.silverpeas.classifieds.model.ClassifiedsRuntimeException;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
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
      ClassifiedServiceFactory factory = ClassifiedServiceFactory.getFactory();
      ClassifiedService service = factory.getClassifiedService();
      service.deleteAllClassifieds(componentId);
      service.deleteAllSubscribes(componentId);
      CommentServiceFactory.getFactory().getCommentService()
          .deleteAllCommentsByComponentInstanceId(componentId);
    } catch (Exception e) {
      throw new ClassifiedsRuntimeException("ClassifiedsInstanciator.delete()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    SilverTrace.info("classifieds", "ClassifiedsInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }
}