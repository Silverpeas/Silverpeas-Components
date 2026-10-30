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
package org.silverpeas.components.discussion.web;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.service.ConversationService;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionParticipantServiceTest {
  @TestManagedMock
  private ConversationService conversations;
  @jakarta.inject.Inject
  private UserProvider users;
  @TestManagedMock
  private SessionManagement sessions;
  @TestedBean
  private DiscussionParticipantService service;

  @Test
  void listsDistinctAuthorsWithVisiblePresenceAndExcludesRemovedAccounts() {
    User requester = author("requester", "Requester");
    when(users.getCurrentRequester()).thenReturn(requester);
    User alice = author("a", "Alice");
    User bob = author("b", "Bob");
    User removed = author("removed", "Removed");
    when(removed.isRemovedState()).thenReturn(true);
    when(conversations.participantIds("discussion1"))
        .thenReturn(List.of("a", "b", "a", "removed", "missing"));
    SessionInfo visible = mock(SessionInfo.class);
    when(visible.getUser()).thenReturn(bob);
    when(sessions.getDistinctConnectedUsersList(requester)).thenReturn(List.of(visible));
    var result = service.list("discussion1");
    assertEquals(List.of("b", "a"), result.stream().map(DiscussionParticipant::id).toList());
    assertTrue(result.getFirst().connected());
    assertFalse(result.getLast().connected());
    assertEquals("/avatar/a", result.getLast().avatar());
    verify(sessions, never()).getConnectedUsersList();
  }

  @Test
  void deniedAccessDoesNotReadProfilesOrSessions() {
    when(conversations.participantIds("discussion1")).thenThrow(SecurityException.class);
    assertThrows(SecurityException.class, () -> service.list("discussion1"));
    verifyNoInteractions(users, sessions);
  }

  private User author(String id, String name) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(id);
    when(user.getDisplayedName()).thenReturn(name);
    when(user.getSmallAvatar()).thenReturn("/avatar/" + id);
    when(users.getUser(id)).thenReturn(user);
    return user;
  }
}
