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
package com.silverpeas.gallery.socialNetwork;

/**
 * SocialGallery is the class representing the events of the gallery *
 * @author bourakbi  
 * @see SocialInformation
 * @see SocialInformationGallery
 **/
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.gallery.control.ejb.GalleryBmEJB;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;

import com.silverpeas.gallery.model.PhotoWithStatus;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.provider.SocialGalleryInterface;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocialGallery implements SocialGalleryInterface {

  private static int firstIndex = 0;
  static private GalleryBm galleryBm = null;

  /**
   * @param user
   * @return the list of photos that the user has been created or updated
   * @see UserDetail
   * @see SocialInformation
   * @see SocialInformationGallery
   **/
  public List<SocialInformation> getAllEventByUser(UserDetail user) {
    GalleryBmEJB galleryEJB = new GalleryBmEJB();
    List<PhotoWithStatus> photos = galleryEJB.getAllPhotosWithStatusbyUserid(user.getId());
    List<SocialInformation> photosGallery = new ArrayList<SocialInformation>(photos.size());
    for (PhotoWithStatus photoId : photos) {
      photosGallery.add(new SocialInformationGallery(photoId));
    }
    return photosGallery;
  }

  /**
   * @param user
   * @param numberOfElement
   *         the number of items wanted
   * @return the list of photos that the user has been created or updated (size of numberOfElement)
   * @see UserDetail
   * @see SocialInformation
   * @see SocialInformationGallery
   **/
  public List<SocialInformation> getAllEventByUser(UserDetail user, int numberOfElement) {
    GalleryBmEJB galleryEJB = new GalleryBmEJB();
    List<PhotoWithStatus> photos = galleryEJB.getAllPhotosWithStatusbyUserid(user.getId(),
        firstIndex, numberOfElement);
    List<SocialInformation> photosGallery = new ArrayList<SocialInformation>(photos.size());
    for (PhotoWithStatus photo : photos) {
      photosGallery.add(new SocialInformationGallery(photo));
    }
    firstIndex = numberOfElement + firstIndex;
    return photosGallery;
  }

  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, int numberOfElement,
      int firstIndex) throws SilverpeasException {

    List<SocialInformation> photosGallery = null;
    try {
      List<PhotoWithStatus> photos = getGalleryBm().getAllPhotosWithStatusbyUserid(userId,
          firstIndex, numberOfElement);
      photosGallery = new ArrayList<SocialInformation>(photos.size());
      for (PhotoWithStatus photo : photos) {
        photosGallery.add(new SocialInformationGallery(photo));
      }
      firstIndex = numberOfElement + firstIndex;

    } catch (RemoteException ex) {
      Logger.getLogger(SocialGallery.class.getName()).log(Level.SEVERE, null, ex);
    }
    return photosGallery;

  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }
}
