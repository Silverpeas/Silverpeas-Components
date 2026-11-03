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

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.silverpeas.core.date.Period;

/** Plain input values; the time zone comes from the authenticated server-side context. */
public record DiscussionEventInput(String calendarId, String title, String description, Timing timing) {
  public DiscussionEventInput {
    if (calendarId == null || !calendarId.matches("[a-zA-Z0-9-]{1,40}")
        || title == null || title.isBlank() || title.strip().length() > 160
        || description != null && description.length() > 2000 || timing == null) {
      throw new IllegalArgumentException("Invalid discussion event");
    }
    title = title.strip().replace('\r', ' ').replace('\n', ' ');
    description = description == null ? "" : description.strip();
  }

  public record Timing(String start, String end) {
    public Period in(ZoneId zone) {
      if (start == null || end == null || zone == null) {
        throw new IllegalArgumentException("An event period is required");
      }
      try {
        var beginning = LocalDateTime.parse(start);
        var ending = LocalDateTime.parse(end);
        if (beginning.getYear() < 1 || ending.getYear() > 9999
            || zone.getRules().getValidOffsets(beginning).isEmpty()
            || zone.getRules().getValidOffsets(ending).isEmpty()
            || !ending.atZone(zone).isAfter(beginning.atZone(zone))) {
          throw new IllegalArgumentException("Invalid event period");
        }
        return Period.between(beginning.atZone(zone), ending.atZone(zone));
      } catch (DateTimeException e) {
        throw new IllegalArgumentException("Invalid event date", e);
      }
    }
  }
}
