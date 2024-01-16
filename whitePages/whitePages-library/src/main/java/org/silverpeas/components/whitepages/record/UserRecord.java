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

import java.util.HashMap;
import java.util.Map;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;

public class UserRecord implements DataRecord {

  private static final long serialVersionUID = 4372981095216600600L;
  private UserDetail user = null;

  public boolean isConnected() {
    return user.isConnected();
  }

  /**
   * A UserRecord is built from a UserDetail
   */
  public UserRecord(UserDetail user) {
    this.user = user;
  }

  public UserDetail getUserDetail() {
    return this.user;
  }

  /**
   * Returns the data record id. The record is known by its external id.
   */
  @Override
  public String getId() {
    return user.getId();
  }

  /**
   * Gives an id to the record. Caution ! the record is known by its external id.
   */
  @Override
  public void setId(String id) {
  }

  /**
   * Returns all the fields
   */
  public Field[] getFields() {

    return null;
  }

  /**
   * Returns the named field.
   * @throws FormException when the fieldName is unknown.
   */
  @Override
  public Field getField(String fieldName) throws FormException {
    TextFieldImpl text = new TextFieldImpl();

    if ("Id".equals(fieldName)) {
      text.setStringValue(user.getId());
    } else if ("SpecificId".equals(fieldName)) {
      text.setStringValue(user.getSpecificId());
    } else if ("DomainId".equals(fieldName)) {
      text.setStringValue(user.getDomainId());
    } else if ("Login".equals(fieldName)) {
      text.setStringValue(user.getLogin());
    } else if ("FirstName".equals(fieldName)) {
      text.setStringValue(user.getFirstName());
    } else if ("LastName".equals(fieldName)) {
      text.setStringValue(user.getLastName());
    } else if ("Mail".equals(fieldName)) {
      text.setStringValue(user.geteMail());
    } else if ("AccessLevel".equals(fieldName)) {
      text.setStringValue(user.getAccessLevel().code());
    } else if (fieldName.startsWith("DC.")) {
      String specificDetail = fieldName.substring(fieldName.indexOf(".") + 1);
      if (user instanceof UserFull) {
        text.setStringValue(((UserFull) user).getValue(specificDetail));
      }
    }
    return text;
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    return null;
  }

  /**
   * Returns the field at the index position in the record.
   * @throws FormException when the fieldIndex is unknown.
   */
  @Override
  public Field getField(int fieldIndex) throws FormException {
    return null;
  }

  @Override
  public String[] getFieldNames() {
    return null;
  }

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  @Override
  public boolean isNew() {
    return false;
  }

  /**
   * Gets the internal id. May be used only by a package class !
   */
  int getInternalId() {
    return -1;
  }

  /**
   * Sets the internal id. May be used only by a package class !
   */
  void setInternalId(int id) {
  }

  @Override
  public String getLanguage() {
    return I18NHelper.defaultLanguage;
  }

  @Override
  public void setLanguage(String language) {
  }

  @Override
  public Map<String, String> getValues(String language) {
    return new HashMap<String, String>();
  }

  @Override
  public ResourceReference getResourceReference() {
    return null;
  }
}
