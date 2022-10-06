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
package org.silverpeas.components.community.web;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.community.CommunityWarBuilder;
import org.silverpeas.components.community.model.CommunityMembership;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.components.community.model.MembershipStatus;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;
import org.silverpeas.web.ResourceGettingTest;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.silverpeas.components.community.web.CommunityMembershipResourceGettingIT.ExpectedMembershipValue.expectedMembership;
import static org.silverpeas.components.community.web.CommunityMembershipResourceGettingIT.ExpectedMembershipValue.matches;

/**
 * Integration tests about the getting of memberships to a given community of users.
 */
@RunWith(Arquillian.class)
public class CommunityMembershipResourceGettingIT extends ResourceGettingTest {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  private static final String EXPECTED_COMMUNITY = "community1";

  private static final String EXPECTED_ID = "fdf8ec9c-650f-43aa-905e-d5289648a008";

  private String authToken;
  private CommunityMembershipEntity expectedEntity;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityMembershipResourceGettingIT.class)
        .addRESTWebServiceEnvironment()
        .addAsResource(DATABASE_CREATION_SCRIPT.substring(1))
        .addAsResource(DATASET_SCRIPT.substring(1))
        .build();
  }

  @Override
  protected String getTableCreationScript() {
    return DATABASE_CREATION_SCRIPT;
  }

  @Override
  protected String getDataSetScript() {
    return DATASET_SCRIPT;
  }

  @Before
  public void prepareTestResources() {
    authToken = getTokenKeyOf(User.getById("1"));
    CommunityOfUsers community = CommunityOfUsers.getByComponentInstanceId(EXPECTED_COMMUNITY)
        .orElseThrow(
            () -> new SilverpeasRuntimeException(
                "No such community instance: " + EXPECTED_COMMUNITY));
    CommunityMembership membership = community.getMembershipsProvider().get(EXPECTED_ID)
        .orElseThrow(
            () -> new SilverpeasRuntimeException("No such membership " + EXPECTED_ID));
    expectedEntity = CommunityMembershipEntity.builder()
        .with(membership)
        .build();
  }

  @Test
  public void getAnExistingCommunityMembership() {
    URI communityURI = computeCommunityURI(EXPECTED_COMMUNITY);
    URI membershipURI = getWebResourceBaseURIBuilder().path(aResourceURI()).build();
    URI userURI = computeUserProfileURI("1");

    CommunityMembershipEntity entity = getAt(aResourceURI(), CommunityMembershipEntity.class);
    assertThat(entity.getURI(), is(membershipURI));
    assertThat(entity.getCommunity(), is(communityURI));
    assertThat(entity.getStatus(), is(MembershipStatus.COMMITTED.name()));
    assertThat(entity.getUser().getURI(), is(userURI));
    assertThat(entity.getUser().getFirstName(), is("Lisa"));
    assertThat(entity.getUser().getLastName(), is("Simpson"));
  }

  @Test
  public void getActualMembershipOfAGivenUser() {
    String userMembershipURI = getBaseResourceURI(EXPECTED_COMMUNITY) + "/users/1";

    CommunityMembershipEntity entity = getAt(userMembershipURI, CommunityMembershipEntity.class);
    assertMembership(entity, matches(expectedMembership()
        .ofUser("1")
        .toCommunity(EXPECTED_COMMUNITY)
        .withId("fdf8ec9c-650f-43aa-905e-d5289648a008")
        .withStatus(MembershipStatus.COMMITTED)));
  }

  @Test
  public void getPendingMembershipsWithoutPagination() {
    String instanceId = getExistingComponentInstances()[1];
    String pendingURI = getBaseResourceURI(instanceId) + "/pending";
    URI membershipsURI = getWebResourceBaseURIBuilder().path(pendingURI).build();

    CommunityMembershipEntities entity = getAt(pendingURI, CommunityMembershipEntities.class);
    assertThat(entity.getURI(), is(membershipsURI));
    assertThat(entity.getRealSize(), is(1L));
    assertThat(entity.getSize(), is(1L));
    assertThat(entity.getMemberships().size(), is(1));
    assertMembership(entity.getMemberships().get(0), matches(expectedMembership()
        .ofUser("4")
        .toCommunity(instanceId)
        .withId("67571a42-443c-4100-b0b0-9a9638204646")
        .withStatus(MembershipStatus.PENDING)));
  }

  @Test
  public void getActualMembershipsWithoutPagination() {
    String instanceId = getExistingComponentInstances()[0];
    String membersURI = getBaseResourceURI(instanceId) + "/members";
    URI membershipsURI = getWebResourceBaseURIBuilder().path(membersURI).build();

    CommunityMembershipEntities entity = getAt(membersURI, CommunityMembershipEntities.class);
    assertThat(entity.getURI(), is(membershipsURI));
    assertThat(entity.getRealSize(), is(3L));
    assertThat(entity.getSize(), is(3L));
    assertThat(entity.getMemberships().size(), is(3));

    assertMembership(entity.getMemberships().get(0), matches(expectedMembership()
        .ofUser("3")
        .toCommunity(instanceId)
        .withId("3b374e3d-2fff-4d1a-9ece-d4abf82bd2f4")
        .withStatus(MembershipStatus.COMMITTED)));

    assertMembership(entity.getMemberships().get(1), matches(expectedMembership()
        .ofUser("2")
        .toCommunity(instanceId)
        .withId("6d571b08-7baf-47d9-b74c-dfbbe3cee6ad")
        .withStatus(MembershipStatus.COMMITTED)));

    assertMembership(entity.getMemberships().get(2), matches(expectedMembership()
        .ofUser("1")
        .toCommunity(instanceId)
        .withId("fdf8ec9c-650f-43aa-905e-d5289648a008")
        .withStatus(MembershipStatus.COMMITTED)));
  }

  @Test
  public void getMembershipsHistoryWithoutPagination() {
    String instanceId = getExistingComponentInstances()[0];
    String membersURI = getBaseResourceURI(instanceId) + "/all";
    URI membershipsURI = getWebResourceBaseURIBuilder().path(membersURI).build();

    CommunityMembershipEntities entity = getAt(membersURI, CommunityMembershipEntities.class);
    assertThat(entity.getURI(), is(membershipsURI));
    assertThat(entity.getRealSize(), is(6L));
    assertThat(entity.getSize(), is(6L));
    assertThat(entity.getMemberships().size(), is(6));

    assertMembership(entity.getMemberships().get(0), matches(expectedMembership()
        .ofUser("4")
        .toCommunity(instanceId)
        .withId("c44064ef-1118-4bfb-8494-de6df5e7cbdb")
        .withStatus(MembershipStatus.REMOVED)));

    assertMembership(entity.getMemberships().get(1), matches(expectedMembership()
        .ofUser("5")
        .toCommunity(instanceId)
        .withId("da60d652-bfe2-486d-ab9c-a8171b5cd7b9")
        .withStatus(MembershipStatus.PENDING)));

    assertMembership(entity.getMemberships().get(2), matches(expectedMembership()
        .ofUser("3")
        .toCommunity(instanceId)
        .withId("3b374e3d-2fff-4d1a-9ece-d4abf82bd2f4")
        .withStatus(MembershipStatus.COMMITTED)));

    assertMembership(entity.getMemberships().get(3), matches(expectedMembership()
        .ofUser("2")
        .toCommunity(instanceId)
        .withId("6d571b08-7baf-47d9-b74c-dfbbe3cee6ad")
        .withStatus(MembershipStatus.COMMITTED)));

    assertMembership(entity.getMemberships().get(4), matches(expectedMembership()
        .ofUser("1")
        .toCommunity(instanceId)
        .withId("fdf8ec9c-650f-43aa-905e-d5289648a008")
        .withStatus(MembershipStatus.COMMITTED)));

    assertMembership(entity.getMemberships().get(5), matches(expectedMembership()
        .ofUser("4")
        .toCommunity(instanceId)
        .withId("1555d0f1-d9ad-4d55-91e4-1f140c8ff914")
        .withStatus(MembershipStatus.REFUSED)));
  }

  private URI computeCommunityURI(final String instanceId) {
    return getWebResourceBaseURIBuilder().path("community").path(instanceId).build();
  }

  private URI computeMembershipURI(final String instanceId, final String membershipId) {
    return getWebResourceBaseURIBuilder().path(getBaseResourceURI(instanceId))
        .path("all").path(membershipId).build();
  }

  private URI computeUserProfileURI(final String userId) {
    return getWebResourceBaseURIBuilder().path(ProfileResourceBaseURIs.uriOfUser(userId).toString())
        .build();
  }

  private String getBaseResourceURI(final String communityId) {
    return "community/" + communityId + "/memberships";
  }

  private void assertMembership(final CommunityMembershipEntity membership,
      final ExpectedMembershipValue value) {
    URI communityURI = computeCommunityURI(value.instanceId);
    URI membershipURI =
        computeMembershipURI(value.instanceId, value.membershipId);
    URI userURI = computeUserProfileURI(value.userId);
    User user = User.getById(value.userId);
    assertThat(membership.getCommunity(), is(communityURI));
    assertThat(membership.getURI(), is(membershipURI));
    assertThat(membership.getStatus(), is(value.status.name()));
    assertThat(membership.getUser().getURI(), is(userURI));
    assertThat(membership.getUser().getFirstName(), is(user.getFirstName()));
    assertThat(membership.getUser().getLastName(), is(user.getLastName()));
  }

  @Override
  public String aResourceURI() {
    return getBaseResourceURI(EXPECTED_COMMUNITY) + "/all/" + EXPECTED_ID;
  }

  @Override
  public String anUnexistingResourceURI() {
    return getBaseResourceURI(EXPECTED_COMMUNITY) + "/all/" + 100;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CommunityMembershipEntity aResource() {
    return expectedEntity;
  }

  @Override
  public String getAPITokenValue() {
    return authToken;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return CommunityOfUsersEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{EXPECTED_COMMUNITY, "community2"};
  }

  static class ExpectedMembershipValue {

    String instanceId;
    String membershipId;
    String userId;
    MembershipStatus status;

    public static ExpectedMembershipValue matches(ExpectedMembershipValue.Builder builder) {
      return builder.build();
    }

    public static Builder expectedMembership() {
      return new Builder();
    }

    public static class Builder {

      private String instanceId;
      private String membershipId;
      private String userId;
      private MembershipStatus status;

      Builder toCommunity(final String instanceId) {
        this.instanceId = instanceId;
        return this;
      }

      Builder ofUser(final String userId) {
        this.userId = userId;
        return this;
      }

      Builder withId(final String membershipId) {
        this.membershipId = membershipId;
        return this;
      }

      Builder withStatus(final MembershipStatus status) {
        this.status = status;
        return this;
      }

      ExpectedMembershipValue build() {
        ExpectedMembershipValue value = new ExpectedMembershipValue();
        value.membershipId = membershipId;
        value.status = status;
        value.userId = userId;
        value.instanceId = instanceId;
        return value;
      }
    }
  }

}