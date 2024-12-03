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
package org.silverpeas.components.community;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.components.community.model.MembershipStatus;
import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.components.community.repository.CommunityOfUsersRepository;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation;
import org.silverpeas.core.persistence.datasource.model.jpa.JpaUpdateOperation;
import org.silverpeas.core.test.unit.EntityIdSetter;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBeans;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.util.Pair;

import javax.persistence.EntityManager;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.exparity.hamcrest.date.OffsetDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests about the business methods of a {@link CommunityOfUsers} object.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans({Transaction.class,
    JpaPersistOperation.class,
    JpaUpdateOperation.class})
class CommunityOfUsersTest {

  private static final String USER_ID = "0";
  private static final String SPACE_ID = "WA42";
  private static final String INSTANCE_ID = "community42";
  private static final String[] SPACE_MANAGERS = new String[]{"0"};
  private static final String[] ADMINS = new String[]{"1"};
  private static final String[] PUBLISHERS = new String[]{"2", "3"};
  private static final String[] WRITERS = new String[]{"4", "5"};
  private static final String[] READERS = new String[]{"6"};
  private static final String[] INHERITED_MANAGERS = new String[]{"42"};
  private static final String NO_MEMBER = "666";

  @TestManagedMock
  EntityManagerProvider entityManagerProvider;
  @TestManagedMock
  CommunityOfUsersRepository communityRepository;
  @TestManagedMock
  CommunityMembershipRepository membershipRepository;

  private final Map<String, List<SpaceProfileInst>> profilesPerSpace = new HashMap<>();
  private final Set<String> members = new HashSet<>();

  private boolean membershipIsPending = false;


  @BeforeEach
  public void fillMembers() {
    members.addAll(List.of(ADMINS));
    members.addAll(List.of(PUBLISHERS));
    members.addAll(List.of(WRITERS));
    members.addAll(List.of(READERS));
  }

  @BeforeEach
  public void mockRequiredResources(
      @TestManagedMock Administration administration,
      @TestManagedMock UserProvider userProvider) throws AdminException {
    mockUsersProviding(userProvider);

    // for the current requester. Requires the user providing to be mocked
    SessionCacheAccessor sessionCacheAccessor = CacheAccessorProvider.getSessionCacheAccessor();
    sessionCacheAccessor.newSessionCache(User.getById(USER_ID));

    mockExpectedAdministrationBehaviour(administration);

    mockRepositoriesBehaviour();
  }

  @Test
  @DisplayName("Getting a community of users managed by a Community component instance")
  void getCommunityOfUsers() {
    String instanceId = INSTANCE_ID;
    Optional<CommunityOfUsers> optCommunity = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();
    assertThat(community.getSpaceId(), is(SPACE_ID));
    assertThat(community.getComponentInstanceId(), is(instanceId));
    assertThat(community.getHomePage(), is(Pair.of("kmelia42", SpaceHomePageType.COMPONENT_INST)));
    assertThat(community.getCharterURL().toString(), is("https://www.silverpeas.org"));

    verify(communityRepository).getByComponentInstanceId(instanceId);
  }

  @Test
  @DisplayName("Getting a community of users of a given collaborative space")
  void getCommunityOfUsersOfSpace() {
    String spaceId = SPACE_ID;
    Optional<CommunityOfUsers> optCommunity = CommunityOfUsers.getBySpaceId(spaceId);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();
    assertThat(community.getSpaceId(), is(spaceId));
    assertThat(community.getComponentInstanceId(), is(INSTANCE_ID));
    assertThat(community.getHomePage(), is(Pair.of("kmelia42", SpaceHomePageType.COMPONENT_INST)));
    assertThat(community.getCharterURL().toString(), is("https://www.silverpeas.org"));

    verify(communityRepository).getBySpaceId(spaceId);
  }

  @Test
  @DisplayName(
      "A user is member of a community whether he plays a non-inherited role and other than" +
          "a space manager in this community.")
  void checkAUserIsAMemberOfTheCommunity() {
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();
    assertThat(community.isMember(User.getById(SPACE_MANAGERS[0])), is(false));
    assertThat(community.isMember(User.getById(INHERITED_MANAGERS[0])), is(false));
    assertThat(community.isMember(User.getById(ADMINS[0])), is(true));
    assertThat(community.isMember(User.getById(PUBLISHERS[0])), is(true));
    assertThat(community.isMember(User.getById(WRITERS[0])), is(true));
    assertThat(community.isMember(User.getById(READERS[0])), is(true));
  }

