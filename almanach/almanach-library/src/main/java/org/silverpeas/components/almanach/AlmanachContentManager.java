/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.components.almanach;

import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.components.almanach.service.AlmanachException;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class AlmanachContentManager implements ContentInterface {

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected AlmanachContentManager(){
  }

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @SuppressWarnings("unchecked")
  public List getSilverContentById(List ids, String peasId, String userId) {
    if (getContentManager() == null) {
      return new ArrayList<EventDetail>();
    }
    return getHeaders(makePKArray(ids, peasId), peasId);
  }

  public int getSilverObjectId(String pubId, String peasId) throws ContentManagerException {
    return getContentManager().getSilverContentId(pubId, peasId);
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param eventDetail the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, EventDetail eventDetail, String userId)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility();
    return getContentManager()
        .addSilverContent(con, eventDetail.getPK().getId(), eventDetail.getPK().getComponentName(),
            userId, scv);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param eventPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, EventPK eventPK) throws ContentManagerException {
    int contentId =
        getContentManager().getSilverContentId(eventPK.getId(), eventPK.getComponentName());

    if (contentId != -1) {
      getContentManager().removeSilverContent(con, contentId, eventPK.getComponentName());
    }
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a EventDetail
   * @param eventDetail the content
   */
  public void updateSilverContentVisibility(EventDetail eventDetail)
      throws ContentManagerException {
    int silverContentId = getContentManager()
        .getSilverContentId(eventDetail.getPK().getId(), eventDetail.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility();


    if (silverContentId == -1) {
      createSilverContent(null, eventDetail, eventDetail.getDelegatorId());
    } else {
      getContentManager()
          .updateSilverContentVisibilityAttributes(scv, silverContentId);
    }

    ClassifyEngine.clearCache();
  }

  /**
   * return a list of almanachPK according to a list of silverContentId
   * @param idList a list of silverContentId
   * @param peasId the id of the instance
   * @return a list of almanachPK
   */
  private List<EventPK> makePKArray(List<Integer> idList, String peasId) {
    List<EventPK> pks = new ArrayList<>();
    EventPK eventPK;
    String id;
    // for each silverContentId, we get the corresponding almanachId
    for (Integer contentId : idList) {
      try {
        id = getContentManager().getInternalContentId(contentId);
        eventPK = new EventPK(id, "useless", peasId);
        pks.add(eventPK);
      } catch (ClassCastException | ContentManagerException e) {
        // ignore unknown item
        SilverLogger.getLogger(this).debug(e.getMessage(), e);
      }
    }
    return pks;
  }

  /**
   * return a list of silverContent according to a list of almanachPK
   * @param ids a list of almanachPK
   * @return a list of EventDetail
   */
  private List<EventDetail> getHeaders(List<EventPK> ids, String peasId) {
    List<EventDetail> headers = new ArrayList<>();
    try {
      Collection<EventDetail> eventDetails = getAlmanachBm().getEvents(ids);
      for (EventDetail eventDetail : eventDetails) {
        eventDetail.setIconUrl("almanachSmall.gif");
        eventDetail.getPK().setComponentName(peasId);
        headers.add(eventDetail);
      }
    } catch (Exception e) {
      // skip unknown and ill formed id.
      SilverLogger.getLogger(this).debug(e.getMessage(), e);
    }
    return headers;
  }

  private AlmanachService getAlmanachBm() throws AlmanachException {
    return AlmanachService.get();
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }
}
