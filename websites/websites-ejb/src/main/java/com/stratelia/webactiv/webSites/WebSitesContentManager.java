/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.webSites;

import com.stratelia.silverpeas.classifyEngine.ClassifyEngine;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBm;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SitePK;
import com.stratelia.webactiv.webSites.siteManage.model.WebSitesRuntimeException;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * The webSites implementation of ContentInterface.
 */
public class WebSitesContentManager implements java.io.Serializable, ContentInterface {

  private static final long serialVersionUID = -8992766242253326927L;
  private ContentManager contentManager = null;
  private WebSiteBm currentWebSiteBm = null;

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param sComponentId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  @Override
  public List getSilverContentById(List<Integer> ids, String sComponentId, String userId,
      List<String> userRoles) {
    return getHeaders(getSiteIds(ids), sComponentId);
  }

  public int getSilverObjectId(String pubId, String peasId) {
    SilverTrace
        .info("webSites", "WebSitesContentManager.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD",
            "pubId = " + pubId);
    try {
      return getContentManager().getSilverContentId(pubId, peasId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSitesContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param siteDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, SiteDetail siteDetail, String userId,
      String prefixTableName, String componentId) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(siteDetail));
    SilverTrace.info("webSites", "WebSitesContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = " + scv.toString());
    SilverTrace.info("webSites", "WebSitesContentManager.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "siteDetail = " + siteDetail.toString());
    return getContentManager()
        .addSilverContent(con, siteDetail.getSitePK().getId(), componentId, userId, scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a SiteDetail
   * @param siteDetail the content
   * @param prefixTableName
   * @param componentId the component instance identifier
   * @throws ContentManagerException
   */
  public void updateSilverContentVisibility(SiteDetail siteDetail, String prefixTableName,
      String componentId) throws ContentManagerException {
    int silverContentId =
        getContentManager().getSilverContentId(siteDetail.getSitePK().getId(), componentId);
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(siteDetail));
    SilverTrace.info("webSites", "WebSitesContentManager.updateSilverContentVisibility()",
        "root.MSG_GEN_ENTER_METHOD", "SilverContentVisibility = " + scv.toString());
    if (silverContentId == -1) {
      createSilverContent(null, siteDetail, siteDetail.getCreatorId(), prefixTableName,
          componentId);
    } else {
      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, componentId, silverContentId);
    }
    ClassifyEngine.clearCache();
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param sitePK the site identifier to delete
   * @param prefixTableName
   * @param componentId the component instance identifier
   * @throws ContentManagerException
   */
  public void deleteSilverContent(Connection con, SitePK sitePK, String prefixTableName,
      String componentId) throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(sitePK.getId(), componentId);
    if (contentId != -1) {
      SilverTrace.info("webSites", "WebSitesContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "siteId = " + sitePK.getId() + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId, componentId);
    }
  }

  private boolean isVisible(SiteDetail siteDetail) {
    return siteDetail.getState() == 1;
  }

  /**
   * return a list of sitePK according to a list of silverContentId
   * @param silverContentIds
   * @return a list of sitePK
   */
  private List<String> getSiteIds(List<Integer> silverContentIds) {
    List<String> ids = new ArrayList<>();
    // for each silverContentId, we get the corresponding publicationId
    for (Integer silverContentId : silverContentIds) {
      try {
        String id = getContentManager().getInternalContentId(silverContentId);
        ids.add(id);
      } catch (ClassCastException | ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return ids;
  }

  /**
   * return a list of silverContent according to a list of sitePK
   * @param siteIds list of site identifier
   * @param componentId the component instance identifier
   * @return a list of SiteDetail
   */
  private List<SiteDetail> getHeaders(List<String> siteIds, String componentId) {
    List<SiteDetail> headers = new ArrayList<>();
    SilverTrace.info("webSites", "WebSitesContentManager.getHeaders()", "root.MSG_GEN_ENTER_METHOD",
        "siteIds = " + siteIds);
    try {
      List<SiteDetail> siteDetails = getWebSiteBm().getWebSites(componentId, siteIds);
      for (SiteDetail siteDetail : siteDetails) {
        siteDetail.getSitePK().setComponentName(componentId);
        siteDetail.setIconUrl("webSitesSmall.gif");
        if (componentId.startsWith("bookmark")) {
          siteDetail.setIconUrl("bookmarkSmall.gif");
        }
        siteDetail.setCreationDate(siteDetail.getCreationDate());

        headers.add(siteDetail);
      }
    } catch (Exception e) {
      // skip unknown and ill formed id.
    }
    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace
            .fatal("webSites", "WebSitesContentManager", "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private WebSiteBm getWebSiteBm() {
    if (currentWebSiteBm == null) {
      try {
        currentWebSiteBm = ServiceProvider.getService(WebSiteBm.class);
      } catch (Exception e) {
        throw new WebSitesRuntimeException("WebSitesContentManager.getWebSiteBm()",
            SilverpeasRuntimeException.ERROR, "IoC error with WebSiteBm", e);
      }
    }
    return currentWebSiteBm;
  }
}