/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.projectmanager;

import org.silverpeas.components.projectmanager.model.ProjectManagerCalendarDAO;
import org.silverpeas.components.projectmanager.model.ProjectManagerDAO;
import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Deletes all the tasks, associated resources, and all the holiday dates that were registered for
 * the ProjectManager that is being deleted.
 * @author mmoquillon
 */
@Technical
@Bean
@Named
public class ProjectManagerInstancePreDestruction implements ComponentInstancePreDestruction {
  /**
   * Performs pre destruction tasks in the behalf of the specified component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      ProjectManagerCalendarDAO.removeAllHolidayDates(connection, componentInstanceId);
      ProjectManagerDAO.removeAllTasks(connection, componentInstanceId);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
