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

package com.stratelia.webactiv.kmelia.model.updatechain;

import com.silverpeas.form.FormException;
import com.silverpeas.form.Util;
import com.silverpeas.util.EncodeHelper;

import java.io.PrintWriter;
import java.util.List;

public class TextFieldDisplayer {
  /**
   * Constructeur
   */
  public TextFieldDisplayer() {

  }

  public void display(PrintWriter out, FieldUpdateChainDescriptor field,
      FieldsContext fieldsContext, boolean mandatory) throws FormException {
    String mandatoryImg = Util.getIcon("mandatoryField");
    List values = field.getValues();
    String value = field.getValue();
    if (!field.getLastValue())
      value = "";
    if (values != null && values.size() > 0) {
      out.println("<select name=\"" + field.getName() + "\">");
      for (Object value1 : values) {
        String currentValue = (String) value1;
        String selected = "";
        if (currentValue.equals(field.getName())) {
          selected = "selected";
        }

        out.println("<option value=\""
            + EncodeHelper.javaStringToHtmlString(currentValue) + "\" "
            + selected + ">"
            + EncodeHelper.javaStringToHtmlString(currentValue) + "</option>");

      }
      out.println("</select>");
    } else {
      out.println("<input type=\"text\" size=\"" + field.getSize()
          + "\" name=\"" + field.getName() + "\" value=\""
          + EncodeHelper.javaStringToHtmlString(value)
          + "\" size=\"60\" maxlength=\"60\">");
    }

    if (mandatory) {
      out.println("<TD><img src=\"" + mandatoryImg
          + "\" width=\"5\" height=\"5\" border=\"0\" ><TD>");
    }

  }

}
