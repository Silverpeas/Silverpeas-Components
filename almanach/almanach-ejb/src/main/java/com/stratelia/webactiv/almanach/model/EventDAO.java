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
package com.stratelia.webactiv.almanach.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

public class EventDAO {

  private static final String COLUMNNAMES =
      "eventId, eventName, eventDelegatorId, eventStartDay, eventEndDay, eventStartHour, eventEndHour, eventPriority, eventTitle, eventPlace, eventUrl, instanceId";
  private static final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(
      "yyyy/MM/dd");

  public static void updateEvent(Connection con, EventDetail event)
      throws SQLException, Exception {

    String updateQuery = "update " + event.getPK().getTableName();
    updateQuery +=
        " set eventName = ? , eventDelegatorId = ? , eventStartDay = ? , eventEndDay = ? , eventStartHour = ? , eventEndHour = ? , eventPriority = ? , eventTitle = ? , eventPlace = ? , eventUrl = ? ";
    updateQuery += " where eventId = ? and instanceId = ?";

    PreparedStatement updateStmt = null;
    try {
      updateStmt = con.prepareStatement(updateQuery);

      // updateStmt.setString(1, event.getNameDescription()); //No more used
      // (replaced by wysiwyg)
      updateStmt.setString(1, "");
      updateStmt.setString(2, event.getDelegatorId());
      updateStmt.setString(3, dateFormat.format(event.getStartDate()));
      if (event.getEndDate() != null) {
        updateStmt.setString(4, dateFormat.format(event.getEndDate()));
      } else {
        updateStmt.setString(4, null);
      }
      updateStmt.setString(5, event.getStartHour());
      updateStmt.setString(6, event.getEndHour());
      updateStmt.setInt(7, event.getPriority());
      updateStmt.setString(8, event.getTitle());
      updateStmt.setString(9, event.getPlace());
      updateStmt.setString(10, event.getEventUrl());
      updateStmt.setInt(11, new Integer(event.getPK().getId()).intValue());
      updateStmt.setString(12, event.getPK().getComponentName());

      updateStmt.executeUpdate();
    } finally {
      DBUtil.close(updateStmt);
    }
  }

