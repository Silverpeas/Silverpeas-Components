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
package org.silverpeas.components.community.repository;

import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.Optional;

/**
 * This repository manages the persistence of communities of users. It abstracts the nature of the
 * datasource in which are stored the communities as well as the persistence engine used to manage
 * the access to such a datasource.
 */
public interface CommunityOfUsersRepository extends EntityRepository<CommunityOfUsers> {

  /**
   * Gets the single instance of this repository.
   */
  static CommunityOfUsersRepository get() {
    return ServiceProvider.getSingleton(CommunityOfUsersRepository.class);
  }

  /**
   * Gets the community of users managed by the specified component instance.
   * @param componentInstanceId the unique identifier of a component instance.
   * @return an {@link Optional} with the {@link CommunityOfUsers} instance or an empty
   * {@link Optional} if no such component instance exists.
   */
  Optional<CommunityOfUsers> getByComponentInstanceId(String componentInstanceId);

  /**
   * Gets the community of users of the specified collaborative space.
   * @param spaceId the unique identifier of a space.
   * @return an {@link Optional} with the {@link CommunityOfUsers} instance or an empty
   * {@link Optional} if no such space exists or if the space doesn't have any community.
   */
  Optional<CommunityOfUsers> getBySpaceId(String spaceId);

  /**
   * Gets all the community of users the specified user is currently a member.
   * @param userId the unique identifier of a user.
   * @return a list of communities to which the given user is a member.
   */
  List<CommunityOfUsers> getAllByUserId(final String userId);
}