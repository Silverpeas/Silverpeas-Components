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
package com.stratelia.webactiv.almanach.control.ejb;

import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.AlmanachContentManager;
import com.stratelia.webactiv.almanach.model.EventDAO;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
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
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.silverpeas.upload.UploadedFile;
import org.silverpeas.wysiwyg.control.WysiwygController;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.silverpeas.util.StringUtil.isDefined;
import static com.stratelia.webactiv.util.DateUtil.*;

@Stateless(name = "Almanach", description
    = "Stateless session bean to manage the almanach component.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class AlmanachBmEJB implements AlmanachBm {

  private static final long serialVersionUID = -8559479482209447676L;
  private static final ResourceLocator settings = new ResourceLocator(
      "org.silverpeas.almanach.settings.almanachSettings", "");
  private AlmanachContentManager almanachContentManager = null;
  private SilverpeasBeanDAO<Periodicity> eventPeriodicityDAO = null;
  private SilverpeasBeanDAO<PeriodicityException> periodicityExceptionDAO = null;
  private EventDAO eventDAO = new EventDAO();

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getAllEvents(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
  public Collection<EventDetail> getAllEvents(EventPK pk) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getAllEvents()", "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<EventDetail> events = getEventDAO().findAllEvents(pk.getInstanceId());
      return events;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getAllEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL", e);
    }
  }

  /**
   * Get all events
   *
   * @param pk
   * @param instanceIds array of instanceId
   * @return Collection of Events
   */
  @Override
  public Collection<EventDetail> getAllEvents(EventPK pk, String[] instanceIds) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getAllEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      String[] almanachIds = Arrays.copyOf(instanceIds, instanceIds.length + 1);
      almanachIds[instanceIds.length] = pk.getInstanceId();
      Collection<EventDetail> events = getEventDAO().findAllEvents(almanachIds);
      return events;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getAllEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL",
          e);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getEvents(java.util.Collection)
   */
  @Override
  public Collection<EventDetail> getEvents(Collection<EventPK> pks) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getEvents()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Collection<EventDetail> events = getEventDAO().findAllEventsByPK(pks);
      return events;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_EVENTS_FAIL", e);
    }
  }

  /**
   * Get Event Detail
   *
   * @param pk
   * @return the corresponding event.
   */
  @Override
  public EventDetail getEventDetail(EventPK pk) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getEventDetail()", "root.MSG_GEN_ENTER_METHOD");
    try {
      return getEventDAO().findEventByPK(pk);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getEventDetail()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_EVENT_DETAIL_FAIL", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * addEvent(com.stratelia.webactiv.almanach.model.EventDetail)
   */
  @Override
  public String addEvent(EventDetail event, Collection<UploadedFile> uploadedFiles) {
    return addEvent(event, uploadedFiles, PdcClassification.NONE_CLASSIFICATION);
  }

  @Override
  public String addEvent(EventDetail event, Collection<UploadedFile> uploadedFiles,
      PdcClassification classification) {
    SilverTrace.info("almanach", "AlmanachBmEJB.addEvent()", "root.MSG_GEN_ENTER_METHOD");
    checkEventDates(event);
    Connection connection = null;
    try {
      connection = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      String id = getEventDAO().addEvent(connection, event);
      event.setPK(new EventPK(id, event.getPK()));

      // manage periodicity
      if (event.getPeriodicity() != null) {
        Periodicity periodicity = event.getPeriodicity();
        periodicity.setEventId(Integer.parseInt(id));
        // Add the periodicity
        addPeriodicity(periodicity);
      }
      createSilverContent(connection, event, event.getCreatorId());
      if (!classification.isEmpty()) {
        PdcClassificationService service = PdcServiceFactory.getFactory().
            getPdcClassificationService();
        classification.ofContent(event.getId());
        service.classifyContent(event, classification);
      }
      WysiwygController.createUnindexedFileAndAttachment(event.getDescription(event.getLanguage()),
          event.getPK(), event.getDelegatorId(), event.getLanguage());
      // Attach uploaded files
      if (CollectionUtil.isNotEmpty(uploadedFiles)) {
        for (UploadedFile uploadedFile : uploadedFiles) {
          // Register attachment
          uploadedFile.registerAttachment(event.getPK(), event.getLanguage(), false);
        }
      }
      createIndex(event);
      return id;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.addEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_ADD_EVENT_FAIL", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * updateEvent() update the event entry, specified by the pk, in the database
   */
  @Override
  public void updateEvent(EventDetail event) {
    SilverTrace.info("almanach", "AlmanachBmEJB.updateEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    checkEventDates(event);
    try {
      getEventDAO().updateEvent(event);

      Periodicity previousPeriodicity = getPeriodicity(event.getPK().getId());
      Periodicity currentPeriodicity = event.getPeriodicity();
      if (previousPeriodicity == null) {
        if (currentPeriodicity != null) {

          // Add the periodicity
          currentPeriodicity.setEventId(new Integer(event.getPK().getId()).intValue());
          addPeriodicity(currentPeriodicity);
        }
      } else {// lastPeriodicity != null
        if (currentPeriodicity == null) {
          // Remove the periodicity and Exceptions
          removePeriodicity(previousPeriodicity);
        } else {
          // Update the periodicity
          currentPeriodicity.setPK(previousPeriodicity.getPK());
          currentPeriodicity.setEventId(Integer.parseInt(event.getPK().getId()));
          updatePeriodicity(currentPeriodicity);
        }
      }

      createIndex(event);
      updateSilverContentVisibility(event);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.updateEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_UPDATE_EVENT_FAIL", e);
    }
  }

  /**
   * removeEvent() remove the Event entry specified by the pk
   */
  @Override
  public void removeEvent(EventPK pk) {
    SilverTrace.info("almanach", "AlmanachBmEJB.removeEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection connection = null;
    try {
      connection = DBUtil.makeConnection(JNDINames.ALMANACH_DATASOURCE);
      // remove periodicity and periodicity exceptions
      Periodicity periodicity = getPeriodicity(pk.getId());
      if (periodicity != null) {
        removeAllPeriodicityException(periodicity.getPK().getId());
        removePeriodicity(periodicity);
      }
      getEventDAO().removeEvent(connection, pk);
      deleteIndex(pk);
      deleteSilverContent(connection, pk);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.removeEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_REMOVE_EVENT_FAIL", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * createIndex(com.stratelia.webactiv.almanach.model.EventDetail)
   */
  @Override
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
  private FullIndexEntry updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry,
      EventDetail eventDetail) {
    EventPK eventPK = eventDetail.getPK();
    if (eventPK != null) {
      SilverTrace.info("almanach", "AlmanachBmEJB.updateIndexEntryWithWysiwygContent()",
          "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry + ", eventPK = " + eventPK);
      WysiwygController.addToIndex(indexEntry, new ForeignPK(eventPK), eventDetail.getLanguage());
    }

    return indexEntry;
  }

  /**
   * @param eventPK
   */
  private void deleteIndex(EventPK eventPK) {
    SilverTrace.info("almanach", "AlmanachBmEJB.deleteIndex()", "almanach.MSG_GEN_ENTER_METHOD",
        "PK=" + eventPK.toString());
    IndexEntryPK indexEntry = new IndexEntryPK(eventPK.getComponentName(), "Event", eventPK.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  /**
   * @return
   */
  private SilverpeasBeanDAO<Periodicity> getEventPeriodicityDAO() {
    if (eventPeriodicityDAO == null) {
      try {
        eventPeriodicityDAO = SilverpeasBeanDAOFactory.getDAO(
            "com.stratelia.webactiv.almanach.model.Periodicity");
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
  private void addPeriodicity(Periodicity periodicity) {
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
  private Periodicity getPeriodicity(String eventId) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getPeriodicity()",
        "root.MSG_GEN_ENTER_METHOD");
    Periodicity periodicity = null;
    try {
      IdPK pk = new IdPK();
      Collection<?> list = getEventPeriodicityDAO().findByWhereClause(pk,
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

  private void removePeriodicity(Periodicity periodicity) {
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

  private void updatePeriodicity(Periodicity periodicity) {
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
  private SilverpeasBeanDAO<PeriodicityException> getPeriodicityExceptionDAO() {
    if (periodicityExceptionDAO == null) {
      try {
        periodicityExceptionDAO = SilverpeasBeanDAOFactory.getDAO(
            "com.stratelia.webactiv.almanach.model.PeriodicityException");
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
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * addPeriodicityException (com.stratelia.webactiv.almanach.model.PeriodicityException)
   */
  @Override
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
  public Collection<PeriodicityException> getListPeriodicityException(String periodicityId) {
    SilverTrace.info("almanach",
        "AlmanachBmEJB.getListPeriodicityException()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      IdPK pk = new IdPK();
      return getPeriodicityExceptionDAO().findByWhereClause(pk, "periodicityId = " + periodicityId);
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

  @Override
  public Calendar getICal4jCalendar(Collection<EventDetail> events, String language) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getICal4jCalendar()",
        "root.MSG_GEN_ENTER_METHOD");
    Calendar calendarAlmanach = new Calendar();
    calendarAlmanach.getProperties().add(CalScale.GREGORIAN);
    for (EventDetail evtDetail : events) {
      Periodicity periodicity = evtDetail.getPeriodicity();
      ExDate exceptionDates = null;
      if (periodicity != null) {
        evtDetail.setPeriodicity(periodicity);
        exceptionDates = generateExceptionDate(periodicity);
      }
      VEvent eventIcal4jCalendar = evtDetail.icalConversion(exceptionDates);
      calendarAlmanach.getComponents().add(eventIcal4jCalendar);
    }
    return calendarAlmanach;
  }

  public RRule generateRecurrenceRule(Periodicity periodicity) {
    return periodicity.generateRecurrenceRule();
  }

  public ExDate generateExceptionDate(Periodicity periodicity) {
    // Exceptions de périodicité
    Collection<PeriodicityException> listException = getListPeriodicityException(periodicity.getPK()
        .getId());
    Iterator<PeriodicityException> itException = listException.iterator();
    PeriodicityException periodicityException;
    DateList dateList = new DateList();
    java.util.Calendar calDateException = java.util.Calendar.getInstance();
    java.util.Calendar calDateFinException = java.util.Calendar.getInstance();
    while (itException.hasNext()) {
      periodicityException = (PeriodicityException) itException.next();
      calDateException.setTime(periodicityException.getBeginDateException());
      calDateFinException.setTime(periodicityException.getEndDateException());
      while (calDateException.before(calDateFinException)
          || calDateException.equals(calDateFinException)) {
        dateList.add(new DateTime(calDateException.getTime()));
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
  @Override
  public Collection<EventDetail> getListRecurrentEvent(Calendar calendarAlmanach,
      java.util.Calendar currentDay, String spaceId, String instanceId, boolean yearScope) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getListRecurrentEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    java.util.Calendar today = currentDay;

    // transformation des VEvent du Calendar ical4j en EventDetail
    boolean isYear = false;
    if (currentDay == null) {
      today = java.util.Calendar.getInstance();
      isYear = true;
    }

    java.util.Calendar firstDayMonth = java.util.Calendar.getInstance();
    firstDayMonth.set(java.util.Calendar.YEAR, today.get(java.util.Calendar.YEAR));
    firstDayMonth.set(java.util.Calendar.MONTH, today.get(java.util.Calendar.MONTH));
    firstDayMonth.set(java.util.Calendar.DATE, 1);
    firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayMonth.set(java.util.Calendar.MINUTE, 0);
    firstDayMonth.set(java.util.Calendar.SECOND, 0);
    firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    if (yearScope) {
      firstDayMonth.set(java.util.Calendar.MONTH, 0);
    }

    SilverTrace.info("almanach", "AlmanachBmEJB.getListRecurrentEvent()",
        "root.MSG_GEN_PARAM_VALUE", "start = " + firstDayMonth.getTime());

    java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
    lastDayMonth.setTime(firstDayMonth.getTime());
    if (yearScope) {
      lastDayMonth.add(java.util.Calendar.YEAR, 1);
    } else {
      if (isYear) {
        lastDayMonth.add(java.util.Calendar.YEAR, 1);
      } else {
        lastDayMonth.add(java.util.Calendar.MONTH, 1);
      }
    }
    lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayMonth.set(java.util.Calendar.MINUTE, 0);
    lastDayMonth.set(java.util.Calendar.SECOND, 0);
    lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);

    SilverTrace.info("almanach", "AlmanachBmEJB.getListRecurrentEvent()",
        "root.MSG_GEN_PARAM_VALUE", "end = " + lastDayMonth.getTime());

    Period monthPeriod = new Period(new DateTime(firstDayMonth.getTime()),
        new DateTime(lastDayMonth.getTime()));

    ComponentList componentList = calendarAlmanach.getComponents(Component.VEVENT);
    Iterator<VEvent> itVEvent = componentList.iterator();

    List<EventDetail> events = new ArrayList<EventDetail>();
    while (itVEvent.hasNext()) {
      VEvent eventIcal4jCalendar = itVEvent.next();
      String idEvent = eventIcal4jCalendar.getProperties().getProperty(Property.UID).getValue();
      // Récupère l'événement
      EventDetail evtDetail = getEventDetail(new EventPK(idEvent, spaceId, instanceId));

      PeriodList periodList = eventIcal4jCalendar.calculateRecurrenceSet(monthPeriod);
      Iterator<Period> itPeriod = periodList.iterator();
      while (itPeriod.hasNext()) {
        Period recurrencePeriod = itPeriod.next();
        // Modification des dates de l'EventDetail
        EventDetail copy = new EventDetail(evtDetail.getPK(), evtDetail.getTitle(),
            new Date(recurrencePeriod.getStart().getTime()),
            new Date(recurrencePeriod.getEnd().getTime()));
        copy.setPriority(evtDetail.getPriority());
        copy.setNameDescription(evtDetail.getNameDescription());
        copy.setStartHour(evtDetail.getStartHour());
        copy.setEndHour(evtDetail.getEndHour());
        copy.setPlace(evtDetail.getPlace());
        copy.setEventUrl(evtDetail.getEventUrl());
        events.add(copy);
      }
    }
    // Tri des Event par ordre de Date de début croissante
    Collections.sort(events, new Comparator<EventDetail>() {
      @Override
      public int compare(EventDetail event1, EventDetail event2) {
        return (event1.getStartDate().compareTo(event2.getStartDate()));
      }
    });
    return events;
  }

  /**
   * **************************************************************************************************************
   */
  /**
   * ContentManager utilization to use PDC *
   */
  /**
   * **************************************************************************************************************
   */

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getSilverObjectId(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
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
        silverObjectId = createSilverContent(null, detail, detail.getDelegatorId());
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
    SilverTrace.info("Almanach", "AlmanachBmEJB.createSilverContent()",
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

  /**
   * ***********************************************************************************
   */
  /* Interface - Fichiers joints */
  /**
   * ***********************************************************************************
   */

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getAttachments(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
  public Collection<SimpleDocument> getAttachments(EventPK eventPK) {
    SilverTrace.info("almanach", "AlmanachBmEJB.getAttachments()",
        "root.MSG_GEN_ENTER_METHOD", "eventId = " + eventPK.getId());
    try {
      Collection<SimpleDocument> attachmentList = AttachmentServiceFactory.getAttachmentService().
          listDocumentsByForeignKey(eventPK, null);
      SilverTrace.info("almanach", "AlmanachBmEJB.getAttachments()", "root.MSG_GEN_PARAM_VALUE",
          "attachmentList.size() = " + attachmentList.size());
      return attachmentList;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getAttachments()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", e);
    }
  }


  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getHTMLPath(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
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
    List<SpaceInst> spaces = getOrganizationController().getSpacePathToComponent(
        componentId);
    Iterator<SpaceInst> iSpaces = spaces.iterator();
    SpaceInst spaceInst = null;
    while (iSpaces.hasNext()) {
      spaceInst = iSpaces.next();
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
    ComponentInstLight component = getOrganizationController().getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  /**
   * @return
   */
  private OrganizationController getOrganizationController() {
    return new OrganizationController();
  }

  @Override
  public List<EventOccurrence> getEventOccurrencesInPeriod(org.silverpeas.date.Period period,
      String... almanachIds) {
    try {
      Collection<EventDetail> events = getEventDAO().findAllEventsInPeriod(period, almanachIds);
      EventOccurrenceGenerator occurrenceGenerator = EventOccurrenceGeneratorFactory.getFactory().
          getEventOccurrenceGenerator();
      return occurrenceGenerator
          .generateOccurrencesInPeriod(period, new ArrayList<EventDetail>(events));
    } catch (Exception e) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getEventOccurrencesInPeriod()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL", e);
    }
  }

  @Override
  public List<EventOccurrence> getNextEventOccurrences(String... almanachIds) {
    List<EventOccurrence> occurrences;
    try {
      com.silverpeas.calendar.Date today = today();
      java.util.Calendar endDate = java.util.Calendar.getInstance();
      String upToDay = null;
      int numberOfMonths = getAlmanachSettings().getInteger("almanach.nextEvents.windowtime", 0);
      if (numberOfMonths > 0) {
        endDate.add(java.util.Calendar.MONTH, numberOfMonths);
        upToDay = date2SQLDate(endDate.getTime());
      }
      Collection<EventDetail> events = getEventDAO().findAllEventsInRange(date2SQLDate(today),
          upToDay, almanachIds);

      EventOccurrenceGenerator occurrenceGenerator = EventOccurrenceGeneratorFactory.getFactory().
          getEventOccurrenceGenerator();
      if (numberOfMonths > 0) {
        com.silverpeas.calendar.Date endDay = new com.silverpeas.calendar.Date(endDate.getTime());
        occurrences = occurrenceGenerator.generateOccurrencesInRange(today, endDay,
            new ArrayList<EventDetail>(events));
      } else {
        occurrences = occurrenceGenerator.generateOccurrencesFrom(today, new ArrayList<EventDetail>(
            events));
      }
    } catch (Exception ex) {
      throw new AlmanachRuntimeException("AlmanachBmEJB.getEventOccurrencesInWeek()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL",
          ex);
    }
    return occurrences;
  }

  protected EventDAO getEventDAO() {
    return this.eventDAO;
  }

  protected com.silverpeas.calendar.Date today() {
    return com.silverpeas.calendar.Date.today();
  }

  private void checkEventDates(final EventDetail event) {
    if (event.getEndDate().before(event.getStartDate())) {
      throw new IllegalArgumentException("The event ends before its start!");
    }
    if (event.getStartDate().equals(event.getEndDate()) && isDefined(event.getEndHour())
        && isDefined(event.getStartHour())) {
      int endHour = extractHour(event.getEndHour());
      int endMinute = extractMinutes(event.getEndHour());
      int startHour = extractHour(event.getStartHour());
      int startMinute = extractMinutes(event.getStartHour());
      if (startHour > endHour || (startHour == endHour && startMinute > endMinute)) {
        throw new IllegalArgumentException("The event ends before its start!");
      }
    }
  }

  private ResourceLocator getAlmanachSettings() {
    return settings;
  }
}
