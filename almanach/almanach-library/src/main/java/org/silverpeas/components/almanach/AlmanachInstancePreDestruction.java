/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.almanach;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.calendar.Calendar;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Before being deleted, remove all events in an almanach instance.
 * @author mmoquillon
 */
@Named
public class AlmanachInstancePreDestruction implements ComponentInstancePreDestruction {

  /**
   * Performs pre destruction tasks in the behalf of the specified Almanach instance.
   * @param componentInstanceId the unique identifier of the Almanach instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    List<Calendar> calendars = Calendar.getByComponentInstanceId(componentInstanceId);
    for (Calendar calendar : calendars) {
      calendar.delete();
    }
  }
}
