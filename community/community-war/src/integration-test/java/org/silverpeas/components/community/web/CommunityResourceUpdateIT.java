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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.community.CommunityWarBuilder;
import org.silverpeas.components.community.model.Community;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.web.ResourceUpdateTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests about the modification of Community contributions.
 */
@RunWith(Arquillian.class)
public class CommunityResourceUpdateIT extends ResourceUpdateTest {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  private static final String EXPECTED_ID = "45d42847-2009-4ca8-86bb-1918909dc094";

  private String authToken;
  private CommunityEntity expected;

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityResourceUpdateIT.class)
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
    expected = new CommunityEntity(Community.getById(EXPECTED_ID));
  }

  @Override
  @Test
  @Ignore
  public void updateOfAResourceFromAnInvalidOne() {
    // TODO remove it once the entity validity is implemented in CommunityResource
  }

  @Override
  @Test
  @Ignore
  public void updateWithAnInvalidResourceState() {
    // TODO remove it once the entity validity is implemented in CommunityResource
  }

  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }

  @Override
  public String aResourceURI() {
    return "community/" + getExistingComponentInstances()[0] + "/" + EXPECTED_ID;
  }

  @Override
  public String anUnexistingResourceURI() {
    return "community/" + getExistingComponentInstances()[0] + "/100";
  }

  @Override
  @SuppressWarnings("unchecked")
  public CommunityEntity aResource() {
    return expected;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CommunityEntity anInvalidResource() {
    return new CommunityEntity();
  }

  @Override
  public String getAPITokenValue() {
    return authToken;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return CommunityEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"community1", "community2"};
  }

}