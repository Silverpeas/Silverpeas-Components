/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Created on 25 oct. 2004
 *
 */
package org.silverpeas.components.projectmanager.model;

import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author neysseri
 */
public class ProjectManagerCalendarDAO {

  private static final String PROJECTMANAGER_CALENDAR_TABLENAME = "SC_ProjectManager_Calendar";
  private static final String HOLIDAY_DATE = "holidayDate";

  /**
   * Hidden constructor.
   */
  private ProjectManagerCalendarDAO() {
  }

  public static void addHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    if (!isHolidayDate(con, holiday)) {
      String insertStatement = "INSERT INTO PROJECTMANAGER_CALENDAR_TABLENAME VALUES ( ? , ? , ? )";
      try (PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
        prepStmt.setString(1, date2DBDate(holiday.getDate()));
        prepStmt.setInt(2, holiday.getFatherId());
        prepStmt.setString(3, holiday.getInstanceId());

        prepStmt.executeUpdate();
      }
    }
  }

  public static void removeHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuilder deleteStatement = new StringBuilder(128);
    deleteStatement.append("delete from ").append(
        PROJECTMANAGER_CALENDAR_TABLENAME);
    deleteStatement.append(" where holidayDate = ? ");
    deleteStatement.append(" and fatherId = ? ");
    deleteStatement.append(" and instanceId = ? ");
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement.toString());

      prepStmt.setString(1, date2DBDate(holiday.getDate()));
      prepStmt.setInt(2, holiday.getFatherId());
      prepStmt.setString(3, holiday.getInstanceId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Removes all the holiday dates that were saved for the specified ProjectManager instance.
   *
   * @param con a connection to the data source into which are stored the holiday dates.
   * @param instanceId the unique identifier of a ProjectManager instance.
   * @throws SQLException if an error occurs while deleting the holiday dates.
   */
  public static void removeAllHolidayDates(Connection con, String instanceId) throws SQLException {
    final String sql = "delete from " + PROJECTMANAGER_CALENDAR_TABLENAME + " where instanceId = ?";
    try (PreparedStatement deletion = con.prepareStatement(sql)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  public static boolean isHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuilder query = new StringBuilder(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" where holidayDate = ? ");
    query.append(" and fatherId = ? ");
    query.append(" and instanceId = ? ");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());

      stmt.setString(1, date2DBDate(holiday.getDate()));
      stmt.setInt(2, holiday.getFatherId());
      stmt.setString(3, holiday.getInstanceId());

      rs = stmt.executeQuery();

      return rs.next();
    } finally {
      DBUtil.close(rs, stmt);
    }
  }

  public static List<Date> getHolidayDates(Connection con, String instanceId)
      throws SQLException {
    List<Date> holidayDates = new ArrayList<>();
    StringBuilder query = new StringBuilder(128);
    query.append("SELECT * ");
    query.append("FROM ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" WHERE instanceId = ? ");
    query.append("ORDER BY holidayDate ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates.add(dbDate2Date(rs.getString(HOLIDAY_DATE)));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static List<Date> getHolidayDates(Connection con, String instanceId,
      Date beginDate, Date endDate) throws SQLException {
    List<Date> holidayDates = new ArrayList<>();
    StringBuilder query = new StringBuilder(128);
    query.append("SELECT * ");
    query.append("FROM ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" WHERE instanceId = ? ");
    query.append(" AND ? <= holidayDate ");
    query.append(" AND holidayDate <= ? ");
    query.append("ORDER BY holidayDate ASC");

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      stmt.setString(2, date2DBDate(beginDate));
      stmt.setString(3, date2DBDate(endDate));
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates.add(dbDate2Date(rs.getString(HOLIDAY_DATE)));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static String date2DBDate(Date date) {
    // the date format used in database to represent a date
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
    return formatter.format(date);
  }

  private static Date dbDate2Date(String dbDate)
      throws SQLException {
    Date date;
    try {
      // the date format used in database to represent a date
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
      date = formatter.parse(dbDate);
    } catch (ParseException e) {
      throw new SQLException("ProjectManagerCalendarDAO : dbDate2Date("
          + ProjectManagerCalendarDAO.HOLIDAY_DATE + ") : format unknown " + e, e);
    }
    return date;
  }
}