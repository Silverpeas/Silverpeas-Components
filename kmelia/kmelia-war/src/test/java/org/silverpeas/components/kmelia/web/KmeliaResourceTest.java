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
package org.silverpeas.components.kmelia.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the checks guarding the writing of the publications. The authorization performed by the
 * REST framework only validates the access of the user to the component instance referred by the
 * URL, whatever the role played in it, whereas the publication to update is entirely defined by the
 * request body.
 * @author mmoquillon
 */
class KmeliaResourceTest {

  private static final String THE_INSTANCE = "kmelia1";
  private static final String ANOTHER_INSTANCE = "kmelia2";

  private KmeliaResource4Test resource;

  @BeforeEach
  void setup() {
    resource = new KmeliaResource4Test();
  }

  @Test
  void aPublicationOfAnotherInstanceCannotBeUpdated() {
    resource.setRole(SilverpeasRole.ADMIN);
    assertThat(forbiddenStatusOf(() -> resource.checkIsUpdatable(publicationIn(ANOTHER_INSTANCE))),
        is(Response.Status.FORBIDDEN));
  }

  @Test
  void aPublicationOfTheInstanceCanBeUpdatedByAWriter() {
    resource.setRole(SilverpeasRole.WRITER);
    assertDoesNotThrow(() -> resource.checkIsUpdatable(publicationIn(THE_INSTANCE)));
  }

  /**
   * Accessing a component instance, which is all the REST framework validates, doesn't entitle to
   * write into it.
   */
  @Test
  void aPublicationCannotBeUpdatedByAUserWithoutTheWriterRole() {
    resource.setRole(SilverpeasRole.READER);
    assertThat(forbiddenStatusOf(() -> resource.checkIsUpdatable(publicationIn(THE_INSTANCE))),
        is(Response.Status.FORBIDDEN));
  }

  @Test
  void aPublicationCannotBeUpdatedByAUserPlayingNoRole() {
    resource.setRole(null);
    assertThat(forbiddenStatusOf(() -> resource.checkIsUpdatable(publicationIn(THE_INSTANCE))),
        is(Response.Status.FORBIDDEN));
  }

  @Test
  void writingIsGrantedToTheRolesAboveTheWriterOne() {
    for (final SilverpeasRole role : new SilverpeasRole[]{SilverpeasRole.WRITER,
        SilverpeasRole.PUBLISHER, SilverpeasRole.ADMIN}) {
      resource.setRole(role);
      assertDoesNotThrow(resource::checkIsWriter, "the role " + role + " should be granted");
    }
  }

  @Test
  void writingIsDeniedToTheRolesBelowTheWriterOne() {
    for (final SilverpeasRole role : new SilverpeasRole[]{SilverpeasRole.READER,
        SilverpeasRole.USER}) {
      resource.setRole(role);
      assertThat("the role " + role + " should be denied",
          forbiddenStatusOf(resource::checkIsWriter), is(Response.Status.FORBIDDEN));
    }
  }

  private PublicationDetail publicationIn(final String instanceId) {
    final PublicationDetail publication = mock(PublicationDetail.class);
    when(publication.getPK()).thenReturn(new PublicationPK("6", instanceId));
    return publication;
  }

  private Response.StatusType forbiddenStatusOf(final Runnable check) {
    final WebApplicationException error = assertThrows(WebApplicationException.class, check::run);
    return error.getResponse().getStatusInfo();
  }

  /**
   * The component instance and the role are the ones the REST framework resolves from the URL and
   * from the profiles of the user, which are out of the scope of the checks verified here.
   */
  private static class KmeliaResource4Test extends KmeliaResource {

    private SilverpeasRole role;

    void setRole(final SilverpeasRole role) {
      this.role = role;
    }

    @Override
    public String getComponentId() {
      return THE_INSTANCE;
    }

    @Override
    public SilverpeasRole getHighestUserRole() {
      return role;
    }
  }
}
