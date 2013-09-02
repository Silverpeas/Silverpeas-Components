/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.newsEdito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBException;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 *
 * @author
 */
public class NewsEditoStatistics implements ComponentStatisticsInterface {

  private NodeBm nodeBm = null;

  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<NodeDetail> details = getElements(spaceId, componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>(
        details.size());
    for (NodeDetail detail : details) {
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
        nodeBm = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBm.class);
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return nodeBm;
  }

  private Collection<NodeDetail> getElements(String spaceId, String componentId)
      throws Exception {
    // recuperation des journaux
    return getNodeBm().getFrequentlyAskedChildrenDetails(new NodePK("0", spaceId, componentId));
  }
}
