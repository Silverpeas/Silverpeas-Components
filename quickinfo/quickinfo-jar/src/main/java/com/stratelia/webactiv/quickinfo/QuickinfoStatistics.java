/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.quickinfo;

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

/*
 * CVS Informations
 *
 * $Id: QuickinfoStatistics.java,v 1.4 2004/11/05 13:45:43 neysseri Exp $
 *
 * $Log: QuickinfoStatistics.java,v $
 * Revision 1.4  2004/11/05 13:45:43  neysseri
 * Nettoyage sources
 *
 * Revision 1.3  2003/10/28 01:15:34  tleroi
 * Correct very curious wysiwyg bug...
 *
 * Revision 1.2  2002/10/09 08:06:37  neysseri
 * no message
 *
 * Revision 1.1.1.1.6.1  2002/09/27 08:11:42  abudnikau
 * Remove debug
 *
 * Revision 1.1.1.1  2002/08/06 14:48:01  nchaix
 * no message
 *
 * Revision 1.2  2002/04/17 17:22:32  mguillem
 * alimentation des stats de volume
 *
 * Revision 1.1  2002/04/05 16:58:24  mguillem
 * alimentation des stats de volume
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class QuickinfoStatistics implements ComponentStatisticsInterface {

  private PublicationBm publicationBm = null;

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Collection c = getQuickInfos(spaceId, componentId);
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

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("quickinfo",
            "QuickinfoStatistics.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  public Collection getQuickInfos(String spaceId, String componentId)
      throws RemoteException {
    Collection result = getPublicationBm().getOrphanPublications(
        new PublicationPK("", spaceId, componentId));

    return result;
  }

}
