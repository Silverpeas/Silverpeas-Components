/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.processManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.filter.FilterManager;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.State;

/**
 * A ProcessFilter is used to select some process from all the process.
 */
public class ProcessFilter {
  /**
   * Builds a process filter which can be used to select process intance of a given process model.
   */
  public ProcessFilter(ProcessModel model, String role, String lang)
      throws ProcessManagerException {
    RecordTemplate rowTemplate = model.getRowTemplate(role, lang);
    filter = new FilterManager(rowTemplate, lang);

    RecordTemplate folderTemplate = null;
    try {
      folderTemplate = model.getDataFolder()
          .toRecordTemplate(role, lang, false);
    } catch (WorkflowException e1) {
      throw new ProcessManagerException("ProcessFilter",
          "processFilter.FAIL_TO_CREATE_CRITERIA_FORM", e1);
    }

    try {
      // Affichage d'une liste déroulante des états possibles
      GenericFieldTemplate state = new GenericFieldTemplate("instance.state",
          "text");
      State[] states = model.getStates();
      String values = "";
      for (int s = 0; s < states.length; s++) {
        if (s != 0)
          values += "##";
        values += ((State) states[s]).getLabel(role, lang);
      }
      // state.addParameter("keys",
      // "A qualifier##Correction en attente##Correction en cours");
      state.addParameter("keys", values);
      filter.addFieldParameter("instance.state", state);

      // Affichage d'une liste déroulante pour chaque donnée multivaluée
      FieldTemplate[] fields = rowTemplate.getFieldTemplates();
      FieldTemplate field = null;
      for (int f = 2; f < fields.length; f++) {
        field = fields[f];
        FieldTemplate folderField = folderTemplate.getFieldTemplate(field
            .getFieldName());
        Map<String, String> parameters = folderField.getParameters(lang);
        if (parameters != null
            && (parameters.containsKey("values") || parameters
            .containsKey("keys"))) {
          filter.addFieldParameter(field.getFieldName(), folderField);
        }
      }
    } catch (FormException e) {
      throw new ProcessManagerException("ProcessFilter",
          "processFilter.FAIL_TO_CREATE_CRITERIA_FORM", e);
    }
  }

  /**
   * Returns the form which can be used to fill the filter criteria.
   */
  public Form getPresentationForm() throws ProcessManagerException {
    try {
      return filter.getCriteriaForm();
    } catch (FormException e) {
      throw new ProcessManagerException("ProcessFilter",
          "processFilter.FAIL_TO_CREATE_CRITERIA_FORM", e);
    }
  }

  /**
   * Get the current criteria.
   */
  public DataRecord getCriteriaRecord() throws ProcessManagerException {
    if (criteria == null) {
      try {
        criteria = filter.getEmptyCriteriaRecord();
      } catch (FormException e) {
        throw new ProcessManagerException("ProcessFilter",
            "processFilter.FAIL_TO_CREATE_CRITERIA_RECORD", e);
      }
    }
    return criteria;
  }

  /**
   * Set the current criteria.
   */
  public void setCriteriaRecord(DataRecord criteria) {
    this.criteria = criteria;
  }

  /**
   * Copy the criteria filled in another context but shared by this filter. We ignore all the form
   * exception, since this copy is only done to simplify the user life.
   */
  public void copySharedCriteria(ProcessFilter source) {
    DataRecord copiedCriteria = null;
    String[] criteriaNames = null;
    try {
      copiedCriteria = source.getCriteriaRecord();
      criteriaNames = source.filter.getCriteriaTemplate().getFieldNames();
      this.getCriteriaRecord();
    } catch (ProcessManagerException ignored) {
      return;
    } catch (FormException ignored) {
      return;
    }

    Field criteriumField;
    for (int i = 0; i < criteriaNames.length; i++) {
      try {
        criteriumField = criteria.getField(criteriaNames[i]);
        if (criteriumField != null && copiedCriteria != null)
          criteriumField.setValue(copiedCriteria.getField(criteriaNames[i])
              .getValue(""), "");
      } catch (FormException ignored) {
        continue;
      }
    }
  }

  /**
   * Returns the collapse status of the filter panel.
   */
  public String getCollapse() {
    return collapse;
  }

  /**
   * Set the collapse status of the filter panel.
   */
  public void setCollapse(String collapse) {
    if ("false".equals(collapse))
      this.collapse = "false";
    else
      this.collapse = "true";
  }

  /**
   * Returns only the process instance matching the filter.
   */
  public DataRecord[] filter(ProcessInstance[] allInstances, String role,
      String lang) throws ProcessManagerException {
    try {
      List<DataRecord> allRecords = new ArrayList<DataRecord>();
      for (int i = 0; i < allInstances.length; i++) {
        allRecords.add(allInstances[i].getRowDataRecord(role, lang));
      }

      if (getCriteriaRecord() != null) {
        allRecords = filter.filter(criteria, allRecords);
      }

      return allRecords.toArray(new DataRecord[0]);
    } catch (WorkflowException e) {
      throw new ProcessManagerException("ProcessFilter",
          "processFilter.FAIL_TO_USE_CRITERIA_RECORD", e);
    } catch (FormException e) {
      throw new ProcessManagerException("ProcessFilter",
          "processFilter.FAIL_TO_USE_CRITERIA_RECORD", e);
    }
  }

  private FilterManager filter = null;
  private String collapse = "true";
  private DataRecord criteria;
}
