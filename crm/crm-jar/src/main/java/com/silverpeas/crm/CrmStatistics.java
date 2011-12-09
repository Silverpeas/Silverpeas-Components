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

package com.silverpeas.crm;

import com.silverpeas.crm.control.CrmDataManager;
import com.silverpeas.crm.control.ServiceFactory;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class CrmStatistics implements ComponentStatisticsInterface {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    int crmsCount = getCrmsCount(componentId);
    List<UserIdCountVolumeCouple> result = new ArrayList<UserIdCountVolumeCouple>(crmsCount);
    for (int i = 0; i < crmsCount; i++) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId("-2");
      myCouple.setCountVolume(1);
      result.add(myCouple);
    }
    return result;
  }

  public int getCrmsCount(String componentId) throws RemoteException {
    ArrayList<CrmContact> publications = new ArrayList<CrmContact>();
    CrmDataManager dataManager = ServiceFactory.getCrmData();
    List<Crm> crms = dataManager.listAllCrms(componentId);
    if (crms != null && !crms.isEmpty()) {
      for (Crm crm : crms) {
        publications.addAll(dataManager.listContactsOfCrm(crm.getPK()));
      }
    }
    return publications.size();
  }

}
