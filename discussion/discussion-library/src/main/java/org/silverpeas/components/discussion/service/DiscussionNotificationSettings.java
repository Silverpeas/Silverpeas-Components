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

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.components.discussion.model.DiscussionReadState;
import org.silverpeas.components.discussion.notification.DiscussionNotificationPreferenceEvent;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Per-user, per-instance preferences, independent of mention notification channels. */
@Service
@Transactional
public class DiscussionNotificationSettings {
  public record Change(String instanceId, String userId, DesktopPreference preference) { }
  @Inject
  private DiscussionAccess access;
  @Inject
  private ConversationRepository conversations;
  @Inject
  private DiscussionReadStateRepository states;
  @Inject
  private Event<Change> changes;
  @Inject
  private DefaultServerEventNotifier notifier;
  @Inject
  private ComponentAccessController receivers;

  public DesktopPreference current(String instanceId) {
    access.require(instanceId, SilverpeasRole.WRITER);
    String userId = User.getCurrentRequester().getId();
    var conversation = conversations.main(instanceId, false);
    return states.find(conversation.getId(), userId)
        .map(state -> new DesktopPreference(state.isDesktopNotifications(), state.getVersion()))
        .orElse(new DesktopPreference(true, -1));
  }

  public DesktopPreference save(String instanceId, boolean enabled) {
    access.require(instanceId, SilverpeasRole.WRITER);
    var user = User.getCurrentRequester();
    var conversation = conversations.main(instanceId, true);
    var state = states.find(conversation.getId(), user.getId()).orElseGet(() -> {
      var created = new DiscussionReadState(conversation.getId(), user.getId());
      created.createdBy(user);
      return created;
    });
    state.setDesktopNotifications(enabled);
    state.updatedBy(user);
    states.save(state);
    states.flush();
    var preference = new DesktopPreference(enabled, state.getVersion());
    changes.fire(new Change(instanceId, user.getId(), preference));
    return preference;
  }

  /** Internal receiver lookup: the server event has already checked this user's instance access. */
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public boolean enabledFor(String instanceId, String userId) {
    return states.desktopEnabled(instanceId, userId);
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void afterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) Change change) {
    notifier.notify(new DiscussionNotificationPreferenceEvent(change, receivers));
  }
}
