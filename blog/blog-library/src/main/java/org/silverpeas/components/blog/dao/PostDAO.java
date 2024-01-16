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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.blog.dao;

import org.apache.commons.lang3.time.FastDateFormat;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.select;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.streamBySplittingOn;

public class PostDAO {

  private static final FastDateFormat FORMATTER = FastDateFormat.getInstance("yyyy/MM/dd");
  private static final String BLOG_POST_TABLE_NAME = "SC_Blog_Post";
  private static final String PUB_ID = "pubId";
  private static final String DATE_EVENT = "dateEvent";
  private static final String EVENT_PERIOD_CLAUSE = "dateEvent >= ? and dateEvent <= ?";
  private static final String INSTANCE_ID_CLAUSE = "instanceId = ?";
  private static final String DESC = " DESC";
  private static final String ORDER_BY_DATE = DATE_EVENT + DESC;
  private static final String[] ORDER_BY_DATE_AND_PUB_ID = {DATE_EVENT + DESC, PUB_ID + DESC};

  private PostDAO () {
  }

  public static void create(Connection con, String pubId, Date dateEvent,
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

  public static Map<String, Date> getEventDateIndexedByPost(final Collection<String> pubIds)
      throws SQLException {
    return streamBySplittingOn(pubIds,
            idBatch -> select(PUB_ID + ", dateEvent")
                .from(BLOG_POST_TABLE_NAME)
                .where(PUB_ID)
                .in(idBatch.stream().map(Integer::parseInt).collect(toList()))
                .execute(r -> Pair.of(r.getString(1), new Date(Long.parseLong(r.getString(2))))))
        .collect(toMap(Pair::getFirst, Pair::getSecond));
  }

  public static void delete(Connection con, String pubId) throws SQLException {
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

  public static void update(Connection con, String pubId, Date dateEvent)
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

  public static Collection<String> getAllPostIds(Connection con, String instanceId)
      throws SQLException {
    return select(PUB_ID)
        .from(BLOG_POST_TABLE_NAME)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .orderBy(ORDER_BY_DATE_AND_PUB_ID)
        .executeWith(con, r -> String.valueOf(r.getInt(PUB_ID)));
  }

  public static Collection<Date> getAllEventDates(Connection con, String instanceId)
      throws SQLException {
    return select("DISTINCT " + DATE_EVENT)
        .from(BLOG_POST_TABLE_NAME)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .orderBy(ORDER_BY_DATE)
        .executeWith(con, r -> new Date(Long.parseLong(r.getString(DATE_EVENT))));
  }

  public static Collection<String> getPostInRange(Connection con, String instanceId,
      String beginDate, String endDate) throws SQLException, ParseException {
    return select(PUB_ID)
        .from(BLOG_POST_TABLE_NAME)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .and(EVENT_PERIOD_CLAUSE,
            Long.toString(FORMATTER.parse(beginDate).getTime()),
            Long.toString(FORMATTER.parse(endDate).getTime()))
        .orderBy(ORDER_BY_DATE_AND_PUB_ID)
        .executeWith(con, r -> String.valueOf(r.getInt(PUB_ID)));
  }
}
