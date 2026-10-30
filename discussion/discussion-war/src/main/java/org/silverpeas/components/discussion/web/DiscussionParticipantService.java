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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.silverpeas.components.discussion.service.ConversationService;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.session.SessionManagement;

/** Resolves authors and presence through native Silverpeas services. */
@Service
public class DiscussionParticipantService {
  @Inject
  private ConversationService conversations;
  @Inject
  private UserProvider users;
  @Inject
  private SessionManagement sessions;

  public List<DiscussionParticipant> list(String instanceId) {
    // Resolve the conversation first: this checks the requester's instance permissions.
    List<String> authorIds = conversations.participantIds(instanceId);
    var connected = sessions.getDistinctConnectedUsersList(users.getCurrentRequester()).stream()
        .map(session -> session.getUser().getId()).collect(Collectors.toSet());
    return authorIds.stream().distinct().map(users::getUser).filter(Objects::nonNull)
        .filter(user -> !user.isRemovedState() && !user.isDeletedState())
        .map(user -> new DiscussionParticipant(user.getId(), user.getDisplayedName(),
            user.getSmallAvatar(), connected.contains(user.getId())))
        .sorted(Comparator.comparing(DiscussionParticipant::connected).reversed()
            .thenComparing(DiscussionParticipant::name, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(DiscussionParticipant::id))
        .toList();
  }
}
