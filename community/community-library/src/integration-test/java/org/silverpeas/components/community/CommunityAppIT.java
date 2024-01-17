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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.community.model.CommunityOfUsers;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.space.SpaceHomePageType;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests about the creation and the deletion of a Community application instance.
 */
@RunWith(Arquillian.class)
public class CommunityAppIT {

  private static final String DATABASE_CREATION_SCRIPT = "/community-database.sql";

  private static final String DATASET_SCRIPT = "/community-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(DATABASE_CREATION_SCRIPT)
      .loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return CommunityWarBuilder.onWarForTestClass(CommunityAppIT.class)
        .initJcr()
        .addAsResource("org/silverpeas/publication/publicationSettings.properties")
        .addAsResource("org/silverpeas/publicationTemplate/settings/mapping.properties")
        .addAsResource("org/silverpeas/publicationTemplate/settings/template.properties")
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

  @Test
  public void createANewAppInstanceShouldCreateACommunity() throws AdminException, QuotaException {
    Administration admin = Administration.get();
    User user = User.getCurrentUser();
    ComponentInst componentInst = newCommunityAppInstance();
    String instanceId = admin.addComponentInst(user.getId(), componentInst);

    SpaceInst spaceInst = admin.getSpaceInstById(componentInst.getSpaceId());
    assertThat(spaceInst.getFirstPageExtraParam(), is(instanceId));
    assertThat(spaceInst.getFirstPageType(), is(SpaceHomePageType.COMPONENT_INST.ordinal()));
    assertThat(spaceInst.isInheritanceBlocked(), is(true));

    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isPresent(), is(true));
    CommunityOfUsers actualCommunity = community.get();
    assertThat(actualCommunity.getComponentInstanceId(), is(instanceId));
    assertThat(actualCommunity.getSpaceId(), is(componentInst.getSpaceId()));
  }

  @Test
  public void deleteAnExistingAppInstanceShouldDeleteTheCommunity() throws AdminException {
    User admin = User.getCurrentUser();
    String instanceId = "community2";
    Administration.get().deleteComponentInst(admin.getId(), instanceId, true);

    Optional<CommunityOfUsers> community = CommunityOfUsers.getByComponentInstanceId(instanceId);
    assertThat(community.isEmpty(), is(true));
  }

  private static ComponentInst newCommunityAppInstance() {
    ComponentInst componentInst = new ComponentInst();
    componentInst.setDomainFatherId("WA4");
    componentInst.setCreatorUserId(User.getCurrentUser().getId());
    componentInst.setName("community");
    componentInst.setLabel("WA4 Community");
    componentInst.setDescription("Community of users for space WA4");
    componentInst.setPublic(true);
    componentInst.setHidden(false);
    componentInst.setInheritanceBlocked(true);
    componentInst.setParameters(new ArrayList<>());
    return componentInst;
  }
}