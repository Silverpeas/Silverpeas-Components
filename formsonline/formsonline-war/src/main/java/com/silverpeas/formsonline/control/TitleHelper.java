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
package com.silverpeas.formsonline.control;

import com.silverpeas.formsonline.model.FormInstance;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

import java.util.StringTokenizer;

public final class TitleHelper {

  private TitleHelper() {
  }

  public static String computeTitle(FormInstance instance, String title) {
    while (title.contains("${")) {
      int begin = title.indexOf("${");
      int end = title.indexOf('}');
      String keyword = title.substring(begin + 2, end);
      title = title.substring(0, begin) + computeKeyword(instance, keyword) +
          title.substring(end + 1, title.length());
    }
    return title;
  }

  private static String computeKeyword(FormInstance instance, String keyword) {
    StringTokenizer tokenizer = new StringTokenizer(keyword, ".");

    if (tokenizer.countTokens() == 2) {
      String firstToken = tokenizer.nextToken();
      if (!firstToken.equals("sender")) {
        return "$$" + keyword + "$$";
      }
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
          value = user.geteMail();
          break;
        default:
          value = "$$" + keyword + "$$";
          break;
      }
      return value;
    } else if (tokenizer.countTokens() == 3) {
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

}
