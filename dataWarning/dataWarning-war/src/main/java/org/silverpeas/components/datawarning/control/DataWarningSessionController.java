/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.datawarning.control;

import org.silverpeas.components.datawarning.DataWarningDBDriver;
import org.silverpeas.components.datawarning.DataWarningDBDrivers;
import org.silverpeas.components.datawarning.DataWarningException;
import org.silverpeas.components.datawarning.model.DataWarning;
import org.silverpeas.components.datawarning.model.DataWarningGroup;
import org.silverpeas.components.datawarning.model.DataWarningQuery;
import org.silverpeas.components.datawarning.model.DataWarningScheduler;
import org.silverpeas.components.datawarning.model.DataWarningUser;
import org.silverpeas.components.datawarning.service.DataWarningEngine;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class DataWarningSessionController extends AbstractComponentSessionController {

  private static final String FREQUENCY_SCHEDULER_REQUEST_KEY = "frequenceSchedulerRequete";
  private static final String HOUR_KEY = "heure";
  private static final String MINUTE_KEY = "minute";
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
   *
   */
  public DataWarningSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.dataWarning.multilang.dataWarning",
        "org.silverpeas.dataWarning.settings.dataWarningIcons");
    try {
      dataWarningEngine = new DataWarningEngine(getComponentId());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
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

  public void setCurrentDBDriver(String dbDriverUniqueId) {
    currentDBDriver = dbDriverUniqueId;
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
    if (groups != null && !groups.isEmpty()) {
      idGroups = new String[groups.size()];
      DataWarningGroup[] dataWarningGroups = groups.toArray(new DataWarningGroup[groups.size()]);
      for (int i = 0; i < groups.size(); i++) {
        idGroups[i] = String.valueOf(dataWarningGroups[i].getGroupId());
      }
    }
    if (users != null && !users.isEmpty()) {
      idUsers = new String[users.size()];
      DataWarningUser[] dataWarningUsers = users.toArray(new DataWarningUser[users.size()]);
      for (int i = 0; i < users.size(); i++) {
        idUsers[i] = String.valueOf(dataWarningUsers[i].getUserId());
      }
    }
    try {
      String curContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
      String hostUrl =
          curContext + URLUtil.getURL(getSpaceId(), getComponentId()) + "SaveNotification";
      String cancelUrl =
          curContext + URLUtil.getURL(getSpaceId(), getComponentId()) + "schedulerParameters";
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

      retour = Selection.getSelectionURL();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
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
          append(WebEncodeHelper.javaStringToHtmlString(selectText)).append("</option>\n");
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
            append(WebEncodeHelper.
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
          if (userIds != null && userIds.length > 0 && isUserIn(userIds)) {
            return true;
          }
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return false;
  }

  private boolean isUserIn(final String[] userIds) {
    for (final String userId : userIds) {
      if (userId.equals(getUserId())) {
        return true;
      }
    }
    return false;
  }

  public String getTextFrequenceScheduler() {
    String retour = "";
    DataWarningScheduler scheduler = dataWarningEngine.getDataWarningScheduler();
    LocalizationBundle messages =
        ResourceLocator.getLocalizationBundle("org.silverpeas.dataWarning.multilang.dataWarning");
    if (scheduler != null) {
      if (scheduler.getNumberOfTimes() == 1) {
        retour = setTextFrequencyFromTimesMoment(retour, scheduler, messages);
      } else {
        retour += messages.getString(FREQUENCY_SCHEDULER_REQUEST_KEY) + " " + scheduler.
            getNumberOfTimes() + " " + messages.getString("schedulerPeriodicity5") + " ";
        if (scheduler.getNumberOfTimesMoment() ==
            DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR) {
          retour += messages.getString(HOUR_KEY);
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

  private String setTextFrequencyFromTimesMoment(String retour,
      final DataWarningScheduler scheduler, final LocalizationBundle messages) {
    switch (scheduler.getNumberOfTimesMoment()) {
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR:
        retour = nTimesMomentHour(retour, scheduler, messages);
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_DAY:
        retour = nTimesMomentDay(retour, scheduler, messages);
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_WEEK:
        retour = nTimesMomentWeek(retour, scheduler, messages);
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_MONTH:
        retour = nTimesMomentMonth(retour, scheduler, messages);
        break;
      case DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_YEAR:
        retour = nTimesMomentYear(retour, scheduler, messages);
        break;
      default:
        break;
    }
    return retour;
  }

  private String nTimesMomentYear(String retour, final DataWarningScheduler scheduler,
      final LocalizationBundle messages) {
    retour += messages.getString(FREQUENCY_SCHEDULER_REQUEST_KEY) + " " +
        messages.getString("schedulerPeriodicity4") + " (" + messages.getString("mois") + " : " +
        (scheduler.
            getTheMonth() + 1) + ", " + messages.getString("jourMois") + " : " + (scheduler.
        getDayOfMonth() + 1) + ", " + messages.getString(HOUR_KEY) + " : " + scheduler.
        getHours() + ", " + messages.getString(MINUTE_KEY) + " : " + scheduler.getMinits() + ")";
    return retour;
  }

  private String nTimesMomentMonth(String retour, final DataWarningScheduler scheduler,
      final LocalizationBundle messages) {
    retour += messages.getString(FREQUENCY_SCHEDULER_REQUEST_KEY) + " " +
        messages.getString("schedulerPeriodicity3") + " (" + messages.getString("jourMois") +
        " : " + (scheduler.getDayOfMonth() + 1) + ", " + messages.getString(HOUR_KEY) + " : " +
        scheduler.getHours() + ", " + messages.getString(MINUTE_KEY) + " : " + scheduler.
        getMinits() + ")";
    return retour;
  }

  private String nTimesMomentWeek(String retour, final DataWarningScheduler scheduler,
      final LocalizationBundle messages) {
    retour += messages.getString(FREQUENCY_SCHEDULER_REQUEST_KEY) + " " +
        messages.getString("schedulerPeriodicity2") + " (" + messages.getString("jourSemaine") +
        " : " + scheduler.getDayOfWeek() + ", " + messages.getString(HOUR_KEY) + " : " + scheduler.
        getHours() + ", " + messages.getString(MINUTE_KEY) + " : " + scheduler.getMinits() + ")";
    return retour;
  }

  private String nTimesMomentDay(String retour, final DataWarningScheduler scheduler,
      final LocalizationBundle messages) {
    retour += messages.getString(FREQUENCY_SCHEDULER_REQUEST_KEY) + " " +
        messages.getString("schedulerPeriodicity1") + " (" + messages.getString(HOUR_KEY) + " : " +
        scheduler.
            getHours() + ", " + messages.getString(MINUTE_KEY) + " : " + scheduler.getMinits() +
        ")";
    return retour;
  }

  private String nTimesMomentHour(String retour, final DataWarningScheduler scheduler,
      final LocalizationBundle messages) {
    retour += messages.getString(FREQUENCY_SCHEDULER_REQUEST_KEY) + " " +
        messages.getString("schedulerPeriodicity0") + " (" + messages.getString(MINUTE_KEY) +
        " : " + scheduler.
        getMinits() + ")";
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

  public String getAnalysisTypeString() {
    return getString("typeAnalyse" + Integer.toString(dataWarningEngine.getDataWarning().
        getAnalysisType()));
  }

  public DataWarningEngine getDataWarningEngine() {
    return dataWarningEngine;
  }

  public DataWarningQuery setCurrentQueryType(int qt) {
    queryType = qt;
    return getCurrentQuery();
  }

  public DataWarningQuery getCurrentQuery() {
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
