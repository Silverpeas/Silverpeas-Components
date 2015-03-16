/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.calendar.Date;
import com.silverpeas.comment.service.CommentServiceProvider;
import com.silverpeas.comment.socialnetwork.SocialInformationComment;
import com.silverpeas.gallery.GalleryComponentSettings;
import com.silverpeas.gallery.control.ejb.MediaServiceProvider;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.model.Sound;
import com.silverpeas.gallery.model.Streaming;
import com.silverpeas.gallery.model.Video;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialCommentGalleryInterface;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.core.admin.OrganizationControllerProvider;
import org.silverpeas.date.Period;
import org.silverpeas.util.exception.SilverpeasException;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class SocialCommentGallery implements SocialCommentGalleryInterface {

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<String>(); //gallery components
    listResourceType.add(Photo.getResourceType());
    listResourceType.add(Video.getResourceType());
    listResourceType.add(Sound.getResourceType());
    listResourceType.add(Streaming.getResourceType());
    return listResourceType;
  }

  @SuppressWarnings("unchecked")
  private List<SocialInformation> decorate(List<SocialInformationComment> listSocialInformation) {
    for (SocialInformationComment socialInformation : listSocialInformation) {
      String resourceId = socialInformation.getComment().getForeignKey().getId();
      String instanceId = socialInformation.getComment().getComponentInstanceId();

      MediaPK mediaPk = new MediaPK(resourceId, instanceId);
      Media media = MediaServiceProvider.getMediaService().getMedia(mediaPk);

      // Set URL and title of the media comment
      socialInformation.setUrl("/Rgallery/" + media.getInstanceId() + "/" + media.getURL());
      socialInformation.setTitle(media.getTitle());
    }

    return (List) listSocialInformation;
  }

  /**
   * get list of SocialInformation
   * @param userId
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end)
      throws SilverpeasException {

    List<SocialInformationComment> listSocialInformation =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListByUserId(getListResourceType(), userId,
                Period.from(begin, end));

    return decorate(listSocialInformation);
  }

  /**
   * get list of socialInformation of my contacts according to ids of my contacts
   * @param myId
   * @param myContactsIds
   * @param begin
   * @param end
   * @return List<SocialInformation>
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {

    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.addAll(
        Arrays.asList(oc.getComponentIdsForUser(myId, GalleryComponentSettings.COMPONENT_NAME)));

    List<SocialInformationComment> listSocialInformation =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListOfMyContacts(getListResourceType(), myContactsIds,
                instanceIds, Period.from(begin, end));

    return decorate(listSocialInformation);
  }
}