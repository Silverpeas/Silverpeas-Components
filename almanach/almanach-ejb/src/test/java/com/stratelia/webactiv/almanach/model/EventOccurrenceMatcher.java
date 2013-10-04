/*
 * Copyright (C) 2000 - 2013 Silverpeas
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.almanach.model;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of event occurrences generated during the tests.
 */
public class EventOccurrenceMatcher extends TypeSafeMatcher<EventOccurrence> {

  private final EventDetail expected;
  private String invalidField = "";
  private String startDate = "";
  private String endDate = "";

  @Override
  protected boolean matchesSafely(EventOccurrence actual) {
    boolean match = true;
    if (!actual.getEventDetail().getId().equals(expected.getId())) {
      invalidField = "(bad event refered by the occurrence)";
      match = false;
    } else if (!actual.getStartDateTimeInISO().equals(startDate)) {
      invalidField = "(the start date differ)";
      match = false;
    } else if (!actual.getEndDateTimeInISO().equals(endDate)) {
      invalidField = "(the end date differ)";
      match = false;
    } else if (actual.isPriority() != expected.isPriority()) {
      invalidField = "(the priority differ)";
      match = false;
    } else if (actual.isAllDay() != expected.isAllDay()) {
      invalidField = "(the all day property differ)";
      match = false;
    }

    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an occurrence of event '" + this.expected.getId() + "' " + invalidField);
  }

  @Factory
  public static <T> Matcher<EventOccurrence> anOccurrenceOfEvent(String eventId,
      String startIso8601Date, String endIso8601Date) throws Exception {
    return new EventOccurrenceMatcher(EventDetailBuilder.anEventDetailOfId(eventId).build(), startIso8601Date, endIso8601Date);
  }

  public static String startingAt(String iso8601Date) {
    String startDate = iso8601Date;
    if (startDate.contains(":")) {
      startDate += "+0200";
    }
    return startDate;
  }

  public static String endingAt(final String iso8601Date) {
    String endDate = iso8601Date;
    if (endDate.contains(":")) {
      endDate += "+0200";
    }
    return endDate;
  }

  private EventOccurrenceMatcher(final EventDetail event, String startIso8601Date, String endIso8601Date) {
    this.expected = event;
    this.startDate = startIso8601Date;
    this.endDate = endIso8601Date;
  }
}
