/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.components.scheduleevent.control;

import org.apache.commons.io.FileUtils;
import org.silverpeas.components.scheduleevent.notification.ScheduleEventUserCallAgainNotification;
import org.silverpeas.components.scheduleevent.notification.ScheduleEventUserNotification;
import org.silverpeas.components.scheduleevent.service.CalendarEventEncoder;
import org.silverpeas.components.scheduleevent.service.ScheduleEventService;
import org.silverpeas.components.scheduleevent.service.ScheduleEventServiceProvider;
import org.silverpeas.components.scheduleevent.service.model.ScheduleEventBean;
import org.silverpeas.components.scheduleevent.service.model.ScheduleEventStatus;
import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEventComparator;
import org.silverpeas.components.scheduleevent.view.BestTimeVO;
import org.silverpeas.components.scheduleevent.view.DateVO;
import org.silverpeas.components.scheduleevent.view.HalfDayDateVO;
import org.silverpeas.components.scheduleevent.view.HalfDayTime;
import org.silverpeas.components.scheduleevent.view.OptionDateVO;
import org.silverpeas.components.scheduleevent.view.ScheduleEventDetailVO;
import org.silverpeas.components.scheduleevent.view.ScheduleEventVO;
import org.silverpeas.components.scheduleevent.view.TimeVO;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.importexport.ical.ExportableCalendar;
import org.silverpeas.core.importexport.ical.ICalExporterProvider;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.silverpeas.core.importexport.ExportDescriptor.withWriter;

public class ScheduleEventSessionController extends AbstractComponentSessionController {
  private static final long serialVersionUID = -1306206668466915664L;

