/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.blog.access;

import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.AbstractAccessController;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A controller of write access on a blog. It controls the user can access the blog and have enough
 * privileges to contribute in the blog.
 * <p>
 * With access rights, a user can create/modify/delete a post.
 * @author mmoquillon
 */
@Service
@Singleton
public class BlogPostWriteAccessController extends AbstractAccessController<String>
    implements BlogPostWriteAccessControl {

  @Inject
  private OrganizationController organisationController;

  @Inject
  private ComponentAccessControl componentAccessController;

  /**
   * Hidden constructor.
   */
  protected BlogPostWriteAccessController() {
  }

  @Override
  public boolean isUserAuthorized(final String userId, final ResourceIdentifier id) {
    return isUserAuthorized(userId, id.asString());
  }

  @Override
  public boolean isUserAuthorized(String userId, String blogId,
      final AccessControlContext context) {
    String[] roles = organisationController.getUserProfiles(userId, blogId);
    return componentAccessController.isUserAuthorized(userId, blogId) &&
        (SilverpeasRole.PUBLISHER.isInRole(roles) || SilverpeasRole.ADMIN.isInRole(roles));
  }
}
