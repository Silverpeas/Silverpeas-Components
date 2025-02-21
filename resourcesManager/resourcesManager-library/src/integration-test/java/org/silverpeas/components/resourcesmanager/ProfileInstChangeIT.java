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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.components.resourcesmanager.service.ResourceService;
import org.silverpeas.components.resourcesmanager.test.WarBuilder4ResourcesManager;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.notification.ProfileInstEventNotifier;
import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.test.integration.rule.DbUnitLoadingRule;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test on the behaviour of the Resource Manager when a user or a group of a user has
 * been remove from the {@link org.silverpeas.core.admin.user.model.SilverpeasRole#PUBLISHER} and
 * the {@link org.silverpeas.core.admin.user.model.SilverpeasRole#WRITER} profiles.
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ProfileInstChangeIT {

  private static final int RESOURCES_MANAGER_ID = 42;
  private static final String INSTANCE_ID = "resourcesManager" + RESOURCES_MANAGER_ID;

  @Inject
  private ProfileInstEventNotifier notifier;
  @Inject
  private ResourceService resourceService;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("service/create-database.sql", "service/resources_dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4ResourcesManager.onWarForTestClass(ProfileInstChangeIT.class).build();
  }

  @Test
  public void aValidatorHasBeenRemovedFromManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setUsers(List.of("0", "1", "2"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setUsers(List.of("0", "2"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    resources.forEach(r ->
        assertTrue(r.getManagers().stream().allMatch(m -> m.getManagerId() != 1)));
  }

  @Test
  public void aNonValidatorHasBeenRemovedFromReadersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.READER.getName());
    beforeUpdate.setLabel("Readers");
    beforeUpdate.setUsers(List.of("0", "1", "2"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.READER.getName());
    afterUpdate.setLabel("Readers");
    afterUpdate.setUsers(List.of("0", "2"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aNonValidatorHasBeenRemovedFromManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setUsers(List.of("0", "1", "2", "11"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setUsers(List.of("0", "1", "2"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aGroupWithAValidatorHasBeenRemovedFromManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setGroups(List.of("1", "2"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setGroups(List.of("2"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> resources = resourceService.getResources(INSTANCE_ID);
    resources.forEach(r ->
        assertTrue(r.getManagers().stream().allMatch(m -> m.getManagerId() != 1)));
  }

  @Test
  public void aGroupWithoutAnyValidatorHasBeenRemovedFromReadersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.READER.getName());
    beforeUpdate.setLabel("Readers");
    beforeUpdate.setGroups(List.of("1", "2"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.READER.getName());
    afterUpdate.setLabel("Readers");
    afterUpdate.setGroups(List.of("2"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aGroupWithoutAnyValidatorHasBeenRemovedFromManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setGroups(List.of("1", "2"));
    beforeUpdate.setComponentFatherId(2);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setGroups(List.of("2"));
    afterUpdate.setComponentFatherId(2);

    List<Resource> expectedResources = resourceService.getResources("resourcesManager2");

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources("resourcesManager2");
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aValidatorHasBeenDirectlyRemovedButItIsInGroupInManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setUsers(List.of("0", "1", "2"));
    beforeUpdate.setGroups(List.of("1"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setUsers(List.of("0", "2"));
    beforeUpdate.setGroups(List.of("1"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aValidatorHasBeenDirectlyRemovedButItIsInSuperGroupInManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setUsers(List.of("0", "1", "2"));
    beforeUpdate.setGroups(List.of("4"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setUsers(List.of("0", "2"));
    beforeUpdate.setGroups(List.of("4"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsInManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setUsers(List.of("0", "1", "2"));
    beforeUpdate.setGroups(List.of("1"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setUsers(List.of("0", "1", "2"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsAnotherGroupInManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setGroups(List.of("1", "3"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    beforeUpdate.setGroups(List.of("3"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aGroupWithAValidatorHasBeenRemovedButTheValidatorIsInASuperGroupInManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setGroups(List.of("1", "4"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    beforeUpdate.setGroups(List.of("4"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

  @Test
  public void aValidatorHasBeenDirectlyRemovedButItIsInSuperGroupAddedInManagersProfile() {
    ProfileInst beforeUpdate = new ProfileInst();
    beforeUpdate.setName(SilverpeasRole.WRITER.getName());
    beforeUpdate.setLabel("Managers");
    beforeUpdate.setUsers(List.of("0", "1", "2"));
    beforeUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);
    ProfileInst afterUpdate = new ProfileInst();
    afterUpdate.setName(SilverpeasRole.WRITER.getName());
    afterUpdate.setLabel("Managers");
    afterUpdate.setUsers(List.of("0", "2"));
    beforeUpdate.setGroups(List.of("4"));
    afterUpdate.setComponentFatherId(RESOURCES_MANAGER_ID);

    List<Resource> expectedResources = resourceService.getResources(INSTANCE_ID);

    notifier.notifyEventOn(ResourceEvent.Type.UPDATE, beforeUpdate, afterUpdate);

    List<Resource> actualResources = resourceService.getResources(INSTANCE_ID);
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(e.getManagers().containsAll(a.getManagers()))
            )
    );
  }

}
  