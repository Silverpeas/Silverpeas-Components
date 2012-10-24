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
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String jspDisplay = UserProcessor.execute(request,response,session,application);
	if (jspDisplay != null)
	{
		response.sendRedirect(response.encodeRedirectURL(jspDisplay));
	}
	else
	{
		ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
	}
%>
<head>
<title>jChatBox</title>
</head>
  <%
	ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
	int dMode = -1;

	if (cUser != null)
	{
		int cID = cUser.getParams().getChatroom();
		try
		{
			if (ChatroomManager != null) dMode = ChatroomManager.getChatroom(cID).getParams().getDisplayMode();
		} catch (ChatException ce)
		  {
			/** Chatroom not found */
			response.sendRedirect(response.encodeRedirectURL(Conf.JSPUSERLOGIN));
		  }
	}
  	if (dMode == Conf.BUFFEREDFRAMED)
  	{%>
<frameset rows="0,*,68" frameborder="NO" border="0" framespacing="0">
  	  <frame name="content" src="<%= response.encodeURL("content.jsp?todo=refresh") %>" scrolling="NO" marginwidth="0" marginheight="0" frameborder="NO" noresize>
  	  <frame name="dbcontent" src="<%= response.encodeURL("content.jsp") %>" scrolling="AUTO" marginwidth="0" marginheight="0" frameborder="NO" noresize>
	  <frame name="text" scrolling="NO" noresize src="<%= response.encodeURL("text.jsp") %>" marginwidth="0" marginheight="0" frameborder="NO" >
</frameset>
  	<%}
  	else
  	{%>
<frameset rows="*,68" frameborder="NO" border="0" framespacing="0">
	  <frame name="content" src="<%= response.encodeURL("content.jsp?todo=refresh") %>" scrolling="AUTO" marginwidth="0" marginheight="0" frameborder="NO" noresize>
	  <frame name="text" scrolling="NO" noresize src="<%= response.encodeURL("text.jsp") %>" marginwidth="0" marginheight="0" frameborder="NO" >
</frameset>
  	<%}
  %>
<noframes>
<body bgcolor="#FFFFFF">
Frames support needed to run jChatBox !
</body>
</noframes>
</html>
