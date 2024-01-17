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

package org.silverpeas.processmanager;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.filter.FilterManager;
import org.silverpeas.core.contribution.content.form.filter.RecordFilter;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A ProcessFilter is used to select some process from all the process.
 */
public class ProcessFilter {

  private final FilterManager filter;
  private boolean collapse = false;
  private DataRecord criteria;

  /**
   * Builds a process filter which can be used to select process instance of a given process model.
   */
  public ProcessFilter(ProcessModel model, String role, String lang, boolean isProcessIdVisible)
      throws ProcessManagerException {
    RecordTemplate rowTemplate = model.getRowTemplate(role, lang, isProcessIdVisible);
    filter = new FilterManager(rowTemplate, lang);

    RecordTemplate folderTemplate;
    try {
      folderTemplate = model.getDataFolder().toRecordTemplate(role, lang, false);
    } catch (WorkflowException e1) {
      throw new ProcessManagerException(
              "Fail to create criteria form of model " + model.getModelId(), e1);
    }

    try {
      // Display the dropdown list with the possible states
      GenericFieldTemplate state = new GenericFieldTemplate("instance.state", "text");
      State[] states = model.getStates();
      StringBuilder values = new StringBuilder();
      for (int s = 0; s < states.length; s++) {
        if (s != 0) {
          values.append("##");
        }
        values.append(states[s].getLabel(role, lang));
      }
      state.addParameter("keys", values.toString());
      filter.addFieldParameter("instance.state", state);

      FieldTemplate[] fields = rowTemplate.getFieldTemplates();
      for (int f = isProcessIdVisible?3:2; f < fields.length; f++) {
        FieldTemplate field = fields[f];
        FieldTemplate folderField = folderTemplate.getFieldTemplate(field.getFieldName());
        Map<String, String> parameters = folderField.getParameters(lang);
        if (parameters != null &&
                (parameters.containsKey("values") || parameters.containsKey("keys") ||
                        "jdbc".equals(folderField.getTypeName()))) {
          filter.addFieldParameter(field.getFieldName(), folderField);
        }
      }
    } catch (FormException e) {
      throw new ProcessManagerException(
              "Fail to create criteria form of model " + model.getModelId(), e);
    }
  }

  /**
   * Returns the form which can be used to fill the filter criteria.
   */
  public Form getPresentationForm() throws ProcessManagerException {
    try {
      return filter.getCriteriaForm();
    } catch (FormException e) {
      throw new ProcessManagerException("Fail to get criteria form", e);
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
        throw new ProcessManagerException("Fail to get criteria record", e);
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
  void copySharedCriteria(ProcessFilter source) {
    DataRecord copiedCriteria;
    String[] criteriaNames;
    try {
      copiedCriteria = source.getCriteriaRecord();
      criteriaNames = source.filter.getCriteriaTemplate().getFieldNames();
      this.getCriteriaRecord();
    } catch (ProcessManagerException | FormException e) {
      SilverLogger.getLogger(this).silent(e);
      return;
    }

    Field criteriumField;
    for (final String criteriaName : criteriaNames) {
      try {
        criteriumField = criteria.getField(criteriaName);
        if (criteriumField != null && copiedCriteria != null) {
          criteriumField.setValue(copiedCriteria.getField(criteriaName).getValue(""), "");
        }
      } catch (FormException e) {
        SilverLogger.getLogger(this).silent(e);
      }
    }
  }

  /**
   * Returns the collapse status of the filter panel.
   */
  public boolean isCollapse() {
    return collapse;
  }

  /**
   * Set the collapse status of the filter panel.
   */
  public void setCollapse(boolean collapse) {
    this.collapse = collapse;
  }

  /**
   * Returns only the process instance matching the filter.
   */
  public List<DataRecord> filter(List<ProcessInstance> allInstances, String role, String lang)
      throws ProcessManagerException {
    try {
      Stream<DataRecord> stream = allInstances.stream().map(p -> getDataRecord(p, role, lang));
      if (getCriteriaRecord() != null) {
        final RecordFilter recordFilter = filter.getRecordFilter(getCriteriaRecord());
        stream = stream.filter(d -> matchCriteria(recordFilter, d));
      }
      return stream.collect(Collectors.toList());
    } catch (SilverpeasRuntimeException | FormException e) {
      throw new ProcessManagerException("Fail to apply filter criteria", e);
    }
  }

  private boolean matchCriteria(final RecordFilter recordFilter, final DataRecord dataRecord) {
    try {
      return recordFilter.match(dataRecord);
    } catch (FormException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  private DataRecord getDataRecord(final ProcessInstance p, final String role, final String lang) {
    try {
      return p.getRowDataRecord(role, lang);
    } catch (WorkflowException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

}
