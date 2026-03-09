/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.gallery.socialnetwork;

import jakarta.inject.Inject;
import org.silverpeas.components.gallery.GalleryComponentSettings;
import org.silverpeas.components.gallery.model.*;
import org.silverpeas.components.gallery.service.GalleryService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.comment.service.CommentService;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialMediaCommentProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Provider
public class SocialGalleryMediaComment implements SocialMediaCommentProvider<SocialInformation> {

  @Inject
  private GalleryService mediaService;
  @Inject
  private CommentService commentService;
  @Inject
  private OrganizationController oc;

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<>(); //gallery components
    listResourceType.add(Photo.getResourceType());
    listResourceType.add(Video.getResourceType());
    listResourceType.add(Sound.getResourceType());
    listResourceType.add(Streaming.getResourceType());
    return listResourceType;
  }

  private List<SocialInformation> decorate(List<SocialInformationComment> listSocialInformation) {
    final List<SocialInformation> info = new ArrayList<>(listSocialInformation.size());
    for (SocialInformationComment socialInformation : listSocialInformation) {
      String resourceId = socialInformation.getComment().getResourceReference().getLocalId();
      String instanceId = socialInformation.getComment().getComponentInstanceId();

      MediaPK mediaPk = new MediaPK(resourceId, instanceId);
      Media media = mediaService.getMedia(mediaPk);

      // Set URL and title of the media comment
      socialInformation.setUrl("/Rgallery/" + media.getInstanceId() + "/" + media.getURL());
      socialInformation.setTitle(media.getTitle());

      info.add(socialInformation);
    }

    return info;
  }

  @Override
  public List<SocialInformation> getSocialInformationList(String userId, Date begin,
      Date end) {
    List<SocialInformationComment> listSocialInformation =
        commentService
            .getSocialInformationCommentsListByUserId(getListResourceType(), userId,
                Period.between(begin.toInstant(), end.toInstant()));

    return decorate(listSocialInformation);
  }

  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(
      final String myId, final List<String> myContactsIds, final Date begin, final Date end) {
    List<String> instanceIds = new ArrayList<>(
        Arrays.asList(oc.getComponentIdsForUser(myId, GalleryComponentSettings.COMPONENT_NAME)));

    List<SocialInformationComment> listSocialInformation =
        commentService
            .getSocialInformationCommentsListOfMyContacts(getListResourceType(), myContactsIds,
                instanceIds, Period.between(begin.toInstant(), end.toInstant()));

    return decorate(listSocialInformation);
  }
}