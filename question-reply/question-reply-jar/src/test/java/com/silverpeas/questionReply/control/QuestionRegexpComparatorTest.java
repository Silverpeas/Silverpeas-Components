/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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
package com.silverpeas.questionReply.control;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import com.silverpeas.questionReply.model.Question;

/**
 *
 * @author ehugonnet
 */
public class QuestionRegexpComparatorTest {

  public QuestionRegexpComparatorTest() {
  }

  /**
   * Test of compare method, of class QuestionRegexpComparator.
   */
  @org.junit.Test
  public void testCompare() {
    Question question1 = new Question();
    question1.setTitle("1.0 - Test");
    Question question2 = new Question();
    question2.setTitle("1.1 - Test 1");
    QuestionRegexpComparator instance = QuestionRegexpComparator.getInstance();
    int result = instance.compare(question1, question2);
    assertTrue(result < 0);
    question1.setTitle("2.0 - Test");
    result = instance.compare(question1, question2);
    assertTrue(result > 0);
  }

  /**
   * Test of extractNumber method, of class QuestionRegexpComparator.
   */
  @org.junit.Test
  public void testCompareNumbers() {
    QuestionRegexpComparator instance = QuestionRegexpComparator.getInstance();
    List<Integer> list1 = Arrays.asList(1, 10, 5);
    List<Integer> list2 = Arrays.asList(1, 5);
    int result = instance.compareNumbers(list1, list2);
    assertTrue(result > 0);
  }
}
