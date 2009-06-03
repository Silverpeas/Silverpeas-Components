package com.silverpeas.webpages;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class WebPagesInstanciator implements ComponentsInstanciatorIntf {

  public WebPagesInstanciator() {
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {
	SilverTrace.info("webPages","WebPagesInstanciator.create()","root.MSG_GEN_ENTER_METHOD", "space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	//insert your code here !
	
	SilverTrace.info("webPages","WebPagesInstanciator.create()","root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("webPages","WebPagesInstanciator.delete()","root.MSG_GEN_ENTER_METHOD","space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	try
	{
		WysiwygController.deleteFileAndAttachment(componentId, componentId);
	}
	catch (Exception e)
	{
		throw new InstanciationException("WebPagesInstanciator.delete", SilverpeasException.ERROR, "webPages.WYSIWYG_DELETION_FAILED", e);		
	}

	SilverTrace.info("webPages","WebPagesInstanciator.delete()","root.MSG_GEN_EXIT_METHOD");
  }
}
