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
package org.silverpeas.components.discussion.notification;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Bridges committed conversation changes to the platform's shared SSE/WebSocket channel. */
@Service
public class DiscussionChangeDispatcher {
  @Inject
  private Event<DiscussionChange> events;
  @Inject
  private DefaultServerEventNotifier notifier;
  @Inject
  private ComponentAccessController access;
  @Inject
  private org.silverpeas.core.admin.service.OrganizationController organization;
  @Inject
  private Event<DiscussionNewMessage> newMessages;

  public DiscussionChangeDispatcher() {
    // CDI constructor.
  }

  DiscussionChangeDispatcher(Event<DiscussionChange> events) {
    this.events = events;
  }

  public void changed(String instanceId) {
    events.fire(new DiscussionChange(instanceId, null));
  }

  public void read(String instanceId, String userId) {
    events.fire(new DiscussionChange(instanceId, userId));
  }

  public void published(String instanceId, String messageId, String authorId, String authorName, String body) {
    newMessages.fire(new DiscussionNewMessage(instanceId, messageId, authorId, authorName, body));
  }

  // AFTER_SUCCESS runs after the publishing transaction has completed. Component lookup
  // needs its own transaction, just like mention notification delivery.
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void newMessageAfterCommit(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) DiscussionNewMessage message) {
    var component = organization.getComponentInstLight(message.instanceId());
    if (component != null) {
      notifier.notify(new DiscussionMessageServerEvent(message, component, access));
    }
  }

  public void afterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) DiscussionChange change) {
    notifier.notify(new DiscussionServerEvent(change, access));
  }
}
