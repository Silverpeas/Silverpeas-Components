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

import java.util.*;

import com.silverpeas.dataWarning.DataWarningException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.WithNested;
import org.silverpeas.core.admin.OrganisationController;

public class DataWarningQueryResult extends Object {

  protected DataWarningQuery queryParent = null;
  // Query result
  protected ArrayList columns = null;
  protected ArrayList values = null;
  protected boolean hasError = false;
  protected Exception errEx = null;
  protected String errQuery = "";
  protected String errLowestLevel = "";
  protected String errFullText = "";
  protected HashMap valuesByUser = null;
  protected int persoColumnNumber = 0;
  protected String persoUID = "";
  protected OrganisationController oc = new OrganizationController();

  public DataWarningQueryResult(DataWarningQuery qp, boolean pe, int colNum, String puid) {
    queryParent = qp;
    columns = new ArrayList();
    values = new ArrayList();
    hasError = false;
    if (pe) {
      valuesByUser = new HashMap();
    } else {
      valuesByUser = null;
    }
    persoColumnNumber = colNum - 1; // Turn to Zero-based number
    persoUID = puid;
  }

  public DataWarningQuery getParent() {
    return queryParent;
  }

  // Error functions
  // -----------------
  public boolean hasError() {
    return hasError;
  }

  public String getErrorFullText() {
    return errFullText;
  }

  public Exception getErrorException() {
    return errEx;
  }

  public void addError(Exception ex, String sqlQuery) {
    StringBuilder sb = new StringBuilder();
    Throwable nested;
    hasError = true;
    errEx = ex;
    errQuery = sqlQuery;
    // Now, put the error messages as values
    ArrayList dummyRow = new ArrayList();

    columns.clear();
    values.clear();
    addColumn("!!! ERREUR !!! (REQUETE)");
    dummyRow.add(sqlQuery);
    sb.append("!!! ERREUR !!!\nREQUETE :\n").append(sqlQuery);
    addColumn("!!! ERREUR !!! (MSG HAUT NIVEAU)");
    sb.append("\nMSG HAUT NIVEAU :\n");
    if (ex instanceof SilverpeasException) {
      boolean notTheSame = false;

      errLowestLevel = ((SilverpeasException) ex).getMessageLang();
      dummyRow.add(errLowestLevel);
      sb.append(errLowestLevel);
      nested = ex;
      while ((nested instanceof WithNested) && (((WithNested) nested).getNested() != null)) {
        nested = ((WithNested) nested).getNested();
        notTheSame = true;
      }
      if (notTheSame) {
        addColumn("!!! ERREUR !!! (MSG BAS NIVEAU)");
        if (nested instanceof SilverpeasException) {
          errLowestLevel = ((SilverpeasException) nested).getMessageLang();
        } else {
          errLowestLevel = nested.getMessage();
        }
        dummyRow.add(errLowestLevel);
        sb.append("\nMSG BAS NIVEAU :\n").append(errLowestLevel);
      }
    } else {
      errLowestLevel = ex.getMessage();
      dummyRow.add(errLowestLevel);
      sb.append(errLowestLevel);
    }
    addRow(dummyRow);
    errFullText = sb.toString();
  }

  // Columns functions
  // -----------------
  public void addColumn(String columnName) {
    columns.add(columnName);
  }

  public ArrayList getColumns() {
    return columns;
  }

  // Values functions
  // ----------------
  // Return all values (arraylist of arraylist)
  public ArrayList getValues() {
    return values;
  }

