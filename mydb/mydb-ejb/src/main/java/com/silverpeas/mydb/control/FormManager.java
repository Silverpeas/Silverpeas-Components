/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.mydb.control;

import java.sql.Types;
import java.text.ParseException;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.fieldType.DateField;
import com.silverpeas.form.fieldType.JdbcRefField;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.form.form.XmlForm;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.mydb.data.date.DateFormatter;
import com.silverpeas.mydb.data.db.DbColumn;
import com.silverpeas.mydb.data.db.DbForeignKey;
import com.silverpeas.mydb.data.db.DbLine;
import com.silverpeas.mydb.data.db.DbTable;
import com.silverpeas.mydb.data.db.DbUtil;
import com.silverpeas.mydb.exception.MyDBException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Database record form manager.
 * @author Antoine HEDIN
 */
public class FormManager {

  public static final String FIELD_PREFIX = "db_";

  private GenericRecordTemplate template = null;

  private DateFormatter dateFormatter;

  public FormManager(DateFormatter dateFormatter) {
    this.dateFormatter = dateFormatter;
  }

  /**
   * @param dbTable The table which the record belongs to.
   * @param resources The resources wrapper.
   * @param consultation The flag indicating if data have to be displayed as labels or input fields.
   * @param newRecord The flag indicating if the record is a new or a modified one.
   * @param beanName The bean name.
   * @param componentId The component name.
   * @param method The method name.
   * @return The XML form describing the concerned database record.
   * @throws MyDBException
   */
  public Form getForm(DbTable dbTable, ResourcesWrapper resources,
      boolean consultation, boolean newRecord, String beanName,
      String componentId, String method) throws MyDBException {
    try {
      return new XmlForm(getRecordTemplate(dbTable, resources, consultation,
          newRecord, beanName, componentId, method));
    } catch (Exception e) {
      throw new MyDBException("FormManager", SilverpeasException.ERROR,
          "myDB.EX_GET_FORM", e);
    }
  }

  /**
   * @param dbTable The table which the record belongs to.
   * @param resources The resources wrapper.
   * @param consultation The flag indicating if data have to be displayed as labels or input fields.
   * @param newRecord The flag indicating if the record is a new or a modified one.
   * @param beanName The bean name.
   * @param componentId The component name.
   * @param method The method name.
   * @return The template needed to create the XML form describing the concerned database record.
   * @throws MyDBException
   */
  public RecordTemplate getRecordTemplate(DbTable dbTable,
      ResourcesWrapper resources, boolean consultation, boolean newRecord,
      String beanName, String componentId, String method) throws MyDBException {
    template = new GenericRecordTemplate();

    if (dbTable != null) {
      try {
        DbColumn[] columns = dbTable.getColumns();
        DbColumn column;
        String columnName;
        int columnType;
        String fieldType;
        for (int i = 0, n = columns.length; i < n; i++) {
          column = columns[i];
          columnName = column.getName();
          columnType = column.getDataType();
          if (consultation) {
            fieldType = TextField.TYPE;
          } else {
            if (column.hasImportedForeignKey()) {
              fieldType = JdbcRefField.TYPE;
            } else {
              if (columnType == Types.DATE) {
                fieldType = DateField.TYPE;
              } else {
                fieldType = TextField.TYPE;
              }
            }
          }

          GenericFieldTemplate ft = new GenericFieldTemplate(FIELD_PREFIX
              + columnName, fieldType);

          if (consultation) {
            ft.setDisplayerName("simpletext");
            ft.setReadOnly(true);
          } else {
            if (fieldType.equals(JdbcRefField.TYPE)) {
              DbForeignKey foreignKey = column.getImportedForeignKey();
              ft.setReadOnly(true);
              ft.setMandatory(!column.isNullable());

              ft.addParameter("beanName", beanName);
              ft.addParameter("componentId", componentId);
              ft.addParameter("method", method);
              ft.addParameter("tableName", foreignKey.getTableName());
              String[][] linkedColumnsNames = dbTable
                  .getForeignKeyColumnsNames(foreignKey.getKeyName());
              StringBuffer fieldsNamesSb = new StringBuffer();
              StringBuffer columnsNamesSb = new StringBuffer();
              for (int j = 0, m = linkedColumnsNames.length; j < m; j++) {
                if (j > 0) {
                  fieldsNamesSb.append(DbUtil.KEY_SEPARATOR);
                  columnsNamesSb.append(DbUtil.KEY_SEPARATOR);
                }
                fieldsNamesSb.append(FIELD_PREFIX).append(
                    linkedColumnsNames[j][0]);
                columnsNamesSb.append(linkedColumnsNames[j][1]);
              }
              ft.addParameter("fieldsNames", fieldsNamesSb.toString());
              ft.addParameter("columnsNames", columnsNamesSb.toString());
            } else {
              switch (columnType) {
                case Types.INTEGER:
                  ft.addParameter(TextField.CONTENT_TYPE,
                      TextField.CONTENT_TYPE_INT);
                  break;
                case Types.FLOAT:
                case Types.DOUBLE:
                  ft.addParameter(TextField.CONTENT_TYPE,
                      TextField.CONTENT_TYPE_FLOAT);
                  break;
              }
              boolean readOnly = ((!newRecord && column.isReadOnly()) || column
                  .isAutoIncrement());
              ft.setReadOnly(readOnly);
              ft.setMandatory(!readOnly && !column.isNullable());
              if (column.hasDataSize()) {
                if (columnType != Types.DATE) {
                  ft.addParameter("maxLength", column.getDataSizeAsString());
                }
              }
            }
          }

          ft.addLabel(columnName, null);

          // add the new FieldTemplate in RecordTemplate
          template.addFieldTemplate(ft);
        }
      } catch (FormException fe) {
        throw new MyDBException("FormManager", SilverpeasException.ERROR,
            "myDB.EX_GET_FORM", fe);
      }
    } else {
      template = null;
    }
    return template;
  }

  /**
   * @param dbLine The database record line.
   * @return The data record corresponding to the line.
   * @throws FormException
   */
  public DataRecord getDataRecord(DbLine dbLine) throws FormException {
    DataRecord record = template.getEmptyRecord();
    if (dbLine == null) {
      return record;
    }
    fillDataRecord(record, dbLine.getAllData());
    return record;
  }

  /**
   * @param formParameters the names and values of the form parameters.
   * @return The data record corresponding to the database record.
   * @throws FormException
   */
  public DataRecord getDataRecord(String[][] formParameters)
      throws FormException {
    DataRecord record = template.getEmptyRecord();
    fillDataRecord(record, formParameters);
    return record;
  }

  /**
   * Fills the data record fields with the data given as parameters.
   * @param defaultRecord The default record.
   * @param data The data used to fill the data record.
   * @throws FormException
   */
  private void fillDataRecord(DataRecord defaultRecord, String[][] data)
      throws FormException {
    for (int i = 0, n = data.length; i < n; i++) {
      Field field = defaultRecord.getField(FIELD_PREFIX + data[i][0]);
      if (field != null) {
        if (field.getTypeName().equals(DateField.TYPE)) {
          try {
            field.setStringValue(dateFormatter.stringToFormString(data[i][1]));
          } catch (ParseException e) {
            SilverTrace.warn("myDB", "FormManager.fillDataRecord()",
                "myDB.MSG_CANNOT_FORMAT_DATE", "Date=" + data[i][1], e);
            field.setStringValue("");
          }
        } else {
          field.setStringValue(data[i][1]);
        }
      }
    }
  }

}