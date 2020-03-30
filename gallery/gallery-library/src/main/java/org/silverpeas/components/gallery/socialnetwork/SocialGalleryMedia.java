/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.components.gallery.socialnetwork;

/**
 * SocialGallery is the class representing the events of the gallery
 *
 *
 * @author bourakbi
 * @see SocialInformation
 * @see SocialInformationGallery
 *
 */

import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialMediaProvider;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class SocialGalleryMedia implements SocialMediaProvider {

  protected SocialGalleryMedia() {
  }

  /**
   * get the my SocialInformationGallery according to number of Item and the first Index
   * @param userId the user identifier
   * @param begin date
   * @param end date
   * @return List<SocialInformationGallery>
   */
  @Override
  @SuppressWarnings("unchecked")
  public List<SocialInformation> getSocialInformationList(String userId, Date begin, Date end) {
    return getGalleryService().getAllMediaByUserId(userId, Period.from(begin, end));
  }

  /**
   * get the SocialInformationGallery of my contatcs according to number of Item and the first
   * Index
   * @param myId
   * @param myContactsIds
   * @param begin date
   * @param end date
   * @return List
   */
  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) {
    List<SocialInformation> listSocialInfo = new ArrayList<>();
    List<String> listComponents = this.getListAvailable(myId);
    if (!listComponents.isEmpty()) {
      listSocialInfo = getGalleryService()
          .getSocialInformationListOfMyContacts(myContactsIds, listComponents,
              Period.from(begin, end));
    }
    return listSocialInfo;
  }

  private GalleryService getGalleryService() {
    return ServiceProvider.getService(GalleryService.class);
  }

  /**
   * gets the available component for a given users list
   * @param userId the user identifier
   * @return List<String>
   */
  private List<String> getListAvailable(String userId) {
    List<ComponentInstLight> availableList = OrganizationControllerProvider.
        getOrganisationController().getAvailComponentInstLights(userId, "gallery");
    List<String> idsList = new ArrayList<>(availableList.size());
    for (ComponentInstLight comp : availableList) {
      idsList.add(comp.getId());
    }
    return idsList;
  }
}
