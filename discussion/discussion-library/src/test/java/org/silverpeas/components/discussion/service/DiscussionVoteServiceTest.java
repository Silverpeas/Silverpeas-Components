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
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
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
class DiscussionVoteServiceTest {
  @TestManagedMock private DiscussionAccess access;
  @TestManagedMock private OrganizationController organization;
  @TestManagedMock private ComponentAccessController componentAccess;
  @TestManagedMock private ConversationRepository conversations;
  @TestManagedMock private ConversationService discussion;
  @TestManagedMock private DiscussionVotePlanner planner;
  @TestManagedMock private DiscussionVoteRollbackDispatcher rollback;
  @TestedBean private DiscussionVoteService service;
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
    when(organization.getAllComponentIds("space1")).thenReturn(new String[]{"pollingStation1", "pollingStation2", "pollingStation3", "survey1"});
    var pollingStation1 = component("pollingStation1", "pollingStation", "space1");
    when(organization.getComponentInstLight("pollingStation1")).thenReturn(pollingStation1);
    var pollingStation2 = component("pollingStation2", "pollingStation", "space2");
    when(organization.getComponentInstLight("pollingStation2")).thenReturn(pollingStation2);
    var pollingStation3 = component("pollingStation3", "pollingStation", "space1");
    when(organization.getComponentInstLight("pollingStation3")).thenReturn(pollingStation3);
    var survey1 = component("survey1", "survey", "space1");
    when(organization.getComponentInstLight("survey1")).thenReturn(survey1);
    when(componentAccess.isUserAuthorized(eq("actor"), anyString())).thenReturn(true);
    when(componentAccess.getUserRoles(eq("actor"), eq("pollingStation1"), any())).thenReturn(Set.of(SilverpeasRole.PUBLISHER));
    when(componentAccess.getUserRoles(eq("actor"), eq("pollingStation3"), any())).thenReturn(Set.of(SilverpeasRole.WRITER));
    when(organization.getComponentParameterValue("pollingStation1", "useAnonymousMode")).thenReturn("yes");
  }

  @Test
  void onlyAuthorizedWriterBoxesInTheSameSpaceAreProposed() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      assertEquals(List.of(new DiscussionVoteService.Choice("pollingStation1", "pollingStation1", true)), service.options("discussion1", "fr"));
      verify(componentAccess, never()).getUserRoles(eq("actor"), eq("pollingStation2"), any());
      when(componentAccess.isUserAuthorized("actor", "pollingStation1")).thenReturn(false);
      assertTrue(service.options("discussion1", "fr").isEmpty());
    }
  }

  @Test
  void creationUsesTheNativePlannerAndRegistersRollbackBeforeAnnouncingInTheCurrentThread() {
    var input = new DiscussionVoteInput("pollingStation1", "Quand ?", List.of("Mardi", "Jeudi"), false, false);
    var pk = new org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK("123", "space1", "pollingStation1");
    when(planner.create(input, "space1", true)).thenReturn(pk);
    var root = mock(DiscussionMessage.class);
    when(root.getId()).thenReturn("root");
    when(discussion.threadRoot("discussion1", "root")).thenReturn(root);
    try (var current = mockStatic(User.class); var urls = mockStatic(URLUtil.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      var created = service.create("discussion1", "root", input);
      assertEquals("https://silverpeas.test/silverpeas/RpollingStation/pollingStation1/searchResult?Type=QuestionContainer&Id=123", created.url());
      var order = inOrder(planner, rollback, discussion);
      order.verify(planner).create(input, "space1", true);
      order.verify(rollback).schedule(pk);
      order.verify(discussion).reply("discussion1", "root", "📊 Quand ?\nMardi · Jeudi\n" + created.url(), List.of(), List.of());
      service.create("discussion1", "", input);
      verify(discussion).publish("discussion1", "📊 Quand ?\nMardi · Jeudi\n" + created.url());
    }
  }

  @Test
  void foreignVotesSurveysRevokedRightsAndArchivedChannelsNeverCreate() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(actor);
      var input = new DiscussionVoteInput("pollingStation2", "Question", List.of("Oui", "Non"), false, false);
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", input));
      when(componentAccess.isUserAuthorized("actor", "pollingStation1")).thenReturn(false);
      assertThrows(SecurityException.class, () -> service.create("discussion1", "", new DiscussionVoteInput("pollingStation1", "Question", List.of("Oui", "Non"), false, false)));
      when(conversation.isArchived()).thenReturn(true);
      assertThrows(IllegalArgumentException.class, () -> service.create("discussion1", "", input));
      verifyNoInteractions(planner);
    }
  }

  @Test
  void invalidQuestionsAnswersAndNonVoteTargetsAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new DiscussionVoteInput("survey1", "Question", List.of("Oui", "Non"), false, false));
    assertThrows(IllegalArgumentException.class, () -> new DiscussionVoteInput("pollingStation1", " ", List.of("Oui", "Non"), false, false));
    for (var answers : List.of(List.of("Oui"), List.of("Oui", "oui"), List.of("Oui", " "), List.of("Oui", "x".repeat(201)), java.util.Collections.nCopies(11, "Oui"))) {
      assertThrows(IllegalArgumentException.class, () -> new DiscussionVoteInput("pollingStation1", "Question", answers, false, false));
    }
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
