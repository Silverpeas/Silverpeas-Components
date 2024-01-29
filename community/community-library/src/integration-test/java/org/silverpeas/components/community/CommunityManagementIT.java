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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.CommunityMembershipsProvider;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.components.community.repository.CommunityMembershipRepository;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.kernel.util.Pair;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.exparity.hamcrest.date.OffsetDateTimeMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;

/**
 * Integration tests about the management of Community contributions.
 */
@RunWith(Arquillian.class)
public class CommunityManagementIT {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  @Inject
  private CommunityMembershipRepository membershipRepository;

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(DATABASE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityManagementIT.class)
        .addAsResource(DATABASE_CREATION_SCRIPT.substring(1))
        .addAsResource(DATASET_SCRIPT.substring(1))
        .build();
  }

  @Before
  public void initCurrentRequester() {
    SessionCacheAccessor sessionCacheAccessor =
        (SessionCacheAccessor) CacheAccessorProvider.getSessionCacheAccessor();
    sessionCacheAccessor.newSessionCache(User.getById("0"));
  }

  @SuppressWarnings("unchecked")
  @After
  public void clearCaches() throws IllegalAccessException {
    Administration.get().reloadCache();
    Map<String, OffsetDateTime> cache =
        (Map<String, OffsetDateTime>) FieldUtils.readDeclaredStaticField(
            CommunityMembershipsProvider.class, "lastSynchronizations", true);
    cache.clear();
  }

  @Test
  public void getAnExistingCommunity() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.getComponentInstanceId(), is(instanceId));
    assertThat(actualCommunity.getSpaceId(), is("WA1"));
    assertThat(actualCommunity.getHomePage(),
        is(Pair.of("kmelia42", SpaceHomePageType.COMPONENT_INST)));
    assertThat(actualCommunity.getCharterURL().toString(), is("https://www.silverpeas.org"));
  }

  @Test
  public void getANonExistingCommunity() {
    String instanceId = "community100";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isEmpty(), is(true));
  }

  @Test
  public void getAnExistingCommunityOfASpace() {
    String spaceId = "WA1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getBySpaceId(spaceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.getComponentInstanceId(), is("community1"));
    assertThat(actualCommunity.getSpaceId(), is(spaceId));
    assertThat(actualCommunity.getHomePage(),
        is(Pair.of("kmelia42", SpaceHomePageType.COMPONENT_INST)));
    assertThat(actualCommunity.getCharterURL().toString(), is("https://www.silverpeas.org"));
  }

  @Test
  public void getACommunityOfANonExistingSpace() {
    String spaceId = "WA100";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getBySpaceId(spaceId);
    assertThat(community.isEmpty(), is(true));
  }

  @Test
  public void getACommunityOfANonCommunitySpace() {
    String spaceId = "WA4";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getBySpaceId(spaceId);
    assertThat(community.isEmpty(), is(true));
  }

  @Test
  public void updateAnExistingCommunity() throws MalformedURLException {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    actualCommunity.setHomePage("https://www.silverpeas.org", SpaceHomePageType.HTML_PAGE);
    actualCommunity.setCharterURL("http://localhost:8080/silverpeas/communities/1/charter.html");
    actualCommunity.save();

    community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));
    CommunityOfUsers updated = community.get();
    assertThat(updated.getId(), is(actualCommunity.getId()));
    assertThat(updated.getHomePage().getFirst(), is("https://www.silverpeas.org"));
    assertThat(updated.getHomePage().getSecond(), is(SpaceHomePageType.HTML_PAGE));
    assertThat(updated.getCharterURL().toString(),
        is("http://localhost:8080/silverpeas/communities/1/charter.html"));
  }

  @Test
  public void updateANonPersistedCommunity() {
    CommunityOfUsers community = new CommunityOfUsers("community4", "WA3");
    assertThat(community.isPersisted(), is(false));
    assertThrows(IllegalStateException.class, community::save);
  }

  @Test
  public void checkAUserIsMemberOfASpaceWithoutRoleInheritance() {
    String instanceId = "community1";
    String spaceManagerId = "0";
    String adminId = "1";
    String publisherId = "2";
    String readerId = "3";
    String noMemberId = "4";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actual = community.get();
    assertThat(actual.isMember(User.getById(spaceManagerId)), is(false));
    assertThat(actual.isMember(User.getById(noMemberId)), is(false));
    assertThat(actual.isMember(User.getById(adminId)), is(true));
    assertThat(actual.isMember(User.getById(publisherId)), is(true));
    assertThat(actual.isMember(User.getById(readerId)), is(true));
  }

  @Test
  public void checkAUserIsMemberOfASpaceWithRoleInheritance() {
    String instanceId = "community2";
    String spaceManagerId = "0";
    String[] publishers = new String[]{"1", "2"};
    String inheritedReaderId = "3";
    String noMemberId = "4";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actual = community.get();
    assertThat(actual.isMember(User.getById(spaceManagerId)), is(false));
    assertThat(actual.isMember(User.getById(noMemberId)), is(false));
    assertThat(actual.isMember(User.getById(inheritedReaderId)), is(false));
    assertThat(actual.isMember(User.getById(publishers[0])), is(true));
    assertThat(actual.isMember(User.getById(publishers[1])), is(true));
  }

  @Test
  public void addAMemberToACommunityShouldAddIt() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    // user 5 isn't a member of the community
    User user = User.getById("5");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    // add him as a member of the community with the role of PUBLISHER
    CommunityMembership membership = actualCommunity.addAsMember(user, SilverpeasRole.PUBLISHER);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    assertThat(membership, notNullValue());
    assertThat(membership.getCommunity(), is(actualCommunity));
    assertThat(membership.getStatus().isMember(), is(true));
    assertThat(membership.getJoiningDate(), within(1, ChronoUnit.SECONDS, now));

    // assert he is actually member of the community, he plays the role of PUBLISHER, and he has
    // been added into the table of members of the community
    assertThat(actualCommunity.isMember(user), is(true));
    assertThat(membership.getMemberRole(), is(SilverpeasRole.PUBLISHER));
    assertThat(membershipRepository.getById(membership.getId()), is(membership));
  }

  @Test
  public void addMemberToACommunityWithInvalidRoleShouldFail() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    // user 5 isn't a member of the community
    User user = User.getById("5");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    assertThrows(IllegalArgumentException.class,
        () -> actualCommunity.addAsMember(user, SilverpeasRole.USER));
  }

  @Test
  public void addFirstMemberToACommunityShouldAddIt() {
    String instanceId = "community3";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.getMembershipsProvider().isEmpty(), is(true));

    // user 1 isn't a member of the community
    User user = User.getById("1");
    assertThat(actualCommunity.isMember(user), is(false));

    // add him as a member of the community with the role of PUBLISHER
    CommunityMembership membership = actualCommunity.addAsMember(user, SilverpeasRole.PUBLISHER);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    assertThat(membership, notNullValue());
    assertThat(membership.getCommunity(), is(actualCommunity));
    assertThat(membership.getStatus().isMember(), is(true));
    assertThat(membership.getJoiningDate(), within(1, ChronoUnit.SECONDS, now));

    // assert he is actually member of the community, he plays the role of PUBLISHER, and he has
    // been added into the table of members of the community
    assertThat(actualCommunity.getMembershipsProvider().isEmpty(), is(false));
    assertThat(actualCommunity.isMember(user), is(true));
    assertThat(membership.getMemberRole(), is(SilverpeasRole.PUBLISHER));
    assertThat(membershipRepository.getById(membership.getId()), is(membership));
  }

  @Test
  public void addAnExistingMemberToACommunityShouldFail() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("1");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(true));

    assertThrows(AlreadyMemberException.class,
        () -> actualCommunity.addAsMember(user, SilverpeasRole.PUBLISHER));
  }

  @Test
  public void addAPreviouslyRemovedMemberToACommunityShouldAddItAsNewMember() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    // user 4 isn't actually a member of the community
    User user = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    // nevertheless he has been in the past a member of the community
    CommunityMembership removed =
        membershipRepository.getById("c44064ef-1118-4bfb-8494-de6df5e7cbdb");
    assertThat(removed, notNullValue());
    assertThat(removed.getUser(), is(user));
    assertThat(removed.getStatus().isNoMoreMember(), is(true));

    // add the user 4 as member of the community with the role READER
    CommunityMembership membership = actualCommunity.addAsMember(user, SilverpeasRole.READER);
    assertThat(membership, notNullValue());
    assertThat(membership.getCommunity(), is(actualCommunity));
    assertThat(membership.getStatus().isMember(), is(true));

    // assert the user 4 is now a member of the community but with another member identifier,
    // he plays the role of READER, and he has been added into the table of members of the
    // community
    assertThat(actualCommunity.isMember(user), is(true));
    assertThat(membership.getMemberRole(), is(SilverpeasRole.READER));
    assertThat(membership, not(removed));
    assertThat(membershipRepository.getById(membership.getId()), is(membership));
  }

  @Test
  public void addAPendingMemberToACommunityWithAValidRoleShouldFail() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));
    Optional<CommunityMembership> pendingMembership =
        actualCommunity.getMembershipsProvider().get(user);
    assertThat(pendingMembership.isPresent(), is(true));
    assertThat(pendingMembership.get().getStatus().isPending(), is(true));

    CommunityMembership membership = actualCommunity.addAsMember(user, SilverpeasRole.PUBLISHER);
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    assertThat(membership, notNullValue());
    assertThat(membership.getCommunity(), is(actualCommunity));
    assertThat(membership.getStatus().isMember(), is(true));
    assertThat(membership.getJoiningDate(), within(1, ChronoUnit.SECONDS, now));

    // assert he is actually member of the community, he plays the role of PUBLISHER, and he has
    // been added into the table of members of the community
    assertThat(actualCommunity.getMembershipsProvider().isEmpty(), is(false));
    assertThat(actualCommunity.isMember(user), is(true));
    assertThat(membership.getMemberRole(), is(SilverpeasRole.PUBLISHER));
    assertThat(membershipRepository.getById(membership.getId()), is(membership));
  }

  @Test
  public void removeAMemberFromACommunityShouldUpdateMembershipStatusToRemoved() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("1");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(true));

    CommunityMembership membership = actualCommunity.removeMembership(user);
    assertThat(membership, notNullValue());
    assertThat(membership.getMemberRole(), nullValue());
    assertThat(membership.getStatus().isNoMoreMember(), is(true));

    CommunityMembership removedMember = membershipRepository.getById(membership.getId());
    assertThat(removedMember, notNullValue());
    assertThat(removedMember.getStatus().isNoMoreMember(), is(true));
  }

  @Test
  public void removeANonMemberFromACommunityShouldDoNothing() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("5");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    // assert nothing is done and hence no membership is returned
    CommunityMembership membership = actualCommunity.removeMembership(user);
    assertThat(membership, nullValue());
  }

  @Test
  public void removeAnAlreadyNonMemberFromACommunityShouldDoNothing() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    // user 4 isn't actually a member of the community
    User user = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    // nevertheless he has been in the past a member of the community
    CommunityMembership removed =
        membershipRepository.getById("c44064ef-1118-4bfb-8494-de6df5e7cbdb");
    assertThat(removed, notNullValue());
    assertThat(removed.getUser(), is(user));
    assertThat(removed.getStatus().isNoMoreMember(), is(true));

    // assert nothing is done and hence no membership is returned
    CommunityMembership membership = actualCommunity.removeMembership(user);
    assertThat(membership, nullValue());
  }

  @Test
  public void getMembersOfACommunityWillFetchCommittedMembershipsOnly() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    List<CommunityMembership> members =
        actualCommunity.getMembershipsProvider().getInRange(new PaginationPage(1, 10));
    assertThat(members, notNullValue());
    assertThat(members.size(), is(3));
    assertThat(members.get(0).getUser().getId(), is("3"));
    assertThat(members.get(0).getStatus().isMember(), is(true));
    assertThat(members.get(1).getUser().getId(), is("2"));
    assertThat(members.get(1).getStatus().isMember(), is(true));
    assertThat(members.get(2).getUser().getId(), is("1"));
    assertThat(members.get(2).getStatus().isMember(), is(true));
  }

  @Test
  public void getPendingMembersOfACommunityWillFetchPendingMembershipsOnly() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    List<CommunityMembership> members =
        actualCommunity.getMembershipsProvider().getPending(new PaginationPage(1, 10));
    assertThat(members, notNullValue());
    assertThat(members.size(), is(1));
    assertThat(members.get(0).getUser().getId(), is("4"));
  }

  @Test
  public void getMembersOfAnEmptyCommunityShouldReturnNothing() {
    String instanceId = "community3";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.getMembershipsProvider().isEmpty(), is(true));

    List<CommunityMembership> members =
        actualCommunity.getMembershipsProvider().getInRange(new PaginationPage(1, 10));
    assertThat(members, notNullValue());
    assertThat(members.isEmpty(), is(true));
  }

  @Test
  public void getHistoryOfMembershipsWillFetchAllWhateverTheMembershipStatus() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    CommunityOfUsers actualCommunity = community.get();
    List<CommunityMembership> history =
        actualCommunity.getMembershipsProvider().getHistory(new PaginationPage(1, 10));
    assertThat(history.size(), is(6));
    assertThat(history.get(0).getUser().getId(), is("4"));
    assertThat(history.get(0).getStatus().isNoMoreMember(), is(true));
    assertThat(history.get(1).getUser().getId(), is("5"));
    assertThat(history.get(1).getStatus().isPending(), is(true));
    assertThat(history.get(2).getUser().getId(), is("3"));
    assertThat(history.get(2).getStatus().isMember(), is(true));
    assertThat(history.get(3).getUser().getId(), is("2"));
    assertThat(history.get(3).getStatus().isMember(), is(true));
    assertThat(history.get(4).getUser().getId(), is("1"));
    assertThat(history.get(4).getStatus().isMember(), is(true));
    assertThat(history.get(5).getUser().getId(), is("4"));
    assertThat(history.get(5).getStatus().isRefused(), is(true));
  }

  @Test
  public void synchronizationIsPerformedWhileGettingCommittedMemberships() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user1 = User.getById("1");
    User user2 = User.getById("2");
    User user3 = User.getById("3");
    CommunityOfUsers actualCommunity = community.get();

    // user 1 plays a role in the community space, although he's not anymore a member
    assertThat(actualCommunity.isMember(user1), is(true));
    var membership = membershipRepository.getById("5d64ded7-89d2-4ffa-87cb-03146e94588d");
    assertThat(membership, notNullValue());
    assertThat(membership.getUser(), is(user1));
    assertThat(membership.getStatus().isNoMoreMember(), is(true));

    // user 2 plays a role in the community space, but his membership hasn't been registered
    assertThat(actualCommunity.isMember(user1), is(true));
    assertThat(membershipRepository.getMembershipsTable(actualCommunity).getByUser(user2).isEmpty(),
        is(true));

    // user 3 doesn't play anymore an explicit role (only an inherited one) in the community
    // space but his membership is always committed
    assertThat(actualCommunity.isMember(user3), is(false));
    membership = membershipRepository.getById("5f78752a-ed3c-4c88-bfe9-c647cd9c98e6");
    assertThat(membership, notNullValue());
    assertThat(membership.getUser(), is(user3));
    assertThat(membership.getStatus().isMember(), is(true));

    // synchronization should be performed while getting members of the community of users
    List<CommunityMembership> memberships =
        actualCommunity.getMembershipsProvider().getInRange(new PaginationPage(1, 10));
    assertThat(memberships, notNullValue());
    assertThat(memberships.size(), is(2));
    assertThat(memberships.get(0).getUser().getId(), is(user2.getId()));
    assertThat(memberships.get(0).getStatus().isMember(), is(true));
    assertThat(actualCommunity.isMember(user2), is(true));
    assertThat(memberships.get(1).getUser().getId(), is(user1.getId()));
    assertThat(memberships.get(1).getStatus().isMember(), is(true));
    assertThat(actualCommunity.isMember(user1), is(true));

    membership = membershipRepository.getById("5f78752a-ed3c-4c88-bfe9-c647cd9c98e6");
    assertThat(membership, notNullValue());
    assertThat(membership.getUser(), is(user3));
    assertThat(membership.getStatus().isNoMoreMember(), is(true));
    assertThat(actualCommunity.isMember(user3), is(false));
  }

  @Test
  public void synchronizationIsPerformedWhileGettingMembershipsHistory() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user1 = User.getById("1");
    User user2 = User.getById("2");
    User user3 = User.getById("3");
    User user4 = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();

    // user 1 plays a role in the community space, although he's not anymore a member
    assertThat(actualCommunity.isMember(user1), is(true));
    var membership = membershipRepository.getById("5d64ded7-89d2-4ffa-87cb-03146e94588d");
    assertThat(membership.getUser(), is(user1));
    assertThat(membership.getStatus().isNoMoreMember(), is(true));

    // user 2 plays a role in the community space, but his membership hasn't been registered
    assertThat(actualCommunity.isMember(user1), is(true));
    assertThat(membershipRepository.getMembershipsTable(actualCommunity).getByUser(user2).isEmpty(),
        is(true));

    // user 3 doesn't play anymore an explicit role (only an inherited one) in the community
    // space but his membership is always committed
    assertThat(actualCommunity.isMember(user3), is(false));
    membership = membershipRepository.getById("5f78752a-ed3c-4c88-bfe9-c647cd9c98e6");
    assertThat(membership.getUser(), is(user3));
    assertThat(membership.getStatus().isMember(), is(true));

    // user 4 doesn't play any role and his membership to the community is pending
    assertThat(actualCommunity.isMember(user4), is(false));
    membership = membershipRepository.getById("67571a42-443c-4100-b0b0-9a9638204646");
    assertThat(membership.getStatus().isPending(), is(true));

    // synchronization should be performed while getting the history of memberships of the
    // community of users
    List<CommunityMembership> members =
        actualCommunity.getMembershipsProvider().getHistory(new PaginationPage(1, 10));
    assertThat(members.size(), is(5));

    // user 3 has been just removed by the synchronization
    assertThat(members.get(0).getUser().getId(), is(user3.getId()));
    assertThat(members.get(0).getStatus().isNoMoreMember(), is(true));
    assertThat(actualCommunity.isMember(user3), is(false));

    // user 2 has been just registered by the synchronization
    assertThat(members.get(1).getUser().getId(), is(user2.getId()));
    assertThat(members.get(1).getStatus().isMember(), is(true));
    assertThat(actualCommunity.isMember(user2), is(true));

    // user 1 is again a member now with the synchronization
    assertThat(members.get(2).getUser().getId(), is(user1.getId()));
    assertThat(members.get(2).getStatus().isMember(), is(true));
    assertThat(actualCommunity.isMember(user1), is(true));

    // but user 1 was also a removed member before being added once again
    assertThat(members.get(3).getUser().getId(), is(user1.getId()));
    assertThat(members.get(3).getStatus().isNoMoreMember(), is(true));

    // user 4 is always a pending member
    assertThat(members.get(4).getUser().getId(), is("4"));
    assertThat(members.get(4).getStatus().isPending(), is(true));
  }

  @Test
  public void pendingMembershipsAreAlsoSynchronized() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();

    assertThat(actualCommunity.isMember(user), is(false));
    Optional<CommunityMembership> membership = actualCommunity.getMembershipsProvider().get(user);
    assertThat(membership.isPresent(), is(true));
    assertThat(membership.get().getStatus().isPending(), is(true));
    assertThat(membership.get().getJoiningDate(), nullValue());

    // add explicitly user 4 in a role of the community space
    Transaction.performInOne(() -> {
      Administration admin = Administration.get();
      SpaceInst spaceInst = admin.getSpaceInstById("WA2");
      SpaceProfileInst profileInst =
          spaceInst.getSpaceProfileInst(SilverpeasRole.PUBLISHER.getName());
      profileInst.getAllUsers().add(user.getId());
      admin.updateSpaceProfileInst(profileInst, "0");
      return null;
    });

    // synchronize (here by getting all the committed memberships
    var actualMemberships =
        actualCommunity.getMembershipsProvider().getInRange(new PaginationPage(1, 10));
    assertThat(actualMemberships, notNullValue());

    // user 4 is now member of the community
    membership = actualCommunity.getMembershipsProvider().get(user);
    assertThat(membership.isPresent(), is(true));
    assertThat(membership.get().getStatus().isMember(), is(true));
    assertThat(membership.get().getJoiningDate(), notNullValue());
  }

  @Test
  public void addAUserAsAPendingMemberShouldAddHisPendingMembership() {
    String instanceId = "community3";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));
    CommunityOfUsers actualCommunity = community.get();

    User user = User.getById("2");
    assertThat(actualCommunity.isMember(user), is(false));

    CommunityMembership membership = actualCommunity.addAsAPendingMember(user);
    assertThat(actualCommunity.isMember(user), is(false));
    assertThat(membership.getMemberRole(), nullValue());
    assertThat(membership.getStatus().isPending(), is(true));

    List<CommunityMembership> pendingMemberships =
        actualCommunity.getMembershipsProvider().getPending(new PaginationPage(1, 10));
    assertThat(pendingMemberships.size(), is(1));
    assertThat(pendingMemberships.get(0).getStatus().isPending(), is(true));
    assertThat(pendingMemberships.get(0).getUser(), is(user));
  }

  @Test
  public void addAnAlreadyPendingMemberToACommunityShouldFail() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    Optional<CommunityMembership> membership = actualCommunity.getMembershipsProvider().get(user);
    assertThat(membership.isPresent(), is(true));
    assertThat(membership.get().getStatus().isPending(), is(true));

    assertThrows(AlreadyMemberException.class,
        () -> actualCommunity.addAsAPendingMember(user));
  }

  @Test
  public void addAnAlreadyMemberAsPendingMemberToACommunityShouldFail() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("1");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(true));

    Optional<CommunityMembership> membership = actualCommunity.getMembershipsProvider().get(user);
    assertThat(membership.isPresent(), is(true));
    assertThat(membership.get().getStatus().isMember(), is(true));

    assertThrows(AlreadyMemberException.class,
        () -> actualCommunity.addAsAPendingMember(user));
  }

  @Test
  public void refuseAPendingMembershipShouldUpdateHisMembershipStatusToRefused() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("4");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));
    Optional<CommunityMembership> membership = actualCommunity.getMembershipsProvider().get(user);
    assertThat(membership.isPresent(), is(true));
    assertThat(membership.get().getStatus().isPending(), is(true));

    CommunityMembership refusedMembership = actualCommunity.refuseMembership(user);
    assertThat(refusedMembership, notNullValue());
    assertThat(refusedMembership.getStatus().isRefused(), is(true));
    assertThat(refusedMembership.getJoiningDate(), nullValue());
  }

  @Test
  public void refuseAnExistingCommittedMembershipShouldFail() {
    String instanceId = "community1";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("1");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(true));

    Optional<CommunityMembership> membership = actualCommunity.getMembershipsProvider().get(user);
    assertThat(membership.isPresent(), is(true));
    assertThat(membership.get().getStatus().isMember(), is(true));

    assertThrows(IllegalStateException.class,
        () -> actualCommunity.refuseMembership(user));
  }

  @Test
  public void refuseANonExistingMembershipShouldFail() {
    String instanceId = "community2";
    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));

    User user = User.getById("5");
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.isMember(user), is(false));

    assertThrows(IllegalStateException.class,
        () -> actualCommunity.refuseMembership(user));
  }
}