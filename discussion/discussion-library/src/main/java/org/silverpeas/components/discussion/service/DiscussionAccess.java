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
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;

/** Resolves rights from the authenticated Silverpeas requester, never from submitted user IDs. */
@Service
public class DiscussionAccess {
  @Inject
  private ComponentAccessController accessController;

  public SilverpeasRole require(String instanceId, SilverpeasRole minimum) {
    SilverpeasRole requiredRole = minimum == SilverpeasRole.READER ? SilverpeasRole.WRITER : minimum;
    User user = User.getCurrentRequester();
    if (user == null || !accessController.isUserAuthorized(user.getId(), instanceId)) {
      throw new SecurityException("Discussion access denied");
    }
    SilverpeasRole role = SilverpeasRole.getHighestFrom(accessController.getUserRoles(
        user.getId(), instanceId, AccessControlContext.init()));
    if (role == null || !role.isGreaterThanOrEquals(requiredRole)
        || user.isAccessGuest() || user.isAnonymous()) {
      throw new SecurityException("Discussion operation denied");
    }
    return role;
  }
}
