/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.hyperlink;

import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;

/*
 * CVS Informations
 *
 * $Id:
 *
 * $Log:
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class HyperlinkStatistics implements ComponentStatisticsInterface {

  public Collection getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList myArrayList = new ArrayList();

    UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
    myCouple.setUserId("-2"); // unknown userId
    myCouple.setCountVolume(1);
    myArrayList.add(myCouple);

    return myArrayList;
  }

}