  private static final String SECURITY_ALERT_FROM_USER_MSG_PREFIX = "Security alert from user ";
  private final transient Selection sel;
  private ScheduleEvent currentScheduleEvent = null;
  private static final String ICS_PREFIX = "scheduleevent";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public ScheduleEventSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.components.scheduleevent.multilang.ScheduleEventBundle",
        "org.silverpeas.components.scheduleevent.settings.ScheduleEventIcons",
        "org.silverpeas.components.scheduleevent.settings.ScheduleEventSettings");
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
    if (isUserOwnerOfEvent(getCurrentScheduleEvent())) {
      String appContext = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
      Pair<String, String> hostComponentName = new Pair<>(getComponentName(), "");
      //noinspection unchecked
      Pair<String, String>[] hostPath = new Pair[1];
      hostPath[0] = new Pair<>(getString("scheduleevent.form.selectContributors"), "");

      sel.resetAll();
      sel.setHostSpaceName("");
      sel.setHostComponentName(hostComponentName);
      sel.setHostPath(hostPath);

      String[] idUsers = getContributorsUserIds(currentScheduleEvent.getContributors());
      sel.setSelectedElements(idUsers);
      sel.setSelectedSets(new String[0]);

      // constraints
      String hostDirection;
      String cancelDirection;
      if (currentScheduleEvent.getId() == null) {
        hostDirection = "ConfirmUsers?popupMode=Yes";
        cancelDirection = "ConfirmScreen?popupMode=Yes";
      } else {
        hostDirection = "ConfirmModifyUsers?scheduleEventId=" + currentScheduleEvent.getId();
        cancelDirection = "Detail?scheduleEventId=" + currentScheduleEvent.getId();
      }
      String hostUrl =
          appContext + URLUtil.getURL(URLUtil.CMP_SCHEDULE_EVENT, null, null) + hostDirection;
      String cancelUrl = appContext + URLUtil.getURL(URLUtil.CMP_SCHEDULE_EVENT, null, null) +
          cancelDirection;
      sel.setGoBackURL(hostUrl);
      sel.setCancelURL(cancelUrl);

      sel.setMultiSelect(true);
      sel.setPopupMode(true);

      return Selection.getSelectionURL();
    } else {
      SilverLogger.getLogger(this).warn(SECURITY_ALERT_FROM_USER_MSG_PREFIX + getUserId());
      return "/admin/jsp/accessForbidden.jsp";
    }
  }

  private static String[] getContributorsUserIds(Set<Contributor> contributors) {
    Set<String> result = new HashSet<>(contributors.size());
    for (Contributor subscriber : contributors) {
      if (subscriber.getUserId() != -1) {
        result.add(String.valueOf(subscriber.getUserId()));
      }
    }
    return result.toArray(new String[0]);
  }

  public void setIdUsersAndGroups() {
    String[] usersId = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.
        getSelectedSets());

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
      boolean foundAlreadyCreated = recordedContributors.stream()
          .anyMatch(c -> detail.getId().equals(String.valueOf(c.getUserId())));
      if (!foundAlreadyCreated) {
        addContributor(recordedContributors, detail.getId());
      }
    }
    if (!foundCreator) {
      addContributor(recordedContributors, String.valueOf(currentScheduleEvent.getAuthor()));
    }
  }

  private void deleteRecordedContributors(String[] selectedUsersIds,
      Set<Contributor> recordedContributors) {
    if (recordedContributors.isEmpty()) {
      return;
    }

    UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(selectedUsersIds);
    Iterator<Contributor> recordedContributorIt = recordedContributors.iterator();
    while (recordedContributorIt.hasNext()) {
      Contributor currentContributor = recordedContributorIt.next();
      String currentContributorUserId = String.valueOf(currentContributor.getUserId());
      if (getUserId().equals(currentContributorUserId)) {
        continue;
      }
      boolean found = Arrays.stream(userDetails)
          .anyMatch(u -> u.getId().equals(currentContributorUserId));
      if (!found) {
        recordedContributorIt.remove();
      }
    }
  }

  public void updateIdUsersAndGroups() {
    String[] usersId = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), sel.
        getSelectedSets());

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
    sendSubscriptionsNotification();

    // delete session object after saving it
    currentScheduleEvent = null;

  }

  public void sendSubscriptionsNotification() {
    try {

      UserNotificationHelper
          .buildAndSend(new ScheduleEventUserNotification(currentScheduleEvent, getUserDetail()));

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public void sendCallAgainNotification(String message) {
    try {

      UserNotificationHelper
          .buildAndSend(new ScheduleEventUserCallAgainNotification(currentScheduleEvent, message,
              getUserDetail()));
      MessageNotifier.addSuccess(getString("scheduleevent.callagain.ok"));

    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private ScheduleEventService getScheduleEventService() {
    return ScheduleEventServiceProvider.getScheduleEventService();
  }

  public List<ScheduleEvent> getScheduleEventsByUserId() {
    Set<ScheduleEvent> allEvents = getScheduleEventService().listAllScheduleEventsByUserId(
        getUserId());
    List<ScheduleEvent> results = new ArrayList<>(allEvents);
    results.sort(new ScheduleEventComparator());

    return results;
  }

  public ScheduleEvent getDetail(String id) {
    ScheduleEvent event = getScheduleEventService().findScheduleEvent(id);
    // update last visited date
    if (event != null) {
      if (event.canBeAccessedBy(getUserDetail())) {
        getScheduleEventService().setLastVisited(event, Integer.parseInt(getUserId()));
      } else {
        event = null;
      }
    }
    return event;
  }

  public void switchState(String id) {
    ScheduleEvent event = getScheduleEventService().findScheduleEvent(id);
    if (isUserOwnerOfEvent(event)) {
      int actualStatus = event.getStatus();
      int newStatus = ScheduleEventStatus.OPEN;
      if (ScheduleEventStatus.OPEN == actualStatus) {
        newStatus = ScheduleEventStatus.CLOSED;
      }
      getScheduleEventService().updateScheduleEventStatus(id, newStatus);
    } else {
      SilverLogger.getLogger(this).warn(SECURITY_ALERT_FROM_USER_MSG_PREFIX + getUserId());
    }
  }

  private boolean isUserOwnerOfEvent(final ScheduleEvent event) {
    return event.getAuthor() == Integer.parseInt(getUserId());
  }

  public void delete(String scheduleEventId) {
    ScheduleEvent scheduleEvent = getScheduleEventService().findScheduleEvent(scheduleEventId);
    if (isUserOwnerOfEvent(scheduleEvent)) {
      getScheduleEventService().deleteScheduleEvent(scheduleEvent);
    } else {
      SilverLogger.getLogger(this).warn(SECURITY_ALERT_FROM_USER_MSG_PREFIX + getUserId());
    }
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
    } catch (Exception ignore) {
    }
    return null;
  }

  public ScheduleEvent purgeOldResponseForUserId(ScheduleEvent scheduleEvent) {
    return getScheduleEventService().purgeOldResponseForUserId(scheduleEvent,
        Integer.parseInt(getUserId()));
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

    MessageNotifier.addSuccess(getString("scheduleevent.form.confirmMessage"));
    return result;
  }

  /**
   * Converts the specified detailed scheduled event into a calendar event.
   * @param event detail.
   * @param listDateOption of dates.
   * @return the calendar events corresponding to the schedule event.
   */
  private List<CalendarEvent> asCalendarEvents(final ScheduleEvent event,
      final List<DateOption> listDateOption) {
    CalendarEventEncoder encoder = new CalendarEventEncoder();
    return encoder.encode(event, listDateOption);
  }

  /**
   * Exports the current ScheduleEvent in iCal format. The iCal file is generated into the temporary
   * directory.
   * @return the iCal file name into which is generated the current ScheduleEvent.
   * @throws ExportException on export error.
   */
  public String exportToICal(ScheduleEvent event) throws ExportException {

    // construction de la liste des dates retenues de l'événement
    List<DateOption> listDateOption = new ArrayList<>();
    ScheduleEventDetailVO scheduleEventDetailVO;
    try {
      scheduleEventDetailVO = new ScheduleEventDetailVO(this, event);
    } catch (Exception e) {
      throw new  ExportException(e);
    }
    BestTimeVO bestTimeVO = scheduleEventDetailVO.getBestTimes();
    if (bestTimeVO.isBestDateExists()) {
      List<TimeVO> listTimeVO = bestTimeVO.getTimes();
      for (TimeVO timeVO : listTimeVO) {
        HalfDayTime halfDayTime = (HalfDayTime) timeVO;
        DateVO dateVO = halfDayTime.getDate();
        HalfDayDateVO halfDayDateVO = (HalfDayDateVO) dateVO;
        Date day = halfDayDateVO.getDate();
        DateOption dateOption = new DateOption();
        dateOption.setDay(day);
        String label = halfDayTime.getMultilangLabel();
        if ("scheduleevent.form.hour.columnam".equals(label)) {
          dateOption.setHour(ScheduleEventVO.MORNING_HOUR);
        } else if ("scheduleevent.form.hour.columnpm".equals(label)) {
          dateOption.setHour(ScheduleEventVO.AFTERNOON_HOUR);
        }
        listDateOption.add(dateOption);
      }
    }

    // transformation des dates en CalendarEvent
    List<CalendarEvent> eventsToExport = asCalendarEvents(event, listDateOption);

    // export iCal
    Exporter<ExportableCalendar> iCalExporter = ICalExporterProvider.getICalExporter();
    String icsFileName = ICS_PREFIX + getUserId() + ".ics";
    String icsFilePath = FileRepositoryManager.getTemporaryPath() + icsFileName;
    try (final FileWriter fileWriter = new FileWriter(icsFilePath)) {
      iCalExporter.exports(withWriter(fileWriter), () -> ExportableCalendar.with(eventsToExport));
    } catch (ExportException ex) {
      File fileToDelete = new File(icsFilePath);
      if (fileToDelete.exists()) {
        FileUtils.deleteQuietly(fileToDelete);
      }
      throw ex;
    } catch (IOException e) {
     throw new ExportException(e);
    }

    return icsFileName;
  }
}
