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

import com.stratelia.webactiv.util.GeneralPropertiesManager;
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
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.RemoveException;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import static com.stratelia.webactiv.almanach.control.CalendarViewType.*;

/**
 * The AlmanachSessionController provides features to handle almanachs and theirs events.
 * A such object wraps in fact the current almanach in the user session; in others words, the
 * almanach the user works currently with. As the almanach is displayed in a given window time,
 * the AlmanachSessionController instance maintains the current opened window time and provides
 * a way to move this window front or back in the time.
 */
public class AlmanachSessionController extends AbstractComponentSessionController {

  private AlmanachBm almanachBm = null;
  private Calendar currentDay = Calendar.getInstance();
  private EventDetail currentEvent;
  private static final String AE_MSG1 = "almanach.ASC_NoSuchFindEvent";
  // Almanach Agregation
  private List<String> agregatedAlmanachsIds = new ArrayList<String>();
  private static final String ALMANACHS_IN_SUBSPACES = "0";
  private static final String ALMANACHS_IN_SPACE_AND_SUBSPACES = "1";
  private static final String ALL_ALMANACHS = "2";
  private static final String ACCESS_ALL = "0";
  private static final String ACCESS_SPACE = "1";
  private static final String ACCESS_NONE = "3";
  private Map<String, String> colors = null;
  private CalendarViewType viewMode = MONTHLY;
  private OrganizationController organizationController = new OrganizationController();

