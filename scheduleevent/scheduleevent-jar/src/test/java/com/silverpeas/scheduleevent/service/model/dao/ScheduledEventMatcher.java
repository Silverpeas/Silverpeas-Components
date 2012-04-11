/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package com.silverpeas.scheduleevent.service.model.dao;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import com.silverpeas.scheduleevent.service.model.beans.ScheduleEvent;
import java.util.Set;
import java.util.SortedSet;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher of scheduled events dedicated to tests.
 */
public class ScheduledEventMatcher extends TypeSafeMatcher<ScheduleEvent> {
  
  private ScheduleEvent expected;
  private String description = "";
  
  public static ScheduledEventMatcher isEqualTo(final ScheduleEvent expected) {
    return new ScheduledEventMatcher(expected);
  }

  @Override
  protected boolean matchesSafely(ScheduleEvent actual) {
    boolean match = true;
    if (!actual.getId().equals(expected.getId())) {
      match = false;
      description += "(actual id: " + actual.getId() + ", expected id: " + expected.getId() + ")";
    }
    if (!actual.getTitle().equals(expected.getTitle())) {
      match = false;
      description += "(actual description: " + actual.getDescription() + ", expected id: " + expected.getDescription() + ")";
    }
    if (actual.getStatus() != expected.getStatus()) {
      match = false;
      description += "(actual status: " + actual.getStatus() + ", expected status: " + expected.getStatus() + ")";
    }
    if (actual.getAuthor() != expected.getAuthor()) {
      match = false;
      description += "(actual author id: " + actual.getAuthor() + ", expected author id: " + expected.getAuthor() + ")";
    }
    if (!actual.getCreationDate().equals(expected.getCreationDate())) {
      match = false;
      description += "(actual creation date: " + actual.getCreationDate() + ", expected creation date: " + expected.getCreationDate() + ")";
    }
    if (!contributorsMatched(actual.getContributors(), expected.getContributors())) {
      match = false;
      description += "(contributors don't match)";
    }
    if (!dateOptionsMatched(actual.getDates(), expected.getDates())) {
      match = false;
      description += "(date options don't match)";
    }
    if (!responsesMatched(actual.getResponses(), expected.getResponses())) {
      match = false;
      description += "(responses don't match)";
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(this.description);
  }
  
  private ScheduledEventMatcher(final ScheduleEvent expectedScheduledEvent) {
    this.expected = expectedScheduledEvent;
  }

  private boolean contributorsMatched(Set<Contributor> actualContributors,
          Set<Contributor> expectedContributors) {
    return areEqual(actualContributors, expectedContributors);
  }

  private boolean dateOptionsMatched(SortedSet<DateOption> actualDates,
          SortedSet<DateOption> expectedDates) {
    return areEqual(actualDates, expectedDates);
  }

  private boolean responsesMatched(Set<Response> actualResponses,
          Set<Response> expectedResponses) {
    return areEqual(actualResponses, expectedResponses);
  }
  
  private <T> boolean areEqual(Set<T> actualSet, Set<T> expectedSet) {
    if (actualSet.size() != expectedSet.size()) {
      return false;
    } else {
      return actualSet.containsAll(expectedSet);
    }
  }
  
}
