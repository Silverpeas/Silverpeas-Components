/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages.control;

import org.silverpeas.components.whitepages.WhitePagesException;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.pdc.classification.ClassifyEngine;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The whitePages implementation of ContentInterface.
 */
public class WhitePagesContentManager implements ContentInterface {

  /**
   * Find all the SilverContent with the given SilverContentId
   */
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String peasId,
      String userId) {
    return getHeaders(getContentManager().getResourcesMatchingContents(ids), peasId);
  }

  private List getHeaders(List<String> ids, String instanceId) {
    ArrayList<CardHeader> headers = new ArrayList<>();
    try {
      ArrayList<Card> cards = (ArrayList<Card>) CardManager.getInstance().getCardsByIds(ids);
      for (Card card : cards) {
        CardHeader header = new CardHeader(Long.parseLong(card.getPK().getId()), card, instanceId,
            card.getCreationDate(), Integer.toString(card.getCreatorId()));
        headers.add(header);
      }
    } catch (WhitePagesException e) {
      // skip unknown and ill formed id.
    }
    Collections.sort(headers);
    return headers;
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param card the user card
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, Card card) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(card));

    return getContentManager()
        .addSilverContent(con, card.getPK().getId(), card.getInstanceId(), card.getUserId(), scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param card the user card
   */
  public void updateSilverContentVisibility(Card card) throws ContentManagerException {
    int silverContentId = getContentManager()
        .getSilverContentId(card.getPK().getId(), card.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(card));

    getContentManager()
        .updateSilverContentVisibilityAttributes(scv, silverContentId);
    ClassifyEngine.clearCache();
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pk the expert identifier to unregister
   */
  public void deleteSilverContent(Connection con, IdPK pk) throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(pk.getId(), pk.getComponentName());

    getContentManager().removeSilverContent(con, contentId, pk.getComponentName());
  }

  private boolean isVisible(Card card) {
    return (card.getHideStatus() == 0);
  }

  private ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }

}
