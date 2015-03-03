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
package com.silverpeas.classifieds;

import com.silverpeas.classifieds.control.ClassifiedService;
import com.silverpeas.classifieds.control.ClassifiedServiceProvider;
import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.silverpeas.silverstatistics.ComponentStatisticsProvider;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import javax.inject.Named;
import javax.inject.Singleton;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

@Singleton
@Named("classifieds" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class ClassifiedsStatistics implements ComponentStatisticsProvider {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList<UserIdCountVolumeCouple> myArrayList = new ArrayList<>();
    Collection<ClassifiedDetail> c = getElements(spaceId, componentId);
    for (ClassifiedDetail classified : c) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(classified.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  public Collection<ClassifiedDetail> getElements(String spaceId, String componentId)
      throws RemoteException {
    ClassifiedService service = ClassifiedServiceProvider.getClassifiedService();
    return service.getAllClassifieds(componentId);
  }

}
