/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.dataWarning.control;

import com.silverpeas.dataWarning.DataWarningDBDriver;
import com.silverpeas.dataWarning.DataWarningDBDrivers;
import com.silverpeas.dataWarning.DataWarningException;
import com.silverpeas.dataWarning.model.*;
import org.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.peasCore.*;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.*;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.GeneralPropertiesManager;
import org.silverpeas.util.Pair;
import org.silverpeas.util.ResourceLocator;

import java.util.*;

public class DataWarningSessionController extends AbstractComponentSessionController {

  /**
   * Interface metier du composant
   */
  private DataWarningEngine dataWarningEngine = null;
  private int queryType = DataWarningQuery.QUERY_TYPE_RESULT;
  private DataWarningScheduler editedScheduler = null;
  private DataWarning editedDataWarning = null;
  private DataWarningDBDrivers dataWarningDBDrivers = null;
  private String currentDBDriver = "";
  private Selection sel;
  private String[] columns;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public DataWarningSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "com.silverpeas.dataWarning.multilang.dataWarning",
        "com.silverpeas.dataWarning.settings.dataWarningIcons");
    try {
      dataWarningEngine = new DataWarningEngine(getComponentId());
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarningSessionController.DataWarningSessionController",
          "DataWarning.EX_DATA_ACCESS_FAILED", null, e);
    }
    sel = getSelection();
  }

  public DataWarningDBDrivers getDataWarningDBDrivers() {
    if (dataWarningDBDrivers == null) {
      dataWarningDBDrivers = new DataWarningDBDrivers();
    }
    return dataWarningDBDrivers;

  }

  public DataWarningDBDriver[] getDBDrivers() {
    return getDataWarningDBDrivers().getDBDrivers();
  }

  public void setCurrentDBDriver(String DBDriverUniqueId) {
    currentDBDriver = DBDriverUniqueId;
  }

  public DataWarningDBDriver getCurrentDBDriver() {
    return getDataWarningDBDrivers().getDBDriver(currentDBDriver);
  }

  public String initSelectionPeas() throws DataWarningException {
    String retour = null;
    //get usersId and groupsId
    Collection<DataWarningGroup> groups = dataWarningEngine.getDataWarningGroups();
    Collection<DataWarningUser> users = dataWarningEngine.getDataWarningUsers();
    //transforme la collection en tableau de string
    String[] idGroups = null;
    String[] idUsers = null;
    if (groups != null && groups.size() > 0) {
      idGroups = new String[groups.size()];
      DataWarningGroup[] Groups = groups.toArray(new DataWarningGroup[groups.size()]);
      for (int i = 0; i < groups.size(); i++) {
        idGroups[i] = String.valueOf(Groups[i].getGroupId());
      }
    }
    if (users != null && users.size() > 0) {
      idUsers = new String[users.size()];
      DataWarningUser[] Users = users.toArray(new DataWarningUser[users.size()]);
      for (int i = 0; i < users.size(); i++) {
        idUsers[i] = String.valueOf(Users[i].getUserId());
      }
    }
    try {
      String curContext =
          GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
      String hostUrl =
          curContext + URLManager.getURL(getSpaceId(), getComponentId()) + "SaveNotification";
      String cancelUrl =
          curContext + URLManager.getURL(getSpaceId(), getComponentId()) + "schedulerParameters";
      Pair<String, String> hostComponentName = new Pair<>("", "");
      String hostSpaceName = getSpaceLabel();

      sel.resetAll();
      sel.setHostSpaceName(hostSpaceName);
      sel.setHostComponentName(hostComponentName);
      sel.setHostPath(null);
      sel.setGoBackURL(hostUrl);
      sel.setCancelURL(cancelUrl);
      sel.setSelectedElements(idUsers);
      sel.setSelectedSets(idGroups);
      sel.setMultiSelect(true);
      sel.setPopupMode(true);

      if ((idUsers == null || idUsers.length == 0) && (idGroups == null || idGroups.length == 0)) {
        //Display welcome page if user has no selection
        sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
      } else {
        //Display basket if user has already done selection
        sel.setFirstPage(Selection.FIRST_PAGE_CART);
      }
      retour = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
    } catch (Exception e) {
      SilverTrace.error("dataWarning", "DataWarningSessionController.initSelectionPeas",
          "DataWarning.MSG_CANT_INIT_SELECTIONPEAS", null, e);
    }
    return retour;
  }

  public void returnSelectionPeas() throws DataWarningException {
    //get groups and users selected
    String[] idGroups = sel.getSelectedSets();
    String[] idUsers = sel.getSelectedElements();
    //save groups and users selected
    dataWarningEngine.deleteDataWarningGroups();
    if (idGroups != null) {
      for (final String idGroup : idGroups) {
        dataWarningEngine.createDataWarningGroup(new DataWarningGroup(getComponentId(), Integer.
            parseInt(idGroup)));
      }
    }
    dataWarningEngine.deleteDataWarningUsers();
    if (idUsers != null) {
      for (final String idUser : idUsers) {
        dataWarningEngine.createDataWarningUser(new DataWarningUser(getComponentId(), Integer.
            parseInt(idUser)));
      }
    }
  }

  public String buildOptions(List ar, String selectValue, String selectText, boolean bSorted) {
    StringBuilder valret = new StringBuilder();
    Properties elmt;
    String selected;
    List arToDisplay = ar;
    int i;

    if (selectText != null) {
      if (selectValue == null || selectValue.length() <= 0) {
        selected = "SELECTED";
      } else {
        selected = "";
      }
      valret.append("<option value=\"\" ").append(selected).append(">").
          append(EncodeHelper.javaStringToHtmlString(selectText)).append("</option>\n");
    }
    if (bSorted) {
      Properties[] theList = (Properties[]) ar.toArray(new Properties[0]);
      Arrays.sort(theList, new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
          return (((Properties) o1).getProperty("name")).toUpperCase().compareTo(((Properties) o2).
              getProperty("name").toUpperCase());
        }
      });
      arToDisplay = new ArrayList(theList.length);
      for (i = 0; i < theList.length; i++) {
        arToDisplay.add(theList[i]);
      }
    }
    if (arToDisplay != null) {
      for (i = 0; i < arToDisplay.size(); i++) {
        elmt = (Properties) arToDisplay.get(i);
        if (elmt.getProperty("id").equalsIgnoreCase(selectValue)) {
          selected = "SELECTED";
        } else {
          selected = "";
        }
        valret.append("<option value=\"").append(elmt.getProperty("id")).append("\" ").
            append(selected).append(">").
            append(EncodeHelper.
                javaStringToHtmlString(elmt.getProperty("name"))).append("</option>\n");
      }
    }
    return valret.toString();
  }

  public List<Properties> getSelectedGroups(String[] selectedGroupsId) throws DataWarningException {
    Group[] selectedGroups;
    Properties p;
    int i;
    List<Properties> ar = new ArrayList<>();

    if (selectedGroupsId != null && selectedGroupsId.length > 0) {
      selectedGroups = getGroupList(selectedGroupsId);

      if (selectedGroups != null && selectedGroups.length > 0) {
        for (i = 0; i < selectedGroups.length; i++) {
          p = new Properties();
          p.setProperty("id", selectedGroups[i].getId());
          p.setProperty("name", selectedGroups[i].getName());
          ar.add(p);
        }
        if (ar.size() != selectedGroups.length) {
          throw new DataWarningException("DataWarningSessionController.getSelectedGroups()",
              SilverpeasException.WARNING, "notificationUser.EX_CANT_GET_SELECTED_GROUPS_INFOS");
        }
      }
    }
    return ar;
  }

  private Group[] getGroupList(String[] idGroups) {
    Group[] setOfGroup = null;
    if (idGroups != null && idGroups.length > 0) {
      setOfGroup = new Group[idGroups.length];
      for (int i = 0; i < idGroups.length; i++) {
        setOfGroup[i] = getOrganisationController().getGroup(idGroups[i]);
      }
    }
    return setOfGroup;
  }

  private UserDetail[] getUserDetailList(String[] idUsers) {
    return getOrganisationController().getUserDetails(idUsers);
  }

  public List<Properties> getSelectedUsersNames(String[] selectedUsersId)
      throws DataWarningException {
    List<Properties> ar = new ArrayList<>();
    UserDetail[] selectedUsers;

    if (selectedUsersId != null && selectedUsersId.length > 0) {
      selectedUsers = getUserDetailList(selectedUsersId);
      if (selectedUsers != null && selectedUsers.length > 0) {
        for (final UserDetail selectedUser : selectedUsers) {
          Properties p = new Properties();
          p.setProperty("id", selectedUser.getId());
          p.setProperty("name", selectedUser.getLastName() + " " + selectedUser.getFirstName());
          ar.add(p);
        }
        if (ar.size() != selectedUsers.length) {
          throw new DataWarningException("DataWarningSessionController.getSelectedUsersNames()",
              SilverpeasException.WARNING, "notificationUser.EX_CANT_GET_SELECTED_USERS_INFOS");
        }
      }
    }
    return ar;
  }

  public List<Properties> getSelectedGroupsNames(String[] selectedGroupsId)
      throws DataWarningException {
    Group[] selectedGroups;
    ArrayList<Properties> ar = new ArrayList<>();

    if (selectedGroupsId != null && selectedGroupsId.length > 0) {
      selectedGroups = getGroupList(selectedGroupsId);
      if (selectedGroups != null && selectedGroups.length > 0) {
        for (final Group selectedGroup : selectedGroups) {
          Properties p = new Properties();
          p.setProperty("id", selectedGroup.getId());
          p.setProperty("name", selectedGroup.getName());
          ar.add(p);
        }
        if (ar.size() != selectedGroups.length) {
          throw new DataWarningException("DataWarningSessionController.getSelectedGroups()",
              SilverpeasException.WARNING, "notificationUser.EX_CANT_GET_SELECTED_GROUPS_INFOS");
        }
      }
    }
    return ar;
  }

  public boolean isUserInDataWarningGroups() throws DataWarningException {
    Collection<DataWarningGroup> dataGroups = dataWarningEngine.getDataWarningGroups();
    if (dataGroups != null) {
      try {
        List<String> groupIds = new ArrayList<>();
        for (DataWarningGroup dataGroup : dataGroups) {
          groupIds.add(Integer.toString(dataGroup.getGroupId()));
        }
        Group[] groups = getOrganisationController()
            .getGroups(groupIds.toArray(new String[groupIds.size()]));
        for (Group group : groups) {
          String[] userIds = group.getUserIds();
          if (userIds != null && userIds.length > 0) {
            for (final String userId : userIds) {
              if (userId.equals(getUserId())) {
                return true;
              }
            }
          }
        }
      } catch (Exception e) {
        SilverTrace.error("dataWarning", "DataWarningSessionController.isUserInDataWarningGroups",
            "DataWarning.DataWarning.EX_LOAD_GROUPS", null, e);
      }
    }
    return false;
  }

  public String getTextFrequenceScheduler() throws DataWarningException {
    String retour = "";
    DataWarningScheduler scheduler = dataWarningEngine.getDataWarningScheduler();
    ResourceLocator messages =
        new ResourceLocator("org.silverpeas.dataWarning.multilang.dataWarning", "");
    if (scheduler != null) {
      if (scheduler.getNumberOfTimes() == 1) {
        switch (scheduler.getNumberOfTimesMoment()) {
          case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR:
            retour += messages.getString("frequenceSchedulerRequete") + " " +
                messages.getString("schedulerPeriodicity0") + " (" + messages.getString("minute") +
                " : " + scheduler.
                getMinits() + ")";
            break;
          case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY:
            retour += messages.getString("frequenceSchedulerRequete") + " " +
                messages.getString("schedulerPeriodicity1") + " (" + messages.getString("heure") +
                " : " + scheduler.
                getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() +
                ")";
            break;
          case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK:
            retour += messages.getString("frequenceSchedulerRequete") + " " +
                messages.getString("schedulerPeriodicity2") + " (" +
                messages.getString("jourSemaine") + " : " + scheduler.getDayOfWeek() + ", " +
                messages.getString("heure") + " : " + scheduler.
                getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() +
                ")";
            break;
          case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH:
            retour += messages.getString("frequenceSchedulerRequete") + " " +
                messages.getString("schedulerPeriodicity3") + " (" +
                messages.getString("jourMois") + " : " + (scheduler.getDayOfMonth() + 1) + ", " +
                messages.getString("heure") + " : " + scheduler.getHours() + ", " +
                messages.getString("minute") + " : " + scheduler.
                getMinits() + ")";
            break;
          case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR:
            retour += messages.getString("frequenceSchedulerRequete") + " " +
                messages.getString("schedulerPeriodicity4") + " (" + messages.getString("mois") +
                " : " + (scheduler.
                getTheMonth() + 1) + ", " + messages.getString("jourMois") + " : " + (scheduler.
                getDayOfMonth() + 1) + ", " + messages.getString("heure") + " : " + scheduler.
                getHours() + ", " + messages.getString("minute") + " : " + scheduler.getMinits() +
                ")";
            break;
        }
      } else {
        retour += messages.getString("frequenceSchedulerRequete") + " " + scheduler.
            getNumberOfTimes() + " " + messages.getString("schedulerPeriodicity5") + " ";
        if (scheduler.getNumberOfTimesMoment() ==
            DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR) {
          retour += messages.getString("heure");
        } else if (scheduler.getNumberOfTimesMoment() ==
            DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY) {
          retour += messages.getString("jour");
        } else if (scheduler.getNumberOfTimesMoment() ==
            DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK) {
          retour += messages.getString("semaine");
        } else if (scheduler.getNumberOfTimesMoment() ==
            DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH) {
          retour += messages.getString("mois");
        } else if (scheduler.getNumberOfTimesMoment() ==
            DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR) {
          retour += messages.getString("annee");
        }
      }
    }
    return retour;
  }

  public String[] getColumns() {
    return columns != null ? columns.clone() : null;
  }

  public void setColumns(String[] col) {
    if (col != null) {
      columns = col.clone();
    } else {
      columns = null;
    }
  }

  public String getAnalysisTypeString() throws DataWarningException {
    return getString("typeAnalyse" + Integer.toString(dataWarningEngine.getDataWarning().
        getAnalysisType()));
  }

  public DataWarningEngine getDataWarningEngine() throws DataWarningException {
    return dataWarningEngine;
  }

  public DataWarningQuery setCurrentQueryType(int qt) throws DataWarningException {
    queryType = qt;
    return getCurrentQuery();
  }

  public DataWarningQuery getCurrentQuery() throws DataWarningException {
    return dataWarningEngine.getDataWarningQuery(queryType);
  }

  public int getCurrentQueryType() {
    return queryType;
  }

  public DataWarningScheduler resetEditedScheduler() {
    editedScheduler = (DataWarningScheduler) dataWarningEngine.getDataWarningScheduler().clone();
    return editedScheduler;
  }

  public DataWarningScheduler getEditedScheduler() {
    if (editedScheduler == null) {
      return resetEditedScheduler();
    } else {
      return editedScheduler;
    }
  }

  public DataWarning resetEditedDataWarning() {
    editedDataWarning = (DataWarning) dataWarningEngine.getDataWarning().clone();
    return editedDataWarning;
  }

  public DataWarning getEditedDataWarning() {
    if (editedDataWarning == null) {
      return resetEditedDataWarning();
    } else {
      return editedDataWarning;
    }
  }
}
