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

package org.silverpeas.processmanager.record;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.HashMap;
import java.util.Map;

public class QuestionRecord implements DataRecord {

  private static final long serialVersionUID = 4978363746794966549L;
  private String id = null;
  private final TextFieldImpl contentField;

  /**
   * A QuestionRecord is built from a Question
   */
  public QuestionRecord(String content) {
    contentField = new TextFieldImpl();
    contentField.setStringValue(content);
  }

  /**
   * Returns the data record id. The record is known by its external id.
   */
  public String getId() {
    return id;
  }

  /**
   * Gives an id to the record. Caution ! the record is known by its external id.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns all the fields
   */
  @SuppressWarnings("unused")
  public Field[] getFields() {
    try {
      Field[] fields = new Field[1];
      fields[0] = getField("Content");
      return fields;
    } catch (FormException fe) {
      return new Field[0];
    }
  }

  /**
   * Returns the named field.
   * @throws FormException when the fieldName is unknown.
   */
  public Field getField(String fieldName) throws FormException {
    if (fieldName.equals("Content")) {
      return getField(0);
    } else {
      throw new FormException("QuestionRecord", "workflowEngine.ERR_FIELD_NOT_FOUND");
    }
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    try {
      return getField(fieldName);
    } catch (FormException e) {
      SilverLogger.getLogger(this).error(e);
    }
    return null;
  }

  /**
   * Returns the field at the index position in the record.
   * @throws FormException when the fieldIndex is unknown.
   */
  public Field getField(int fieldIndex) throws FormException {
    if (fieldIndex == 0) {
      return contentField;
    } else {
      throw new FormException("QuestionRecord", "workflowEngine.ERR_FIELD_NOT_FOUND");
    }
  }

  public String[] getFieldNames() {
    return new String[0];
  }

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  public boolean isNew() {
    return true;
  }

  public String getLanguage() {
    return null;
  }

  public void setLanguage(String language) {
    // nothing to do
  }

  @Override
  public Map<String, String> getValues(String language) {
    return new HashMap<>();
  }

  @Override
  public ResourceReference getResourceReference() {
    return null;
  }
}