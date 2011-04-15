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

package com.stratelia.webactiv.webSites;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBm;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBmHome;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SitePK;
import com.stratelia.webactiv.webSites.siteManage.model.WebSitesRuntimeException;

/**
 * The webSites implementation of ContentInterface.
 */
public class WebSitesContentManager implements java.io.Serializable, ContentInterface {
  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    return getHeaders(getSiteIds(ids), peasId);
  }

  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace.info("webSites", "WebSitesContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(
          "WebSitesContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param siteDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, SiteDetail siteDetail,
      String userId, String prefixTableName, String componentId)
      throws ContentManagerException, RemoteException {
    SilverContentVisibility scv = new SilverContentVisibility(
        isVisible(siteDetail));
    SilverTrace.info("webSites",
        "WebSitesContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    SilverTrace.info("webSites",
        "WebSitesContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "siteDetail = " + siteDetail.toString());
    return getContentManager().addSilverContent(con,
        siteDetail.getSitePK().getId(), componentId, userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a SiteDetail
   * @param siteDetail the content
   * @param silverObjectId the unique identifier of the content
   */
  public void updateSilverContentVisibility(SiteDetail siteDetail,
      String prefixTableName, String componentId)
      throws ContentManagerException, RemoteException {
    int silverContentId = getContentManager().getSilverContentId(
        siteDetail.getSitePK().getId(), componentId);
    SilverContentVisibility scv = new SilverContentVisibility(
        isVisible(siteDetail));
    SilverTrace.info("webSites",
        "WebSitesContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = "
        + scv.toString());
    if (silverContentId == -1)
      createSilverContent(null, siteDetail, siteDetail.getCreatorId(),
          prefixTableName, componentId);
    else
      getContentManager().updateSilverContentVisibilityAttributes(scv,
          componentId, silverContentId);

    ClassifyEngine.clearCache();
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pubPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, SitePK sitePK,
      String prefixTableName, String componentId)
      throws ContentManagerException, RemoteException {
    int contentId = getContentManager().getSilverContentId(sitePK.getId(),
        componentId);
    if (contentId != -1) {
      SilverTrace.info("webSites",
          "WebSitesContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "siteId = " + sitePK.getId()
          + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId, componentId);
    }
  }

  private boolean isVisible(SiteDetail siteDetail)
      throws ContentManagerException {
    return siteDetail.getState() == 1;
  }

  /**
   * return a list of sitePK according to a list of silverContentId
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of sitePK
   */
  private List getSiteIds(List silverContentIds) {
    ArrayList ids = new ArrayList();
    String id = null;
    // for each silverContentId, we get the corresponding publicationId
    for (int i = 0; i < silverContentIds.size(); i++) {
      int silverContentId = ((Integer) silverContentIds.get(i)).intValue();
      try {
        id = getContentManager().getInternalContentId(silverContentId);
        ids.add(id);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return ids;
  }

  /**
   * return a list of silverContent according to a list of sitePK
   * @param ids a list of sitePK
   * @return a list of SiteDetail
   */

  private List getHeaders(List siteIds, String peasId) {
    SiteDetail siteDetail = null;
    ArrayList headers = new ArrayList();
    SilverTrace.info("webSites", "WebSitesContentManager.getHeaders()",
        "root.MSG_GEN_ENTER_METHOD", "siteIds = " + siteIds.toString());
    try {
      ArrayList siteDetails = (ArrayList) getWebSiteBm(peasId).getWebSites(
          siteIds);
      for (int i = 0; i < siteDetails.size(); i++) {
        siteDetail = (SiteDetail) siteDetails.get(i);
        siteDetail.getSitePK().setComponentName(peasId);
        siteDetail.setIconUrl("webSitesSmall.gif");
        if (peasId.startsWith("bookmark"))
          siteDetail.setIconUrl("bookmarkSmall.gif");

        // rustine : the getDate() of the SilverContentInterface have to return
        // a date like yyyy/MM/dd

        // CBO : UPDATE
        // siteDetail.setDate(siteDetail.getDate());
        siteDetail.setCreationDate(siteDetail.getCreationDate());

        headers.add(siteDetail);
      }
    } catch (RemoteException e) {
      // skip unknown and ill formed id.
    }
    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("webSites", "WebSitesContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private WebSiteBm getWebSiteBm(String componentId) {
    try {
      WebSiteBmHome webSiteBmHome = (WebSiteBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.WEBSITESBM_EJBHOME, WebSiteBmHome.class);
      currentWebSiteBm = webSiteBmHome.create();
      currentWebSiteBm.setComponentId(componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException(
          "WebSitesContentManager.getWebSiteBm()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_IMPOSSIBLE_DE_FABRIQUER_BOOKMARKBM_HOME", e);
    }
    return currentWebSiteBm;
  }

  private ContentManager contentManager = null;
  private WebSiteBm currentWebSiteBm = null;

}