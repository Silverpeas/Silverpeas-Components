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

import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.repository.CalendarEventRepository;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class DiscussionLinkedEventServiceTest {
  @TestManagedMock private CalendarEventRepository events;
  @TestedBean private DiscussionLinkedEventService service;
  private CalendarEvent event;
  private User user;
  private String body;

  @BeforeEach
  void prepare() {
    event = mock(CalendarEvent.class);
    var calendar = mock(Calendar.class);
    when(calendar.getComponentInstanceId()).thenReturn("almanach22");
    when(event.getCalendar()).thenReturn(calendar);
    when(event.getTitle()).thenReturn("Réunion");
    when(events.getById("event1")).thenReturn(event);
    user = mock(User.class);
    body = "📅 Réunion\nhttps://silverpeas.test/silverpeas/Contribution/"
        + Base64.getEncoder().encodeToString("almanach22:CalendarEvent:event1".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void confirmationExposesNativeRightsAndTheRecurringEventWarning() {
    try (var urls = mockStatic(URLUtil.class); var users = mockStatic(User.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      users.when(User::getCurrentRequester).thenReturn(user);
      when(event.canBeModifiedBy(user)).thenReturn(true);
      when(event.isRecurrent()).thenReturn(true);
      assertEquals(new DiscussionEventDeletion(true, true, "Réunion", true), service.describe(body));
      verify(event, never()).delete();
      when(event.canBeModifiedBy(user)).thenReturn(false);
      assertEquals(new DiscussionEventDeletion(true, false, "", false), service.describe(body));
      assertThrows(SecurityException.class, () -> service.delete(body));
      verify(event, never()).delete();
    }
  }

  @Test
  void nativeCancellationRequiresTheCurrentRightsAndAnExistingMatchingCalendar() {
    try (var urls = mockStatic(URLUtil.class); var users = mockStatic(User.class)) {
      urls.when(URLUtil::getAbsoluteApplicationURL).thenReturn("https://silverpeas.test/silverpeas");
      users.when(User::getCurrentRequester).thenReturn(user);
      when(event.canBeModifiedBy(user)).thenReturn(true);
      service.delete(body);
      verify(event).delete();
      when(event.getCalendar().getComponentInstanceId()).thenReturn("almanachOther");
      assertEquals(DiscussionEventDeletion.none(), service.describe(body));
      when(events.getById("event1")).thenReturn(null);
      assertDoesNotThrow(() -> service.delete(body));
      assertEquals(DiscussionEventDeletion.none(), service.describe(body));
      assertEquals(Transactional.TxType.MANDATORY, DiscussionLinkedEventService.class.getAnnotation(Transactional.class).value());
    }
  }
}
