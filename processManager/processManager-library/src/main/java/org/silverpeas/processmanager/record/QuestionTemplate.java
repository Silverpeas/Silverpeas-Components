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

package org.silverpeas.processmanager.record;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

public class QuestionTemplate implements RecordTemplate {
  private LocalizationBundle label = null;
  private String language;
  private boolean readonly;

  /**
   * A QuestionTemplate is built from a language.
   * @param language the language
   * @param readonly is read only
   */
  public QuestionTemplate(String language, boolean readonly) {
    label = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.processManager.multilang.processManagerBundle",
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
   * @throws FormException if the field name is unknown.
   */
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    GenericFieldTemplate fieldTemplate = null;
    fieldTemplate = new GenericFieldTemplate(fieldName, "text");
    fieldTemplate.addLabel(label.getString("processManager." + fieldName), language);
    if (readonly) {
      fieldTemplate.setDisplayerName("simpletext");
    } else {
      fieldTemplate.setDisplayerName("textarea");
      fieldTemplate.setMandatory(true);
      fieldTemplate.setReadOnly(readonly);
      fieldTemplate.addParameter(TextField.PARAM_MAXLENGTH, "500");
    }
    return fieldTemplate;
  }

  /**
   * Returns the field index of the named field.
   * @throws FormException if the field name is unknown.
   */
  public int getFieldIndex(String fieldName) throws FormException {
    if (fieldName.equals("Content")) {
      return 0;
    } else {
      return -1;
    }
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