  public long returnTriggerValueFromResult() throws DataWarningException {
    SilverTrace.info("dataWarning", "DataWarningQueryResult.returnTriggerValueFromResult()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      if (isPersoEnabled()) {
        SilverTrace.info("dataWarning", "DataWarningQueryResult.returnTriggerValueFromResult()",
            "root.MSG_GEN_PARAM_VALUE", "persoColumnNumber=" + persoColumnNumber);
        long maxVal = 0;
        long theVal;
        int theCol;

        if ((persoColumnNumber > 0) || (getNbColumns() <= 1)) {
          theCol = 0;
        } else {
          theCol = 1;
        }
        for (int i = 0; i < getNbRows(); i++) {
          theVal = Long.parseLong(getValue(i, theCol));
          if ((i == 0) || queryParent.checkTriggerSatisfied(theVal)) {
            maxVal = theVal;
          }
        }
        SilverTrace.info("dataWarning", "DataWarningQueryResult.returnTriggerValueFromResult(perso)",
            "root.MSG_GEN_PARAM_VALUE", "maxVal = " + maxVal);
        return maxVal;
      } else {
        return Long.parseLong(getValue(0, 0));
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningQueryResult.returnTriggerValueFromResult()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", e);
    }
  }

  public void addRow(ArrayList row) {
    // Add Full row to global result
    values.add(row);

    // Add reduced row to user specific results if needed
    if (isPersoEnabled()) {
      String userPersoValue = (String) row.get(persoColumnNumber);
      ArrayList allUserRows = getValues(userPersoValue);
      ArrayList clonedOne = (ArrayList) row.clone();
      if (allUserRows == null) {
        allUserRows = new ArrayList();
      }
      // Remove User-specific Id
      clonedOne.remove(persoColumnNumber);
      allUserRows.add(clonedOne);
      valuesByUser.put(userPersoValue, allUserRows);
    }
  }

  // Return entire row
  protected ArrayList getRow(int row) {
    if (values.size() > row) {
      return (ArrayList) values.get(row);
    } else {
      return new ArrayList();
    }
  }

  public String getValue(int row, int col) {
    ArrayList al = getRow(row);
    if (al.size() > col) {
      return (String) (al.get(col));
    } else {
      return "";
    }
  }

  public int getNbColumns() {
    return columns.size();
  }

  public int getNbRows() {
    return values.size();
  }

  // Values By User functions
  // ------------------------
  public boolean isPersoEnabled() {
    return (valuesByUser != null);
  }

