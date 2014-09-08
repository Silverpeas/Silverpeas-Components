<%@ page import="com.stratelia.webactiv.yellowpages.control.DisplayContactsHelper" %>
<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>

<%
Collection contacts = (Collection) request.getAttribute("Contacts");
%>

<html>
<head>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</head>
<BODY>
<view:browseBar path='<%=yellowpagesScc.getString("GML.print")%>'/>
<view:operationPane>
<view:operation altText='<%=yellowpagesScc.getString("GML.print")%>' action='javaScript:window.print();'/>
</view:operationPane>
<view:window popup="true">
<view:frame>
<view:board>
<%
DisplayContactsHelper.displayContactsUser(yellowpagesScc, contacts, null, componentLabel, gef, request, session, resources, out);
%>
</view:board>
</view:frame>
</view:window>
</body>
</html>