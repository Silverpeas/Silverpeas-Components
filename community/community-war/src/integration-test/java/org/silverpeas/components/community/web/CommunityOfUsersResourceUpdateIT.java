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
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.Pair;
import org.silverpeas.web.test.ResourceUpdateTest;

import java.net.MalformedURLException;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests about the update of some properties of a community of users.
 */
@RunWith(Arquillian.class)
public class CommunityOfUsersResourceUpdateIT extends ResourceUpdateTest {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  private static final String EXPECTED_ID = "community1";

  private String authToken;
  private CommunityOfUsersEntity expectedEntity;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityOfUsersResourceUpdateIT.class)
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
  public void prepareTestResources() throws MalformedURLException {
    authToken = getTokenKeyOf(User.getById("1"));
    CommunityOfUsers community = CommunityOfUsers.getByComponentInstanceId(EXPECTED_ID)
        .orElseThrow(
            () -> new SilverpeasRuntimeException("No such community instance: " + EXPECTED_ID));
    community.setHomePage("https://www.silverpeas.org", SpaceHomePageType.HTML_PAGE);
    community.setCharterURL("http://localhost:8080/silverpeas/charters/community1.html");
    expectedEntity = CommunityOfUsersEntity.builder()
        .with(community)
        .build();
    expectedEntity.setUri(getWebResourceBaseURIBuilder().path(aResourceURI()).build());
    expectedEntity.setMemberships(
        getWebResourceBaseURIBuilder().path(aResourceURI()).path("memberships").build());
  }

  @Test
  public void updateAnExistingCommunity() {
    URI communityURI = getWebResourceBaseURIBuilder().path(aResourceURI()).build();
    URI membershipsURI =
        getWebResourceBaseURIBuilder().path(aResourceURI()).path("memberships").build();

    CommunityOfUsersEntity entity = putAt(aResourceURI(), aResource());
    assertThat(entity.getURI(), is(communityURI));
    assertThat(entity.getId(), is(EXPECTED_ID));
    assertThat(entity.getSpaceId(), is("WA1"));
    assertThat(entity.getMemberships(), is(membershipsURI));
    assertThat(entity.getHomePage(),
        is(Pair.of("https://www.silverpeas.org", SpaceHomePageType.HTML_PAGE)));
    assertThat(entity.getCharterURL().toString(),
        is("http://localhost:8080/silverpeas/charters/community1.html"));
  }

  @Override
  public String aResourceURI() {
    return "community/" + EXPECTED_ID;
  }

  @Override
  public String anUnexistingResourceURI() {
    return "community/100";
  }

  @Override
  @SuppressWarnings("unchecked")
  public CommunityOfUsersEntity aResource() {
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
    return new String[]{EXPECTED_ID, "community2"};
  }

  @SuppressWarnings("unchecked")
  @Override
  public CommunityOfUsersEntity anInvalidResource() {
    CommunityOfUsers community = new CommunityOfUsers("community4", "WA4");
    return CommunityOfUsersEntity.builder()
        .with(community)
        .build();
  }
}