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
package org.silverpeas.components.discussion.service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.silverpeas.components.discussion.notification.DiscussionVisioDispatcher;
import org.silverpeas.components.discussion.notification.DiscussionVisioEvent;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.ChatSettings;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Invites discussion members through native notifications to the configured video service. */
@Service
@Transactional
public class DiscussionVisioService {
  @Inject private DiscussionAccess access;
  @Inject private ChatSettings settings;
  @Inject @org.silverpeas.core.chat.servers.DefaultChatServer private ChatServer chat;
  @Inject private ConversationRepository conversations;
  @Inject private ConversationService discussion;
  @Inject private OrganizationController organization;
  @Inject private ComponentAccessController componentAccess;
  @Inject private DiscussionVisioDispatcher invitations;

  public record Meeting(String id, String title, String url) {
    public String getId() { return id; }
    public String getUrl() { return url; }
  }
  public record Options(boolean available, int invitees) {
    public boolean isAvailable() { return available; }
    public int getInvitees() { return invitees; }
  }

  public Options options(String instanceId) {
    requireWritable(instanceId, false);
    boolean available = available();
    return new Options(available, available ? recipients(instanceId).size() : 0);
  }

  public Meeting create(String instanceId, String rootId, String title) {
    requireWritable(instanceId, true);
    if (!available()) { throw new SecurityException("Video meetings are unavailable"); }
    if (title == null || title.isBlank() || title.length() > 160 || title.chars().anyMatch(Character::isISOControl)) {
      throw new IllegalArgumentException("Invalid meeting title");
    }
    if (rootId != null && !rootId.isBlank()
        && !rootId.equals(discussion.threadRoot(instanceId, rootId).getId())) {
      throw new IllegalArgumentException("Invalid discussion thread");
    }
    String id = UUID.randomUUID().toString();
    String base = settings.getVisioUrl().replaceAll("/+$", "");
    var uri = URI.create(base);
    if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
        || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null
        || uri.getUserInfo() != null) {
      throw new IllegalArgumentException("Invalid configured video URL");
    }
    var meeting = new Meeting(id, title.trim(), base + "/discussion-" + id);
    // Escape Markdown metacharacters so an invitation title cannot create another link or mention.
    String label = meeting.title().replace("\\", "\\\\").replaceAll("([\\[\\]()*_~`@<>])", "\\\\$1");
    String body = "📹 [" + label + "](" + meeting.url() + ")";
    if (rootId == null || rootId.isBlank()) { discussion.publish(instanceId, body); }
    else { discussion.reply(instanceId, rootId, body, List.of(), List.of()); }
    invitations.schedule(new DiscussionVisioEvent(instanceId, meeting,
        User.getCurrentRequester(), recipients(instanceId)));
    return meeting;
  }

  private boolean available() {
    return settings.isChatEnabled() && settings.isVisioEnabled()
        && chat.isAllowed(User.getCurrentRequester()) && !settings.getVisioUrl().isBlank();
  }

  private Set<String> recipients(String instanceId) {
    String sender = User.getCurrentRequester().getId();
    return Arrays.stream(organization.<User>getAllUsers(instanceId))
        .filter(user -> !user.isAnonymous() && !user.isAccessGuest() && !user.isRemovedState()
            && !user.isDeletedState() && !user.isDeactivatedState())
        .map(User::getId).filter(id -> !sender.equals(id))
        .filter(id -> componentAccess.isUserAuthorized(id, instanceId)).collect(Collectors.toSet());
  }

  private void requireWritable(String instanceId, boolean lock) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (conversations.current(instanceId, lock).isArchived()) {
      throw new IllegalArgumentException("This channel is archived");
    }
  }
}
