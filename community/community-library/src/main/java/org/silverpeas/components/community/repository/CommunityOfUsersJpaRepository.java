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
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;

/**
 * Implementation of the repository of Community of users by extending the
 * {@link org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository} base
 * repository that provides all the basic and necessary methods to save, to update, to delete and to
 * get the business entities by using the JPA engine.
 */
@Repository
public class CommunityOfUsersJpaRepository
    extends BasicJpaEntityRepository<CommunityOfUsers>
    implements CommunityOfUsersRepository {

  @Override
  public Optional<CommunityOfUsers> getByComponentInstanceId(final String componentInstanceId) {
    NamedParameters parameters = newNamedParameters();
    return Optional.ofNullable(findFirstByNamedQuery("CommunityByComponentInstanceId",
        parameters.add("componentInstanceId", componentInstanceId)));
  }

  @Override
  public Optional<CommunityOfUsers> getBySpaceId(final String spaceId) {
    NamedParameters parameters = newNamedParameters();
    return Optional.ofNullable(findFirstByNamedQuery("CommunityBySpaceId",
        parameters.add("spaceId", spaceId)));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<CommunityOfUsers> getAllByUserId(final String userId) {
    Query query = getEntityManager().createQuery(
        "select c from CommunityOfUsers c inner join CommunityMembership m on m.community = c " +
            "where m.userId = :userId and m.status = org.silverpeas.components.community.model" +
            ".MembershipStatus.COMMITTED");
    return newNamedParameters().add("userId", parseInt(userId)).applyTo(query).getResultList();
  }
}