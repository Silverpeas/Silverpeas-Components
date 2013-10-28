/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package com.silverpeas.gallery;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.gallery.model.PhotoDetail;
import com.silverpeas.gallery.model.PhotoPK;

import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * The gallery implementation of ContentInterface.
 */
public class GalleryContentManager implements ContentInterface, java.io.Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Find all the SilverContent with the given list of SilverContentId
   *
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  @Override
  public List getSilverContentById(List<Integer> ids, String peasId, String userId,
      List<String> userRoles) {
    if (getContentManager() == null) {
      return new ArrayList<PhotoDetail>();
    }

    return getHeaders(makePKArray(ids, peasId));
  }

  public int getSilverObjectId(String photoId, String peasId) {
    SilverTrace.info("gallery", "GalleryContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "photoId = " + photoId);
    try {
      return getContentManager().getSilverContentId(photoId, peasId);
    } catch (Exception e) {
      throw new GalleryRuntimeException("GalleryContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param photoDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   * @throws ContentManagerException
   */
  public int createSilverContent(Connection con, PhotoDetail photoDetail, String userId)
      throws ContentManagerException {
    return getContentManager().addSilverContent(con,
        photoDetail.getPhotoPK().getId(),
        photoDetail.getPhotoPK().getComponentName(), userId);
  }

  /**
   * delete a content. It is registered to contentManager service
   *
   * @param con a Connection
   * @param photoPK the identifiant of the content to unregister
   * @throws ContentManagerException
   */
  public void deleteSilverContent(Connection con, PhotoPK photoPK) throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(photoPK.getId(),
        photoPK.getComponentName());
    if (contentId != -1) {
      SilverTrace.info("gallery", "GalleryContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "photoId = " + photoPK.getId()
          + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          photoPK.getComponentName());
    }
  }

  /**
   * return a list of photoPK according to a list of silverContentId
   *
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of PhotoPK
   */
  private ArrayList<PhotoPK> makePKArray(List<Integer> idList, String peasId) {
    ArrayList<PhotoPK> pks = new ArrayList<PhotoPK>();
    // for each silverContentId, we get the corresponding photoId
    for(int contentId : idList)  {
      try {
        String id = getContentManager().getInternalContentId(contentId);
        PhotoPK photoPK = new PhotoPK(id, peasId);
        pks.add(photoPK);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of photoPK
   *
   * @param ids a list of photoPK
   * @return a list of photoDetail
   */
  private List<PhotoDetail> getHeaders(List<PhotoPK> ids) {
    ArrayList<PhotoDetail> headers = new ArrayList<PhotoDetail>();
    // création de la liste "headers" avec toutes les photos (format PhotoDetail)
    // en fonction de la liste "ids" des photos (format PhotoPK)
    if (ids != null) {
      for (PhotoPK photoPK : ids) {
        PhotoDetail photo = getGalleryBm().getPhoto(photoPK);
        photo.setIconUrl("gallerySmall.gif");
        headers.add(photo);
      }
    }
    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverTrace.fatal("gallery", "GalleryContentManager",
            "root.EX_UNKNOWN_CONTENT_MANAGER", e);
      }
    }
    return contentManager;
  }

  private GalleryBm getGalleryBm() {
    if (currentGalleryBm == null) {
      try {
        currentGalleryBm = EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME,
            GalleryBm.class);
      } catch (Exception e) {
        throw new GalleryRuntimeException("GalleryContentManager.getGalleryBm()",
            SilverpeasRuntimeException.ERROR, "gallery.EX_IMPOSSIBLE_DE_FABRIQUER_GALLERYBM_HOME",
            e);
      }
    }
    return currentGalleryBm;
  }
  private ContentManager contentManager = null;
  private GalleryBm currentGalleryBm = null;
}
