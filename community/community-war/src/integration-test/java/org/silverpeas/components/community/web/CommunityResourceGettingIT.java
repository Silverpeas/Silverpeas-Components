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
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.web.ResourceGettingTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests about the getting of Community contributions.
 */
@RunWith(Arquillian.class)
public class CommunityResourceGettingIT extends ResourceGettingTest {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  private static final String EXPECTED_ID = "community1";

  private String authToken;
  private CommunityOfUsersEntity expectedEntity;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityResourceGettingIT.class)
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
    CommunityOfUsers community = CommunityOfUsers.getByComponentInstanceId(EXPECTED_ID)
        .orElseThrow(
            () -> new SilverpeasRuntimeException("No such community instance: " + EXPECTED_ID));
    expectedEntity = new CommunityOfUsersEntity(community);
  }

  @Test
  public void getAnExistingCommunity() {
    CommunityOfUsersEntity entity = getAt(aResourceURI(), CommunityOfUsersEntity.class);
    assertThat(entity.getURI().toString().endsWith(aResourceURI()), is(true));
    assertThat(entity.getId(), is(EXPECTED_ID));
    assertThat(entity.getSpaceId(), is("WA1"));
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

}