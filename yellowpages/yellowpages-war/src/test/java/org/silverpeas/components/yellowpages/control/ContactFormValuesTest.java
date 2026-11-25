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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the export of the values of the XML form of a contact.
 * @author mmoquillon
 */
class ContactFormValuesTest {

  private static final String INSTANCE_ID = "yellowpages12";
  private final RecordSet recordSet = mock(RecordSet.class);
  private final PublicationTemplate template = mock(PublicationTemplate.class);
  private ContactFormValues formValues;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void prepareTheForm() throws Exception {
    List<FieldTemplate> fields = List.of(fieldTemplate("service", TextField.TYPE),
        fieldTemplate("badge", Field.TYPE_FILE), fieldTemplate("poste", TextField.TYPE));
    Form viewForm = mock(Form.class);
    when(viewForm.getFieldTemplates()).thenReturn(fields);

    when(template.getViewForm()).thenReturn(viewForm);
    when(template.getRecordSet()).thenReturn(recordSet);
    DataRecord contactData =
        record(Map.of("service", "Maintenance\n", "badge", "badge.png", "poste", "Technicien"));
    DataRecord emptyData = record(Map.of("service", "", "badge", "", "poste", ""));
    when(recordSet.getRecord(anyString())).thenReturn(null);
    when(recordSet.getRecord("42")).thenReturn(contactData);
    when(recordSet.getEmptyRecord()).thenReturn(emptyData);

    FieldDisplayer<Field> textDisplayer = mock(FieldDisplayer.class);
    doAnswer(invocation -> {
      PrintWriter out = invocation.getArgument(0);
      Field field = invocation.getArgument(1);
      out.println(field.getValue());
      return null;
    }).when(textDisplayer).display(any(), any(), any(), any());
    TypeManager typeManager = mock(TypeManager.class);
    when(typeManager.<Field>getDisplayer(anyString(), anyString())).thenReturn(textDisplayer);

    formValues = new ContactFormValues(typeManager);
  }

  @Test
  void theValuesOfTheFormOfAContactAreExportedWithoutTheFiles() throws Exception {
    List<String> values = formValues.getValuesOf(contact("42"), template);
    assertThat(values, contains("Maintenance", "Technicien"));
  }

  @Test
  void aContactWithoutFormDataHasEmptyValues() throws Exception {
    List<String> values = formValues.getValuesOf(contact("43"), template);
    assertThat(values, contains("", ""));
  }

  private static ContactDetail contact(String id) {
    return new ContactDetail(new ContactPK(id, INSTANCE_ID), "John", "Doe", "john@doe.org", "",
        "", null, null, null);
  }

  private static FieldTemplate fieldTemplate(String name, String type) {
    FieldTemplate fieldTemplate = mock(FieldTemplate.class);
    when(fieldTemplate.getFieldName()).thenReturn(name);
    when(fieldTemplate.getTypeName()).thenReturn(type);
    return fieldTemplate;
  }

  private static DataRecord record(Map<String, String> values) throws Exception {
    DataRecord data = mock(DataRecord.class);
    for (Map.Entry<String, String> value : values.entrySet()) {
      Field field = mock(Field.class);
      when(field.getValue()).thenReturn(value.getValue());
      when(data.getField(value.getKey())).thenReturn(field);
    }
    return data;
  }
}
