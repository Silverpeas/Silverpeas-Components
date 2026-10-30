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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.control;

import java.util.List;
import java.util.Map;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.service.ConversationService;
import org.silverpeas.components.discussion.service.MessagePage;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionWebControllerTest {
  @TestManagedMock
  private ConversationService service;
  @TestManagedMock
  private org.silverpeas.components.discussion.web.DiscussionParticipantService participants;
  private DiscussionWebController controller;
  private DiscussionWebRequestContext context;
  private HttpRequest request;

  @BeforeEach
  void prepare() {
    var main = mock(MainSessionController.class);
    when(main.getFavoriteLanguage()).thenReturn("fr");
    var component = mock(ComponentContext.class);
    when(component.getCurrentComponentName()).thenReturn("discussion");
    controller = new DiscussionWebController(main, component);
    context = mock(DiscussionWebRequestContext.class);
    request = mock(HttpRequest.class);
    when(context.getRequest()).thenReturn(request);
    when(context.getComponentInstanceId()).thenReturn("discussion1");
    when(context.getUser()).thenReturn(mock(UserDetail.class));
    when(context.getHighestUserRole()).thenReturn(SilverpeasRole.WRITER);
    when(context.getPathVariables()).thenReturn(Map.of("id", "message"));
  }

  @Test
  void homepageLoadsOnlyTheLatestPageAndDoesNotMarkItRead() {
    var page = new MessagePage(List.of(), Long.MAX_VALUE, false, 0, 0);
    when(service.history("discussion1", Long.MAX_VALUE, 30)).thenReturn(page);
    controller.home(context);
    verify(request).setAttribute("messagePage", page);
    verify(participants).list("discussion1");
    verify(request).setAttribute("canWrite", true);
    verify(service, never()).markRead(anyString(), anyLong());
  }

  @Test
  void olderHistoryUsesTheSubmittedCursorAndRejectsMalformedNumbers() {
    when(request.getParameter("before")).thenReturn("21");
    controller.history(context);
    verify(service).history("discussion1", 21, 30);
    when(request.getParameter("before")).thenReturn("not-a-number");
    assertEquals(400, assertThrows(WebApplicationException.class,
        () -> controller.history(context)).getResponse().getStatus());
  }

  @Test
  void mutationsUseTheInstanceFromTheSessionAndTheVersionFromTheForm() {
    when(request.getParameter("body")).thenReturn("body");
    when(request.getParameter("version")).thenReturn("3");
    when(request.getParameter("sequence")).thenReturn("42");
    controller.publish(context);
    controller.edit(context);
    controller.delete(context);
    controller.markRead(context);
    verify(service).publish("discussion1", "body");
    verify(service).edit("discussion1", "message", 3, "body");
    verify(service).delete("discussion1", "message", 3);
    verify(service).markRead("discussion1", 42);
  }
  @Test
  void businessFailuresKeepTheirHttpMeaning() {
    doThrow(new SecurityException()).when(service).publish("discussion1", "body");
    when(request.getParameter("body")).thenReturn("body");
    assertEquals(403, assertThrows(WebApplicationException.class,
        () -> controller.publish(context)).getResponse().getStatus());
    doThrow(new IllegalArgumentException()).when(service).markRead("discussion1", 0);
    when(request.getParameter("sequence")).thenReturn("0");
    assertEquals(400, assertThrows(WebApplicationException.class,
        () -> controller.markRead(context)).getResponse().getStatus());
    doThrow(new jakarta.persistence.OptimisticLockException()).when(service)
        .delete("discussion1", "message", 3);
    when(request.getParameter("version")).thenReturn("3");
    assertEquals(409, assertThrows(WebApplicationException.class,
        () -> controller.delete(context)).getResponse().getStatus());
  }

  @Test
  void participantRefreshChecksPermissionsWithoutReloadingHistory() {
    controller.participants(context);
    verify(participants).list("discussion1");
    verifyNoInteractions(service);
    when(participants.list("discussion1")).thenThrow(SecurityException.class);
    assertEquals(403, assertThrows(WebApplicationException.class,
        () -> controller.participants(context)).getResponse().getStatus());
  }

  @Test
  void discussionResponsesAreNotStoredInBrowserCaches() {
    var response = mock(jakarta.servlet.http.HttpServletResponse.class);
    when(context.getResponse()).thenReturn(response);
    controller.beforeRequestProcessing(context);
    verify(response).setHeader("Cache-Control", "no-store");
  }

}
