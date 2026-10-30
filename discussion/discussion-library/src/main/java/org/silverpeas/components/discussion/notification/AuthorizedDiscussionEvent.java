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

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.CommonServerEvent;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Shared receiver authorization, checked at delivery time. */
public abstract class AuthorizedDiscussionEvent extends CommonServerEvent {
  private final String instanceId;
  private final ComponentAccessController access;

  protected AuthorizedDiscussionEvent(String instanceId, ComponentAccessController access) {
    this.instanceId = instanceId;
    this.access = access;
  }

  @Override
  public boolean isConcerned(String sessionId, User receiver) {
    if (receiver == null || receiver.isAnonymous() || receiver.isAccessGuest()
        || receiver.isDeletedState() || receiver.isRemovedState()
        || !access.isUserAuthorized(receiver.getId(), instanceId)) {
      return false;
    }
    var role = SilverpeasRole.getHighestFrom(access.getUserRoles(receiver.getId(), instanceId, AccessControlContext.init()));
    return role != null && role.isGreaterThanOrEquals(SilverpeasRole.WRITER);
  }
}