  public static String addEvent(Connection con, EventDetail event)
      throws SQLException, Exception {

    String insertQuery = "insert into " + event.getPK().getTableName();
    insertQuery += " (" + COLUMNNAMES + ") ";
    insertQuery += " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement insertStmt = null;

    int id = 0;
    try {
      insertStmt = con.prepareStatement(insertQuery);

      id = DBUtil.getNextId(event.getPK().getTableName(), "eventId");
      event.getPK().setId(String.valueOf(id));

      insertStmt.setInt(1, id);
      // insertStmt.setString(2, event.getNameDescription()); //No more used
      // (replaced by wysiwyg)
      insertStmt.setString(2, "");
      insertStmt.setString(3, event.getDelegatorId());
      insertStmt.setString(4, dateFormat.format(event.getStartDate()));
      if (event.getEndDate() != null) {
        insertStmt.setString(5, dateFormat.format(event.getEndDate()));
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
    } catch (com.stratelia.webactiv.util.exception.UtilException ue) {
      SilverTrace.warn("almanach", "EventDAO.addEvent()",
          "almanach.EXE_ADD_EVENT_FAIL", "id : " + id, ue);
    } finally {
      DBUtil.close(insertStmt);
    }

    return String.valueOf(id);
  }

  public static void removeEvent(Connection con, EventPK pk)
      throws SQLException {
    String deleteQuery = "delete from " + pk.getTableName();
    deleteQuery += " where eventId=" + pk.getId();
    Statement deleteStmt = null;

    try {
      deleteStmt = con.createStatement();
      deleteStmt.executeUpdate(deleteQuery);
    } finally {
      DBUtil.close(deleteStmt);
    }
  }

  public static Collection<EventDetail> getMonthEvents(Connection con, EventPK pk,
      Date date, String[] instanceIds) throws SQLException, Exception {
    ResultSet rs = null;
    Statement selectStmt = null;
    String paramInstanceIds = "";
    if (instanceIds != null) {
      if (instanceIds.length > 0) {
        paramInstanceIds = " and (instanceId='" + pk.getComponentName() + "'";
        for (int i = 0; i < instanceIds.length; i++) {
          paramInstanceIds += " or instanceId='" + instanceIds[i] + "'";
        }
      } else {
        paramInstanceIds = " and instanceId='" + pk.getComponentName() + "'";
      }
    } else {
      paramInstanceIds = " and (instanceId='" + pk.getComponentName() + "'";
    }

    paramInstanceIds += ")";

    SilverTrace.info("almanach", "EventDAO.getMonthEvents()",
        "paramInstanceIds=" + paramInstanceIds);
    try {
      String month = dateFormat.format(date);
      month = month.substring(0, month.length() - 2);
      String startDay = month + "01";
      String endDay = month + "31";
      String selectQuery = "select distinct " + COLUMNNAMES
          + " from " + pk.getTableName()
          + " where ((eventStartDay < '" + startDay + "' and eventEndDay > '" + endDay + "')"
          + " or eventStartDay like '" + month + "%'"
          + " or eventEndDay like '" + month + "%')"
          + paramInstanceIds + " order by eventStartDay";

      SilverTrace.info("almanach", "EventDAO.getMonthEvents()", "selectQuery="
          + selectQuery);

      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      List<EventDetail> list = new ArrayList<EventDetail>();
      while (rs.next()) {
        EventDetail event = getEventDetailFromResultSet(rs);
        list.add(event);
      }
      return list;
    } finally {
      DBUtil.close(rs, selectStmt);
    }
  }

  public static Collection<EventDetail> getAllEvents(Connection con, EventPK pk)
      throws SQLException, Exception {
    ResultSet rs = null;
    Statement selectStmt = null;

    String selectQuery = "select distinct " + COLUMNNAMES + " from "
        + pk.getTableName() + " where instanceId='" + pk.getComponentName()
        + "'" + " order by eventStartDay";

    try {
      SilverTrace.info("almanach", "EventDAO.getAllEvents()",
          "almanach.MSG_SQL_REQUEST", "selectRequest = " + selectQuery);

      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      List<EventDetail> list = new ArrayList<EventDetail>();
      while (rs.next()) {
        list.add(getEventDetailFromResultSet(rs));
      }
      return list;
    } finally {
      DBUtil.close(rs, selectStmt);
    }
  }

  public static Collection<EventDetail> getAllEvents(Connection con, EventPK pk,
      String[] instanceIds) throws SQLException, Exception {
    ResultSet rs = null;
    Statement selectStmt = null;
    String paramInstanceIds = "";
    if (instanceIds != null) {
      if (instanceIds.length > 0) {
        paramInstanceIds = " (instanceId='" + pk.getComponentName() + "'";
        for (int i = 0; i < instanceIds.length; i++) {
          paramInstanceIds += " or instanceId='" + instanceIds[i] + "'";
        }
      } else {
        paramInstanceIds = " instanceId='" + pk.getComponentName() + "'";
      }
    } else {
      paramInstanceIds = " (instanceId='" + pk.getComponentName() + "'";
    }

    paramInstanceIds += ")";

    SilverTrace.info("almanach", "EventDAO.getAllEvents()", "paramInstanceIds="
        + paramInstanceIds);
    try {
      String selectQuery = "select distinct " + COLUMNNAMES + " from "
          + pk.getTableName() + " where " + paramInstanceIds
          + " order by eventStartDay";

      SilverTrace.info("almanach", "EventDAO.getAllEvents()", "selectQuery="
          + selectQuery);

      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      List<EventDetail> list = new ArrayList<EventDetail>();
      while (rs.next()) {
        EventDetail event = getEventDetailFromResultSet(rs);
        list.add(event);
      }
      return list;
    } finally {
      DBUtil.close(rs, selectStmt);
    }
  }

  public static EventDetail getEventDetailFromResultSet(ResultSet rs)
      throws SQLException, Exception {
    String id = "" + rs.getInt(1);
    String name = rs.getString(2);
    EventDetail event = new EventDetail(new EventPK(id), name);
    event.setDelegatorId(rs.getString(3));
    event.setStartDate(dateFormat.parse(rs.getString(4)));

    if (rs.getString(5) != null) {
      event.setEndDate(dateFormat.parse(rs.getString(5)));
    }

    event.setStartHour(rs.getString(6));
    event.setEndHour(rs.getString(7));

    event.setPriority(rs.getInt(8));
    event.setTitle(rs.getString(9));
    event.setPlace(rs.getString(10));
    event.setEventUrl(rs.getString(11));
    event.getPK().setComponentName(rs.getString(12));
    return event;
  }

  public static Collection<EventDetail> selectByEventPKs(Connection con,
      Collection<EventPK> eventPKs)
      throws SQLException, Exception {
    List<EventDetail> events = new ArrayList<EventDetail>();
    Iterator<EventPK> iterator = eventPKs.iterator();

    while (iterator.hasNext()) {
      EventPK eventPK = iterator.next();
      EventDetail event = getEventDetail(con, eventPK);

      events.add(event);
    }
    return events;
  }

  public static EventDetail getEventDetail(Connection con, EventPK pk)
      throws SQLException, Exception {
    ResultSet rs = null;
    Statement selectStmt = null;
    String selectQuery = "select " + COLUMNNAMES + " from " + pk.getTableName()
        + " where eventId=" + pk.getId();

    EventDetail event = null;
    try {
      selectStmt = con.createStatement();
      rs = selectStmt.executeQuery(selectQuery);
      if (rs.next()) {
        event = getEventDetailFromResultSet(rs);
      }
    } finally {
      DBUtil.close(rs, selectStmt);
    }
    return event;
  }

  public static Collection<EventDetail> getNextEvents(Connection con, EventPK pk,
      int nbReturned) throws SQLException, Exception {
    ResultSet rs = null;
    PreparedStatement prepStmt = null;
    String selectQuery = "SELECT DISTINCT " + COLUMNNAMES + " FROM "
        + pk.getTableName() + " WHERE instanceId= ? AND eventStartDay >= ? ORDER BY eventStartDay";
    try {
      SilverTrace.info("almanach", "EventDAO.getNextEvents()", "almanach.MSG_SQL_REQUEST",
          "selectRequest = " + selectQuery);
      prepStmt = con.prepareStatement(selectQuery);
      prepStmt.setString(1, pk.getComponentName());
      prepStmt.setString(2, dateFormat.format(new Date()));
      rs = prepStmt.executeQuery();
      List<EventDetail> list = new ArrayList<EventDetail>();
      while (rs.next() && nbReturned != 0) {
        list.add(getEventDetailFromResultSet(rs));
        nbReturned--;
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}
