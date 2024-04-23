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
package org.silverpeas.components.community.model;

import org.silverpeas.components.community.AlreadyMemberException;
import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.component.model.InheritableSpaceRoles;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.util.Pair;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.silverpeas.core.admin.space.SpaceHomePageType.STANDARD;
import static org.silverpeas.kernel.util.StringUtil.EMPTY;

/**
 * The community of users for a collaborative space. Users in the community are said to be members
 * of this community, and hence of the space for which the community has been spawned. The space is
 * then said a community space. A community is always managed by a Community application instance;
 * it is like the space for which the community has been created delegates its management to that
 * application instance. The difference between a community space with a collaborative space is in
 * the former the users ask to join the community and then gain access rights to the space. Another
 * difference is the community space is visible to all users of the platform without requiring this
 * space to be public; they have just a view of it and of its presentation page.
 * <p>
 * The actual members of a community of users are all included in a specific group of users that is
 * automatically created when the community is spawned. This group of members is kept up-to-date
 * with the users playing a role in the community space. The group of members allow to get in one
 * shot all the actual members of the community. To have a glance about the memberships to the
 * community, passed, actual and pending, please asks to the {@link CommunityMembershipsProvider}
 * object associated with the community of users.
 * </p>
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
  @NotNull
  private String spaceId;
  @Column(name = "instanceId", nullable = false)
  @NotNull
  private String componentInstanceId;

  Integer groupId;

  private String homePage;

  @Enumerated(EnumType.ORDINAL)
  private SpaceHomePageType homePageType;

  private URL charterURL;

  @Transient
  private transient CommunityMembershipsProvider provider;

  @Transient
  private transient CommunitySpace communitySpace;

  /**
   * Constructs a new empty Community instance.
   */
  protected CommunityOfUsers() {
    // this constructor is for the persistence engine.
  }

  /**
   * Constructs a new Community instance for the specified resource.
   *
   * @param componentInstanceId the unique identifier of a component instance managing the
   * community.
   * @param spaceId the unique identifier of a resource in Silverpeas for which the community is
   * constructed.
   */
  public CommunityOfUsers(final String componentInstanceId, final String spaceId) {
    this.componentInstanceId = componentInstanceId;
    this.spaceId = spaceId;
    this.communitySpace = new CommunitySpace(this);
  }

  /**
   * Gets all the communities of users existing in Silverpeas.
   * @return a list of exiting community of users.
   */
  public static List<CommunityOfUsers> getAll() {
    CommunityOfUsersRepository repository = CommunityOfUsersRepository.get();
    return repository.getAll();
  }

  /**
   * Gets the community of users managed by the specified component instance. If the component
   * instance doesn't exist then nothing is returned.
   *
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
   *
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
   *
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
   *
   * @return the unique identifier of a space in Silverpeas.
   */
  public String getSpaceId() {
    return spaceId;
  }

  /**
   * Checks the given user is a member of this community of users by verifying he's playing a role
   * in the community space.
   *
   * @param user {@link User} instance.
   * @return true whether the user is a member of this community, false otherwise.
   * @implSpec a user is member of the community if and only if he plays a non-inherited role in the
   * parent space (the community space) among the following ones: {@link SilverpeasRole#ADMIN},
   * {@link SilverpeasRole#PUBLISHER}, {@link SilverpeasRole#WRITER} and
   * {@link SilverpeasRole#READER}.
   */
  public boolean isMember(final User user) {
    return getCommunitySpace().getAllUsers().contains(user.getId());
  }

  /**
   * Gets the roles the given user has on the given community.
   *
   * @param user {@link User} instance.
   * @return an unmodifiable set of {@link SilverpeasRole}.
   */
  public Set<SilverpeasRole> getUserRoles(final User user) {
    return Set.copyOf(ComponentAccessControl.get()
        .getUserRoles(user.getId(), getComponentInstanceId(), AccessControlContext.init()));
  }

  /**
   * Gets the home page of this community of users for its members.
   *
   * @return the home page of the community of users for the members.
   */
  public Pair<String, SpaceHomePageType> getHomePage() {
    return Pair.of(ofNullable(homePage).orElse(EMPTY), ofNullable(homePageType).orElse(STANDARD));
  }

  /**
   * Gets the URL at which the charter (or a community guide) is located. The charter has to be
   * validated by a user in order to join the community of users.
   *
   * @return the URL of the charter.
   */
  public URL getCharterURL() {
    return charterURL;
  }

  /**
   * Sets the URL at which the charter (or a community guide) is located. The charter, once set, has
   * to be validated by a user in order to join the community of users.
   *
   * @param charterURL the URL of the charter to set.
   */
  public void setCharterURL(@Nonnull final URL charterURL) {
    Objects.requireNonNull(charterURL);
    this.charterURL = charterURL;
  }

  /**
   * Sets the URL at which the charter (or a community guide) is located. The charter, once set, has
   * to be validated by a user in order to join the community of users.
   *
   * @param charterURL the URL of the charter to set.
   * @throws MalformedURLException if the specified URL is malformed.
   */
  public void setCharterURL(final String charterURL) throws MalformedURLException {
    this.charterURL = new URL(charterURL);
  }

  /**
   * Unsets the charter.
   *
   * @see #setCharterURL(String)
   */
  public void unsetCharterURL() {
    this.charterURL = null;
  }

  /**
   * y Sets the home page of this community of users to render to the members.
   *
   * @param homePage the home page of the community of users.
   * @param homePageType the type of the home page.
   */
  public void setHomePage(final String homePage, SpaceHomePageType homePageType) {
    this.homePage = homePage;
    this.homePageType = homePageType;
  }

  /**
   * Adds the specified user as a member pending his membership to this community to be committed.
   *
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
   *
   * @param user the user to add as a committed member.
   * @param role the role the user should play in the community.
   * @return the committed membership of the user.
   * @throws SilverpeasRuntimeException if an unexpected error occurs while adding the specified
   * user in this community with the given role.
   */
  public CommunityMembership addAsMember(final User user, final SilverpeasRole role) {
    if (!InheritableSpaceRoles.isASpaceRole(role)) {
      throw new IllegalArgumentException("The role " + role.getName() + " isn't a role of a space");
    }
    checkAlreadyMember(user);
    return Transaction.performInOne(() -> {
      getCommunitySpace().addUser(user, role);
      return commitMembership(user);
    });
  }

  /**
   * Refuses the membership application of the specified user to this community of users. For doing,
   * the user must have a membership application pending for validation, otherwise an
   * {@link IllegalStateException} is thrown.
   *
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
   *
   * @param user the user to remove from this community.
   * @return the removed membership with the status updated or null if the given user isn't member
   * of this community.
   * @implSpec the user will be removed from all the roles of the parent space (the community space)
   * and then his membership status is updated to {@link MembershipStatus#REMOVED}.
   */
  public CommunityMembership removeMembership(final User user) {
    return Transaction.performInOne(() -> {
      getCommunitySpace().removeUser(user);
      return getMembershipsProvider().get(user)
          .map(m -> {
            m.delete();
            return m;
          })
          .orElse(null);
    });
  }

  /**
   * Gets all the memberships to this community of users.
   *
   * @return a provider of memberships to this community with which fine requests can be invoked to
   * get some parts of the memberships of the users to this community.
   */
  public CommunityMembershipsProvider getMembershipsProvider() {
    if (provider == null) {
      provider = CommunityMembershipsProvider.getProvider(this);
    }
    return provider;
  }

  /**
   * Saves the modification in this community of users. If the community isn't a persisted one, then
   * an {@link IllegalStateException} exception is thrown.
   */
  public void save() {
    if (!isPersisted()) {
      throw new IllegalStateException("This community isn't a persisted one!");
    }
    Transaction.performInOne(() -> {
      CommunityOfUsersRepository.get().save(this);
      return null;
    });
  }

  /**
   * Deletes this community of users. The memberships to this community should be removed before.
   * Take care this deletion will be definitely. This method should be used only by inner
   * administration tasks.
   */
  public void delete() {
    Transaction.performInOne(() -> {
      var repository = CommunityOfUsersRepository.get();
      repository.delete(this);
      // to ensure the community is removed (and hence its link to the group) before deleting the
      // referenced group
      repository.flush();
      getCommunitySpace().deleteMembersGroup();
      return null;
    });
  }

  /**
   * Gets the group of all the members of this community of users. If this community isn't
   * persisted, null is returned. Otherwise the group of members of this community of users is
   * returned. In the case such a group isn't yet created, then this method will create it before
   * returning it. If no users are currently members in this community, then the returned group of
   * users will be empty.
   *
   * @return the group of users who are all members of this community or null if this community
   * isn't yet persisted.
   * @throws SilverpeasRuntimeException if an error occurs while creating or getting the group of
   * members.
   */
  public GroupDetail getGroupOfMembers() {
    if (!isPersisted()) {
      return null;
    }
    GroupDetail group;
    try {
      if (groupId == null) {
        SpaceInst spaceInst = getCommunitySpace().getSilverpeasSpace();
        group = getCommunitySpace().createMembersGroup(spaceInst);
      } else {
        group = getCommunitySpace().getMembersGroup();
      }
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return group;
  }

  @Override
  public boolean equals(final Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Gets the Silverpeas space mapped with this community of users.
   *
   * @return the Silverpeas space as a community space.
   */
  CommunitySpace getCommunitySpace() {
    if (communitySpace == null) {
      communitySpace = new CommunitySpace(this);
    }
    return communitySpace;
  }

  private CommunityMembership commitMembership(User user) {
    var mayBeMember = getMembershipsProvider().get(user);
    CommunityMembership
        m = mayBeMember
        .filter(mb -> mb.getStatus().isPending())
        .orElseGet(() -> CommunityMembership.asMember(user, this));
    m.setStatus(MembershipStatus.COMMITTED);
    m.save();
    return m;
  }

  private void synchronizeRemovingIfAny(final User user) {
    Transaction.performInOne(() -> {
      getMembershipsProvider().get(user)
          .filter(m -> m.getStatus().isMember())
          .ifPresent(CommunityMembership::delete);
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

  /**
   * A community space. It is a Silverpeas space which represents a community and for which a group
   * of users is maintained for the members of the community. The community of users is ruled by the
   * community application instanciated in this same space. Any users playing a role in the space is
   * considered as a member of the community and each member of a community has to play at least one
   * role in the space. As such, a member of a community should be a user in the members group. It
   * is the responsibility of the {@link CommunityMembershipsProvider}, from which memberships can
   * be got, to ensure users playing a role in the space are members of the community and are in the
   * members group.
   * <p>
   * A community space is responsible to manage both the user profiles for the space related by a
   * community of users, and the members group associated to this space.
   * </p>
   */
  static class CommunitySpace {

    private static final SettingBundle settings = ResourceLocator.getSettingBundle(
        "org.silverpeas.components.community.settings.communitySettings");

    private final Administration administration;
    private final User requester;
    private final CommunityOfUsers community;

    /**
     * Creates a new community space representing the Silverpeas space mapped with the specified
     * community of users.
     *
     * @param community the community of users underlying to this community space.
     */
    public CommunitySpace(CommunityOfUsers community) {
      this.community = community;
      administration = Administration.get();
      requester = User.getCurrentRequester();
    }

    /**
     * Adds the specified user into the this community space with the provided role.
     *
     * @param user the user to add in the community space as member.
     * @param role the role the user will play in the community space.
     * @throws SilverpeasRuntimeException if an unexpected error occurs while adding the user in the
     * community space.
     * @implNote the adding of a user in the community space consists of adding him both into the
     * corresponding user profile of the space (identified by the Silverpeas role) and into the
     * group of users dedicated to the members of the community space. If the group doesn't yet
     * exist, then it is created.
     */
    public void addUser(@NonNull User user, @NonNull SilverpeasRole role) {
      Objects.requireNonNull(user);
      Objects.requireNonNull(role);
      execute(() -> {
        var space = getSilverpeasSpace();
        var profile = getSpaceProfile(role, space);
        addUserInSpaceProfile(user, profile);
        addUserInMembershipGroup(user, space);
      });
    }

    /**
     * Removes the specified user from this community space.
     *
     * @param user the user to remove.
     * @implNote the user is both removed from any user profile of the space and from the user group
     * of all the members of the community. If the user to remove doesn't play any role in the
     * community space or if the members group isn't yet created, then no remove operation relative
     * to these resources is done.
     */
    public void removeUser(@NonNull User user) {
      Objects.requireNonNull(user);
      execute(() -> {
        var space = getSilverpeasSpace();
        removeUserFromAllSpaceProfiles(user, space);
        removeUserFromMembershipGroup(user);
      });
    }

    /**
     * Deletes definitely the members group associated with this community space. This method is to
     * be invoked in community deletion. If no members group has been created before the deletion of
     * a space, then nothing is done.
     */
    private void deleteMembersGroup() {
      execute(() -> {
        var group = getMembersGroup();
        if (group != null) {
          administration.deleteGroupById(group.getId(), true);
        }
      });
    }

    /**
     * Gets the unique identifier of all the users playing a role in this space. Those users should
     * be a member of the corresponding community of users. Nevertheless, be aware a lag can exist
     * between the users in the space and their memberships in the community of users.
     *
     * @return a set of user identifiers. The set is empty is there is no yet users playing a role
     * in the community space.
     */
    public Set<String> getAllUsers() {
      Set<String> users = new HashSet<>();
      execute(() -> {
        SpaceInst space = getSilverpeasSpace();
        streamOnNonInheritedSpaceProfiles(space)
            .flatMap(p -> p.getAllUsers().stream())
            .forEach(users::add);
      });
      // now we ensure the members group is up-to-date with the users playing a role in the
      // community space
      var spaceSynchro = community.getCommunitySpace().getSynchronizationTask();
      spaceSynchro.synchronizeMembersGroup(users);
      return users;
    }

    /**
     * Gets all the profiles the specified user has in the community space.
     *
     * @param userId the unique identifier of a user.
     * @return a set with all the roles the user play in community space.
     */
    public Set<SpaceProfileInst> getAllSpaceProfilesOfUser(String userId) {
      Set<SpaceProfileInst> profiles = new HashSet<>();
      execute(() -> {
        SpaceInst space = getSilverpeasSpace();
        streamOnNonInheritedSpaceProfiles(space)
            .filter(p -> p.getAllUsers().contains(userId))
            .forEach(profiles::add);
      });
      return profiles;
    }

    /**
     * Gets a synchronization task related to ensure the community space data are up-to-date with
     * the membership of a user. This method is for the {@link CommunityMembershipsProvider}.
     *
     * @return a synchronization task for this community space.
     */
    private SynchronizationTask getSynchronizationTask() {
      return new SynchronizationTask();
    }

    private SpaceProfileInst getSpaceProfile(SilverpeasRole role, SpaceInst space)
        throws AdminException {
      var profile = space.getSpaceProfileInst(role.getName());
      if (profile == null) {
        profile = new SpaceProfileInst();
        profile.setName(role.getName());
        profile.setSpaceFatherId(space.getId());
        profile.setInherited(false);
        administration.addSpaceProfileInst(profile, requester.getId());
        space.addSpaceProfileInst(profile);
      }
      return profile;
    }

    private void addUserInSpaceProfile(User user, SpaceProfileInst profile) throws AdminException {
      if (!isUserHasProfile(user, profile)) {
        profile.addUser(user.getId());
        administration.updateSpaceProfileInst(profile, requester.getId());
      }
    }

    private void addUserInMembershipGroup(User user, SpaceInst space) throws AdminException {
      var group = getMembersGroup();
      boolean newGroup = group == null;
      if (newGroup) {
        group = createMembersGroup(space);
      }
      if (newGroup || !isUserInGroup(user, group)) {
        administration.addUserInGroup(user.getId(), group.getId());
      }
    }

    /**
     * Creates the group of the members for the specified community space.
     *
     * @param space an existing community space in Silverpeas
     * @return the created group of members
     * @throws AdminException if an error occurs while creating the group of members.
     */
    private GroupDetail createMembersGroup(SpaceInst space) throws AdminException {
      GroupDetail group;
      group = new GroupDetail();
      String groupName = settings.getString("community.group.symbol", "") + " " +
          space.getName();
      group.setName(groupName.trim());
      community.groupId = Integer.parseInt(administration.addGroup(group, true));
      community.save();
      var profile = getSpaceProfile(SilverpeasRole.READER, space);
      profile.addGroup(group.getId());
      administration.updateSpaceProfileInst(profile, requester.getId());
      return group;
    }

    private void removeUserFromAllSpaceProfiles(User user, SpaceInst space) {
      streamOnNonInheritedSpaceProfiles(space)
          .filter(p -> isUserHasProfile(user, p))
          .forEach(p -> execute(() -> {
            p.removeUser(user.getId());
            administration.updateSpaceProfileInst(p, requester.getId());
          }));
    }

    private Stream<SpaceProfileInst> streamOnNonInheritedSpaceProfiles(SpaceInst space) {
      return space.getAllSpaceProfilesInst().stream()
          .filter(not(SpaceProfileInst::isManager).and(not(SpaceProfileInst::isInherited)));
    }

    private void removeUserFromMembershipGroup(User user) throws AdminException {
      var group = getMembersGroup();
      if (group != null && isUserInGroup(user, group)) {
        administration.removeUserFromGroup(user.getId(), group.getId());
      }
    }

    private GroupDetail getMembersGroup() throws AdminException {
      if (community.groupId == null) {
        return null;
      }
      GroupDetail group = administration.getGroup(String.valueOf(community.groupId));

      // check the symbol for group of members didn't change
      String symbol = settings.getString("community.group.symbol", "") + " ";
      SpaceInst space = getSilverpeasSpace();
      if ((symbol.isBlank() && !group.getName().equals(space.getName())) ||
          (!symbol.isBlank() && !group.getName().startsWith(symbol))) {
        group.setName((symbol + space.getName()).trim());
        administration.updateGroup(group, true);
      }
      return group;
    }

    private SpaceInst getSilverpeasSpace() throws AdminException {
      return administration.getSpaceInstById(community.spaceId);
    }

    private boolean isUserInGroup(User user, GroupDetail group) {
      return List.of(group.getUserIds()).contains(user.getId());
    }

    public boolean isUserHasProfile(User user, SpaceProfileInst profile) {
      return profile.getAllUsers().contains(user.getId());
    }

    private void execute(AdminTask task) {
      try {
        task.perform();
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException("Unexpected error: " + e.getMessage());
      }
    }

    @FunctionalInterface
    private interface AdminTask {

      void perform() throws AdminException;
    }

    /**
     * A synchronization task ensures the members group associated with the community space is
     * up-to-date with all the users playing at least one role in this same community space.
     */
    class SynchronizationTask {

      /**
       * Synchronizes the memberships of the underlying community space so that the members group of
       * this space is up-to-date with the users playing a role in it. If a user playing a role in
       * the space isn't yet in the members group, then he's added in this group. If the a user in
       * the group isn't playing any role in the space, then he's removed from the members group.
       *
       * @param usersPlayingARole a set with the unique identifiers of the users who play a role in
       * the community space.
       */
      void synchronizeMembersGroup(final Set<String> usersPlayingARole) {
        execute(() -> {
          // get both the users playing currently a role in the community space and the users of
          // the members group associated with the community space
          if (usersPlayingARole.isEmpty()) {
            // if there is no users, no need of synchronization
            return;
          }
          var space = CommunitySpace.this.getSilverpeasSpace();
          var group = getMembersGroup();
          if (group == null) {
            group = createMembersGroup(space);
          }
          var members = Set.of(group.getUserIds());

          // ensure the users playing a role in the community space are also in the members group
          for (String userId : usersPlayingARole) {
            if (!members.contains(userId)) {
              administration.addUserInGroup(userId, group.getId());
            }
          }

          // ensure all users in the members group are playing a role in the community space
          for (String memberId : members) {
            if (!usersPlayingARole.contains(memberId)) {
              administration.removeUserFromGroup(memberId, group.getId());
            }
          }
        });
      }
    }
  }
}