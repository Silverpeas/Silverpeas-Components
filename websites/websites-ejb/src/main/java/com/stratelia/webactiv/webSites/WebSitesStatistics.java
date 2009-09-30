/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.webSites;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBm;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBmHome;

/*
 * CVS Informations
 *
 * $Id: WebSitesStatistics.java,v 1.3 2004/07/27 14:25:50 neysseri Exp $
 *
 * $Log: WebSitesStatistics.java,v $
 * Revision 1.3  2004/07/27 14:25:50  neysseri
 * Le rôle " Gestionnaire " n'était pas pris en compte correctement. Il avait les mêmes privilèges que le rôle " Lecteur ".
 * + Nettoyage eclipse
 *
 * Revision 1.2  2004/06/22 16:33:00  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.1.1.1  2002/08/06 14:48:01  nchaix
 * no message
 *
 * Revision 1.3  2002/05/16 10:14:22  mguillem
 * merge branch V2001_Statistics01
 *
 * Revision 1.2.6.1  2002/04/22 11:28:59  mguillem
 * alimentation volume stat
 *
 * Revision 1.2  2002/04/17 17:23:58  mguillem
 * alimentation des stats de volume
 *
 * Revision 1.1  2002/04/12 07:08:28  sleroux
 * WebSites interface Stats
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
public class WebSitesStatistics implements ComponentStatisticsInterface {

  private WebSiteBm webSiteEjb = null;

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Collection c = getWebSites(spaceId, componentId);
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      iter.next();

      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      // ATTENTION getAuthor ne renvoie pas l'id mais Nom+Prénom
      // myCouple.setUserId(detail.getAuthor());
      myCouple.setUserId("-2"); // unknown userId
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private WebSiteBm getWebSiteEjb() {
    if (webSiteEjb == null) {
      try {
        WebSiteBmHome webSiteEjbHome = (WebSiteBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.WEBSITESBM_EJBHOME, WebSiteBmHome.class);
        webSiteEjb = webSiteEjbHome.create();
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return webSiteEjb;
  }

  public Collection getWebSites(String spaceId, String componentId)
      throws RemoteException {
    getWebSiteEjb().setPrefixTableName(spaceId);
    getWebSiteEjb().setComponentId(componentId);

    Collection result = getWebSiteEjb().getAllWebSite();
    return result;
  }
}
