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
import java.util.stream.Collectors;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** No invitation is sent for a rolled-back announcement. */
@Service
public class DiscussionVisioDispatcher {
  @Inject private Event<DiscussionVisioEvent> events;
  @Inject private OrganizationController organization;
  @Inject private ComponentAccessController access;

  public DiscussionVisioDispatcher() {
    // CDI constructor.
  }

  DiscussionVisioDispatcher(Event<DiscussionVisioEvent> events, OrganizationController organization,
      ComponentAccessController access) {
    this.events = events;
    this.organization = organization;
    this.access = access;
  }

  public void schedule(DiscussionVisioEvent event) { events.fire(event); }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void afterCommit(@Observes(during = TransactionPhase.AFTER_SUCCESS) DiscussionVisioEvent event) {
    var recipients = event.recipients().stream().filter(id -> {
      var user = organization.getUserDetail(id);
      return user != null && !user.isAnonymous() && !user.isAccessGuest()
          && !user.isRemovedState() && !user.isDeletedState() && !user.isDeactivatedState()
          && access.isUserAuthorized(id, event.instanceId());
    }).collect(Collectors.toSet());
    if (!recipients.isEmpty()) {
      UserNotificationHelper.buildAndSend(new DiscussionVisioNotification(
          new DiscussionVisioEvent(event.instanceId(), event.meeting(), event.sender(), recipients)));
    }
  }
}
