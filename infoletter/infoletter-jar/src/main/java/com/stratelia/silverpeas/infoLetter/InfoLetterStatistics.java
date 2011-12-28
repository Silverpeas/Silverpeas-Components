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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.infoLetter;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.infoLetter.control.ServiceFactory;
import com.stratelia.silverpeas.infoLetter.model.InfoLetter;
import com.stratelia.silverpeas.infoLetter.model.InfoLetterDataInterface;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 *
 * @author
 */
public class InfoLetterStatistics implements ComponentStatisticsInterface {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    List publications = getInfoLetters(componentId);
    if (publications == null) {
      return null;
    }

    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>(
        publications.size());

    for (int i = 0; i < publications.size(); i++) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId("-2");
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  public List getInfoLetters(String componentId) throws RemoteException {
    List publications = new ArrayList();
    InfoLetterDataInterface dataInterface = ServiceFactory.getInfoLetterData();
    List<InfoLetter> listLettres = (List<InfoLetter>) dataInterface.getInfoLetters(componentId);
    if (listLettres != null) {
      for (InfoLetter defaultLetter : listLettres) {
        List listParutions = dataInterface.getInfoLetterPublications(defaultLetter.getPK());
        publications.addAll(listParutions);
      }
    }
    return publications;
  }

}
