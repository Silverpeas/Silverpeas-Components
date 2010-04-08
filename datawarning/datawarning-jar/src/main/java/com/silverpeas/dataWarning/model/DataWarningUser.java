package com.silverpeas.dataWarning.model;

import com.stratelia.webactiv.persistence.SilverpeasBean;
import com.stratelia.webactiv.persistence.SilverpeasBeanDAO;

public class DataWarningUser extends SilverpeasBean {

  private String instanceId;
  private int userId;

  public DataWarningUser()
  {
	  super();
  }

  public DataWarningUser(String instanceId, int userId)
  {
	  this.instanceId = instanceId;
	  this.userId = userId;
  }
  
  public String getInstanceId()
  {
	return instanceId;
  }
  public int getUserId()
  {
	return userId;
  }


  public void setInstanceId(String instanceId)
  {
	this.instanceId = instanceId;
  }
  public void setUserId(int userId)
  {
	this.userId = userId;
  }


  public String _getTableName()
  {
	return "SC_DataWarning_Rel_User";
  }


  public int _getConnectionType()
  {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }
}
