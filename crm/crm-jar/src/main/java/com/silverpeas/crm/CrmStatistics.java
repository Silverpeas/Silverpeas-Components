package com.silverpeas.crm;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import com.silverpeas.crm.control.ServiceFactory;
import com.silverpeas.crm.model.Crm;
import com.silverpeas.crm.model.CrmContact;
import com.silverpeas.crm.model.CrmDataInterface;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

/**
 * Class declaration
 * @author
 */
public class CrmStatistics implements ComponentStatisticsInterface {

  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    int crmsCount = getCrmsCount(spaceId, componentId);
    ArrayList<UserIdCountVolumeCouple> list = new ArrayList<UserIdCountVolumeCouple>();
    for (int i = 0; i < crmsCount; i++) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId("-2");
      myCouple.setCountVolume(1);
      list.add(myCouple);
    }

    return list;
  }

  public int getCrmsCount(String spaceId, String componentId) throws RemoteException {
    ArrayList<CrmContact> publications = new ArrayList<CrmContact>();
    CrmDataInterface dataInterface = ServiceFactory.getCrmData();
    ArrayList<Crm> crms = dataInterface.getCrms(componentId);
    if (crms != null) {
      for (Crm crm : crms) {
        publications.addAll(dataInterface.getCrmContacts(crm.getPK()));
      }
    }
    return publications.size();
  }

}
