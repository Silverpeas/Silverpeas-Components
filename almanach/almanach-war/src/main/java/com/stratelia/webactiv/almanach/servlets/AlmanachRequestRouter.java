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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.almanach.servlets;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.control.AlmanachSessionController;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;

public class AlmanachRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;

  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext context) {
    return ((ComponentSessionController) new AlmanachSessionController(
        mainSessionCtrl, context));
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  public String getSessionControlBeanName() {
    return "almanach";
  }

  /**
   * Set almanach settings
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
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @param request The entering request. The request Router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {

    SilverTrace.info("almanach", "AlmanachRequestRouter.getDestination()",
        "root.MSG_GEN_ENTER_METHOD");

    AlmanachSessionController almanach = (AlmanachSessionController) componentSC;
    setGlobalInfo(almanach, request);

    String destination = "";

    // the flag is the best user's profile
    String flag = getFlag(componentSC.getUserRoles());
    try {
      if (function.startsWith("Main") || function.startsWith("almanach")
          || function.startsWith("portlet")) {

        // initialisation d'un MonthCalendar du viewgenerator
        MonthCalendar monthC = almanach.getMonthCalendar();

        // contrôle de l'Action de l'utilisateur
        String action = request.getParameter("Action");

        // accès première fois, initialisation de l'almanach à la date du jour
        // (utile pour générer le Header)
        if (action == null || action.length() == 0) {
          action = "View";
        } else if (action.equals("PreviousMonth")) {
          almanach.previousMonth();
        } else if (action.equals("NextMonth")) {
          almanach.nextMonth();
        } else if (action.equals("GoToday")) {
          almanach.today();
        }

        // initialisation d'un Calendar ical4j
        Calendar calendarAlmanach = almanach.getICal4jCalendar(almanach
            .getAllEventsAgregation());
        almanach.setCurrentICal4jCalendar(calendarAlmanach);

        // transformation des VEvent du Calendar ical4j en Event du
        // MonthCalendar
        java.util.Calendar firstDayMonth = almanach.getCurrentDay();
        firstDayMonth.set(java.util.Calendar.DATE, 1);
        firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
        firstDayMonth.set(java.util.Calendar.MINUTE, 0);
        firstDayMonth.set(java.util.Calendar.SECOND, 0);
        firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
        lastDayMonth.setTime(firstDayMonth.getTime());
        lastDayMonth.add(java.util.Calendar.MONTH, 1);
        lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
        lastDayMonth.set(java.util.Calendar.MINUTE, 0);
        lastDayMonth.set(java.util.Calendar.SECOND, 0);
        lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);
        Period monthPeriod = new Period(new DateTime(firstDayMonth.getTime()),
            new DateTime(lastDayMonth.getTime()));

        ComponentList componentList = calendarAlmanach
            .getComponents(Component.VEVENT);
        Iterator<VEvent> itVEvent = componentList.iterator();

        VEvent eventIcal4jCalendar;
        PeriodList periodList;
        Iterator<Period> itPeriod;
        Period recurrencePeriod;
        String idEvent;
        EventDetail evtDetail;
        Event evt;
        while (itVEvent.hasNext()) {
          eventIcal4jCalendar = itVEvent.next();
          idEvent = eventIcal4jCalendar.getProperties().getProperty(
              Property.UID).getValue();

          // Récupère l'événement
          evtDetail = almanach.getEventDetail(idEvent);

          periodList = eventIcal4jCalendar.calculateRecurrenceSet(monthPeriod);
          itPeriod = periodList.iterator();
          while (itPeriod.hasNext()) {
            recurrencePeriod = itPeriod.next();

            // Construction de l'Event du MonthCalendar (pour affichage)
            evt = new Event(idEvent, evtDetail.getName(), new java.util.Date(
                recurrencePeriod.getStart().getTime()), new java.util.Date(
                recurrencePeriod.getEnd().getTime()), evtDetail.getURL(),
                evtDetail.getPriority());
            evt.setStartHour(evtDetail.getStartHour());
            evt.setEndHour(evtDetail.getEndHour());
            evt.setPlace(evtDetail.getPlace());
            if (almanach.isAgregationUsed()) {
              evt
                  .setColor(almanach
                  .getAlmanachColor(evtDetail.getInstanceId()));
            }
            evt.setInstanceId(evtDetail.getInstanceId());
            monthC.addEvent(evt);
          }
        }

        // initialisation de monthC avec la date courante issue de almanach
        monthC.setCurrentMonth(almanach.getCurrentDay().getTime());

        request.setAttribute("MonthCalendar", monthC);
        request.setAttribute("RSSUrl", almanach.getRSSUrl());

        request.setAttribute("AccessibleInstances", almanach
            .getAccessibleInstances());

        if (function.startsWith("portlet")) {
          destination = "/almanach/jsp/portletAlmanach.jsp?flag=" + flag;
        } else {
          destination = "/almanach/jsp/almanach.jsp?flag=" + flag;
        }
      } else if (function.startsWith("viewEventContent")) {
        // initialisation de l'objet event
        String id = request.getParameter("Id"); // not null

        // récupère l'Event et sa périodicité
        EventDetail event = almanach.getCompleteEventDetail(id);
        
        //Met en session l'événement courant
        almanach.setCurrentEvent(event);

        if (event.getPeriodicity() != null) {
          String dateIteration = request.getParameter("Date"); // not null (yyyy/MM/jj)
          java.util.Calendar calDateIteration = java.util.Calendar.getInstance();
          calDateIteration.setTime(DateUtil.parse(dateIteration));
          request.setAttribute("DateDebutIteration", calDateIteration.getTime());
          calDateIteration.add(java.util.Calendar.DATE, event.getNbDaysDuration());
          request.setAttribute("DateFinIteration", calDateIteration.getTime());
        } else {
          request.setAttribute("DateDebutIteration", event.getStartDate());
          request.setAttribute("DateFinIteration", event.getEndDate());
        }

        request.setAttribute("CompleteEvent", event);

        destination = "/almanach/jsp/viewEventContent.jsp?flag=" + flag;
      } else if (function.startsWith("createEvent")) {
        String day = request.getParameter("Day");

        EventDetail event = new EventDetail();
        if (day != null && day.length() > 0) {
          event
              .setStartDate(DateUtil.stringToDate(day, almanach.getLanguage()));
        }

        request.setAttribute("Event", event);

        if (flag.equals("publisher") || flag.equals("admin")) {
          destination = "/almanach/jsp/createEvent.jsp";
        } else {
          destination = GeneralPropertiesManager.getGeneralResourceLocator()
              .getString("sessionTimeout");
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
        String periodicityUntilDate = request
            .getParameter("PeriodicityUntilDate");

        event.setTitle(title);
        event.setNameDescription(description);
        event.setStartDate(DateUtil.stringToDate(startDate, almanach
            .getLanguage()));
        event.setStartHour(startHour);
        if ((endDate != null) && (endDate.length() > 0)) {
          event.setEndDate(DateUtil.stringToDate(endDate, almanach
              .getLanguage()));
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

        if (unity != null && !"0".equals(unity)) {
          if (periodicity == null) {
            periodicity = new Periodicity();
          }
          periodicity.setUnity(new Integer(unity).intValue());
          periodicity.setFrequency(new Integer(frequency).intValue());

          if ("2".equals(unity)) {// Periodicity.UNITY_WEEK
            String daysWeekBinary = "";
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek2);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek3);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek4);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek5);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek6);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek7);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek1);
            periodicity.setDaysWeekBinary(daysWeekBinary);
          } else if ("3".equals(unity)) {// Periodicity.UNITY_MONTH
            if ("MonthDay".equals(choiceMonth)) {
              periodicity.setNumWeek(new Integer(monthNumWeek).intValue());
              periodicity.setDay(new Integer(monthDayWeek).intValue());
            }
          }

          if (periodicityUntilDate != null && periodicityUntilDate.length() > 0) {
            periodicity.setUntilDatePeriod(DateUtil.stringToDate(
                periodicityUntilDate, almanach.getLanguage()));
          }
        } else {// update -> pas de périodicité
          periodicity = null;
        }
        event.setPeriodicity(periodicity);

        // Ajoute l'événement
        almanach.addEvent(event);

        destination = getDestination("almanach", almanach, request);
      } else if (function.startsWith("editEvent")) {
        String id = request.getParameter("Id"); // peut etre null en cas de
        // création
        String dateIteration = request.getParameter("Date"); // peut etre null
        // en cas de
        // création
        // (yyyy/MM/jj)

        // récupère l'Event et sa périodicité
        EventDetail event = almanach.getCompleteEventDetail(id);

        java.util.Calendar calDateIteration = java.util.Calendar.getInstance();
        calDateIteration.setTime(DateUtil.parse(dateIteration));
        request.setAttribute("DateDebutIteration", calDateIteration.getTime());
        calDateIteration
            .add(java.util.Calendar.DATE, event.getNbDaysDuration());
        request.setAttribute("DateFinIteration", calDateIteration.getTime());
        request.setAttribute("CompleteEvent", event);

        // Met en session l'événement courant
        almanach.setCurrentEvent(event);

        if (flag.equals("publisher") || flag.equals("admin")) {
          destination = "/almanach/jsp/editEvent.jsp";
        } else {
          destination = GeneralPropertiesManager.getGeneralResourceLocator()
              .getString("sessionTimeout");
        }
      } else if (function.equals("ReallyUpdateEvent")) {
        String action = request.getParameter("Action");// ReallyUpdateOccurence
        // | ReallyUpdateSerial |
        // ReallyUpdate
        String id = request.getParameter("Id"); // not null
        String dateDebutIteration = request.getParameter("DateDebutIteration"); // format
        // client
        String dateFinIteration = request.getParameter("DateFinIteration"); // format
        // client

        EventDetail event = almanach.getCompleteEventDetail(id);

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
        String periodicityStartDate = request
            .getParameter("PeriodicityStartDate");
        String periodicityUntilDate = request
            .getParameter("PeriodicityUntilDate");

        event.setTitle(title);
        event.setNameDescription(description);
        event.setStartDate(DateUtil.stringToDate(startDate, almanach
            .getLanguage()));
        event.setStartHour(startHour);
        if ((endDate != null) && (endDate.length() > 0)) {
          event.setEndDate(DateUtil.stringToDate(endDate, almanach
              .getLanguage()));
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
          periodicity.setUnity(new Integer(unity).intValue());
          periodicity.setFrequency(new Integer(frequency).intValue());

          if ("2".equals(unity)) {// Periodicity.UNITY_WEEK
            String daysWeekBinary = "";
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek2);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek3);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek4);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek5);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek6);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek7);
            daysWeekBinary = addValueBinary(daysWeekBinary, weekDayWeek1);
            periodicity.setDaysWeekBinary(daysWeekBinary);
          } else if ("3".equals(unity)) {// Periodicity.UNITY_MONTH
            if ("MonthDay".equals(choiceMonth)) {
              periodicity.setNumWeek(new Integer(monthNumWeek).intValue());
              periodicity.setDay(new Integer(monthDayWeek).intValue());
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
          almanach.updateEventOccurence(event, dateDebutIteration,
              dateFinIteration);
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

      } else if (function.startsWith("editAttFiles")) {
        String id = request.getParameter("Id");
        String dateIteration = request.getParameter("Date");

        // récupère l'Event
        EventDetail event = almanach.getEventDetail(id);

        request.setAttribute("DateDebutIteration", DateUtil
            .parse(dateIteration));
        request.setAttribute("Event", event);

        destination = "/almanach/jsp/editAttFiles.jsp";
      } else if (function.startsWith("pdcPositions")) {
        String id = request.getParameter("Id");
        String dateIteration = request.getParameter("Date");

        // récupère l'Event
        EventDetail event = almanach.getEventDetail(id);

        request.setAttribute("DateDebutIteration", DateUtil
            .parse(dateIteration));
        request.setAttribute("Event", event);

        destination = "/almanach/jsp/pdcPositions.jsp";
      } else if (function.startsWith("printAlmanach")) {
        request.setAttribute("ListEvent", almanach.getListRecurrentEvent());

        destination = "/almanach/jsp/printAlmanach.jsp";
      } else if (function.startsWith("EventPdf")) {
        // Recuperation des parametres
        String fileName = almanach.buildPdf();
        request.setAttribute("FileName", fileName);

        destination = "/almanach/jsp/pdf.jsp";
      } else if (function.startsWith("MonthPdf")) {
        // Recuperation des parametres
        String fileName = almanach.buildPdf(true);
        request.setAttribute("FileName", fileName);

        destination = "/almanach/jsp/pdf.jsp";
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id");

        // récupère l'Event et sa périodicité
        EventDetail event = almanach.getCompleteEventDetail(id);

        java.util.Calendar calDateIteration = java.util.Calendar.getInstance();
        calDateIteration.setTime(event.getStartDate());
        request.setAttribute("DateDebutIteration", calDateIteration.getTime());
        calDateIteration
            .add(java.util.Calendar.DATE, event.getNbDaysDuration());
        request.setAttribute("DateFinIteration", calDateIteration.getTime());
        request.setAttribute("CompleteEvent", event);

        destination = "/almanach/jsp/viewEventContent.jsp?flag=" + flag;
      } else if (function.startsWith("GoToFilesTab")) { // ??
        destination = "/almanach/jsp/editAttFiles.jsp?Id="
            + request.getParameter("Id");
      } else if (function.equals("RemoveEvent")) {
        String dateDebutIteration = request.getParameter("DateDebutIteration"); // format
        // client
        String dateFinIteration = request.getParameter("DateFinIteration"); // format
        // client

        String action = request.getParameter("Action");

        if ("ReallyDeleteOccurence".equals(action)) {
          // Supprime l'occurence
          almanach.removeOccurenceEvent(almanach.getCurrentEvent(),
              dateDebutIteration, dateFinIteration);

        } else if ("ReallyDelete".equals(action)) {

          // Supprime l'événement et toutes les occurences de la série
          almanach
              .removeEvent(almanach.getCurrentEvent().getPK().getId());
        }

        destination = getDestination("Main", componentSC, request);
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
      }

      else {
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
      if (profiles[i].equals("admin"))
        return profiles[i];
      else if (profiles[i].equals("publisher"))
        flag = profiles[i];
    }
    return flag;
  }

  private String addValueBinary(String binary, String test) {
    if (test != null) {
      binary += "1";
    } else {
      binary += "0";
    }
    return binary;
  }
}