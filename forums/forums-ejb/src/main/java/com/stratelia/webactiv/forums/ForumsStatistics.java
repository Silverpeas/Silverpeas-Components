/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.forums;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.forums.forumEntity.ejb.ForumPK;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.forums.models.Forum;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

/*
 * CVS Informations
 *
 * $Id: ForumsStatistics.java,v 1.3 2009/03/31 08:10:57 neysseri Exp $
 *
 * $Log: ForumsStatistics.java,v $
 * Revision 1.3  2009/03/31 08:10:57  neysseri
 * eShop : Livraison 2
 *
 * Revision 1.2  2004/06/22 16:26:10  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.1.1.1  2002/08/06 14:47:57  nchaix
 * no message
 *
 * Revision 1.1  2002/04/17 17:20:34  mguillem
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
public class ForumsStatistics implements ComponentStatisticsInterface {

  private ForumsBM forumsBM = null;

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList couples = new ArrayList();
    ArrayList forums = getForums(spaceId, componentId);
    Forum forum;
    for (int i = 0, n = forums.size(); i < n; i++) {
      forum = (Forum) forums.get(i);
      UserIdCountVolumeCouple couple = new UserIdCountVolumeCouple();
      couple.setUserId(forum.getCategory());
      couple.setCountVolume(1);
      couples.add(couple);
    }
    return couples;
  }

  private ForumsBM getForumsBM() {
    if (forumsBM == null) {
      try {
        ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);
        forumsBM = forumsBMHome.create();
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return forumsBM;
  }

  public ArrayList getForums(String spaceId, String componentId)
      throws RemoteException {
    return getForumsBM().getForums(new ForumPK(componentId, spaceId));
  }

}
