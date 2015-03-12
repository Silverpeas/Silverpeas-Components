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

package org.silverpeas.components.quickinfo.socialNetwork;

import com.silverpeas.calendar.Date;
import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.comment.socialnetwork.SocialInformationComment;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialCommentQuickInfosInterface;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.silverpeas.components.quickinfo.model.News;
import org.silverpeas.components.quickinfo.model.QuickInfoServiceFactory;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.date.Period;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SocialCommentQuickInfos implements SocialCommentQuickInfosInterface {

  private List<String> getListResourceType() {
    List<String> listResourceType = new ArrayList<String>();
    listResourceType.add(News.getResourceType()); //quickinfo components
    return listResourceType;
  }

  @SuppressWarnings("unchecked")
  private List<SocialInformation> decorate(List<SocialInformationComment> listSocialInformation) {
    for (SocialInformationComment socialInformation : listSocialInformation) {
      String resourceId = socialInformation.getComment().getForeignKey().getId();

      News news = QuickInfoServiceFactory.getQuickInfoService().getNews(resourceId);

      //set URL, title and description of the news
      socialInformation.setUrl(URLManager
          .getSimpleURL(URLManager.URL_PUBLI, news.getPublicationId(),
              news.getComponentInstanceId(), false));
      socialInformation.setTitle(news.getTitle());
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
        CommentServiceFactory.getFactory().getCommentService()
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

    OrganisationController oc = OrganisationControllerFactory.getOrganisationController();
    List<String> instanceIds = new ArrayList<String>();
    instanceIds.addAll(Arrays.asList(oc.getComponentIdsForUser(myId, "quickinfo")));

    List<SocialInformationComment> listSocialInformation =
        CommentServiceFactory.getFactory().getCommentService()
            .getSocialInformationCommentsListOfMyContacts(getListResourceType(), myContactsIds,
                instanceIds, Period.from(begin, end));

    return decorate(listSocialInformation);
  }
}