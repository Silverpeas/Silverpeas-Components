/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.blog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.silverpeas.util.DBUtil;

public class PostDAO {
  static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  private PostDAO () {
  }

  public static void createDateEvent(Connection con, String pubId, Date dateEvent,
      String instanceId) throws SQLException {
    // Création
    PreparedStatement prepStmt = null;
    try {
      // création de la requete
      String query = "insert into SC_Blog_Post (pubId, dateEvent, instanceId) values (?, ?, ?)";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(pubId));
      prepStmt.setString(2, Long.toString((dateEvent).getTime()));
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static Date getDateEvent(Connection con, String pubId) throws SQLException {
    // récupérer la date
    String query = "select dateEvent from SC_Blog_Post where pubId = ? ";
    Date dateEvent = new Date();
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(pubId));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        // recuperation de la date
        dateEvent = new Date(Long.parseLong(rs.getString("dateEvent")));
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return dateEvent;
  }

  public static void deleteDateEvent(Connection con, String pubId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SC_Blog_Post where pubId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(pubId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void updateDateEvent(Connection con, String pubId, Date dateEvent)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      // mettre à jour la date d'évènement
      String query = "update SC_Blog_Post set dateEvent = ? where pubId = ?";
      // initialisation des paramètres
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, "" + dateEvent.getTime());
      prepStmt.setInt(2, Integer.parseInt(pubId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static Collection<String> getAllEvents(Connection con, String instanceId)
      throws SQLException {
    // récupérer les derniers posts par date d'évènement
    List<String> listEvents = new ArrayList<>();
    String query =
        "select pubId from SC_Blog_Post where instanceId = ? order by dateEvent DESC, pubId DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        String pubId = String.valueOf(rs.getInt("pubId"));
        listEvents.add(pubId);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listEvents;
  }

  public static Collection<Date> getAllDateEvents(Connection con, String instanceId)
      throws SQLException {
    ArrayList<Date> dateEvents = null;
    String query =
        "select dateEvent from SC_Blog_Post where instanceId = ? order by dateEvent DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      rs = prepStmt.executeQuery();
      dateEvents = new ArrayList<>();
      while (rs.next()) {
        dateEvents.add(new Date(Long.parseLong(rs.getString("dateEvent"))));
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return dateEvents;
  }

  public static Collection<String> getEventsByDates(Connection con, String instanceId,
      String beginDate, String endDate) throws SQLException, ParseException {
    // récupérer les posts par date d'évènement entre 2 dates
    ArrayList<String> listEvents = null;

    String query =
        "select pubId from SC_Blog_Post where instanceId = ? and dateEvent >= ? and dateEvent <= " +
            "? order by dateEvent DESC";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, instanceId);
      prepStmt.setString(2, Long.toString((formatter.parse(beginDate)).getTime()));
      prepStmt.setString(3, Long.toString((formatter.parse(endDate)).getTime()));
      rs = prepStmt.executeQuery();
      listEvents = new ArrayList<>();
      while (rs.next()) {
        String pubId = "" + rs.getInt("pubId");
        listEvents.add(pubId);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return listEvents;
  }
}
