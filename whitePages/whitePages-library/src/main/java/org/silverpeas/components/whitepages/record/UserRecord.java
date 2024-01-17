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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.i18n.I18NHelper;

import java.util.HashMap;
import java.util.Map;

public class UserRecord implements DataRecord {

  private static final long serialVersionUID = 4372981095216600600L;
  private final User user;

  public boolean isConnected() {
    return user.isConnected();
  }

  /**
   * A UserRecord is built from a {@link UserDetail}
   * @param user a user in Silverpeas.
   */
  public UserRecord(User user) {
    this.user = user;
  }

  public User getUserDetail() {
    return this.user;
  }

  @Override
  public String getId() {
    return user.getId();
  }

  @Override
  public void setId(String id) {
    // this record has no id. Id is the one of the underlying user.
  }

  public Field[] getFields() {
    return new Field[0];
  }

  @Override
  public Field getField(String fieldName) throws FormException {
    TextFieldImpl text = new TextFieldImpl();
    User requester = User.getCurrentRequester();
    boolean isAdmin = requester != null && (requester.isAccessAdmin() ||
        requester.isAccessDomainManager() && requester.getDomainId().equals(user.getDomainId()));
    boolean sensitiveData =
        user instanceof UserDetail && !isAdmin && ((UserDetail) user).hasSensitiveData();
    if ("Id".equals(fieldName)) {
      text.setStringValue(user.getId());
    } else if ("SpecificId".equals(fieldName) && user instanceof UserDetail) {
      text.setStringValue(((UserDetail)user).getSpecificId());
    } else if ("DomainId".equals(fieldName)) {
      text.setStringValue(user.getDomainId());
    } else if ("Login".equals(fieldName)) {
      text.setStringValue(user.getLogin());
    } else if ("FirstName".equals(fieldName)) {
      text.setStringValue(user.getFirstName());
    } else if ("LastName".equals(fieldName)) {
      text.setStringValue(user.getLastName());
    } else if ("Mail".equals(fieldName) && !sensitiveData) {
      text.setStringValue(user.getEmailAddress());
    } else if ("AccessLevel".equals(fieldName)) {
      text.setStringValue(user.getAccessLevel().code());
    } else if (fieldName.startsWith("DC.")) {
      String specificDetail = fieldName.substring(fieldName.indexOf(".") + 1);
      setSpecificDetail(specificDetail, sensitiveData, text);
    }
    return text;
  }

  private void setSpecificDetail(String specificDetail, boolean sensitiveData, TextFieldImpl text) {
    if (user instanceof UserFull) {
      UserFull userFull = (UserFull) user;
      DomainProperty prop = userFull.getProperty(specificDetail);
      if (!prop.isSensitive() || !sensitiveData) {
        text.setStringValue(((UserFull) user).getValue(specificDetail));
      }
    }
  }

  @Override
  public Field getField(String fieldName, int occurrence) {
    return null;
  }

  @Override
  public Field getField(int fieldIndex) {
    return null;
  }

  @Override
  public int size() {
    return -1;
  }

  @Override
  public String[] getFieldNames() {
    return new String[0];
  }

  @Override
  public boolean isNew() {
    return false;
  }

  @Override
  public String getLanguage() {
    return I18NHelper.DEFAULT_LANGUAGE;
  }

  @Override
  public void setLanguage(String language) {
    // user has no l10n content
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
