package com.silverpeas.webpages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

public class WebPagesStatistics implements ComponentStatisticsInterface {
  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    List myArrayList = new ArrayList();

    UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
    myCouple.setUserId("-2"); // unknown userId
    myCouple.setCountVolume(1);
    myArrayList.add(myCouple);

    return myArrayList;
  }
}