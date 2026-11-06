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

import org.silverpeas.components.discussion.DiscussionSettings;
import java.util.HashMap;
import java.util.Map;
import jakarta.inject.Inject;
import org.silverpeas.components.discussion.notification.DiscussionTyping;
import org.silverpeas.components.discussion.notification.DiscussionTypingServerEvent;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Bounded, expiring state scoped by authenticated user and browser tab. */
@Service
public class DiscussionTypingService {
  private record Key(String instanceId, String userId, String session, String threadRootId, String channelScope) { }
  private record State(long expiry, long sentAt) { }
  private final Map<Key, State> states = new HashMap<>();
  @Inject
  private DiscussionChannelContext channelContext;
  @Inject
  private DiscussionAccess access;
  @Inject
  private UserProvider users;
  @Inject
  private ComponentAccessController receivers;
  @Inject
  private DefaultServerEventNotifier notifier;
  @Inject
  private org.silverpeas.core.security.session.SessionManagement sessions;

  @Inject
  private ConversationService conversations;

  public void typing(String instanceId, String session, boolean active) {
    typing(instanceId, session, active, "");
  }

  public synchronized void typing(String instanceId, String session, boolean active, String threadRootId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    if (threadRootId == null) {
      throw new IllegalArgumentException("Invalid typing scope");
    }
    if (!threadRootId.isEmpty() &&
        !threadRootId.equals(conversations.threadRoot(instanceId, threadRootId).getId())) {
      throw new IllegalArgumentException("Invalid thread root");
    }
    var user = users.getCurrentRequester();
    long now = System.currentTimeMillis();
    states.entrySet().removeIf(entry -> entry.getValue().expiry() < now);
    String scope = channelContext.getScope() == null ? "main" : channelContext.getScope();
    var key = new Key(instanceId, user.getId(), session, threadRootId, scope);
    var previous = states.get(key);
    if (active) {
      long lastSent = previous == null ? 0 : previous.sentAt();
      boolean send = now - lastSent >= DiscussionSettings.TYPING_THROTTLE_MS.value();
      states.put(key, new State(now + DiscussionSettings.typingTtlMs(), send ? now : lastSent));
      if (!send) {
        return;
      }
    } else {
      states.remove(key);
      if (previous == null) {
        return;
      }
    }
    if (states.size() > DiscussionSettings.TYPING_MAX_STATES.value()) {
      states.keySet().stream().findFirst().ifPresent(states::remove);
    }
    long expiry = states.entrySet().stream()
        .filter(entry -> entry.getKey().instanceId().equals(instanceId) && entry.getKey().userId().equals(user.getId())
            && entry.getKey().threadRootId().equals(threadRootId) && entry.getKey().channelScope().equals(scope))
        .mapToLong(entry -> entry.getValue().expiry()).max().orElse(0);
    notifier.notify(new DiscussionTypingServerEvent(new DiscussionTyping(instanceId, user.getId(),
        user.getDisplayedName(), expiry, threadRootId, scope), receivers, sessions));
  }
}
