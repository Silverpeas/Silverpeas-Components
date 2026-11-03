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
  private org.silverpeas.components.discussion.search.DiscussionSearchService search;
  @TestManagedMock
  private ConversationService service;
  @TestManagedMock
  private org.silverpeas.components.discussion.web.DiscussionParticipantService participants;
  @TestManagedMock
  private org.silverpeas.components.discussion.web.DiscussionInstanceInfoService info;
  @TestManagedMock
  private org.silverpeas.components.discussion.service.DiscussionMentionService mentions;
  @TestManagedMock
  private org.silverpeas.components.discussion.web.DiscussionPublicationService publications;
  @TestManagedMock
  private org.silverpeas.components.discussion.service.DiscussionTypingService typing;
  @TestManagedMock
  private org.silverpeas.components.discussion.service.DiscussionNotificationSettings notificationSettings;
  @TestManagedMock
  private org.silverpeas.components.discussion.service.DiscussionChannelService channels;
  @TestManagedMock
  private org.silverpeas.components.discussion.service.DiscussionEventService eventService;
  private DiscussionWebController controller;
  private DiscussionWebRequestContext context;
  private HttpRequest request;

  @BeforeEach
  void prepare() {
    var main = mock(MainSessionController.class);
    when(main.getFavoriteLanguage()).thenReturn("fr");
    var component = mock(ComponentContext.class);
    when(component.getCurrentComponentName()).thenReturn("discussion");
    when(service.history(anyString(), anyLong(), anyInt())).thenReturn(new MessagePage(List.of(), 1, false, 0, 0));
    controller = new DiscussionWebController(main, component);
    context = mock(DiscussionWebRequestContext.class);
    request = mock(HttpRequest.class);
    when(context.getRequest()).thenReturn(request);
    when(context.getChannelScope()).thenReturn("main");
    var channel = mock(org.silverpeas.components.discussion.model.Conversation.class);
    when(channel.getScopeKey()).thenReturn("main");
    when(channels.select(anyString(), anyString())).thenReturn(channel);
    when(channels.selectMessage(anyString(), anyString())).thenReturn(channel);
    when(context.getComponentInstanceId()).thenReturn("discussion1");
    when(context.getUser()).thenReturn(mock(UserDetail.class));
    when(context.getHighestUserRole()).thenReturn(SilverpeasRole.WRITER);
    when(context.getPathVariables()).thenReturn(Map.of("id", "message"));
    var root = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
    when(root.getId()).thenReturn("message");
    when(service.threadRoot("discussion1", "message")).thenReturn(root);
  }

  @Test
  void eventCreationUsesTheAuthenticatedTimeZoneAndCurrentChannelOrThread() {
    var zone = java.time.ZoneId.of("Europe/Paris");
    when(context.getZoneId()).thenReturn(zone);
    when(request.getParameter("calendar")).thenReturn("calendar1");
    when(request.getParameter("title")).thenReturn("Réunion");
    when(request.getParameter("start")).thenReturn("2026-11-03T10:00");
    when(request.getParameter("end")).thenReturn("2026-11-03T11:00");
    when(request.getParameter("threadRootId")).thenReturn("root");
    var created = new org.silverpeas.components.discussion.service.DiscussionEventService.CreatedEvent("event", "/event");
    when(eventService.create(eq("discussion1"), eq("root"), any(), eq(zone))).thenReturn(created);
    controller.channelCreateEvent(context);
    verify(request).setAttribute("createdEvent", created);
    var input = org.mockito.ArgumentCaptor.forClass(org.silverpeas.components.discussion.service.DiscussionEventInput.class);
    verify(eventService).create(eq("discussion1"), eq("root"), input.capture(), eq(zone));
    assertEquals("calendar1", input.getValue().calendarId());
    assertEquals("2026-11-03T10:00", input.getValue().timing().start());
    controller.channelEventOptions(context);
    verify(eventService).options(eq("discussion1"), eq(zone), any());
    when(request.getParameter("title")).thenReturn(" ");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.createEvent(context)).getResponse().getStatus());
  }

  @Test
  void refreshedChannelListsExposeManagementControlsOnlyToManagers() {
    controller.channelList(context);
    verify(request).setAttribute("canManageChannels", false);
    when(context.getHighestUserRole()).thenReturn(SilverpeasRole.ADMIN);
    controller.channelList(context);
    verify(request).setAttribute("canManageChannels", true);
  }

  @Test
  void canonicalNotificationRequestSelectsItsMessageChannelBeforeRendering() {
    when(context.getResponse()).thenReturn(mock(jakarta.servlet.http.HttpServletResponse.class));
    controller.beforeRequestProcessing(context);
    verify(channels).selectMessage("discussion1", "message");
    verify(context).setChannelScope("main");
    when(context.getPathVariables()).thenReturn(Map.of("channel", "projects", "id", "message"));
    controller.beforeRequestProcessing(context);
    verify(channels).select("discussion1", "projects");
    verify(context).setChannelScope("projects");
  }

  @Test
  void invalidChannelNotificationPreferenceReturnsBadRequest() {
    when(request.getParameter("mode")).thenReturn("unknown");
    doThrow(IllegalArgumentException.class).when(channels).notifications("discussion1", "unknown");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.channelNotifications(context)).getResponse().getStatus());
  }

  @Test
  void threadViewsScopeHistoryUpdatesAndPublishingToTheValidatedRoot() {
    when(context.getPathVariables()).thenReturn(Map.of("root", "message", "id", "reply"));
    var page = new MessagePage(List.of(), 1, false, 20, 4);
    when(service.threadHistory("discussion1", "message", Long.MAX_VALUE, 30)).thenReturn(page);
    when(service.threadUpdates("discussion1", "message", 0, List.of())).thenReturn(page);
    controller.thread(context);
    controller.threadHistory(context);
    controller.threadUpdates(context);
    when(request.getParameter("body")).thenReturn("Une réponse");
    controller.reply(context);
    verify(service).reply("discussion1", "message", "Une réponse", List.of(), List.of());
    verify(context).addRedirectVariable("root", "message");
    verify(request, atLeastOnce()).setAttribute(eq("threadRoot"), any());
    verify(request, times(3)).setAttribute("messagePage", page);
  }

  @Test
  void directReplyLinksOpenTheThreadAndThreadActionsRejectForeignRoots() {
    var root = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
    when(root.getId()).thenReturn("root");
    when(service.threadRoot("discussion1", "message")).thenReturn(root);
    when(service.historyAround("discussion1", "root")).thenReturn(new MessagePage(List.of(), 1, false, 20, 1));
    controller.message(context);
    verify(request).setAttribute("initialThreadRootId", "root");
    verify(request).setAttribute("initialThreadMessageId", "message");
    verify(request).setAttribute("focusedMessageId", "root");
    verify(request, never()).setAttribute(eq("threadRoot"), any());
    verify(service).historyAround("discussion1", "root");
    when(context.getPathVariables()).thenReturn(Map.of("root", "foreign", "id", "message"));
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.threadEdit(context)).getResponse().getStatus());
    verify(service, never()).edit(anyString(), anyString(), anyLong(), any());
    when(context.getPathVariables()).thenReturn(Map.of("root", "root", "id", "message"));
    when(request.getParameter("version")).thenReturn("0");
    when(request.getParameter("body")).thenReturn("edited");
    controller.threadEdit(context);
    controller.threadDelete(context);
    verify(service).edit("discussion1", "message", 0, "edited");
    verify(service).delete("discussion1", "message", 0);
  }

  @Test
  void searchUsesOnlyCurrentInstanceAndRejectsInvalidPagination() {
    var page = new org.silverpeas.components.discussion.search.DiscussionSearchPage(List.of(), 0, 0);
    when(request.getParameter("q")).thenReturn("projet");
    when(request.getParameter("offset")).thenReturn("20");
    when(search.search("discussion1", "projet", 20, false)).thenReturn(page);
    controller.search(context);
    verify(request).setAttribute("searchPage", page);
    when(request.getParameter("offset")).thenReturn("-1");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.search(context)).getResponse().getStatus());
    when(request.getParameter("offset")).thenReturn("2147483648");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.search(context)).getResponse().getStatus());
  }

  @Test
  void panelNotificationTargetLoadsReplyContextAndRejectsMessagesFromAnotherThread() {
    when(context.getPathVariables()).thenReturn(Map.of("root", "message"));
    when(request.getParameter("message")).thenReturn("old-reply");
    var root = service.threadRoot("discussion1", "message");
    when(service.threadRoot("discussion1", "old-reply")).thenReturn(root);
    var page = new MessagePage(List.of(), 3, false, 100, 2);
    when(service.historyAround("discussion1", "old-reply")).thenReturn(page);
    controller.thread(context);
    verify(request).setAttribute("messagePage", page);
    verify(request).setAttribute("focusedMessageId", "old-reply");
    verify(service, never()).threadHistory(anyString(), anyString(), anyLong(), anyInt());
    var foreign = mock(org.silverpeas.components.discussion.model.DiscussionMessage.class);
    when(foreign.getId()).thenReturn("other-root");
    when(service.threadRoot("discussion1", "old-reply")).thenReturn(foreign);
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.thread(context)).getResponse().getStatus());
  }

  @Test
  void nativeGlobalSearchOpensMessageThroughExistingAuthorizedRoute() {
    when(request.getParameter("Id")).thenReturn("message-uuid");
    controller.searchResult(context);
    verify(context).addRedirectVariable("id", "message-uuid");
    when(request.getParameter("Id")).thenReturn("../elsewhere");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.searchResult(context)).getResponse().getStatus());
  }

  @Test
  void homepageLoadsOnlyTheLatestPageAndDoesNotMarkItRead() {
    var page = new MessagePage(List.of(), Long.MAX_VALUE, false, 0, 0);
    when(service.history("discussion1", Long.MAX_VALUE, 30)).thenReturn(page);
    controller.home(context);
    verify(request).setAttribute("messagePage", page);
    verify(request).setAttribute("discussionInstanceId", "discussion1");
    verify(request).setAttribute("defaultAvatarPath", org.silverpeas.core.admin.user.model.User.DEFAULT_AVATAR_PATH);
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
  void liveUpdatesUseSessionScopeAndRejectMalformedRequests() {
    when(request.getParameter("after")).thenReturn("42");
    when(request.getParameter("ids")).thenReturn("m1,m2");
    var page = new MessagePage(List.of(), 0, false, 42, 1);
    when(service.updates("discussion1", 42, List.of("m1", "m2"))).thenReturn(page);
    controller.updates(context);
    verify(request).setAttribute("messagePage", page);
    when(request.getParameter("after")).thenReturn("oops");
    assertEquals(400, assertThrows(WebApplicationException.class,
        () -> controller.updates(context)).getResponse().getStatus());
    when(request.getParameter("ids")).thenReturn("x".repeat(4101));
    assertEquals(400, assertThrows(WebApplicationException.class,
        () -> controller.updates(context)).getResponse().getStatus());
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
  @Test
  void likeRequiresAnExplicitBooleanAndUsesTheSessionInstance() {
    when(request.getParameter("liked")).thenReturn("true");
    controller.like(context);
    verify(service).setLike("discussion1", "message", true);
    when(request.getParameter("liked")).thenReturn("invalid");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.like(context))
        .getResponse().getStatus());
  }
  @Test
  void readOnlyViewsDoNotExposeTheMentionDirectory() {
    when(context.getHighestUserRole()).thenReturn(SilverpeasRole.READER);
    controller.home(context);
    verifyNoInteractions(mentions);
    verify(request).setAttribute("mentionCandidates", List.of());
  }
  @Test
  void notificationRoutesProvideTheFocusedMessageWithoutRelyingOnAUrlFragment() {
    var page = new MessagePage(List.of(), 0, false, 0, 0);
    when(service.historyAround("discussion1", "message")).thenReturn(page);
    controller.message(context);
    verify(request).setAttribute("focusedMessageId", "message");
    verify(request).setAttribute("messagePage", page);
  }

  @Test
  void visibleAcknowledgmentsAreScopedAndReturnTheUnreadCount() {
    when(request.getParameter("ids")).thenReturn("m1,m2");
    when(service.markVisible("discussion1", List.of("m1", "m2"))).thenReturn(3L);
    controller.visible(context);
    verify(request).setAttribute("unreadCount", 3L);
    when(request.getParameter("ids")).thenReturn(null);
    assertEquals(400, assertThrows(WebApplicationException.class,
        () -> controller.visible(context)).getResponse().getStatus());
  }

  @Test
  void citationRefreshUsesTheSessionInstanceAndPreservesAccessErrors() {
    controller.publications(context);
    verify(publications).list("discussion1", "fr");
    when(publications.list("discussion1", "fr")).thenThrow(SecurityException.class);
    assertEquals(403, assertThrows(WebApplicationException.class,
        () -> controller.publications(context)).getResponse().getStatus());
  }

  @Test
  void multipartPublicationValidatesImagesBeforeCallingTheConversationService() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn("multipart/form-data; boundary=example");
    when(request.getParameter("body")).thenReturn("");
    var file = mock(org.silverpeas.core.util.file.FileItem.class);
    when(file.getFieldName()).thenReturn("images");
    when(file.getFileName()).thenReturn("photo.png");
    var output = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", output);
    byte[] png = output.toByteArray();
    when(file.getSize()).thenReturn((long) png.length);
    when(file.getInputStream()).thenAnswer(call -> new java.io.ByteArrayInputStream(png));
    when(request.getFileItems()).thenReturn(List.of(file));
    controller.publish(context);
    verify(service).publish(eq("discussion1"), eq(""), argThat(images -> images.size() == 1 && "image/png".equals(images.getFirst().contentType())));
    clearInvocations(service);
    when(request.getFileItems()).thenReturn(java.util.Collections.nCopies(5, file));
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.publish(context)).getResponse().getStatus());
    verifyNoInteractions(service);
  }

  @Test
  void fileUploadsAndTheSidebarUseTheCurrentInstance() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn("multipart/form-data; boundary=example");
    when(request.getParameter("body")).thenReturn("");
    var file = mock(org.silverpeas.core.util.file.FileItem.class);
    when(file.getFieldName()).thenReturn("files");
    when(file.getFileName()).thenReturn("budget.pdf");
    when(file.getSize()).thenReturn(3L);
    when(file.getInputStream()).thenAnswer(call -> new java.io.ByteArrayInputStream(new byte[]{1,2,3}));
    when(request.getFileItems()).thenReturn(List.of(file));
    controller.publish(context);
    verify(service).publish(eq("discussion1"), eq(""), eq(List.of()), argThat(files -> files.size() == 1 && "budget.pdf".equals(files.getFirst().filename())));
    controller.sharedFiles(context);
    verify(service).sharedFiles("discussion1");
    clearInvocations(service);
    when(request.getFileItems()).thenReturn(java.util.Collections.nCopies(5, file));
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.publish(context)).getResponse().getStatus());
    verifyNoInteractions(service);
  }

  @Test
  void notificationSettingsArePersonalAndRejectNonBooleanValues() {
    when(request.getParameter("enabled")).thenReturn("false");
    controller.saveNotificationPreferences(context);
    verify(notificationSettings).save("discussion1", false);
    controller.notificationPreferences(context);
    verify(notificationSettings).current("discussion1");
    when(request.getParameter("enabled")).thenReturn("invalid");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.saveNotificationPreferences(context)).getResponse().getStatus());
  }

  @Test
  void pinRoutesUseTheAuthorizedInstanceAndRejectInvalidFlags() {
    when(request.getParameter("pinned")).thenReturn("true");
    controller.pin(context);
    verify(service).setPinned("discussion1", "message", true);
    controller.pins(context);
    verify(service).pinnedMessages("discussion1");
    when(request.getParameter("pinned")).thenReturn("invalid");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.pin(context)).getResponse().getStatus());
  }

  @Test
  void typingUsesTheAuthenticatedHttpSessionAndRejectsInvalidPayloads() {
    when(request.getParameter("active")).thenReturn("true");
    when(request.getParameter("tab")).thenReturn("tab1");
    var session = mock(jakarta.servlet.http.HttpSession.class);
    when(session.getId()).thenReturn("authenticated");
    when(request.getSession()).thenReturn(session);
    controller.typing(context);
    verify(typing).typing("discussion1", "authenticated:tab1", true);
    when(request.getParameter("active")).thenReturn("bad");
    assertEquals(400, assertThrows(WebApplicationException.class, () -> controller.typing(context)).getResponse().getStatus());
  }

  @Test
  void everyNavigationAndMutationRouteRequiresAWriter() {
    for (var method : DiscussionWebController.class.getDeclaredMethods()) {
      if (method.isAnnotationPresent(jakarta.ws.rs.GET.class)
          || method.isAnnotationPresent(jakarta.ws.rs.POST.class)) {
        var restriction = method.getAnnotation(
            org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess.class);
        assertNotNull(restriction, method.getName());
        assertEquals(java.util.Set.of("createChannel", "updateChannel").contains(method.getName())
            ? SilverpeasRole.ADMIN : SilverpeasRole.WRITER, restriction.value(), method.getName());
      }
    }
  }

}
