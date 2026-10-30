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
package org.silverpeas.components.discussion.notification;

import java.util.Set;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionChangeDispatcherTest {
  @TestManagedMock
  private DefaultServerEventNotifier notifier;
  @TestManagedMock
  private ComponentAccessController access;
  @TestManagedMock
  private org.silverpeas.core.admin.service.OrganizationController organization;
  @TestedBean
  private DiscussionChangeDispatcher dispatcher;

  @Test
  void mutationsScheduleInvalidationsAndReadsTargetTheirAuthor() {
    Event<DiscussionChange> events = mock(Event.class);
    var scheduler = new DiscussionChangeDispatcher(events);
    scheduler.changed("discussion1");
    scheduler.read("discussion1", "reader");
    verify(events).fire(new DiscussionChange("discussion1", null));
    verify(events).fire(new DiscussionChange("discussion1", "reader"));
  }

  @Test
  void onlyCommittedChangesArePublishedOnTheSharedPlatformChannel() throws Exception {
    var method = DiscussionChangeDispatcher.class.getMethod("afterCommit", DiscussionChange.class);
    assertEquals(TransactionPhase.AFTER_SUCCESS,
        method.getParameters()[0].getAnnotation(Observes.class).during());
    dispatcher.afterCommit(new DiscussionChange("discussion1", null));
    var capture = ArgumentCaptor.forClass(DiscussionServerEvent.class);
    verify(notifier).notify(capture.capture());
    assertEquals("DISCUSSION_CHANGED", capture.getValue().getName().asString());
    assertEquals(java.util.List.of("/sse/common"), capture.getValue().getEventSourceURIs());
    assertTrue(capture.getValue().getData("session", null).contains("\"instanceId\":\"discussion1\""));
    assertTrue(capture.getValue().getData("session", null).contains("\"contentChanged\":true"));
  }

  @Test
  void receiverPermissionsAreCheckedOnEveryDeliveryIncludingReplays() {
    var receiver = mock(User.class);
    when(receiver.getId()).thenReturn("writer");
    when(access.isUserAuthorized("writer", "discussion1")).thenReturn(true);
    when(access.getUserRoles(eq("writer"), eq("discussion1"), any())).thenReturn(Set.of(SilverpeasRole.WRITER));
    var event = new DiscussionServerEvent(new DiscussionChange("discussion1", null), access);
    assertTrue(event.isConcerned("session", receiver));
    when(access.getUserRoles(eq("writer"), eq("discussion1"), any())).thenReturn(Set.of(SilverpeasRole.READER));
    assertFalse(event.isConcerned("session", receiver));
    when(access.getUserRoles(eq("writer"), eq("discussion1"), any())).thenReturn(Set.of(SilverpeasRole.ADMIN));
    assertTrue(event.isConcerned("session", receiver));
    when(receiver.isAccessGuest()).thenReturn(true);
    assertFalse(event.isConcerned("session", receiver));
    when(receiver.isAccessGuest()).thenReturn(false);
    when(access.isUserAuthorized("writer", "discussion1")).thenReturn(false);
    assertFalse(event.isConcerned("session", receiver));
  }

  @Test
  void newMessageDesktopSignalsAreAfterCommitLiveOnlyAndExcludeTheAuthor() throws Exception {
    var method = DiscussionChangeDispatcher.class.getMethod("newMessageAfterCommit", DiscussionNewMessage.class);
    assertEquals(TransactionPhase.AFTER_SUCCESS, method.getParameters()[0].getAnnotation(Observes.class).during());
    assertEquals(Transactional.TxType.REQUIRES_NEW,
        method.getAnnotation(Transactional.class).value(),
        "Component lookup must run in a fresh transaction after publication commits");
    var component = mock(org.silverpeas.core.admin.component.model.ComponentInstLight.class);
    when(component.getLabel("fr")).thenReturn("Projet IA");
    when(organization.getComponentInstLight("discussion1")).thenReturn(component);
    dispatcher.newMessageAfterCommit(new DiscussionNewMessage("discussion1", "m1", "1", "Alice"));
    var capture = ArgumentCaptor.forClass(DiscussionMessageServerEvent.class);
    verify(notifier).notify(capture.capture());
    var event = capture.getValue();
    assertInstanceOf(org.silverpeas.core.notification.sse.behavior.IgnoreStoring.class, event);
    var recipient = mock(User.class);
    when(recipient.getId()).thenReturn("2");
    var preferences = new org.silverpeas.core.personalization.UserPreferences();
    preferences.setLanguage("fr");
    when(recipient.getUserPreferences()).thenReturn(preferences);
    when(access.isUserAuthorized("2", "discussion1")).thenReturn(true);
    when(access.getUserRoles(eq("2"), eq("discussion1"), any())).thenReturn(Set.of(SilverpeasRole.WRITER));
    assertTrue(event.isConcerned("session", recipient));
    assertTrue(event.getData("session", recipient).contains("Projet IA"));
    assertTrue(event.getData("session", recipient).contains("messages/m1"));
    when(recipient.getId()).thenReturn("1");
    assertFalse(event.isConcerned("session", recipient));
  }

  @Test
  void readAcknowledgmentsArePrivateToTheUsersOwnSessions() {
    var receiver = mock(User.class);
    when(receiver.getId()).thenReturn("other");
    var event = new DiscussionServerEvent(new DiscussionChange("discussion1", "writer"), access);
    assertFalse(event.isConcerned("session", receiver));
    assertTrue(event.getData("session", receiver).contains("\"contentChanged\":false"));
    verifyNoInteractions(access);
  }
}
