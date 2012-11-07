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
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%
String login 	= (String) request.getAttribute("Login");
String password = (String) request.getAttribute("Password");
String domain 	= (String) request.getAttribute("Domain");
String url 		= (String) request.getAttribute("URL");
%>

<html>
<head>
<script language="JavaScript">
function getHTTPObject() {
	if (typeof XMLHttpRequest != 'undefined') 
	{ 
		return new XMLHttpRequest(); 
	} 
	try 
	{ 
		return new ActiveXObject("Msxml2.XMLHTTP"); 
	} 
	catch (e) 
	{ 
		try 
		{ 
			return new ActiveXObject("Microsoft.XMLHTTP"); 
		} 
		catch (e) {} 
	} 
	return false; 
}

window.onload = function() 
{ 
	var http = getHTTPObject(); 
	if (http) 
	{
		http.open("get", "<%=EncodeHelper.javaStringToJsString(url)%>", false, "<%=domain%>\\<%=login%>", "<%=EncodeHelper.javaStringToJsString(password)%>");
		http.send(""); 
		if (http.status == 200) 
		{
			document.location = "<%=EncodeHelper.javaStringToJsString(url)%>"; 
		} 
		else 
		{ 
			alert("Login et/ou Mot de passe incorrect !");
		} 
		return false;
	}
}
</script>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<H1>Connexion en cours, merci de patienter...</H1>
</body>
</html>