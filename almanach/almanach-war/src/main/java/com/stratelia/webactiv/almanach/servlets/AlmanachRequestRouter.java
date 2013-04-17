/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package com.stratelia.webactiv.almanach.servlets;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.upload.FileUploadManager;
import org.silverpeas.upload.UploadedFile;

import com.silverpeas.export.ExportException;
import com.silverpeas.export.NoDataToExportException;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.almanach.control.AlmanachCalendarView;
import com.stratelia.webactiv.almanach.control.AlmanachSessionController;
import com.stratelia.webactiv.almanach.control.CalendarViewType;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

import static com.silverpeas.util.StringUtil.isDefined;
import static com.silverpeas.util.StringUtil.isInteger;
import static com.stratelia.webactiv.almanach.control.CalendarViewType.*;


public class AlmanachRequestRouter extends ComponentRequestRouter<AlmanachSessionController> {

  private static final long serialVersionUID = 1L;

  @Override
  public AlmanachSessionController createComponentSessionController(
          MainSessionController mainSessionCtrl, ComponentContext context) {
    return new AlmanachSessionController(mainSessionCtrl, context);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   *
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return "almanach";
  }

  /**
   * Set almanach settings
   *
   * @param almanach
   * @param request
   */
  private void setGlobalInfo(AlmanachSessionController almanach,
          HttpServletRequest request) {
    ResourceLocator settings = almanach.getSettings();
    request.setAttribute("settings", settings);
  }

