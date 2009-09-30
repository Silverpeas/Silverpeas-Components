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
package com.silverpeas.gallery;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
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
public class GalleryContentManager implements ContentInterface,
    java.io.Serializable {
  /**
   * Find all the SilverContent with the given list of SilverContentId
   * 
   * @param ids
   *          list of silverContentId to retrieve
   * @param peasId
   *          the id of the instance
   * @param userId
   *          the id of the user who wants to retrieve silverContent
   * @param userRoles
   *          the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null)
      return new ArrayList();

    return getHeaders(makePKArray(ids, peasId));
  }

  public int getSilverObjectId(String photoId, String peasId) {
    SilverTrace.info("gallery", "GalleryContentManager.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "photoId = " + photoId);
    try {
      return getContentManager().getSilverContentId(photoId, peasId);
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GalleryContentManager.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param photoDetail
   *          the content to register
   * @param userId
   *          the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, PhotoDetail photoDetail,
      String userId) throws ContentManagerException {
    // SilverContentVisibility scv = new
    // SilverContentVisibility(photoDetail.getBeginDate(),
    // photoDetail.getEndDate());
    // SilverTrace.info("gallery","GalleryContentManager.createSilverContent()",
    // "root.MSG_GEN_ENTER_METHOD",
    // "SilverContentVisibility = "+scv.toString());
    return getContentManager().addSilverContent(con,
        photoDetail.getPhotoPK().getId(),
        photoDetail.getPhotoPK().getComponentName(), userId);
  }

  /**
   * delete a content. It is registered to contentManager service
   * 
   * @param con
   *          a Connection
   * @param photoPK
   *          the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, PhotoPK photoPK)
      throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(photoPK.getId(),
        photoPK.getComponentName());
    if (contentId != -1) {
      SilverTrace.info("gallery",
          "GalleryContentManager.deleteSilverContent()",
          "root.MSG_GEN_ENTER_METHOD", "photoId = " + photoPK.getId()
              + ", contentId = " + contentId);
      getContentManager().removeSilverContent(con, contentId,
          photoPK.getComponentName());
    }
  }

  /**
   * return a list of photoPK according to a list of silverContentId
   * 
   * @param idList
   *          a list of silverContentId
   * @param peasId
   *          the id of the instance
   * @return a list of PhotoPK
   */
  private ArrayList makePKArray(List idList, String peasId) {
    ArrayList pks = new ArrayList();
    PhotoPK photoPK = null;
    Iterator iter = idList.iterator();
    String id = null;
    // for each silverContentId, we get the corresponding photoId
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        photoPK = new PhotoPK(id, peasId);
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
   * @param ids
   *          a list of photoPK
   * @return a list of photoDetail
   */
  private List getHeaders(List ids) {
    PhotoDetail photo = null;
    ArrayList headers = new ArrayList();

    // création de la liste "headers" avec toutes les photos (format
    // PhotoDetail)
    // en fonction de la liste "ids" des photos (format PhotoPK)
    if (ids != null) {
      Iterator it = ids.iterator();
      while (it.hasNext()) {
        PhotoPK photoPK = (PhotoPK) it.next();
        try {
          photo = getGalleryBm().getPhoto(photoPK);
        } catch (RemoteException e) {
          throw new GalleryRuntimeException(
              "GalleryContentManager.getHeaders()",
              SilverpeasRuntimeException.ERROR, "gallery.MSG_PHOTO_NOT_EXIST",
              e);
        }
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
        GalleryBmHome GalleryBmHome = (GalleryBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
        currentGalleryBm = GalleryBmHome.create();
      } catch (Exception e) {
        throw new GalleryRuntimeException(
            "GalleryContentManager.getGalleryBm()",
            SilverpeasRuntimeException.ERROR,
            "gallery.EX_IMPOSSIBLE_DE_FABRIQUER_GALLERYBM_HOME", e);
      }
    }
    return currentGalleryBm;
  }

  private ContentManager contentManager = null;
  private GalleryBm currentGalleryBm = null;

}
