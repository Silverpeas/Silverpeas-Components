package com.silverpeas.rssAgregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.rssAgregator.control.RssAgregatorBm;
import com.silverpeas.rssAgregator.control.RssAgregatorBmImpl;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

public class RssAgregatorStatistics implements ComponentStatisticsInterface {
  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    RssAgregatorBm rss = new RssAgregatorBmImpl();
    Collection channels = rss.getChannels(componentId);
    Iterator iter = channels.iterator();
    while (iter.hasNext()) {
      SPChannel channel = (SPChannel) iter.next();

      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

      myCouple.setUserId(channel.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }
}
