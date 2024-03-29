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

package org.silverpeas.components.kmelia.search;

import org.silverpeas.components.kmelia.dao.TopicSearchDao;
import org.silverpeas.components.kmelia.model.MostInterestedQueryVO;
import org.silverpeas.components.kmelia.model.TopicSearch;
import org.silverpeas.components.kmelia.repository.TopicSearchRepository;
import org.silverpeas.core.annotation.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

/**
 * This implementation use JPA TopicSearchRepository and JDBC TopicSearchDao
 * @see TopicSearchService
 */
@Service
public class TopicSearchServiceImpl implements TopicSearchService {

  @Inject
  private TopicSearchRepository topicSearchRepo;

  @Inject
  private TopicSearchDao topicSearchDao;

  @Transactional
  @Override
  public void createTopicSearch(TopicSearch kmeliaSearch) {
    topicSearchRepo.saveAndFlush(kmeliaSearch);
  }

  @Override
  public List<MostInterestedQueryVO> getMostInterestedSearch(String instanceId) {
    return topicSearchDao.getMostInterestedSearch(instanceId);
  }
}
