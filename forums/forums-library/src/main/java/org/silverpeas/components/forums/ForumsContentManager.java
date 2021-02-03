/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.components.forums;

import org.silverpeas.components.forums.model.ForumPK;
import org.silverpeas.components.forums.service.ForumsServiceProvider;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.model.Contribution;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The forums implementation of ContentInterface.
 */
@Service
public class ForumsContentManager extends AbstractContentInterface {

  private static final String CONTENT_ICON_FILE_NAME = "forumsSmall.gif";

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected ForumsContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    final Contribution forumDetail = ForumsServiceProvider.getForumsService()
        .getForumDetail(new ForumPK(componentInstanceId, resourceId));
    return Optional.ofNullable(forumDetail);
  }

  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    List<ForumPK> ids = resourceReferences.stream()
        .map(r -> new ForumPK(r.getComponentInstanceId(), r.getLocalId()))
        .collect(Collectors.toList());
    return new ArrayList<>(ForumsServiceProvider.getForumsService().getForums(ids));
  }

  @Override
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      final T contribution) {
    return new SilverContentVisibility(true);
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param forumPK the content to register
   * @param userId the creator of the content
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, ForumPK forumPK, String userId)
      throws ContentManagerException {
    return createSilverContent(con, forumPK.getId(), forumPK.getComponentName(), userId);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param forumPK the identifiant of the content to unregister
   */
  public void deleteSilverContent(Connection con, ForumPK forumPK) throws ContentManagerException {
    deleteSilverContent(con, forumPK.getId(), forumPK.getComponentName());
  }
}
