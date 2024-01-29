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

package org.silverpeas.components.community.model;

import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.core.util.SilverpeasList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A provider of memberships to a community of users. The provider is always related to a community
 * of users for which it provides access to his table of  memberships in the database. All the
 * memberships to a community of users are accessed only through such a provider with which
 * memberships can be requested on only some of a subset of them.
 * @author mmoquillon
 */
public class CommunityMembershipsProvider {

  private final CommunityOfUsers community;

  private final CommunityMembershipRepository repository;
  private final CommunityMembershipRepository.CommunityMembershipsTable memberships;
  private static final Map<String, OffsetDateTime> lastSynchronizations = new HashMap<>();

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.components.community.settings.communitySettings");

  /**
   * Gets the memberships provider of the specified community.
   * @param community a community of users.
   * @return a provider of memberships to the given community of users.
   */
  static CommunityMembershipsProvider getProvider(final CommunityOfUsers community) {
    return new CommunityMembershipsProvider(community);
  }

  private CommunityMembershipsProvider(final CommunityOfUsers community) {
    this.community = community;
    this.repository = ServiceProvider.getService(CommunityMembershipRepository.class);
    this.memberships = repository.getMembershipsTable(this.community);
  }

  /**
   * Is the underlying community of users hasn't yet any memberships?
   * @return true if no membership has been registered for the community of users. False otherwise.
   */
  public boolean isEmpty() {
    return memberships.isEmpty();
  }

  /**
   * Gets the membership to the community of users with the specified unique identifier.
   * @param membershipId the unique identifier of a membership to the community of users.
   * @return either a {@link CommunityMembership} instance representing the asked membership or
   * nothing if no such membership to the community of users exists.
   */
  public Optional<CommunityMembership> get(@Nonnull final String membershipId) {
    Objects.requireNonNull(membershipId);
    return Optional.ofNullable(repository.getById(membershipId));
  }

  /**
   * Gets the membership of the specified user to the community of users. If the user isn't member
   * of the community, then nothing is returned. Only the user whose membership is either pending or
   * committed is returned.
   * @param user a user in Silverpeas. If null, nothing is returned.
   * @return either a {@link CommunityMembership} instance representing the membership of the user
   * to the community or nothing if the user isn't (anymore) member of the community.
   */
  public Optional<CommunityMembership> get(@Nullable final User user) {
    return user == null ? Optional.empty() : memberships.getByUser(user);
  }

  /**
   * Gets all pending memberships to the community of users.
   * @param page a page in the table of pending members defining a range of them to get. If null,
   * all is got.
   * @return a paginated list of pending members.
   */
  public SilverpeasList<CommunityMembership> getPending(@Nullable final PaginationPage page) {
    return memberships.getPending(page);
  }

  /**
   * Gets all the committed memberships to the community of users that are within the specified
   * pagination page. Because the members of a community can be huge, only a range of their
   * membership is allowed to be got.
   * @param page a page in the table of memberships defining a range of them to get.
   * @return a paginated list of actual memberships to the community.
   * @implNote a synchronization between the roles of the community space and the table of
   * memberships is performed before getting the memberships.
   */
  public SilverpeasList<CommunityMembership> getInRange(@Nonnull PaginationPage page) {
    Objects.requireNonNull(page);
    synchronize();
    return memberships.getMembers(page);
  }

  /**
   * Gets the history of memberships to the community of users that are within the specified
   * pagination page. All memberships are taken into account, whatever the status of membership.
   * @param page a page in the table of memberships defining a range of them to get.
   * @return a paginated list of memberships to the community of users, whatever the status of
   * membership.
   * @implNote a synchronization between the roles of the community space and the table of
   * memberships is performed before getting the history.
   */
  public SilverpeasList<CommunityMembership> getHistory(@Nonnull PaginationPage page) {
    Objects.requireNonNull(page);
    synchronize();
    return memberships.getAll(page);
  }

  /**
   * <p>
   * Synchronize the table of memberships of the community of users with the users playing a role in
   * the community space. A user is member of a community space if, and only if, he plays a role in
   * it. So, to ensure to keep the table of memberships up-to-date, a synchronization of it with the
   * roles played in the community space is required. The goal is to ensure the state of the table
   * of memberships reflect the users in the different roles of the community space:
   * </p>
   * <ul>
   *   <li>
   *     if a user doesn't play anymore a role in the community space, then his membership to the
   *     community is removed. The date of his removal is the one of the synchronization.
   *   </li>
   *   <li>
   *     if a new user plays a non-inherited role in the community space, then he's added as a
   *     member in the community. The date of his adding is the one of the synchronization.
   *   </li>
   *   <li>
   *     if a user whose membership to the community is pending plays a non-inherited role in the
   *     community space, then his membership is committed. The date of the joining is the one of
   *     the synchronization.
   *   </li>
   * </ul>
   * <p>
   *   The synchronization is only performed if the last one has been done more than one hour.
   * </p>
   */
  private void synchronize() {
    // check the last datetime of the synchronization
    synchronized (lastSynchronizations) {
      OffsetDateTime now = OffsetDateTime.now();
      OffsetDateTime lastSynchronization = lastSynchronizations.get(community.getId());
      long duration = settings.getLong("community.memberships.synchronization");
      if (lastSynchronization != null && now.minusMinutes(duration).isBefore(lastSynchronization)) {
        return;
      }
      lastSynchronizations.put(community.getId(), now);
    }

    // fetch all the committed and pending memberships of the community
    List<CommunityMembership> actualMemberships = this.memberships.getAllMembers();

    // fetch all the users playing at least one role in the community space
    Set<String> usersPlayingRole = community.streamOfNonInheritedSpaceProfiles()
        .flatMap(p -> p.getAllUsers().stream())
        .collect(Collectors.toSet());

    Transaction.performInOne(() -> {
      // first we are looking for users playing a role in the community space but not yet registered
      // as member in order to register them
      usersPlayingRole.stream()
          .filter(u -> actualMemberships.stream()
              .filter(m -> m.getStatus().isMember())
              .noneMatch(m -> m.getUser().getId().equals(u)))
          .map(User::getById)
          .map(u -> CommunityMembership.asMember(u, community))
          .forEach(CommunityMembership::save);

      // then we are looking for members not playing anymore a role in the community space in
      // order to update their membership status
      actualMemberships.stream()
          .filter(m -> m.getStatus().isMember())
          .filter(m -> !usersPlayingRole.contains(m.getUser().getId()))
          .forEach(m -> {
            m.setStatus(MembershipStatus.REMOVED);
            repository.save(m);
          });

      // finally we are looking for pending members who were added explicitly in a role of the
      // community space by an administrator to update accordingly their membership status
      actualMemberships.stream()
          .filter(m -> m.getStatus().isPending())
          .filter(m -> usersPlayingRole.contains(m.getUser().getId()))
          .forEach(m -> {
            m.setStatus(MembershipStatus.COMMITTED);
            repository.save(m);
          });

      return null;
    });
  }
}
