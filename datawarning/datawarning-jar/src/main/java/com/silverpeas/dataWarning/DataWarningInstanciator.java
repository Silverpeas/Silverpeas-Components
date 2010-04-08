package com.silverpeas.dataWarning;

import java.sql.Connection;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

import com.stratelia.silverpeas.silvertrace.*;
import com.silverpeas.dataWarning.model.*;
import com.stratelia.webactiv.util.exception.*;

/** 
 *
 * @author  nesseric
 * 
 *  */
public class DataWarningInstanciator implements ComponentsInstanciatorIntf {

  public DataWarningInstanciator() {
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("dataWarning","DataWarningInstanciator.create()","root.MSG_GEN_ENTER_METHOD");
    try
    {
        DataWarningDataManager dataManager = new DataWarningDataManager();
        dataManager.createDataWarning(new DataWarning("", "", "", "", 0, componentId, DataWarning.INCONDITIONAL_QUERY));
        dataManager.createDataWarningScheduler(new DataWarningScheduler(componentId,1,DataWarningScheduler.SCHEDULER_N_TIMES_MOMENT_HOUR,0,0,2,0,0,DataWarningScheduler.SCHEDULER_STATE_OFF));
        dataManager.createDataWarningQuery(new DataWarningQuery(componentId));
    }
    catch (DataWarningException dwe)
    {
		throw new InstanciationException("DataWarningInstanciator.create()", SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", dwe);
    }
	SilverTrace.info("dataWarning","DataWarningInstanciator.create()","root.MSG_GEN_EXIT_METHOD");
   
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("dataWarning","DataWarningInstanciator.delete()","root.MSG_GEN_ENTER_METHOD");
    try
    {
        DataWarningDataManager dataManager = new DataWarningDataManager();
        dataManager.deleteDataWarningUsers(componentId);
        dataManager.deleteDataWarningGroups(componentId);
        dataManager.deleteDataWarningQuery(componentId);
        dataManager.deleteDataWarningScheduler(componentId);
        dataManager.deleteDataWarning(componentId);
    }
    catch (DataWarningException dwe)
    {
		throw new InstanciationException("DataWarningInstanciator.delete()", SilverpeasException.ERROR, "DataWarning.EX_DATA_ACCESS_FAILED", dwe);
    }
	SilverTrace.info("dataWarning","DataWarningInstanciator.delete()","root.MSG_GEN_EXIT_METHOD");
  }
  
  
}
