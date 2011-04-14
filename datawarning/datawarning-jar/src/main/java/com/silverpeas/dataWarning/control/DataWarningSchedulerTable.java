/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.silverpeas.dataWarning.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataWarningSchedulerTable {

  private static Map<String, DataWarningSchedulerImpl> schedulers =
      Collections.synchronizedMap(new HashMap<String, DataWarningSchedulerImpl>());

  /**
   * Add a scheduler
   */
  public static void addScheduler(String instanceId) {
    SilverTrace.info("dataWarning", "DataWarningSchedulerTable.addScheduler()",
        "root.MSG_GEN_PARAM_VALUE", "Add Scheduler : " + instanceId);
    //test if connecteur already exist
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
    SilverTrace.info("dataWarning", "DataWarningSchedulerTable.removeScheduler()",
        "root.MSG_GEN_PARAM_VALUE", "Remove Scheduler : " + instanceId);
    try {
      DataWarningSchedulerImpl theScheduler = getScheduler(instanceId);
      if (theScheduler != null) {
        theScheduler.stop();
        schedulers.remove(instanceId);
      }
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarningSchedulerTable.removeScheduler()", "", e);
    }
  }

  /**
   * Get the scheduler associated with an instance
   */
  public static DataWarningSchedulerImpl getScheduler(String instanceId) {
    return (DataWarningSchedulerImpl) schedulers.get(instanceId);
  }
};