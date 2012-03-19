package com.silverpeas.kmelia.dao;

import java.util.List;

import com.silverpeas.kmelia.model.MostInterestedQueryVO;

public interface TopicSearchDao {

  /**
   * @param instanceId the current instance identifier (i.e. kmeliaXXX)
   * @return the list of most interested query for the application given in parameter
   */
  List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId);

}
