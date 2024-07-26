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

package org.silverpeas.components.datawarning.model;

import org.silverpeas.components.datawarning.DataWarningException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.bean.BeanCriteria;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("deprecation")
public class DataWarningDataManager {
  private static final String INSTANCE_ID = "instanceId";
  private final SilverpeasBeanDAO<DataWarning> dataWarningDAO;
  private final SilverpeasBeanDAO<DataWarningGroup> dataWarningGroupDAO;
  private final SilverpeasBeanDAO<DataWarningUser> dataWarningUserDAO;
  private final SilverpeasBeanDAO<DataWarningQuery> dataWarningQueryDAO;
  private final SilverpeasBeanDAO<DataWarningScheduler> dataWarningSchedulerDAO;

  public DataWarningDataManager() throws DataWarningException {
    try {
      dataWarningDAO = SilverpeasBeanDAOFactory.getDAO(DataWarning.class);
      dataWarningGroupDAO = SilverpeasBeanDAOFactory.getDAO(DataWarningGroup.class);
      dataWarningUserDAO = SilverpeasBeanDAOFactory.getDAO(DataWarningUser.class);
      dataWarningQueryDAO = SilverpeasBeanDAOFactory.getDAO(DataWarningQuery.class);
      dataWarningSchedulerDAO = SilverpeasBeanDAOFactory.getDAO(DataWarningScheduler.class);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  // ------------------------------------------------------------------
  // DataWarning object
  // ------------------------------------------------------------------

  public void createDataWarning(DataWarning dw) throws DataWarningException {
    try {
      dataWarningDAO.add(dw);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarning(String instanceId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      dataWarningDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
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
      throw new DataWarningException(e);
    }
  }

  public DataWarning getDataWarning(String instanceId) throws DataWarningException {
    DataWarning bean = null;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      Collection<DataWarning> data = dataWarningDAO.findBy(criteria);
      if (data != null && !data.isEmpty()) {
        Iterator<DataWarning> it = data.iterator();
        bean = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
    return bean;
  }

  // ------------------------------------------------------------------
  // DataWarningQuery object
  // ------------------------------------------------------------------

  public void createDataWarningQuery(DataWarningQuery dwq) throws DataWarningException {
    try {
      dataWarningQueryDAO.add(dwq);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarningQuery(String instanceId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      dataWarningQueryDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
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
      throw new DataWarningException(e);
    }
  }

  public DataWarningQuery getDataWarningQuery(String instanceId, int queryCondition)
      throws DataWarningException {
    DataWarningQuery bean = null;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId)
          .and("queryCondition", queryCondition);
      Collection<DataWarningQuery> data =
          dataWarningQueryDAO.findBy(criteria);
      if (data != null && !data.isEmpty()) {
        Iterator<DataWarningQuery> it = data.iterator();
        bean = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
    return bean;

  }

  public Collection<DataWarningQuery> getDataWarningQueries(String instanceId)
      throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      return dataWarningQueryDAO.findBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  // ------------------------------------------------------------------
  // DataWarningScheduler object
  // ------------------------------------------------------------------

  public void createDataWarningScheduler(DataWarningScheduler dws) throws DataWarningException {
    try {
      dataWarningSchedulerDAO.add(dws);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarningScheduler(String instanceId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      dataWarningSchedulerDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
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
      throw new DataWarningException(e);
    }
  }

  public DataWarningScheduler getDataWarningScheduler(String instanceId)
      throws DataWarningException {
    DataWarningScheduler bean = null;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      Collection<DataWarningScheduler> data =
          dataWarningSchedulerDAO.findBy(criteria);
      if (data != null && !data.isEmpty()) {
        Iterator<DataWarningScheduler> it = data.iterator();
        bean = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
    return bean;
  }

  public List<String> getDataWarningSchedulerInstances() throws DataWarningException {
    List<String> retour = new ArrayList<>();
    Connection con = null;
    Statement stmt = null;
    ResultSet rsSchedulers = null;
    try {
      con = DBUtil.openConnection();
      //noinspection SqlNoDataSourceInspection
      String sqlForSchedulers =
          "SELECT DISTINCT instanceId FROM SC_DataWarning_Scheduler WHERE SCHEDULERSTATE=" +
              DataWarningScheduler.SCHEDULER_STATE_ON;
      stmt = con.createStatement();
      rsSchedulers = stmt.executeQuery(sqlForSchedulers);
      while (rsSchedulers.next()) {
        retour.add(rsSchedulers.getString(1));
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
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
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarningGroup(String instanceId, int groupId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId)
          .and("groupId", groupId);
      dataWarningGroupDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarningGroups(String instanceId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      dataWarningGroupDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void updateDataWarningGroup(DataWarningGroup dwg) throws DataWarningException {
    try {
      dataWarningGroupDAO.update(dwg);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public Collection<DataWarningGroup> getDataWarningGroups(String instanceId)
      throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      return dataWarningGroupDAO.findBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public DataWarningGroup getDataWarningGroup(String instanceId, int groupId)
      throws DataWarningException {
    DataWarningGroup bean = null;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId)
          .and("groupId", groupId);
      Collection<DataWarningGroup> data =
          dataWarningGroupDAO.findBy(criteria);
      if (data != null && !data.isEmpty()) {
        Iterator<DataWarningGroup> it = data.iterator();
        bean = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
    return bean;
  }

  // ------------------------------------------------------------------
  // DataWarningUser object
  // ------------------------------------------------------------------

  public void createDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    try {
      dataWarningUserDAO.add(dwu);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarningUser(String instanceId, int userId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId)
          .and("userId", userId);
      dataWarningUserDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void deleteDataWarningUsers(String instanceId) throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      dataWarningUserDAO.removeBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public void updateDataWarningUser(DataWarningUser dwu) throws DataWarningException {
    try {
      dataWarningUserDAO.update(dwu);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public Collection<DataWarningUser> getDataWarningUsers(String instanceId)
      throws DataWarningException {
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId);
      return dataWarningUserDAO.findBy(criteria);
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
  }

  public DataWarningUser getDataWarningUser(String instanceId, String userId)
      throws DataWarningException {
    DataWarningUser bean = null;
    try {
      BeanCriteria criteria = BeanCriteria.addCriterion(INSTANCE_ID, instanceId)
          .and("userId", Integer.parseInt(userId));
      Collection<DataWarningUser> data =
          dataWarningUserDAO.findBy(criteria);
      if (data != null && !data.isEmpty()) {
        Iterator<DataWarningUser> it = data.iterator();
        bean = it.next();
      }
    } catch (Exception e) {
      throw new DataWarningException(e);
    }
    return bean;
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