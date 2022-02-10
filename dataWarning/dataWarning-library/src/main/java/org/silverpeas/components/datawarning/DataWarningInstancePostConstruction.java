/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.datawarning;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.ComponentInstancePostConstruction;
import org.silverpeas.components.datawarning.model.DataWarning;
import org.silverpeas.components.datawarning.model.DataWarningDataManager;
import org.silverpeas.components.datawarning.model.DataWarningQuery;
import org.silverpeas.components.datawarning.model.DataWarningScheduler;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Once an instance of the DataWarning application is created, creates a data warning entity,
 * prepares a data warning scheduler and query.
 * @author mmoquillon
 */
@Named
public class DataWarningInstancePostConstruction implements ComponentInstancePostConstruction {
  /**
   * Performs post construction tasks in the behalf of the specified DataWarning instance.
   * @param componentInstanceId the unique identifier of the DataWarning instance.
   */
  @Transactional
  @Override
  public void postConstruct(final String componentInstanceId) {
    try {
      final int tuesday = 2;
      DataWarningDataManager dataManager = new DataWarningDataManager();
      dataManager.createDataWarning(
          new DataWarning("", "", "", "", 0, componentInstanceId, DataWarning.INCONDITIONAL_QUERY));
      dataManager.createDataWarningScheduler(new DataWarningScheduler(componentInstanceId, 1,
          DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR, 0, 0, tuesday, 0, 0,
          DataWarningScheduler.SCHEDULER_STATE_OFF));
      dataManager.createDataWarningQuery(new DataWarningQuery(componentInstanceId));
    } catch (DataWarningException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
