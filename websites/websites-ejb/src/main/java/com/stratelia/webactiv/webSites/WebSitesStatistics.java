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
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;

public class WebSitesStatistics implements ComponentStatisticsInterface {

  private WebSiteBm webSiteEjb = null;

  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    ArrayList<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
    Collection<SiteDetail> c = getWebSites(spaceId, componentId);
    Iterator<SiteDetail> iter = c.iterator();
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

  public Collection<SiteDetail> getWebSites(String spaceId, String componentId)
      throws RemoteException {
    getWebSiteEjb().setPrefixTableName(spaceId);
    getWebSiteEjb().setComponentId(componentId);

    Collection<SiteDetail> result = getWebSiteEjb().getAllWebSite();
    return result;
  }
}
