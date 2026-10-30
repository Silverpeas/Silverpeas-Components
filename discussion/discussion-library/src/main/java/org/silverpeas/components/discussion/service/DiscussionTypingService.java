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
  private record Key(String instanceId, String userId, String session) { }
  private record State(long expiry, long sentAt) { }
  private final Map<Key, State> states = new HashMap<>();
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

  public synchronized void typing(String instanceId, String session, boolean active) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var user = users.getCurrentRequester();
    long now = System.currentTimeMillis();
    states.entrySet().removeIf(entry -> entry.getValue().expiry() < now);
    var key = new Key(instanceId, user.getId(), session);
    var previous = states.get(key);
    if (active) {
      long lastSent = previous == null ? 0 : previous.sentAt();
      boolean send = now - lastSent >= 2000;
      states.put(key, new State(now + 8000, send ? now : lastSent));
      if (!send) {
        return;
      }
    } else {
      states.remove(key);
      if (previous == null) {
        return;
      }
    }
    if (states.size() > 10000) {
      states.keySet().stream().findFirst().ifPresent(states::remove);
    }
    long expiry = states.entrySet().stream()
        .filter(entry -> entry.getKey().instanceId().equals(instanceId) && entry.getKey().userId().equals(user.getId()))
        .mapToLong(entry -> entry.getValue().expiry()).max().orElse(0);
    notifier.notify(new DiscussionTypingServerEvent(new DiscussionTyping(instanceId, user.getId(),
        user.getDisplayedName(), expiry), receivers, sessions));
  }
}
