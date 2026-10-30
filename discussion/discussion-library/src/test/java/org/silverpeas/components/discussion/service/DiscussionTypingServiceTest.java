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

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.components.discussion.notification.DiscussionTypingServerEvent;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.notification.sse.DefaultServerEventNotifier;
import org.silverpeas.core.notification.sse.behavior.IgnoreStoring;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.*;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionTypingServiceTest {
  @TestManagedMock
  private DiscussionAccess access;
  @TestManagedMock
  private ComponentAccessController receivers;
  @TestManagedMock
  private DefaultServerEventNotifier notifier;
  @Inject
  private UserProvider users;
  @TestManagedMock
  private org.silverpeas.core.security.session.SessionManagement sessions;
  @TestedBean
  private DiscussionTypingService service;

  @Test
  void typingIsThrottledExpiresAndRemainsActiveUntilEveryTabStops() {
    var user = mock(User.class);
    when(user.getId()).thenReturn("2");
    when(user.getDisplayedName()).thenReturn("Bernard");
    when(users.getCurrentRequester()).thenReturn(user);
    service.typing("discussion1", "session:tabA", true);
    service.typing("discussion1", "session:tabA", true);
    verify(notifier, times(1)).notify(org.mockito.ArgumentMatchers.any());
    service.typing("discussion1", "session:tabB", true);
    service.typing("discussion1", "session:tabA", false);
    var events = ArgumentCaptor.forClass(DiscussionTypingServerEvent.class);
    verify(notifier, times(3)).notify(events.capture());
    assertFalse(events.getValue().getData("session", user).contains("\"ttl\":0"));
    assertInstanceOf(IgnoreStoring.class, events.getValue());
    assertFalse(events.getValue().isConcerned("session", user));
    service.typing("discussion1", "session:tabB", false);
    verify(notifier, times(4)).notify(events.capture());
    assertTrue(events.getValue().getData("session", user).contains("\"ttl\":0"));
    assertFalse(events.getValue().getData("session", user).contains("draft"));
  }

  @Test
  void receiverMustBeAuthorizedAndAllowedToSeeTheConnectedAuthor() {
    var author = mock(User.class);
    when(author.getId()).thenReturn("2");
    when(author.getDisplayedName()).thenReturn("Bernard");
    when(users.getCurrentRequester()).thenReturn(author);
    service.typing("discussion1", "session:tab", true);
    var events = ArgumentCaptor.forClass(DiscussionTypingServerEvent.class);
    verify(notifier).notify(events.capture());
    var receiver = mock(User.class);
    when(receiver.getId()).thenReturn("3");
    when(receivers.isUserAuthorized("3", "discussion1")).thenReturn(true);
    when(receivers.getUserRoles(org.mockito.ArgumentMatchers.eq("3"), org.mockito.ArgumentMatchers.eq("discussion1"),
        org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Set.of(SilverpeasRole.WRITER));
    var session = mock(org.silverpeas.core.security.session.SessionInfo.class);
    when(session.getUser()).thenReturn(author);
    when(sessions.getDistinctConnectedUsersList(receiver)).thenReturn(java.util.List.of(session));
    assertTrue(events.getValue().isConcerned("receiver-session", receiver));
    when(sessions.getDistinctConnectedUsersList(receiver)).thenReturn(java.util.List.of());
    assertFalse(events.getValue().isConcerned("receiver-session", receiver));
  }

  @Test
  void unauthorizedRequestsNeverPublishPresence() {
    when(access.require("discussion1", SilverpeasRole.WRITER)).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.typing("discussion1", "session", true));
    verifyNoInteractions(notifier);
  }
}
