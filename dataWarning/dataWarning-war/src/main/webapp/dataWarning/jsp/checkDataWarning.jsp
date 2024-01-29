<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	response.setHeader("Cache-Control","no-store"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil" %>
<%@ page import="org.silverpeas.components.datawarning.DataWarningDBDriver" %>
<%@ page import="org.silverpeas.components.datawarning.model.DataWarning" %>
<%@ page import="org.silverpeas.components.datawarning.model.DataWarningScheduler" %>
<%@ page import="org.silverpeas.components.datawarning.model.DataWarningResult" %>
<%@ page import="org.silverpeas.components.datawarning.model.DataWarningQueryResult" %>
<%@ page import="org.silverpeas.components.datawarning.model.DataWarningQuery" %>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.components.datawarning.control.DataWarningSessionController"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>

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
