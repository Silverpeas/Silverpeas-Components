/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on 25 oct. 2004
 *
 */
package com.silverpeas.projectManager.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.exception.UtilException;

/**
 * @author neysseri
 */
public class ProjectManagerCalendarDAO {

  // the date format used in database to represent a date
  private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
  private final static String PROJECTMANAGER_CALENDAR_TABLENAME = "SC_ProjectManager_Calendar";

  public static void addHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException, UtilException {
    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.addHolidayDate()",
        "root.MSG_GEN_ENTER_METHOD", holiday.getDate().toString());

    if (!isHolidayDate(con, holiday)) {
      StringBuilder insertStatement = new StringBuilder(128);
      insertStatement.append("INSERT INTO ").append(
          PROJECTMANAGER_CALENDAR_TABLENAME);
      insertStatement.append(" VALUES ( ? , ? , ? )");
      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(insertStatement.toString());

        prepStmt.setString(1, date2DBDate(holiday.getDate()));
        prepStmt.setInt(2, holiday.getFatherId());
        prepStmt.setString(3, holiday.getInstanceId());

        prepStmt.executeUpdate();
      } finally {
        DBUtil.close(prepStmt);
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

  public static boolean isHolidayDate(Connection con, HolidayDetail holiday)
      throws SQLException {
    StringBuilder query = new StringBuilder(128);
    query.append("select * ");
    query.append("from ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" where holidayDate = ? ");
    query.append(" and fatherId = ? ");
    query.append(" and instanceId = ? ");

    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.isHolidayDate()",
        "root.MSG_GEN_PARAM_VALUE", "date = " + holiday.getDate().toString());

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
    List<Date> holidayDates = new ArrayList<Date>();
    StringBuilder query = new StringBuilder(128);
    query.append("SELECT * ");
    query.append("FROM ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" WHERE instanceId = ? ");
    query.append("ORDER BY holidayDate ASC");

    SilverTrace.info("projectManager",
        "ProjectManagerCalendarDAO.getHolidayDates()",
        "root.MSG_GEN_PARAM_VALUE", "instanceId = " + instanceId);

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates.add(dbDate2Date(rs.getString("holidayDate"), "holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static List<Date> getHolidayDates(Connection con, String instanceId,
      Date beginDate, Date endDate) throws SQLException {
    List<Date> holidayDates = new ArrayList<Date>();
    StringBuilder query = new StringBuilder(128);
    query.append("SELECT * ");
    query.append("FROM ").append(PROJECTMANAGER_CALENDAR_TABLENAME);
    query.append(" WHERE instanceId = ? ");
    query.append(" AND ? <= holidayDate ");
    query.append(" AND holidayDate <= ? ");
    query.append("ORDER BY holidayDate ASC");

    SilverTrace.info("projectManager", "ProjectManagerCalendarDAO.getHolidayDates()",
        "root.MSG_GEN_PARAM_VALUE", "instanceId = " + instanceId + ", beginDate=" +
            beginDate.toString() + ", endDate=" + endDate.toString());

    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = con.prepareStatement(query.toString());
      stmt.setString(1, instanceId);
      stmt.setString(2, date2DBDate(beginDate));
      stmt.setString(3, date2DBDate(endDate));
      rs = stmt.executeQuery();
      while (rs.next()) {
        holidayDates.add(dbDate2Date(rs.getString("holidayDate"), "holidayDate"));
      }
    } finally {
      DBUtil.close(rs, stmt);
    }
    return holidayDates;
  }

  public static String date2DBDate(Date date) {
    String dbDate = formatter.format(date);
    return dbDate;
  }

  private static Date dbDate2Date(String dbDate, String fieldName)
      throws SQLException {
    Date date = null;
    try {
      date = formatter.parse(dbDate);
    } catch (ParseException e) {
      throw new SQLException("ProjectManagerCalendarDAO : dbDate2Date(" + fieldName +
          ") : format unknown " + e.toString());
    }
    return date;
  }
}