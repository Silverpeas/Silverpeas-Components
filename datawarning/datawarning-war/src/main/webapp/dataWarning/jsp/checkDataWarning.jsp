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
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.tabs.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.silverpeas.dataWarning.control.DataWarningSessionController"%>
<%@ page import="com.silverpeas.dataWarning.model.*"%>
<%@ page import="com.silverpeas.dataWarning.*"%>
<%@ page import="org.silverpeas.util.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory gef;
	Window window;
	TabbedPane tabbedPane;
	Frame frame;
	ButtonPane buttonPane;
	OperationPane operationPane;
	
	DataWarningSessionController dataWarningSC;
    MultiSilverpeasBundle resource;

	gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  	
  	String flag = (String)request.getAttribute("flag");
  	if (flag == null)
  		flag = "user";
  	

	dataWarningSC = (DataWarningSessionController) request.getAttribute("DataWarningSC");
	
	if (dataWarningSC == null) {
		// No DataWarning session controller in the request -> security exception
		String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
		getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
		return;
	}

  	//objet window
  	window = gef.getWindow();
    //browse bar
	String space = dataWarningSC.getSpaceLabel();
	String component = dataWarningSC.getComponentLabel();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(space);
	browseBar.setComponentName(component, "Main.jsp");
	//frame
	frame = gef.getFrame();
	//buttons
	buttonPane = gef.getButtonPane();
	//operation pane
	operationPane = window.getOperationPane();

	String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
    resource = (MultiSilverpeasBundle)request.getAttribute("resources");
%>
