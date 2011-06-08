/*
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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
 * A matcher of event details to use in tests.
 */
public class EventDetailMatcher extends TypeSafeMatcher<EventDetail> {
  
  private final EventDetail expected;
  private String invalidField = "";

  @Override
  protected boolean matchesSafely(EventDetail actual) {
    if (!actual.getPK().equals(expected.getPK())) {
      invalidField = "(PKs differ)";
      return false;
    }
    if (!actual.getCreatorId().equals(expected.getCreatorId())) {
      invalidField = "(creator id differ)";
      return false;
    }
    if (!actual.getDelegatorId().equals(expected.getDelegatorId())) {
      invalidField = "(delegator ids differ)";
      return false;
    }
   if ((actual.getStartDate() != null && !actual.getStartDate().equals(expected.getStartDate())) ||
        expected.getStartDate() != null && !expected.getStartDate().equals(actual.getStartDate())) {
      invalidField = "(start date differ)";
      return false;
    }
    if (!actual.getStartHour().equals(expected.getStartHour())) {
      invalidField = "(start hour differ)";
      return false;
    }
    if ((actual.getEndDate() != null && !actual.getEndDate().equals(expected.getEndDate())) ||
        expected.getEndDate() != null && !expected.getEndDate().equals(actual.getEndDate())) {
      invalidField = "(end date differ)";
      return false;
    }
    if (!actual.getEndHour().equals(expected.getEndHour())) {
      invalidField = "(end hour differ)";
      return false;
    }
    if (!actual.getName().equals(expected.getName())) {
      invalidField = "(name differ)";
      return false;
    }
    if (actual.getPriority() != expected.getPriority()) {
      invalidField = "(priority differ)";
      return false;
    }
    if (!actual.getEventUrl().equals(expected.getEventUrl())) {
      invalidField = "(url differ)";
      return false;
    }
    if (!actual.getTitle().equals(expected.getTitle())) {
      invalidField = "(title differ)";
      return false;
    }
    if (actual.getPeriodicity() != null && expected.getPeriodicity() != null) {
      Periodicity actualPeriodicity = actual.getPeriodicity();
      Periodicity expectedPeriodicity = expected.getPeriodicity();
      if (actualPeriodicity.getDay() != expectedPeriodicity.getDay()) {
        invalidField = "(periodicity.day differ)";
      }
      if (!actualPeriodicity.getDaysWeekBinary().equals(expectedPeriodicity.getDaysWeekBinary())) {
        invalidField = "(periodicity.daysWeekBinary differ)";
      }
      if (actualPeriodicity.getEventId() != expectedPeriodicity.getEventId()) {
        invalidField = "(periodicity.eventId differ)";
      }
      if (actualPeriodicity.getFrequency() != expectedPeriodicity.getFrequency()) {
        invalidField = "(periodicity.frequency differ)";
      }
      if (actualPeriodicity.getNumWeek() != expectedPeriodicity.getNumWeek()) {
        invalidField = "(periodicity.numWeek differ)";
      }
      if (actualPeriodicity.getUnity() != expectedPeriodicity.getUnity()) {
        invalidField = "(periodicity.unity differ)";
      }
      if (actualPeriodicity.getUntilDatePeriod().getTime() != expectedPeriodicity.getUntilDatePeriod().getTime()) {
        invalidField = "(periodicity.untilDatePeriod differ)";
      }
    } else if (actual.getPeriodicity() != expected.getPeriodicity()) {
      invalidField = "(periodicity differ)";
      return false;
    }
    
    return true;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("the event detail '" + this.expected.getId() + "' " + invalidField);
  }
  
  @Factory
  public static <T> Matcher<EventDetail> theEventDetail(final EventDetail event) {
    return new EventDetailMatcher(event);
  }
  
  private EventDetailMatcher(final EventDetail eventDetail) {
    this.expected = eventDetail;
  }

}
