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