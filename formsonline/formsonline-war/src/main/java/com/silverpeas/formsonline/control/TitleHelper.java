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

package com.silverpeas.formsonline.control;

import java.util.StringTokenizer;

import com.silverpeas.formsonline.model.FormInstance;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import org.silverpeas.core.admin.OrganisationController;

public class TitleHelper {
  private static OrganisationController controller = new OrganizationController();

  /**
   * Hide Utility Class Constructor with a private constuctor...
   */
  private TitleHelper() {
  }

  public static String computeTitle(FormInstance instance, String title) {
    while (title.indexOf("${") != -1) {
      int begin = title.indexOf("${");
      int end = title.indexOf('}');
      String keyword = title.substring(begin + 2, end);
      title =
          title.substring(0, begin) + computeKeyword(instance, keyword) +
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
      UserDetail user = controller.getUserDetail(instance.getCreatorId());
      String value = "";

      if (fieldName.equals("firstName")) {
        value = user.getFirstName();
      }

      else if (fieldName.equals("lastName")) {
        value = user.getLastName();
      }

      else if (fieldName.equals("fullName")) {
        value = user.getDisplayedName();
      }

      else if (fieldName.equals("emailName")) {
        value = user.geteMail();
      }

      else {
        value = "$$" + keyword + "$$";
      }

      return value;
    }

    else if (tokenizer.countTokens() == 3) {
      String firstToken = tokenizer.nextToken();
      String secondToken = tokenizer.nextToken();
      if ((!firstToken.equals("sender")) || ((!secondToken.equals("attribute")))) {
        return "$$" + keyword + "$$";
      }

      UserFull user = controller.getUserFull(instance.getCreatorId());
      String propertyName = tokenizer.nextToken();
      return user.getValue(propertyName, "$$" + keyword + "$$");
    }

    else {
      return "$$" + keyword + "$$";
    }
  }
}
