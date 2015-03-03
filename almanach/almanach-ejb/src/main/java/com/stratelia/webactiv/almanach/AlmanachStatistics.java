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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.almanach;

import com.silverpeas.silverstatistics.ComponentStatisticsProvider;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachRuntimeException;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 * @author
 */
@Singleton
@Named("almanach" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class AlmanachStatistics implements ComponentStatisticsProvider {

  @Inject
  private AlmanachBm almanachBm = null;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<EventDetail> events = getEvents(spaceId, componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(events.size());
    for (EventDetail detail : events) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(detail.getDelegatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  private AlmanachBm getAlmanachBm() throws Exception {
    if (almanachBm == null) {
      throw new AlmanachRuntimeException("almanach", SilverpeasRuntimeException.ERROR,
          "AlmanachStatistics.getAlmanachBm", "CDI bootstrap error");
    }
    return almanachBm;
  }

  public Collection<EventDetail> getEvents(String spaceId, String componentId) throws Exception {
    return getAlmanachBm().getAllEvents(new EventPK("", spaceId, componentId));
  }

}
