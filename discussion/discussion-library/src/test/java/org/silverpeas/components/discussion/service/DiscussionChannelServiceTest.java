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
package org.silverpeas.components.discussion.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionReadState;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionReadStateRepository;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionChannelServiceTest {
  @TestManagedMock
  private DiscussionAccess access;
  @TestManagedMock
  private ConversationRepository conversations;
  @TestManagedMock
  private DiscussionReadStateRepository states;
  @TestManagedMock
  private DiscussionChannelContext context;
  @TestManagedMock
  private ConversationService conversationService;
  @org.silverpeas.core.persistence.datasource.PersistOperation
  @TestManagedMock
  private org.silverpeas.core.persistence.datasource.model.jpa.JpaPersistOperation persistOperation;
  @TestedBean
  private DiscussionChannelService service;

  @Test
  void managersCanReactivateAnArchivedChannelWithoutReplacingItsHistoryContainer() {
    var channel = Conversation.channel("discussion1", "Projet", "Description");
    channel.setArchived(true);
    String scope = channel.getScopeKey();
    when(conversations.channel("discussion1", scope, true)).thenReturn(channel);
    when(conversations.channels("discussion1")).thenReturn(List.of(channel));
    var user = mock(User.class);
    when(user.getId()).thenReturn("manager");
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.update("discussion1", scope, "Projet", "Description", false);
      assertFalse(channel.isArchived());
      assertEquals(scope, channel.getScopeKey());
      verify(conversations).save(channel);
      verify(access).require("discussion1", SilverpeasRole.ADMIN);
    }
  }

  @Test
  void channelMetadataValidatesInputAndGeneralCannotBeArchived() {
    var general = new Conversation("discussion1");
    assertTrue(general.isMain());
    assertThrows(IllegalArgumentException.class, () -> general.setArchived(true));
    var channel = Conversation.channel("discussion1", "  Projets  ", " Une description ");
    assertEquals("Projets", channel.getChannelName());
    assertEquals("Une description", channel.getChannelDescription());
    assertNotEquals("main", channel.getScopeKey());
    channel.setArchived(true);
    assertTrue(channel.isArchived());
    assertThrows(IllegalArgumentException.class, () -> channel.rename(" ", ""));
    assertThrows(IllegalArgumentException.class, () -> channel.rename("x".repeat(81), ""));
    assertThrows(IllegalArgumentException.class, () -> channel.rename("Nom", "x".repeat(256)));
  }

  @Test
  void selectionIsBoundToTheInstanceAndCanonicalMessageChannel() {
    var channel = Conversation.channel("discussion1", "Projets", "");
    when(conversations.channel("discussion1", "scope", false)).thenReturn(channel);
    when(conversations.forMessage("discussion1", "message")).thenReturn(channel);
    assertSame(channel, service.select("discussion1", "scope"));
    assertSame(channel, service.selectMessage("discussion1", "message"));
    verify(context, times(2)).select(channel.getScopeKey());
    verify(access, times(2)).require("discussion1", SilverpeasRole.WRITER);
    when(conversations.channel("discussion2", "scope", false)).thenThrow(IllegalArgumentException.class);
    assertThrows(IllegalArgumentException.class, () -> service.select("discussion2", "scope"));
    verify(context, times(2)).select(anyString());
  }

  @Test
  void creatingAndArchivingChannelsRequireAnInstanceManager() {
    when(access.require("discussion1", SilverpeasRole.ADMIN)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.create("discussion1", "Projet", ""));
    assertThrows(SecurityException.class, () -> service.update("discussion1", "scope", "Projet", "", true));
    verifyNoInteractions(conversations);
  }

  @Test
  void duplicateNamesAreRejectedWithoutSaving() {
    var channel = Conversation.channel("discussion1", "Projets", "");
    when(conversations.channels("discussion1")).thenReturn(List.of(channel));
    assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "projets", ""));
    verify(conversations, never()).save(any(Conversation.class));
  }

  @Test
  void notificationPreferenceOnlyChangesTheCurrentUsersChannelState() {
    var channel = mock(Conversation.class);
    when(channel.getId()).thenReturn("channel1");
    when(conversations.current("discussion1", true)).thenReturn(channel);
    var user = mock(User.class);
    when(user.getId()).thenReturn("user1");
    var state = new DiscussionReadState("channel1", "user1");
    when(states.find("channel1", "user1")).thenReturn(Optional.of(state));
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      service.notifications("discussion1", "MENTIONS");
      assertEquals("MENTIONS", state.getNotificationMode());
      verify(states).save(state);
      assertThrows(IllegalArgumentException.class, () -> service.notifications("discussion1", "invalid"));
    }
  }
  @Test
  void creatingAChannelAnnouncesItsLinkInGeneralAndRestoresTheSelectedChannel() {
    var user = mock(User.class, RETURNS_DEEP_STUBS);
    when(user.getId()).thenReturn("manager");
    when(user.getUserPreferences().getLanguage()).thenReturn("fr");
    when(context.getScope()).thenReturn("projects");
    try (var current = mockStatic(User.class);
         var urls = mockStatic(org.silverpeas.core.util.URLUtil.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      urls.when(org.silverpeas.core.util.URLUtil::getApplicationURL).thenReturn("/silverpeas");
      var channel = service.create("discussion1", "Projet [IA]", "Description");
      var order = inOrder(conversations, context, conversationService);
      order.verify(conversations).main("discussion1", true);
      order.verify(conversations).channels("discussion1");
      order.verify(conversations).save(channel);
      order.verify(context).getScope();
      order.verify(context).select("main");
      order.verify(conversationService).publish("discussion1", "Un nouveau canal a été créé : "
          + "[Projet \\[IA\\]](/silverpeas/Rdiscussion/discussion1/channels/" + channel.getScopeKey() + "/Main)");
      order.verify(context).select("projects");
    }
  }

  @Test
  void announcementFailurePropagatesAndRestoresThePreviousChannelForTransactionRollback() {
    var user = mock(User.class, RETURNS_DEEP_STUBS);
    when(user.getId()).thenReturn("manager");
    when(user.getUserPreferences().getLanguage()).thenReturn("fr");
    when(context.getScope()).thenReturn("projects");
    doThrow(new IllegalStateException("Cannot publish")).when(conversationService).publish(eq("discussion1"), anyString());
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(user);
      assertThrows(IllegalStateException.class, () -> service.create("discussion1", "Projet", ""));
      verify(context).select("main");
      verify(context).select("projects");
    }
  }

}
