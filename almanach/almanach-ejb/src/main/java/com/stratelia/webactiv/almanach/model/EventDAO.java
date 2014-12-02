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
package com.stratelia.webactiv.almanach.model;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.date.Period;
import org.silverpeas.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.silverpeas.util.DateUtil.*;
import static org.silverpeas.util.StringUtil.isDefined;

public class EventDAO {

  private static final String EVENT_COLUMNNAMES =
      "eventId, eventName, eventDelegatorId, eventStartDay, eventEndDay, eventStartHour, " +
          "eventEndHour, eventPriority, eventTitle, eventPlace, eventUrl, instanceId";
  private static final String EVENTS_IN_RANGE_QUERY =
      "select distinct * from " + EventPK.TABLE_NAME + " left outer join " +
          Periodicity.getTableName() + " on " + EventPK.TABLE_NAME + ".eventId = " +
          Periodicity.getTableName() + ".eventId where ((eventStartDay <= ''{0}'' " +
          "and eventEndDay >= ''{0}'') or (eventStartDay >= ''{0}'' and eventStartDay <= ''{1}'')" +
          " or " +
          "(id is not null and eventStartDay < ''{1}'' and (untildateperiod >= ''{0}'' or " +
          "untildateperiod is null))) and instanceId in ({2}) order by eventStartDay";
  private static final String EVENTS_FROM_DATE =
      "select distinct * from " + EventPK.TABLE_NAME + " left outer join " +
          Periodicity.getTableName() + " on " + EventPK.TABLE_NAME + ".eventId = " +
          Periodicity.getTableName() + ".eventId where ((eventStartDay >= ''{0}'') " +
          "or (eventEndDay >= ''{0}'') or (id is not null and (untildateperiod >= ''{0}'' or " +
          "untildateperiod is null))) and instanceId in ({1}) order by eventStartDay";

  public void updateEvent(final EventDetail event) throws Exception {
    Connection connection = openConnection();

    String updateQuery = "update " + event.getPK().getTableName();
    updateQuery +=
        " set eventName = ? , eventDelegatorId = ? , eventStartDay = ? , eventEndDay = ? , " +
            "eventStartHour = ? , eventEndHour = ? , eventPriority = ? , eventTitle = ? , " +
            "eventPlace = ? , eventUrl = ? ";
    updateQuery += " where eventId = ? and instanceId = ?";

    PreparedStatement updateStmt = null;
    try {
      updateStmt = connection.prepareStatement(updateQuery);

      // updateStmt.setString(1, event.getNameDescription()); //No more used
      // (replaced by wysiwyg)
      updateStmt.setString(1, "");
      updateStmt.setString(2, event.getDelegatorId());
      updateStmt.setString(3, formatDate(event.getStartDate()));
      if (event.getEndDate() != null) {
        updateStmt.setString(4, formatDate(event.getEndDate()));
      } else {
        updateStmt.setString(4, null);
      }
      updateStmt.setString(5, event.getStartHour());
      updateStmt.setString(6, event.getEndHour());
      updateStmt.setInt(7, event.getPriority());
      updateStmt.setString(8, event.getTitle());
      updateStmt.setString(9, event.getPlace());
      updateStmt.setString(10, event.getEventUrl());
      updateStmt.setInt(11, Integer.parseInt(event.getPK().getId()));
      updateStmt.setString(12, event.getPK().getComponentName());

      updateStmt.executeUpdate();
    } finally {
      DBUtil.close(updateStmt);
      closeConnection(connection);
    }
  }

  public String addEvent(final Connection connection, final EventDetail event) throws Exception {
    String insertQuery = "insert into " + event.getPK().getTableName();
    insertQuery += " (" + EVENT_COLUMNNAMES + ") ";
    insertQuery += " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement insertStmt;

    int id = 0;
    try {
      insertStmt = connection.prepareStatement(insertQuery);

      id = DBUtil.getNextId(event.getPK().getTableName(), "eventId");
      event.getPK().setId(String.valueOf(id));

      insertStmt.setInt(1, id);
      // insertStmt.setString(2, event.getNameDescription()); //No more used
      // (replaced by wysiwyg)
      insertStmt.setString(2, "");
      insertStmt.setString(3, event.getDelegatorId());
      insertStmt.setString(4, formatDate(event.getStartDate()));
      if (event.getEndDate() != null) {
        insertStmt.setString(5, formatDate(event.getEndDate()));
      } else {
        insertStmt.setString(5, null);
      }
      insertStmt.setString(6, event.getStartHour());
      insertStmt.setString(7, event.getEndHour());
      insertStmt.setInt(8, event.getPriority());
      insertStmt.setString(9, event.getTitle());
      insertStmt.setString(10, event.getPlace());
      insertStmt.setString(11, event.getEventUrl());
      insertStmt.setString(12, event.getPK().getComponentName());

      insertStmt.executeUpdate();
    } catch (SQLException ue) {
      SilverTrace
          .warn("almanach", "EventDAO.addEvent()", "almanach.EXE_ADD_EVENT_FAIL", "id : " + id, ue);
    }

    return String.valueOf(id);
  }

