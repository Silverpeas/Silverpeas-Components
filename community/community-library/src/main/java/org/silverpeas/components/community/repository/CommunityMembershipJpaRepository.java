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
 */
package org.silverpeas.components.community.repository;

import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.components.community.model.MembershipStatus;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.PaginationCriterion;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;
import org.silverpeas.core.util.SilverpeasList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Query;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of the repository of members of communities of users by extending the
 * {@link org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository}
 * base repository that provides all the basic and necessary methods to save, to update, to delete
 * and to get the business entities by using the JPA engine.
 * @author mmoquillon
 */
@Repository
public class CommunityMembershipJpaRepository extends SilverpeasJpaEntityRepository<CommunityMembership>
    implements CommunityMembershipRepository {

  private static final String COMMUNITY_FIELD = "community";
  private static final String STATUS = "status";
  private static final String SELECT_MEMBER =
      "select m from " + CommunityMembership.class.getSimpleName() + " m ";

  @Override
  public CommunityMembershipsTable getMembershipsTable(final CommunityOfUsers community) {
    return new CommunityMembershipsTableImpl(community);
  }

  @Override
  public long deleteByComponentInstanceId(final String componentInstanceId) {
    Query deleteQuery = getEntityManager().createQuery(
        "delete from CommunityMembership m where m.community in (select c from CommunityOfUsers c " +
            "where c.componentInstanceId = :id)");
    return newNamedParameters().add("id", componentInstanceId).applyTo(deleteQuery).executeUpdate();
  }

  private class CommunityMembershipsTableImpl implements CommunityMembershipsTable {

    private final CommunityOfUsers community;

    public CommunityMembershipsTableImpl(final CommunityOfUsers community) {
      this.community = community;
    }

    @Override
    public void deleteAll() {
      Query deleteQuery = getEntityManager().createQuery(
          "delete from CommunityMembership m where m.community = :community");
      newNamedParameters().add(COMMUNITY_FIELD, community).applyTo(deleteQuery).executeUpdate();
    }

    @Override
    public Optional<CommunityMembership> getByUser(final User user) {
      NamedParameters parameters = newNamedParameters()
          .add("userId", Integer.valueOf(user.getId()))
          .add(COMMUNITY_FIELD, community);
      return Optional.ofNullable(findFirstByNamedQuery("byUserIdAndByCommunity", parameters));
    }

    @Override
    public SilverpeasList<CommunityMembership> getPending(@Nullable final PaginationPage page) {
      NamedParameters parameters = newNamedParameters()
          .add(COMMUNITY_FIELD, community)
          .add(STATUS, MembershipStatus.PENDING);
      String query = SELECT_MEMBER +
          "where m.community = :community and status = :status order by m.lastUpdateDate DESC";
      PaginationCriterion pagination =
          page == null ? PaginationCriterion.NO_PAGINATION : page.asCriterion();
      return listFromJpqlString(query, parameters, pagination, CommunityMembership.class);
    }

    @Override
    public SilverpeasList<CommunityMembership> getMembers(@Nonnull final PaginationPage page) {
      Objects.requireNonNull(page);
      NamedParameters parameters = newNamedParameters()
          .add(COMMUNITY_FIELD, community)
          .add(STATUS, MembershipStatus.COMMITTED);
      return listFromJpqlString(SELECT_MEMBER +
              "where m.status = :status and m.community = :community order by m.lastUpdateDate DESC",
          parameters, page.asCriterion());
    }

    @Override
    public SilverpeasList<CommunityMembership> getAll(@Nonnull final PaginationPage page) {
      Objects.requireNonNull(page);
      NamedParameters parameters = newNamedParameters()
          .add(COMMUNITY_FIELD, community);
      return listFromJpqlString(SELECT_MEMBER +
              "where m.community = :community order by m.lastUpdateDate DESC", parameters,
          page.asCriterion());
    }

    @Override
    public List<CommunityMembership> getAllMembers() {
      NamedParameters parameters = newNamedParameters()
          .add(COMMUNITY_FIELD, community);
      return findByNamedQuery("allNonRemoved", parameters);
    }

    @Override
    public boolean isEmpty() {
      Query countQuery = getEntityManager().createQuery(
          "select count(m) from CommunityMembership m where m.community = :community");
      long count =
          (long) newNamedParameters().add(COMMUNITY_FIELD, community).applyTo(countQuery)
              .getSingleResult();
      return count == 0;
    }
  }
}
