package com.silverpeas.mailinglist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

public class MailingListStatistics implements ComponentStatisticsInterface {

  @SuppressWarnings("unchecked")
  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    MailingList ml = ServicesFactory.getMailingListService().findMailingList(
        componentId);
    int totalNumberOfMessages = ServicesFactory.getMessageService()
        .getTotalNumberOfMessages(ml);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
    UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
    myCouple.setUserId("-2"); // unknown userId
    myCouple.setCountVolume(totalNumberOfMessages);
    myArrayList.add(myCouple);
    return myArrayList;
  }
}