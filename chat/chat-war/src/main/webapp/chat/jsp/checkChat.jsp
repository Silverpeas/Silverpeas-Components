<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ page import="com.stratelia.silverpeas.chat.control.ChatSessionController "%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>
<%

	ChatSessionController chatScc = (ChatSessionController) request.getAttribute("chat");

	ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
	
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel = browseContext[0];
	String componentLabel = browseContext[1];
	//String spaceId = browseContext[2];
	String componentId = browseContext[3];
	String chatUrl = browseContext[4];

	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	OperationPane operationPane = window.getOperationPane();
	Frame frame = gef.getFrame();
%>
