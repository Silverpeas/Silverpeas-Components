/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.resourcesmanager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.service.ResourceService;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.util.List;

/**
 * Integration test on the behaviour of the Resources Manager when a user doesn't belong anymore
 * to a group playing a role in some Resources Manager applications.
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class GroupMembershipChangeIT extends BaseEventListenerTest {

  @Inject
  private ResourceService resourceService;

  @Inject
  private AdminController admin;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("service/create-database.sql", "service/resources_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(ProfileInstChangeIT.class).build();
  }

  @Before
  public void reloadCache() {
    admin.reloadCaches();
  }

  /**
   * Given a user is in a group G playing the manager role for some Resources Manager applications A
   * and this user is a validator of some resources R in those applications A,
   * When he's removed from the group G,
   * Then he should be also removed from the validators of resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenRemovedFromAGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));

    admin.removeUserFromGroup("1", "1");

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a user is in a group G playing the reader role for some Resources Manager applications A,
   * When he's removed from the group G,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aNonValidatorHasBeenRemovedFromAGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("11", "1");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user is in a group G playing the reader role for some Resources Manager applications A
   * and this user is also a validator of some resources R in those applications A,
   * When he's removed from the group G,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenRemovedFromAGroupInReadersProfile() {
    setUpProfileInstWith(READER_ROLE, NO_IDS, List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "1");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing the manager role for some Resources Manager applications A,
   * When a user is added into the group G,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorHasBeenAddedInAGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("2"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.addUserInGroup("1", "2");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user is in a subgroup G of a group playing the manager role for some Resources Manager
   * applications A and this user is a validator of some resources R in those applications A,
   * When he's removed from the subgroup G,
   * Then he should be also removed from the validators of resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenRemovedFromASubGroupOfAGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("3"));

    admin.removeUserFromGroup("1", "4");

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a group G playing the publisher role for an applications other than Resources
   * Manager and playing no role in any Resources Manager applications A,
   * Given a user U being a validator for some resources in the applications A,
   * When the user U is removed from the group G,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aUserHasBeenRemovedFromAGroupInPublishersProfileInAnotherApplication() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "1");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing the manager role in a Resources Manager applications A,
   * Given a user U playing explicitly the manager role in the application A, belonging to the
   * group G, and is a validator of some resources R in those applications A,
   * When the user U is removed from the group G,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorInManagersProfileHasBeenRemovedFromAGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "1");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing the manager role in a Resources Manager applications A,
   * Given a user U playing explicitly the manager role in the application A, belonging to a
   * subgroup S of G, and is a validator of some resources R in those applications A,
   * When the user U is removed from the subgroup S,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorInManagersProfileHasBeenRemovedFromASubGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), List.of("3"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "4");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing by rights inheritance the manager role in a Resources Manager
   * applications A,
   * Given a user U playing explicitly the manager role in the application A, belonging to the
   * group G, and is a validator of some resources R in those applications A,
   * When the user U is removed from the group G,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorInManagersProfileHasBeenRemovedFromAGroupInInheritedManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "1");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing by rights inheritance the manager role in a Resources Manager
   * applications A,
   * Given a user U playing explicitly the manager role in the application A, belonging to the
   * subgroup S of the group G, and is a validator of some resources R in those applications A,
   * When the user U is removed from the subgroup S,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorInManagersProfileHasBeenRemovedFromASubGroupInInheritedManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("3"));
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "4");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing the manager role in a Resources Manager applications A,
   * Given a user U playing by inheritance the manager role in the application A, belonging to the
   * group G, and is a validator of some resources R in those applications A,
   * When the user U is removed from the group G,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorInInheritedManagersProfileHasBeenRemovedFromAGroupInManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), NO_IDS);
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "1");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a group G playing the manager role in a Resources Manager applications A,
   * Given a user U playing by inheritance the manager role in the application A, belonging to the
   * subgroup S of the group G, and is a validator of some resources R in those applications A,
   * When the user U is removed from the subgroup S,
   * Then nothing should be done with the validators of all the resources in the applications A.
   */
  @Test
  public void aValidatorInInheritedManagersProfileHasBeenRemovedFromASubGroupInManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, List.of("1"), NO_IDS);
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("3"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    admin.removeUserFromGroup("1", "4");

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }
}
