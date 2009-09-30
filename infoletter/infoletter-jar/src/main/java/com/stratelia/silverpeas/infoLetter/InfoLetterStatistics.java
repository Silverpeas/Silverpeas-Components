/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.infoLetter;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import com.stratelia.silverpeas.infoLetter.control.ServiceFactory;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class InfoLetterStatistics implements ComponentStatisticsInterface {

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Vector publications = getInfoLetters(spaceId, componentId);

    if (publications == null)
      return null;

    for (int i = 0; i < publications.size(); i++) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId("-2");
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  public Vector getInfoLetters(String spaceId, String componentId)
      throws RemoteException {
    Vector publications = new Vector();
    InfoLetterDataInterface dataInterface = ServiceFactory.getInfoLetterData();
    Vector listLettres = dataInterface.getInfoLetters(componentId);
    if (listLettres != null) {
      Vector listParutions = null;
      for (int i = 0; i < listLettres.size(); i++) {
        InfoLetter defaultLetter = (InfoLetter) listLettres.elementAt(i);
        listParutions = dataInterface.getInfoLetterPublications(defaultLetter
            .getPK());
        publications.addAll(listParutions);
      }
    }

    return publications;
  }

}
