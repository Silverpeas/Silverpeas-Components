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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.form.HtmlForm;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.logging.SilverLogger;

public class UserTemplate implements RecordTemplate {
  private static final long serialVersionUID = -6724850073409580787L;

  private static final String SPECIFIC_DETAILS = "SpecificDetails";
  private final transient LocalizationBundle label;
  private transient HtmlForm viewForm;

  /**
   * A UserTemplate is built from a file name and a language.
   * @param fileName the name of a file in which are defined the template of each fields.
   * @param language the ISO-631 code of a language.
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
    fieldNames[8] = SPECIFIC_DETAILS;

    return fieldNames;
  }

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
      fieldTemplates[8] = getFieldTemplate(SPECIFIC_DETAILS);
    } catch (FormException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
    }
    return fieldTemplates;
  }

  @Override
  public FieldTemplate getFieldTemplate(String fieldName) throws FormException {
    GenericFieldTemplate fieldTemplate = null;

    if (!fieldName.equals(SPECIFIC_DETAILS)) {
      fieldTemplate = new GenericFieldTemplate(fieldName, TextField.TYPE);
      fieldTemplate.setLabel(label.getString(fieldName));
      fieldTemplate.setDisplayerName("simpletext");
      fieldTemplate.setReadOnly(true);
    }

    return fieldTemplate;
  }

  @Override
  public int getFieldIndex(String fieldName) {
    return -1;
  }

  @Override
  public DataRecord getEmptyRecord() {
    return null;
  }

  @Override
  public boolean checkDataRecord(DataRecord record) {
    return true;
  }

  /**
   * Gets the form dedicated to be rendered to the end users.
   * @return a {@link Form} instance.
   */
  public Form getViewForm() {
    return viewForm;
  }

  /**
   * Gets a {@link UserRecord} of the specified user built on this template.
   * @param idUser the unique identifier of a user in Silverpeas.
   * @return the {@link UserRecord} instance for the given user.
   */
  public UserRecord getRecord(String idUser) {
    UserDetail userDetail = UserFull.getById(idUser);
    return new UserRecord(userDetail);
  }
}
