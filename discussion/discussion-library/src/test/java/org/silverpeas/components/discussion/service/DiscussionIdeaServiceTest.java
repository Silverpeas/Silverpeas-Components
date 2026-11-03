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
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.model.Conversation;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.repository.ConversationRepository;
import org.silverpeas.components.discussion.repository.DiscussionMessageRepository;
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionIdeaServiceTest {
  @TestManagedMock private DiscussionAccess access;
  @TestManagedMock private OrganizationController organization;
  @TestManagedMock private ComponentAccessController componentAccess;
  @TestManagedMock private ConversationRepository conversations;
  @TestManagedMock private DiscussionMessageRepository messages;
  @TestManagedMock private ConversationService discussion;
  @TestManagedMock private DiscussionIdeaPublisher publisher;
  @TestedBean private DiscussionIdeaService service;
  private User actor;
  private Conversation conversation;

  @BeforeEach
  void prepare() {
    actor = mock(User.class, RETURNS_DEEP_STUBS);
    when(actor.getId()).thenReturn("actor");
    when(actor.getUserPreferences().getLanguage()).thenReturn("fr");
    conversation = mock(Conversation.class);
    when(conversation.getId()).thenReturn("conversation1");
    when(conversations.current(eq("discussion1"), anyBoolean())).thenReturn(conversation);
    var discussion1 = component("discussion1", "discussion", "space1");
    when(organization.getComponentInstLight("discussion1")).thenReturn(discussion1);
    when(organization.getAllComponentIds("space1")).thenReturn(new String[]{"suggestionBox1", "suggestionBox2", "suggestionBox3", "almanach1"});
    var suggestionBox1 = component("suggestionBox1", "suggestionBox", "space1");
    when(organization.getComponentInstLight("suggestionBox1")).thenReturn(suggestionBox1);
    var suggestionBox2 = component("suggestionBox2", "suggestionBox", "space2");
    when(organization.getComponentInstLight("suggestionBox2")).thenReturn(suggestionBox2);
    var suggestionBox3 = component("suggestionBox3", "suggestionBox", "space1");
    when(organization.getComponentInstLight("suggestionBox3")).thenReturn(suggestionBox3);
    var almanach1 = component("almanach1", "almanach", "space1");
    when(organization.getComponentInstLight("almanach1")).thenReturn(almanach1);
    when(componentAccess.isUserAuthorized(eq("actor"), anyString())).thenReturn(true);
    when(componentAccess.getUserRoles(eq("actor"), eq("suggestionBox1"), any())).thenReturn(Set.of(SilverpeasRole.WRITER));
    when(componentAccess.getUserRoles(eq("actor"), eq("suggestionBox3"), any())).thenReturn(Set.of(SilverpeasRole.READER));
  }

  @Test
  void onlyAuthorizedWriterBoxesInTheSameSpaceAreProposed() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertEquals(List.of(new DiscussionIdeaService.Choice("suggestionBox1", "suggestionBox1")), service.options("discussion1", "", "fr").boxes());
      verify(componentAccess, never()).getUserRoles(eq("actor"), eq("suggestionBox2"), any());
      when(componentAccess.isUserAuthorized("actor", "suggestionBox1")).thenReturn(false);
      assertTrue(service.options("discussion1", "", "fr").boxes().isEmpty());
    }
  }

  @Test
  void sourceTextIsLoadedOnlyWithinTheCurrentChannelAndDeletedSourcesAreRejected() {
    var source = mock(DiscussionMessage.class);
    when(source.getConversationId()).thenReturn("conversation1");
    when(source.getBody()).thenReturn("Bonjour @[Bernard](2) !");
    when(messages.getById("source")).thenReturn(source);
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertEquals("Bonjour @Bernard !", service.options("discussion1", "source", "fr").body());
      when(source.getConversationId()).thenReturn("foreign");
      assertThrows(IllegalArgumentException.class, () -> service.options("discussion1", "source", "fr"));
      when(source.getConversationId()).thenReturn("conversation1");
      when(source.isDeleted()).thenReturn(true);
      assertThrows(IllegalArgumentException.class, () -> service.options("discussion1", "source", "fr"));
      assertThrows(IllegalArgumentException.class, () -> service.options("discussion1", "missing", "fr"));
    }
  }

  @Test
  void nativePublicationStatusAndShortPermalinkAnnouncementArePreservedInThreads() {
    var suggestion = mock(Suggestion.class, RETURNS_DEEP_STUBS);
    when(suggestion.getId()).thenReturn("idea1");
    when(suggestion.getIdentifier()).thenReturn(ContributionIdentifier.from("suggestionBox1", "idea1", "Suggestion"));
    when(suggestion.getValidation().isPendingValidation()).thenReturn(true);
    var input = new DiscussionIdeaInput("suggestionBox1", " Une idée ", "Description", "");
    when(publisher.publish(input)).thenReturn(suggestion);
    var root = mock(DiscussionMessage.class);
    when(root.getId()).thenReturn("root");
    when(discussion.threadRoot("discussion1", "root")).thenReturn(root);
    try (var current = mockStatic(User.class); var urls = mockStatic(URLUtil.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      var created = service.create("discussion1", "", input);
      assertTrue(created.pending());
      assertTrue(created.url().startsWith("https://silverpeas.test/silverpeas/Contribution/"));
      assertEquals("suggestionBox1:Suggestion:idea1", new String(java.util.Base64.getDecoder().decode(created.url().substring(created.url().lastIndexOf('/') + 1)), java.nio.charset.StandardCharsets.UTF_8));
      verify(discussion).publish("discussion1", "💡 Une idée\n" + created.url());
      service.create("discussion1", "root", input);
      verify(discussion).reply("discussion1", "root", "💡 Une idée\n" + created.url(), List.of(), List.of());
    }
  }

  @Test
  void revokedRightsArchivedChannelsAndForeignSourcesNeverCreateAnIdea() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      var input = new DiscussionIdeaInput("suggestionBox2", "Titre", "Description", "");
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", input));
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", new DiscussionIdeaInput("suggestionBox1", "Titre", "Description", "missing")));
      when(conversation.isArchived()).thenReturn(true);
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", input));
      verifyNoInteractions(publisher);
    }
  }

  @Test
  void inputRejectsMalformedTargetsBlankAndOversizedText() {
    assertThrows(IllegalArgumentException.class, () -> new DiscussionIdeaInput("almanach1", "Titre", "Texte", ""));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionIdeaInput("suggestionBox1", " ", "Texte", ""));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionIdeaInput("suggestionBox1", "Titre", " ", ""));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionIdeaInput("suggestionBox1", "Titre", "x".repeat(2001), ""));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionIdeaInput("suggestionBox1", "x".repeat(161), "Texte", ""));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionIdeaInput("suggestionBox1", "Titre", "Texte", "../foreign"));
  }

  private ComponentInstLight component(String id, String name, String space) {
    var component = mock(ComponentInstLight.class);
    when(component.getId()).thenReturn(id);
    when(component.getName()).thenReturn(name);
    when(component.getSpaceId()).thenReturn(space);
    when(component.getLabel("fr")).thenReturn(id);
    return component;
  }
}
