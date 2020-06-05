<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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

<%@ page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCell" %>

<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>

<%@ page import="java.util.Collection"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="org.silverpeas.core.node.model.NodeDetail"%>
<%@ page import="org.silverpeas.core.contribution.publication.model.PublicationDetail"%>
<%@ page import="org.silverpeas.core.mylinks.model.LinkDetail"%>
<%@ page import="org.silverpeas.core.web.tools.agenda.view.CalendarHtmlView"%>
<%@ page import="org.silverpeas.core.web.tools.agenda.control.AgendaSessionController"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.calendar.Calendar"%>
<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.admin.user.model.SilverpeasRole"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");
String language = resource.getLanguage();

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();

String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String instanceId = browseContext[3];

String userId = m_MainSessionCtrl.getUserId(); 
%>
