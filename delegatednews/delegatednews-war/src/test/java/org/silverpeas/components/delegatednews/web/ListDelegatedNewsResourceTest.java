/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.components.delegatednews.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.SilverpeasRole;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the check guarding the modification and the deletion of the delegated news, reserved to
 * the managers of the application. The authorization performed by the REST framework only validates
 * the access of the user to the component instance, whatever the role played in it.
 * @author mmoquillon
 */
class ListDelegatedNewsResourceTest {

  private ListDelegatedNewsResource4Test resource;

  @BeforeEach
  void setup() {
    resource = new ListDelegatedNewsResource4Test();
  }

  @Test
  void theDelegatedNewsAreManagedByTheManagers() {
    resource.setRole(SilverpeasRole.ADMIN);
    assertDoesNotThrow(resource::checkIsManager);
  }

  /**
   * Unlike the writing of a contribution, managing the delegated news isn't granted to the roles
   * below the manager one: publishing a news is decided here, not written.
   */
  @Test
  void theDelegatedNewsArentManagedByTheOtherRoles() {
    for (final SilverpeasRole role : new SilverpeasRole[]{SilverpeasRole.PUBLISHER,
        SilverpeasRole.WRITER, SilverpeasRole.READER, SilverpeasRole.USER}) {
      resource.setRole(role);
      assertThat("the role " + role + " should be denied", forbiddenStatusOfTheCheck(),
          is(Response.Status.FORBIDDEN));
    }
  }

  @Test
  void theDelegatedNewsArentManagedByAUserPlayingNoRole() {
    resource.setRole(null);
    assertThat(forbiddenStatusOfTheCheck(), is(Response.Status.FORBIDDEN));
  }

  private Response.StatusType forbiddenStatusOfTheCheck() {
    final WebApplicationException error =
        assertThrows(WebApplicationException.class, resource::checkIsManager);
    return error.getResponse().getStatusInfo();
  }

  /**
   * The role is the one the REST framework resolves from the profiles of the user on the component
   * instance, which is out of the scope of the check verified here.
   */
  private static class ListDelegatedNewsResource4Test extends ListDelegatedNewsResource {

    private SilverpeasRole role;

    void setRole(final SilverpeasRole role) {
      this.role = role;
    }

    @Override
    public SilverpeasRole getHighestUserRole() {
      return role;
    }
  }
}
