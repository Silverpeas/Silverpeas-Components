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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.kmelia.dao;

import org.silverpeas.components.kmelia.model.MostInterestedQueryVO;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.SQLException;
import java.util.List;

/**
 * This class is the Jdbc Dao implementation of TopicSearchDao
 * @author ebonnet
 */
@Repository
public class TopicSearchDaoImpl implements TopicSearchDao {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.kmelia.settings.kmeliaSettings");
  private static final String QUERY = "query";

  @Override
  public List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId) {
    JdbcSqlQuery jdbcSqlQuery =
        JdbcSqlQuery.select("count(*) as nb, query").from("sc_kmelia_search")
            .where("instanceid = ?", instanceId)
            .groupBy(QUERY, "language")
            .orderBy("nb DESC", QUERY)
            .configure(config ->
                config.withResultLimit(
                    settings.getInteger("kmelia.stats.most.interested.query.limit", 10)));
    List<MostInterestedQueryVO> mostInterestedQueries = null;
    try {
      mostInterestedQueries = jdbcSqlQuery
          .execute(row -> new MostInterestedQueryVO(row.getString(QUERY), row.getInt("nb")));
    } catch (SQLException e) {
      SilverLogger.getLogger(this)
          .error("Problem to execute SQL query " + jdbcSqlQuery.getSqlQuery() +
              " with intanceId = " + instanceId, e);
    }
    return mostInterestedQueries;
  }
}
