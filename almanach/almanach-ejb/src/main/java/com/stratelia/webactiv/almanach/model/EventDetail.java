/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.almanach.model;

import java.util.Collection;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Uid;

import static com.silverpeas.util.StringUtil.*;

public class EventDetail extends AbstractI18NBean implements
        SilverContentInterface, Serializable {

  private static final long serialVersionUID = 9077018265272108291L;
  public static ResourceLocator almanachSettings =
          new ResourceLocator("com.stratelia.webactiv.almanach.settings.almanachSettings", "");
  private String _name = null;
  private EventPK _pk = null;
  private Date _startDate = null;
  private Date _endDate = null;
  private String _delegatorId = null;
  private int _priority = 0;
  private String _title = null;
  private String _iconUrl = "";
  private String startHour = "";
  private String endHour = "";
  private String place = "";
  private String eventUrl = "";
  private Periodicity periodicity;

  public String getPlace() {
    return place;
  }

  public void setPlace(String place) {
    this.place = place;
  }

  public EventDetail() {
  }

  public EventDetail(EventPK pk, String title, Date startDate, Date endDate) {
    if (endDate.before(startDate)) {
      throw new IllegalArgumentException("The end date cannot be before the start date of the event");
    }
    this._pk = pk;
    this._title = title;
    this._startDate = new Date(startDate.getTime());
    this._endDate = new Date(endDate.getTime());
  }

  /**
   * Is this event periodic?
   * @return true if the event is recurrent, false otherwise.
   */
  public boolean isPeriodic() {
    return this.periodicity != null;
  }

  public EventPK getPK() {
    return _pk;
  }

  public void setPK(EventPK pk) {
    _pk = pk;
  }

  public String getNameDescription() {
    return _name;
  }

  @Override
  public String getName() {
    return _title;
  }

  public void setNameDescription(String name) {
    _name = name;
  }

  public String getDelegatorId() {
    return _delegatorId;
  }

  public void setDelegatorId(String delegatorId) {
    _delegatorId = delegatorId;
  }

  public int getPriority() {
    return _priority;
  }

  public void setPriority(int priority) {
    _priority = priority;
  }

  public boolean isPriority() {
    return getPriority() > 0;
  }

  public Date getStartDate() {
    return new Date(_startDate.getTime());
  }

  public void setStartDate(Date date) {
    if (date == null) {
      throw new IllegalArgumentException("The start date cannot be null");
    }
    _startDate = new Date(date.getTime());
    if (getEndDate() == null) {
      setEndDate(_startDate);
    }
  }

  public Date getEndDate() {
    Date date = null;
    if (_endDate != null) {
      date = new Date(_endDate.getTime());
    } else {
      date = getStartDate();
    }
    return date;
  }

  /**
   * Sets the date at which this event ends. The end date cannot be null.
   * @param date end date of this event.
   */
  public void setEndDate(Date date) {
    if (date == null) {
      throw new IllegalArgumentException("The end date cannot be null");
    }
    _endDate = new Date(date.getTime());
  }

  public boolean isAllDay() {
    return !isDefined(getStartHour()) || !isDefined(getEndHour());
  }

  public String getTitle() {
    return _title;
  }

  public void setTitle(String title) {
    _title = title;
  }

  @Override
  public String getDescription() {
    return getNameDescription();
  }

  @Override
  public String getURL() {
    return "searchResult?Type=Event&Id=" + getId();
  }

  @Override
  public String getId() {
    return getPK().getId();
  }

  @Override
  public String getInstanceId() {
    return getPK().getComponentName();
  }

  @Override
  public String getDate() {
    return null;
  }

  @Override
  public String getSilverCreationDate() {
    return null;
  }

  public void setIconUrl(String iconUrl) {
    this._iconUrl = iconUrl;
  }

  @Override
  public String getIconUrl() {
    return this._iconUrl;
  }

  @Override
  public String getCreatorId() {
    return getDelegatorId();
  }

  public String getEndHour() {
    String hour = endHour;
    if (!isDefined(hour)) {
      hour = getStartHour();
    }
    return hour;
  }

  public void setEndHour(String endHour) {
    this.endHour = endHour;
  }

  public String getStartHour() {
    return startHour;
  }

  public void setStartHour(String startHour) {
    this.startHour = startHour;
  }

  public String getEventUrl() {
    return eventUrl;
  }

  public void setEventUrl(String eventUrl) {
    this.eventUrl = eventUrl;
  }

  public String getPermalink() {
    if (URLManager.displayUniversalLinks()) {
      return URLManager.getApplicationURL() + "/Event/" + getId();
    }

    return null;
  }

  @Override
  public String getDescription(String language) {
    return getDescription();
  }

  @Override
  public String getName(String language) {
    return getName();
  }

  @Override
  public Iterator<String> getLanguages() {
    return null;
  }

  public String getWysiwyg() throws WysiwygException {
    String wysiwygContent = null;
    wysiwygContent = WysiwygController.loadFileAndAttachment(
            getPK().getSpace(), getPK().getComponentName(), getPK().getId());
    return wysiwygContent;
  }

  public Collection<AttachmentDetail> getAttachments() {
    try {
      AlmanachBm almanachService = ((AlmanachBmHome) EJBUtilitaire.getEJBObjectRef(
              JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class)).create();
      return almanachService.getAttachments(getPK());
    } catch (Exception ex) {
      Logger.getLogger(EventDetail.class.getName()).log(Level.SEVERE, null, ex);
      throw new AlmanachRuntimeException("EventDetail.getAttachments()",
              SilverpeasRuntimeException.ERROR,
              "almanach.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS", ex);
    }
  }

  public Periodicity getPeriodicity() {
    return periodicity;
  }

  public void setPeriodicity(Periodicity periodicity) {
    this.periodicity = periodicity;
  }

  public int getNbDaysDuration() {
    int nbDaysDuration = 0;

    if (_endDate != null) {
      Calendar calStartDate = Calendar.getInstance();
      calStartDate.setTime(_startDate);

      Calendar calEndDate = Calendar.getInstance();
      calEndDate.setTime(_endDate);

      while (!calStartDate.equals(calEndDate)) {
        calStartDate.add(Calendar.DATE, 1);
        nbDaysDuration++;
      }
    }
    return nbDaysDuration;
  }

  public VEvent icalConversion(ExDate exDate) {
    net.fortuna.ical4j.model.Date dtStart = toIcalDate(getStartDate(), getStartHour());
    net.fortuna.ical4j.model.Date dtEnd = toIcalDate(getEndDate(), getEndHour());
    VEvent iCalEvent = new VEvent(dtStart, dtEnd, _title);

    if (_pk != null) {
      Uid uid = new Uid(_pk.getId());
      iCalEvent.getProperties().add(uid);
    }
    Description description = new Description(getDescription());
    iCalEvent.getProperties().add(description);
    if (periodicity != null) {
      iCalEvent.getProperties().add(periodicity.generateRecurrenceRule());
      // Exceptions in the recurrence
      if (exDate != null) {
        iCalEvent.getProperties().add(exDate);
      }
    }
    return iCalEvent;
  }

  /**
   * Gets the time zone in which this event is defined.
   * @return the time zone in which the event occur.
   */
  public TimeZone getTimeZone() {
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    return registry.getTimeZone(almanachSettings.getString("almanach.timezone"));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EventDetail other = (EventDetail) obj;
    if (_pk == null) {
      if (other._pk != null) {
        return false;
      }
    } else if (!_pk.equals(other._pk)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + (this._pk != null ? this._pk.hashCode() : 0);
    return hash;
  }

  private net.fortuna.ical4j.model.Date toIcalDate(final Date date, final String hour) {
    net.fortuna.ical4j.model.Date iCalDate;
    Calendar calDate = java.util.Calendar.getInstance();
    calDate.setTime(date);
    if (isDefined(hour)) {
      calDate.set(java.util.Calendar.HOUR_OF_DAY, DateUtil.extractHour(hour));
      calDate.set(java.util.Calendar.MINUTE, DateUtil.extractMinutes(hour));
    }
    iCalDate = new DateTime(calDate.getTime());
    ((DateTime) iCalDate).setTimeZone(getTimeZone());
    return iCalDate;
  }
}
