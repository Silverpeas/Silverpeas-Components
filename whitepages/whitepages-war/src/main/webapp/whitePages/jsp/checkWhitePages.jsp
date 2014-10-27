<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.Window"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.ResourcesWrapper"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.Button"%>

<%@ page import="org.silverpeas.util.*"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.silverpeas.whitePages.control.*"%>
<%@ page import="com.stratelia.silverpeas.containerManager.*"%>
<%@ page import="org.silverpeas.util.GeneralPropertiesManager" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%

MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
//OrganizationController organizationCtrl = m_MainSessionCtrl.getOrganisationController();
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