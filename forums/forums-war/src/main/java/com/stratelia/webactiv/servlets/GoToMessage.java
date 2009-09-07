package com.stratelia.webactiv.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.forums.forumsException.ForumsRuntimeException;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBM;
import com.stratelia.webactiv.forums.forumsManager.ejb.ForumsBMHome;
import com.stratelia.webactiv.forums.url.ActionUrl;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class GoToMessage extends GoTo
{
	
	public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
		throws Exception
	{
		int forumId	= Integer.parseInt(req.getParameter("ForumId"));
		String componentName = getForumsBM().getForumInstanceId(forumId);
		String messageUrl = ActionUrl.getUrl(
			"viewMessage", "viewForum", 1, Integer.parseInt(objectId), forumId);
		String gotoURL = URLManager.getURL(null, componentName) + messageUrl;
		return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
	}

	private ForumsBM getForumsBM()
	{
		ForumsBM forumsBM = null;
		try
		{
			ForumsBMHome forumsBMHome = (ForumsBMHome) EJBUtilitaire.getEJBObjectRef(
				JNDINames.FORUMSBM_EJBHOME, ForumsBMHome.class);
			forumsBM = forumsBMHome.create();
		}
		catch (Exception e)
		{
			throw new ForumsRuntimeException("RssServlet.getForumsBM()",
				SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
		}
		return forumsBM;
	}
	
}