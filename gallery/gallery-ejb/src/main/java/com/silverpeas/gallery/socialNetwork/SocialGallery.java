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
package com.silverpeas.gallery.socialNetwork;

/**
 * SocialGallery is the class representing the events of the gallery
 *
 *
 * @author bourakbi
 * @see SocialInformation
 * @see SocialInformationGallery
 *
 */

import com.silverpeas.calendar.Date;
import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialGalleryInterface;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import org.silverpeas.util.EJBUtilitaire;
import org.silverpeas.util.JNDINames;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;

import java.util.ArrayList;
import java.util.List;

public class SocialGallery implements SocialGalleryInterface {

  /**
   * get the my SocialInformationGallery according to number of Item and the first Index
   *
   * @param userId
   * @param begin 
   * @param end 
   * @return List<SocialInformationGallery>
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end) {
    return getGalleryBm().getAllMediaByUserId(userId, Period.from(begin, end));
  }

  /**
   * get the SocialInformationGallery of my contatcs according to number of Item and the first Index
   *
   * @param myId
   * @param myContactsIds
   * @param begin 
   * @param end 
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    List<SocialInformation> listSocialInfo = new ArrayList<SocialInformation>();
    List<String> listComponents = this.getListAvailable(myId);
    if(listComponents != null && listComponents.size() > 0) {
      listSocialInfo = getGalleryBm().getSocialInformationListOfMyContacts(myContactsIds, listComponents, Period.from(begin, end));
    }
    return listSocialInfo;
  }

  private GalleryBm getGalleryBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
  }

  /**
   * gets the available component for a given users list
   *
   * @param userId
   * @return List<String>
   */
  private List<String> getListAvailable(String userId) {
    List<ComponentInstLight> availableList = OrganisationControllerFactory.
        getOrganisationController().getAvailComponentInstLights(userId, "gallery");
    List<String> idsList = new ArrayList<String>(availableList.size());
    for (ComponentInstLight comp : availableList) {
      idsList.add(comp.getId());
    }
    return idsList;
  }
}
