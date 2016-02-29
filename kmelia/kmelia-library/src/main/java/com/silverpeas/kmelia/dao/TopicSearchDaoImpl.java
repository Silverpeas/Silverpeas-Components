/**
 * Copyright (C) 2000 - 2014 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.kmelia.dao;

import com.silverpeas.kmelia.model.MostInterestedQueryVO;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.persistence.jdbc.JdbcSqlQuery;
import org.silverpeas.util.ResourceLocator;
import org.silverpeas.util.SettingBundle;

import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.List;

/**
 * This class is the Jdbc Dao implementation of TopicSearchDao
 * @author ebonnet
 */
@Singleton
public class TopicSearchDaoImpl implements TopicSearchDao {

  private static final String QUERY_GET_LIST_MOST_INTERESTED_QUERY =
      "count(*) as nb, query FROM sc_kmelia_search WHERE instanceid = ? GROUP BY query, " +
          "language ORDER BY nb DESC, query";

  private static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.kmelia.settings.kmeliaSettings");

  @Override
  public List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId) {
    // Set the max number of result to retrieve
    JdbcSqlQuery jdbcSqlQuery =
        JdbcSqlQuery.createSelect(QUERY_GET_LIST_MOST_INTERESTED_QUERY, instanceId).configure(
            config -> config.withResultLimit(
                settings.getInteger("kmelia.stats.most.interested.query.limit", 10)));
    List<MostInterestedQueryVO> mostInterestedQueries = null;
    try {
      mostInterestedQueries = jdbcSqlQuery
          .execute(row -> new MostInterestedQueryVO(row.getString("query"), row.getInt("nb")));
    } catch (SQLException e) {
      SilverTrace.error("kmelia", TopicSearchDaoImpl.class.getSimpleName(),
          "Problem to execute SQL query " + QUERY_GET_LIST_MOST_INTERESTED_QUERY +
              " with intanceId = " + instanceId, e);
    }
    return mostInterestedQueries;
  }
}