  public long returnTriggerValueFromResult(String userId) throws DataWarningException {
    try {
      if (isPersoEnabled()) {
        SilverTrace.info("dataWarning",
            "DataWarningQueryResult.returnTriggerValueFromResult(userId,persoEnabled)",
            "root.MSG_GEN_PARAM_VALUE", "maxVal = " + getValue(userId, 0, 0));
        return Long.parseLong(getValue(userId, 0, 0));
      } else {
        SilverTrace.info("dataWarning",
            "DataWarningQueryResult.returnTriggerValueFromResult(userId)",
            "root.MSG_GEN_PARAM_VALUE", "maxVal = " + Long.parseLong(getValue(0, 0)));
        return Long.parseLong(getValue(0, 0));
      }
    } catch (Exception e) {
      throw new DataWarningException("DataWarningQueryResult.returnTriggerValueFromResult()",
          SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", "UserId = " + userId, e);
    }
  }

  public ArrayList getValues(String userId) {
    if (isPersoEnabled()) {
      // Translate user Id to user Perso Value
      String userPersoValue = returnPersoValue(userId);
      SilverTrace.debug("dataWarning", "DataWarningQueryResult.getValues(userId)",
          "root.MSG_GEN_PARAM_VALUE", "userPersoValue = " + userPersoValue);
      ArrayList valret = new ArrayList();
      for (int i = 0; i < values.size(); i++) {
        ArrayList valeur = (ArrayList) getValues().get(i);
        SilverTrace.debug("dataWarning", "DataWarningQueryResult.getValues(userId)",
            "root.MSG_GEN_PARAM_VALUE", "valeur = " + valeur);
        SilverTrace.debug("dataWarning", "DataWarningQueryResult.getValues(userId)",
            "root.MSG_GEN_PARAM_VALUE", "valeur.size = " + valeur.size());
        SilverTrace.debug("dataWarning", "DataWarningQueryResult.getValues(userId)",
            "root.MSG_GEN_PARAM_VALUE", "persoColumnNumber = " + persoColumnNumber);
        SilverTrace.debug("dataWarning", "DataWarningQueryResult.getValues(userId)",
            "root.MSG_GEN_PARAM_VALUE", "userPersoValue = " + userPersoValue);
        if (valeur.size() > 1) {
          if (valeur.get(persoColumnNumber).equals(userPersoValue)) {
            valret.add(valeur);
          }
        }
      }

      if (valret == null) {
        valret = new ArrayList();
      }
      SilverTrace.info("dataWarning", "DataWarningQueryResult.getValues(userId)",
          "root.MSG_GEN_PARAM_VALUE", "Taille valret = " + valret.size());
      return valret;
    } else {
      return getValues();
    }
  }

  protected ArrayList getRow(String userId, int row) {
    if (isPersoEnabled()) {
      ArrayList al = getValues(userId);
      SilverTrace.info("dataWarning", "DataWarningQueryResult.getRow(userId, row)",
          "root.MSG_GEN_PARAM_VALUE", "userId=" + userId + " row=" + row + " al=" + al);
      if (al.size() > row) {
        return (ArrayList) al.get(row);
      } else {
        return new ArrayList();
      }
    } else {
      if (values.size() > row) {
        return (ArrayList) values.get(row);
      } else {
        return new ArrayList();
      }
    }
  }

  public String getValue(String userId, int row, int col) {

    SilverTrace.info("dataWarning", "DataWarningQueryResult.getValue(userId, row, col)",
        "root.MSG_GEN_PARAM_VALUE", "UserId=" + userId + " Row=" + row + " Col=" + col);
    ArrayList al = getRow(userId, row);
    SilverTrace.info("dataWarning", "DataWarningQueryResult.getValue(userId, row, col)",
        "root.MSG_GEN_PARAM_VALUE", "al=" + al);
    if (al.size() > col) {
      return (String) (al.get(col));
    } else {
      return "";
    }
  }

  public ArrayList getColumns(String userId) {
    if (isPersoEnabled()) {
      ArrayList clo = ((ArrayList) columns.clone());
      if (clo.size() > persoColumnNumber) {
        clo.remove(persoColumnNumber);
      }
      return clo;
    } else {
      return columns;
    }
  }

  public int getNbColumns(String userId) {
    if (isPersoEnabled()) {
      return columns.size() - 1;
    } else {
      return columns.size();
    }
  }

  public int getNbRows(String userId) {
    ArrayList allUserRows = getValues(userId);
    if (allUserRows != null) {
      return allUserRows.size();
    } else {
      return 0;
    }
  }

  public int getPersoColumnNumber() {
    return persoColumnNumber;
  }

  public String returnPersoValue(String userId) {
    try {
      UserDetail ud = oc.getUserDetail(userId);

      if (ud == null) {
        return "";
      }
      if (DataWarningQuery.QUERY_PERSO_UID_ID.equals(persoUID)) {
        return ud.getId();
      } else if (DataWarningQuery.QUERY_PERSO_UID_LOGIN.equals(persoUID)) {
        return ud.getLogin();
      } else if (DataWarningQuery.QUERY_PERSO_UID_LASTNAME.equals(persoUID)) {
        return ud.getLastName();
      } else if (DataWarningQuery.QUERY_PERSO_UID_SPECIFICID.equals(persoUID)) {
        return ud.getSpecificId();
      } else if (DataWarningQuery.QUERY_PERSO_UID_EMAIL.equals(persoUID)) {
        return ud.geteMail();
      } else {
        UserFull uf = oc.getUserFull(userId);
        return uf.getValue(persoUID, "");
      }
    } catch (Exception e) {
      SilverTrace.warn("dataWarning", "DataWarningQueryResult.returnPersoValue()",
          "root.MSG_GEN_ENTER_METHOD", "UserId=" + userId, e);
      return "";
    }
  }
}
