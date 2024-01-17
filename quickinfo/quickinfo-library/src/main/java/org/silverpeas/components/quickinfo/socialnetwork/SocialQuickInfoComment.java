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

package org.silverpeas.components.quickinfo.socialnetwork;

import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceProvider;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.comment.service.CommentServiceProvider;
import org.silverpeas.core.comment.socialnetwork.SocialInformationComment;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.provider.SocialNewsCommentProvider;
import org.silverpeas.core.util.URLUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Provider
public class SocialQuickInfoComment implements SocialNewsCommentProvider {

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<>();
    listResourceType.add(News.getResourceType()); //quickinfo components
    return listResourceType;
  }

  @SuppressWarnings("unchecked")
  private List<SocialInformation> decorate(List<SocialInformationComment> listSocialInformation) {
    for (SocialInformationComment socialInformation : listSocialInformation) {
      String resourceId = socialInformation.getComment().getResourceReference().getLocalId();

      News news = QuickInfoServiceProvider.getQuickInfoService().getNews(resourceId);

      //set URL, title and description of the news
      socialInformation.setUrl(URLUtil
          .getSimpleURL(URLUtil.URL_PUBLI, news.getPublicationId(),
              news.getComponentInstanceId(), false));
      socialInformation.setTitle(news.getTitle());
    }

    return (List) listSocialInformation;
  }

  @Override
  public List<SocialInformation> getSocialInformationList(String userId, Date begin, Date end) {
    List<SocialInformationComment> listSocialInformation =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListByUserId(getListResourceType(), userId,
                Period.between(begin.toInstant(), end.toInstant()));

    return decorate(listSocialInformation);
  }

  @Override
  public List<SocialInformation> getSocialInformationListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) {
    OrganizationController oc = OrganizationControllerProvider.getOrganisationController();
    List<String> instanceIds = new ArrayList<>();
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "quickinfo")));

    List<SocialInformationComment> listSocialInformation =
        CommentServiceProvider.getCommentService()
            .getSocialInformationCommentsListOfMyContacts(getListResourceType(), myContactsIds,
                instanceIds, Period.between(begin.toInstant(), end.toInstant()));

    return decorate(listSocialInformation);
  }
}