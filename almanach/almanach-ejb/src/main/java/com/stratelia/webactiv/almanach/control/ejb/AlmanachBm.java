/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.pdc.model.PdcClassification;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventOccurrence;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.almanach.model.PeriodicityException;
import net.fortuna.ical4j.model.Calendar;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.date.Period;
import org.silverpeas.upload.UploadedFile;
import org.silverpeas.util.ServiceProvider;

import java.util.Collection;
import java.util.List;

public interface AlmanachBm {

  static AlmanachBm get() {
    return ServiceProvider.getService(AlmanachBm.class);
  }

  /**
   * Gets the event occurrences that occur in the specified period and that are defined in the
   * specified almanachs.
   *
   * @param period the period in which the events occur.
   * @param almanachIds the identifier of the almanachs in which the events are defined.
   * @return a list of event occurrences.
   * @ if an error occurs with the remote business service.
   */
  List<EventOccurrence> getEventOccurrencesInPeriod(Period period, String... almanachIds);

  /**
   * Gets the next event occurrences that will occur andd that are defined in the specified
   * almanachs.
   *
   * @param almanachIds the identifier of the almanachs in which the events are defined.
   * @return a list of event occurrences that will occur in the future.
   * @ if an error occurs with the remote business service.
   */
  List<EventOccurrence> getNextEventOccurrences(String... almanachIds);

  /**
   * this method provide a collection of event
   *
   * @param : EventPk pk, to obtain the space and component
   * @ return: java.util.Collection
   */
  Collection<EventDetail> getAllEvents(EventPK pk);

  /**
   * Get all events of instanceId Almanachs
   *
   * @param pk
   * @param instanceIds String[] of instanceIds
   * @return Collection of Events
   */
  Collection<EventDetail> getAllEvents(EventPK pk, String[] instanceIds);

  Collection<EventDetail> getEvents(Collection<EventPK> pks);

  /**
   * addEvent() add an event entry in the database.
   *
   * @param event detail about the event to add in Silverpeas.
   * @param uploadedFiles attached files.
   * @return the unique identifier of the added event.
   */
  String addEvent(EventDetail event, Collection<UploadedFile> uploadedFiles);

  /**
   * Adds the event in the almanach with the specified classification on the PdC.
   *
   * @param event detail about the event to add in Silverpeas.
   * @param uploadedFiles attached files.
   * @param withClassification the classificationwith which the event will be classified on the PdC.
   * @return the unique identifier of the added event.
   */
  String addEvent(EventDetail event, Collection<UploadedFile> uploadedFiles,
      PdcClassification withClassification);

  /**
   * updateEvent() update the event entry, specified by the pk, in the database
   */
  void updateEvent(EventDetail event);

  /**
   * removeEvent() remove the Event entry specified by the pk
   */
  void removeEvent(EventPK pk);

  /**
   * getEventDetail() returns the EventDetail represented by the pk
   */
  EventDetail getEventDetail(EventPK pk);

  int getSilverObjectId(EventPK pk);

  void createIndex(EventDetail detail);

  void addPeriodicityException(PeriodicityException exception);

  Calendar getICal4jCalendar(Collection<EventDetail> events, String language);

  Collection<EventDetail> getListRecurrentEvent(Calendar calendarAlmanach,
      java.util.Calendar currentDay, String spaceId, String instanceId, boolean yearScope);

  Collection<SimpleDocument> getAttachments(EventPK eventPK);

  String getHTMLPath(EventPK eventPK);
}
