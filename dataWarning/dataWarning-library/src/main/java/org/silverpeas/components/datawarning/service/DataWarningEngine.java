/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.components.datawarning.service;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.components.datawarning.DataWarningException;
import org.silverpeas.components.datawarning.model.DataWarning;
import org.silverpeas.components.datawarning.model.DataWarningDataManager;
import org.silverpeas.components.datawarning.model.DataWarningGroup;
import org.silverpeas.components.datawarning.model.DataWarningQuery;
import org.silverpeas.components.datawarning.model.DataWarningQueryResult;
import org.silverpeas.components.datawarning.model.DataWarningResult;
import org.silverpeas.components.datawarning.model.DataWarningScheduler;
import org.silverpeas.components.datawarning.model.DataWarningUser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DataWarningEngine {

  private String componentId = null;
  private DataWarningDataManager dataManager = null;
  private DataWarning dataWarning = null;
  private DataWarningScheduler dataScheduler = null;
  private Map<Integer, DataWarningQuery> dataWarningQueries = null;

  public DataWarningEngine(String cid) throws DataWarningException {
    dataManager = new DataWarningDataManager();
    componentId = cid;
    init();
  }

  public void init() throws DataWarningException {
    Collection<DataWarningQuery> queries;
    dataWarning = dataManager.getDataWarning(componentId);
    dataScheduler = dataManager.getDataWarningScheduler(componentId);

    // Get Queries
    queries = dataManager.getDataWarningQueries(componentId);
    dataWarningQueries = Collections.synchronizedMap(new HashMap<>());
    for (DataWarningQuery query : queries) {
      dataWarningQueries.put(query.getType(), query);
    }
  }

  // --- DataWarning ---
  public DataWarningResult run() {
    DataWarningResult valret = null;
    DataWarningQuery theQuery = null;
    DataWarningQueryResult theResult = null;


    switch (dataWarning.getAnalysisType()) {
      case DataWarning.INCONDITIONAL_QUERY:
        valret = new DataWarningResult(false);
        theQuery = getDataWarningQuery(DataWarningQuery.QUERY_TYPE_RESULT);
        theResult = theQuery.executeQuery(dataWarning);
        valret.setDataQuery(theQuery);
        valret.setQueryResult(theResult);
        break;
      case DataWarning.TRIGGER_ANALYSIS:
        valret = new DataWarningResult(true);
        valret.setTriggerQuery(getDataWarningQuery(DataWarningQuery.QUERY_TYPE_TRIGGER));
        valret.computeTriggerResult(dataWarning);
        if (valret.getTriggerEnabled()) {
          // Get the result
          theQuery = getDataWarningQuery(DataWarningQuery.QUERY_TYPE_RESULT);
          if (theQuery.getQuery().length() > 0) {
            theResult = theQuery.executeQuery(dataWarning);
          }
        }
        valret.setDataQuery(theQuery);
        valret.setQueryResult(theResult);
        break;
    }
    return valret;
  }

  public void updateDataWarning(DataWarning dw) throws DataWarningException {
    if (dataWarning.getAnalysisType() != dw.getAnalysisType()) {
      stopScheduler();
      deleteDataQueries();
      switch (dw.getAnalysisType()) {
        case DataWarning.INCONDITIONAL_QUERY:
          createDataWarningQuery(DataWarningQuery.QUERY_TYPE_RESULT);
          break;
        case DataWarning.TRIGGER_ANALYSIS:
          createDataWarningQuery(DataWarningQuery.QUERY_TYPE_RESULT);
          createDataWarningQuery(DataWarningQuery.QUERY_TYPE_TRIGGER);
          break;
      }
    }
    dataManager.updateDataWarning(dw);
    dataWarning = dataManager.getDataWarning(componentId);
    restartSchedulerIfNeeded();
  }

  public DataWarning getDataWarning() {
    return dataWarning;
  }

  public DataWarning getDataWarningWritable() {
    return (DataWarning) dataWarning.clone();
  }

  // --- DataWarningQuery ---
  protected void createDataWarningQuery(int queryType) throws DataWarningException {
    DataWarningQuery dwq = new DataWarningQuery();

    SilverTrace
        .info("dataWarning", "DataWarningEngine.createDataWarningQuery()", "root.MSG_ENTER_METHOD",
            "queryType=" + queryType);
    dwq.setInstanceId(componentId);
    dwq.setType(queryType);
    dataManager.createDataWarningQuery(dwq);
    dataWarningQueries.put(queryType, dwq);
    restartSchedulerIfNeeded();
  }

  protected void deleteDataQueries() throws DataWarningException {
    dataManager.deleteDataWarningQuery(componentId);
    restartSchedulerIfNeeded();
  }

  public void updateDataWarningQuery(DataWarningQuery dwq) throws DataWarningException {
    dataManager.updateDataWarningQuery(dwq);
    dataWarningQueries.put(dwq.getType(), dwq);
    restartSchedulerIfNeeded();
  }

  public DataWarningQuery getDataWarningQuery(int queryType) {
    return dataWarningQueries.get(queryType);
  }

  // --- DataWarningScheduler ---
  public void updateDataWarningScheduler(DataWarningScheduler dws) throws DataWarningException {
    dws.setWakeUp(0);
    dataManager.updateDataWarningScheduler(dws);
    dataScheduler = dataManager.getDataWarningScheduler(componentId);
    restartSchedulerIfNeeded();
  }

  public void updateSchedulerWakeUp(long nextTime) {
    try {
      dataScheduler = dataManager.getDataWarningScheduler(componentId);
      dataScheduler.setWakeUp(nextTime);
      dataManager.updateDataWarningScheduler(dataScheduler);
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarningEngine.updateSchedulerWakeUp()",
          "root.MSG_GEN_ENTER_METHOD", "componentId=" + componentId);
    }
  }

  public DataWarningScheduler getDataWarningScheduler() {
    return dataScheduler;
  }

  public void restartSchedulerIfNeeded() {
    if (dataScheduler.getSchedulerState() == DataWarningScheduler.SCHEDULER_STATE_ON) {
      DataWarningSchedulerTable.removeScheduler(componentId);
      DataWarningSchedulerTable.addScheduler(componentId);
    }
  }

  public void startScheduler() throws DataWarningException {
    //get scheduler to update state
    dataScheduler.setSchedulerState(DataWarningScheduler.SCHEDULER_STATE_ON);
    updateDataWarningScheduler(dataScheduler);
    //add scheduler
    DataWarningSchedulerTable.addScheduler(componentId);
  }

  public void stopScheduler() throws DataWarningException {
    //remove scheduler
    DataWarningSchedulerTable.removeScheduler(componentId);
    //get scheduler to update state
    dataScheduler.setSchedulerState(DataWarningScheduler.SCHEDULER_STATE_OFF);
    updateDataWarningScheduler(dataScheduler);
  }

  // --- DataWarningGroup ---
  public void createDataWarningGroup(DataWarningGroup dwg) throws DataWarningException {
    dataManager.createDataWarningGroup(dwg);
    restartSchedulerIfNeeded();
  }

  public void deleteDataWarningGroup(int groupId) throws DataWarningException {
    dataManager.deleteDataWarningGroup(componentId, groupId);
    restartSchedulerIfNeeded();
  }

  public void deleteDataWarningGroups() throws DataWarningException {
    dataManager.deleteDataWarningGroups(componentId);
    restartSchedulerIfNeeded();
  }

  public void updateDataWarningGroup(DataWarningGroup dwg) throws DataWarningException {
    dataManager.updateDataWarningGroup(dwg);
    restartSchedulerIfNeeded();
  }

  public Collection<DataWarningGroup> getDataWarningGroups() throws DataWarningException {
    return dataManager.getDataWarningGroups(componentId);
  }

  public DataWarningGroup getDataWarningGroup(int groupId) throws DataWarningException {
    return dataManager.getDataWarningGroup(componentId, groupId);
  }

  // --- DataWarningUser ---
  public void createDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    dataManager.createDataWarningUser(dwu);
    restartSchedulerIfNeeded();
  }

  public void deleteDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    dataManager.deleteDataWarningUser(dwu.getInstanceId(), dwu.getUserId());
    restartSchedulerIfNeeded();
  }

  public void deleteDataWarningUsers() throws DataWarningException {
    dataManager.deleteDataWarningUsers(componentId);
    restartSchedulerIfNeeded();
  }

  public void updateDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    dataManager.updateDataWarningUser(dwu);
    restartSchedulerIfNeeded();
  }

  public Collection<DataWarningUser> getDataWarningUsers() throws DataWarningException {
    return dataManager.getDataWarningUsers(componentId);
  }

  public DataWarningUser getDataWarningUser(String userId) throws DataWarningException {
    return dataManager.getDataWarningUser(componentId, userId);
  }
};