  public void removeEvent(final Connection connection, final EventPK pk) throws Exception {
    String deleteQuery = "delete from " + pk.getTableName();
    deleteQuery += " where eventId=" + pk.getId();
    Statement deleteStmt = null;

    try {
      deleteStmt = connection.createStatement();
      deleteStmt.executeUpdate(deleteQuery);
    } finally {
      DBUtil.close(deleteStmt);
    }
  }

  /**
   * Find all events that can occur in the specified range and for the specified almanachs.
   * @param startDay the start date of the range. It has to be in the format yyyy/MM/dd.
   * @param endDay the end date of the range. It has to be in the format yyyy/MM/dd. If the end
   * date is null or empty, then there is no end date and all events from startDay are taken into
   * account.
   * @param almanachIds the identifiers of the almanachs.
   * @return a collection of events that should occur in the specified range.
   * @throws SQLException if an error occurs while executing the SQL request.
   * @throws Exception if an error occurs during the process of this method.
   */
  public Collection<EventDetail> findAllEventsInRange(String startDay, String endDay,
      String... almanachIds) throws Exception {
    Connection connection = openConnection();

    ResultSet rs = null;
    Statement selectStmt = null;
    StringBuilder paramInstanceIds = new StringBuilder();
    if (almanachIds != null && almanachIds.length > 0) {
      paramInstanceIds.append("'").append(almanachIds[0]).append("'");
      for (int i = 1; i < almanachIds.length; i++) {
        paramInstanceIds.append(",'").append(almanachIds[i]).append("'");
      }
    } else {
      throw new SQLException("Missing instance identifiers");
    }

    String selectQuery;
    if (isDefined(endDay)) {
      selectQuery = MessageFormat
          .format(EVENTS_IN_RANGE_QUERY, startDay, endDay, paramInstanceIds.toString());
    } else {
      selectQuery = MessageFormat.format(EVENTS_FROM_DATE, startDay, paramInstanceIds.toString());
    }
    try {
      selectStmt = connection.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      List<EventDetail> events = new ArrayList<>();
      while (rs.next()) {
        EventDetail event = decodeEventDetailFromResultSet(rs);
        events.add(event);
      }
      return events;
    } finally {
      DBUtil.close(rs, selectStmt);
      closeConnection(connection);
    }
  }

  public Collection<EventDetail> findAllEventsInPeriod(final Period period, String... instanceIds)
      throws Exception {
    String startDay = formatDate(period.getBeginDate());
    String endDay = formatDate(period.getEndDate());
    return findAllEventsInRange(startDay, endDay, instanceIds);
  }

  public Collection<EventDetail> findAllEvents(String... instanceIds)
      throws Exception {
    Connection connection = openConnection();

    ResultSet rs = null;
    Statement selectStmt = null;
    String paramInstanceIds = "";
    if (instanceIds != null && instanceIds.length > 0) {
      paramInstanceIds = " where (instanceId='" + instanceIds[0] + "'";
      for (int i = 1; i < instanceIds.length; i++) {
        paramInstanceIds += " or instanceId='" + instanceIds[i] + "'";
      }
      paramInstanceIds += ")";
    } else {
      throw new SQLException("Missing instance identifiers");
    }

    SilverTrace
        .debug("almanach", "EventDAO.findAllEvents()", "paramInstanceIds=" + paramInstanceIds);
    try {
      String selectQuery =
          "select distinct * " + " from " + EventPK.TABLE_NAME + " left outer join " +
              Periodicity.getTableName() + " on " + EventPK.TABLE_NAME + ".eventId = " +
              Periodicity.getTableName() + ".eventId" + paramInstanceIds +
              " order by eventStartDay";

      SilverTrace.debug("almanach", "EventDAO.getAllEvents()", "selectQuery=" + selectQuery);

      selectStmt = connection.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      List<EventDetail> list = new ArrayList<>();
      while (rs.next()) {
        EventDetail event = decodeEventDetailFromResultSet(rs);
        list.add(event);
      }
      return list;
    } finally {
      DBUtil.close(rs, selectStmt);
      closeConnection(connection);
    }
  }

