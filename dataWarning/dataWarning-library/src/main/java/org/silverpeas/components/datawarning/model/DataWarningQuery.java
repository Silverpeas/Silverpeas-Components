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
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.kernel.logging.SilverLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

public class DataWarningQuery extends SilverpeasBean {

  private static final long serialVersionUID = -7619202031176865356L;
  public static final int QUERY_CONDITION_FIRST = 0;
  // Not used yet
  public static final int QUERY_CONDITION_AND = 1;
  // Not used yet
  public static final int QUERY_CONDITION_OR = 2;
  public static final int QUERY_TYPE_RESULT = 0;
  public static final int QUERY_TYPE_TRIGGER = 1;
  public static final int TRIGGER_CONDITION_SUP = 0;
  public static final int TRIGGER_CONDITION_SUP_OU_EG = 1;
  public static final int TRIGGER_CONDITION_INF = 2;
  public static final int TRIGGER_CONDITION_INF_OU_EG = 3;
  public static final int TRIGGER_CONDITION_EG = 4;
  public static final int TRIGGER_CONDITION_DIF = 5;
  public static final int QUERY_PERSO_NOT_VALID = 0;
  public static final int QUERY_PERSO_VALID = 1;
  public static final String QUERY_PERSO_UID_ID = "QUERY_PERSO_UID_ID";
  public static final String QUERY_PERSO_UID_LOGIN = "QUERY_PERSO_UID_LOGIN";
  public static final String QUERY_PERSO_UID_LASTNAME = "QUERY_PERSO_UID_LASTNAME";
  public static final String QUERY_PERSO_UID_SPECIFICID = "QUERY_PERSO_UID_SPECIFICID";
  public static final String QUERY_PERSO_UID_EMAIL = "QUERY_PERSO_UID_EMAIL";
  private String instanceId;
  private String description = "";
  private String query = "";
  private int queryCondition = QUERY_CONDITION_FIRST;
  private int type = QUERY_TYPE_RESULT;
  private long theTrigger = 0;
  private int theTriggerCondition = TRIGGER_CONDITION_SUP;
  private long theTriggerPrecedent = 0;
  private String persoUID = "";
  private int persoColNB = 1;
  private int persoValid = QUERY_PERSO_NOT_VALID;

  public DataWarningQuery() {
    super();
  }

  public DataWarningQuery(String instanceId) {
    super();
    this.instanceId = instanceId;
  }

  public DataWarningQuery(String instanceId, String description, String query, int queryCondition,
      int type, long theTrigger, int theTriggerCondition, long theTriggerPrecedent) {
    super();
    this.instanceId = instanceId;
    this.description = description;
    this.query = query;
    this.queryCondition = queryCondition;
    this.type = type;
    this.theTrigger = theTrigger;
    this.theTriggerCondition = theTriggerCondition;
    this.theTriggerPrecedent = theTriggerPrecedent;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getDescription() {
    return getSureString(description);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getQuery() {
    return getSureString(query);
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getQueryCondition() {
    return queryCondition;
  }

  public void setQueryCondition(int queryCondition) {
    this.queryCondition = queryCondition;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public long getTheTrigger() {
    return theTrigger;
  }

  public void setTheTrigger(long theTrigger) {
    this.theTrigger = theTrigger;
  }

  public int getTheTriggerCondition() {
    return theTriggerCondition;
  }

  public void setTheTriggerCondition(int theTriggerCondition) {
    this.theTriggerCondition = theTriggerCondition;
  }

  public long getTheTriggerPrecedent() {
    return theTriggerPrecedent;
  }

  public void setTheTriggerPrecedent(long theTriggerPrecedent) {
    this.theTriggerPrecedent = theTriggerPrecedent;
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  @Override
  public String _getTableName() {
    return "SC_DataWarning_Query";
  }

  /**
   * return un tableau a deux dimensions : nom de colonne / valeur
   * [0][...] = nom des colonnes
   * [1][...] = valeurs associees
   * [2][...] = valeurs associees
   * [...][...]
   */
  public DataWarningQueryResult executeQuery(DataWarning dataModel) {
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    DataWarningQueryResult valret =
        new DataWarningQueryResult(this, (getPersoValid() == QUERY_PERSO_VALID), getPersoColNB(),
            getPersoUID());
    try {
      con = dataModel.openConnection();
      prepStmt = con.prepareStatement(getQuery());
      rs = prepStmt.executeQuery();
      if (rs != null) {
        ResultSetMetaData rsmd = rs.getMetaData();
        if (rsmd != null) {
          //get columns names
          for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
            valret.addColumn(rsmd.getColumnName(i));
          }
        }
        int j = 1;
        while (rs.next() && ((dataModel.getRowLimit() <= 0) || (j <= dataModel.getRowLimit()))) {
          ArrayList ligne = new ArrayList();
          for (int k = 1; k < rsmd.getColumnCount() + 1; k++) {
            ligne.add(rs.getString(k));
          }
          valret.addRow(ligne);
          j++;
        }
      }
    } catch (Exception e) {
      valret.addError(e, getQuery());
      SilverLogger.getLogger(this).error("Fail to execute query " + getQuery(), e);
    } finally {
      DBUtil.close(rs, prepStmt);
      dataModel.closeConnection(con);
    }
    return valret;
  }

  public boolean checkTriggerSatisfied(DataWarning dataModel) throws DataWarningException {
    return checkTriggerSatisfied(executeQuery(dataModel).returnTriggerValueFromResult());
  }

  public boolean checkTriggerSatisfied(long theValue) {
    // default is true
    boolean valret = true;

    switch (theTriggerCondition) {
      case TRIGGER_CONDITION_SUP:
        valret = (theValue > theTrigger);
        break;
      case TRIGGER_CONDITION_SUP_OU_EG:
        valret = (theValue >= theTrigger);
        break;
      case TRIGGER_CONDITION_INF:
        valret = (theValue < theTrigger);
        break;
      case TRIGGER_CONDITION_INF_OU_EG:
        valret = (theValue <= theTrigger);
        break;
      case TRIGGER_CONDITION_EG:
        valret = (theValue == theTrigger);
        break;
      case TRIGGER_CONDITION_DIF:
        valret = (theValue != theTrigger);
        break;
      default:
        break;
    }
    return valret;
  }

  /**
   * @return Returns the persoColNB.
   */
  public int getPersoColNB() {
    return persoColNB;
  }

  /**
   * @param persoColNB The persoColNB to set.
   */
  public void setPersoColNB(int persoColNB) {
    this.persoColNB = persoColNB;
  }

  /**
   * @return Returns the persoUID.
   */
  public String getPersoUID() {
    return persoUID;
  }

  /**
   * @param persoUID The persoUID to set.
   */
  public void setPersoUID(String persoUID) {
    this.persoUID = persoUID;
  }

  /**
   * @return Returns the persoValid.
   */
  public int getPersoValid() {
    return persoValid;
  }

  /**
   * @param persoValid The persoValid to set.
   */
  public void setPersoValid(int persoValid) {
    this.persoValid = persoValid;
  }
}