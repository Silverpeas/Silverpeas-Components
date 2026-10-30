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

import java.util.List;
import java.util.Set;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.model.DiscussionMessage;
import org.silverpeas.components.discussion.service.DiscussionAccess;
import org.silverpeas.components.discussion.service.DiscussionMentionService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.*;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionMentionServiceTest {
  @TestManagedMock
  private OrganizationController organization;
  @TestManagedMock
  private ComponentAccessController componentAccess;
  @TestManagedMock
  private DiscussionAccess access;
  @TestManagedMock
  private DiscussionMentionDispatcher notifications;
  @TestedBean
  private DiscussionMentionService service;
  private User sender;

  @BeforeEach
  void users() {
    sender = user("1", "Sender");
    User alice = user("2", "Alice");
    User forbidden = user("3", "Forbidden");
    when(organization.getAllUsers("discussion1")).thenReturn(new User[]{sender, alice, forbidden});
    when(componentAccess.isUserAuthorized("1", "discussion1")).thenReturn(true);
    when(componentAccess.isUserAuthorized("2", "discussion1")).thenReturn(true);
  }

  @Test
  void canonicalNamesAndAuthorizedSuggestionsAreUsed() {
    assertEquals(List.of("2", "1"), service.candidates("discussion1").stream().map(c -> c.id()).toList());
    assertEquals("Hello @[Alice](2) 😀", service.normalize("discussion1", "Hello @[spoofed](2) 😀"));
    assertThrows(IllegalArgumentException.class, () -> service.normalize("discussion1", "@[Forbidden](3)"));
    assertEquals("https://example.org 😀", service.normalize("discussion1", "https://example.org 😀"));
  }

  @Test
  void onlyNewDistinctMentionsOtherThanTheAuthorAreNotified() {
    try (var current = mockStatic(User.class)) {
      current.when(User::getCurrentRequester).thenReturn(sender);
      var message = new DiscussionMessage("conversation", 1, "@[Sender](1) @[Alice](2) @[Alice](2)");
      service.notifyNewMentions("discussion1", message, "");
      var captured = ArgumentCaptor.forClass(DiscussionMentionEvent.class);
      verify(notifications).schedule(captured.capture());
      assertEquals(Set.of("2"), captured.getValue().recipients());
      clearInvocations(notifications);
      service.notifyNewMentions("discussion1", message, "@[Alice](2)");
      verifyNoInteractions(notifications);
    }
  }

  @Test
  void nativeNotificationIsBuiltOnlyInTheAfterCommitObserver() {
    var message = new DiscussionMessage("conversation", 1, "@[Alice](2)");
    try (var helper = mockStatic(UserNotificationHelper.class)) {
      new DiscussionMentionDispatcher(mock(Event.class)).notifyAfterCommit(new DiscussionMentionEvent("discussion1", message, sender, Set.of("2")));
      var notification = ArgumentCaptor.forClass(DiscussionMentionNotification.class);
      helper.verify(() -> UserNotificationHelper.buildAndSend(notification.capture()));
      assertEquals(Set.of("2"), notification.getValue().getUserIdsToNotify());
      assertEquals("discussion1", notification.getValue().getComponentInstanceId());
      assertEquals("1", notification.getValue().getSender());

    }
  }

  @Test
  void notificationsAreScheduledAsTransactionalEvents() {
    Event<DiscussionMentionEvent> events = mock(Event.class);
    var dispatcher = new DiscussionMentionDispatcher(events);
    var event = new DiscussionMentionEvent("discussion1", new DiscussionMessage("c", 1, "Hello"), sender, Set.of("2"));
    dispatcher.schedule(event);
    verify(events).fire(event);
  }

  private User user(String id, String name) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(id);
    when(user.getDisplayedName()).thenReturn(name);
    return user;
  }
}
