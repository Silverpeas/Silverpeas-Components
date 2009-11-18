
package com.silverpeas.components.organizationchart;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class OrganizationChartInstanciator implements ComponentsInstanciatorIntf {

  public OrganizationChartInstanciator() {
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {
	SilverTrace.info("organizationchart","OrganizationChartInstanciator.create()","root.MSG_GEN_ENTER_METHOD", "space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//aucune information stockée en base pour organigramme via LDAP
	
	SilverTrace.info("organizationchart","OrganizationChartInstanciator.create()","root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("organizationchart","OrganizationChartInstanciator.delete()","root.MSG_GEN_ENTER_METHOD","space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//aucune information stockée en base pour organigramme via LDAP

	SilverTrace.info("organizationchart","OrganizationChartInstanciator.delete()","root.MSG_GEN_EXIT_METHOD");
  }
}