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

import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.notification.SpaceEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.kernel.annotation.Technical;

import javax.inject.Inject;

/**
 * When a community space is removed, id est moved into the administrative bin, all the user
 * profiles of this space are automatically deleted and cannot then be restored. As such, the
 * existing members of the space are considered then to be also deleted. Thus, the members group
 * have to be also deleted, as well as the memberships of the community of users.
 *
 * @author mmoquillon
 */
@Technical
@Bean
class CommunitySpaceRemovingListener extends CDIResourceEventListener<SpaceEvent> {

  @Inject
  private Administration administration;

  @Inject
  private CommunityOfUsersRepository communityRepository;

  @Inject
  private CommunityMembershipRepository membershipRepository;

  @Override
  public void onRemoving(SpaceEvent event) {
    var space = event.getTransition().getBefore();
    var community = CommunityOfUsers.getBySpaceId(space.getId());
    community
        .filter(c -> c.groupId != null)
        .ifPresent(c -> {
          String groupId = String.valueOf(c.groupId);
          c.groupId = null;

          Transaction.performInOne(() -> {
            communityRepository.save(c);
            // to ensure the members group referred by the community is well removed  before
            // deleting the referenced group
            communityRepository.flush();
            // clean up the memberships to this community
            membershipRepository.getMembershipsTable(c).deleteAll();
            // then delete the group
            administration.deleteGroupById(groupId, true);
            return null;
          });
        });
  }
}
  