  @Test
  @DisplayName("A user added as a member of a community with a given role should be then a member" +
      " of this community")
  void addAUserAsAMemberOfACommunity() {
    User user = User.getById("42");
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    CommunityMembership membership = community.addAsMember(user, SilverpeasRole.READER);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    assertThat(membership, notNullValue());
    assertThat(membership.getUser().getId(), is(user.getId()));
    assertThat(membership.getJoiningDate(), within(1, ChronoUnit.SECONDS, now));
    assertThat(membership.getStatus().isMember(), is(true));
    assertThat(community.isMember(user), is(true));
    verify(membershipRepository).save(membership);
  }

  @Test
  @DisplayName("The acceptation of a pending membership of a user should add this user as a " +
      "member of the community with the given role")
  void acceptPendingMembershipOfUser() {
    User user = User.getById("42");
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    setPendingMembership(user);

    CommunityMembership membership = community.addAsMember(user, SilverpeasRole.READER);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    assertThat(membership, notNullValue());
    assertThat(membership.getUser().getId(), is(user.getId()));
    assertThat(membership.getJoiningDate(), within(1, ChronoUnit.SECONDS, now));
    assertThat(membership.getStatus().isMember(), is(true));
    assertThat(community.isMember(user), is(true));
    verify(membershipRepository).save(membership);
  }

  @Test
  @DisplayName("A user added as a member of a community with a role not supported by a space " +
      "should throw an exception")
  void addAUserAsAMemberOfACommunityWithAnInvalidRole() {
    User user = User.getById("42");
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    assertThrows(IllegalArgumentException.class,
        () -> community.addAsMember(user, SilverpeasRole.USER));
  }

  @Test
  @DisplayName("A user added as a member of a community in which he's yet a member should throw " +
      "an exception")
  void addAgainAUserAsAMemberOfACommunityWhateverTheValidRole() {
    User user = User.getById(PUBLISHERS[0]);
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    assertThrows(AlreadyMemberException.class,
        () -> community.addAsMember(user, SilverpeasRole.READER));

    assertThrows(AlreadyMemberException.class,
        () -> community.addAsMember(user, SilverpeasRole.WRITER));
  }

  @Test
  @DisplayName(
      "A user added as a pending member of a community should to register his membership with a " +
          " status 'pending'")
  void addAUserAsAPendingMemberOfACommunity() {
    User user = User.getById("42");
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();
    CommunityMembership membership = community.addAsAPendingMember(user);

    assertThat(membership, notNullValue());
    assertThat(membership.getUser().getId(), is(user.getId()));
    assertThat(membership.getJoiningDate(), nullValue());
    assertThat(membership.getStatus().isPending(), is(true));
    assertThat(community.isMember(user), is(false));
    verify(membershipRepository).save(membership);
  }

  @Test
  @DisplayName(
      "A user added as a pending member of a community for which his membership has been already " +
          "registered should throw an exception")
  void addAgainAUserAsAPendingMemberOfACommunity() {
    User user = User.getById("42");
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    // when getting the membership of the user, ensure the status of his membership is pending
    setPendingMembership(user);

    assertThrows(AlreadyMemberException.class,
        () -> community.addAsAPendingMember(user));
  }

