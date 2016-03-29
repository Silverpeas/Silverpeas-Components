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

<%@ page errorPage="/admin/jsp/errorpageMain.jsp"
%><%@ page import="java.sql.Types"
%><%@ page import="java.text.MessageFormat"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Arrays"
%><%@ page import="java.util.Collection"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.List"
%><%@ page import="org.silverpeas.core.contribution.content.form.DataRecord"
%><%@ page import="org.silverpeas.core.contribution.content.form.Form"
%><%@ page import="org.silverpeas.core.contribution.content.form.PagesContext"
%><%@ page import="org.silverpeas.core.contribution.content.form.Util"
%><%@ page import="com.silverpeas.mydb.MyDBConstants"
%><%@ page import="org.silverpeas.mydb.control.DriverManager"
%><%@ page import="com.silverpeas.mydb.control.MyDBSessionController"
%><%@ page import="com.silverpeas.mydb.control.TableManager"
%><%@ page import="com.silverpeas.mydb.data.datatype.DataType"
%><%@ page import="com.silverpeas.mydb.data.datatype.DataTypeList"
%><%@ page import="com.silverpeas.mydb.data.db.DbColumn"
%><%@ page import="com.silverpeas.mydb.data.db.DbFilter"
%><%@ page import="com.silverpeas.mydb.data.db.DbLine"
%><%@ page import="com.silverpeas.mydb.data.db.DbTable"
%><%@ page import="com.silverpeas.mydb.data.db.DbUtil"
%><%@ page import="com.silverpeas.mydb.data.index.IndexInfo"
%><%@ page import="com.silverpeas.mydb.data.index.IndexList"
%><%@ page import="com.silverpeas.mydb.data.key.ForeignKey"
%><%@ page import="com.silverpeas.mydb.data.key.ForeignKeyError"
%><%@ page import="com.silverpeas.mydb.data.key.ForeignKeys"
%><%@ page import="com.silverpeas.mydb.data.key.PrimaryKey"
%><%@ page import="com.silverpeas.mydb.data.key.UnicityKey"
%><%@ page import="com.silverpeas.mydb.data.key.UnicityKeys"
%><%@ page import="org.silverpeas.util.MultiSilverpeasBundle"
%><%@ page import="org.silverpeas.util.DBUtil"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"
%><%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"
%>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %><%
	response.setHeader("Cache-Control", "no-store"); // HTTP 1.1
	response.setHeader("Pragma", "no-cache");        // HTTP 1.0
	response.setDateHeader("Expires", -1);           // prevents caching at the proxy server

	MyDBSessionController myDBSC = (MyDBSessionController)request.getAttribute("MyDB");
	MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");
	String userRoleLevel = (String)request.getAttribute("userRoleLevel");
	String applicationURL = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
	
	GraphicElementFactory gef = (GraphicElementFactory)session.getAttribute("SessionGraphicElementFactory");
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	OperationPane operationPane = window.getOperationPane();
	TabbedPane tabbedPane = gef.getTabbedPane();
	Frame frame = gef.getFrame();
	
	browseBar.setDomainName(myDBSC.getSpaceLabel());
	browseBar.setComponentName(myDBSC.getComponentLabel(), MyDBConstants.ACTION_MAIN);
%>