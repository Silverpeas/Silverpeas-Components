/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.silverpeas.whitePages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.whitePages.control.CardManager;
import com.silverpeas.whitePages.model.Card;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

/*
 * CVS Informations
 *
 * $Id: WhitePagesStatistics.java,v 1.2 2004/06/22 16:33:40 neysseri Exp $
 *
 * $Log: WhitePagesStatistics.java,v $
 * Revision 1.2  2004/06/22 16:33:40  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.1.1.1  2002/08/06 14:48:02  nchaix
 * no message
 *
 * Revision 1.2  2002/05/16 10:14:22  mguillem
 * merge branch V2001_Statistics01
 *
 * Revision 1.1.2.1  2002/04/29 07:07:40  pbialevich
 * add statistics
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
public class WhitePagesStatistics implements ComponentStatisticsInterface {

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();
    Collection c = getWhitePages(spaceId, componentId);
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      Card detail = (Card) iter.next();
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

      myCouple.setUserId(detail.getUserId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  public Collection getWhitePages(String spaceId, String componentId)
      throws WhitePagesException {
    Collection result = CardManager.getInstance().getCards(componentId);
    return result;
  }

}
