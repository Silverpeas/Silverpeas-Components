/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.kmelia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;

import org.silverpeas.core.admin.OrganisationController;

import com.silverpeas.look.PublicationHelper;
import com.silverpeas.util.StringUtil;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

public class KmeliaTransversal implements PublicationHelper {

  private String userId = null;
  private PublicationBm publicationBm = null;
  private OrganisationController organizationControl = null;

  public KmeliaTransversal() {
  }

  public KmeliaTransversal(String userId) {
    this.userId = userId;
  }

  public KmeliaTransversal(MainSessionController mainSC) {
    userId = mainSC.getUserId();
  }

  @Override
  public void setMainSessionController(MainSessionController mainSC) {
    userId = mainSC.getUserId();
  }

  public List<PublicationDetail> getPublications() {
    return getPublications(null);
  }

  public List<PublicationDetail> getPublications(int nbPublis) {
    return getPublications(null, nbPublis);
  }

  public List<PublicationDetail> getPublications(String spaceId) {
    return getPublications(spaceId, -1);
  }

  @Override
  public List<PublicationDetail> getPublications(String spaceId, int nbPublis) {
    SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId + ", nbPublis = " + nbPublis);
    List<String> componentIds = getAvailableComponents(spaceId);
    SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
        "root.MSG_GEN_PARAM_VALUE", "componentIds = " + componentIds.toString());
    List<PublicationPK> publicationPKs = null;
    try {
      publicationPKs = (List<PublicationPK>) getPublicationBm().getPublicationPKsByStatus(
          PublicationDetail.VALID, componentIds);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublications()",
          "kmelia.CANT_GET_PUBLICATIONS_PKS", "spaceId = " + spaceId, e);
    }
    Collection<PublicationPK> filteredPublicationPKs = filterPublicationPKs(publicationPKs,
        nbPublis);

    try {
      return (List<PublicationDetail>) getPublicationBm().getPublications(filteredPublicationPKs);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublications()",
          "kmelia.CANT_GET_PUBLICATIONS", "spaceId = " + spaceId, e);
    }
    return new ArrayList<PublicationDetail>();
  }

  @Override
  public List<PublicationDetail> getUpdatedPublications(String spaceId, int since, int nbReturned) {
    int maxAge = since;
    if (maxAge > 0) {
      maxAge = -1 * maxAge;
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, maxAge);
      return getUpdatedPublications(spaceId, calendar.getTime(), nbReturned);
    }
    return getPublications(spaceId, nbReturned);
  }

  protected List<PublicationDetail> getUpdatedPublications(String spaceId, Date since, int nbPublis) {
    SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
        "root.MSG_GEN_ENTER_METHOD", "spaceId = " + spaceId + ", nbPublis = " + nbPublis);
    List<String> componentIds = getAvailableComponents(spaceId);
    SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
        "root.MSG_GEN_PARAM_VALUE", "componentIds = " + componentIds.toString());

    List<PublicationPK> publicationPKs = null;
    try {
      publicationPKs = (List<PublicationPK>) getPublicationBm().getUpdatedPublicationPKsByStatus(
          PublicationDetail.VALID, since, 0, componentIds);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublications()",
          "kmelia.CANT_GET_PUBLICATIONS_PKS", "spaceId = " + spaceId, e);
    }
    Collection<PublicationPK> filteredPublicationPKs = filterPublicationPKs(publicationPKs,
        nbPublis);
    try {
      return (List<PublicationDetail>) getPublicationBm().getPublications(filteredPublicationPKs);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublications()",
          "kmelia.CANT_GET_PUBLICATIONS", "spaceId = " + spaceId, e);
    }
    return new ArrayList<PublicationDetail>();
  }

  protected List<String> getAvailableComponents(String spaceId) {
    List<String> componentIds = new ArrayList<String>();
    if (!StringUtil.isDefined(spaceId)) {
      String[] cIds = getOrganizationControl().getComponentIdsForUser(userId, "kmelia");
      componentIds.addAll(Arrays.asList(cIds));
      cIds = getOrganizationControl().getComponentIdsForUser(userId, "toolbox");
      componentIds.addAll(Arrays.asList(cIds));
      cIds = getOrganizationControl().getComponentIdsForUser(userId, "kmax");
      componentIds.addAll(Arrays.asList(cIds));
    } else {
      String[] cIds = getOrganizationControl().getAvailCompoIds(spaceId, userId);
      SilverTrace.debug("kmelia", "KmeliaTransversal.getPublications()",
          "root.MSG_GEN_PARAM_VALUE", "#cIds = " + cIds.length);
      for (String id : cIds) {
        if (id.startsWith("kmelia") || id.startsWith("toolbox") || id.startsWith("kmax")) {
          componentIds.add(id);
        }
      }
    }
    return componentIds;
  }

  public List<PublicationDetail> getPublicationsByComponentId(String componentId) {
    List<PublicationDetail> publications = null;
    try {
      List<String> componentIds = new ArrayList<String>();
      componentIds.add(componentId);
      publications = (List<PublicationDetail>) getPublicationBm().getPublicationsByStatus("Valid",
          componentIds);
    } catch (Exception e) {
      SilverTrace.error("kmelia", "KmeliaTransversal.getPublicationsByComponentId()",
          "kmelia.CANT_GET_PUBLICATIONS", "componentId = " + componentId, e);
    }
    return filterPublications(publications, -1);
  }

  private List<PublicationDetail> filterPublications(List<PublicationDetail> publications,
      int nbPublis) {
    List<PublicationDetail> filteredPublications = new ArrayList<PublicationDetail>();
    KmeliaSecurity security = new KmeliaSecurity();

    PublicationDetail pub = null;
    for (int p = 0; publications != null && p < publications.size(); p++) {
      pub = publications.get(p);
      if (security.isObjectAvailable(pub.getPK().getInstanceId(), userId, pub.getPK().getId(),
          "Publication")) {
        filteredPublications.add(pub);
      }

      if (nbPublis != -1 && filteredPublications.size() == nbPublis) {
        return filteredPublications;
      }
    }

    return filteredPublications;
  }

  private List<PublicationPK> filterPublicationPKs(List<PublicationPK> publicationPKs, int nbPublis) {
    List<PublicationPK> filteredPublicationPKs = new ArrayList<PublicationPK>();
    KmeliaSecurity security = new KmeliaSecurity();

    PublicationPK pk = null;
    for (int p = 0; publicationPKs != null && p < publicationPKs.size(); p++) {
      pk = publicationPKs.get(p);
      if (security.isObjectAvailable(pk.getInstanceId(), userId, pk.getId(),
          "Publication")) {
        filteredPublicationPKs.add(pk);
      }

      if (nbPublis != -1 && filteredPublicationPKs.size() == nbPublis) {
        return filteredPublicationPKs;
      }
    }

    return filteredPublicationPKs;
  }

  private OrganisationController getOrganizationControl() {
    if (organizationControl == null) {
      organizationControl = new OrganizationController();
    }
    return organizationControl;
  }

  private PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        SilverTrace.error("quickinfo","QuickInfoTransversalSC.getPublicationBm()",
            "root.MSG_EJB_CREATE_FAILED", JNDINames.PUBLICATIONBM_EJBHOME, e);
        throw new EJBException(e);
      }

    }
    return publicationBm;
  }
}
