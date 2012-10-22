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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http:/www.silverpeas.com/legal/licensing"
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

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.dataWarning.model.DataWarning;
import com.silverpeas.dataWarning.model.DataWarningDataManager;
import com.silverpeas.dataWarning.model.DataWarningQuery;
import com.silverpeas.dataWarning.model.DataWarningScheduler;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.sql.Connection;

/**
 *
 * @author  nesseric
 *
 */
public class DataWarningInstanciator implements ComponentsInstanciatorIntf {

  public DataWarningInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("dataWarning", "DataWarningInstanciator.create()", "root.MSG_GEN_ENTER_METHOD");
    try {
      DataWarningDataManager dataManager = new DataWarningDataManager();
      dataManager.createDataWarning(new DataWarning("", "", "", "", 0, componentId,
          DataWarning.INCONDITIONAL_QUERY));
      dataManager.createDataWarningScheduler(new DataWarningScheduler(componentId, 1,
          DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR, 0, 0, 2, 0, 0,
          DataWarningScheduler.SCHEDULER_STATE_OFF));
      dataManager.createDataWarningQuery(new DataWarningQuery(componentId));
    } catch (DataWarningException dwe) {
      throw new InstanciationException("DataWarningInstanciator.create()", SilverpeasException.ERROR,
          "DataWarning.EX_DATA_ACCESS_FAILED", dwe);
    }
    SilverTrace.info("dataWarning", "DataWarningInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");

  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("dataWarning", "DataWarningInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD");
    try {
      DataWarningDataManager dataManager = new DataWarningDataManager();
      dataManager.deleteDataWarningUsers(componentId);
      dataManager.deleteDataWarningGroups(componentId);
      dataManager.deleteDataWarningQuery(componentId);
      dataManager.deleteDataWarningScheduler(componentId);
      dataManager.deleteDataWarning(componentId);
    } catch (DataWarningException dwe) {
      throw new InstanciationException("DataWarningInstanciator.delete()", SilverpeasException.ERROR,
          "DataWarning.EX_DATA_ACCESS_FAILED", dwe);
    }
    SilverTrace.info("dataWarning", "DataWarningInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }
}
