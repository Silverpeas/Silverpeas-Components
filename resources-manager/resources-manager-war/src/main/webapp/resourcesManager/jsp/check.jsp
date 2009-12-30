<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
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
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ReservationDetail"%>
<%@ page import="com.silverpeas.form.DataRecord"%>
<%@ page import="com.silverpeas.form.Form"%>
<%@ page import="com.silverpeas.form.RecordSet"%>
<%@ page import="com.silverpeas.form.PagesContext"%>

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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.*"%>
<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>
<%@ page import="com.silverpeas.resourcesmanager.control.ResourcesManagerSessionController"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.List"%>


<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.lang.Integer"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ page import="com.stratelia.webactiv.util.exception.*"%>

<%@ page import="com.silverpeas.util.*"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
ResourcesManagerSessionController  resourcesManagerSC = (ResourcesManagerSessionController) request.getAttribute("rsc");
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
TabbedPane tabbedPane = gef.getTabbedPane();
String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String componentId = browseContext[3];
%>