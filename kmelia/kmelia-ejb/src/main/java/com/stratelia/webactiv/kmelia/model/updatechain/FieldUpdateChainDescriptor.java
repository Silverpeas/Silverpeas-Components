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

package com.stratelia.webactiv.kmelia.model.updatechain;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import com.silverpeas.form.FormException;

public class FieldUpdateChainDescriptor {
  private String type;

  private String name;
  private boolean lastValue;
  private boolean suggestion;
  private String value;
  private List<String> values;
  private int size;

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  private List<FieldParameter> parameters;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean getLastValue() {
    return lastValue;
  }

  public void setLastValue(boolean lastValue) {
    this.lastValue = lastValue;
  }

  public boolean getSuggestion() {
    return suggestion;
  }

  public void setSuggestion(boolean suggestion) {
    this.suggestion = suggestion;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public List<FieldParameter> getParams() {
    return parameters;
  }

  public void setParams(List<FieldParameter> params) {
    this.parameters = params;
  }

  public void display(JspWriter jw, FieldsContext fieldsContext, boolean mandatory)
      throws IOException, FormException {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw, true);
    if (type.equals("text")) {
      TextFieldDisplayer fieldDisplayer = new TextFieldDisplayer();
      fieldDisplayer.display(out, this, fieldsContext, mandatory);
    } else if (type.equals("jdbc")) {
      JdbcFieldDisplayer fieldDisplayer = new JdbcFieldDisplayer();
      fieldDisplayer.display(out, this, fieldsContext, mandatory);
    }
    out.flush();
    jw.write(sw.toString());
  }

}