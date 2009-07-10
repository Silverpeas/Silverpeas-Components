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

public class ProcessManagerInstanciator implements ComponentsInstanciatorIntf
{

	public ProcessManagerInstanciator()
	{
	}

	public void create(Connection con,
						String spaceId,
						String componentId,
						String userId)
		throws InstanciationException
	{
		String XMLFileName = null;
		try
		{ 
			Admin admin = new Admin();         
			XMLFileName = admin.getComponentParameterValue(componentId, "XMLFileName");

			Workflow.getProcessModelManager().createProcessModel(XMLFileName, componentId);
		}
		catch (WorkflowException e)
		{
			throw new InstanciationException("ProcessManagerInstanciator",
											SilverpeasException.ERROR,
											"processManager.PROCESS_MODEL_CREATE_FAILED",
											"peasId="+componentId+", XMLFileName="+XMLFileName,
											e);
		}        
	}

	public void delete(Connection con,
						String spaceId,
						String componentId,
						String userId)
		throws InstanciationException
	{
		try
		{ 
			//delete forms managed by module named 'formTemplate'
			Workflow.getProcessModelManager().deleteProcessModel(componentId);
			
			//delete all process instances
			ProcessInstance[] processInstances = Workflow.getProcessInstanceManager().getProcessInstances(componentId, null, "supervisor");
			ProcessInstance instance = null;
			for (int p=0; p<processInstances.length; p++)
			{
				instance = (ProcessInstance) processInstances[p];
				((UpdatableProcessInstanceManager) Workflow.getProcessInstanceManager()).removeProcessInstance(instance.getInstanceId());
			}

			//delete attachments
			AttachmentInstanciator attachmentI = new AttachmentInstanciator();
			attachmentI.delete(con, spaceId, componentId, userId);
			
			//delete versioning
			VersioningInstanciator versioningI = new VersioningInstanciator();
			versioningI.delete(con, spaceId, componentId, userId);
			
			//delete todos
			TodoBackboneAccess tbba = new TodoBackboneAccess();
			tbba.removeEntriesByInstanceId(componentId);
		}
		catch (WorkflowException e)
		{
			throw new InstanciationException("ProcessManagerInstanciator", SilverpeasException.ERROR,
											"processManager.PROCESS_MODEL_DELETE_FAILED",
											"peasId="+componentId, e);
		}        
	}
}