/*
 * Copyright (C) 2000 - 2017 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * When a FormsOnline instance is being deleted, this process deletes all the forms' data that
 * were managed by this instance.
 * @author mmoquillon
 */
@Named
public class FormsOnlineInstancePreDestruction implements ComponentInstancePreDestruction {

  private static final String FORMS_QUERY =
      "select distinct xmlFormName from SC_FormsOnline_Forms where instanceId = ?";
  private static final String USER_RIGHTS_DELETION =
      "delete from SC_FormsOnline_UserRights where instanceId = ?";
  private static final String GROUP_RIGHTS_DELETION =
      "delete from SC_FormsOnline_GroupRights where instanceId = ?";
  private static final String FORM_INSTANCES_DELETION =
      "delete from SC_FormsOnline_FormInstances where instanceId = ?";
  private static final String FORMS_DELETION =
      "delete from SC_FormsOnline_Forms where instanceId = ?";


  /**
   * Performs pre destruction tasks in the behalf of the specified FormsOnline instance.
   * @param componentInstanceId the unique identifier of the FormsOnline instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    try(Connection connection = DBUtil.openConnection()) {
      deleteForms(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private void deleteForms(Connection con, String componentId) {
    for (String deletion : Arrays.asList(USER_RIGHTS_DELETION, GROUP_RIGHTS_DELETION,
        FORM_INSTANCES_DELETION, FORMS_DELETION)) {
      try (PreparedStatement stmt = con.prepareStatement(deletion)) {
        stmt.setString(1, componentId);
        stmt.executeUpdate();
      } catch (SQLException e) {
        throw new RuntimeException(
            "Cannot delete forms in " + componentId + " (query: " + deletion + ")", e);
      }
    }
  }
}
