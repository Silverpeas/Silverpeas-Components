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
package com.silverpeas.dataWarning;

import com.silverpeas.dataWarning.control.DataWarningSchedulerTable;
import com.silverpeas.dataWarning.model.DataWarningDataManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.initialization.Initialization;

import java.util.List;

/**
 * Initializes the data warning application by setting up the schedulers.
 */
public class DataWarningInitialize implements Initialization {

  public DataWarningInitialize() {
  }

  @Override
  public void init() {
    try {
      DataWarningDataManager dwdm = new DataWarningDataManager();
      List<String> schedulerInstances = dwdm.getDataWarningSchedulerInstances();
      for (String scheduler : schedulerInstances) {
        DataWarningSchedulerTable.addScheduler(scheduler);
      }
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarningInitialize.initialize()", "", e);
    }
  }
}
