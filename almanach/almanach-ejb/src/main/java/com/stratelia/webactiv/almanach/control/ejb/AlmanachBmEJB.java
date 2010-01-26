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
package com.stratelia.webactiv.almanach.control.ejb;

import java.sql.Connection;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.almanach.AlmanachContentManager;
import com.stratelia.webactiv.almanach.model.EventDAO;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.almanach.model.PeriodicityException;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.persistence.IdPK;
import com.stratelia.webactiv.persistence.PersistenceException;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAOFactory;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class AlmanachBmEJB implements AlmanachBmBusinessSkeleton, SessionBean {

  private AlmanachContentManager almanachContentManager = null;
  private SilverpeasBeanDAO eventPeriodicityDAO = null;
  private SilverpeasBeanDAO periodicityExceptionDAO = null;

  /**
   * Get the events of the month
   *
   * @author dlesimple
   * @param pk
   * @param date
   * @param String
   *          [] of instanceIds
   * @return Collection of Events
   */
  public Collection getMonthEvents(EventPK pk, java.util.Date date,
      String[] instanceIds) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getMonthsEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      Collection result = EventDAO.getMonthEvents(con, pk, date, instanceIds);
      return result;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getMonthEvents()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EXE_GET_MONTH_EVENTS_FAIL", e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.getMonthEvents()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);

      }
    }
  }

  /**
   * Get the events of the month
   *
   * @author dlesimple
   * @param pk
   * @param date
   * @return Collection of Events
   */
  public Collection getMonthEvents(EventPK pk, java.util.Date date) {
    return getMonthEvents(pk, date, null);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getAllEvents(com.stratelia.webactiv.almanach.model.EventPK)
   */
  public Collection getAllEvents(EventPK pk) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getAllEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      Collection result = EventDAO.getAllEvents(con, pk);
      return result;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getAllEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL",
          e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.getAllEvents()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /**
   * Get all events
   *
   * @param pk
   * @param String
   *          [] of instanceIds
   * @return Collection of Events
   */
  public Collection getAllEvents(EventPK pk, String[] instanceIds) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getAllEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      Collection result = EventDAO.getAllEvents(con, pk, instanceIds);
      return result;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getAllEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL",
          e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.getAllEvents()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);

      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getEvents(java.util.Collection)
   */
  public Collection getEvents(Collection pks) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      Collection result = EventDAO.selectByEventPKs(con, pks);
      return result;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_EVENTS_FAIL", e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.getEvents()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /**
   * Get Event Detail
   */
  public EventDetail getEventDetail(EventPK pk) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getEventDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      EventDetail result = EventDAO.getEventDetail(con, pk);
      return result;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getEventDetail()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EXE_GET_EVENT_DETAIL_FAIL", e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.getEventDetail()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * addEvent(com.stratelia.webactiv.almanach.model.EventDetail)
   */
  public String addEvent(EventDetail event) {
    SilverTrace.info("almanach", "AlmanachBmEJB.addEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      String id = EventDAO.addEvent(con, event);
      event.setPK(new EventPK(id, event.getPK()));
      createIndex(event);
      createSilverContent(con, event, event.getCreatorId());
      return id;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.addEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_ADD_EVENT_FAIL", e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.addEvent()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /**
   * updateEvent() update the event entry, specified by the pk, in the database
   */
  public void updateEvent(EventDetail event) {
    SilverTrace.info("almanach", "AlmanachBmEJB.updateEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      EventDAO.updateEvent(con, event);
      createIndex(event);
      updateSilverContentVisibility(event);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.updateEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_UPDATE_EVENT_FAIL", e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.updateEvent()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /**
   * removeEvent() remove the Event entry specified by the pk
   */
  public void removeEvent(EventPK pk) {
    SilverTrace.info("almanach", "AlmanachBmEJB.removeEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      EventDAO.removeEvent(con, pk);
      deleteIndex(pk);
      deleteSilverContent(con, pk);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.removeEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_REMOVE_EVENT_FAIL", e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.removeEvent()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * createIndex(com.stratelia.webactiv.almanach.model.EventDetail)
   */
  public void createIndex(EventDetail detail) {
    SilverTrace.info("almanach", "AlmanachBmEJB.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "PK=" + detail.getPK());
    try {
      FullIndexEntry indexEntry = null;
      if (detail != null) {
        indexEntry = new FullIndexEntry(detail.getPK().getComponentName(),
            "Event", detail.getPK().getId());
        indexEntry.setTitle(detail.getTitle());
        indexEntry = updateIndexEntryWithWysiwygContent(indexEntry, detail);
        indexEntry.setCreationUser(detail.getDelegatorId());
        IndexEngineProxy.addIndexEntry(indexEntry);
      }
    } catch (Exception e) {
      SilverTrace.warn("almanach", "AlmanachBmEJB.createIndex()",
          "root.EXE_CREATE_INDEX_FAIL", null, e);
    }
  }

  /**
   * Update Index Entry
   *
   * @param indexEntry
   * @param eventDetail
   * @return FullIndexEntry
   */
  private FullIndexEntry updateIndexEntryWithWysiwygContent(
      FullIndexEntry indexEntry, EventDetail eventDetail) {
    EventPK eventPK = eventDetail.getPK();
    SilverTrace.info("almanach",
        "AlmanachBmEJB.updateIndexEntryWithWysiwygContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString()
            + ", eventPK = " + eventPK.toString());
    try {
      if (eventPK != null) {
        String wysiwygContent = WysiwygController.load(eventPK.getInstanceId(),
            eventPK.getId(), eventDetail.getLanguage());
        if (StringUtil.isDefined(wysiwygContent)) {
          String wysiwygPath = WysiwygController.getWysiwygPath(eventPK
              .getInstanceId(), eventPK.getId(), eventDetail.getLanguage());
          indexEntry.addFileContent(wysiwygPath, null, "text/html", eventDetail
              .getLanguage());
        }
      }
    } catch (Exception e) {
      // No wysiwyg associated
    }
    return indexEntry;
  }

  /**
   * @param eventPK
   */
  private void deleteIndex(EventPK eventPK) {
    SilverTrace.info("almanach", "AlmanachBmEJB.deleteIndex()",
        "almanach.MSG_GEN_ENTER_METHOD", "PK=" + eventPK.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(eventPK.getComponentName(),
        "Event", eventPK.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getNextEvents(java.lang.String, int)
   */
  public Collection getNextEvents(String instanceId, int nbReturned) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getNextEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      EventPK pk = new EventPK("", "", instanceId);
      con = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      Collection result = EventDAO.getNextEvents(con, pk, nbReturned);
      return result;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getNextEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL",
          e);
    } finally {
      try {
        if (con != null)
          con.close();
      } catch (Exception e) {
        throw new AlmanachRuntimeException("AlmanachBmEJB.getNextEvents()",
            SilverpeasRuntimeException.ERROR,
            "root.EXE_CONNECTION_CLOSE_FAILED", e);
      }
    }
  }

  /**
   * @return
   */
  private SilverpeasBeanDAO getEventPeriodicityDAO() {
    if (eventPeriodicityDAO == null) {
      try {
        eventPeriodicityDAO = SilverpeasBeanDAOFactory
            .getDAO("com.stratelia.webactiv.almanach.model.Periodicity");
      } catch (PersistenceException pe) {
        throw new AlmanachRuntimeException(
            "AlmanachBmEJB.getEventPeriodicityDAO()",
            SilverpeasRuntimeException.ERROR,
            "almanach.EX_PERSISTENCE_PERIODICITY", pe);
      }
    }
    return eventPeriodicityDAO;
  }

  /**
   * @param periodicity
   */
  public void addPeriodicity(Periodicity periodicity) {
    SilverTrace.info("almanach", "AlmanachBmEJB.addPeriodicity()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      IdPK pk = new IdPK();
      periodicity.setPK(pk);
      getEventPeriodicityDAO().add(periodicity);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.addPeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_ADD_PERIODICITY", e);
    }
  }

  /**
   * @param eventId
   * @return
   */
  public Periodicity getPeriodicity(String eventId) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getPeriodicity()",
        "root.MSG_GEN_ENTER_METHOD");
    Periodicity periodicity = null;
    try {
      IdPK pk = new IdPK();
      Collection list = getEventPeriodicityDAO().findByWhereClause(pk,
          "eventId = " + eventId);
      if (list != null && list.size() > 0) {
        periodicity = (Periodicity) list.iterator().next();
      }
      return periodicity;
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getPeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_GET_PERIODICITY", e);
    }
  }

  public void removePeriodicity(Periodicity periodicity) {
    SilverTrace.info("almanach", "AlmanachBmEJB.removePeriodicity()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      IdPK pk = new IdPK();
      pk.setId(periodicity.getPK().getId());
      getEventPeriodicityDAO().remove(pk);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.removePeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_REMOVE_PERIODICITY", e);
    }
  }

  public void updatePeriodicity(Periodicity periodicity) {
    SilverTrace.info("almanach", "AlmanachBmEJB.updatePeriodicity()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      getEventPeriodicityDAO().update(periodicity);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.updatePeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_UPDATE_PERIODICITY", e);
    }
  }

  /**
   * @return
   */
  private SilverpeasBeanDAO getPeriodicityExceptionDAO() {
    if (periodicityExceptionDAO == null) {
      try {
        periodicityExceptionDAO = SilverpeasBeanDAOFactory
            .getDAO("com.stratelia.webactiv.almanach.model.PeriodicityException");
      } catch (PersistenceException pe) {
        throw new AlmanachRuntimeException(
            "AlmanachBmEJB.getPeriodicityExceptionDAO()",
            SilverpeasRuntimeException.ERROR,
            "almanach.EX_PERSISTENCE_PERIODICITY_EXCEPTION", pe);
      }
    }
    return periodicityExceptionDAO;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * addPeriodicityException
   * (com.stratelia.webactiv.almanach.model.PeriodicityException)
   */
  public void addPeriodicityException(PeriodicityException periodicityException) {
    SilverTrace.info("almanach", "AlmanachBmEJB.addPeriodicityException()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      IdPK pk = new IdPK();
      periodicityException.setPK(pk);
      getPeriodicityExceptionDAO().add(periodicityException);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException(
          "AlmanachBmEJB.addPeriodicityException()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_ADD_PERIODICITY_EXCEPTION", e);
    }
  }

  /**
   * @param periodicityId
   * @return
   */
  public Collection getListPeriodicityException(String periodicityId) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getListPeriodicityException()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      IdPK pk = new IdPK();
      return getPeriodicityExceptionDAO().findByWhereClause(pk,
          "periodicityId = " + periodicityId);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException(
          "AlmanachBmEJB.getListPeriodicityException()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_GET_PERIODICITY_EXCEPTION", e);
    }
  }

  /**
   * @param periodicityId
   */
  public void removeAllPeriodicityException(String periodicityId) {
    SilverTrace.info("almanach",
        "AlmanachBmEJB.removeAllPeriodicityException()",
        "root.MSG_GEN_ENTER_METHOD");

    try {
      IdPK pk = new IdPK();
      getPeriodicityExceptionDAO().removeWhere(pk,
          "periodicityId = " + periodicityId);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException(
          "AlmanachBmEJB.removeAllPeriodicityException()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_REMOVE_PERIODICITY_EXCEPTION", e);
    }
  }

  public Calendar getICal4jCalendar(Collection events, String language) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getICal4jCalendar()",
        "root.MSG_GEN_ENTER_METHOD");

    Iterator itEvent = events.iterator();

    Calendar calendarAlmanach = new Calendar();
    calendarAlmanach.getProperties().add(CalScale.GREGORIAN);
    
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    ResourceLocator almanachSettings = new ResourceLocator("com.stratelia.webactiv.almanach.settings.almanachSettings","");
    TimeZone localTimeZone = registry.getTimeZone(almanachSettings.getString("almanach.timezone"));

    // transformation des événements (EventDetail) en VEvent du Calendar ical4j
    EventDetail evtDetail;
    String eventId;
    String title;
    String descriptionWysiwyg;
    java.util.Date startDate;
    String startHour;
    java.util.Date endDate;
    String endHour;
    // String eventFatherId;
    java.util.Calendar calStartDate;
    java.util.Calendar calEndDate;
    VEvent eventIcal4jCalendar;
    Uid uid;
    // RecurrenceId recurrenceid;
    DateTime dtStart;
    Periodicity periodicity;
    DateTime dtEnd;
    Description description;

    while (itEvent.hasNext()) {
      evtDetail = (EventDetail) (itEvent.next());
      eventId = evtDetail.getPK().getId();
      title = evtDetail.getTitle();
      descriptionWysiwyg = evtDetail.getDescription(language);
      startDate = evtDetail.getStartDate();
      startHour = evtDetail.getStartHour();
      endDate = evtDetail.getEndDate();
      endHour = evtDetail.getEndHour();

      // Construction du VEvent du Calendar ical4j (pour gestion)
      calStartDate = java.util.Calendar.getInstance();
      calStartDate.setTime(startDate);
      if (StringUtil.isDefined(startHour)) {
        calStartDate.set(java.util.Calendar.HOUR_OF_DAY, DateUtil
            .extractHour(startHour));
        calStartDate.set(java.util.Calendar.MINUTE, DateUtil
            .extractMinutes(startHour));
      }
      calEndDate = java.util.Calendar.getInstance();
      calEndDate.setTime(startDate);
      if (endDate != null) {
        calEndDate.setTime(endDate);
        if (StringUtil.isDefined(endHour)) {
          calEndDate.set(java.util.Calendar.HOUR_OF_DAY, DateUtil
              .extractHour(endHour));
          calEndDate.set(java.util.Calendar.MINUTE, DateUtil
              .extractMinutes(endHour));
        }
      }

      dtStart = new DateTime(calStartDate.getTime());
      dtStart.setTimeZone(localTimeZone);
      dtEnd = new DateTime(calEndDate.getTime());
      dtEnd.setTimeZone(localTimeZone);
      eventIcal4jCalendar = new VEvent(dtStart, dtEnd, title);
      
      /*
       * if(eventFatherId != null && eventFatherId.length()>0) { //Occurence
       * spécifique d'un événement récurrent uid = new Uid(eventFatherId);
       * recurrenceid = new RecurrenceId(new Date(calStartDate.getTime()));
       * eventIcal4jCalendar.getProperties().add(recurrenceid); } else {
       */
      uid = new Uid(eventId);
      /* } */
      eventIcal4jCalendar.getProperties().add(uid);
      
      description = new Description(descriptionWysiwyg);
      eventIcal4jCalendar.getProperties().add(description);

      // Périodicité
      periodicity = getPeriodicity(eventId);

      if (periodicity != null) {

        eventIcal4jCalendar.getProperties().add(
            generateRecurrenceRule(periodicity));

        // Exceptions de périodicité
        eventIcal4jCalendar.getProperties().add(
            generateExceptionDate(periodicity));
      }

      calendarAlmanach.getComponents().add(eventIcal4jCalendar);
    }
    return calendarAlmanach;
  }

  public RRule generateRecurrenceRule(Periodicity periodicity) {
    String typeRecurence = Recur.DAILY;
    if (periodicity.getUnity() == Periodicity.UNITY_WEEK) {
      typeRecurence = Recur.WEEKLY;
    } else if (periodicity.getUnity() == Periodicity.UNITY_MONTH) {
      typeRecurence = Recur.MONTHLY;
    } else if (periodicity.getUnity() == Periodicity.UNITY_YEAR) {
      typeRecurence = Recur.YEARLY;
    }

    Recur recur = new Recur(typeRecurence, null);
    recur.setInterval(periodicity.getFrequency());

    if (Recur.WEEKLY.equals(typeRecurence)) {

      if (periodicity.getDaysWeekBinary().charAt(0) == '1') {// Monday
        recur.getDayList().add(WeekDay.MO);
      }
      if (periodicity.getDaysWeekBinary().charAt(1) == '1') {// Tuesday
        recur.getDayList().add(WeekDay.TU);
      }
      if (periodicity.getDaysWeekBinary().charAt(2) == '1') {
        recur.getDayList().add(WeekDay.WE);
      }
      if (periodicity.getDaysWeekBinary().charAt(3) == '1') {
        recur.getDayList().add(WeekDay.TH);
      }
      if (periodicity.getDaysWeekBinary().charAt(4) == '1') {
        recur.getDayList().add(WeekDay.FR);
      }
      if (periodicity.getDaysWeekBinary().charAt(5) == '1') {
        recur.getDayList().add(WeekDay.SA);
      }
      if (periodicity.getDaysWeekBinary().charAt(6) == '1') {
        recur.getDayList().add(WeekDay.SU);
      }

    } else if (Recur.MONTHLY.equals(typeRecurence)) {
      if (periodicity.getNumWeek() != 0) {// option choix du jour de la semaine
        if (periodicity.getDay() == java.util.Calendar.MONDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.MO, periodicity.getNumWeek()));
        } else if (periodicity.getDay() == java.util.Calendar.TUESDAY) {// Tuesday
          recur.getDayList().add(
              new WeekDay(WeekDay.TU, periodicity.getNumWeek()));
        } else if (periodicity.getDay() == java.util.Calendar.WEDNESDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.WE, periodicity.getNumWeek()));
        } else if (periodicity.getDay() == java.util.Calendar.THURSDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.TH, periodicity.getNumWeek()));
        } else if (periodicity.getDay() == java.util.Calendar.FRIDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.FR, periodicity.getNumWeek()));
        } else if (periodicity.getDay() == java.util.Calendar.SATURDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.SA, periodicity.getNumWeek()));
        } else if (periodicity.getDay() == java.util.Calendar.SUNDAY) {
          recur.getDayList().add(
              new WeekDay(WeekDay.SU, periodicity.getNumWeek()));
        }
      }
    }

    if (periodicity.getUntilDatePeriod() != null) {
      recur.setUntil(new Date(periodicity.getUntilDatePeriod()));
    }
    RRule rrule = new RRule(recur);
    return rrule;
  }

  public ExDate generateExceptionDate(Periodicity periodicity) {
    // Exceptions de périodicité
    Collection listException = getListPeriodicityException(periodicity.getPK()
        .getId());
    Iterator itException = (Iterator) listException.iterator();
    PeriodicityException periodicityException;
    DateList dateList = new DateList();
    java.util.Calendar calDateException = java.util.GregorianCalendar
        .getInstance();
    java.util.Calendar calDateFinException = java.util.GregorianCalendar
        .getInstance();
    while (itException.hasNext()) {
      periodicityException = (PeriodicityException) itException.next();
      calDateException.setTime(periodicityException.getBeginDateException());
      calDateFinException.setTime(periodicityException.getEndDateException());
      while (calDateException.before(calDateFinException)
          || calDateException.equals(calDateFinException)) {
        dateList.add(new Date(calDateException.getTime()));
        calDateException.add(java.util.Calendar.DATE, 1);
      }
    }
    ExDate exDate = new ExDate(dateList);
    return exDate;
  }

  /**
   * @param calendarAlmanach
   * @param currentDay
   * @param spaceId
   * @param instanceId
   * @return
   */
  public Collection getListRecurrentEvent(Calendar calendarAlmanach,
      java.util.Calendar currentDay, String spaceId, String instanceId) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getListRecurrentEvent()",
        "root.MSG_GEN_ENTER_METHOD");

    // transformation des VEvent du Calendar ical4j en EventDetail
    boolean isYear = false;
    if (currentDay == null) {
      currentDay = java.util.Calendar.getInstance();
      isYear = true;
    }
    java.util.Calendar firstDayMonth = currentDay;
    firstDayMonth.set(java.util.Calendar.DATE, 1);
    firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayMonth.set(java.util.Calendar.MINUTE, 0);
    firstDayMonth.set(java.util.Calendar.SECOND, 0);
    firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
    lastDayMonth.setTime(firstDayMonth.getTime());
    if (isYear) {
      lastDayMonth.add(java.util.Calendar.YEAR, 1);
    } else {
      lastDayMonth.add(java.util.Calendar.MONTH, 1);
    }
    lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayMonth.set(java.util.Calendar.MINUTE, 0);
    lastDayMonth.set(java.util.Calendar.SECOND, 0);
    lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    Period monthPeriod = new Period(new DateTime(firstDayMonth.getTime()),
        new DateTime(lastDayMonth.getTime()));

    ComponentList componentList = calendarAlmanach
        .getComponents(Component.VEVENT);
    Iterator<?> itVEvent = componentList.iterator();

    VEvent eventIcal4jCalendar;
    PeriodList periodList;
    Iterator<?> itPeriod;
    Period recurrencePeriod;
    String idEvent;
    List<EventDetail> events = new ArrayList<EventDetail>();
    while (itVEvent.hasNext()) {
      eventIcal4jCalendar = (VEvent) itVEvent.next();
      idEvent = eventIcal4jCalendar.getProperties().getProperty(Property.UID)
          .getValue();

      // Récupère l'événement
      EventDetail evtDetail = getEventDetail(new EventPK(idEvent, spaceId, instanceId));

      periodList = eventIcal4jCalendar.calculateRecurrenceSet(monthPeriod);
      itPeriod = periodList.iterator();
      while (itPeriod.hasNext()) {
        recurrencePeriod = (Period) itPeriod.next();

        // Modification des dates de l'EventDetail
        EventDetail copy = new EventDetail(evtDetail.getNameDescription(), evtDetail
            .getPK(), evtDetail.getPriority(), evtDetail.getTitle(), evtDetail
            .getStartHour(), evtDetail.getEndHour(), evtDetail.getPlace(),
            evtDetail.getEventUrl());

        copy.setStartDate(new java.util.Date(recurrencePeriod.getStart()
            .getTime()));
        copy
            .setEndDate(new java.util.Date(recurrencePeriod.getEnd().getTime()));

        events.add(copy);
      }
    }
	//Tri des Event par ordre de Date de début croissante
	Collections.sort(events, new Comparator<EventDetail>()
	{
	  public int compare(EventDetail event1, EventDetail event2)
	  {
	    return (event1.getStartDate().compareTo(event2.getStartDate()));
	  }
	  public boolean equals(EventDetail o)
	  {
	    return false;
	  }
	});
    return events;
  }

  /*****************************************************************************************************************/
  /** ContentManager utilization to use PDC **/
  /*****************************************************************************************************************/

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getSilverObjectId(com.stratelia.webactiv.almanach.model.EventPK)
   */
  public int getSilverObjectId(EventPK eventPK) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "eventPK = " + eventPK.toString());
    int silverObjectId = -1;
    EventDetail detail = null;
    try {
      silverObjectId = getAlmanachContentManager().getSilverObjectId(
          eventPK.getId(), eventPK.getComponentName());
      if (silverObjectId == -1) {
        detail = getEventDetail(eventPK);
        silverObjectId = createSilverContent(null, detail, detail
            .getDelegatorId());
      }
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  /**
   * @param con
   * @param eventDetail
   * @param creatorId
   * @return
   */
  private int createSilverContent(Connection con, EventDetail eventDetail,
      String creatorId) {
    SilverTrace
        .info("Almanach", "AlmanachBmEJB.createSilverContent()",
            "root.MSG_GEN_ENTER_METHOD", "eventId = "
                + eventDetail.getPK().getId());
    try {
      return getAlmanachContentManager().createSilverContent(con, eventDetail,
          creatorId);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param con
   * @param eventPK
   */
  private void deleteSilverContent(Connection con, EventPK eventPK) {
    SilverTrace.info("almanach", "AlmanachBmEJB.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "eventId = " + eventPK.getId());
    try {
      getAlmanachContentManager().deleteSilverContent(con, eventPK);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param eventDetail
   */
  private void updateSilverContentVisibility(EventDetail eventDetail) {
    try {
      getAlmanachContentManager().updateSilverContentVisibility(eventDetail);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @return
   */
  private AlmanachContentManager getAlmanachContentManager() {
    if (almanachContentManager == null) {
      almanachContentManager = new AlmanachContentManager();
    }
    return almanachContentManager;
  }

  /**************************************************************************************/
  /* Interface - Fichiers joints */
  /**************************************************************************************/

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getAttachments(com.stratelia.webactiv.almanach.model.EventPK)
   */
  public Collection getAttachments(EventPK eventPK) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "eventId = " + eventPK.getId());
    String ctx = "Images";
    AttachmentPK foreignKey = new AttachmentPK(eventPK.getId(), null, eventPK
        .getComponentName());
    SilverTrace.info("almanach", "AlmanachBmEJB.getAttachments()",
        "root.MSG_GEN_PARAM_VALUE", "foreignKey = " + foreignKey.toString());

    Connection con = null;
    try {
      con = getConnection();
      Collection attachmentList = AttachmentController
          .searchAttachmentByPKAndContext(foreignKey, ctx, con);
      SilverTrace.info("almanach", "AlmanachBmEJB.getAttachments()",
          "root.MSG_GEN_PARAM_VALUE", "attachmentList.size() = "
              + attachmentList.size());
      return attachmentList;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getAttachments()",
          SilverpeasRuntimeException.ERROR,
          "almanach.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", e);
    }
  }

  /**
   * @return
   */
  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getHTMLPath(com.stratelia.webactiv.almanach.model.EventPK)
   */
  public String getHTMLPath(EventPK eventPK) {
    String htmlPath = "";
    try {
      htmlPath = getSpacesPath(eventPK.getInstanceId())
          + getComponentLabel(eventPK.getInstanceId());
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getHTMLPath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION",
          e);
    }
    return htmlPath;
  }

  /**
   * @param componentId
   * @return
   */
  private String getSpacesPath(String componentId) {
    String spacesPath = "";
    List spaces = getOrganizationController().getSpacePathToComponent(
        componentId);
    Iterator iSpaces = spaces.iterator();
    SpaceInst spaceInst = null;
    while (iSpaces.hasNext()) {
      spaceInst = (SpaceInst) iSpaces.next();
      spacesPath += spaceInst.getName();
      spacesPath += " > ";
    }
    return spacesPath;
  }

  /**
   * @param componentId
   * @return
   */
  private String getComponentLabel(String componentId) {
    ComponentInstLight component = getOrganizationController()
        .getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null)
      componentLabel = component.getLabel();
    return componentLabel;
  }

  /**
   * @return
   */
  private OrganizationController getOrganizationController() {
    OrganizationController orga = new OrganizationController();
    return orga;
  }

  /**
   * ejb methods
   */
  public void ejbCreate() throws CreateException {
    SilverTrace.info("almanach", "AlmanachBmEJB.ejbCreate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  public void ejbRemove() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.info("almanach", "AlmanachBmEJB.ejbRemove()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  public void ejbActivate() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.info("almanach", "AlmanachBmEJB.ejbActivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  public void ejbPassivate() throws javax.ejb.EJBException,
      java.rmi.RemoteException {
    SilverTrace.info("almanach", "AlmanachBmEJB.ejbPassivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  public void setSessionContext(final javax.ejb.SessionContext p1)
      throws javax.ejb.EJBException, java.rmi.RemoteException {
    SilverTrace.info("almanach", "AlmanachBmEJB.setSessionContext()",
        "root.MSG_GEN_ENTER_METHOD");
  }

}