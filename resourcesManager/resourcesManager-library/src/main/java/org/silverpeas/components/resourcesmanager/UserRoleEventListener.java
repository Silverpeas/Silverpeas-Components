/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.resourcesmanager;

import org.silverpeas.core.admin.user.notification.role.UserRoleEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;
import java.util.Set;

/**
 * Listeners of events about changes in the users list of the role named 'responsable'. For any
 * users who don't play anymore this role, they are removed from the managers in charge of the
 * validation for all the bookable resources handled by the Resources Manager applications.
 *
 * @author mmoquillon
 */
@Service
public class UserRoleEventListener extends CDIResourceEventListener<UserRoleEvent> {

  private static final String MANAGER_ROLE = "responsable";

  @Inject
  private ResourcesManagersSynchronizer synchronizer;

  @Override
  public void onDeletion(UserRoleEvent event) {
    if (event.getRole().equals(MANAGER_ROLE)) {
      Set<String> userIds = event.getUserIds();
      event.getInstanceIds().forEach(a -> synchronizer.synchronize(a, userIds));
    }
  }
}
  