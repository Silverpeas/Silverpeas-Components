/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.formsonline.control;

import org.silverpeas.components.formsonline.model.FormInstance;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;

import java.util.StringTokenizer;

public final class TitleHelper {

  private TitleHelper() {
  }

  public static String computeTitle(FormInstance instance, String title) {
    while (title.contains("${")) {
      int begin = title.indexOf("${");
      int end = title.indexOf('}');
      final int startField = 2;
      String keyword = title.substring(begin + startField, end);
      title = title.substring(0, begin) + computeKeyword(instance, keyword) +
          title.substring(end + 1, title.length());
    }
    return title;
  }

  private static String computeKeyword(FormInstance instance, String keyword) {
    StringTokenizer tokenizer = new StringTokenizer(keyword, ".");

    final int fieldToken = 2;
    final int attributeToken = 3;
    if (tokenizer.countTokens() == fieldToken) {
      String firstToken = tokenizer.nextToken();
      if (!"sender".equals(firstToken)) {
        return "$$" + keyword + "$$";
      }
      return getFieldValue(instance, keyword, tokenizer);
    } else if (tokenizer.countTokens() == attributeToken) {
      String firstToken = tokenizer.nextToken();
      String secondToken = tokenizer.nextToken();
      if (!"sender".equals(firstToken) || (!"attribute".equals(secondToken))) {
        return "$$" + keyword + "$$";
      }
      UserFull user = UserFull.getById(instance.getCreatorId());
      String propertyName = tokenizer.nextToken();
      return user.getValue(propertyName, "$$" + keyword + "$$");
    } else {
      return "$$" + keyword + "$$";
    }
  }

  private static String getFieldValue(final FormInstance instance, final String keyword,
      final StringTokenizer tokenizer) {
    String fieldName = tokenizer.nextToken();
    UserDetail user = UserDetail.getById(instance.getCreatorId());
    String value;
    switch (fieldName) {
      case "firstName":
        value = user.getFirstName();
        break;
      case "lastName":
        value = user.getLastName();
        break;
      case "fullName":
        value = user.getDisplayedName();
        break;
      case "emailName":
        value = user.getEmailAddress();
        break;
      default:
        value = "$$" + keyword + "$$";
        break;
    }
    return value;
  }

}
