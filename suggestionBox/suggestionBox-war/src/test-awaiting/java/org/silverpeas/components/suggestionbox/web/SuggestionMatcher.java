/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.components.suggestionbox.web;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.silverpeas.components.suggestionbox.model.Suggestion;


/**
 * A matcher to compare a Suggestion instance with its SuggestionEntity counterpart.
 * @author mmoquillon
 */
public class SuggestionMatcher extends TypeSafeMatcher<SuggestionEntity> {

  public static Matcher<SuggestionEntity> matches(final Suggestion suggestion) {
    return new SuggestionMatcher(suggestion);
  }

  private final Suggestion expected;

  private SuggestionMatcher(Suggestion suggestion) {
    this.expected = suggestion;
  }

  @Override
  protected boolean matchesSafely(SuggestionEntity actual) {
    final EqualsBuilder matcher = new EqualsBuilder();
    matcher.append(actual.getId(), expected.getId());
    matcher.append(actual.getTitle(), expected.getTitle());
    matcher.append(actual.getContent(), expected.getContent());
    matcher.append(actual.getValidation().getStatus(), expected.getValidation().getStatus());
    return matcher.isEquals();
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(expected);
  }

}
