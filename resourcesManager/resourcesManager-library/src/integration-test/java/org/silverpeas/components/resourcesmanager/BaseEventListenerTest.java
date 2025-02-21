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

import org.silverpeas.components.resourcesmanager.model.Resource;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.ProfileInstManager;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class of all integration tests about the event listeners.
 * @author mmoquillon
 */
public abstract class BaseEventListenerTest {

  public static final String RESPONSIBLE_ROLE = "responsable";
  public static final String READER_ROLE = "publisher";
  public static final List<String> NO_IDS = List.of();
  public static final String INSTANCE_ID = "resourcesManager42";

  @Inject
  private ProfileInstManager profileInstManager;

  public ProfileInst getProfileInst(String roleName) {
    String id;
    switch (roleName) {
      case "admin":
        id = "1";
        break;
      case RESPONSIBLE_ROLE:
        id = "2";
        break;
      default:
        id = "3";
        break;
    }
    return getProfileInstById(id);
  }

  public ProfileInst getProfileInstById(String id) {
    return Transaction.performInOne(() -> {
      try {
        return profileInstManager.getProfileInst(id, false);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
  }

  public void updateProfileInst(ProfileInst profileInst) {
    Transaction.performInOne(() -> {
      try {
        profileInstManager.updateProfileInst(profileInst);
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
      return null;
    });
  }

  private String createProfileInst(ProfileInst profileInst) {
    return Transaction.performInOne(() -> {
      try {
        return profileInstManager.createProfileInst(profileInst, profileInst.getComponentFatherId());
      } catch (AdminException e) {
        throw new SilverpeasRuntimeException(e);
      }
    });
  }

  public void setUpProfileInstWith(String profileName, List<String> users, List<String> groups) {
    ProfileInst initialProfile = getProfileInst(profileName);
    initialProfile.getAllUsers().addAll(users);
    initialProfile.getAllGroups().addAll(groups);
    updateProfileInst(initialProfile);
  }

  public String setUpInheritedProfileInstWith(String profileName, List<String> users,
      List<String> groups) {
    ProfileInst initialProfile = new ProfileInst();
    initialProfile.setComponentFatherId(42);
    initialProfile.setInherited(true);
    initialProfile.setName(profileName);
    initialProfile.setLabel(profileName);
    initialProfile.getAllUsers().addAll(users);
    initialProfile.getAllGroups().addAll(groups);
    return createProfileInst(initialProfile);
  }

  public static void assertValidatorsEquality(List<Resource> actualResources,
      List<Resource> expectedResources) {
    actualResources.forEach(a ->
        expectedResources.stream().filter(e -> a.getId().equals(e.getId())).findFirst()
            .ifPresent(e ->
                assertTrue(a.getManagers().containsAll(e.getManagers()))
            )
    );
  }

  public static void assertValidatorIsRemoved(int validatorId, List<Resource> actualResources) {
    actualResources.forEach(r ->
        assertTrue(r.getManagers().stream().
            allMatch(m -> m.getManagerId() != validatorId)));
  }
}
  