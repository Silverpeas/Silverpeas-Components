package com.silverpeas.silvercrawler;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class SilverCrawlerInstanciator implements ComponentsInstanciatorIntf {

  public SilverCrawlerInstanciator() {
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {
	SilverTrace.info("silverCrawler","SilverCrawlerInstanciator.create()","root.MSG_GEN_ENTER_METHOD", "space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//insert your code here !
	
  }


  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("silverCrawler","SilverCrawlerInstanciator.delete()","root.MSG_GEN_ENTER_METHOD","space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//insert your code here !

	SilverTrace.info("silverCrawler","SilverCrawlerInstanciator.delete()","root.MSG_GEN_EXIT_METHOD");
  }
  
}