  /**
   * Constructs a new AlmanachSessionController instance.
   * @param mainSessionCtrl the main session controller of the user.
   * @param context the context of the almanach component.
   */
  public AlmanachSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context,
        "com.stratelia.webactiv.almanach.multilang.almanach",
        "com.stratelia.webactiv.almanach.settings.almanachIcons",
        "com.stratelia.webactiv.almanach.settings.almanachSettings");
  }

  /**
   * Gets the current day in the current window in time.
   * @return the current day.
   */
  public Date getCurrentDay() {
    return currentDay.getTime();
  }

  /**
   * Sets explicitly the new current day.
   * @param date the date of the new current day.
   */
  public void setCurrentDay(Date date) {
    currentDay.setTime(date);
  }

  /**
   * Gets the current event, selected by the user.
   * @return the detail about the current selected event or null if no event is selected.
   */
  public EventDetail getCurrentEvent() {
    return currentEvent;
  }

  /**
   * Sets the current event the user has selected.
   * @param event the detail of the current selected event.
   */
  public void setCurrentEvent(EventDetail event) {
    this.currentEvent = event;
  }

  /**
   * Moves the window in time to the next calendar view according to the current view mode.
   */
  public void nextView() {
    switch(viewMode) {
      case MONTHLY:
        currentDay.add(Calendar.MONTH, 1);
        break;
      case WEEKLY:
        currentDay.add(Calendar.WEEK_OF_MONTH, 1);
        break;
    }

  }

  /**
   * Moves the window in time to the previous calendar view according to the current view mode.
   */
  public void previousView() {
    switch(viewMode) {
      case MONTHLY:
        currentDay.add(Calendar.MONTH, -1);
        break;
      case WEEKLY:
        currentDay.add(Calendar.WEEK_OF_MONTH, -1);
        break;
    }
  }

  /**
   * Moves the window in time in a such way the current day is now today.
   */
  public void today() {
    currentDay = Calendar.getInstance();
  }

  /**
   * Sets the current view mode of the almanach rendering.
   * @param viewMode the view mode (monthly, weekly, ...).
   */
  public void setViewMode(final CalendarViewType viewMode) {
    this.viewMode = viewMode;
  }

  /**
   * Gets all events of the underlying almanach.
   * @return a list with the details of the events registered in the almanach.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public List<EventDetail> getAllEvents() throws AlmanachException, RemoteException {
    EventPK pk = new EventPK("", getSpaceId(), getComponentId());
    return new ArrayList<EventDetail>(getAlmanachBm().getAllEvents(pk));
  }

  /**
   * Gets all events of the agregation of the current almanach and others agregated ones.
   * @return a list with the details of the all events in the agregation of several almanachs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public List<EventDetail> getAllAgregationEvents() throws AlmanachException,
      RemoteException {
    if (isAgregationUsed()) {
      return getAllEvents(agregatedAlmanachsIds);
    } else {
      return getAllEvents();
    }
  }

  /**
   * Gets the count of almanachs agregated with the undermying one.
   * @return the number of agregated almanachs.
   */
  public int getAgregatedAlmanachsCount() {
    return agregatedAlmanachsIds.size();
  }

  /**
   * Gets the events of the specified almanachs.
   * @param instanceIds the identifiers of the almanachs.
   * @return a list with the details of the events in the specified almanachs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  private List<EventDetail> getAllEvents(final List<String> instanceIds)
      throws AlmanachException, RemoteException {
    EventPK pk = new EventPK("", getSpaceId(), getComponentId());
    return new ArrayList<EventDetail>(getAlmanachBm().getAllEvents(pk,
        instanceIds.toArray(new String[instanceIds.size()])));
  }

  /**
   * Gets the detail of the event identified by the specified identifier.
   * @param id the unique identifier of the event to get.
   * @return the detail of the event.
   * @throws AlmanachException if an error occurs while getting the detail of the event.
   * @throws AlmanachNoSuchFindEventException if no event exists with a such identifier.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public EventDetail getEventDetail(final String id) throws AlmanachException,
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
   * Removes the event identified by the specified identifier.
   * @param id the identifier of the event to remove.
   * @throws AlmanachException if an error occurs while removing the event.
   * @throws RemoteException if the communication with the remote business object fails.
   * @throws UtilException if an error occurs while getting the WYSIWYG content of the event.
   */
  public void removeEvent(final String id) throws AlmanachException, RemoteException, UtilException {
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

    SilverTrace.info("almanach", "AlmanachSessionController.removeEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Removes just an occurrence of the specified event. The occurrence is identified by its start
   * and end date.
   * @param eventDetail the detail of the event to which the occurrence belongs.
   * @param startDate the start date of the event occurrence.
   * @param endDate the end date of the event occurrence.
   * @throws ParseException if an error occurs while parsing date infomation.
   * @throws RemoteException if the communication with the remote business object fails.
   * @throws AlmanachException if an error occurs while removing the occurrence of the event.
   */
  public void removeOccurenceEvent(EventDetail eventDetail,
      String startDate, String endDate)
      throws ParseException, RemoteException, AlmanachException {
    SilverTrace.info("almanach",
        "AlmanachSessionController.removeOccurenceEvent()",
        "root.MSG_GEN_ENTER_METHOD");

    PeriodicityException periodicityException = new PeriodicityException();
    periodicityException.setPeriodicityId(new Integer(eventDetail.getPeriodicity().getPK().getId()).
        intValue());
    periodicityException.setBeginDateException(DateUtil.parse(startDate));
    periodicityException.setEndDateException(DateUtil.parse(endDate));

    // add exception periodicity in DB
    getAlmanachBm().addPeriodicityException(periodicityException);
    SilverTrace.info("almanach", "AlmanachSessionController.removeOccurenceEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Adds the specified event into the underlying almanach.
   * @param eventDetail the detail of the event to add.
   * @throws AlmanachBadParamException if the event detail isn't well defined.
   * @throws AlmanachException if an error occurs while adding the event.
   * @throws WysiwygException if an error occurs while parsing the WYSIWYG content of the event.
   */
  public void addEvent(EventDetail eventDetail) throws AlmanachBadParamException, AlmanachException,
      WysiwygException {
    SilverTrace.info("almanach", "AlmanachSessionController.addEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      eventDetail.setPK(new EventPK("", getSpaceId(), getComponentId()));
      eventDetail.setDelegatorId(getUserId());
      // Add the event
      String eventId = getAlmanachBm().addEvent(eventDetail);
      Date startDate = eventDetail.getStartDate();
      // currentDay
      if (startDate != null) {
        setCurrentDay(startDate);
      }
      // Add the wysiwyg content
      WysiwygController.createFileAndAttachment(eventDetail.getDescription(getLanguage()),
          getSpaceId(),
          getComponentId(), eventId);
      getAlmanachBm().getEventDetail(new EventPK(eventId, getSpaceId(),
          getComponentId()));
    } catch (RemoteException e) {
      throw new AlmanachRuntimeException(
          "AlmanachSessionController.addEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_ADD_EVENT_FAIL", e);
    }
    SilverTrace.info("almanach", "AlmanachSessionController.addEvent()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Updates the specified event into the underlying almanach.
   * @param eventDetail the detail of the event to update.
   * @throws AlmanachBadParamException if the event detail isn't well defined.
   * @throws AlmanachException if an error occurs while updating the event.
   * @throws WysiwygException if an error occurs while parsing the WYSIWYG content of the event.
   */
  public void updateEvent(EventDetail eventDetail) throws AlmanachBadParamException,
      AlmanachException,
      WysiwygException {
    SilverTrace.info("almanach", "AlmanachSessionController.updateEvent()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      eventDetail.getPK().setSpace(getSpaceId());
      eventDetail.getPK().setComponentName(getComponentId());

      // Update event
      getAlmanachBm().updateEvent(eventDetail);

      Date startDate = eventDetail.getStartDate();
      String startHour = eventDetail.getStartHour();
      Date endDate = eventDetail.getEndDate();
      String endHour = eventDetail.getEndHour();

      // currentDay
      if (startDate != null) {
        setCurrentDay(startDate);
      }

      // Update the Wysiwyg if exists, create one otherwise
      if (StringUtil.isDefined(eventDetail.getWysiwyg())) {
        WysiwygController.updateFileAndAttachment(eventDetail.getDescription(getLanguage()),
            getSpaceId(),
            getComponentId(),
            eventDetail.getId(), getUserId());
      } else {
        WysiwygController.createFileAndAttachment(eventDetail.getDescription(getLanguage()),
            getSpaceId(),
            getComponentId(),
            eventDetail.getId());
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

      net.fortuna.ical4j.model.Calendar calendarAlmanach = this.getICal4jCalendar(this.
          getAllAgregationEvents());
      ComponentList listCompo = calendarAlmanach.getComponents();
      VEvent eventIcal4jCalendar = null;
      boolean ok = false;
      for (Object event : listCompo) {
        eventIcal4jCalendar = (VEvent) event;
        if (eventDetail.getPK().getId().equals(
            eventIcal4jCalendar.getProperties().getProperty(Property.UID).getValue())) {
          ok = true;
          break;
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
            eventDetail.getPK().getId());
        Periodicity periodicity = eventDetail.getPeriodicity();
        eventIcal4jCalendar.getProperties().remove(Property.RRULE);

        if (lastPeriodicity == null) {
          if (periodicity != null) {

            // Add the periodicity
            periodicity.setEventId(new Integer(eventDetail.getPK().getId()).intValue());
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
            periodicity.setEventId(Integer.parseInt(eventDetail.getPK().getId()));
            getAlmanachBm().updatePeriodicity(periodicity);
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
   * Indexes the specified event for the Silverpeas search engine.
   * @param event the detail of the event to index.
   * @throws AlmanachException if an error occurs while indexing the event.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public void indexEvent(EventDetail event) throws AlmanachException,
      RemoteException {
    getAlmanachBm().createIndex(event);
  }

  /**
   * Gets the remote business object for handling almanachs and events.
   * @return the remote business object.
   * @throws AlmanachException if an error occurs while getting the remote object.
   */
  protected AlmanachBm getAlmanachBm() throws AlmanachException {
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
   * Sets a specific reference to a remote Almanach business object
   * @param anAlmanachBm the reference to a remote business object.
   */
  protected void setAlmanachBm(final AlmanachBm anAlmanachBm) {
    this.almanachBm = anAlmanachBm;
  }

  /**
   * Builds a PDF document with the events of the underlying almanach and that satisfy the specified
   * criteria key.
   * @param mode the criteria key.
   * @return the content of the PDF document as a String.
   */
  public String buildPdf(final String mode) {
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
   * @return
   */
  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    return "yes".equals(parameterValue.toLowerCase());
  }

  /**
   * Is the weekend is taken in charge by the current underlying almanach?
   * @return true if the weekend should be displayed for the current almanach, false otherwise.
   */
  public boolean isWeekendNotVisible() {
    String parameterValue = getComponentParameterValue("weekendNotVisible");
    return "yes".equals(parameterValue.toLowerCase());
  }

  /**
   * Is RSS information exists for the events in the current underlying almanach?
   * @return true if the RSS stream is supported for the current almanach, false otherwise.
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
   * Is the agregation is activated for the current underlying almanach?
   * @return true if the agregation is used for the current almanach, false otherwise.
   */
  public boolean isAgregationUsed() {
    String parameterValue = getComponentParameterValue("useAgregation");
    return "yes".equalsIgnoreCase(parameterValue);
  }

  /**
   * Gets policy currently in use to access to the data of the current almanach.
   * @return the policy identifier.
   */
  private String getAccessPolicy() {
    String param = getComponentParameterValue("directAccess");
    if (!StringUtil.isDefined(param)) {
      return ACCESS_ALL;
    }
    return param;
  }

  public List<AlmanachDTO> getAccessibleInstances() {
    List<AlmanachDTO> accessibleInstances = new ArrayList<AlmanachDTO>();

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
    if (instanceIds.length > 1) {
      for (String instanceId : instanceIds) {
        SilverTrace.info("almanach", "AlmanachSessionController.getAccessibleInstances()",
            "root.MSG_GEN_PARAM_VALUE", "instanceId=" + instanceId);
        ComponentInstLight almanachInst = organizationController.getComponentInstLight(
            instanceId);

        boolean keepIt = false;
        if (ACCESS_SPACE.equals(getAccessPolicy())) {
          keepIt = almanachInst.getDomainFatherId().equals(getSpaceId());
        } else {
          keepIt = true;
        }

        if (keepIt) {
          SpaceInstLight si = organizationController.getSpaceInstLightById(almanachInst.
              getDomainFatherId());
          String url = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")
              + URLManager.getURL(null, instanceId);
          AlmanachDTO almanach = new AlmanachDTO()
              .setInstanceId(instanceId)
              .setLabel(almanachInst.getLabel())
              .setSpaceId(si.getName())
              .setUrl(url);
          accessibleInstances.add(almanach);
        }
      }
    }
    return accessibleInstances;
  }

  /**
   * @param objectId
   * @return
   * @throws AlmanachBadParamException
   * @throws AlmanachException
   * @throws RemoteException
   */
  public int getSilverObjectId(final String objectId)
      throws AlmanachBadParamException, AlmanachException, RemoteException {
    return getAlmanachBm().getSilverObjectId(new EventPK(objectId, getSpaceId(), getComponentId()));
  }

  /**
   * Get the color of the almanach
   * @author dlesimple
   * @param instanceId
   * @return color of almanach
   */
  public String getAlmanachColor(final String instanceId) {
    if (colors == null) {
      colors = new HashMap<String, String>();
      List<AlmanachDTO> almanachs = getOthersAlmanachs();
      if (almanachs != null) {
        for (AlmanachDTO almanach : almanachs) {
          colors.put(almanach.getInstanceId(), almanach.getColor());
        }
      }
    }
    return colors.get(instanceId);
  }

  /**
   * Gets all almanachs others than the current one in the session.
   * @return a list of AlmanachDTO instances, each of them carrying some data about an almanach.
   */
  public List<AlmanachDTO> getOthersAlmanachs() {
    List<AlmanachDTO> othersAlmanachs = new ArrayList<AlmanachDTO>();

    String agregationMode = SilverpeasSettings.readString(getSettings(),
        "almanachAgregationMode", ALMANACHS_IN_SUBSPACES);
    String[] instanceIds = null;
    boolean inCurrentSpace = false;
    boolean inAllSpaces = false;
    if (agregationMode.equals(ALMANACHS_IN_SPACE_AND_SUBSPACES)) {
      inCurrentSpace = true;
    } else if (agregationMode.equals(ALL_ALMANACHS)) {
      inCurrentSpace = true;
      inAllSpaces = true;
    }
    instanceIds = organizationController.getAllComponentIdsRecur(getSpaceId(),
        getUserId(), getComponentRootName(), inCurrentSpace, inAllSpaces);
    SilverTrace.debug("almanach",
        "AlmanachSessionController.getOthersAlmanachs()",
        "root.MSG_GEN_PARAM_VALUE", "instanceIds=" + instanceIds + " spaceId="
        + getSpaceId());
    for (int i = 0; i < instanceIds.length; i++) {
      String instanceId = instanceIds[i];
      if (!instanceId.equals(getComponentId())) {
        ComponentInstLight almanachInst = organizationController.getComponentInstLight(
            instanceId);
        AlmanachDTO almanach = new AlmanachDTO()
            .setInstanceId(instanceId)
            .setAgregated(isAlmanachAgregated(instanceId))
            .setColor(getAlmanachColor(i))
            .setLabel(almanachInst.getLabel());
        othersAlmanachs.add(almanach);
      }
    }

    return othersAlmanachs;
  }

  /**
   * Is the specified almanach is agregated with the current underlying one.
   * @param almanachId the unique identifier of the almanach instance.
   * @return boolean true if the almanach is currently agregated with the current one.
   */
  public boolean isAlmanachAgregated(final String almanachId) {
    for (String anAlmanachId : agregatedAlmanachsIds) {
      if (anAlmanachId.equals(almanachId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Clears the list of the agregated almanachs.
   */
  private void clearAgregatedAlmanachs() {
    agregatedAlmanachsIds.clear();
  }

  /**
   * Updates the list of the agregated almanachs with the specified ones.
   * @param instancesIds the identifier of the new agregated almanachs.
   */
  public void updateAgregatedAlmanachs(final String[] instanceIds) {
    clearAgregatedAlmanachs();
    if (instanceIds != null && instanceIds.length > 0) {
      agregatedAlmanachsIds.addAll(Arrays.asList(instanceIds));
    }
  }

  /**
   * Gets the color with which the events in an almanach should be rendered.
   * @author dlesimple
   * @param position in the array of supported colors
   * @return the HTML/CSS code of the color.
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
  public String initAlertUser(final String eventId) throws RemoteException, AlmanachException,
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

  private synchronized NotificationMetaData getAlertNotificationEvent(final String eventId)
      throws RemoteException, AlmanachException, AlmanachNoSuchFindEventException {
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

  private String getNotificationSubject(final ResourceLocator message) {
    return message.getString("notifSubject");
  }

  private String getNotificationBody(final EventDetail eventDetail, final String htmlPath,
      final ResourceLocator message, final String senderName) {
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
   * @param events
   * @return
   * @throws RemoteException
   * @throws AlmanachException
   */
  protected net.fortuna.ical4j.model.Calendar getICal4jCalendar(Collection<EventDetail> events)
      throws
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
  public EventDetail getCompleteEventDetail(final String id)
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
  public void updateEventOccurence(final EventDetail event,
      final String dateDebutIteration, final String dateFinIteration)
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

  private RRule generateRecurrenceRule(final Periodicity periodicity)
      throws RemoteException, AlmanachException {
    return getAlmanachBm().generateRecurrenceRule(periodicity);
  }

  /**
   * Gets the events defined in the underlying almanach.
   * @param yearScope
   * @return
   * @throws RemoteException
   * @throws AlmanachException
   */
  public Collection<EventDetail> getListRecurrentEvent(boolean yearScope) throws RemoteException,
      AlmanachException {
    // Récupère le Calendar ical4j
    net.fortuna.ical4j.model.Calendar calendarAlmanach = getICal4jCalendar(getAllAgregationEvents());
    return getAlmanachBm().getListRecurrentEvent(calendarAlmanach,
        currentDay, getSpaceId(), getComponentId(), yearScope);

  }

  /**
   * Gets a view in time of the current underlying almanach.
   * The view depends on the current selected view mode and the current selected window in time.
   * @return an AlmanachCalendarView instance.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the underlying iCal
   * calendar cannot be found.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  public AlmanachCalendarView getAlmanachCalendarView() throws AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    AlmanachDTO almanachDTO = new AlmanachDTO()
        .setColor(getAlmanachColor(getComponentId()))
        .setInstanceId(getComponentId())
        .setLabel(getComponentLabel())
        .setAgregated(isAgregationUsed())
        .setUrl(getComponentUrl());
    AlmanachDay currentAlmanachDay = new AlmanachDay(currentDay.getTime());
    AlmanachCalendarView view = new AlmanachCalendarView(almanachDTO, currentAlmanachDay, viewMode);
    view.setLocale(getLanguage());
    String label = getString("mois" + currentAlmanachDay.getMonth())
            + " " + String.valueOf(currentAlmanachDay.getYear());
    switch (viewMode) {
      case MONTHLY:
        view.setEvents(listCurrentMonthEvents());
        view.setLabel(label);
        break;
      case WEEKLY:
        view.setEvents(listCurrentWeekEvents());
        view.setLabel(view.getFirstDay().getDayOfMonth() + " - " + view.getLastDay().getDayOfMonth()
            + " " + label);
        break;
    }
    return view;
  }

  /**
   * Gets the event occurrences of the events defined in the underlying calendar in the current
   * selected month.
   * @param month the month as a Calendar instance.
   * @return a list of event DTOs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the underlying iCal
   * calendar cannot be found.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  protected List<EventOccurrenceDTO> listCurrentMonthEvents() throws AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    EventOccurrencesGenerator occurrencesGenerator = new EventOccurrencesGenerator(
        getICal4jCalendar(getAllAgregationEvents()), getComponentId());
    occurrencesGenerator.setAlmanachBm(getAlmanachBm());
    return occurrencesGenerator.getEventOccurrencesInMonth(currentDay);
  }

  /**
   * Gets the event occurrences of the events defined in the underlying calendar in the current
   * selected week.
   * @param month the month as a Calendar instance.
   * @return a list of event DTOs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the underlying iCal
   * calendar cannot be found.
   * @throws RemoteException if the communication with the remote business object fails.
   */
  protected List<EventOccurrenceDTO> listCurrentWeekEvents() throws AlmanachException,
      AlmanachNoSuchFindEventException, RemoteException {
    EventOccurrencesGenerator occurrencesGenerator = new EventOccurrencesGenerator(
        getICal4jCalendar(getAllAgregationEvents()), getComponentId());
    occurrencesGenerator.setAlmanachBm(getAlmanachBm());
    return occurrencesGenerator.getEventOccurrencesInWeek(currentDay);
  }
}
