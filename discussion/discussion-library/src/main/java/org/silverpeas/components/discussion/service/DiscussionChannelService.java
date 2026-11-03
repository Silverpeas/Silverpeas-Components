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
import java.util.Comparator;
import java.util.List;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionReadState;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.components.discussion.notification.DiscussionChangeDispatcher;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;

/** Public channels inherit instance permissions. Archiving retains their history and links. */
@Service
@Transactional
public class DiscussionChannelService {
  public record Channel(Conversation conversation, long unread, long mentions) {
    public Conversation getConversation() { return conversation; }
    public long getUnread() { return unread; }
    public long getMentions() { return mentions; }
  }
  @Inject
  private DiscussionAccess access;
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionMessageRepository messages;
  @Inject
  private DiscussionReadStateRepository states;
  @Inject
  private DiscussionChannelContext context;
  @Inject
  private DiscussionChangeDispatcher changes;

  public Conversation select(String instanceId, String scope) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var channel = conversations.channel(instanceId, scope, false);
    context.select(channel.getScopeKey());
    return channel;
  }

  public Conversation selectMessage(String instanceId, String messageId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var channel = conversations.forMessage(instanceId, messageId);
    context.select(channel.getScopeKey());
    return channel;
  }

  public List<Channel> list(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    String user = User.getCurrentRequester().getId();
    return conversations.channels(instanceId).stream()
        .sorted(Comparator.comparing(Conversation::isMain).reversed()
            .thenComparing(Conversation::isArchived)
            .thenComparing(c -> c.getChannelName() == null ? "" : c.getChannelName(), String.CASE_INSENSITIVE_ORDER))
        .map(c -> {
          long lastRead = states.find(c.getId(), user).map(DiscussionReadState::getLastReadSequence).orElse(0L);
          return new Channel(c, messages.unread(c.getId(), user, lastRead), messages.unreadMentions(c.getId(), user, lastRead));
        })
        .toList();
  }

  public String notificationMode(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    return states.find(conversations.current(instanceId, false).getId(), User.getCurrentRequester().getId())
        .map(DiscussionReadState::getNotificationMode).orElse("ALL");
  }

  public void notifications(String instanceId, String mode) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var user = User.getCurrentRequester();
    var channel = conversations.current(instanceId, true);
    var state = states.find(channel.getId(), user.getId()).orElseGet(() -> {
      var value = new DiscussionReadState(channel.getId(), user.getId());
      value.createdBy(user);
      return value;
    });
    state.setNotificationMode(mode);
    state.updatedBy(user);
    states.save(state);
    changes.read(instanceId, user.getId());
  }

  public Conversation create(String instanceId, String name, String description) {
    access.require(instanceId, SilverpeasRole.ADMIN);
    // Serialize channel management through the permanent general channel.
    conversations.main(instanceId, true);
    var channel = Conversation.channel(instanceId, name, description);
    requireUniqueName(instanceId, channel);
    channel.createdBy(User.getCurrentRequester());
    conversations.save(channel);
    changes.changed(instanceId);
    return channel;
  }

  public void update(String instanceId, String scope, String name, String description, boolean archived) {
    access.require(instanceId, SilverpeasRole.ADMIN);
    conversations.main(instanceId, true);
    var channel = conversations.channel(instanceId, scope, true);
    channel.rename(name, description);
    requireUniqueName(instanceId, channel);
    channel.setArchived(archived);
    channel.updatedBy(User.getCurrentRequester());
    conversations.save(channel);
    changes.changed(instanceId);
  }

  private void requireUniqueName(String instanceId, Conversation channel) {
    if (conversations.channels(instanceId).stream().anyMatch(c -> !c.getScopeKey().equals(channel.getScopeKey())
        && (c.getChannelName() != null && c.getChannelName().equalsIgnoreCase(channel.getChannelName())
            || c.isMain() && c.getChannelName() == null && java.util.Set.of("général", "general", "allgemein")
                .contains(channel.getChannelName().toLowerCase(java.util.Locale.ROOT))))) {
      throw new IllegalArgumentException("A channel already has this name");
    }
  }
}
