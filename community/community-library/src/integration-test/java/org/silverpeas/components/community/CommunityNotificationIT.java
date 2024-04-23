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
import org.silverpeas.components.community.notification.CommunityEvent;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.cache.service.SessionCacheAccessor;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the notification about the creation and the deletion of a community of
 * users.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class CommunityNotificationIT {

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
        CacheAccessorProvider.getSessionCacheAccessor();
    sessionCacheAccessor.newSessionCache(User.getById("0"));
  }

  @Test
  public void toBeNotifiedAtCommunityCreation() throws Exception {
    Administration admin = Administration.get();
    User user = User.getCurrentUser();
    ComponentInst componentInst = TestScope.newCommunityAppInstance();
    admin.addComponentInst(user.getId(), componentInst);
  }

  @Test
  public void toBeNotifiedAtCommunityDeletion() throws Exception {
    User admin = User.getCurrentUser();
    String instanceId = "community2";
    Administration.get().deleteComponentInst(admin.getId(), instanceId, true);
  }

  @Bean
  public static class CommunityEventListener extends CDIResourceEventListener<CommunityEvent> {


    @Override
    public void onDeletion(CommunityEvent event) {
      CommunityOfUsers community = event.getTransition().getBefore();
      assertThat(community.isPersisted(), is(false));
      var optionalCommunity =
          CommunityOfUsers.getByComponentInstanceId(community.getComponentInstanceId());
      assertThat(optionalCommunity.isEmpty(), is(true));
    }

    @Override
    public void onCreation(CommunityEvent event) {
      CommunityOfUsers community = event.getTransition().getAfter();
      assertThat(community, is(notNullValue()));
      assertThat(community.isPersisted(), is(true));
      assertThat(community.getGroupOfMembers(), is(notNullValue()));
      assertThat(community.getGroupOfMembers().getAllUsers(), is(empty()));
      assertThat(community.getGroupOfMembers().getSubGroups(), is(empty()));
    }
  }
}
  