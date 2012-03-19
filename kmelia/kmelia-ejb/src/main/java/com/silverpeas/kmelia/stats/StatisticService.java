package com.silverpeas.kmelia.stats;

import com.silverpeas.kmelia.model.StatsFilterVO;
import com.silverpeas.kmelia.model.TopicSearchStatsVO;

public interface StatisticService {

  /**
   * 
   * @param statFilter
   * @return
   */
  public Integer getNbConsultedPublication(StatsFilterVO statFilter);
  
  /**
   * 
   * @param statFilter
   * @return
   */
  public TopicSearchStatsVO getStatisticActivityByPeriod(StatsFilterVO statFilter);

}
