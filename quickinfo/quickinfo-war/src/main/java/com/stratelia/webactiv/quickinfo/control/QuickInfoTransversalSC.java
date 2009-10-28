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
package com.stratelia.webactiv.quickinfo.control;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import javax.ejb.EJBException;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * 
 * @author squere
 * @version
 */
public class QuickInfoTransversalSC {

  private String userId = null;

  private ResourceLocator message = null;
  private PublicationBm publicationBm = null;
  private OrganizationController organizationControl = null;

  /** Creates new QuickInfoSessionController */
  public QuickInfoTransversalSC() {

  }

  public void init(MainSessionController mainSC) {
    this.userId = mainSC.getUserId();
    SilverTrace.info("quickinfo", "QuickInfoTransversalSC.init()",
        "root.MSG_GEN_PARAM_VALUE", "Init Quick Info : User=" + userId);
  }

  public String getUserId() {
    return userId;
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class)).create();
      } catch (Exception e) {
        SilverTrace.error("quickinfo",
            "QuickInfoTransversalSC.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }

    }
    return publicationBm;
  }

  private Collection filterVisibleQuickInfos(Collection all) {
    ArrayList result = new ArrayList();
    Iterator qi = all.iterator();

    Date now = new Date();

    SilverTrace.info("quickinfo",
        "QuickInfoTransversalSC.filterVisibleQuickInfos()",
        "root.MSG_GEN_PARAM_VALUE", "Enter filterVisibleQuickInfos");
    try {
      SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
      now = format.parse(format.format(now));
    } catch (Exception e) {
      SilverTrace.error("quickinfo",
          "QuickInfoTransversalSC.filterVisibleQuickInfos()",
          "quickinfo.PARSE_ERROR", e);
    }

    while (qi.hasNext()) {
      PublicationDetail detail = (PublicationDetail) qi.next();
      if (detail.getEndDate() == null) {
        if (detail.getBeginDate() == null)
          result.add(detail);
        else if (detail.getBeginDate().compareTo(now) <= 0)
          result.add(detail);
      } else {
        if (detail.getBeginDate() == null) {
          if (detail.getEndDate().compareTo(now) >= 0)
            result.add(detail);
        } else if ((detail.getEndDate().compareTo(now) >= 0)
            && (detail.getBeginDate().compareTo(now) <= 0))
          result.add(detail);
      }
    }
    return result;
  }

  public Collection getAllQuickInfos() throws java.rmi.RemoteException,
      javax.naming.NamingException, java.sql.SQLException {
    SilverTrace
        .info("quickinfo", "QuickInfoTransversalSC.getAllQuickInfos()",
            "root.MSG_GEN_PARAM_VALUE", "Enter Get All Quick Info : User="
                + userId);
    ArrayList result = new ArrayList();
    CompoSpace[] compoSpaces = getOrganizationControl().getCompoForUser(
        this.userId, "quickinfo");
    for (int i = 0; i < compoSpaces.length; i++) {
      String spaceId = compoSpaces[i].getSpaceId();
      String componentId = compoSpaces[i].getComponentId();
      SilverTrace.info("quickinfo",
          "QuickInfoTransversalSC.getAllQuickInfos()",
          "root.MSG_GEN_PARAM_VALUE", "spaceId = " + spaceId
              + ", componentId = " + componentId);
      try {
        Collection quickinfos = getPublicationBm().getOrphanPublications(
            new PublicationPK("", spaceId, componentId));
        result.addAll(filterVisibleQuickInfos(quickinfos));
      } catch (Exception e) {
        SilverTrace.error("quickinfo",
            "QuickInfoTransversalSC.getAllQuickInfos()",
            "quickinfo.CANT_GET_QUICKINFOS", spaceId, e);
      }
    }
    return sortByDateDesc(result);
  }

  public OrganizationController getOrganizationControl() {
    if (organizationControl == null)
      organizationControl = new OrganizationController();
    return organizationControl;
  }

  public ResourceLocator getMessage() {
    if (message == null)
      message = new ResourceLocator(
          "com.stratelia.webactiv.quickinfo.multilang.quickinfo", "");
    return message;
  }

  public Collection sortByDateDesc(ArrayList alPubDetails) {
    Comparator comparator = QuickInfoDateComparatorDesc.comparator;

    Collections.sort(alPubDetails, comparator);

    return alPubDetails;
  }

}