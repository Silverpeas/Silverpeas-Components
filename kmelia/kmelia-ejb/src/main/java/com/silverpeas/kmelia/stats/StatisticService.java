/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.kmelia.stats;

import com.silverpeas.kmelia.model.StatisticActivityVO;
import com.silverpeas.kmelia.model.StatsFilterVO;

public interface StatisticService {

  /**
   * @param statFilter the statistic filter (Date range, group identifier, application and topic
   * identifier)
   * @return the number of consulted publications (number of access to publications) which respect
   * the statistic filter parameters. If statistic filter is null return -1.
   */
  public Integer getNbConsultedPublication(StatsFilterVO statFilter);

  /**
   * @param statFilter the statistic filter (Date range, group identifier, application and topic
   * identifier)
   * @return the number of statistic activity it means the number of created or modified publication
   * which respect the statistic filter given in parameter. If statistic filter is null return -1.
   */
  public Integer getNbStatisticActivityByPeriod(StatsFilterVO statFilter);

  /**
   * @param statFilter the statistic filter (Date range, group identifier, application and topic
   * identifier)
   * @return a StatisticActivityVO which contains the detail with number of created and the number
   * of modified publications in a specific time interval (between statFilter.startDate and
   * statFilter.endDate)
   */
  public StatisticActivityVO getStatisticActivity(StatsFilterVO statFilter);

  /**
   * @param statFilter the statistic filter (Date range, application and topic identifier, group
   * identifier)
   * @return the number of different consulted publications. If statistic filter is null return -1.
   */
  public Integer getNumberOfDifferentConsultedPublications(StatsFilterVO statFilter);
}
