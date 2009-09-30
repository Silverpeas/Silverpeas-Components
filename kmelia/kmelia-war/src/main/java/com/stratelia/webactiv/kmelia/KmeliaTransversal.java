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
package com.stratelia.webactiv.kmelia;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJBException;

import com.silverpeas.look.PublicationHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class KmeliaTransversal implements PublicationHelper {

  private String userId = null;
  private PublicationBm publicationBm = null;
  private OrganizationController organizationControl = null;

  public KmeliaTransversal() {
  }

  public KmeliaTransversal(MainSessionController mainSC) {
    userId = mainSC.getUserId();
  }

  public void setMainSessionController(MainSessionController mainSC) {
    userId = mainSC.getUserId();
  }

  public List getPublications() {
    return getPublications(null);
  }

  public List getPublications(int nbPublis) {
    return getPublications(null, nbPublis);
  }

  public List getPublications(String spaceId) {
    return getPublications(spaceId, -1);
  }

  public List getPublications(String spaceId, int nbPublis) {
    SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId + ", nbPublis = "
            + nbPublis);
    List componentIds = new ArrayList();
    if (!StringUtil.isDefined(spaceId)) {
      String[] cIds = getOrganizationControl().getComponentIdsForUser(userId,
          "kmelia");
      for (int c = 0; c < cIds.length; c++) {
        componentIds.add("kmelia" + cIds[c]);
      }
      cIds = getOrganizationControl().getComponentIdsForUser(userId, "toolbox");
      for (int c = 0; c < cIds.length; c++) {
        componentIds.add("toolbox" + cIds[c]);
      }
      cIds = getOrganizationControl().getComponentIdsForUser(userId, "kmax");
      for (int c = 0; c < cIds.length; c++) {
        componentIds.add("kmax" + cIds[c]);
      }
    } else {
      String[] cIds = getOrganizationControl()
          .getAvailCompoIds(spaceId, userId);
      SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
          "root.MSG_GEN_PARAM_VALUE", "#cIds = " + cIds.length);
      for (int c = 0; c < cIds.length; c++) {
        if (cIds[c].startsWith("kmelia") || cIds[c].startsWith("toolbox")
            || cIds[c].startsWith("kmax"))
          componentIds.add(cIds[c]);
      }
    }

    SilverTrace
        .debug("kmelia", "KmeliaTransversal.getPublications()",
            "root.MSG_GEN_PARAM_VALUE", "componentIds = "
                + componentIds.toString());

    List publicationPKs = null;
    try {
      publicationPKs = (List) getPublicationBm().getPublicationPKsByStatus(
          "Valid", componentIds);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublications()",
          "kmelia.CANT_GET_PUBLICATIONS_PKS", "spaceId = " + spaceId, e);
    }
    Collection filteredPublicationPKs = filterPublicationPKs(publicationPKs,
        nbPublis);

    try {
      return (List) getPublicationBm().getPublications(filteredPublicationPKs);
    } catch (RemoteException e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublications()",
          "kmelia.CANT_GET_PUBLICATIONS", "spaceId = " + spaceId, e);
    }

    return new ArrayList();
  }

  public List getPublicationsByComponentId(String componentId) {
    List publications = null;
    try {
      List componentIds = new ArrayList();
      componentIds.add(componentId);
      publications = (List) getPublicationBm().getPublicationsByStatus("Valid",
          componentIds);
    } catch (Exception e) {
      SilverTrace.error("kmelia",
          "KmeliaTransversal.getPublicationsByComponentId()",
          "kmelia.CANT_GET_PUBLICATIONS", "componentId = " + componentId, e);
    }
    return filterPublications(publications, -1);
  }

  private List filterPublications(List publications, int nbPublis) {
    List filteredPublications = new ArrayList();
    KmeliaSecurity security = new KmeliaSecurity();

    PublicationDetail pub = null;
    for (int p = 0; publications != null && p < publications.size(); p++) {
      pub = (PublicationDetail) publications.get(p);
      if (security.isObjectAvailable(pub.getPK().getInstanceId(), userId, pub
          .getPK().getId(), "Publication"))
        filteredPublications.add(pub);

      if (nbPublis != -1 && filteredPublications.size() == nbPublis)
        return filteredPublications;
    }

    return filteredPublications;
  }

  private List filterPublicationPKs(List publicationPKs, int nbPublis) {
    List filteredPublicationPKs = new ArrayList();
    KmeliaSecurity security = new KmeliaSecurity();

    PublicationPK pk = null;
    for (int p = 0; publicationPKs != null && p < publicationPKs.size(); p++) {
      pk = (PublicationPK) publicationPKs.get(p);
      if (security.isObjectAvailable(pk.getInstanceId(), userId, pk.getId(),
          "Publication"))
        filteredPublicationPKs.add(pk);

      if (nbPublis != -1 && filteredPublicationPKs.size() == nbPublis)
        return filteredPublicationPKs;
    }

    return filteredPublicationPKs;
  }

  private OrganizationController getOrganizationControl() {
    if (organizationControl == null)
      organizationControl = new OrganizationController();
    return organizationControl;
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
}
