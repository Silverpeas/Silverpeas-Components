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
package org.silverpeas.components.community.model;

import org.silverpeas.components.community.AlreadyMemberException;
import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.component.model.InheritableSpaceRoles;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.Mutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * The community of users for a collaborative space. Users in the community are said to be members
 * of this community, and hence of the space for which the community has been spawned. The space is
 * then said a community space. A community is always managed by a Community application instance;
 * it is like the space for which the community has been created delegates its management to that
 * application instance. The difference between a community space with a collaborative space is in
 * the former the users ask to join the community and then gain access rights to the space. Another
 * difference is the community space is visible to all users of the platform without requiring this
 * space to be public; they have just a view of it and of its presentation page.
 */
@Entity
@Table(name = "SC_Community")
@NamedQuery(
    name = "CommunityByComponentInstanceId",
    query = "select c from CommunityOfUsers c where c.componentInstanceId = :componentInstanceId")
@NamedQuery(
    name = "CommunityBySpaceId",
    query = "select c from CommunityOfUsers c where c.spaceId = :spaceId")
public class CommunityOfUsers
    extends BasicJpaEntity<CommunityOfUsers, UuidIdentifier> {
  private static final long serialVersionUID = -4908726669864467915L;

  @Column(name = "spaceId", nullable = false)
  private String spaceId;

  @Column(name = "instanceId", nullable = false)
  private String componentInstanceId;

  @Transient
  private transient CommunityMembershipsProvider provider;

  /**
   * Constructs a new empty Community instance.
   */
  protected CommunityOfUsers() {
    // this constructor is for the persistence engine.
  }

  /**
   * Constructs a new Community instance for the specified resource.
   * @param componentInstanceId the unique identifier of a component instance managing the
   * community.
   * @param spaceId the unique identifier of a resource in Silverpeas for which the community is
   * constructed.
   */
  public CommunityOfUsers(final String componentInstanceId, final String spaceId) {
    this.componentInstanceId = componentInstanceId;
    this.spaceId = spaceId;
  }

  /**
   * Gets the community of users managed by the specified component instance. If the component
   * instance doesn't exist then nothing is returned.
   * @param instanceId the unique identifier of a Community application instance.
   * @return maybe a community instance or nothing if the component instance doesn't exist.
   */
  public static Optional<CommunityOfUsers> getByComponentInstanceId(final String instanceId) {
    CommunityOfUsersRepository repository = CommunityOfUsersRepository.get();
    return repository.getByComponentInstanceId(instanceId);
  }

  /**
   * Gets the community of users of the specified collaborative space. Nothing is returned if either
   * the space doesn't exist or it isn't a community space.
   * @param spaceId the unique identifier of a space in Silverpeas.
   * @return maybe a community instance or nothing if there is no community for the specified space.
   */
  public static Optional<CommunityOfUsers> getBySpaceId(final String spaceId) {
    CommunityOfUsersRepository repository = CommunityOfUsersRepository.get();
    return repository.getBySpaceId(spaceId);
  }

  /**
   * Gets the rich content of the presentation of the community space. A presentation of the parent
   * space can be defined in order for users out of the community to have a glance of what the
   * community space is about. The goal is to give them enough information for deciding to join the
   * community of the space.
   * @return a {@link WysiwygContent} instance.
   */
  public WysiwygContent getSpacePresentationContent() {
    return WysiwygController.get(getComponentInstanceId(), "SpaceFacade", null);
  }

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Gets the unique identifier of the community space, that is to say the collaborative space for
   * which this community of users is.
   * @return the unique identifier of a space in Silverpeas.
   */
  public String getSpaceId() {
    return spaceId;
  }

  /**
   * Checks the given user is a member of this community of users by verifying he's playing a role
   * in the community space.
   * @param user {@link User} instance.
   * @return true whether the user is a member of this community, false otherwise.
   * @implSpec a user is member of the community if and only if he plays a non-inherited role in the
   * parent space (the community space) among the following ones: {@link SilverpeasRole#ADMIN},
   * {@link SilverpeasRole#PUBLISHER}, {@link SilverpeasRole#WRITER} and
   * {@link SilverpeasRole#READER}.
   */
  public boolean isMember(final User user) {
    return streamOfNonInheritedSpaceProfiles()
        .flatMap(p -> p.getAllUsers().stream())
        .anyMatch(i -> i.equals(user.getId()));
  }

  /**
   * Gets the roles the given user has on the given community.
   * @param user {@link User} instance.
   * @return an unmodifiable set of {@link SilverpeasRole}.
   */
  public Set<SilverpeasRole> getUserRoles(final User user) {
    return Set.copyOf(ComponentAccessControl.get()
        .getUserRoles(user.getId(), getComponentInstanceId(), AccessControlContext.init()));
  }

  /**
   * Adds the specified user as a member pending his membership to this community to be committed.
   * @param user the user to add as a pending member.
   * @return the pending membership of the user.
   */
  public CommunityMembership addAsAPendingMember(final User user) {
    checkAlreadyMember(user);
    getMembershipsProvider().get(user)
        .map(CommunityMembership::getStatus)
        .filter(s -> s == MembershipStatus.PENDING)
        .ifPresent(s -> {
          throw new AlreadyMemberException(
              "The membership of the user " + user.getId() + " is already pending!");
        });
    return Transaction.performInOne(() -> {
      CommunityMembership membership = CommunityMembership.asMember(user, this);
      membership.setStatus(MembershipStatus.PENDING);
      membership.save();
      return membership;
    });
  }

  /**
   * Adds the specified user as a member of this community of users and with the specified role. In
   * the case a membership application is pending for the user, his membership is then validated,
   * and he's added in the specifying role in the community space.
   * @param user the user to add as a committed member.
   * @param role the role the user should play in the community.
   * @return the committed membership of the user.
   */
  public CommunityMembership addAsMember(final User user, final SilverpeasRole role) {
    if (!InheritableSpaceRoles.isASpaceRole(role)) {
      throw new IllegalArgumentException("The role " + role.getName() + " isn't a role of a space");
    }
    checkAlreadyMember(user);
    User requester = User.getCurrentRequester();
    return Transaction.performInOne(() -> {
      Mutable<CommunityMembership> membership = Mutable.empty();
      Administration administration = Administration.get();
      Optional.of(getSpaceId())
          .map(s -> handleException(administration::getSpaceInstById, s))
          .map(s -> Optional.ofNullable(s.getSpaceProfileInst(role.getName())).orElseGet(() -> {
            SpaceProfileInst p = new SpaceProfileInst();
            p.setName(role.getName());
            p.setSpaceFatherId(getSpaceId());
            p.setInherited(false);
            handleException(administration::addSpaceProfileInst, p, requester.getId());
            return p;
          }))
          .ifPresent(p -> {
            p.getAllUsers().add(user.getId());
            handleException(administration::updateSpaceProfileInst, p, requester.getId());
            var mayBeMember = getMembershipsProvider().get(user);
            CommunityMembership
                m = mayBeMember
                .filter(mb -> mb.getStatus().isPending())
                .orElseGet(() -> CommunityMembership.asMember(user, this));
            m.setStatus(MembershipStatus.COMMITTED);
            m.save();
            membership.set(m);
          });

      return membership.orElseThrow(
          () -> new SilverpeasRuntimeException("Unexpected error: No such space " + getSpaceId()));
    });
  }

  /**
   * Refuses the membership application of the specified user to this community of users. For doing,
   * the user must have a membership application pending for validation, otherwise an
   * {@link IllegalStateException} is thrown.
   * @param user the user for whom his membership application is refused.
   * @return the refused membership of the user to this community of users.
   */
  public CommunityMembership refuseMembership(final User user) {
    CommunityMembership membership = getMembershipsProvider().get(user)
        .filter(m -> m.getStatus().isPending())
        .orElseThrow(() -> new IllegalStateException(
            "The user " + user.getId() + " has no pending membership to the community " +
                getId()));
    return Transaction.performInOne(() -> {
      membership.setStatus(MembershipStatus.REFUSED);
      membership.save();
      return membership;
    });
  }

  /**
   * Removes the membership of the specified user to this community of users. The membership of the
   * user isn't actually deleted; only his membership status is updated to
   * {@link MembershipStatus#REMOVED}. Once removed, a user isn't anymore member of the community
   * and hence his membership status cannot be updated. In others words, when such a user is added
   * again among the members of the community, a new entry in the memberships table of the community
   * is created for this user; he has a new membership data.
   * @param user the user to remove from this community.
   * @return the removed membership with the status updated or null if the given user isn't member
   * of this community.
   * @implSpec the user will be removed from all the roles of the parent space (the community space)
   * and then his membership status is updated to {@link MembershipStatus#REMOVED}.
   */
  public CommunityMembership removeMembership(final User user) {
    return Transaction.performInOne(() -> {
      final Administration administration = Administration.get();
      Mutable<CommunityMembership> membership = Mutable.empty();
      streamOfNonInheritedSpaceProfiles()
          .filter(p -> p.getAllUsers().contains(user.getId()))
          .forEach(p -> {
            p.getAllUsers().remove(user.getId());
            handleException(administration::updateSpaceProfileInst, p,
                User.getCurrentRequester().getId());
            getMembershipsProvider().get(user).ifPresent(m -> {
              m.setStatus(MembershipStatus.REMOVED);
              CommunityMembershipRepository.get().save(m);
              membership.set(m);
            });
          });
      return membership.orElse(null);
    });
  }

  /**
   * Gets all the memberships to this community of users.
   * @return a provider of memberships to this community with which fine requests can be invoked to
   * get some parts of the memberships of the users to this community.
   */
  public CommunityMembershipsProvider getMembershipsProvider() {
    if (provider == null) {
      provider = CommunityMembershipsProvider.getProvider(this);
    }
    return provider;
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  protected Stream<SpaceProfileInst> streamOfNonInheritedSpaceProfiles() {
    final OrganizationController controller = OrganizationController.get();
    return Optional.of(getSpaceId())
        .map(controller::getSpaceInstById)
        .stream()
        .flatMap(s -> s.getAllSpaceProfilesInst().stream())
        .filter(not(SpaceProfileInst::isManager).and(not(SpaceProfileInst::isInherited)));
  }

  private void synchronizeRemovingIfAny(final User user) {
    Transaction.performInOne(() -> {
      getMembershipsProvider().get(user)
          .filter(m -> m.getStatus().isMember())
          .ifPresent(m -> {
            m.setStatus(MembershipStatus.REMOVED);
            CommunityMembershipRepository.get().save(m);
          });
      return null;
    });
  }

  private void checkAlreadyMember(final User user) {
    if (isMember(user)) {
      throw new AlreadyMemberException(
          "User " + user.getId() + " is already a member of the community " + getId());
    } else {
      synchronizeRemovingIfAny(user);
    }
  }

  private interface FunctionWithException<T, R> {

    R apply(T t) throws AdminException;
  }

  private interface BiFunctionWithException<T, U, R> {

    R apply(T t, U u) throws AdminException;
  }

  private <T, R> R handleException(FunctionWithException<T, R> function, T arg) {
    try {
      return function.apply(arg);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage());
    }
  }

  @SuppressWarnings("UnusedReturnValue")
  private <T, U, R> R handleException(BiFunctionWithException<T, U, R> function, T arg1, U arg2) {
    try {
      return function.apply(arg1, arg2);
    } catch (Exception e) {
      throw new SilverpeasRuntimeException(e.getMessage());
    }
  }
}