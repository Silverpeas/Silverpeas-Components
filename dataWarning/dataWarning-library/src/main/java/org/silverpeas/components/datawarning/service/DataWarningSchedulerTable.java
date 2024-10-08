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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.datawarning.service;

import org.silverpeas.kernel.logging.SilverLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataWarningSchedulerTable {

  private static final Map<String, DataWarningSchedulerImpl> schedulers =
      Collections.synchronizedMap(new HashMap<>());

  private DataWarningSchedulerTable() {

  }

  /**
   * Add a scheduler
   */
  public static void addScheduler(String instanceId) {

    //test if connector already exist
    DataWarningSchedulerImpl theScheduler = getScheduler(instanceId);
    if (theScheduler != null) {
      removeScheduler(instanceId);
    } else {
      theScheduler = new DataWarningSchedulerImpl(instanceId);
    }

    //add the scheduler
    schedulers.put(instanceId, theScheduler);

    //launch the scheduler
    theScheduler.start();
  }

  /**
   * Remove a scheduler with the instance id
   */
  public static void removeScheduler(String instanceId) {

    try {
      DataWarningSchedulerImpl theScheduler = getScheduler(instanceId);
      if (theScheduler != null) {
        theScheduler.stop();
        schedulers.remove(instanceId);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(DataWarningSchedulerTable.class).error(e);
    }
  }

  /**
   * Get the scheduler associated with an instance
   */
  public static DataWarningSchedulerImpl getScheduler(String instanceId) {
    return schedulers.get(instanceId);
  }
}