/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mailinglist;

import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MailingListStatistics implements ComponentStatisticsInterface {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
    UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
    myCouple.setUserId("-2"); // unknown userId
    MailingList ml = ServicesFactory.getMailingListService().findMailingList(componentId);
    SilverTrace.debug("mailingList", "MailingListStatistics.getVolume()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = " + componentId);
    if (ml != null) {
      int totalNumberOfMessages = ServicesFactory.getMessageService().getTotalNumberOfMessages(ml);
      myCouple.setCountVolume(totalNumberOfMessages);
    } else {
      SilverTrace.warn("mailingList", "MailingListStatistics.getVolume()",
          "root.MSG_GEN_ENTER_METHOD",
          "space = " + spaceId + ", componentId = " + componentId +
              " doesn't look like a mailinglist");
      myCouple.setCountVolume(0);
    }
    myArrayList.add(myCouple);
    return myArrayList;
  }
}