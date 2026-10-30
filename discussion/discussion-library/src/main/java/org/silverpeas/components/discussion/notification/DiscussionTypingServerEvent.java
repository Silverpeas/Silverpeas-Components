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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.JSONCodec;

public class DiscussionTypingServerEvent extends AuthorizedDiscussionEvent implements IgnoreStoring {
  public DiscussionTypingServerEvent(DiscussionTyping typing, ComponentAccessController access,
      org.silverpeas.core.security.session.SessionManagement sessions) {
    super(typing.instanceId(), access);
    getId();
    withData((session, receiver) -> JSONCodec.encodeObject(json -> json.put("instanceId", typing.instanceId())
        .put("userId", typing.userId()).put("name", typing.name()).put("revision", getId())
        .put("ttl", Math.max(0, typing.expiresAt() - System.currentTimeMillis()))));
    this.userId = typing.userId();
    this.sessions = sessions;
  }
  private final String userId;
  private final org.silverpeas.core.security.session.SessionManagement sessions;
  @Override
  public ServerEventName getName() { return () -> "DISCUSSION_TYPING"; }
  @Override
  public boolean isConcerned(String sessionId, User receiver) {
    return receiver != null && !userId.equals(receiver.getId()) && super.isConcerned(sessionId, receiver)
        && sessions.getDistinctConnectedUsersList(receiver).stream()
            .anyMatch(session -> userId.equals(session.getUser().getId()));
  }
}
