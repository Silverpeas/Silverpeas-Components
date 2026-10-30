/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.discussion.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.util.URLUtil;

/** Derives references from the whole conversation and resolves titles through native authorization. */
@Service
@Transactional
public class DiscussionPublicationService {
  @Inject
  private DiscussionAccess access;
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionMessageRepository messages;
  @Inject
  private PublicationService publications;
  @Inject
  private PublicationAccessControl publicationAccess;
  @Inject
  private UserProvider users;

  public List<DiscussionPublication> list(String instanceId, String language) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var conversation = conversations.main(instanceId, false);
    var application = URI.create(URLUtil.getAbsoluteApplicationURL());
    List<String> ids;
    try (var bodies = messages.publicationLinkBodies(conversation.getId())) {
      ids = bodies.flatMap(body -> DiscussionPublicationLinks.ids(body, application).stream()).distinct().toList();
    }
    if (ids.isEmpty()) {
      return List.of();
    }
    var result = new ArrayList<DiscussionPublication>();
    String userId = users.getCurrentRequester().getId();
    for (int offset = 0; offset < ids.size(); offset += 100) {
      var batch = publications.getByIds(ids.subList(offset, Math.min(offset + 100, ids.size())));
      batch.stream().filter(publication -> publicationAccess.isUserAuthorized(userId, publication))
          .map(publication -> new DiscussionPublication(publication.getId(),
              publication.getName(language), URLUtil.getSimpleURL(URLUtil.URL_PUBLI, publication.getId())))
          .forEach(result::add);
    }
    return result.stream().sorted(Comparator.comparing(DiscussionPublication::title,
        String.CASE_INSENSITIVE_ORDER).thenComparing(DiscussionPublication::id)).toList();
  }
}
