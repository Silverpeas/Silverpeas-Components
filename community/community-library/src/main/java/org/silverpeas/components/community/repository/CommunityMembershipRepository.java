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
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SilverpeasList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A repository to persist the memberships to a community of users. This repository doesn't support
 * the deletion of the entities in the repository neither the getting of all the entities. When a
 * member is removed from a community, only its status is changed: he passes then from the
 * {@link org.silverpeas.components.community.model.MembershipStatus#COMMITTED} status to the
 * {@link org.silverpeas.components.community.model.MembershipStatus#REMOVED} one; this is for
 * keeping the history of the memberships of a community. Members of a community is really deleted
 * only when their community is also deleted.
 * @author mmoquillon
 */
public interface CommunityMembershipRepository extends EntityRepository<CommunityMembership> {

  /**
   * The table of memberships of a given community of users. This table gathers all the memberships
   * of the users to the given community; it includes both the removed memberships, the pending ones
   * and the committed ones. In fact, when a member is removed from a community, his membership in
   * the table is never deleted but just updated with the status
   * {@link org.silverpeas.components.community.model.MembershipStatus#REMOVED}.
   */
  interface CommunityMembershipsTable {

    /**
     * Deletes all the memberships in this table. This method has to be invoked only when the
     * community is being itself deleted. This method should be invoked only by the
     * {@link org.silverpeas.components.community.CommunityInstancePreDestruction} bean.
     */
    void deleteAll();

    /**
     * Gets the membership to the  community of the specified user.  Only committed or pending
     * membership is considered.
     * @param user a user.
     * @return either a {@link CommunityMembership} instance related to the specified user or
     * nothing if the user isn't (anymore) member of the community of users.
     */
    Optional<CommunityMembership> getByUser(final User user);

    /**
     * Gets the membership that are pending and included in the specified pagination page. If the
     * page is null then all the pending memberships are returned.
     * @param page the page from which the pending membership are got or null to get all the pending
     * memberships from this table.
     * @return a paginated list of pending memberships to the community if users.
     */
    SilverpeasList<CommunityMembership> getPending(@Nullable final PaginationPage page);

    /**
     * Gets the committed memberships of the specified community included in the specified
     * pagination page.
     * @param page the page from which the memberships are got.
     * @return a paginated list of committed memberships to the community of users.
     */
    SilverpeasList<CommunityMembership> getMembers(@Nonnull final PaginationPage page);

    /**
     * Gets all the memberships registered into this table and included in the specified pagination
     * page. All the memberships are taken, whatever their status, and hence even those being
     * pending, removed or refused.
     * @param page the page from which the memberships are got.
     * @return a paginated list of memberships to the community of users.
     */
    SilverpeasList<CommunityMembership> getAll(@Nonnull final PaginationPage page);

    /**
     * Gets all the committed or pending memberships to the community of users. This method is
     * mainly to be used for synchronization as no pagination is performed.
     * @return a list of actual memberships of the community.
     */
    List<CommunityMembership> getAllMembers();

    /**
     * Is this table is empty?
     * @return true if the community of users hasn't yet any memberships registered. False
     * otherwise.
     */
    boolean isEmpty();

  }

  /**
   * Gets the single instance of this repository.
   */
  static CommunityMembershipRepository get() {
    return ServiceProvider.getSingleton(CommunityMembershipRepository.class);
  }

  /**
   * Gets the table of members of the specified community.
   * @param community a community of users.
   * @return a {@link CommunityMembershipsTable} instance through which the members of the specified
   * community can be requested.
   */
  CommunityMembershipsTable getMembershipsTable(final CommunityOfUsers community);

  /**
   * Deletes all the members of the community of users managed by the specified component instance.
   * The method has to be invoked only when the given component instance, and thus the community
   * managed of users by it, is being deleted.
   * @param componentInstanceId the unique component instance identifier.
   * @return the number of deleted members.
   * @see CommunityMembershipRepository.CommunityMembershipsTable#deleteAll()
   */
  @Override
  long deleteByComponentInstanceId(final String componentInstanceId);

  /**
   * @param entity the entity/entities to delete.
   * @apiNote Not supported
   */
  @Override
  default void delete(CommunityMembership... entity) {
    delete(Arrays.asList(entity));
  }

  /**
   * @param entities the entities to delete.
   * @apiNote Not supported
   */
  @Override
  default void delete(List<CommunityMembership> entities) {
    throw new NotSupportedException("Deletion of members of a community isn't supported");
  }

  /**
   * @param ids the identifiers of the entities to delete.
   * @return nothing
   * @apiNote Not supported
   */
  @Override
  default long deleteById(final String... ids) {
    return deleteById(Arrays.asList(ids));
  }

  /**
   * @param ids the identifiers of the entities to delete.
   * @return nothing
   * @apiNote Not supported
   */
  @Override
  default long deleteById(final Collection<String> ids) {
    throw new NotSupportedException("Deletion of members of a community isn't supported");
  }

  /**
   * @return nothing
   * @apiNote Not supported
   */
  @Override
  default SilverpeasList<CommunityMembership> getAll() {
    throw new NotSupportedException(
        "Getting all members of all community isn't supported for secure and performance reasons");
  }
}
