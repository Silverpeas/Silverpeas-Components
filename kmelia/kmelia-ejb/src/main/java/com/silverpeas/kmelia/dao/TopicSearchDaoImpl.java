/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.kmelia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.silverpeas.kmelia.model.MostInterestedQueryVO;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class is the Jdbc Dao implementation of TopicSearchDao
 * @author ebonnet
 */
@Named("topicSearchDao")
public class TopicSearchDaoImpl extends JdbcDaoSupport implements TopicSearchDao {

  private static final String QUERY_GET_LIST_MOST_INTERESTED_QUERY =
      "SELECT count(*) as nb, query FROM sc_kmelia_search WHERE instanceid = ? GROUP BY query, language ORDER BY nb DESC, query";

  private static ResourceLocator settings =
      new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "fr");

  @Override
  public List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId) {
    // Set the max number of result to retrieve
    getJdbcTemplate().setMaxRows(
        settings.getInteger("kmelia.stats.most.interested.query.limit", 10));

    return getJdbcTemplate().query(new MyPreparedStatementCreator(instanceId),
        new MostInterestedQueryRowMapper());
  }

  private static class MostInterestedQueryRowMapper implements
      ParameterizedRowMapper<MostInterestedQueryVO> {

    @Override
    public MostInterestedQueryVO mapRow(ResultSet rs, int rowNum) throws SQLException {
      MostInterestedQueryVO interestedQuery =
          new MostInterestedQueryVO(rs.getString("query"), rs.getInt("nb"));
      return interestedQuery;
    }
  }

  /**
   * Inner class to create a Spring PreparedStatementCreator object
   */
  class MyPreparedStatementCreator implements PreparedStatementCreator {

    private String instanceId;

    public MyPreparedStatementCreator(String instanceId) {
      this.instanceId = instanceId;
    }

    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
      PreparedStatement ps = connection.prepareStatement(QUERY_GET_LIST_MOST_INTERESTED_QUERY);
      ps.setString(1, getInstanceId());
      return ps;
    }

    /**
     * @return the instanceId
     */
    public String getInstanceId() {
      return instanceId;
    }
  }
}
