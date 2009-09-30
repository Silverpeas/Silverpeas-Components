/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.kmax;

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
 * 
 * 
 * @author
 */
public class KmaxStatistics implements ComponentStatisticsInterface {

  private PublicationBm publicationBm = null;

  /**
   * Method declaration
   * 
   * 
   * @param spaceId
   * @param componentId
   * 
   * @return
   * 
   * @see
   */
  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Collection c = getPublications(spaceId, componentId);
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
   * 
   * 
   * @return
   * 
   * @see
   */
  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("kmax", "KmaxStatistics.getPublicationBm",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  /**
   * Method declaration
   * 
   * 
   * @param spaceId
   * @param componentId
   * 
   * @return
   * 
   * @throws RemoteException
   * 
   * @see
   */
  public Collection getPublications(String spaceId, String componentId)
      throws RemoteException {
    Collection result = getPublicationBm().getAllPublications(
        new PublicationPK("useless", spaceId, componentId));

    return result;
  }

}
