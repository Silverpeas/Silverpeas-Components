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

package com.silverpeas.processManager.record;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.stratelia.webactiv.util.ResourceLocator;

public class QuestionTemplate implements RecordTemplate {
  private static ResourceLocator label = null;
  private String language;
  private boolean readonly;

  /**
   * A UserTemplate is built from a language : use addFieldTemplate for each field.
   * @see addFieldTemplate
   */
  public QuestionTemplate(String language, boolean readonly) {
    label = new ResourceLocator(
        "com.silverpeas.processManager.multilang.processManagerBundle",
        language);
    this.language = language;
    this.readonly = readonly;
  }

  /**
   * Returns all the field names of the UserRecord built on this template.
   */
  public String[] getFieldNames() {
    String[] fieldNames = new String[1];
    fieldNames[0] = "Content";

    return fieldNames;
  }

  /**
   * Returns all the field templates.
   */
  public FieldTemplate[] getFieldTemplates() {
    try {
      FieldTemplate[] templates = new FieldTemplate[1];
      templates[0] = getFieldTemplate("Content");

      return templates;
    } catch (FormException fe) {
      return null;
    }
  }

  /**
   * Returns the FieldTemplate of the named field.
   * @throw FormException if the field name is unknown.
   */
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    GenericFieldTemplate fieldTemplate = null;
    fieldTemplate = new GenericFieldTemplate(fieldName, "text");
    fieldTemplate.addLabel(label.getString("processManager." + fieldName),
        language);
    fieldTemplate.setDisplayerName("textarea");
    fieldTemplate.setMandatory(true);
    fieldTemplate.setReadOnly(readonly);

    return fieldTemplate;
  }

  /**
   * Returns the field index of the named field.
   * @throw FormException if the field name is unknown.
   */
  public int getFieldIndex(String fieldName) throws FormException {
    if (fieldName.equals("Content"))
      return 0;
    else
      return -1;
  }

  /**
   * Returns an empty DataRecord built on this template.
   */
  public DataRecord getEmptyRecord() throws FormException {
    return new QuestionRecord("");
  }

  /**
   * Returns true if the data record is built on this template and all the constraints are ok.
   */
  public boolean checkDataRecord(DataRecord record) {
    try {
      String value = (String) (record.getField("Content").getObjectValue());
      return (value != null && value.length() > 0);
    } catch (FormException fe) {
      return false;
    }
  }
}