  public Collection<EventDetail> findAllEventsByPK(final Collection<EventPK> eventPKs)
      throws Exception {
    Connection connection = openConnection();

    List<EventDetail> events = new ArrayList<>();
    try {
      for (EventPK pk : eventPKs) {
        EventDetail event = getEventDetail(connection, pk);
        events.add(event);
      }
    } finally {
      closeConnection(connection);
    }
    return events;
  }

  public EventDetail findEventByPK(final EventPK pk) throws Exception {
    Connection connection = openConnection();
    try {
      return getEventDetail(connection, pk);
    } finally {
      closeConnection(connection);
    }
  }

  private EventDetail getEventDetail(final Connection connection, final EventPK pk)
      throws Exception {
    ResultSet rs = null;
    Statement selectStmt = null;
    String selectQuery = "select * " + " from " + pk.getTableName() + " left outer join " +
        Periodicity.getTableName() + " on " + pk.getTableName() + ".eventId = " +
        Periodicity.getTableName() + ".eventId" + " where " + pk.getTableName() + ".eventId=" +
        pk.getId();

    EventDetail event = null;
    try {
      selectStmt = connection.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      if (rs.next()) {
        event = decodeEventDetailFromResultSet(rs);
      }
    } finally {
      DBUtil.close(rs, selectStmt);
    }
    return event;
  }

  protected EventDetail decodeEventDetailFromResultSet(final ResultSet rs) throws Exception {
    String id;
    try {
      id = rs.getString(EventPK.TABLE_NAME + ".eventId");
    } catch (Exception ex) {
      id = rs.getString("eventId");
    }
    String title = rs.getString("eventTitle");
    Date startDate = parseDate(rs.getString("eventStartDay"));
    Date endDate = startDate;
    if (rs.getString("eventEndDay") != null) {
      endDate = parseDate(rs.getString("eventEndDay"));
    }
    EventDetail event = new EventDetail(new EventPK(id), title, startDate, endDate);
    event.setNameDescription(rs.getString("eventName"));
    event.setDelegatorId(rs.getString("eventDelegatorId"));
    event.setStartHour(rs.getString("eventStartHour"));
    event.setEndHour(rs.getString("eventEndHour"));
    event.setPriority(rs.getInt("eventPriority"));
    event.setPlace(rs.getString("eventPlace"));
    event.setEventUrl(rs.getString("eventUrl"));
    event.getPK().setComponentName(rs.getString("instanceId"));

    if (rs.getString("id") != null) {
      Periodicity periodicity = new Periodicity();
      periodicity.setPK(new EventPK(rs.getString("id"), null, rs.getString("instanceId")));
      periodicity.setEventId(rs.getInt("eventId"));
      periodicity.setDay(rs.getInt("day"));
      periodicity.setDaysWeekBinary(rs.getString("daysweekbinary"));
      periodicity.setFrequency(rs.getInt("frequency"));
      periodicity.setNumWeek(rs.getInt("numweek"));
      periodicity.setUnity(rs.getInt("unity"));
      periodicity.setUntilDatePeriod(parseDate(rs.getString("untildateperiod")));
      event.setPeriodicity(periodicity);
    }
    fixIncorrectDatesForAlreadyExistingEvent(event);
    return event;
  }

  protected void fixIncorrectDatesForAlreadyExistingEvent(final EventDetail event)
      throws Exception {
    if (event.getStartDate().equals(event.getEndDate()) && isDefined(event.getEndHour())) {
      int endHour = extractHour(event.getEndHour());
      int endMinute = extractMinutes(event.getEndHour());
      int startHour = extractHour(event.getStartHour());
      int startMinute = extractMinutes(event.getStartHour());
      if (endHour < startHour || (endHour == startHour && endMinute < startMinute)) {
        String hour = event.getStartHour();
        event.setStartHour(event.getEndHour());
        event.setEndHour(hour);
        updateEvent(event);
      }
    }
  }

  protected Connection openConnection() throws SQLException {
    return DBUtil.openConnection();
  }

  protected void closeConnection(final Connection connection) {
    DBUtil.close(connection);
  }
}
