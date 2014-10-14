/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.blog.access;

import com.silverpeas.accesscontrol.AbstractAccessController;
import com.silverpeas.accesscontrol.AccessControlContext;
import com.silverpeas.accesscontrol.AccessController;
import com.stratelia.webactiv.SilverpeasRole;
import org.silverpeas.accesscontrol.ComponentAccessControl;
import org.silverpeas.accesscontrol.ComponentAccessController;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A controller of write access on a blog. It controls the user can access the blog and have enough
 * privileges to contribute in the blog.
 * <p/>
 * With right accesses, a user can create/modify/delete a post.
 * @author mmoquillon
 */
@Singleton
@BlogPostWriteAccessControl
public class BlogPostWriteAccessController extends AbstractAccessController<String> {

  @Inject
  private OrganisationController organisationController;

  @Inject
  private @ComponentAccessControl AccessController<String> componentAccessController;

  /**
   * Hidden constructor.
   */
  protected BlogPostWriteAccessController() {
  }

  @Override
  public boolean isUserAuthorized(String userId, String blogId,
      final AccessControlContext context) {
    String[] roles = organisationController.getUserProfiles(userId, blogId);
    return componentAccessController.isUserAuthorized(userId, blogId) &&
        (SilverpeasRole.publisher.isInRole(roles) || SilverpeasRole.admin.isInRole(roles));
  }
}
