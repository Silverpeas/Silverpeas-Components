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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import java.sql.Connection;

import com.silverpeas.versioning.VersioningInstanciator;
import com.silverpeas.workflow.api.UpdatableProcessInstanceManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.util.attachment.AttachmentInstanciator;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ProcessManagerInstanciator implements ComponentsInstanciatorIntf {

  public ProcessManagerInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    String XMLFileName = null;
    try {
      Admin admin = new Admin();
      XMLFileName = admin
          .getComponentParameterValue(componentId, "XMLFileName");

      Workflow.getProcessModelManager().createProcessModel(XMLFileName,
          componentId);
    } catch (WorkflowException e) {
      throw new InstanciationException("ProcessManagerInstanciator",
          SilverpeasException.ERROR,
          "processManager.PROCESS_MODEL_CREATE_FAILED", "peasId=" + componentId
              + ", XMLFileName=" + XMLFileName, e);
    }
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    try {
      // delete forms managed by module named 'formTemplate'
      Workflow.getProcessModelManager().deleteProcessModel(componentId);

      // delete all process instances
      ProcessInstance[] processInstances = Workflow.getProcessInstanceManager()
          .getProcessInstances(componentId, null, "supervisor");
      ProcessInstance instance = null;
      for (int p = 0; p < processInstances.length; p++) {
        instance = (ProcessInstance) processInstances[p];
        ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager())
            .removeProcessInstance(instance.getInstanceId());
      }

      // delete attachments
      AttachmentInstanciator attachmentI = new AttachmentInstanciator();
      attachmentI.delete(con, spaceId, componentId, userId);

      // delete versioning
      VersioningInstanciator versioningI = new VersioningInstanciator();
      versioningI.delete(con, spaceId, componentId, userId);

      // delete todos
      TodoBackboneAccess tbba = new TodoBackboneAccess();
      tbba.removeEntriesByInstanceId(componentId);
    } catch (WorkflowException e) {
      throw new InstanciationException("ProcessManagerInstanciator",
          SilverpeasException.ERROR,
          "processManager.PROCESS_MODEL_DELETE_FAILED",
          "peasId=" + componentId, e);
    }
  }
}