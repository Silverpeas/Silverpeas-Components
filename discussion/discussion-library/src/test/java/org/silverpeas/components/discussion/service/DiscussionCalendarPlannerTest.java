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

import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.CalendarEvent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscussionCalendarPlannerTest {
  @Test
  void nativePlanningSetsCreatorAndAttendeesAndEscapesPlainDescription() {
    var actor = mock(User.class);
    var attendee = mock(User.class);
    var calendar = mock(Calendar.class);
    var event = mock(CalendarEvent.class, RETURNS_SELF);
    var input = new DiscussionEventInput("calendar1", "Réunion", "<script>bad</script>",
        new DiscussionEventInput.Timing("2026-11-03T10:00", "2026-11-03T11:00"));
    var period = input.timing().in(ZoneId.of("Europe/Paris"));
    try (var users = mockStatic(User.class); var events = mockStatic(CalendarEvent.class)) {
      users.when(User::getCurrentRequester).thenReturn(actor);
      events.when(() -> CalendarEvent.on(period)).thenReturn(event);
      assertSame(event, new DiscussionCalendarPlanner().plan(calendar, period, input, List.of(attendee)));
      verify(event).withTitle("Réunion");
      verify(event).withDescription("&lt;script&gt;bad&lt;/script&gt;");
      verify(event).createdBy(actor);
      verify(event).withAttendee(attendee);
      verify(event).planOn(calendar);
    }
  }
}
