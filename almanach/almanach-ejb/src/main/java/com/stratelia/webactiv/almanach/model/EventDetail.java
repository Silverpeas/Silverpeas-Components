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

import com.silverpeas.util.StringUtil;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.wysiwyg.WysiwygException;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Uid;

public class EventDetail extends AbstractI18NBean implements
    SilverContentInterface, Serializable {

  private static final long serialVersionUID = 9077018265272108291L;
  private String _name = null;
  private EventPK _pk = null;
  private Date _startDate = null;
  private Date _endDate = null;
  private String _delegatorId = null;
  private int _priority = 0;
  private String _title = null;
  private String _iconUrl = "";
  private String startHour;
  private String endHour;
  private String place;
  private String eventUrl;
  private Periodicity periodicity;

  public String getPlace() {
    return place;
  }

  public void setPlace(String place) {
    this.place = place;
  }

  public EventDetail() {
  }

  public EventDetail(EventPK pk, String name) {
    this._pk = pk;
    this._name = name;
  }

  public EventDetail(String _name, EventPK _pk, int _priority, String _title,
      String startHour, String endHour, String place, String eventUrl) {
    this._name = _name;
    this._pk = _pk;
    this._priority = _priority;
    this._title = _title;
    this.startHour = startHour;
    this.endHour = endHour;
    this.place = place;
    this.eventUrl = eventUrl;
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

  public Date getStartDate() {
    return _startDate;
  }

  public void setStartDate(Date date) {
    _startDate = date;
  }

  public Date getEndDate() {
    return _endDate;
  }

  public void setEndDate(Date date) {
    _endDate = date;
  }

  public String getTitle() {
    return _title;
  }

  public void setTitle(String title) {
    _title = title;
  }

  public String getDescription() {
    return getNameDescription();
  }

  public String getURL() {
    return "searchResult?Type=Event&Id=" + getId();
  }

  public String getId() {
    return getPK().getId();
  }

  public String getInstanceId() {
    return getPK().getComponentName();
  }

  public String getDate() {
    return null;
  }

  public String getSilverCreationDate() {
    return null;
  }

  public void setIconUrl(String iconUrl) {
    this._iconUrl = iconUrl;
  }

  public String getIconUrl() {
    return this._iconUrl;
  }

  public String getCreatorId() {
    return getDelegatorId();
  }

  public String getEndHour() {
    return endHour;
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

  public String getDescription(String language) {
    return getDescription();
  }

  public String getName(String language) {
    return getName();
  }

  public Iterator<String> getLanguages() {
    return null;
  }

  public String getWysiwyg() throws WysiwygException {
    String wysiwygContent = null;
    wysiwygContent = WysiwygController.loadFileAndAttachment(
        getPK().getSpace(), getPK().getComponentName(), getPK().getId());
    return wysiwygContent;
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
    // Construction du VEvent du Calendar ical4j (pour gestion)
    Calendar calStartDate = java.util.Calendar.getInstance();
    calStartDate.setTime(_startDate);
    if (StringUtil.isDefined(startHour)) {
      calStartDate.set(java.util.Calendar.HOUR_OF_DAY, DateUtil.extractHour(startHour));
      calStartDate.set(java.util.Calendar.MINUTE, DateUtil.extractMinutes(startHour));
    }
    Calendar calEndDate = java.util.Calendar.getInstance();
    calEndDate.setTime(_startDate);
    if (_endDate != null) {
      calEndDate.setTime(_endDate);
      if (StringUtil.isDefined(endHour)) {
        calEndDate.set(java.util.Calendar.HOUR_OF_DAY, DateUtil.extractHour(endHour));
        calEndDate.set(java.util.Calendar.MINUTE, DateUtil.extractMinutes(endHour));
      }
    }
    TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
    ResourceLocator almanachSettings =
        new ResourceLocator("com.stratelia.webactiv.almanach.settings.almanachSettings", "");
    TimeZone localTimeZone = registry.getTimeZone(almanachSettings.getString("almanach.timezone"));

    DateTime dtStart = new DateTime(calStartDate.getTime());
    dtStart.setTimeZone(localTimeZone);
    DateTime dtEnd = new DateTime(calEndDate.getTime());
    dtEnd.setTimeZone(localTimeZone);
    VEvent eventIcal4jCalendar = new VEvent(dtStart, dtEnd, _title);

    if (_pk != null) {
      Uid uid = new Uid(_pk.getId());
      eventIcal4jCalendar.getProperties().add(uid);
    }
    Description description = new Description(getDescription());
    eventIcal4jCalendar.getProperties().add(description);
    if (periodicity != null) {
      eventIcal4jCalendar.getProperties().add(periodicity.generateRecurrenceRule());
      // Exceptions de périodicité
      if (exDate != null) {
        eventIcal4jCalendar.getProperties().add(exDate);
      }
    }
    return eventIcal4jCalendar;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EventDetail other = (EventDetail) obj;
    if (_pk == null) {
      if (other._pk != null)
        return false;
    } else if (!_pk.equals(other._pk))
      return false;
    return true;
  }
}
