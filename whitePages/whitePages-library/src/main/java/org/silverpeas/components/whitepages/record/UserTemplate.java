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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.whitepages.record;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.form.HtmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;

public class UserTemplate implements RecordTemplate {

  private LocalizationBundle label = null;
  private HtmlForm viewForm;

  /**
   * A UserTemplate is built from a fileName and a language : use addFieldTemplate for each field.
   */
  public UserTemplate(String fileName, String language) {
    label = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.whitePages.multilang.whitePagesBundle", language);
    try {
      this.viewForm = new HtmlForm(this);
    } catch (FormException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    viewForm.setFileName(fileName);
  }

  /**
   * Returns all the field names of the UserRecord built on this template.
   */
  @Override
  public String[] getFieldNames() {
    String[] fieldNames = new String[9];
    fieldNames[0] = "Id";
    fieldNames[1] = "SpecificId";
    fieldNames[2] = "DomainId";
    fieldNames[3] = "Login";
    fieldNames[4] = "FirstName";
    fieldNames[5] = "LastName";
    fieldNames[6] = "Mail";
    fieldNames[7] = "AccessLevel";
    fieldNames[8] = "SpecificDetails";

    return fieldNames;
  }

  /**
   * Returns all the field templates.
   */
  @Override
  public FieldTemplate[] getFieldTemplates() {
    FieldTemplate[] fieldTemplates = new FieldTemplate[9];
    try {
      fieldTemplates[0] = getFieldTemplate("Id");
      fieldTemplates[1] = getFieldTemplate("SpecificId");
      fieldTemplates[2] = getFieldTemplate("DomainId");
      fieldTemplates[3] = getFieldTemplate("Login");
      fieldTemplates[4] = getFieldTemplate("FirstName");
      fieldTemplates[5] = getFieldTemplate("LastName");
      fieldTemplates[6] = getFieldTemplate("Mail");
      fieldTemplates[7] = getFieldTemplate("AccessLevel");
      fieldTemplates[8] = getFieldTemplate("SpecificDetails");
    } catch (FormException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return fieldTemplates;
  }

  /**
   * Returns the FieldTemplate of the named field.
   * @throws FormException if the field name is unknown.
   */
  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    GenericFieldTemplate fieldTemplate = null;

    if (!fieldName.equals("SpecificDetails")) {
      fieldTemplate = new GenericFieldTemplate(fieldName, TextField.TYPE);
      fieldTemplate.setLabel(label.getString(fieldName));
      fieldTemplate.setDisplayerName("simpletext");
      fieldTemplate.setReadOnly(true);
    }

    return fieldTemplate;
  }

  /**
   * Returns the field index of the named field.
   * @throws FormException if the field name is unknown.
   */
  @Override
  public int getFieldIndex(String fieldName) throws FormException {
    return -1;
  }

  /**
   * Returns an empty DataRecord built on this template.
   */
  @Override
  public DataRecord getEmptyRecord() throws FormException {
    return null;
  }

  /**
   * Returns true if the data record is built on this template and all the constraints are ok.
   */
  @Override
  public boolean checkDataRecord(DataRecord record) {
    return true;
  }

  /**
   * Returns the Form
   */
  public Form getViewForm() {
    return viewForm;
  }

  /**
   * Returns the UserRecord
   */
  public UserRecord getRecord(String idUser) {
    UserDetail userDetail = UserFull.getById(idUser);
    return new UserRecord(userDetail);
  }
}
