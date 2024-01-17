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
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.components.community;

import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * A listener of events concerning an operation applied on a user profile in Silverpeas. This
 * listener has for goal to listen events triggered by a user profile deletion in order to check
 * whether the user being deleted is a member of one or more community of users in Silverpeas. In
 * this case, it will remove him from those communities.
 * @author mmoquillon
 */
@Bean
public class CommunityUserDeletionListener extends CDIResourceEventListener<UserEvent> {

  @Inject
  private CommunityOfUsersRepository repository;

  @Override
  @Transactional
  public void onDeletion(final UserEvent event) {
    User user = event.getTransition().getBefore();
    repository.getAllByUserId(user.getId()).forEach(c -> c.removeMembership(user));
  }
}
