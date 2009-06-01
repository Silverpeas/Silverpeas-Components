package com.silverpeas.rssAgregator.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.rssAgregator.control.RssAgregatorSessionController;
import com.silverpeas.rssAgregator.model.SPChannel;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class RssAgregatorRequestRouter extends ComponentRequestRouter
{
	public String getSessionControlBeanName()
	{
		return "rssAgregator";
	}

	public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
	{
		return new RssAgregatorSessionController(mainSessionCtrl, componentContext);
	}

	public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
	{
		String destination = "";
		RssAgregatorSessionController rssAgregatorSC = (RssAgregatorSessionController) componentSC;
		SilverTrace.info("rssAgregator", "rssAgregatorRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "User=" + componentSC.getUserId() + " Function=" + function);

		String role = getRole(rssAgregatorSC.getUserRoles());

		try
		{
			if (function.startsWith("Main") || function.equals("portlet"))
			{
				List channels = rssAgregatorSC.getAvailableChannels();

				if (function.startsWith("Main"))
					request.setAttribute("Role", role);
				else
					request.setAttribute("Role", "user");

				if (channels.size() != 0)
				{
					request.setAttribute("Channels", channels);
					destination = "/rssAgregator/jsp/welcome.jsp";
				}
				else
				{
					destination = "/rssAgregator/jsp/whatIsRss.jsp";
				}
			}
			else if (function.equals("LoadChannels"))
			{
				rssAgregatorSC.getChannelsContent();

				destination = getDestination("Main", rssAgregatorSC, request);
			}
			else if (function.equals("ToCreateChannel"))
			{
				destination = "/rssAgregator/jsp/newChannel.jsp";
			}
			else if (function.equals("CreateChannel"))
			{
				SPChannel channel = buildSPChannelFromRequest(request);

				rssAgregatorSC.addChannel(channel);

				destination = "/rssAgregator/jsp/reload.jsp";
			}
			else if (function.equals("ToUpdateChannel"))
			{
				String id = request.getParameter("Id");
				SPChannel channel = rssAgregatorSC.getChannel(id);

				request.setAttribute("Channel", channel);

				destination = "/rssAgregator/jsp/updateChannel.jsp";
			}
			else if (function.equals("UpdateChannel"))
			{
				SPChannel channel = buildSPChannelFromRequest(request);

				rssAgregatorSC.updateChannel(channel);

				destination = "/rssAgregator/jsp/reload.jsp";
			}
			else if (function.equals("DeleteChannel"))
			{
				String id 	= request.getParameter("Id");

				rssAgregatorSC.deleteChannel(id);

				destination = getDestination("Main", rssAgregatorSC, request);
			}
		}
		catch (Exception e)
		{
			request.setAttribute("javax.servlet.jsp.jspException", e);
			destination = "/admin/jsp/errorpageMain.jsp";
		}

		SilverTrace.info("rssAgregator", "rssAgregatorRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
		return destination;
	}

	/**
	 * This method return the highest profiles, for a couple of profiles given
	 *
	 * @param		profiles		User's profiles for this instance of kmelia
	 * @return		profile which gives the higher access
	 */
	public String getRole(String[] profiles) {
		String role = "user";
		for (int i=0; i < profiles.length; i++) {
			// if admin, return it, we won't find a better profile
			if (profiles[i].equals("admin"))
				return profiles[i];
		}
		return role;
	}

	private SPChannel buildSPChannelFromRequest(HttpServletRequest request)
	{
		String id 			= request.getParameter("Id");
		String url 			= request.getParameter("Url");
		String refreshRate 	= request.getParameter("RefreshRate");
		String nbItems 		= request.getParameter("NbItems");
		String displayImage = request.getParameter("DisplayImage");

		SilverTrace.info("rssAgregator", "rssAgregatorRequestRouter.buildSPChannelFromRequest", "root.MSG_GEN_PARAM_VALUE", "Id = "+id+", url = "+url+", refreshRate = "+refreshRate+", nbItems = "+nbItems+", displayImage = "+displayImage);

		SPChannel channel = null;

		if (id != null && !id.equals(""))
			channel = new SPChannel(id, url);
		else
			channel = new SPChannel(url);

		if (nbItems != null && !"".equals(nbItems))
			channel.setNbDisplayedItems(new Integer(nbItems).intValue());
		channel.setRefreshRate(new Integer(refreshRate).intValue());
		if (displayImage != null)
			channel.setDisplayImage(1);

		return channel;
	}
}