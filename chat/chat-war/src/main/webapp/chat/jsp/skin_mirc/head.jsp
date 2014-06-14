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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="../checkChat.jsp" %>
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.Chat.Filter.*,java.util.*,java.net.*" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String chatroomName = "", buffering = null, todo = null;
	int chatroomMaxUsers = -1, chatroomTotalUsers = -1, refreshValue = 20, dMode = -1;
	Chatroom chatroom = null;
	ChatroomUser cUser = null;
	String jspDisplay = UserProcessor.execute(request,response,session,application);

		ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
		cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
		if (cUser != null)
		{
			int cID = cUser.getParams().getChatroom();
			refreshValue = cUser.getParams().getRefresh();
			try
			{
				chatroom = ChatroomManager.getChatroom(cID);
				dMode = chatroom.getParams().getDisplayMode();
				chatroomName = chatroom.getParams().getName();
				chatroomMaxUsers = chatroom.getParams().getMaxUsers();
				chatroomTotalUsers = chatroom.getTotalUsers();
				buffering = request.getParameter("buffering");
				todo = request.getParameter("todo");
			} catch (ChatException ce)
			  {
			  	/** Chatroom not found */
				response.sendRedirect(response.encodeRedirectURL(Conf.JSPUSERLOGIN));
			  }
		}


browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);
browseBar.setPath(chatroomName);
%>
<html>
<head>
<title></title>
<view:looknfeel/>
<script>
function refreshList(){
	top.window.opener.location="login.jsp";
}
</script>
</head>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="javascript:refreshList()">
<table cellpadding=0 cellspacing=0 border=0 width="98%"><tr><td>
<%
out.println(browseBar.print());
%>
</td></tr></table>
<table cellpadding=0 cellspacing=0 border=0 width="100%"><tr>
<td width="100%"><img src="<%=resource.getIcon("chat.px")%>"></td>
<td  background="<%=resource.getIcon("chat.line")%>"><img src="<%=resource.getIcon("chat.px")%>" width=58 height=30></td>
</tr></table>
</body>
</html>