/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.components.datawarning.model;

import org.silverpeas.components.datawarning.DataWarningException;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DataWarningDataManager {
  private static final String DATA_WARNING_EX_DATA_ACCESS_FAILED =
      "DataWarning.EX_DATA_ACCESS_FAILED";
  private static final String INSTANCE_ID_MSG = "instanceId = '";
  private SilverpeasBeanDAO<DataWarning> dataWarningDAO;
  private SilverpeasBeanDAO<DataWarningGroup> dataWarningGroupDAO;
  private SilverpeasBeanDAO<DataWarningUser> dataWarningUserDAO;
  private SilverpeasBeanDAO<DataWarningQuery> dataWarningQueryDAO;
  private SilverpeasBeanDAO<DataWarningScheduler> dataWarningSchedulerDAO;

  public DataWarningDataManager() throws DataWarningException {
    try {
      dataWarningDAO =
          SilverpeasBeanDAOFactory.getDAO(DataWarning.class.getName());
      dataWarningGroupDAO =
          SilverpeasBeanDAOFactory.getDAO(DataWarningGroup.class.getName());
      dataWarningUserDAO =
          SilverpeasBeanDAOFactory.getDAO(DataWarningUser.class.getName());
      dataWarningQueryDAO =
          SilverpeasBeanDAOFactory.getDAO(DataWarningQuery.class.getName());
      dataWarningSchedulerDAO =
          SilverpeasBeanDAOFactory.getDAO(DataWarningScheduler.class.getName());
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.DataWarningDataManager()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  // ------------------------------------------------------------------
  // DataWarning object
  // ------------------------------------------------------------------

  public void createDataWarning(DataWarning dw) throws DataWarningException {
    try {
      dataWarningDAO.add(dw);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.createDataWarning()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarning(String instanceId) throws DataWarningException {
    try {
      String whereClause = "instanceID = '" + instanceId + "'";
      dataWarningDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.deleteDataWarning()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void updateDataWarning(DataWarning dw) throws DataWarningException {
    try {
      DataWarning dataWarning = getDataWarning(dw.getInstanceId());
      if (dataWarning == null) {
        createDataWarning(dw);
      } else {
        dataWarningDAO.update(dw);
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.updateDataWarning()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public DataWarning getDataWarning(String instanceId) throws DataWarningException {
    DataWarning retour = null;
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      Collection<DataWarning> datas = dataWarningDAO.findByWhereClause(new IdPK(), whereClause);
      if (datas != null && !datas.isEmpty()) {
        Iterator<DataWarning> it = datas.iterator();
        retour = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarning()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;
  }

  // ------------------------------------------------------------------
  // DataWarningQuery object
  // ------------------------------------------------------------------

  public void createDataWarningQuery(DataWarningQuery dwq) throws DataWarningException {
    try {
      dataWarningQueryDAO.add(dwq);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.createDataWarningQuery()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningQuery(String instanceId) throws DataWarningException {
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      dataWarningQueryDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.deleteDataWarningQuery()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningQuery(String instanceId, int queryCondition)
      throws DataWarningException {
    try {
      String whereClause =
          INSTANCE_ID_MSG + instanceId + "' and queryCondition = " + queryCondition;
      dataWarningQueryDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.deleteDataWarningQuery()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void updateDataWarningQuery(DataWarningQuery dwq) throws DataWarningException {
    try {
      DataWarningQuery dataQuery =
          getDataWarningQuery(dwq.getInstanceId(), dwq.getQueryCondition());
      if (dataQuery == null) {
        createDataWarningQuery(dwq);
      } else {
        dataWarningQueryDAO.update(dwq);
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.updateDataWarningQuery()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public DataWarningQuery getDataWarningQuery(String instanceId, int queryCondition)
      throws DataWarningException {
    DataWarningQuery retour = null;
    try {
      String whereClause =
          INSTANCE_ID_MSG + instanceId + "' and queryCondition = " + queryCondition;
      Collection<DataWarningQuery> datas =
          dataWarningQueryDAO.findByWhereClause(new IdPK(), whereClause);
      if (datas != null && !datas.isEmpty()) {
        Iterator<DataWarningQuery> it = datas.iterator();
        retour = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningQuery()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;

  }

  public Collection<DataWarningQuery> getDataWarningQueries(String instanceId)
      throws DataWarningException {
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      return dataWarningQueryDAO.findByWhereClause(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningQueries()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  // ------------------------------------------------------------------
  // DataWarningScheduler object
  // ------------------------------------------------------------------

  public void createDataWarningScheduler(DataWarningScheduler dws) throws DataWarningException {
    try {
      dataWarningSchedulerDAO.add(dws);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.createDataWarningScheduler()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningScheduler(String instanceId) throws DataWarningException {
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      dataWarningSchedulerDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.deleteDataWarningScheduler()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void updateDataWarningScheduler(DataWarningScheduler dws) throws DataWarningException {
    try {
      DataWarningScheduler scheduler = getDataWarningScheduler(dws.getInstanceId());
      if (scheduler == null) {
        createDataWarningScheduler(dws);
      } else {
        dataWarningSchedulerDAO.update(dws);
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.updateDataWarningScheduler()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public DataWarningScheduler getDataWarningScheduler(String instanceId)
      throws DataWarningException {
    DataWarningScheduler retour = null;
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      Collection<DataWarningScheduler> datas =
          dataWarningSchedulerDAO.findByWhereClause(new IdPK(), whereClause);
      if (datas != null && !datas.isEmpty()) {
        Iterator<DataWarningScheduler> it = datas.iterator();
        retour = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningScheduler()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;
  }

  public List<String> getDataWarningSchedulerInstances() throws DataWarningException {
    List<String> retour = new ArrayList<>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rsSchedulers = null;
    try {
      con = DBUtil.openConnection();
      String sqlForSchedulers =
          "SELECT DISTINCT instanceId FROM SC_DataWarning_Scheduler WHERE SCHEDULERSTATE=" +
              DataWarningScheduler.SCHEDULER_STATE_ON;
      stmt = con.createStatement();
      rsSchedulers = stmt.executeQuery(sqlForSchedulers);
      while (rsSchedulers.next()) {
        retour.add(rsSchedulers.getString(1));
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningSchedulerInstances",
          SilverpeasRuntimeException.WARNING, e.getMessage(), "getDataWarningScheduler", e);
    } finally {
      closeAllConnection(con, stmt, rsSchedulers);
    }
    return retour;
  }

  // ------------------------------------------------------------------
  // DataWarningGroup object
  // ------------------------------------------------------------------

  public void createDataWarningGroup(DataWarningGroup dwg) throws DataWarningException {
    try {
      dataWarningGroupDAO.add(dwg);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.createDataWarningGroup()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningGroup(String instanceId, int groupId) throws DataWarningException {
    try {
      String whereClause =
          INSTANCE_ID_MSG + instanceId + "' and groupId = " + Integer.toString(groupId);
      dataWarningGroupDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.deleteDataWarningGroup()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningGroups(String instanceId) throws DataWarningException {
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      dataWarningGroupDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.deleteDataWarningGroups()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void updateDataWarningGroup(DataWarningGroup dwg) throws DataWarningException {
    try {
      dataWarningGroupDAO.update(dwg);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.updateDataWarningGroup()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public Collection<DataWarningGroup> getDataWarningGroups(String instanceId)
      throws DataWarningException {
    Collection<DataWarningGroup> retour;
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      retour = dataWarningGroupDAO.findByWhereClause(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningGroups()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;
  }

  public DataWarningGroup getDataWarningGroup(String instanceId, int groupId)
      throws DataWarningException {
    DataWarningGroup retour = null;
    try {
      String whereClause =
          INSTANCE_ID_MSG + instanceId + "' and groupId = " + Integer.toString(groupId);
      Collection<DataWarningGroup> datas =
          dataWarningGroupDAO.findByWhereClause(new IdPK(), whereClause);
      if (datas != null && !datas.isEmpty()) {
        Iterator<DataWarningGroup> it = datas.iterator();
        retour = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningGroup()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;
  }

  // ------------------------------------------------------------------
  // DataWarningUser object
  // ------------------------------------------------------------------

  public void createDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    try {
      dataWarningUserDAO.add(dwu);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.createDataWarningUser()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningUser(String instanceId, int userId) throws DataWarningException {
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "' and userId = " + userId;
      dataWarningUserDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningSessionController.deleteDataWarningUser()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void deleteDataWarningUsers(String instanceId) throws DataWarningException {
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "'";
      dataWarningUserDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningSessionController.deleteDataWarningUsers()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public void updateDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    try {
      dataWarningUserDAO.update(dwu);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningSessionController.updateDataWarningUser()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
  }

  public Collection<DataWarningUser> getDataWarningUsers(String instanceId)
      throws DataWarningException {
    Collection<DataWarningUser> retour;
    try {
      String whereClause = "instanceId ='" + instanceId + "'";
      retour = dataWarningUserDAO.findByWhereClause(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException("DataWarningSessionController.getDataWarningUsers()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;
  }

  public DataWarningUser getDataWarningUser(String instanceId, String userId)
      throws DataWarningException {
    DataWarningUser retour = null;
    try {
      String whereClause = INSTANCE_ID_MSG + instanceId + "' and userId = " + userId;
      Collection<DataWarningUser> datas =
          dataWarningUserDAO.findByWhereClause(new IdPK(), whereClause);
      if (datas != null && !datas.isEmpty()) {
        Iterator<DataWarningUser> it = datas.iterator();
        retour = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarningUser()",
          SilverpeasException.ERROR, DATA_WARNING_EX_DATA_ACCESS_FAILED, e);
    }
    return retour;
  }

  /**
   * Ouverture de la connection vers la source de donnees
   * @return Connection la connection
   * @throws DataWarningException
   */
  public Connection openConnection() throws DataWarningException {
    Connection con;
    try {
      con = DBUtil.openConnection();
    } catch (Exception e) {
      throw new DataWarningException(
          DataWarningDataManager.class.getName(),
          SilverpeasRuntimeException.WARNING, e.getMessage(), "openConnection", e);
    }

    return con;
  }

  public void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public void closeAllConnection(Connection con, Statement st) {
    try {
      if (st != null) {
        st.close();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    } finally {
      closeConnection(con);
    }
  }

  public void closeAllConnection(Connection con, Statement st, ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    } finally {
      closeAllConnection(con, st);
    }
  }
}