  @Test
  @DisplayName(
      "A user added as a pending member of a community for which he's already a member should " +
          "throw an exception")
  void addAnAlreadyMemberAsAPendingMemberOfACommunity() {
    User user = User.getById(PUBLISHERS[0]);
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));
    CommunityOfUsers community = optCommunity.get();

    assertThrows(AlreadyMemberException.class,
        () -> community.addAsAPendingMember(user));
  }

  @Test
  @DisplayName("The retirement of a user from a community removes him from any role in the space " +
      "and updates his membership status to REMOVED")
  void removeAMemberFromACommunityUpdateItsStatusToRemoved() {
    User user = User.getById(PUBLISHERS[0]);
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();
    CommunityMembership membership = community.removeMembership(user);
    assertThat(membership, notNullValue());
    assertThat(membership.getStatus(), is(MembershipStatus.REMOVED));

    assertThat(community.isMember(user), is(false));
    verify(membershipRepository).getMembershipsTable(community);
    verify(membershipRepository).save(membership);
  }

  @Test
  @DisplayName("The retirement of a non member from a community will does nothing")
  void removeANonMemberFromACommunityDoesNothing() {
    User user = User.getById(NO_MEMBER);
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();
    CommunityMembership membership = community.removeMembership(user);
    assertThat(membership, nullValue());

    assertThat(community.isMember(user), is(false));
    //noinspection ConstantConditions
    verify(membershipRepository, never()).save(membership);
  }

  @Test
  @DisplayName("Refuse a pending membership should update its status to REFUSED")
  void refuseAPendingMember() {
    User user = User.getById("42");
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    // when getting the membership of the user, ensure the status of his membership is pending
    setPendingMembership(user);

    CommunityMembership membership = community.refuseMembership(user);
    assertThat(membership, notNullValue());
    assertThat(membership.getUser(), is(user));
    assertThat(membership.getStatus().isRefused(), is(true));
    assertThat(membership.getJoiningDate(), nullValue());
    verify(membershipRepository).save(membership);
  }

  @Test
  @DisplayName(
      "Refuse the membership application of a user who has asked no such membership should throw" +
          " an exception")
  void refuseAUserWithNoMembershipApplication() {
    User user = User.getById(NO_MEMBER);
    Optional<CommunityOfUsers> optCommunity =
        CommunityOfUsers.getByComponentInstanceId(INSTANCE_ID);
    assertThat(optCommunity.isPresent(), is(true));

    CommunityOfUsers community = optCommunity.get();

    assertThrows(IllegalStateException.class,
        () -> community.refuseMembership(user));
  }

  private void setPendingMembership(final User user) {
    members.add(user.getId());
    membershipIsPending = true;
  }

  private static void initProfiles(final String spaceId, final List<SpaceProfileInst> profiles) {
    SpaceProfileInst m = new SpaceProfileInst();
    m.setId("0");
    m.setSpaceFatherId(spaceId);
    m.setName(SilverpeasRole.MANAGER.getName());
    m.setInherited(false);
    m.setUsers(List.of(SPACE_MANAGERS));
    profiles.add(m);

    SpaceProfileInst a = new SpaceProfileInst();
    a.setId("1");
    a.setSpaceFatherId(spaceId);
    a.setName(SilverpeasRole.ADMIN.getName());
    a.setInherited(false);
    a.setUsers(List.of(ADMINS));
    profiles.add(a);

    SpaceProfileInst p = new SpaceProfileInst();
    p.setId("2");
    p.setSpaceFatherId(spaceId);
    p.setName(SilverpeasRole.PUBLISHER.getName());
    p.setInherited(false);
    p.setUsers(List.of(PUBLISHERS));
    profiles.add(p);

    SpaceProfileInst w = new SpaceProfileInst();
    w.setId("3");
    w.setSpaceFatherId(spaceId);
    w.setName(SilverpeasRole.WRITER.getName());
    w.setInherited(false);
    w.setUsers(List.of(WRITERS));
    profiles.add(w);

    SpaceProfileInst r = new SpaceProfileInst();
    r.setId("4");
    r.setSpaceFatherId(spaceId);
    r.setName(SilverpeasRole.READER.getName());
    r.setInherited(false);
    r.setUsers(List.of(READERS));
    profiles.add(r);

    SpaceProfileInst inh = new SpaceProfileInst();
    inh.setId("5");
    inh.setSpaceFatherId(spaceId);
    inh.setName(SilverpeasRole.PUBLISHER.getName());
    inh.setInherited(true);
    inh.setUsers(List.of(INHERITED_MANAGERS));
    profiles.add(inh);
  }

  private void mockRepositoriesBehaviour() {
    // mock the behaviour of the repositories in the community app
    EntityIdSetter idSetter = new EntityIdSetter(UuidIdentifier.class);
    String communityId = new UuidIdentifier().generateNewId().asString();

    EntityManager entityManager = mock(EntityManager.class);
    when(entityManagerProvider.getEntityManager()).thenReturn(entityManager);
    when(entityManager.find(any(), any())).thenAnswer(i -> {
      CommunityOfUsers community = new CommunityOfUsers(INSTANCE_ID, SPACE_ID);
      community.setHomePage("kmelia42", SpaceHomePageType.COMPONENT_INST);
      community.setCharterURL("https://www.silverpeas.org");
      idSetter.setIdTo(community, communityId);
      return community;
    });

    when(communityRepository.getByComponentInstanceId(anyString())).thenAnswer(i -> {
      String instanceId = i.getArgument(0);
      CommunityOfUsers community = new CommunityOfUsers(instanceId, SPACE_ID);
      community.setHomePage("kmelia42", SpaceHomePageType.COMPONENT_INST);
      community.setCharterURL("https://www.silverpeas.org");
      idSetter.setIdTo(community, communityId);
      return Optional.of(community);
    });
    when(communityRepository.getBySpaceId(anyString())).thenAnswer(i -> {
      String spaceId = i.getArgument(0);
      CommunityOfUsers community = new CommunityOfUsers(INSTANCE_ID, spaceId);
      community.setHomePage("kmelia42", SpaceHomePageType.COMPONENT_INST);
      community.setCharterURL("https://www.silverpeas.org");
      idSetter.setIdTo(community, communityId);
      return Optional.of(community);
    });
    when(communityRepository.save(any(CommunityOfUsers.class))).thenAnswer(i -> i.getArgument(0));
    when(membershipRepository.save(any(CommunityMembership.class))).thenAnswer(i -> {
      CommunityMembership membership = i.getArgument(0);
      members.add(membership.getUser().getId());
      return membership;
    });

    when(membershipRepository.getMembershipsTable(any(CommunityOfUsers.class))).thenAnswer(i -> {
      CommunityOfUsers community = i.getArgument(0);
      CommunityMembershipRepository.CommunityMembershipsTable memberships =
          mock(CommunityMembershipRepository.CommunityMembershipsTable.class);
      when(memberships.getByUser(any(User.class))).thenAnswer(j -> {
        User user = j.getArgument(0);
        if (!members.contains(user.getId())) {
          //noinspection ConstantConditions
          return Optional.ofNullable(null);
        }
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandles.Lookup
            privateLookup = MethodHandles.privateLookupIn(CommunityMembership.class, lookup);
        MethodType methodType =
            MethodType.methodType(CommunityMembership.class, User.class, CommunityOfUsers.class);
        MethodHandle asMember =
            privateLookup.findStatic(CommunityMembership.class, "asMember", methodType);
        CommunityMembership membership = (CommunityMembership) asMember.invoke(user, community);
        if (membershipIsPending) {
          MethodHandle statusSetter =
              privateLookup.findSetter(CommunityMembership.class, "status",
                  MembershipStatus.class);
          statusSetter.invoke(membership, MembershipStatus.PENDING);
        }
        return Optional.of(membership);
      });
      return memberships;
    });

    // for persistence operations
    OperationContext.fromUser(USER_ID);
  }

  private void mockExpectedAdministrationBehaviour(Administration administration) throws AdminException {
    // get a mocked space instance with all the roles initialized with a set of users
    when(administration.getSpaceInstById(anyString())).thenAnswer(i -> {
      String spaceId = i.getArgument(0);
      List<SpaceProfileInst> profiles =
          profilesPerSpace.computeIfAbsent(spaceId, s -> new ArrayList<>());
      SpaceInst space = mock(SpaceInst.class);
      when(space.getId()).thenReturn(spaceId);
      when(space.getName()).thenReturn("My community");
      if (profiles.isEmpty()) {
        when(space.getAllSpaceProfilesInst()).thenAnswer(j -> {
          initProfiles(spaceId, profiles);
          return profiles;
        });
      } else {
        when(space.getAllSpaceProfilesInst()).thenReturn(profiles);
      }

      when(space.getDirectSpaceProfileInst(anyString())).thenAnswer(j -> {
        String role = j.getArgument(0);
        return profiles.stream()
            .filter(p -> !p.isInherited())
            .filter(p -> p.getName().equals(role))
            .findFirst()
            .map(p -> {
              SpaceProfileInst pp = new SpaceProfileInst(p);
              pp.setId(p.getId());
              return pp;
            })
            .orElse(null);
      });

      return space;
    });

    // mock the behaviour of a space role update with a modified set of users playing this role
    // the behaviour update the profiles cache for the space used in the test
    when(
        administration.updateSpaceProfileInst(any(SpaceProfileInst.class), anyString())).thenAnswer(
        i -> {
          SpaceProfileInst profile = i.getArgument(0);
          List<SpaceProfileInst> profiles =
              profilesPerSpace.computeIfAbsent(profile.getSpaceFatherId(), s -> new ArrayList<>());
          profiles.stream()
              .filter(p -> p.getId().equals(profile.getId()))
              .findFirst()
              .ifPresent(p -> p.setUsers(profile.getAllUsers()));
          return profile.getId();
        });

    when(administration.getGroup(anyString())).thenAnswer(i -> {
      GroupDetail group = new GroupDetail();
      group.setName("foo");
      group.setId(i.getArgument(0));
      return group;
    });

    when(administration.addGroup(any(GroupDetail.class), any(Boolean.class))).thenAnswer(i -> {
      GroupDetail group = i.getArgument(0);
      group.setId("42");
      return group.getId();
    });
  }

  private static void mockUsersProviding(UserProvider userProvider) {
    // get a user whatever its id
    Answer<? extends User> userAnswer = a -> {
      String id = a.getArgument(0);
      UserDetail user = new UserDetail();
      user.setId(id);
      return user;
    };
    when(userProvider.getUser(anyString())).thenAnswer(userAnswer);
  }
}