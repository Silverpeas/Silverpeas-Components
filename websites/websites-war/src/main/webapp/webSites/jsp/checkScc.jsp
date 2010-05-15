<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.webSites.control.WebSiteSessionController"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
    WebSiteSessionController 	scc 		= (WebSiteSessionController) request.getAttribute("webSites");
    GraphicElementFactory 		gef 		= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
    ResourcesWrapper 			resources 	= (ResourcesWrapper)request.getAttribute("resources");

	//CBO : ADD
	String[] 						browseContext 	= (String[]) request.getAttribute("browseContext");

	if (scc == null)
	{
	    // No webSite session controller in the request -> security exception
	    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}

	//CBO : ADD
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    
	String spaceLabel = browseContext[0];
	String componentLabel = browseContext[1];
	String spaceId = browseContext[2];
	String componentId = browseContext[3];
	
	boolean bookmarkMode = componentId.startsWith("bookmark");
%>