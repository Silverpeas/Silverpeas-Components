<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkScc.jsp" %>

<html>
<title><%=resources.getString("GML.popupTitle")%></title>
<head>
<view:looknfeel/>
</head>
<body>
<view:browseBar path='<%=resources.getString("NotationSites")%>'/>
<view:window popup="true">
<view:frame>
<%
    Collection icones = scc.getAllIcons();
    Iterator i = icones.iterator();
    i.next(); //saute la premiere icone reference (site important)
    while (i.hasNext()) {
        IconDetail ic = (IconDetail) i.next();
        out.println("<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><tr><td><TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=\"100%\" CLASS=intfdcolor4><tr><td><p align=justify><img src=\""+ic.getAddress()+"\" align=absmiddle>&nbsp;&nbsp;<font face=verdana size=2><b>"+resources.getString(ic.getName())+" :</b></font><br><font face=verdana size=1>");
        out.println(resources.getString(ic.getDescription())+"</p></font></td></tr></table></td></tr></table><br>");
    }
%>
</view:frame>
</view:window>
</body>
</html>