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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.calendar.Date;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.silverpeas.socialNetwork.model.SocialInformation;
import com.silverpeas.socialNetwork.provider.SocialGalleryInterface;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class SocialGallery implements SocialGalleryInterface {

  /**
   * get the my  SocialInformationGallery  according
   * to number of Item and the first Index
   * @param userId
   * @param limit
   * @param offset
   * @return List<SocialInformationGallery>
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end) {
    try {
      return getGalleryBm().getAllPhotosByUserid(userId, begin, end);
    } catch (RemoteException rex) {
      throw new GalleryRuntimeException("SocialGallery.getSocialInformationsList()",
          SilverpeasException.ERROR, "Error obtaining all photos fo user", rex);
    }

  }

  /**
   * get the   SocialInformationGallery of my contatcs according to number of Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId, List<String> myContactsIds,
      Date begin, Date end) throws SilverpeasException {
    try {
      return getGalleryBm().getSocialInformationsListOfMyContacts(myContactsIds, this.
          getListAvailable(myId), begin, end);
    } catch (RemoteException rex) {
      throw new GalleryRuntimeException("SocialGallery.getSocialInformationsListOfMyContacts()",
          SilverpeasException.ERROR, "Error obtaining all photos fo user", rex);
    }
  }

  /**
   * getEJB
   * @return instance of CalendarBmHome
   */
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
 /**
   * gets the available component for a given users list
   * @param myId
   * @param myContactsIds
   * @param firstIndex
   * @return List<String>
   */
  private List<String> getListAvailable(String userid) {
    OrganizationController org = new OrganizationController();
    List<ComponentInstLight> availableList = new ArrayList<ComponentInstLight>();
    availableList = org.getAvailComponentInstLights(userid, "gallery");
    List<String> idsList = new ArrayList<String>();
    for (ComponentInstLight comp : availableList) {
      idsList.add(comp.getId());
    }
    return idsList;
  }
}
