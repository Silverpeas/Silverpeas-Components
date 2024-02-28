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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.community.model;

import org.silverpeas.core.admin.component.notification.ComponentInstanceEvent;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Inject;

/**
 * The community of users application is removed, that is to say it is moved into the bin of the
 * spaces and component instances. In a such situation, the group of members of this community
 * should be also removed (id est moved into the bin of user groups)
 *
 * @author mmoquillon
 */
@Technical
@Bean
public class CommunityOfUsersRemovingListener
    extends CDIResourceEventListener<ComponentInstanceEvent> {

  @Inject
  private Administration administration;

  @Override
  public void onRemoving(ComponentInstanceEvent event) {
    var componentInstance = event.getTransition().getBefore();
    CommunityOfUsers.getByComponentInstanceId(componentInstance.getId())
        .filter(c -> c.groupId != null)
        .ifPresent(c -> {
          String groupId = String.valueOf(c.groupId);
          Transaction.performInOne(() -> {
            administration.removeGroup(groupId);
            return null;
          });
        });
  }

  @Override
  public void onRecovery(ComponentInstanceEvent event) {
    var componentInstance = event.getTransition().getBefore();
    CommunityOfUsers.getByComponentInstanceId(componentInstance.getId())
        .filter(c -> c.groupId != null)
        .ifPresent(c -> {
          String groupId = String.valueOf(c.groupId);
          Transaction.performInOne(() -> {
            administration.restoreGroup(groupId);
            return null;
          });
        });
  }
}
  