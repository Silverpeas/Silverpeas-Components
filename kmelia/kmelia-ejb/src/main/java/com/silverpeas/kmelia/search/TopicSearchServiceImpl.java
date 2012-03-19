package com.silverpeas.kmelia.search;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.kmelia.dao.TopicSearchDao;
import com.silverpeas.kmelia.domain.TopicSearch;
import com.silverpeas.kmelia.model.MostInterestedQueryVO;
import com.silverpeas.kmelia.repository.TopicSearchRepository;

/**
 * This implementation use JPA TopicSearchRepository and JDBC TopicSearchDao
 * @see TopicSearchService
 */
@Named("topicSearchService")
public class TopicSearchServiceImpl implements TopicSearchService {

  @Inject
  private TopicSearchRepository topicSearchRepo;
  
  @Inject
  private TopicSearchDao topicSearchDao;
  
  @Override
  public void createTopicSearch(TopicSearch kmeliaSearch) {
    topicSearchRepo.saveAndFlush(kmeliaSearch);
  }

  @Override
  public List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId) {
    return topicSearchDao.getMostInterestedSearch(instanceId);
  }
}
