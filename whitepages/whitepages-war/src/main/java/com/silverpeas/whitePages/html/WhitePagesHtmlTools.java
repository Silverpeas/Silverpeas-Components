/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.whitePages.html;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.Value;

public class WhitePagesHtmlTools {

  /**
   * Hide constructor
   */
  private WhitePagesHtmlTools() {
  }

  public static String generateHtmlForPdc(List<SearchAxis> axis, String language,
      HttpServletRequest request) {
    StringBuilder result = new StringBuilder("");
    for (SearchAxis searchAxis : axis) {
      result.append("<div>");
      int axisId = searchAxis.getAxisId();
      String valueInContext = request.getAttribute("Axis" + String.valueOf(axisId)) != null ?
          (String) request.getAttribute("Axis" + String.valueOf(axisId)) : null;
      String increment = "";
      String selected = "";
      String axisName = searchAxis.getAxisName();
      StringBuilder buffer = new StringBuilder("<select name=\"Axis" + axisId + "\" size=\"1\">");
      buffer.append("<option value=\"\"></option>");
      List<Value> values = searchAxis.getValues();
      for (Value value : values) {
        for (int inc = 0; inc < value.getLevelNumber(); inc++) {
          increment += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }

        if (value.getFullPath().equals(valueInContext)) {
          selected = " selected";
        }

        buffer.append("<option value=\"").append(value.getFullPath()).append("\"").append(selected)
            .append(">").append(increment).append(value.getName(language));
        buffer.append("</option>");

        increment = "";
        selected = "";
      }
      buffer.append("</select>");
      result.append("<label class=\"txtlibform\" for=\"Axis");
      result.append(axisId);
      result.append("\">");
      result.append(axisName);
      result.append("</label>");
      result.append(buffer.toString());
      result.append("</div>");
    }
    return result.toString();
  }

}