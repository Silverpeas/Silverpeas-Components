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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.classifieds;

import com.silverpeas.classifieds.control.ClassifiedService;
import com.silverpeas.classifieds.control.ClassifiedServiceFactory;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import com.silverpeas.classifieds.model.ClassifiedDetail;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

/**
 * Class declaration
 * @author
 */
public class ClassifiedsStatistics implements ComponentStatisticsInterface {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
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
    ClassifiedServiceFactory factory = ClassifiedServiceFactory.getFactory();
    ClassifiedService service = factory.getClassifiedService();
    Collection<ClassifiedDetail> result = service.getAllClassifieds(componentId);
    return result;
  }

}
