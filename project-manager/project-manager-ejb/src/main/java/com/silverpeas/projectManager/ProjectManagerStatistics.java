package com.silverpeas.projectManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.silverpeas.projectManager.control.ejb.ProjectManagerBm;
import com.silverpeas.projectManager.control.ejb.ProjectManagerBmHome;
import com.silverpeas.projectManager.model.ProjectManagerRuntimeException;
import com.silverpeas.projectManager.model.TaskDetail;
import com.stratelia.silverpeas.silverstatistics.control.ComponentStatisticsInterface;
import com.stratelia.silverpeas.silverstatistics.control.UserIdCountVolumeCouple;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class ProjectManagerStatistics implements ComponentStatisticsInterface
{

	public Collection getVolume(String spaceId, String componentId) throws Exception
	{
		ArrayList  myArrayList = new ArrayList();
				
		Collection tasks = getProjectManagerBm().getAllTasks(componentId, null);
		Iterator   iter = tasks.iterator();
        while (iter.hasNext())
        {
            TaskDetail task = (TaskDetail) iter.next();
            UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
            myCouple.setUserId(Integer.toString(task.getOrganisateurId()));
            myCouple.setCountVolume(1);
            myArrayList.add(myCouple);
        }

        return myArrayList;
	}
	
	private ProjectManagerBm getProjectManagerBm()
	{
		ProjectManagerBm projectManagerBm = null;
		try {
			ProjectManagerBmHome projectManagerBmHome = (ProjectManagerBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PROJECTMANAGERBM_EJBHOME, ProjectManagerBmHome.class);
			projectManagerBm = projectManagerBmHome.create();
		} catch (Exception e) {
			throw new ProjectManagerRuntimeException("ProjectManagerSessionController.getProjectManagerBm()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
		}
		return projectManagerBm;
	}
    
}
