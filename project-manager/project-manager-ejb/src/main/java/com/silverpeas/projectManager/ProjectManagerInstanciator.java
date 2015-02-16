/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.projectManager;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.ServiceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author neysseric
 */
public class ProjectManagerInstanciator implements ComponentsInstanciatorIntf {

  private static final String PROJECTMANAGER_DELETE_COMPONENT_TASKS =
      "DELETE FROM SC_ProjectManager_Tasks WHERE instanceId = ? ";
  private static final String PROJECTMANAGER_DELETE_COMPONENT_CALENDAR =
      "DELETE FROM SC_ProjectManager_Calendar WHERE instanceId = ? ";

  public ProjectManagerInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace
        .info("projectManager", "ProjectManagerInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
            "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);

    SilverTrace
        .info("projectManager", "ProjectManagerInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId)
      throws InstanciationException {
    SilverTrace
        .info("projectManager", "ProjectManagerInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
            "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);

    TodoBackboneAccess tbba = ServiceProvider.getService(TodoBackboneAccess.class);
    tbba.removeEntriesByInstanceId(componentId);
    PreparedStatement stmt = null;
    try {
      String deleteStatement = PROJECTMANAGER_DELETE_COMPONENT_TASKS;
      stmt = con.prepareStatement(deleteStatement);
      stmt.setString(1, componentId);
      stmt.executeUpdate();
      String holidaysStatement = PROJECTMANAGER_DELETE_COMPONENT_CALENDAR;
      stmt = con.prepareStatement(holidaysStatement);
      stmt.setString(1, componentId);
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new InstanciationException("ProjectManagerInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED", e);
    } finally {
      DBUtil.close(stmt);
    }
    SilverTrace
        .info("projectManager", "ProjectManagerInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }
}