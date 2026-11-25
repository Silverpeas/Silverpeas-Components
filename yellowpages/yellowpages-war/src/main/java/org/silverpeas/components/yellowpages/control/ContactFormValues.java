/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.yellowpages.control;

import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * The values of the XML form of the contacts, as they are exported in plain text. The fields of
 * type file aren't exported.
 * @author mmoquillon
 */
class ContactFormValues {

  private final TypeManager typeManager;

  /**
   * @param typeManager the manager of the types of the fields of the XML forms.
   */
  ContactFormValues(TypeManager typeManager) {
    this.typeManager = typeManager;
  }

  /**
   * Gets the values of the XML form of the specified contact. If the contact has no data for
   * this form, the values are those of an empty form.
   * @param contact a contact.
   * @param template the XML form of the contact.
   * @return the values of the fields of the form, rendered as plain text and in the order of
   * the fields.
   * @throws PublicationTemplateException if the XML form can't be read.
   * @throws FormException if the data of the contact can't be read.
   */
  List<String> getValuesOf(ContactDetail contact, PublicationTemplate template)
      throws PublicationTemplateException, FormException {
    RecordSet recordSet = template.getRecordSet();
    DataRecord data = recordSet.getRecord(contact.getPK().getId());
    if (data == null) {
      // the contact was never filled in with this form
      data = recordSet.getEmptyRecord();
    }

    List<String> values = new ArrayList<>();
    for (FieldTemplate fieldTemplate : template.getViewForm().getFieldTemplates()) {
      String fieldType = fieldTemplate.getTypeName();
      Field field = data.getField(fieldTemplate.getFieldName());
      if (field != null && !fieldType.equals(Field.TYPE_FILE)) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw, true);
        FieldDisplayer<Field> fieldDisplayer = typeManager.getDisplayer(fieldType, "simpletext");
        if (fieldDisplayer != null) {
          fieldDisplayer.display(out, field, fieldTemplate, new PagesContext());
        }
        // removing ending carriage return appended by out.println() of displayers
        values.add(sw.toString().replaceFirst("\\s+$", ""));
      }
    }
    return values;
  }
}
