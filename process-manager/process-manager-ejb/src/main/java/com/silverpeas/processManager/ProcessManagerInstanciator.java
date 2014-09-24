/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.workflow.api.UpdatableProcessInstanceManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.attachment.SimpleDocumentInstanciator;

import java.sql.Connection;

public class ProcessManagerInstanciator implements ComponentsInstanciatorIntf {

  public ProcessManagerInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    String xmlFilename = null;
    try {
      xmlFilename = AdminReference.getAdminService().getComponentParameterValue(componentId, "XMLFileName");
      Workflow.getProcessModelManager().createProcessModel(xmlFilename, componentId);
    } catch (WorkflowException e) {
      throw new InstanciationException("ProcessManagerInstanciator", SilverpeasException.ERROR,
          "processManager.PROCESS_MODEL_CREATE_FAILED",
          "peasId=" + componentId + ", XMLFileName=" + xmlFilename, e);
    }
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    try {
      Workflow.getProcessModelManager().deleteProcessModel(componentId);

      ProcessInstance[] processInstances = Workflow.getProcessInstanceManager().getProcessInstances(
          componentId, null, "supervisor");
      for (ProcessInstance instance : processInstances) {
        ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager()).
            removeProcessInstance(instance.getInstanceId());
      }
      new SimpleDocumentInstanciator().delete(componentId);
      TodoBackboneAccess tbba = new TodoBackboneAccess();
      tbba.removeEntriesByInstanceId(componentId);
    } catch (WorkflowException e) {
      throw new InstanciationException("ProcessManagerInstanciator", SilverpeasException.ERROR,
          "processManager.PROCESS_MODEL_DELETE_FAILED", "peasId=" + componentId, e);
    }
  }
}