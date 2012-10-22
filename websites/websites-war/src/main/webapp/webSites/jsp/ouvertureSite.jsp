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
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.rmi.PortableRemoteObject"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%> 
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>

<%@ include file="checkScc.jsp" %>

<%

String url = (String) request.getParameter("URL");
String popup = (String) request.getParameter("Popup");

%>

 <!-- ouvertureSite -->          
 
<HTML>
<HEAD>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>

<%
out.println(gef.getLookStyleSheet());
%>
<!--CBO : UPDATE-->
<!--<script type="text/javascript" src="<%/*iconsPath*/%>/util/javaScript/animation.js"></script>-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">
function openSite(URL, popup) {
	winName = "Site";
	larg="670" ;
	haut="500" ;
	windowParams="width="+ larg + ",height=" + haut	+ " , toolbar=yes, scrollbars=yes, resizable, alwaysRaised";
	if (popup == "0") {
		document.location.replace(URL);
	} else {
		site=window.open(URL,winName,windowParams);
		document.location.replace("Main");
	}
}
</Script>

</HEAD>

<BODY bgcolor="white" topmargin="5" leftmargin="5" onLoad="openSite('<%=url%>')">
</BODY>     
</HTML>
