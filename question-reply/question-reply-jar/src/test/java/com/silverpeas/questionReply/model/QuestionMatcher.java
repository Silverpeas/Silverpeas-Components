/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.model;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 *
 * @author emmanuel.hugonnet@silverpeas.org
 */
public class QuestionMatcher extends BaseMatcher<Question> {

  private Question question;

  /**
   * Creates a new matcher with the specified question.
   * @param thequestion the comment to match.
   * @return a question matcher.
   */
  public static QuestionMatcher matches(final Question thequestion) {
    return new QuestionMatcher(thequestion);
  }

  private QuestionMatcher(final Question thequestion) {
    this.question = thequestion;
  }

  @Override
  public boolean matches(Object item) {
    boolean match = false;
    if (item instanceof Question) {
      Question actual = (Question) item;
      match = question.getPK().getId().equals(actual.getPK().getId()) &&
          question.getInstanceId().equals(actual.getInstanceId()) &&
          question.getCreationDate().equals(actual.getCreationDate()) &&
          question.getTitle().equals(actual.getTitle()) &&
          question.getCategoryId().equals(actual.getCategoryId()) &&
          question.getContent().equals(actual.getContent()) &&
          question.getCreatorId().equals(actual.getCreatorId()) &&
          question.getStatus() == actual.getStatus() &&
          question.getPrivateReplyNumber() == actual.getPrivateReplyNumber() &&
          question.getPublicReplyNumber()  == actual.getPublicReplyNumber() &&
          question.getReplyNumber() == actual.getReplyNumber();
    }
    return match;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(question);
  }
}
