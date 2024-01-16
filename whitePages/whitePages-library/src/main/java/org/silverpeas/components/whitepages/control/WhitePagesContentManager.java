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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages.control;

import org.silverpeas.components.whitepages.WhitePagesException;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.util.logging.SilverLogger;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The whitePages implementation of ContentInterface.
 */
@Service
public class WhitePagesContentManager extends AbstractContentInterface {

  private static final String CONTENT_ICON_FILE_NAME = "whitePagesSmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected WhitePagesContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    final List<? extends Contribution> contributions = getAccessibleContributions(
        Collections.singletonList(new ResourceReference(resourceId, componentInstanceId)), null);
    return contributions.isEmpty() ? Optional.empty() : Optional.of(contributions.get(0));
  }

  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    try {
      final List<String> resourceIds = resourceReferences.stream()
          .map(ResourceReference::getLocalId)
          .collect(Collectors.toList());
      return CardManager.getInstance().getCardsByIds(resourceIds).stream()
          .map(c -> new CardHeader(Long.parseLong(c.getPK().getId()), c, c.getInstanceId(),
                                   c.getCreationDate(), Integer.toString(c.getCreatorId())))
          .collect(Collectors.toList());
    } catch (WhitePagesException e) {
      SilverLogger.getLogger(this).error(e);
      return Collections.emptyList();
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param card the user card
   * @return the unique silverObjectId which identified the new content
   */
  protected int createSilverContent(Connection con, Card card) throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(card));
    return getContentManager()
        .addSilverContent(con, card.getPK().getId(), card.getInstanceId(), card.getUserId(), scv);
  }

  /**
   * update the visibility attributes of the content. Here, the type of content is a
   * PublicationDetail
   * @param card the user card
   */
  protected void updateSilverContentVisibility(Card card) throws ContentManagerException {
    int silverContentId = getContentManager()
        .getSilverContentId(card.getPK().getId(), card.getPK().getComponentName());
    SilverContentVisibility scv = new SilverContentVisibility(isVisible(card));
    getContentManager()
        .updateSilverContentVisibilityAttributes(scv, silverContentId);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param pk the expert identifier to unregister
   */
  protected void deleteSilverContent(Connection con, IdPK pk) throws ContentManagerException {
    deleteSilverContent(con, pk.getId(), pk.getComponentName());
  }

  private boolean isVisible(Card card) {
    return card.getHideStatus() == 0;
  }
}
