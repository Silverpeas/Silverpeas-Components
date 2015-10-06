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


<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="org.silverpeas.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttons.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.window.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.browseBars.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.quickinfo.control.*"%>
<%@ page import="com.stratelia.webactiv.publication.model.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.util.EncodeHelper"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
  GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

  QuickInfoSessionController quickinfo = (QuickInfoSessionController) request.getAttribute("quickinfo");
  
  MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");

  String language = resources.getLanguage();
  String[] browseContext = (String[]) request.getAttribute("browseContext");
  String spaceLabel = browseContext[0];
  String componentLabel = browseContext[1];
  String spaceId = browseContext[2];
  String componentId = browseContext[3];
  String quickinfoUrl = browseContext[4];

  if (quickinfo == null) {
      // No quickinfo session controller in the request -> security exception
      String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
      getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
      return;
  }

  String m_context = URLManager.getApplicationURL();
%>