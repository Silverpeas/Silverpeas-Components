package com.silverpeas.kmelia.stats;

import com.silverpeas.kmelia.model.StatsFilterVO;

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
  public Integer getStatisticActivityByPeriod(StatsFilterVO statFilter);

}
