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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.projectManager;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.silverpeas.comment.CommentInstanciator;
import com.silverpeas.versioning.VersioningInstanciator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.attachment.AttachmentInstanciator;

/**
 * @author neysseric
 */
public class ProjectManagerInstanciator implements ComponentsInstanciatorIntf {

  private final static String PROJECTMANAGER_TASKS_TABLENAME = "SC_ProjectManager_Tasks";
  private final static String PROJECTMANAGER_CALENDAR_TABLENAME = "SC_ProjectManager_Calendar";

  public ProjectManagerInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("projectManager", "ProjectManagerInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
        + componentId + ", userId =" + userId);

    SilverTrace.info("projectManager", "ProjectManagerInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("projectManager", "ProjectManagerInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
        + componentId + ", userId =" + userId);

    // delete attachments
    AttachmentInstanciator attachments = new AttachmentInstanciator();
    attachments.delete(con, spaceId, componentId, userId);

    // delete versioning infos
    VersioningInstanciator version = new VersioningInstanciator();
    version.delete(con, spaceId, componentId, userId);

    // delete comments
    CommentInstanciator comment = new CommentInstanciator();
    comment.delete(con, spaceId, componentId, userId);

    // delete todos
    TodoBackboneAccess tbba = new TodoBackboneAccess();
    tbba.removeEntriesByInstanceId(componentId);

    // delete tasks and holidays
    PreparedStatement stmt = null;
    try {
      // delete tasks
      StringBuffer deleteStatement = new StringBuffer(128);
      deleteStatement.append("delete from ").append(
          PROJECTMANAGER_TASKS_TABLENAME).append(" where instanceId = ? ");

      stmt = con.prepareStatement(deleteStatement.toString());
      stmt.setString(1, componentId);
      stmt.executeUpdate();

      // delete holidays
      StringBuffer holidaysStatement = new StringBuffer(128);
      holidaysStatement.append("delete from ").append(
          PROJECTMANAGER_CALENDAR_TABLENAME).append(" where instanceId = ? ");

      stmt = con.prepareStatement(holidaysStatement.toString());
      stmt.setString(1, componentId);
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new InstanciationException("ProjectManagerInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED", e);
    } finally {
      DBUtil.close(stmt);
    }

    SilverTrace.info("projectManager", "ProjectManagerInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }
}