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
package com.silverpeas.processManager;

import com.silverpeas.admin.components.ComponentInstancePreDestruction;
import com.silverpeas.workflow.api.UpdatableProcessInstanceManager;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import org.silverpeas.attachment.AttachmentService;

import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * Removes all the process instances defined for the ProcessManager instance that is being
 * deleted.
 * @author mmoquillon
 */
@Named
public class ProcessManagerInstancePreDestruction implements ComponentInstancePreDestruction {

  /**
   * Performs pre destruction tasks in the behalf of the specified ProcessManager instance.
   * @param componentInstanceId the unique identifier of the ProcessManager instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    try {
      ProcessInstance[] processInstances = Workflow.getProcessInstanceManager()
          .getProcessInstances(componentInstanceId, null, "supervisor");
      for (ProcessInstance instance : processInstances) {
        ((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager()).
            removeProcessInstance(instance.getInstanceId());
      }
      Workflow.getProcessModelManager().deleteProcessModel(componentInstanceId);
    } catch (WorkflowException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
