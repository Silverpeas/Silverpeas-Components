/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.processmanager.record;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;

public class QuestionTemplate implements RecordTemplate {
  private static final String CONTENT = "Content";
  private final transient LocalizationBundle label;
  private final String language;
  private final boolean readonly;

  /**
   * A QuestionTemplate is built for a given language.
   * @param language the language the ISO-631 code of a supported language.
   * @param readonly is read only a boolean indicating if the question should be readonly
   */
  public QuestionTemplate(String language, boolean readonly) {
    label = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.processManager.multilang.processManagerBundle",
        language);
    this.language = language;
    this.readonly = readonly;
  }

  @Override
  public String[] getFieldNames() {
    String[] fieldNames = new String[1];
    fieldNames[0] = CONTENT;

    return fieldNames;
  }

  @Override
  public FieldTemplate[] getFieldTemplates() {
    try {
      FieldTemplate[] templates = new FieldTemplate[1];
      templates[0] = getFieldTemplate(CONTENT);

      return templates;
    } catch (FormException fe) {
      return new FieldTemplate[0];
    }
  }

  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    GenericFieldTemplate fieldTemplate;
    fieldTemplate = new GenericFieldTemplate(fieldName, "text");
    fieldTemplate.addLabel(label.getString("processManager." + fieldName), language);
    if (readonly) {
      fieldTemplate.setDisplayerName("simpletext");
    } else {
      fieldTemplate.setDisplayerName("textarea");
      fieldTemplate.setMandatory(true);
      fieldTemplate.setReadOnly(false);
      fieldTemplate.addParameter(TextField.PARAM_MAXLENGTH, "500");
    }
    return fieldTemplate;
  }

  @Override
  public int getFieldIndex(String fieldName) {
    if (fieldName.equals(CONTENT)) {
      return 0;
    } else {
      return -1;
    }
  }

  @Override
  public DataRecord getEmptyRecord() {
    return new QuestionRecord("");
  }

  @Override
  public boolean checkDataRecord(DataRecord record) {
    try {
      String value = (String) (record.getField(CONTENT).getObjectValue());
      return (value != null && value.length() > 0);
    } catch (FormException fe) {
      return false;
    }
  }
}