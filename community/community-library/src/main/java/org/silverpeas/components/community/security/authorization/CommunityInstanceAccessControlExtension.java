/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.community.security.authorization;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.security.authorization.DefaultInstanceAccessControlExtension;

import javax.inject.Named;

import static org.silverpeas.core.admin.space.SpaceInst.SPACE_KEY_PREFIX;
import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * @author silveryocha
 */
@Named
@Service
public class CommunityInstanceAccessControlExtension
    extends DefaultInstanceAccessControlExtension {

  @Override
  protected boolean mustUserBeComponentInstanceAdminIfManagerOfParentSpace(
      final ComponentAccessController.DataManager dataManager, final User user,
      final SilverpeasComponentInstance componentInstance) {
    return dataManager.getManageableSpaceIds(user.getId())
        .contains(componentInstance.getSpaceId().replace(SPACE_KEY_PREFIX, EMPTY));
  }

  @Override
  protected boolean canAnonymousAccessInstance() {
    return false;
  }
}
