<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.silverpeas.whitePages.control.*"%>
<%@ page import="com.stratelia.silverpeas.containerManager.*"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%

MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");
//OrganizationController organizationCtrl = m_MainSessionCtrl.getOrganizationController();
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

WhitePagesSessionController scc = (WhitePagesSessionController) request.getAttribute("whitePages");
if (scc == null)
{
    // No whitePages session controller in the request -> security exception
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}
	

ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");

String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];

ContainerContext containerContext = (ContainerContext) request.getAttribute("ContainerContext");
String   contentId = (String) request.getAttribute("contentId");
String returnURL = (String) request.getAttribute("ReturnURL");



String routerUrl = URLManager.getApplicationURL() + URLManager.getURL("whitePages", spaceId, componentId);

String language = scc.getLanguage();
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

String pdcUtilizationSrc = m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
TabbedPane tabbedPane = gef.getTabbedPane();
Frame frame = gef.getFrame();

if (containerContext == null) {
	if (returnURL != null && returnURL.length() > 0)
		browseBar.setComponentName(componentLabel, m_context+returnURL);
	else
		browseBar.setComponentName(componentLabel, "Main");
} else {
	browseBar.setComponentName(componentLabel, m_context+containerContext.getReturnURL()); 
}

%>