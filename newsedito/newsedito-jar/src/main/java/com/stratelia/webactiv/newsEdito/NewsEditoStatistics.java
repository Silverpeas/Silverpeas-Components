/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.stratelia.webactiv.newsEdito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 * @author
 */
public class NewsEditoStatistics implements ComponentStatisticsInterface {

  private NodeBm nodeBm = null;

  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
    Collection<NodeDetail> c = getElements(spaceId, componentId);
    Iterator<NodeDetail> iter = c.iterator();

    while (iter.hasNext()) {
      NodeDetail detail = iter.next();

      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();

      myCouple.setUserId(detail.getCreatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }

    return myArrayList;
  }

  private NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        nodeBm = ((NodeBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NODEBM_EJBHOME, NodeBmHome.class)).create();
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return nodeBm;
  }

  private Collection<NodeDetail> getElements(String spaceId, String componentId)
      throws Exception {
    // recuperation des journaux
    Collection<NodeDetail> archives = getNodeBm().getFrequentlyAskedChildrenDetails(
        new NodePK("0", spaceId, componentId));
    return archives;
  }
}
