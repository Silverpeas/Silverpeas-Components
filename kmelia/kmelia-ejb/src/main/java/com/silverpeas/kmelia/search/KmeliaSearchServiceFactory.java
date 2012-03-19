package com.silverpeas.kmelia.search;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.silverpeas.kmelia.stats.StatisticService;

/**
 * Factory of KmeliaSearchService, you can change the implementation using spring
 * IoC configuration
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
