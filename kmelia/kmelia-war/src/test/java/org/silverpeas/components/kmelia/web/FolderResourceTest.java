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
import org.silverpeas.components.kmelia.service.KmeliaService;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.web.rs.RESTRequestContext;
import org.silverpeas.core.webapi.node.NodeAttrEntity;
import org.silverpeas.core.webapi.node.NodeEntity;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the creation of a folder through the REST folder resource of Kmelia. They guard the
 * resource against the creation of a folder by a user that isn't a folder administrator and against
 * the usurpation of the identity of another user as the creator of the folder.
 * @author mmoquillon
 */
class FolderResourceTest {

  private static final String COMPONENT_ID = "kmelia42";
  private static final String REQUESTER_ID = "5";
  private static final String ANOTHER_USER_ID = "9";
  private static final NodePK PARENT_PK = new NodePK("0", COMPONENT_ID);

  private KmeliaService kmeliaService;
  private FolderResource resource;

  @BeforeEach
  void setUpResourceOnBehalfOfTheRequester() {
    kmeliaService = mock(KmeliaService.class);
    resource = spy(new FolderResource());
    doReturn(COMPONENT_ID).when(resource).getComponentId();
    doReturn(kmeliaService).when(resource).getKmeliaService();
    final User requester = mock(User.class);
    when(requester.getId()).thenReturn(REQUESTER_ID);
    final RESTRequestContext context = mock(RESTRequestContext.class);
    when(context.getUser()).thenReturn(requester);
    doReturn(context).when(resource).getSilverpeasContext();
  }

  @Test
  void aReaderCannotCreateAFolder() {
    when(kmeliaService.getUserTopicProfile(any(NodePK.class), eq(REQUESTER_ID)))
        .thenReturn(SilverpeasRole.USER.getName());

    final WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> resource.createFolderInto(PARENT_PK, folderNamed("A new folder")));

    assertThat(exception.getResponse().getStatus(), is(Status.FORBIDDEN.getStatusCode()));
    verify(kmeliaService, never()).createTopic(any(), any(), any(), any(), any(), any());
  }

  @Test
  void aWriterCannotCreateAFolder() {
    when(kmeliaService.getUserTopicProfile(any(NodePK.class), eq(REQUESTER_ID)))
        .thenReturn(SilverpeasRole.WRITER.getName());

    final WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> resource.createFolderInto(PARENT_PK, folderNamed("A new folder")));

    assertThat(exception.getResponse().getStatus(), is(Status.FORBIDDEN.getStatusCode()));
    verify(kmeliaService, never()).createTopic(any(), any(), any(), any(), any(), any());
  }

  @Test
  void aFolderAdministratorCreatesTheFolderAsItselfWhateverTheProvidedCreator() {
    when(kmeliaService.getUserTopicProfile(any(NodePK.class), eq(REQUESTER_ID)))
        .thenReturn(SilverpeasRole.ADMIN.getName());
    when(kmeliaService.getFolderChildren(any(NodePK.class), eq(REQUESTER_ID)))
        .thenReturn(List.of());
    final NodeDetail created = mock(NodeDetail.class);
    when(kmeliaService.createTopic(COMPONENT_ID, "0", null, REQUESTER_ID, "A new folder", "a desc"))
        .thenReturn(created);

    // the entity claims another user as the creator: it must be ignored
    final NodeDetail node =
        resource.createFolderInto(PARENT_PK, folderNamed("A new folder", ANOTHER_USER_ID, "a desc"));

    assertThat(node, is(sameInstance(created)));
    verify(kmeliaService)
        .createTopic(eq(COMPONENT_ID), eq("0"), isNull(), eq(REQUESTER_ID), eq("A new folder"),
            eq("a desc"));
  }

  @Test
  void aFolderWhoseNameIsAlreadyUsedBySiblingIsRejected() {
    when(kmeliaService.getUserTopicProfile(any(NodePK.class), eq(REQUESTER_ID)))
        .thenReturn(SilverpeasRole.ADMIN.getName());
    final NodeDetail sibling = mock(NodeDetail.class);
    when(sibling.getName()).thenReturn("Existing folder");
    when(kmeliaService.getFolderChildren(any(NodePK.class), eq(REQUESTER_ID)))
        .thenReturn(List.of(sibling));

    final WebApplicationException exception = assertThrows(WebApplicationException.class,
        () -> resource.createFolderInto(PARENT_PK, folderNamed("Existing folder")));

    assertThat(exception.getResponse().getStatus(), is(Status.NOT_ACCEPTABLE.getStatusCode()));
    verify(kmeliaService, never()).createTopic(any(), any(), any(), any(), any(), any());
  }

  private NodeEntity folderNamed(final String name) {
    return folderNamed(name, null, "");
  }

  private NodeEntity folderNamed(final String name, final String creatorId,
      final String description) {
    final NodeEntity entity = mock(NodeEntity.class);
    when(entity.getText()).thenReturn(name);
    final NodeAttrEntity attr = mock(NodeAttrEntity.class);
    when(attr.getDescription()).thenReturn(description);
    when(attr.getCreatorId()).thenReturn(creatorId);
    when(entity.getAttr()).thenReturn(attr);
    return entity;
  }
}
