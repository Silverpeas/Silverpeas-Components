package com.silverpeas.rssAgregator;

import java.sql.Connection;

import com.silverpeas.rssAgregator.control.RssAgregatorBm;
import com.silverpeas.rssAgregator.control.RssAgregatorBmImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/** 
 *
 * @author  nesseric
 * updated by Sébastien Antonio
 */
public class RssAgregatorInstanciator implements ComponentsInstanciatorIntf
{

	public RssAgregatorInstanciator()
	{
	}

	public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException
	{
		SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.create()", "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);
		SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
	}

	public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException
	{
		SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);
		RssAgregatorBm rss = new RssAgregatorBmImpl();
		try {
			rss.deleteChannels(componentId);
		} catch (Exception e) {
			throw new InstanciationException("RssAgregatorInstanciator", SilverpeasException.ERROR, "", e);
		}
		SilverTrace.info("RssAgregator", "RssAgregatorInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
	}

}
