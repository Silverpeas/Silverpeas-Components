/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.mailinglist;

import org.silverpeas.components.mailinglist.service.MailingListServicesProvider;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Provider
@Named("mailinglist" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class MailingListStatistics implements ComponentStatisticsProvider {

  private static final String UNKNOWN_USER_ID = "-2";

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId) {
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>();
    UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
    myCouple.setUserId(UNKNOWN_USER_ID);
    MailingList ml =
        MailingListServicesProvider.getMailingListService().findMailingList(componentId);
    if (ml != null) {
      long totalNumberOfMessages =
          MailingListServicesProvider.getMessageService().getTotalNumberOfMessages(ml);
      myCouple.setCountVolume(totalNumberOfMessages);
    } else {
      SilverLogger.getLogger(this)
          .warn("space {0}, componentId {1} doesn't look like a mailinglist", spaceId, componentId);
      myCouple.setCountVolume(0);
    }
    myArrayList.add(myCouple);
    return myArrayList;
  }
}
