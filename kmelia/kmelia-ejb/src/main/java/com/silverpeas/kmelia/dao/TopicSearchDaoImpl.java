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
      "SELECT count(*) as nb, query FROM public.sc_kmelia_search WHERE instanceid = ? GROUP BY query, language ORDER BY nb DESC, query";

  private static ResourceLocator settings =
      new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", "fr");

  @Override
  public List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId) {
    // Set the max number of result to retrieve
    getJdbcTemplate().setMaxRows(settings.getInteger("kmelia.stats.most.interested.query.limit", 10));

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
