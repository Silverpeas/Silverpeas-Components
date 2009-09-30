/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.almanach;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBm;
import com.stratelia.webactiv.almanach.control.ejb.AlmanachBmHome;
import com.stratelia.webactiv.almanach.model.EventDetail;
import com.stratelia.webactiv.almanach.model.EventPK;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

/*
 * CVS Informations
 *
 * $Id: AlmanachStatistics.java,v 1.3 2004/06/22 16:17:26 neysseri Exp $
 *
 * $Log: AlmanachStatistics.java,v $
 * Revision 1.3  2004/06/22 16:17:26  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.2  2002/10/09 08:06:37  neysseri
 * no message
 *
 * Revision 1.1.1.1.6.1  2002/09/27 08:11:42  abudnikau
 * Remove debug
 *
 * Revision 1.1.1.1  2002/08/06 14:47:56  nchaix
 * no message
 *
 * Revision 1.1  2002/04/17 17:14:07  mguillem
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
public class AlmanachStatistics implements ComponentStatisticsInterface {

  private AlmanachBm almanachBm = null;

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Collection c = getEvents(spaceId, componentId);
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      EventDetail detail = (EventDetail) iter.next();

      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

      myCouple.setUserId(detail.getDelegatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private AlmanachBm getAlmanachBm() {
    if (almanachBm == null) {
      try {
        almanachBm = ((AlmanachBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.ALMANACHBM_EJBHOME, AlmanachBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("almanach", "AlmanachStatistics.getAlmanachBm",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }
    }
    return almanachBm;
  }

  public Collection getEvents(String spaceId, String componentId)
      throws RemoteException {
    return getAlmanachBm().getAllEvents(new EventPK("", spaceId, componentId));
  }

}
