/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.kmelia.search;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.silverpeas.kmelia.stats.StatisticService;

/**
 * Factory of KmeliaSearchService, you can change the implementation using spring IoC configuration
 * @author ebonnet
 */
public class KmeliaSearchServiceFactory implements ApplicationContextAware {

  public static final String TOPIC_SEARCH_SERVICE = "topicSearchService";
  public static final String STATISTIC_SERVICE = "statisticService";

  private ApplicationContext context;
  private static KmeliaSearchServiceFactory instance;

  /**
   * Default private constructor
   */
  private KmeliaSearchServiceFactory() {
  }

  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.context = context;
  }

  /**
   * Gets an instance of this KmeliaSearchServiceFactory class.
   * @return a KmeliaSearchServiceFactory instance.
   */
  public static KmeliaSearchServiceFactory getInstance() {
    synchronized (KmeliaSearchServiceFactory.class) {
      if (KmeliaSearchServiceFactory.instance == null) {
        KmeliaSearchServiceFactory.instance = new KmeliaSearchServiceFactory();
      }
    }
    return KmeliaSearchServiceFactory.instance;
  }

  public static TopicSearchService getTopicSearchService() {
    return (TopicSearchService) getInstance().context.getBean(TOPIC_SEARCH_SERVICE);
  }

  public static StatisticService getStatisticService() {
    return (StatisticService) getInstance().context.getBean(STATISTIC_SERVICE);
  }
}
