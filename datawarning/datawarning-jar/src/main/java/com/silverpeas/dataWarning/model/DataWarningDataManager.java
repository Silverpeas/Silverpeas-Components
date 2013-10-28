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

package com.silverpeas.dataWarning.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.dataWarning.DataWarningException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.pool.ConnectionPool;
import java.util.List;

public class DataWarningDataManager {
  private SilverpeasBeanDAO DataWarningDAO;
  private SilverpeasBeanDAO DataWarningGroupDAO;
  private SilverpeasBeanDAO DataWarningUserDAO;
  private SilverpeasBeanDAO DataWarningQueryDAO;
  private SilverpeasBeanDAO DataWarningSchedulerDAO;

  public DataWarningDataManager() throws DataWarningException {
    try {
      DataWarningDAO = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.dataWarning.model.DataWarning");
      DataWarningGroupDAO = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.dataWarning.model.DataWarningGroup");
      DataWarningUserDAO = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.dataWarning.model.DataWarningUser");
      DataWarningQueryDAO = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.dataWarning.model.DataWarningQuery");
      DataWarningSchedulerDAO = SilverpeasBeanDAOFactory
          .getDAO("com.silverpeas.dataWarning.model.DataWarningScheduler");
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.DataWarningDataManager()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  // ------------------------------------------------------------------
  // DataWarning object
  // ------------------------------------------------------------------

  public void createDataWarning(DataWarning dw) throws DataWarningException {
    try {
      DataWarningDAO.add(dw);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.createDataWarning()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarning(String instanceId) throws DataWarningException {
    try {
      String whereClause = "instanceID = '" + instanceId + "'";
      DataWarningDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.deleteDataWarning()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void updateDataWarning(DataWarning dw) throws DataWarningException {
    try {
      DataWarning dataWarning = getDataWarning(dw.getInstanceId());
      if (dataWarning == null)
        createDataWarning(dw);
      else
        DataWarningDAO.update(dw);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.updateDataWarning()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public DataWarning getDataWarning(String instanceId)
      throws DataWarningException {
    DataWarning retour = null;
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      Collection datas = DataWarningDAO.findByWhereClause(new IdPK(),
          whereClause);
      if (datas != null && datas.size() > 0) {
        Iterator it = datas.iterator();
        retour = (DataWarning) it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningDataManager.getDataWarning()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;
  }

  // ------------------------------------------------------------------
  // DataWarningQuery object
  // ------------------------------------------------------------------

  public void createDataWarningQuery(DataWarningQuery dwq)
      throws DataWarningException {
    try {
      DataWarningQueryDAO.add(dwq);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.createDataWarningQuery()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningQuery(String instanceId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      DataWarningQueryDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.deleteDataWarningQuery()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningQuery(String instanceId, int queryCondition)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId
          + "' and queryCondition = " + queryCondition;
      DataWarningQueryDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.deleteDataWarningQuery()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void updateDataWarningQuery(DataWarningQuery dwq)
      throws DataWarningException {
    try {
      DataWarningQuery dataQuery = getDataWarningQuery(dwq.getInstanceId(), dwq
          .getQueryCondition());
      if (dataQuery == null)
        createDataWarningQuery(dwq);
      else
        DataWarningQueryDAO.update(dwq);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.updateDataWarningQuery()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public DataWarningQuery getDataWarningQuery(String instanceId,
      int queryCondition) throws DataWarningException {
    DataWarningQuery retour = null;
    try {
      String whereClause = "instanceId = '" + instanceId
          + "' and queryCondition = " + queryCondition;
      Collection datas = DataWarningQueryDAO.findByWhereClause(new IdPK(),
          whereClause);
      if (datas != null && datas.size() > 0) {
        Iterator it = datas.iterator();
        retour = (DataWarningQuery) it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningQuery()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;

  }

  public Collection getDataWarningQueries(String instanceId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      return DataWarningQueryDAO.findByWhereClause(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningQueries()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  // ------------------------------------------------------------------
  // DataWarningScheduler object
  // ------------------------------------------------------------------

  public void createDataWarningScheduler(DataWarningScheduler dws)
      throws DataWarningException {
    try {
      DataWarningSchedulerDAO.add(dws);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.createDataWarningScheduler()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningScheduler(String instanceId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      DataWarningSchedulerDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.deleteDataWarningScheduler()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void updateDataWarningScheduler(DataWarningScheduler dws)
      throws DataWarningException {
    try {
      DataWarningScheduler scheduler = getDataWarningScheduler(dws
          .getInstanceId());
      if (scheduler == null)
        createDataWarningScheduler(dws);
      else
        DataWarningSchedulerDAO.update(dws);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.updateDataWarningScheduler()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public DataWarningScheduler getDataWarningScheduler(String instanceId)
      throws DataWarningException {
    DataWarningScheduler retour = null;
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      Collection datas = DataWarningSchedulerDAO.findByWhereClause(new IdPK(),
          whereClause);
      if (datas != null && datas.size() > 0) {
        Iterator it = datas.iterator();
        retour = (DataWarningScheduler) it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningScheduler()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;
  }

  public List<String> getDataWarningSchedulerInstances()
      throws DataWarningException {
    List<String> retour = new ArrayList<String>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rs_Schedulers = null;
    try {
      con = ConnectionPool.getConnection();
      String sqlForSchedulers = "SELECT DISTINCT instanceId FROM SC_DataWarning_Scheduler WHERE SCHEDULERSTATE="
          + DataWarningScheduler.SCHEDULER_STATE_ON;
      stmt = con.createStatement();
      rs_Schedulers = stmt.executeQuery(sqlForSchedulers);
      while (rs_Schedulers.next())
        retour.add(rs_Schedulers.getString(1));
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningSchedulerInstances",
          SilverpeasRuntimeException.WARNING, e.getMessage(),
          "getDataWarningScheduler", e);
    } finally {
      closeAllConnection(con, stmt, rs_Schedulers);
    }
    return retour;
  }

  // ------------------------------------------------------------------
  // DataWarningGroup object
  // ------------------------------------------------------------------

  public void createDataWarningGroup(DataWarningGroup dwg)
      throws DataWarningException {
    try {
      DataWarningGroupDAO.add(dwg);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.createDataWarningGroup()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningGroup(String instanceId, int groupId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "' and groupId = "
          + Integer.toString(groupId);
      DataWarningGroupDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.deleteDataWarningGroup()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningGroups(String instanceId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      DataWarningGroupDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.deleteDataWarningGroups()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void updateDataWarningGroup(DataWarningGroup dwg)
      throws DataWarningException {
    try {
      DataWarningGroupDAO.update(dwg);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.updateDataWarningGroup()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public Collection getDataWarningGroups(String instanceId)
      throws DataWarningException {
    Collection retour = null;
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      retour = DataWarningGroupDAO.findByWhereClause(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningGroups()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;
  }

  public DataWarningGroup getDataWarningGroup(String instanceId, int groupId)
      throws DataWarningException {
    DataWarningGroup retour = null;
    try {
      String whereClause = "instanceId = '" + instanceId + "' and groupId = "
          + Integer.toString(groupId);
      Collection datas = DataWarningGroupDAO.findByWhereClause(new IdPK(),
          whereClause);
      if (datas != null && datas.size() > 0) {
        Iterator it = datas.iterator();
        retour = (DataWarningGroup) it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningGroup()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;
  }

  // ------------------------------------------------------------------
  // DataWarningUser object
  // ------------------------------------------------------------------

  public void createDataWarningUser(DataWarningUser dwu)
      throws DataWarningException {
    try {
      DataWarningUserDAO.add(dwu);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.createDataWarningUser()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningUser(String instanceId, int userId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "' and userId = "
          + userId;
      DataWarningUserDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningSessionController.deleteDataWarningUser()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void deleteDataWarningUsers(String instanceId)
      throws DataWarningException {
    try {
      String whereClause = "instanceId = '" + instanceId + "'";
      DataWarningUserDAO.removeWhere(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningSessionController.deleteDataWarningUsers()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void updateDataWarningUser(DataWarningUser dwu)
      throws DataWarningException {
    try {
      DataWarningUserDAO.update(dwu);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningSessionController.updateDataWarningUser()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public Collection getDataWarningUsers(String instanceId)
      throws DataWarningException {
    Collection retour = null;
    try {
      String whereClause = "instanceId ='" + instanceId + "'";
      retour = DataWarningUserDAO.findByWhereClause(new IdPK(), whereClause);
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningSessionController.getDataWarningUsers()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;
  }

  public DataWarningUser getDataWarningUser(String instanceId, String userId)
      throws DataWarningException {
    DataWarningUser retour = null;
    try {
      String whereClause = "instanceId = '" + instanceId + "' and userId = "
          + userId;
      Collection datas = DataWarningUserDAO.findByWhereClause(new IdPK(),
          whereClause);
      if (datas != null && datas.size() > 0) {
        Iterator it = datas.iterator();
        retour = (DataWarningUser) it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(
          "DataWarningDataManager.getDataWarningUser()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
    return retour;
  }

  /**
   * Ouverture de la connection vers la source de donnees
   *
   * @return Connection la connection
   * @exception DataWarningException
   */
  public Connection openConnection() throws DataWarningException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.DATAWARNING_DATASOURCE);
    } catch (RuntimeException e) {
      throw new DataWarningException(
          "com.silverpeas.dataWarning.implementation.DataWarningDataManager",
          SilverpeasRuntimeException.WARNING, e.getMessage(), "openConnection",
          e);
    } catch (Exception e) {
      throw new DataWarningException(
          "com.silverpeas.dataWarning.implementation.DataWarningDataManager",
          SilverpeasRuntimeException.WARNING, e.getMessage(), "openConnection",
          e);
    }

    return con;
  }

  public void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      SilverTrace.error("dataWarning",
          "DataWarningDataManager.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    }
  }

  public void closeAllConnection(Connection con, Statement st) {
    try {
      if (st != null) {
        st.close();
      }
    } catch (Exception e) {
      SilverTrace.error("dataWarning",
          "DataWarningDataManager.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", e);
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
      SilverTrace.error("dataWarning",
          "DataWarningDataManager.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", e);
    } finally {
      closeAllConnection(con, st);
    }
  }
}