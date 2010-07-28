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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.mailinglist.service.notification;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;

import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.model.Attendee;
import com.stratelia.webactiv.calendar.model.Category;
import com.stratelia.webactiv.calendar.model.HolidayDetail;
import com.stratelia.webactiv.calendar.model.JournalHeader;
import com.stratelia.webactiv.calendar.model.ToDoHeader;

public class StubCalendarBm implements CalendarBm {

  public void addHolidayDate(HolidayDetail holiday) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void addHolidayDates(List holidayDates) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public String addJournal(JournalHeader journal) throws RemoteException
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void addJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void addJournalCategory(String journalId, String categoryId)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public String addToDo(ToDoHeader todo) throws RemoteException
      {
    return "" + todo.hashCode();
  }

  public void addToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public Collection countMonthSchedulablesForUser(String month, String userId,
      String categoryId, String participation) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getAllCategories() throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Category getCategory(String categoryId) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getClosedToDos(String organizerId) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getExternalJournalHeadersForUser(String userId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getExternalJournalHeadersForUserAfterDate(String userId,
      Date startDate) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getExternalTodos(String spaceId, String componentId,
      String externalId) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public List getHolidayDates(String userId) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public List getHolidayDates(String userId, Date beginDate, Date endDate)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getJournalAttendees(String journalId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getJournalCategories(String journalId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public JournalHeader getJournalHeader(String journalId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getJournalHeadersForUserAfterDate(String userId,
      Date startDate, int nbReturned) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getNextDaySchedulablesForUser(String day, String userId,
      String categoryId, String participation) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getNotCompletedToDosForUser(String userId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getOrganizerToDos(String organizerId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getPeriodSchedulablesForUser(String begin, String end,
      String userId, String categoryId, String participation)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getTentativeSchedulablesForUser(String userId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Collection getToDoAttendees(String todoId) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public ToDoHeader getToDoHeader(String todoId) throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasTentativeSchedulablesForUser(String userId)
      throws RemoteException {
    // TODO Auto-generated method stub
    return false;
  }

  public void indexAllJournal() throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void indexAllTodo() throws RemoteException {
    // TODO Auto-generated method stub

  }

  public boolean isHolidayDate(HolidayDetail date) throws RemoteException {
    // TODO Auto-generated method stub
    return false;
  }

  public void removeHolidayDate(HolidayDetail holiday) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeHolidayDates(List holidayDates) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeJournal(String journalId) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeJournalAttendee(String journalId, Attendee attendee)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeJournalCategory(String journalId, String categoryId)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeToDo(String id) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeToDoAttendee(String todoId, Attendee attendee)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void removeToDoByInstanceId(String instanceId) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void setJournalAttendees(String journalId, String[] userIds)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void setJournalCategories(String journalId, String[] categoryIds)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void setJournalParticipationStatus(String journalId, String userId,
      String participation) throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void setToDoAttendees(String todoId, String[] userIds)
      throws RemoteException {
    // TODO Auto-generated method stub

  }

  public void updateJournal(JournalHeader journal) throws RemoteException
  {
    // TODO Auto-generated method stub

  }

  public void updateToDo(ToDoHeader todo) throws RemoteException
  {
    // TODO Auto-generated method stub

  }

  public EJBHome getEJBHome() throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Handle getHandle() throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getPrimaryKey() throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isIdentical(EJBObject arg0) throws RemoteException {
    // TODO Auto-generated method stub
    return false;
  }

  public void remove() throws RemoteException, RemoveException {
    // TODO Auto-generated method stub

  }

  @Override
  public List<JournalHeader> getNextEventsForUser(String day, String userId, String classification,
      int limit, int offset) throws RemoteException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
