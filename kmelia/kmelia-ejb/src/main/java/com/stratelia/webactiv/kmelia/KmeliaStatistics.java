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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.kmelia;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Class declaration
 * @author
 */
public class KmeliaStatistics implements ComponentStatisticsInterface {

  private PublicationBm publicationBm = null;

  /**
   * Method declaration
   * @param spaceId
   * @param componentId
   * @return
   * @see
   */
  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Collection c = getElements(spaceId, componentId);
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      PublicationDetail detail = (PublicationDetail) iter.next();

      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

      myCouple.setUserId(detail.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("kmelia", "KmeliaStatistics.getPublicationBm",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  /**
   * Method declaration
   * @param spaceId
   * @param componentId
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getElements(String spaceId, String componentId)
      throws RemoteException {
    Collection result = getPublicationBm().getAllPublications(
        new PublicationPK("useless", componentId));

    return result;
  }

}
