/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.mailinglist.service.notification;

import com.stratelia.webactiv.calendar.control.SilverpeasCalendar;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.HolidayDetail;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.ToDoHeader;
import com.stratelia.webactiv.calendar.socialnetwork.SocialInformationEvent;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class StubSilverpeasCalendarBm implements SilverpeasCalendar {

  @Override
  public void addHolidayDate(HolidayDetail holiday) {
    // TODO Auto-generated method stub
  }

  @Override
  public void addHolidayDates(List holidayDates) {
    // TODO Auto-generated method stub
  }

  @Override
  public String addJournal(JournalHeader journal) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addJournalAttendee(String journalId, Attendee attendee) {
    // TODO Auto-generated method stub
  }

  @Override
  public void addJournalCategory(String journalId, String categoryId) {
    // TODO Auto-generated method stub
  }

  @Override
  public String addToDo(ToDoHeader todo) {
    return "" + todo.hashCode();
  }

  @Override
  public void addToDoAttendee(String todoId, Attendee attendee) {
    // TODO Auto-generated method stub
  }

  @Override
  public Collection countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getAllCategories() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Category getCategory(String categoryId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getClosedToDos(String organizerId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getExternalJournalHeadersForUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getExternalJournalHeadersForUserAfterDate(String userId,
      Date startDate) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getExternalTodos(String spaceId, String componentId,
      String externalId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List getHolidayDates(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List getHolidayDates(String userId, Date beginDate, Date endDate) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getJournalAttendees(String journalId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getJournalCategories(String journalId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JournalHeader getJournalHeader(String journalId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getJournalHeadersForUserAfterDate(String userId,
      Date startDate, int nbReturned) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getNotCompletedToDosForUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllToDoForUser(final String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getOrganizerToDos(String organizerId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getTentativeSchedulablesForUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Collection getToDoAttendees(String todoId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ToDoHeader getToDoHeader(String todoId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasTentativeSchedulablesForUser(String userId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void indexAllJournal() {
    // TODO Auto-generated method stub
  }

  @Override
  public void indexAllTodo() {
    // TODO Auto-generated method stub
  }

  @Override
  public boolean isHolidayDate(HolidayDetail date) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void removeHolidayDate(HolidayDetail holiday) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeHolidayDates(List holidayDates) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeJournal(String journalId) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeJournalAttendee(String journalId, Attendee attendee) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeJournalCategory(String journalId, String categoryId) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeToDo(String id) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeToDoAttendee(String todoId, Attendee attendee) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeToDoByInstanceId(String instanceId) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setJournalAttendees(String journalId, String[] userIds) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setJournalCategories(String journalId, String[] categoryIds) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setJournalParticipationStatus(String journalId, String userId,
      String participation) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setToDoAttendees(String todoId, String[] userIds) {
    // TODO Auto-generated method stub
  }

  @Override
  public void updateJournal(JournalHeader journal) {
    // TODO Auto-generated method stub
  }

  @Override
  public void updateToDo(ToDoHeader todo) {
    // TODO Auto-generated method stub
  }

  public EJBHome getEJBHome() {
    // TODO Auto-generated method stub
    return null;
  }

  public Handle getHandle() {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getPrimaryKey() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isIdentical(EJBObject arg0) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<JournalHeader> getNextEventsForUser(String day, String userId, String classification,
      Date begin, Date end) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SocialInformationEvent> getNextEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SocialInformationEvent> getLastEventsForMyContacts(String day, String myId,
      List<String> myContactsIds, Date begin, Date end) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SocialInformationEvent> getMyLastEvents(String day, String myId, Date begin,
      Date end) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
