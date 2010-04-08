package com.silverpeas.dataWarning.model;

import com.stratelia.webactiv.persistence.*;

public class DataWarningGroup extends SilverpeasBean {

  private String instanceId;
  private int groupId;

	public DataWarningGroup()
	{
		super();
	}

  public DataWarningGroup(String instanceId, int groupId)
  {
	this.instanceId = instanceId;
	this.groupId = groupId;
  }

  public String getInstanceId() {
	return instanceId;
  }
  public int getGroupId() {
	return groupId;
  }

  public void setInstanceId(String instanceId) {
	this.instanceId = instanceId;
  }
  public void setGroupId(int groupId) {
	this.groupId = groupId;
  }

  public String _getTableName() {
	return "SC_DataWarning_Rel_Group";
  }

  public int _getConnectionType()
  {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }
}
