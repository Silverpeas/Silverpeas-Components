/**
 * Copyright (C) 2000 - 2015 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.processmanager;

import com.silverpeas.admin.components.ComponentInstancePostConstruction;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.stratelia.webactiv.beans.admin.AdministrationServiceProvider;

import javax.inject.Named;

import static com.silverpeas.admin.components.ComponentInstancePostConstruction
    .WORKFLOW_POST_CONSTRUCTION;

/**
 * Find and creates for the spawned ProcessManager instance a process model, id est instantiates
 * the workflow defined into an the XML descriptor associated with the ProcessManager instance. If
 * an error occurs while creating the workflow, a RuntimeException is thrown.
 * @author mmoquillon
 */
@Named(WORKFLOW_POST_CONSTRUCTION)
public class ProcessManagerInstancePostConstruction implements ComponentInstancePostConstruction {

  @Override
  public void postConstruct(final String componentInstanceId) {
    String xmlFilename = null;
    try {
      xmlFilename = AdministrationServiceProvider.getAdminService()
          .getComponentParameterValue(componentInstanceId, "XMLFileName");
      Workflow.getProcessModelManager().createProcessModel(xmlFilename, componentInstanceId);
    } catch (WorkflowException e) {
      throw new RuntimeException("Process model creation failure from " + xmlFilename +
      " for instance " + componentInstanceId);
    }
  }
}
