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
package com.stratelia.webactiv.almanach.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.alertUser.AlertUser;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBadParamException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachNoSuchFindEventException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.almanach.model.Periodicity;
import com.stratelia.webactiv.almanach.model.PeriodicityException;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.Event;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendar;
import com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.MonthCalendarWA1;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.RemoveException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;

/**
 * @author squere
 * @version
 */
public class AlmanachSessionController extends AbstractComponentSessionController {

  public AlmanachBm almanachBm = null;
  private Calendar currentDay = Calendar.getInstance();
  private net.fortuna.ical4j.model.Calendar currentICal4jCalendar;
  private EventDetail currentEvent;
  private static final String AE_MSG1 = "almanach.ASC_NoSuchFindEvent";
  // Almanach Agregation
  private String[] agregatedAlmanachsIds = null;
  private static final String ALMANACHS_IN_SUBSPACES = "0";
  private static final String ALMANACHS_IN_SPACE_AND_SUBSPACES = "1";
  private static final String ALL_ALMANACHS = "2";
  private static final String ACCESS_ALL = "0";
  private static final String ACCESS_SPACE = "1";
  private static final String ACCESS_NONE = "3";
  private Map<String, String> colors = null;
  private OrganizationController organizationController = new OrganizationController();

  public AlmanachSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.almanach.multilang.almanach", null,
        "com.stratelia.webactiv.almanach.settings.almanachSettings");
  }

  public Calendar getCurrentDay() {
    return currentDay;
  }

  public void setCurrentDay(Date date) {
    currentDay.setTime(date);
  }

  public EventDetail getCurrentEvent() {
    return currentEvent;
  }

  public void setCurrentEvent(EventDetail event) {
    this.currentEvent = event;
  }

  public void nextMonth() {
    currentDay.add(Calendar.MONTH, 1);
  }

  public void previousMonth() {
    currentDay.add(Calendar.MONTH, -1);
  }

  public void today() {
    currentDay = Calendar.getInstance();
  }

  /**
   * @return
   * @throws AlmanachException
   * @throws RemoteException
   * @author David Lesimple
   */
  public Collection<EventDetail> getMonthEvents(String[] instanceIds)
      throws AlmanachException, RemoteException {
    SilverTrace.info("almanach", "AlmanachSessionController.getMonthEvents()",
        "root.MSG_GEN_ENTER_METHOD", "instanceIds=" + instanceIds);
    List<EventDetail> events = (List<EventDetail>) getAlmanachBm().getMonthEvents(
        new EventPK("", getSpaceId(), getComponentId()),
        getCurrentDay().getTime(), instanceIds);

    if (events != null) {
      SilverTrace.info("almanach",
          "AlmanachSessionController.getMonthEvents()",
          "root.MSG_GEN_PARAM_VALUE", "# of events = " + events.size());
    }

    // tri
    EventDetailBeginDateComparatorAsc comparateur = new EventDetailBeginDateComparatorAsc();
    Collections.sort(events, comparateur);

    SilverTrace.info("almanach", "AlmanachSessionController.getMonthEvents()",
        "root.MSG_GEN_PARAM_VALUE", "# of events after sorting = "
            + events.size());

    return events;
  }

  /**
   * @return
   * @throws AlmanachException
   * @throws RemoteException
   */
  public Collection<EventDetail> getAllEvents() throws AlmanachException, RemoteException {
    EventPK pk = new EventPK("", getSpaceId(), getComponentId());
    return getAlmanachBm().getAllEvents(pk);
  }

  /**
   * @return
   * @throws AlmanachException
   * @throws RemoteException
   */
  public Collection<EventDetail> getAllEventsAgregation() throws AlmanachException,
      RemoteException {
    if (isAgregationUsed()) {
      return getAllEvents(getAgregatedAlmanachs());
    } else {
      return getAllEvents();
    }
  }

  private Collection<EventDetail> getAllEvents(String[] instanceIds)
      throws AlmanachException, RemoteException {
    EventPK pk = new EventPK("", getSpaceId(), getComponentId());
    return getAlmanachBm().getAllEvents(pk, instanceIds);
  }

  /**
   * @param id
   * @return
   * @throws AlmanachException
   * @throws AlmanachNoSuchFindEventException
   * @throws RemoteException
   */
  public EventDetail getEventDetail(String id) throws AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    EventDetail detail = getAlmanachBm().getEventDetail(
        new EventPK(id, getSpaceId(), getComponentId()));
    if (detail != null) {
      return detail;
    } else {
      throw new AlmanachNoSuchFindEventException(AE_MSG1);
    }
  }

  /**
   * Delete event
   * @param id
   * @throws AlmanachException
   * @throws RemoteException
   * @throws UtilException
   */
  public void removeEvent(String id) throws AlmanachException, RemoteException, UtilException {
    SilverTrace.info("almanach", "AlmanachSessionController.removeEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    EventPK pk = new EventPK(id, getSpaceId(), getComponentId());
    // remove event from DB
    getAlmanachBm().removeEvent(pk);
    // remove attachments from filesystem
    AttachmentController.deleteAttachmentByCustomerPK(pk);
    // Delete the Wysiwyg if exists
    if (WysiwygController.haveGotWysiwyg(getSpaceId(), getComponentId(), id)) {
      FileFolderManager.deleteFile(WysiwygController.getWysiwygPath(getComponentId(), id));
    }

    // Suppression du VEvent du Calendar ical4j (pour gestion)
    if (this.getCurrentICal4jCalendar() == null) {
      // initialisation d'un Calendar ical4j
      net.fortuna.ical4j.model.Calendar calendarAlmanach = this.getICal4jCalendar(this.
          getAllEventsAgregation());
      this.setCurrentICal4jCalendar(calendarAlmanach);
    }
    ComponentList listCompo = this.getCurrentICal4jCalendar().getComponents();
    Iterator<VEvent> it = listCompo.iterator();
    VEvent eventIcal4jCalendar = null;
    while (it.hasNext()) {
      eventIcal4jCalendar = it.next();
      if (id.equals(eventIcal4jCalendar.getProperties().getProperty(
          Property.UID).getValue())) {
        break;
      }
    }
    this.getCurrentICal4jCalendar().getComponents().remove(eventIcal4jCalendar);

    SilverTrace.info("almanach", "AlmanachSessionController.removeEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Delete an occurence of event
   * @param event
   * @param dateDebutException
   * @param dateFinException
   * @throws ParseException
   * @throws RemoteException
   * @throws AlmanachException
   */
  public void removeOccurenceEvent(EventDetail event,
      String dateDebutException, String dateFinException)
      throws ParseException, RemoteException, AlmanachException {
    SilverTrace.info("almanach",
        "AlmanachSessionController.removeOccurenceEvent()",
        "root.MSG_GEN_ENTER_METHOD");

    PeriodicityException periodicityException = new PeriodicityException();
    periodicityException.setPeriodicityId(new Integer(event.getPeriodicity().getPK().getId()).
        intValue());
    periodicityException.setBeginDateException(DateUtil.parse(dateDebutException));
    periodicityException.setEndDateException(DateUtil.parse(dateFinException));

    // add exception periodicity in DB
    getAlmanachBm().addPeriodicityException(periodicityException);

    // Ajout de l'Exception de périodicité dans le VEvent du Calendar ical4j
    // (pour gestion)
    if (this.getCurrentICal4jCalendar() == null) {
      // initialisation d'un Calendar ical4j
      net.fortuna.ical4j.model.Calendar calendarAlmanach = this.getICal4jCalendar(this.
          getAllEventsAgregation());
      this.setCurrentICal4jCalendar(calendarAlmanach);
    }
    ComponentList listCompo = this.getCurrentICal4jCalendar().getComponents();
    Iterator<VEvent> it = listCompo.iterator();
    VEvent eventIcal4jCalendar = null;
    while (it.hasNext()) {
      eventIcal4jCalendar = it.next();
      if (event.getId().equals(
          eventIcal4jCalendar.getProperties().getProperty(Property.UID).getValue())) {
        break;
      }
    }
    eventIcal4jCalendar.getProperties().add(generateExceptionDate(event.getPeriodicity()));
    SilverTrace.info("almanach", "AlmanachSessionController.removeOccurenceEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Add event
   * @param event
   * @throws AlmanachBadParamException
   * @throws AlmanachException
   * @throws RemoteException
   * @throws WysiwygException
   */
  public void addEvent(EventDetail event) throws AlmanachBadParamException, AlmanachException,
      RemoteException, WysiwygException {
    SilverTrace.info("almanach", "AlmanachSessionController.addEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      event.setPK(new EventPK("", getSpaceId(), getComponentId()));
      event.setDelegatorId(getUserId());
      // Add the event
      String eventId = getAlmanachBm().addEvent(event);
      Date startDate = event.getStartDate();
      // currentDay
      if (startDate != null) {
        getCurrentDay().setTime(startDate);
      }
      // Add the wysiwyg content
      WysiwygController.createFileAndAttachment(event.getDescription(getLanguage()), getSpaceId(),
          getComponentId(), eventId);
      EventDetail savedEvent = getAlmanachBm().getEventDetail(new EventPK(eventId, getSpaceId(),
          getComponentId()));
      VEvent eventIcal4jCalendar = savedEvent.icalConversion(null);
      if (this.getCurrentICal4jCalendar() == null) {
        // initialisation d'un Calendar ical4j
        net.fortuna.ical4j.model.Calendar calendarAlmanach = this.getICal4jCalendar(this.
            getAllEventsAgregation());
        this.setCurrentICal4jCalendar(calendarAlmanach);
      }
      this.getCurrentICal4jCalendar().getComponents().add(eventIcal4jCalendar);

    } catch (RemoteException e) {
      throw new AlmanachRuntimeException(
          "AlmanachSessionController.addEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_ADD_EVENT_FAIL", e);
    }
    SilverTrace.info("almanach", "AlmanachSessionController.addEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Update event
   * @param event
   * @throws AlmanachBadParamException
   * @throws AlmanachException
   * @throws RemoteException
   */
  public void updateEvent(EventDetail event) throws AlmanachBadParamException, AlmanachException,
      RemoteException, WysiwygException {
    SilverTrace.info("almanach", "AlmanachSessionController.updateEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      event.getPK().setSpace(getSpaceId());
      event.getPK().setComponentName(getComponentId());

      // Update event
      getAlmanachBm().updateEvent(event);

      Date startDate = event.getStartDate();
      String startHour = event.getStartHour();
      Date endDate = event.getEndDate();
      String endHour = event.getEndHour();

      // currentDay
      if (startDate != null) {
        getCurrentDay().setTime(startDate);
      }

      // Update the Wysiwyg if exists, create one otherwise
      if (StringUtil.isDefined(event.getWysiwyg())) {
        WysiwygController.updateFileAndAttachment(event.getDescription(getLanguage()),
            getSpaceId(),
            getComponentId(),
            event.getId(), getUserId());
      } else {
        WysiwygController.createFileAndAttachment(event.getDescription(getLanguage()),
            getSpaceId(),
            getComponentId(),
            event.getId());
      }

      // Mise à jour du VEvent du Calendar ical4j (pour gestion)
      Calendar calStartDate = Calendar.getInstance();
      calStartDate.setTime(startDate);
      if (StringUtil.isDefined(startHour)) {
        calStartDate.set(Calendar.HOUR_OF_DAY, DateUtil.extractHour(startHour));
        calStartDate.set(Calendar.MINUTE, DateUtil.extractMinutes(startHour));
      }
      Calendar calEndDate = Calendar.getInstance();
      calEndDate.setTime(startDate);
      if (endDate != null) {
        calEndDate.setTime(endDate);
        if (StringUtil.isDefined(endHour)) {
          calEndDate.set(Calendar.HOUR_OF_DAY, DateUtil.extractHour(endHour));
          calEndDate.set(Calendar.MINUTE, DateUtil.extractMinutes(endHour));
        }
      }

      // retrouve l'event en question
      if (this.getCurrentICal4jCalendar() == null) {
        // initialisation d'un Calendar ical4j
        net.fortuna.ical4j.model.Calendar calendarAlmanach = this.getICal4jCalendar(this.
            getAllEventsAgregation());
        this.setCurrentICal4jCalendar(calendarAlmanach);
      }
      ComponentList listCompo = this.getCurrentICal4jCalendar().getComponents();
      Iterator<VEvent> it = listCompo.iterator();
      VEvent eventIcal4jCalendar = null;
      boolean ok = false;
      while (it.hasNext() && !ok) {
        eventIcal4jCalendar = it.next();
        if (event.getPK().getId().equals(
            eventIcal4jCalendar.getProperties().getProperty(Property.UID).getValue())) {
          ok = true;
        }
      }

      if (ok) {
        eventIcal4jCalendar.getProperties().remove(Property.DTSTART);
        DtStart dtStart = new DtStart(new net.fortuna.ical4j.model.Date(calStartDate.getTime()));
        eventIcal4jCalendar.getProperties().add(dtStart);
        eventIcal4jCalendar.getProperties().remove(Property.DTEND);
        DtEnd dtEnd = new DtEnd(new net.fortuna.ical4j.model.Date(calEndDate.getTime()));
        eventIcal4jCalendar.getProperties().add(dtEnd);

        // Périodicité
        Periodicity lastPeriodicity = getAlmanachBm().getPeriodicity(
            event.getPK().getId());
        Periodicity periodicity = event.getPeriodicity();
        eventIcal4jCalendar.getProperties().remove(Property.RRULE);

        if (lastPeriodicity == null) {
          if (periodicity != null) {

            // Add the periodicity
            periodicity.setEventId(new Integer(event.getPK().getId()).intValue());
            getAlmanachBm().addPeriodicity(periodicity);
            eventIcal4jCalendar.getProperties().add(generateRecurrenceRule(periodicity));
          }
        } else {// lastPeriodicity != null
          if (periodicity == null) {
            // Remove the periodicity and Exceptions
            getAlmanachBm().removePeriodicity(lastPeriodicity);
          } else {
            // Update the periodicity
            periodicity.setPK(lastPeriodicity.getPK());
            periodicity.setEventId(Integer.parseInt(event.getPK().getId()));
            getAlmanachBm().updatePeriodicity(periodicity);

            // Mise à jour du VEvent du Calendar ical4j (pour gestion)
            eventIcal4jCalendar.getProperties().add(
                generateRecurrenceRule(periodicity));
          }
        }
      }
    } catch (RemoteException e) {
      throw new AlmanachRuntimeException("AlmanachSessionController.addEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_UPDATE_EVENT_FAIL", e);
    }
    SilverTrace.info("almanach", "AlmanachSessionController.updateEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * @param event
   * @throws AlmanachException
   * @throws RemoteException
   */
  public void indexEvent(EventDetail event) throws AlmanachException,
      RemoteException {
    getAlmanachBm().createIndex(event);
  }

  /**
   * @return
   */
  public CompoSpace[] getAlmanachInstances() {
    return getOrganizationController().getCompoForUser(getUserId(), "almanach");
  }

  private AlmanachBm getAlmanachBm() throws AlmanachException {
    if (almanachBm == null) {
      try {
        almanachBm = ((AlmanachBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class)).create();
      } catch (Exception e) {
        throw new AlmanachException("AlmanachSessionControl.getAlmanachBm()",
            SilverpeasException.ERROR, "almanach.EX_EJB_CREATION_FAIL", e);
      }
    }
    return almanachBm;
  }

  /**
   * @param mode
   * @param bCompleteMonth
   * @return
   */
  public String buildPdf(String mode) {
    String name = "almanach" + (new Date()).getTime() + ".pdf";

    try {
      AlmanachPdfGenerator.buildPdf(name, this, mode);
    } catch (AlmanachRuntimeException ex) {
      SilverTrace.warn("almanach", "AlmanachSessionController.buildPdf()",
          "almanach.MSG_BUILD_PDF_FAIL", ex);
      return null;
    }

    return FileServerUtils.getUrlToTempDir(name);
  }

  /**
   * return the MonthCalendar Object
   * @return
   */
  public MonthCalendar getMonthCalendar() {
    int numbersDays = 7;
    if (isWeekendNotVisible()) {
      numbersDays = 5;
    }

    return (new MonthCalendarWA1(getLanguage(), numbersDays));
  }

  /**
   * @return
   */
  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    return "yes".equals(parameterValue.toLowerCase());
  }

  // AJOUT : pour traiter l'affichage des semaines sur 5 ou 7 jours
  /**
   * @return
   */
  public boolean isWeekendNotVisible() {
    String parameterValue = getComponentParameterValue("weekendNotVisible");
    return "yes".equals(parameterValue.toLowerCase());
  }

  /**
   * @return
   */
  private boolean isUseRss() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("rss"));
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.silverpeas.peasCore.AbstractComponentSessionController#getRSSUrl ()
   */
  @Override
  public String getRSSUrl() {
    if (isUseRss()) {
      return super.getRSSUrl();
    }
    return null;
  }

  /**
   * @return
   * @author dlesimple
   */
  public boolean isAgregationUsed() {
    String parameterValue = getComponentParameterValue("useAgregation");
    return "yes".equalsIgnoreCase(parameterValue);
  }

  private String getAccessPolicy() {
    String param = getComponentParameterValue("directAccess");
    if (!StringUtil.isDefined(param)) {
      return ACCESS_ALL;
    }
    return param;
  }

  public List<List<String>> getAccessibleInstances() {
    if (ACCESS_NONE.equals(getAccessPolicy())) {
      return null;
    }

    boolean inCurrentSpace = true;
    boolean inAllSpaces = ACCESS_ALL.equals(getAccessPolicy());

    // Get almanachIds
    String[] instanceIds = organizationController.getAllComponentIdsRecur(
        getSpaceId(), getUserId(), getComponentRootName(), inCurrentSpace, inAllSpaces);

    SilverTrace.info("almanach", "AlmanachSessionController.getAccessibleInstances()",
        "root.MSG_GEN_PARAM_VALUE", "instanceIds=" + instanceIds + " spaceId=" + getSpaceId());
    List<List<String>> almanachs = null;
    if (instanceIds.length > 1) // exclude this instance
    {
      for (int i = 0; i < instanceIds.length; i++) {
        SilverTrace.info("almanach", "AlmanachSessionController.getAccessibleInstances()",
            "root.MSG_GEN_PARAM_VALUE", "instanceId=" + instanceIds[i]);
        List<String> almanach = new ArrayList<String>();

        ComponentInstLight almanachInst = organizationController.getComponentInstLight(
            instanceIds[i]);

        boolean keepIt = false;
        if (ACCESS_SPACE.equals(getAccessPolicy())) {
          keepIt = almanachInst.getDomainFatherId().equals(getSpaceId());
        } else {
          keepIt = true;
        }

        if (keepIt) {
          almanach.add(instanceIds[i]);
          almanach.add(almanachInst.getLabel());

          SpaceInstLight si = organizationController.getSpaceInstLightById(almanachInst.
              getDomainFatherId());
          almanach.add(si.getName());

          if (almanachs == null) {
            almanachs = new ArrayList<List<String>>();
          }
          almanachs.add(almanach);
        }
      }
    }
    return almanachs;
  }

  /**
   * @param objectId
   * @return
   * @throws AlmanachBadParamException
   * @throws AlmanachException
   * @throws RemoteException
   */
  public int getSilverObjectId(String objectId)
      throws AlmanachBadParamException, AlmanachException, RemoteException {
    return getAlmanachBm().getSilverObjectId(new EventPK(objectId, getSpaceId(), getComponentId()));
  }

  /**
   * Get the color of the almanach
   * @author dlesimple
   * @param instanceId
   * @return color of almanach
   */
  public String getAlmanachColor(String instanceId) {
    if (colors == null) {
      colors = new Hashtable<String, String>();
      ArrayList<List<String>> almanachs = getOthersAlmanachs();
      if (almanachs != null) {
        for (Iterator<List<String>> iterator = almanachs.iterator(); iterator.hasNext();) {
          List<String> almanach = iterator.next();
          colors.put(almanach.get(0), almanach.get(2));
        }
      }
    }
    return (String) colors.get(instanceId);
  }

  /**
   * Get the others almanachs
   * @author dlesimple
   * @return ArrayList of ArrayList (with almanachId, almanachLabel, almanachColor)
   */
  public ArrayList<List<String>> getOthersAlmanachs() {
    boolean inCurrentSpace = false;
    boolean inAllSpaces = false;

    String agregationMode = SilverpeasSettings.readString(getSettings(),
        "almanachAgregationMode", ALMANACHS_IN_SUBSPACES);

    // Array of almanach(id, label, color)
    ArrayList<List<String>> almanachs = new ArrayList<List<String>>();
    String[] instanceIds = null;
    if (agregationMode.equals(ALMANACHS_IN_SPACE_AND_SUBSPACES)) {
      inCurrentSpace = true;
    } else if (agregationMode.equals(ALL_ALMANACHS)) {
      inCurrentSpace = true;
      inAllSpaces = true;
    }

    // Get almanachIds
    instanceIds = organizationController.getAllComponentIdsRecur(getSpaceId(),
        getUserId(), getComponentRootName(), inCurrentSpace, inAllSpaces);
    SilverTrace.info("almanach",
        "AlmanachSessionController.getOthersAlmanachs()",
        "root.MSG_GEN_PARAM_VALUE", "instanceIds=" + instanceIds + " spaceId="
            + getSpaceId());
    for (int i = 0; i < instanceIds.length; i++) {
      SilverTrace.info("almanach",
          "AlmanachSessionController.getOthersAlmanachs()",
          "root.MSG_GEN_PARAM_VALUE", "instanceId=" + instanceIds[i]);
      ArrayList<String> almanach = new ArrayList<String>();
      if (!instanceIds[i].equals(getComponentId())) {
        ComponentInstLight almanachInst = organizationController.getComponentInstLight(
            instanceIds[i]);
        almanach.add(instanceIds[i]);
        almanach.add(almanachInst.getLabel());
        almanach.add(getAlmanachColor(i));
        almanachs.add(almanach);
      }
    }
    return almanachs;
  }

  /**
   * Get agregated almanachs
   * @author dlesimple
   * @return String[] of almanachIds
   */
  public String[] getAgregatedAlmanachs() {
    return agregatedAlmanachsIds;
  }

  /**
   * Return if an almanach is agregated
   * @author dlesimple
   * @param instanceId
   * @return boolean
   */
  public boolean isAlmanachAgregated(String instanceId) {
    boolean isAgregated = false;
    if (agregatedAlmanachsIds != null) {
      for (int i = 0; i < agregatedAlmanachsIds.length && !isAgregated; i++) {
        if (agregatedAlmanachsIds[i].equals(instanceId)) {
          isAgregated = true;
        }
      }
    }
    return isAgregated;
  }

  /**
   * Set almanachs to be agregated
   * @author dlesimple
   * @param String [] of instanceIds
   */
  private void setAgregatedAlmanachs(String[] instancesIds) {
    agregatedAlmanachsIds = new String[instancesIds.length];
    for (int i = 0; i < instancesIds.length; i++) {
      agregatedAlmanachsIds[i] = instancesIds[i];
    }
  }

  /**
   * Delete agregated almanachs
   * @author dlesimple
   */
  private void deleteAgregatedAlmanachs() {
    agregatedAlmanachsIds = null;
  }

  /**
   * Update list of agregated almanachs
   * @author dlesimple
   * @param String [] of instanceIds
   */
  public void updateAgregatedAlmanachs(String[] instanceIds) {
    if (instanceIds != null) {
      for (int i = 0; i < instanceIds.length; i++) {
        setAgregatedAlmanachs(instanceIds);
      }
    } else {
      deleteAgregatedAlmanachs();
    }
  }

  /**
   * Get the color of an almanach
   * @author dlesimple
   * @param position in the array
   * @return almanachColor
   */
  private String getAlmanachColor(int pos) {
    String almanachColor = SilverpeasSettings.readString(getSettings(),
        "almanachColor" + (pos + 1), "");

    SilverTrace.info("almanach", "AlmanachSessionController.getAlmanachColor",
        "root.MSG_GEN_PARAM_VALUE", " color=" + almanachColor);
    return almanachColor;
  }

  /**
   * @param eventId
   * @return
   * @throws RemoteException
   * @throws AlmanachException
   * @throws AlmanachNoSuchFindEventException
   */
  public String initAlertUser(String eventId) throws RemoteException, AlmanachException,
      AlmanachNoSuchFindEventException {
    AlertUser sel = getAlertUser();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentId(getComponentId());
    PairObject hostComponentName = new PairObject(getComponentLabel(), null);
    sel.setHostComponentName(hostComponentName);
    SilverTrace.debug("almanach", "AlmanachSessionController.initAlertUser()",
        "root.MSG_GEN_PARAM_VALUE", "name = " + hostComponentName
            + " componentId=" + getComponentId());
    sel.setNotificationMetaData(getAlertNotificationEvent(eventId));
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  private synchronized NotificationMetaData getAlertNotificationEvent(
      String eventId) throws RemoteException, AlmanachException, AlmanachNoSuchFindEventException {
    // création des données ...
    EventPK eventPK = new EventPK(eventId, getSpaceId(), getComponentId());
    String senderName = getUserDetail().getDisplayedName();
    EventDetail eventDetail = getAlmanachBm().getEventDetail(eventPK);
    SilverTrace.debug("alamanch",
        "AlamanachSessionController.getAlertNotificationEvent()",
        "root.MSG_GEN_PARAM_VALUE", "event = " + eventDetail.toString());

    // recherche de l’emplacement de l’évènement
    String htmlPath = getAlmanachBm().getHTMLPath(eventPK);

    // création des messages ...
    ResourceLocator message = new ResourceLocator(
        "com.stratelia.webactiv.almanach.multilang.almanach", "fr");
    ResourceLocator message_en = new ResourceLocator(
        "com.stratelia.webactiv.almanach.multilang.almanach", "en");

    // notifications en français
    String subject = getNotificationSubject(message);
    String body = getNotificationBody(eventDetail, htmlPath, message,
        senderName);
    SilverTrace.debug("almanach",
        "AlamanachSessionController.getAlertNotificationEvent()",
        "root.MSG_GEN_PARAM_VALUE", "message = " + message.toString()
            + " message_en = " + message_en.toString());
    SilverTrace.debug("almanach",
        "AlamanachSessionController.getAlertNotificationEvent()",
        "root.MSG_GEN_PARAM_VALUE", "sujet = " + subject + " corps = " + body);

    // english notifications
    String subject_en = getNotificationSubject(message_en);
    String body_en = getNotificationBody(eventDetail, htmlPath, message_en,
        senderName);
    SilverTrace.debug("almanach",
        "AlmanachSessionController.getAlertNotificationEvent()",
        "root.MSG_GEN_PARAM_VALUE", "sujet_en = " + subject_en + " corps_en = "
            + body_en);

    // création des notifications
    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, subject, body);
    notifMetaData.addLanguage("en", subject_en, body_en);
    notifMetaData.setLink(getObjectUrl(eventDetail));
    notifMetaData.setComponentId(eventPK.getInstanceId());
    notifMetaData.setSender(getUserId());

    return notifMetaData;
  }

  private String getNotificationSubject(ResourceLocator message) {
    return message.getString("notifSubject");
  }

  private String getNotificationBody(EventDetail eventDetail, String htmlPath,
      ResourceLocator message, String senderName) {
    StringBuilder messageText = new StringBuilder();
    messageText.append(senderName).append(" ");
    messageText.append(message.getString("notifInfo")).append(" ").append(
        eventDetail.getName()).append(" ");
    messageText.append(message.getString("notifInfo2")).append("\n\n");
    messageText.append(message.getString("path")).append(" : ").append(htmlPath);
    return messageText.toString();
  }

  private String getObjectUrl(EventDetail eventDetail) {
    return URLManager.getURL(null, getComponentId()) + eventDetail.getURL();
  }

  @Override
  public void close() {
    try {
      if (almanachBm != null) {
        almanachBm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("almanachSession", "AlmanachSessionController.close",
          "", e);
    } catch (RemoveException e) {
      SilverTrace.error("almanachSession", "AlmanachSessionController.close",
          "", e);
    }
  }

  /**
   * @return
   */
  public net.fortuna.ical4j.model.Calendar getCurrentICal4jCalendar() {
    return this.currentICal4jCalendar;
  }

  /**
   * @param currentICal4jCalendar
   */
  public void setCurrentICal4jCalendar(net.fortuna.ical4j.model.Calendar currentICal4jCalendar) {
    this.currentICal4jCalendar = currentICal4jCalendar;
  }

  /**
   * @param events
   * @return
   * @throws RemoteException
   * @throws AlmanachException
   */
  public net.fortuna.ical4j.model.Calendar getICal4jCalendar(Collection<EventDetail> events) throws
      RemoteException,
      AlmanachException {
    return getAlmanachBm().getICal4jCalendar(events, getLanguage());

  }

  /**
   * @param id
   * @return
   * @throws AlmanachException
   * @throws AlmanachNoSuchFindEventException
   * @throws RemoteException
   */
  public EventDetail getCompleteEventDetail(String id)
      throws AlmanachException, AlmanachNoSuchFindEventException, RemoteException {
    EventDetail detail = getAlmanachBm().getEventDetail(
        new EventPK(id, getSpaceId(), getComponentId()));
    if (detail != null) {
      Periodicity periodicity = getAlmanachBm().getPeriodicity(id);
      detail.setPeriodicity(periodicity);
      return detail;
    } else {
      throw new AlmanachNoSuchFindEventException(AE_MSG1);
    }
  }

  /**
   * Update event occurence (cas particulier de modification d'une occurence d'événement périodique)
   * @param event
   * @param dateDebutIteration
   * @param dateFinIteration
   * @throws AlmanachBadParamException
   * @throws AlmanachException
   * @throws RemoteException
   * @throws WysiwygException
   * @throws ParseException
   */
  public void updateEventOccurence(EventDetail event,
      String dateDebutIteration, String dateFinIteration)
      throws AlmanachBadParamException, AlmanachException, RemoteException, WysiwygException,
      ParseException {
    SilverTrace.info("almanach",
        "AlmanachSessionController.updateEventOccurence()",
        "root.MSG_GEN_ENTER_METHOD");

    // Supprime l'occurence : exception dans la série
    removeOccurenceEvent(event, dateDebutIteration, dateFinIteration);

    // Ajoute un nouvel événement indépendant
    event.setPeriodicity(null);
    addEvent(event);

    SilverTrace.info("almanach",
        "AlmanachSessionController.updateEventOccurence()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private RRule generateRecurrenceRule(Periodicity periodicity)
      throws RemoteException, AlmanachException {
    return getAlmanachBm().generateRecurrenceRule(periodicity);
  }

  private ExDate generateExceptionDate(Periodicity periodicity)
      throws RemoteException, AlmanachException {
    // Exceptions de périodicité
    return getAlmanachBm().generateExceptionDate(periodicity);

  }

  /**
   * @return
   * @throws AlmanachException
   * @throws RemoteException
   */
  public Collection<EventDetail> getListRecurrentEvent() throws RemoteException, AlmanachException {
    return getListRecurrentEvent(false);
  }

  public Collection<EventDetail> getListRecurrentEvent(boolean yearScope) throws RemoteException,
      AlmanachException {
    // Récupère le Calendar ical4j
    net.fortuna.ical4j.model.Calendar calendarAlmanach = getCurrentICal4jCalendar();

    return getAlmanachBm().getListRecurrentEvent(calendarAlmanach,
        getCurrentDay(), getSpaceId(), getComponentId(), yearScope);

  }

  public List<Event> listCurrentMonthEvents() throws AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    List<Event> events = new ArrayList<Event>();
    // transformation des VEvent du Calendar ical4j en Event du MonthCalendar
    java.util.Calendar firstDayMonth = java.util.Calendar.getInstance();
    firstDayMonth.set(java.util.Calendar.YEAR, getCurrentDay().get(java.util.Calendar.YEAR));
    firstDayMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
    firstDayMonth.set(java.util.Calendar.MONTH, getCurrentDay().get(java.util.Calendar.MONTH));
    firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayMonth.set(java.util.Calendar.MINUTE, 0);
    firstDayMonth.set(java.util.Calendar.SECOND, 0);
    firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
    lastDayMonth.set(java.util.Calendar.YEAR, getCurrentDay().get(java.util.Calendar.YEAR));
    lastDayMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
    lastDayMonth.set(java.util.Calendar.MONTH, getCurrentDay().get(java.util.Calendar.MONTH));
    lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayMonth.set(java.util.Calendar.MINUTE, 0);
    lastDayMonth.set(java.util.Calendar.SECOND, 1);
    lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    lastDayMonth.add(java.util.Calendar.MONTH, 1);
    Period monthPeriod = new Period(new DateTime(firstDayMonth.getTime()),
        new DateTime(lastDayMonth.getTime()));

    ComponentList componentList = this.currentICal4jCalendar.getComponents(Component.VEVENT);
    Iterator<VEvent> itVEvent = componentList.iterator();
    while (itVEvent.hasNext()) {
      VEvent eventIcal4jCalendar = itVEvent.next();
      String idEvent = eventIcal4jCalendar.getProperties().getProperty(Property.UID).getValue();
      // Récupère l'événement
      EventDetail evtDetail = getEventDetail(idEvent);
      PeriodList periodList = eventIcal4jCalendar.calculateRecurrenceSet(monthPeriod);
      Iterator<Period> itPeriod = periodList.iterator();
      while (itPeriod.hasNext()) {
        Period recurrencePeriod = itPeriod.next();
        // Construction de l'Event du MonthCalendar (pour affichage)
        Event evt = new Event(idEvent, evtDetail.getName(), new java.util.Date(
            recurrencePeriod.getStart().getTime()), new java.util.Date(
            recurrencePeriod.getEnd().getTime()), evtDetail.getURL(),
            evtDetail.getPriority());
        evt.setStartHour(evtDetail.getStartHour());
        evt.setEndHour(evtDetail.getEndHour());
        evt.setPlace(evtDetail.getPlace());
        if (isAgregationUsed()) {
          evt.setColor(getAlmanachColor(evtDetail.getInstanceId()));
        }
        evt.setInstanceId(evtDetail.getInstanceId());
        events.add(evt);
      }
    }
    return events;
  }
}
