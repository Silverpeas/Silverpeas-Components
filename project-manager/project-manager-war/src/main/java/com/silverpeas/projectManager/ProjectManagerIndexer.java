/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.projectManager;

import com.silverpeas.projectManager.control.ProjectManagerSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

/**
 * @author neysseri
 *
 */
public class ProjectManagerIndexer implements ComponentIndexerInterface {

	private ProjectManagerSessionController pm = null;
	
	/* (non-Javadoc)
	 * @see com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface#index(com.stratelia.silverpeas.peasCore.MainSessionController, com.stratelia.silverpeas.peasCore.ComponentContext)
	 */
	public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception {
		pm = new ProjectManagerSessionController(mainSessionCtrl, context);
		
		pm.index();	
	}
}
