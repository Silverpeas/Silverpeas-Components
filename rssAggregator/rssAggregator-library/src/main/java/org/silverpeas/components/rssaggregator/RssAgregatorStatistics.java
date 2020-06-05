/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.rssaggregator;

import org.silverpeas.components.rssaggregator.model.SPChannel;
import org.silverpeas.components.rssaggregator.service.RssAggregator;
import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
@Named("rssAgregator" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class RssAgregatorStatistics implements ComponentStatisticsProvider {

  @Inject
  private RssAggregator rssAggregator;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws SilverpeasException {
    List<SPChannel> channels = rssAggregator.getChannels(componentId);
    List<UserIdCountVolumeCouple> statsList = new ArrayList<>(channels.size());
    for (SPChannel channel : channels) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(channel.getCreatorId());
      myCouple.setCountVolume(1);
      statsList.add(myCouple);
    }
    return statsList;
  }
}