  /**
   * This method has to be implemented by the component request Router it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param almanach The component Session Control, build and initialised.
   * @param request The entering request. The request Router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, AlmanachSessionController almanach,
          HttpServletRequest request) {

    SilverTrace.info("almanach", "AlmanachRequestRouter.getDestination()",
            "root.MSG_GEN_ENTER_METHOD");

    setGlobalInfo(almanach, request);

    String destination = "";

    // the flag is the best user's profile
    String flag = getFlag(almanach.getUserRoles());
    try {
      if (function.startsWith("Main") || function.startsWith("almanach")) {

        // contrôle de l'Action de l'utilisateur
        String action = request.getParameter("Action");

        // accès première fois, initialisation de l'almanach à la date du jour
        // (utile pour générer le Header)
        if (action == null || action.isEmpty()) {
          action = "View";
          String viewType = request.getParameter("view");
          if (isDefined(viewType)) {
            almanach.setViewMode(CalendarViewType.valueOf(viewType));
          }
        } else if ("PreviousView".equals(action)) {
          almanach.previousView();
        } else if ("NextView".equals(action)) {
          almanach.nextView();
        } else if ("GoToday".equals(action)) {
          almanach.today();
        } else if ("ViewByMonth".equals(action)) {
          almanach.setViewMode(MONTHLY);
        } else if ("ViewByWeek".equals(action)) {
          almanach.setViewMode(WEEKLY);
        } else if ("ViewNextEvents".equals(action)) {
          almanach.setViewMode(NEXT_EVENTS);
        }

        AlmanachCalendarView view = almanach.getAlmanachCalendarView();
        request.setAttribute("calendarView", view);
        request.setAttribute("othersAlmanachs", almanach.getAggregatedAlmanachs());
        request.setAttribute("accessibleInstances", almanach.getAccessibleInstances());
        request.setAttribute("RSSUrl", almanach.getRSSUrl());
        request.setAttribute("almanachURL", almanach.getAlmanachICSURL());

        if (view.getViewType() == NEXT_EVENTS) {
          destination = "/almanach/jsp/listOfEvents.jsp?flag=" + flag;
        } else {
          destination = "/almanach/jsp/calendar.jsp?flag=" + flag;
        }
      } else if (function.startsWith("portlet")) {
        AlmanachCalendarView view = almanach.getAlmanachCalendarViewOnTheNextEvents(false);
        request.setAttribute("calendarView", view);
        destination = "/almanach/jsp/portletCalendar.jsp";
      } else if (function.startsWith("viewEventContent")) {
        // initialisation de l'objet event
        String id = request.getParameter("Id");

        if (!isDefined(id)) {
          id = (String) request.getAttribute("Id");
        } else {
          request.setAttribute("From", request.getParameter("Function"));
        }

        // récupère l'Event et sa périodicité
        EventDetail event = almanach.getEventDetail(id);

        // Met en session l'événement courant
        almanach.setCurrentEvent(event);

        String dateIteration = request.getParameter("Date"); // not null (yyyy/MM/jj)
        if (event.isPeriodic() && isDefined(dateIteration)) {
          java.util.Calendar calDateIteration = java.util.Calendar.getInstance();
          calDateIteration.setTime(DateUtil.parse(dateIteration));
          request.setAttribute("EventStartDate", calDateIteration.getTime());
          calDateIteration.add(java.util.Calendar.DATE, event.getNbDaysDuration());
          request.setAttribute("EventEndDate", calDateIteration.getTime());
        } else {
          request.setAttribute("EventStartDate", event.getStartDate());
          request.setAttribute("EventEndDate", event.getEndDate());
        }

        request.setAttribute("Event", event);
        request.setAttribute("Contributor", almanach.getUserDetail(event.getCreatorId()));

        destination = "/almanach/jsp/viewEventContent.jsp?flag=" + flag;
      } else if (function.startsWith("createEvent")) {
        String day = request.getParameter("Day");

        EventDetail event = new EventDetail();
        String[] startDay = {"", ""};
        if (day != null && day.length() > 0) {
          event.setStartDate(DateUtil.parseISO8601Date(day));
          ResourcesWrapper resources = (ResourcesWrapper) request.getAttribute("resources");
          startDay[0] = resources.getInputDate(event.getStartDate());
          if (!day.endsWith("00:00")) {
            startDay[1] = day.substring(day.indexOf('T') + 1);
            event.setStartHour(startDay[1]);
          }
        }
        request.setAttribute("Day", startDay);
        request.setAttribute("Event", event);
        request.setAttribute("Language", almanach.getLanguage());
        request.setAttribute("MaxDateFieldLength", DBUtil.getDateFieldLength());
        request.setAttribute("MaxTextFieldLength", DBUtil.getTextFieldLength());

        if (flag.equals("publisher") || flag.equals("admin")) {
          destination = "/almanach/jsp/createEvent.jsp";
        } else {
          destination = GeneralPropertiesManager.getString("sessionTimeout");
        }
      } else if (function.equals("ReallyAddEvent")) {

        EventDetail event = new EventDetail();

        String title = request.getParameter("Title");
        String description = request.getParameter("Description");
        String startDate = request.getParameter("StartDate");
        String startHour = request.getParameter("StartHour");
        String endDate = request.getParameter("EndDate");
        String endHour = request.getParameter("EndHour");
        String place = request.getParameter("Place");
        String eventUrl = request.getParameter("EventUrl");
        String priority = request.getParameter("Priority");

        int unity = 0;
        String unit = request.getParameter("Unity");
        if (isDefined(unit) && isInteger(unit)) {
          unity = Integer.parseInt(unit);
        }
        String frequency = request.getParameter("Frequency");
        String weekDayWeek2 = request.getParameter("WeekDayWeek2");
        String weekDayWeek3 = request.getParameter("WeekDayWeek3");
        String weekDayWeek4 = request.getParameter("WeekDayWeek4");
        String weekDayWeek5 = request.getParameter("WeekDayWeek5");
        String weekDayWeek6 = request.getParameter("WeekDayWeek6");
        String weekDayWeek7 = request.getParameter("WeekDayWeek7");
        String weekDayWeek1 = request.getParameter("WeekDayWeek1");
        String choiceMonth = request.getParameter("ChoiceMonth");
        String monthNumWeek = request.getParameter("MonthNumWeek");
        String monthDayWeek = request.getParameter("MonthDayWeek");
        String periodicityUntilDate = request.getParameter("PeriodicityUntilDate");

        event.setTitle(title);
        event.setNameDescription(description);
        event.setStartDate(DateUtil.stringToDate(startDate, almanach.getLanguage()));
        event.setStartHour(startHour);
        if (isDefined(endDate)) {
          event.setEndDate(DateUtil.stringToDate(endDate, almanach.getLanguage()));
        } else {
          event.setEndDate(null);
        }
        event.setEndHour(endHour);
        event.setPlace(place);
        event.setEventUrl(eventUrl);

        int priorityInt = 0;
        if (priority != null && priority.length() > 0) {
          priorityInt = 1;
        }
        event.setPriority(priorityInt);

        // Périodicité
        Periodicity periodicity = null;

        if (unity > Periodicity.UNIT_NONE) {
          if (periodicity == null) {
            periodicity = new Periodicity();
          }
          periodicity.setUnity(unity);
          periodicity.setFrequency(Integer.parseInt(frequency));
          switch (unity) {
            case Periodicity.UNIT_WEEK:
              String daysWeekBinary = "";
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek2);
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek3);
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek4);
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek5);
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek6);
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek7);
              daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek1);
              periodicity.setDaysWeekBinary(daysWeekBinary);
              break;
            case Periodicity.UNIT_MONTH:
              if ("MonthDay".equals(choiceMonth)) {
                periodicity.setNumWeek(Integer.parseInt(monthNumWeek));
                periodicity.setDay(Integer.parseInt(monthDayWeek));
              }
              break;
          }
          if (isDefined(periodicityUntilDate)) {
            periodicity.setUntilDatePeriod(DateUtil.stringToDate(periodicityUntilDate, endHour,
                    almanach.getLanguage()));
          }
        } else {// update -> pas de périodicité
          periodicity = null;
        }
        event.setPeriodicity(periodicity);

        // Ajoute l'événement
        Collection<UploadedFile> uploadedFiles = FileUploadManager.getUploadedFiles(request);
        String positions = request.getParameter("Positions");
        if (StringUtil.isDefined(positions)) {
          PdcClassificationEntity withClassification = PdcClassificationEntity.fromJSON(positions);
          almanach.addEvent(event, uploadedFiles, withClassification);
        } else {
          almanach.addEvent(event, uploadedFiles);
        }

        destination = getDestination("almanach", almanach, request);
      } else if (function.startsWith("editEvent")) {
        String id = request.getParameter("Id"); // peut etre null en cas de
        // création
        String eventDate = request.getParameter("Date"); // peut etre null
        // récupère l'Event et sa périodicité
        EventDetail event = almanach.getEventDetail(id);

        java.util.Calendar calDate = java.util.Calendar.getInstance();
        calDate.setTime(DateUtil.parse(eventDate));
        request.setAttribute("EventStartDate", calDate.getTime());
        calDate.add(java.util.Calendar.DATE, event.getNbDaysDuration());
        request.setAttribute("EventEndDate", calDate.getTime());
        request.setAttribute("Event", event);

        // Met en session l'événement courant
        almanach.setCurrentEvent(event);

        if (flag.equals("publisher") || flag.equals("admin")) {
          destination = "/almanach/jsp/editEvent.jsp";
        } else {
          destination = GeneralPropertiesManager.getString("sessionTimeout");
        }
      } else if (function.equals("ReallyUpdateEvent")) {
        String action = request.getParameter("Action");// ReallyUpdateOccurence
        // | ReallyUpdateSerial |
        // ReallyUpdate
        String id = request.getParameter("Id"); // not null
        String eventStartDate = request.getParameter("EventStartDate"); // format
        // client
        String eventEndDate = request.getParameter("EventEndDate"); // format
        // client

        EventDetail event = almanach.getEventDetail(id);

        String title = request.getParameter("Title");
        String description = request.getParameter("Description");
        String startDate = request.getParameter("StartDate");
        String startHour = request.getParameter("StartHour");
        String endDate = request.getParameter("EndDate");
        String endHour = request.getParameter("EndHour");
        String place = request.getParameter("Place");
        String eventUrl = request.getParameter("EventUrl");
        String priority = request.getParameter("Priority");

        String unity = request.getParameter("Unity");
        String frequency = request.getParameter("Frequency");
        String weekDayWeek2 = request.getParameter("WeekDayWeek2");
        String weekDayWeek3 = request.getParameter("WeekDayWeek3");
        String weekDayWeek4 = request.getParameter("WeekDayWeek4");
        String weekDayWeek5 = request.getParameter("WeekDayWeek5");
        String weekDayWeek6 = request.getParameter("WeekDayWeek6");
        String weekDayWeek7 = request.getParameter("WeekDayWeek7");
        String weekDayWeek1 = request.getParameter("WeekDayWeek1");
        String choiceMonth = request.getParameter("ChoiceMonth");
        String monthNumWeek = request.getParameter("MonthNumWeek");
        String monthDayWeek = request.getParameter("MonthDayWeek");
        String periodicityStartDate = request.getParameter("PeriodicityStartDate");
        String periodicityUntilDate = request.getParameter("PeriodicityUntilDate");

        event.setTitle(title);
        event.setNameDescription(description);
        event.setStartDate(DateUtil.stringToDate(startDate, almanach.getLanguage()));
        event.setStartHour(startHour);
        if ((endDate != null) && (endDate.length() > 0)) {
          event.setEndDate(DateUtil.stringToDate(endDate, almanach.getLanguage()));
        } else {
          event.setEndDate(null);
        }
        int nbDaysDuration = event.getNbDaysDuration();
        event.setEndHour(endHour);
        event.setPlace(place);
        event.setEventUrl(eventUrl);

        int priorityInt = 0;
        if (priority != null && priority.length() > 0) {
          priorityInt = 1;
        }
        event.setPriority(priorityInt);

        // Périodicité
        Periodicity periodicity = null;
        if (id != null && id.length() > 0) {
          // récupère la périodicité de l'événement
          periodicity = event.getPeriodicity();
        }

        if (unity != null && !"0".equals(unity)) {
          if (periodicity == null) {
            periodicity = new Periodicity();
          }
          periodicity.setUnity(Integer.parseInt(unity));
          periodicity.setFrequency(Integer.parseInt(frequency));

          if ("2".equals(unity)) {// Periodicity.UNIT_WEEK
            String daysWeekBinary = "";
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek2);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek3);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek4);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek5);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek6);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek7);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek1);
            periodicity.setDaysWeekBinary(daysWeekBinary);
          } else if ("3".equals(unity)) {// Periodicity.UNIT_MONTH
            if ("MonthDay".equals(choiceMonth)) {
              periodicity.setNumWeek(Integer.parseInt(monthNumWeek));
              periodicity.setDay(Integer.parseInt(monthDayWeek));
            }
          }

          if (periodicityUntilDate != null && periodicityUntilDate.length() > 0) {
            periodicity.setUntilDatePeriod(DateUtil.stringToDate(
                    periodicityUntilDate, almanach.getLanguage()));
          } else {
            periodicity.setUntilDatePeriod(null);
          }
        } else {// update -> pas de périodicité
          periodicity = null;
          periodicityStartDate = startDate; // meme date
        }
        event.setPeriodicity(periodicity);

        if ("ReallyUpdateOccurence".equals(action)) {

          // Met à jour l'événement et toutes les occurences de la série
          almanach.updateEventOccurence(event, eventStartDate, eventEndDate);
        } else if ("ReallyUpdateSerial".equals(action)) {
          java.util.Date startDateEvent = DateUtil.stringToDate(
                  periodicityStartDate, almanach.getLanguage());
          event.setStartDate(startDateEvent);
          java.util.Calendar calStartDate = java.util.Calendar.getInstance();
          calStartDate.setTime(startDateEvent);
          calStartDate.add(java.util.Calendar.DATE, nbDaysDuration);
          event.setEndDate(calStartDate.getTime());

          // Met à jour l'événement
          almanach.updateEvent(event);
        } else if ("ReallyUpdate".equals(action)) {
          java.util.Date startDateEvent = DateUtil.stringToDate(
                  periodicityStartDate, almanach.getLanguage());
          event.setStartDate(startDateEvent);
          java.util.Calendar calStartDate = java.util.Calendar.getInstance();
          calStartDate.setTime(startDateEvent);
          calStartDate.add(java.util.Calendar.DATE, nbDaysDuration);
          event.setEndDate(calStartDate.getTime());

          // Met à jour l'événement
          almanach.updateEvent(event);
        }

        destination = getDestination("almanach", almanach, request);

      } else if (function.startsWith("Pdf")) {
        // Recuperation des parametres
        String fileName = almanach.buildPdf(function);
        request.setAttribute("FileName", fileName);

        destination = "/almanach/jsp/pdf.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");
        request.setAttribute("Id", id);

        destination = getDestination("viewEventContent", almanach, request);
      } else if (function.equals("RemoveEvent")) {
        String startDate = request.getParameter("EventStartDate"); // format
        // client

        String action = request.getParameter("Action");

        if ("ReallyDeleteOccurence".equals(action)) {
          // Supprime l'occurence
          almanach.removeOccurenceEvent(almanach.getCurrentEvent(), startDate);

        } else if ("ReallyDelete".equals(action)) {

          // Supprime l'événement et toutes les occurences de la série
          almanach.removeEvent(almanach.getCurrentEvent().getPK().getId());
        }

        destination = getDestination("Main", almanach, request);
      } else if (function.equals("UpdateAgregation")) {
        String[] instanceIds = request.getParameterValues("chk_almanach");
        SilverTrace.info("almanach", "AlmanachRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "instanceIds = " + instanceIds);
        almanach.updateAgregatedAlmanachs(instanceIds);
        destination = getDestination("almanach", almanach, request);
      } else if (function.equals("ToAlertUser")) {
        String id = request.getParameter("Id");
        SilverTrace.info("almanach", "AlmanachRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "id = " + id);
        try {
          destination = almanach.initAlertUser(id);
        } catch (Exception e) {
          request.setAttribute("javax.servlet.jsp.jspException", e);
        }
      } else if ("ViewYearEvents".equals(function)) {
        AlmanachCalendarView calendar = almanach.getYearlyAlmanachCalendarView();
        request.setAttribute("calendarView", calendar);
        request.setAttribute("Function", function);
        destination = "/almanach/jsp/listOfEvents.jsp";
      } else if ("ViewMonthEvents".equals(function)) {
        AlmanachCalendarView calendar = almanach.getMonthlyAlmanachCalendarView();
        request.setAttribute("calendarView", calendar);
        request.setAttribute("Function", function);
        destination = "/almanach/jsp/listOfEvents.jsp";
      } else if ("ViewYearEventsPOPUP".equals(function)) {
        AlmanachCalendarView calendarView = almanach.getYearlyAlmanachCalendarView();
        request.setAttribute("calendarView", calendarView);
        request.setAttribute("Function", function);
        destination = "/almanach/jsp/viewEventsPopup.jsp";
      } else if ("exportToICal".equals(function)) {
        try {
          String icsFile = almanach.exportToICal();
          request.setAttribute("messageKey", "almanach.export.ical.success");
          request.setAttribute("icsName", icsFile);
          request.setAttribute("icsURL", FileServerUtils.getUrlToTempDir(icsFile));
        } catch (NoDataToExportException ex) {
          SilverTrace.info("almanach", getClass().getSimpleName() + ".getDestination()",
                  "root.EX_NO_MESSAGE", ex.getMessage());
          request.setAttribute("messageKey", "almanach.export.ical.empty");
        } catch (ExportException ex) {
          SilverTrace.error("almanach", getClass().getSimpleName() + ".getDestination()",
                  "root.EX_NO_MESSAGE", ex.getMessage());
          request.setAttribute("messageKey", "almanach.export.ical.failure");
        }
        destination = "/almanach/jsp/exportIcal.jsp";
      } else {
        destination = "/almanach/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }

    SilverTrace.info("almanach", "AlmanachRequestRouter.getDestination()",
            "root.MSG_GEN_EXIT_METHOD", "destination = " + destination);
    return destination;
  }

  public String getFlag(String[] profiles) {
    String flag = "user";
    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("admin")) {
        return profiles[i];
      } else if (profiles[i].equals("publisher")) {
        flag = profiles[i];
      }
    }
    return flag;
  }

  private String addValueBinary(final String binary, final String test) {
    String result = binary;
    if (test != null) {
      result += "1";
    } else {
      result += "0";
    }
    return result;
  }
}
