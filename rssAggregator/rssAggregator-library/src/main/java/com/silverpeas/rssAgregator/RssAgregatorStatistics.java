/*
 * Copyright (C) 2000 - 2015 Silverpeas
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

package com.silverpeas.rssAgregator;

import com.silverpeas.rssAgregator.control.RssAgregatorBm;
import com.silverpeas.rssAgregator.control.RssAgregatorBmImpl;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.silverpeas.silverstatistics.ComponentStatisticsProvider;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
@Named("rssAgregator" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class RssAgregatorStatistics implements ComponentStatisticsProvider {
  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    RssAgregatorBm rss = new RssAgregatorBmImpl();
    List<SPChannel> channels = rss.getChannels(componentId);
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
