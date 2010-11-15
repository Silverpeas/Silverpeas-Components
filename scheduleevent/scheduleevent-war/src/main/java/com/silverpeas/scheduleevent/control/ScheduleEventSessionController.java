/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.scheduleevent.control;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.silverpeas.scheduleevent.service.ServicesFactory;
import com.silverpeas.scheduleevent.service.model.ScheduleEventStatus;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.ContributorComparator;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.DateOptionsComparator;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEventComparator;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class ScheduleEventSessionController extends AbstractComponentSessionController {
  private Selection sel = null;

  private ScheduleEvent currentScheduleEvent = null;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ScheduleEventSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle",
        "com.silverpeas.components.scheduleevent.settings.ScheduleEventIcons");
    sel = getSelection();
  }

  public void setCurrentScheduleEvent(ScheduleEvent currentScheduleEvent) {
    this.currentScheduleEvent = currentScheduleEvent;
  }

  public ScheduleEvent getCurrentScheduleEvent() {
    return currentScheduleEvent;
  }

  public void setUsers(String[] idUsers, String[] idGroups) {

  }

  public String initSelectUsersPanel() {
    SilverTrace.debug("ScheduleEvent",
        "ScheduleEventSessionController.initSelectUsersPanel()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER METHOD");

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    String hostUrl = m_context
        + URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT) + "ConfirmUsers?popupMode=Yes";
    String cancelUrl = m_context
        + URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT) + "ConfirmScreen?popupMode=Yes";
    PairObject hostComponentName = new PairObject(getComponentName(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("scheduleevent.form.selectContributors"), "");

    sel.resetAll();
    sel.setHostSpaceName(this.getString("domainName"));
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    String[] idUsers = getContributorsUserIds(currentScheduleEvent.getContributors());
    sel.setSelectedElements(idUsers);
    sel.setSelectedSets(new String[0]);
    // Contraintes
    sel.setMultiSelect(true);
    sel.setPopupMode(true);
    if ((idUsers == null) || (idUsers.length == 0)) {
      sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);
    } else {
      sel.setFirstPage(Selection.FIRST_PAGE_CART);
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private static String[] getContributorsUserIds(
      Set<Contributor> contributors) {
    Set<String> result = new HashSet<String>(contributors.size());
    for (Contributor subscriber : contributors) {
      if (subscriber.getUserId() != -1) {
        result.add(String.valueOf(subscriber.getUserId()));
      }
    }
    return (String[]) result.toArray(new String[result.size()]);
  }

  public void setIdUsersAndGroups() {
    String[] usersId =
        SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.getSelectedSets());

    SortedSet<Contributor> contributors = new TreeSet<Contributor>(new ContributorComparator());
    if (usersId.length > 0) {
      UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(usersId);
      for (int i = 0; i < userDetails.length; i++) {
        Contributor contributor = new Contributor();
        contributor.setScheduleEvent(currentScheduleEvent);
        UserDetail detail = userDetails[i];
        contributor.setUserId(Integer.parseInt(detail.getId()));
        contributor.setUserName(detail.getDisplayedName());
        contributors.add(contributor);
      }
    }
    currentScheduleEvent.setContributors(contributors);
  }

  public void save() {

    // add last info for a complete save
    currentScheduleEvent.setAuthor(Integer.parseInt(getUserId()));
    currentScheduleEvent.setStatus(ScheduleEventStatus.OPEN);
    currentScheduleEvent.setCreationDate(new Date());

    // create all dateoption for database
    preTreatementForDateOption();

    ServicesFactory.getScheduleEventService().createScheduleEvent(currentScheduleEvent);
    // delete session object after saving it
    currentScheduleEvent = null;

  }

  private void preTreatementForDateOption() {
    SortedSet<DateOption> dates = currentScheduleEvent.getDates();
    DateOption[] dateoptions = dates.toArray(new DateOption[dates.size()]);
    for (int i = 0; i < dateoptions.length; i++) {
      DateOption current = dateoptions[i];
      if (current.getHour() > 24) {
        // case all the day -> AM + PM
        dates.remove(current);
        DateOption newAM = new DateOption();
        newAM.setDay(current.getDay());
        newAM.setHour(8);
        dates.add(newAM);
        DateOption newPM = new DateOption();
        newPM.setDay(current.getDay());
        newPM.setHour(14);
        dates.add(newPM);
      }
    }
    currentScheduleEvent.setDates(dates);
  }

  public SortedSet<ScheduleEvent> getScheduleEventsByUserId() {
    Set<ScheduleEvent> allEvents = new HashSet<ScheduleEvent>();
    allEvents =
        ServicesFactory.getScheduleEventService().listAllScheduleEventsByUserId(getUserId());
    SortedSet<ScheduleEvent> eventsSorted =
        new TreeSet<ScheduleEvent>(new ScheduleEventComparator());
    eventsSorted.addAll(allEvents);
    return eventsSorted;
  }

  public ScheduleEvent getDetail(String id) {
    // update last visited date
    ServicesFactory.getScheduleEventService().setLastVisited(id, Integer.valueOf(getUserId()));
    // return detail for page
    return ServicesFactory.getScheduleEventService().findScheduleEvent(id);
  }

  public void modifyState(String id) {

    ScheduleEvent event = ServicesFactory.getScheduleEventService().findScheduleEvent(id);
    int actualStatus = event.getStatus();
    int newStatus = ScheduleEventStatus.OPEN;
    if (ScheduleEventStatus.OPEN == actualStatus) {
      newStatus = ScheduleEventStatus.CLOSED;
    }
    ServicesFactory.getScheduleEventService().updateScheduleEventStatus(id, newStatus);
  }

  public void delete(String scheduleEventId) {
    ServicesFactory.getScheduleEventService().deleteScheduleEvent(scheduleEventId);
  }

  public void update(ScheduleEvent scheduleEvent) {
    ServicesFactory.getScheduleEventService().updateScheduleEvent(scheduleEvent);
  }

  public ScheduleEvent purgeOldResponseForUserId(ScheduleEvent scheduleEvent) {
    return ServicesFactory.getScheduleEventService().purgeOldResponseForUserId(scheduleEvent,
        Integer.parseInt(getUserId()));
  }

}