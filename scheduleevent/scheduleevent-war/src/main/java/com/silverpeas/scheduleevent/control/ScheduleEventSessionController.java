/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
package com.silverpeas.scheduleevent.control;

import static com.silverpeas.export.ExportDescriptor.withWriter;
import com.silverpeas.export.ExportException;
import com.silverpeas.export.Exporter;
import com.silverpeas.export.ExporterFactory;
import com.silverpeas.export.ical.ExportableCalendar;
import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.scheduleevent.notification.ScheduleEventUserNotification;
import com.silverpeas.scheduleevent.service.CalendarEventEncoder;
import com.silverpeas.scheduleevent.service.ScheduleEventService;
import com.silverpeas.scheduleevent.service.ServicesFactory;
import com.silverpeas.scheduleevent.service.model.ScheduleEventBean;
import com.silverpeas.scheduleevent.service.model.ScheduleEventStatus;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEventComparator;
import com.silverpeas.scheduleevent.view.BestTimeVO;
import com.silverpeas.scheduleevent.view.DateVO;
import com.silverpeas.scheduleevent.view.HalfDayDateVO;
import com.silverpeas.scheduleevent.view.HalfDayTime;
import com.silverpeas.scheduleevent.view.OptionDateVO;
import com.silverpeas.scheduleevent.view.ScheduleEventDetailVO;
import com.silverpeas.scheduleevent.view.ScheduleEventVO;
import com.silverpeas.scheduleevent.view.TimeVO;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.silverpeas.calendar.CalendarEvent;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ScheduleEventSessionController extends AbstractComponentSessionController {

  private Selection sel = null;
  private ScheduleEvent currentScheduleEvent = null;
  private static final String ICS_PREFIX = "scheduleevent";

  /**
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public ScheduleEventSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle",
        "com.silverpeas.components.scheduleevent.settings.ScheduleEventIcons",
        "com.silverpeas.components.scheduleevent.settings.ScheduleEventSettings");
    sel = getSelection();
  }

  public void setCurrentScheduleEvent(ScheduleEvent currentScheduleEvent) {
    this.currentScheduleEvent = currentScheduleEvent;
  }

  public ScheduleEvent getCurrentScheduleEvent() {
    return currentScheduleEvent;
  }

  public ScheduleEventBean getCurrentScheduleEventVO() {
    return new ScheduleEventVO(getCurrentScheduleEvent());
  }

  public void resetScheduleEventCreationBuffer() {
    setCurrentScheduleEvent(null);
  }

  private void addContributor(Set<Contributor> contributors, String userId) {
    Contributor contributor = new Contributor();
    contributor.setScheduleEvent(currentScheduleEvent);
    contributor.setUserId(Integer.parseInt(userId));
    contributor.setUserName(getUserDetail(userId).getDisplayedName());
    contributors.add(contributor);
  }

  public void createCurrentScheduleEvent() {
    setCurrentScheduleEvent(new ScheduleEvent());
    currentScheduleEvent.setAuthor(Integer.parseInt(getUserId()));
    addContributor(currentScheduleEvent.getContributors(), getUserId());
  }

  public boolean isCurrentScheduleEventDefined() {
    return getCurrentScheduleEvent() != null;
  }

  public String initSelectUsersPanel() {
    SilverTrace.debug("scheduleevent",
        "ScheduleEventSessionController.initSelectUsersPanel()",
        "root.MSG_GEN_PARAM_VALUE", "ENTER METHOD");

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
    PairObject hostComponentName = new PairObject(getComponentName(), "");
    PairObject[] hostPath = new PairObject[1];
    hostPath[0] = new PairObject(getString("scheduleevent.form.selectContributors"), "");

    sel.resetAll();
    sel.setHostSpaceName(this.getString("domainName"));
    sel.setHostComponentName(hostComponentName);
    sel.setHostPath(hostPath);

    String[] idUsers = getContributorsUserIds(currentScheduleEvent.getContributors());
    sel.setSelectedElements(idUsers);
    sel.setSelectedSets(new String[0]);

    // Contraintes
    String hostDirection, cancelDirection;
    if (currentScheduleEvent.getId() == null) {
      hostDirection = "ConfirmUsers?popupMode=Yes";
      cancelDirection = "ConfirmScreen?popupMode=Yes";
    } else {
      hostDirection = "ConfirmModifyUsers?scheduleEventId=" + currentScheduleEvent.getId();
      cancelDirection = "Detail?scheduleEventId=" + currentScheduleEvent.getId();
    }

    String hostUrl =
        m_context + URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT, null, null) + hostDirection;
    String cancelUrl =
        m_context + URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT, null, null) + cancelDirection;
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);

    sel.setMultiSelect(true);
    sel.setPopupMode(true);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  private static String[] getContributorsUserIds(Set<Contributor> contributors) {
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

    if (usersId.length < 1) {
      return;
    }
    Set<Contributor> recordedContributors = currentScheduleEvent.getContributors();
    deleteRecordedContributors(usersId, recordedContributors);
    addContributors(usersId, recordedContributors);
  }

  public void addContributors(String[] usersId, Set<Contributor> recordedContributors) {
    if (usersId.length < 1) {
      return;
    }
    UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(usersId);
    boolean foundCreator = false;
    for (UserDetail detail : userDetails) {
      if (detail.getId().equals(String.valueOf(currentScheduleEvent.getAuthor()))) {
        foundCreator = true;
      }
      boolean foundAlreadyCreated = false;
      for (Contributor contributor : recordedContributors) {
        if (detail.getId().equals(String.valueOf(contributor.getUserId()))) {
          foundAlreadyCreated = true;
        }
      }
      if (!foundAlreadyCreated) {
        addContributor(recordedContributors, detail.getId());
        SilverTrace.debug("scheduleevent", "ScheduleEventSessionController.addContributors()",
            "Contributor '" + getUserDetail(detail.getId()).getDisplayedName()
            + "' added to event '" + currentScheduleEvent.getTitle() + "'");
      }
    }
    if (!foundCreator) {
      addContributor(recordedContributors, String.valueOf(currentScheduleEvent.getAuthor()));
    }
  }

  private void deleteRecordedContributors(String[] usersId, Set<Contributor> recordedContributors) {
    // if (usersId.length < 1 || recordedContributors.isEmpty()) {
    if (recordedContributors.isEmpty()) {
      return;
    }

    UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(usersId);
    Contributor[] contrib =
        (Contributor[]) recordedContributors.toArray(new Contributor[recordedContributors.size()]);
    boolean found = false;
    for (int c = contrib.length - 1; c >= 0; c--) {
      if (getUserId().equals(String.valueOf(contrib[c].getUserId()))) {
        continue;
      }
      for (int i = 0; i < userDetails.length; i++) {
        if (userDetails[i].getId().equals(String.valueOf(contrib[c].getUserId()))) {
          found = true;
        }
      }
      if (!found) {
        // if (currentScheduleEvent.id == null) {
        // getScheduleEventService().deleteContributor(contrib[c].getId());
        // } else {
        currentScheduleEvent.getContributors().remove(contrib[c]);
        // }
        SilverTrace.debug("scheduleevent", "ScheduleEventSessionController.deleteRecordedContributors()",
            "Contributor '" + contrib[c].getUserName() + "' deleted from event '"
            + currentScheduleEvent.getTitle() + "'");
      }
    }
  }

  public void updateIdUsersAndGroups() {
    String[] usersId =
        SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.getSelectedSets());

    Set<Contributor> recordedContributors = currentScheduleEvent.getContributors();
    deleteRecordedContributors(usersId, recordedContributors);
    addContributors(usersId, recordedContributors);
    getScheduleEventService().updateScheduleEvent(currentScheduleEvent);
  }

  public void save() {
    // add last info for a complete save
    currentScheduleEvent.setAuthor(Integer.parseInt(getUserId()));
    currentScheduleEvent.setStatus(ScheduleEventStatus.OPEN);
    currentScheduleEvent.setCreationDate(new Date());

    // create all dateoption for database
    // preTreatementForDateOption();

    getScheduleEventService().createScheduleEvent(currentScheduleEvent);

    // notify contributors
    // initAlertUser();
    sendSubscriptionsNotification("create");

    // delete session object after saving it
    currentScheduleEvent = null;

  }

  public void sendSubscriptionsNotification(final String type) {
    // Send email alerts
    try {

      UserNotificationHelper
          .buildAndSend(new ScheduleEventUserNotification(currentScheduleEvent, getUserDetail(),
          type));

    } catch (Exception e) {
      SilverTrace.warn("scheduleevent",
          "ScheduleEventSessionController.sendSubscriptionsNotification()",
          "scheduleEvent.EX_IMPOSSIBLE_DALERTER_LES_UTILISATEURS", "", e);
    }
  }

  private ScheduleEventService getScheduleEventService() {
    return ServicesFactory.getFactory().getScheduleEventService();
  }

  public List<ScheduleEvent> getScheduleEventsByUserId() {
    Set<ScheduleEvent> allEvents =
        getScheduleEventService().listAllScheduleEventsByUserId(getUserId());
    List<ScheduleEvent> results = new ArrayList<ScheduleEvent>(allEvents);
    Collections.sort(results, new ScheduleEventComparator());

    return results;
  }

  public ScheduleEvent getDetail(String id) {
    // update last visited date
    getScheduleEventService().setLastVisited(id, Integer.valueOf(getUserId()));
    // return detail for page
    return getScheduleEventService().findScheduleEvent(id);
  }

  public void switchState(String id) {
    ScheduleEvent event = getScheduleEventService().findScheduleEvent(id);
    int actualStatus = event.getStatus();
    int newStatus = ScheduleEventStatus.OPEN;
    if (ScheduleEventStatus.OPEN == actualStatus) {
      newStatus = ScheduleEventStatus.CLOSED;
    }
    getScheduleEventService().updateScheduleEventStatus(id, newStatus);
  }

  public void delete(String scheduleEventId) {
    getScheduleEventService().deleteScheduleEvent(scheduleEventId);
  }

  public void updateUserAvailabilities(ScheduleEvent scheduleEvent) {
    updateValidationDate(scheduleEvent, getUserId());
    getScheduleEventService().updateScheduleEvent(scheduleEvent);
  }

  private void updateValidationDate(ScheduleEvent scheduleEvent, String userId) {
    Contributor contributor = getContributor(scheduleEvent, userId);
    if (contributor != null) {
      contributor.setLastValidation(new Date());
    }
  }

  private Contributor getContributor(ScheduleEvent scheduleEvent, String id) {
    try {
      int userId = Integer.parseInt(id);
      for (Contributor contributor : scheduleEvent.getContributors()) {
        if (contributor.getUserId() == userId) {
          return contributor;
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  public ScheduleEvent purgeOldResponseForUserId(ScheduleEvent scheduleEvent) {
    return getScheduleEventService().purgeOldResponseForUserId(scheduleEvent,
        Integer.parseInt(getUserId()));
  }

  public Double getSubscribersRateAnswerFor(ScheduleEvent event) {
    return 0.0;
  }

  public Set<OptionDateVO> getCurrentOptionalDateIndexes() throws Exception {
    return ((ScheduleEventVO) getCurrentScheduleEventVO()).getOptionalDateIndexes();
  }

  public void setCurrentScheduleEventWith(Set<OptionDateVO> optionalDays) {
    ((ScheduleEventVO) getCurrentScheduleEventVO()).setScheduleEventWith(optionalDays);
  }

  public Response makeReponseFor(ScheduleEvent scheduleEvent, String dateId) {
    // TODO: Can add checks for dateId, scheduleEvent integrity
    Response result = new Response();
    result.setScheduleEvent(scheduleEvent);
    result.setUserId(Integer.parseInt(getUserId()));
    result.setOptionId(dateId);
    return result;
  }
  
  /**
   * Converts the specified detailed scheduleevent into a calendar event.
   *
   * @param scheduleevent detail.
   * @param list of dates.
   * @return the calendar events corresponding to the schedule event.
   */
  private List<CalendarEvent> asCalendarEvents(final ScheduleEvent event, final List<DateOption> listDateOption) {
    CalendarEventEncoder encoder = new CalendarEventEncoder();
    return encoder.encode(event, listDateOption);
  }
  
  /**
   * Exports the current ScheduleEvent in iCal format. The iCal file is generated into the temporary
   * directory.
   *
   * @return the iCal file name into which is generated the current ScheduleEvent.
   * @throws Exception 
   */
  public String exportToICal(ScheduleEvent event) throws Exception {
    
    //construction de la liste des dates retenues de l'événement
    List<DateOption> listDateOption = new ArrayList<DateOption>();
    ScheduleEventDetailVO scheduleEventDetailVO = new ScheduleEventDetailVO(this, event);
    BestTimeVO bestTimeVO = scheduleEventDetailVO.getBestTimes();
    if(bestTimeVO.isBestDateExists()) {
      List<TimeVO> listTimeVO = bestTimeVO.getTimes();
      for(TimeVO timeVO : listTimeVO) {
        HalfDayTime halfDayTime = (HalfDayTime) timeVO;
        DateVO dateVO = halfDayTime.getDate();
        HalfDayDateVO halfDayDateVO = (HalfDayDateVO) dateVO;
        Date day = halfDayDateVO.getDate();
        DateOption dateOption = new DateOption();
        dateOption.setDay(day);
        String label = halfDayTime.getMultilangLabel();
        if("scheduleevent.form.hour.columnam".equals(label)) {
          dateOption.setHour(ScheduleEventVO.MORNING_HOUR);
        } else if ("scheduleevent.form.hour.columnpm".equals(label)) {
          dateOption.setHour(ScheduleEventVO.AFTERNOON_HOUR);
        }
        listDateOption.add(dateOption);
      }
    }
    
    //transformation des dates en CalendarEvent
    List<CalendarEvent> eventsToExport = asCalendarEvents(event, listDateOption);
    
    //export iCal
    ExporterFactory exporterFactory = ExporterFactory.getFactory();
    Exporter<ExportableCalendar> iCalExporter = exporterFactory.getICalExporter();
    String icsFileName = ICS_PREFIX + getUserId() + ".ics";
    String icsFilePath = FileRepositoryManager.getTemporaryPath() + icsFileName;
    FileWriter fileWriter = new FileWriter(icsFilePath);
    try {
      iCalExporter.export(withWriter(fileWriter), ExportableCalendar.with(eventsToExport));
    } catch (ExportException ex) {
      File fileToDelete = new File(icsFilePath);
      if (fileToDelete.exists()) {
        FileUtils.deleteQuietly(fileToDelete);
      }
      throw ex;
    }

    return icsFileName;
  }
}
