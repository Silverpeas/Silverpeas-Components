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
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.util.List;

/**
 * Integration test on the behaviour of the Resources Manager when a user or a group of a user has
 * been remove from Manager role profile.
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ProfileInstChangeIT extends BaseEventListenerTest {

  @Inject
  private ResourceService resourceService;
  
  @Inject
  private Administration admin;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("service/create-database.sql", "service/resources_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(ProfileInstChangeIT.class).build();
  }

  @Before
  public void reloadCache() {
    admin.reloadCache();
  }

  /**
   * Given a user is playing the manager role for some Resources Manager applications A
   * and this user is a validator of some resources R in those applications A,
   * When he's removed from manager role,
   * Then he should be also removed from the validators of resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenRemovedFromManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), NO_IDS);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a user is playing the manager role for some Resources Manager applications A
   * and this user is a validator of some resources R in those applications A,
   * When he's removed from manager role,
   * Then he should be also removed from the validators of resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenRemovedFromInheritedManagersProfile() {
    String profileId = setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"),
        NO_IDS);

    ProfileInst profileToUpdate = getProfileInstById(profileId);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a user is playing the reader role for some Resources Manager applications A
   * and this user is also a validator of some resources R in those applications A,
   * When he's removed from the reader role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenRemovedFromReadersProfile() {
    setUpProfileInstWith(READER_ROLE, List.of("0", "1", "2"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(READER_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user is playing the manager role for some Resources Manager applications A
   * and this user isn't a validator of any resources R in those applications A,
   * When he's removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aNonValidatorHasBeenRemovedFromManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2", "11"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("11");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A
   * and some users U in this group are also validators of some resources R in those applications A,
   * When the group G is removed from the manager role,
   * Then the users U should be also removed from the validators of resources R in the
   * applications A.
   */
  @Test
  public void aGroupWithAValidatorHasBeenRemovedFromManagersProfile() {
    // user 1 belongs to the user group 1
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1", "2"));

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A
   * and some users U in this group are also validators of some resources R in those applications A,
   * When the group G is removed from the manager role,
   * Then the users U should be also removed from the validators of resources R in the
   * applications A.
   */
  @Test
  public void aGroupWithAValidatorHasBeenRemovedFromInheritedManagersProfile() {
    // user 1 belongs to the user group 1
    String profileId = setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1", "2"));

    ProfileInst profileToUpdate = getProfileInstById(profileId);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a user group G is playing the reader role for some Resources Manager applications A
   * and some users in the group G are also validators of resources R in those applications A,
   * When the group G is removed from the reader role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aGroupWithoutAnyValidatorHasBeenRemovedFromReadersProfile() {
    setUpProfileInstWith(READER_ROLE, NO_IDS, List.of("1", "2"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(READER_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A
   * and no users in the group G are validators of resources R in those applications A,
   * When the group G is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aGroupWithoutAnyValidatorHasBeenRemovedFromManagersProfile() {
    // group 4 has no validators
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("4", "5"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("5");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A,
   * Given a user U belonging to the group G is also playing explicitly the manager Role for those
   * applications A and he's also validator of some resources R in those applications A,
   * When the user U is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenDirectlyRemovedButItIsInGroupInManagersProfile() {
    // user 1 belongs to user group 1
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user group G is playing buy inheritance the manager role for some Resources Manager
   * applications A,
   * Given a user U belonging to the group G is also playing explicitly the manager Role for those
   * applications A and he's also validator of some resources R in those applications A,
   * When the user U is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenDirectlyRemovedButItIsInGroupInInheritedManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a subgroup G of a user group playing the manager role for some Resources Manager
   * applications A,
   * Given a user U belonging to the subgroup G is also playing explicitly the manager Role for
   * those applications A and he's also validator of some resources R in those applications A,
   * When the user U is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenDirectlyRemovedButItIsInSubGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), List.of("3"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A,
   * Given a user U is playing the manager Role for those applications A and he doesn't belong
   * to the group G and he's also validator of some resources R in those applications A,
   * When the user U is removed from the manager role,
   * Then the users U should be also removed from the validators of resources R in the
   * applications A.
   */
  @Test
  public void aValidatorHasBeenDirectlyRemovedAndItIsNotInGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), List.of("5"));

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A,
   * Given a user U is playing the manager Role for those applications A and he doesn't belong
   * to the group G and he's also validator of some resources R in those applications A,
   * When the user U is removed from the manager role,
   * Then the users U should be also removed from the validators of resources R in the
   * applications A.
   */
  @Test
  public void aValidatorHasBeenDirectlyRemovedAndItIsNotInGroupInInheritedManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("5"));
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), NO_IDS);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, resources);
  }

  /**
   * Given a group G is playing the manager role for some Resources Manager applications A and some
   * users U in this group G are validators of some resources R in those applications A,
   * When the group G is removed from the manager role for A,
   * Then the users U should be also removed from the validators of R in the applications A.
   */
  @Test
  public void aGroupInManagerProfileWithAValidatorHasBeenRemoved() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, actualResources);
  }

  /**
   * Given a group G is playing the manager role for some Resources Manager applications A and some
   * users U in this group G are validators of some resources R in those applications A,
   * When the group G is removed from the manager role for A,
   * Then the users U should be also removed from the validators of R in the applications A.
   */
  @Test
  public void aGroupInInheritedManagerProfileWithAValidatorHasBeenRemoved() {
    String profileId = setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));

    ProfileInst profileToUpdate = getProfileInstById(profileId);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorIsRemoved(1, actualResources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A,
   * Given a user belonging to the group G is also playing explicitly the manager Role for those
   * applications A and he's also validator of some resources R in those applications A,
   * When the group G is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user group G is playing the manager role for some Resources Manager applications A,
   * Given a user belonging to the group G is also playing by inheritance the manager Role for those
   * applications A and he's also validator of some resources R in those applications A,
   * When the group G is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsInInheritedManagersProfile() {
    setUpInheritedProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), NO_IDS);
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given two user groups are playing the manager role for some Resources Manager applications A,
   * Given a user belonging to these two groups is also validator of some resources R in those
   * applications A,
   * When only one of these two groups is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsAnotherGroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1", "4"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given two user groups G1 and G2 are playing the manager role for some Resources Manager
   * applications A,
   * Given a user is belonging to the group G1 and to a subgroup of G2, and he's also validator of
   * some resources R in those applications A,
   * When the group G1 is removed from the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsInASubgroupInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, NO_IDS, List.of("1", "3"));
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllGroups().remove("1");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }

  /**
   * Given a user is playing the manager role for some Resources Manager applications A
   * and this user is also a validator of some resources R in those applications A,
   * When he's removed from the manager role whereas in the same time a group to which he belongs
   * is added in the manager role,
   * Then nothing should be done with the validators of the resources R in the applications A.
   */
  @Test
  public void aValidatorHasBeenDirectlyRemovedButHeIsInSuperGroupAddedInManagersProfile() {
    setUpProfileInstWith(RESPONSIBLE_ROLE, List.of("0", "1", "2"), NO_IDS);
    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    ProfileInst profileToUpdate = getProfileInst(RESPONSIBLE_ROLE);
    profileToUpdate.getAllUsers().remove("1");
    profileToUpdate.getAllGroups().add("4");
    updateProfileInst(profileToUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    assertValidatorsEquality(actualResources, expectedResources);
  }
}
  