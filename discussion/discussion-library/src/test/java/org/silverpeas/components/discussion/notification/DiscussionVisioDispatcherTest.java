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

import jakarta.enterprise.event.Event;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.silverpeas.components.discussion.service.DiscussionVisioService.Meeting;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionVisioDispatcherTest {
  @TestManagedMock private Event<DiscussionVisioEvent> events;
  @TestManagedMock private OrganizationController organization;
  @TestManagedMock private ComponentAccessController access;

  @Test
  void schedulesWithoutSendingAndRechecksAccessBeforeNativeDelivery() {
    var dispatcher = new DiscussionVisioDispatcher(events, organization, access);
    var sender = mock(User.class);
    when(sender.getId()).thenReturn("sender");
    var meeting = new Meeting("room", "Review", "https://host/visio/room");
    var event = new DiscussionVisioEvent("discussion1", meeting, sender, Set.of("active", "revoked", "missing"));
    var active = mock(UserDetail.class);
    when(organization.getUserDetail("active")).thenReturn(active);
    when(organization.getUserDetail("revoked")).thenReturn(mock(UserDetail.class));
    when(access.isUserAuthorized("active", "discussion1")).thenReturn(true);
    try (var notifications = mockStatic(UserNotificationHelper.class)) {
      dispatcher.schedule(event);
      verify(events).fire(event);
      notifications.verifyNoInteractions();
      dispatcher.afterCommit(event);
      notifications.verify(() -> UserNotificationHelper.buildAndSend(argThat(builder -> {
        var invitation = (DiscussionVisioNotification) builder;
        assertEquals(Set.of("active"), invitation.getUserIdsToNotify());
        assertEquals("sender", invitation.getSender());
        assertEquals(meeting.url(), invitation.getResourceURL(meeting));
        assertEquals("visioInvitation", invitation.getTemplateFileName());
        return true;
      })));
    }
  }
}
