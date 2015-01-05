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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and wait the template in the editor.
 */
package com.silverpeas.questionReply.control;

import com.silverpeas.questionReply.model.Question;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ehugonnet
 */
public class QuestionRegexpComparator implements Comparator<Question> {

  private static QuestionRegexpComparator instance;
  private Pattern pattern = Pattern.compile("([0-9\\.]*)\\s?-\\s?.*");
  private Pattern numberPattern = Pattern.compile("[0-9]+");

  private QuestionRegexpComparator() {
  }

  public static QuestionRegexpComparator getInstance() {
    synchronized (QuestionRegexpComparator.class) {
      if (instance == null) {
        instance = new QuestionRegexpComparator();
      }
      return instance;
    }
  }

  @Override
  public int compare(Question question1, Question question2) {
    int result = -1;
    if (question1 == question2) {
      result = 0;
    } else {
      if (question1 == null || question1.getTitle() == null) {
        result = -1;
      } else if (question2 == null || question2.getTitle() == null) {
        result = 1;
      } else {
        List<Integer> numbersQuestion1 = extractNumber(question1.getTitle());
        List<Integer> numbersQuestion2 = extractNumber(question2.getTitle());
        if (numbersQuestion1.isEmpty() || numbersQuestion2.isEmpty()) {
          result = question1.getTitle().compareTo(question2.getTitle());
        } else {
          if (numbersQuestion1.size() > numbersQuestion2.size()) {
            result = compareNumbers(numbersQuestion1, numbersQuestion2);
          } else {
            result = -1 * compareNumbers(numbersQuestion2, numbersQuestion1);
          }
        }
      }
    }
    return result;
  }

  protected List<Integer> extractNumber(final String label) {
    Matcher matcher = pattern.matcher(label);
    List<Integer> result = new ArrayList<>();
    String prefix = null;
    if (matcher.matches()) {
      prefix = matcher.group(1);
    }
    if (prefix != null) {
      matcher = numberPattern.matcher(prefix);
      while (matcher.find()) {
        String number = prefix.substring(matcher.start(), matcher.end());
        result.add(Integer.valueOf(number));
      }
    }
    return result;
  }

  protected int compareNumbers(List<Integer> bigList, List<Integer> smallList) {
    for (int i = 0; i < smallList.size(); i++) {
      int value1 = bigList.get(i);
      int value2 = smallList.get(i);
      if (value1 != value2) {
        return value1 - value2;
      }
    }
    return bigList.size() - smallList.size();
  }
}
