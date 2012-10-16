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

<html>
<%@ page import="jChatBox.Util.*" %>
<%@ include file="../configureme.jsp" %>
<jsp:useBean id="SystemLogin" class="jChatBox.Service.SystemLogin" scope="request" />
<%!
	// Overides jspDestroy method to backup jChatBox.
	public void jspDestroy()
	{
		jChatBox.Util.Debug.log(0,"jChatBox sysDestroy() ...","");
		jChatBox.Chat.ChatroomManager chatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
		chatroomManager.destroy();
		jChatBox.Util.Debug.destroy();
	}
%>
<%
	String jspDisplay = SystemLogin.doLogin(request,session);
	if (jspDisplay != null)
	{
		%><%
		response.sendRedirect(((String)request.getAttribute("myComponentURL"))+jspDisplay);
	}
%>
<head>
<LINK REL=STYLESHEET TYPE="text/css" HREF="styles/admin.css">
<title></title>
<SCRIPT LANGUAGE="JavaScript">
                       <!-- Hide from non-JS Aware Browsers
                       function autosend()
                       {
 						 document.chat.name.value = "system";
						 document.chat.password.value = "password";
                         document.chat.submit();
                       }
                         -->
</SCRIPT>
</head>
<BODY BGCOLOR="#FFFFFF" LINK="#0000FF" VLINK="#800080" TEXT="#000000" onload = autosend() TOPMARGIN=0 LEFTMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0>

<form method="post" action="index.jsp" name="chat">
                          <input type="hidden" name="name" size="1">
                          <input type="hidden" name="password" size="1">
</body>
</html>
