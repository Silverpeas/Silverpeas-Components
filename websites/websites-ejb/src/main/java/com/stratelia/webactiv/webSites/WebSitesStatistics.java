/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.webSites;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;

import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBm;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;

public class WebSitesStatistics implements ComponentStatisticsInterface {

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>();
    Collection<SiteDetail> websites = getWebSites(componentId);
    for (SiteDetail detail : websites) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId("-2"); // unknown userId
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  public Collection<SiteDetail> getWebSites(String componentId) {
    WebSiteBm webSiteEjb = EJBUtilitaire.getEJBObjectRef(JNDINames.WEBSITESBM_EJBHOME,
        WebSiteBm.class);
    return webSiteEjb.getAllWebSite(componentId);
  }
}
