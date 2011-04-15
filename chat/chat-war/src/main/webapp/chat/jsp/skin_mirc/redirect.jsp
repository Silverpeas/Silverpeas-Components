<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,java.util.*,java.text.SimpleDateFormat,com.stratelia.silverpeas.silvertrace.*" %>
<%@ include file="../checkChat.jsp" %>
<%
		int cID ;
		ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
		chatScc.setCurrentChatRoomId(request.getParameter("chatrooms"));
		if ( cUser != null )
		{ // the user is already using the chat
			//if ( cUser.getName().equals("system") )
			//{
				// the user was in admin mode, remove it before continu
				//session.removeValue(XMLConfig.USERSESSIONID);
			//}
			//else
			//{
				cID = cUser.getParams().getChatroom();


				if ( cID != Integer.parseInt(request.getParameter("chatrooms")) )
				{
					session.removeValue(XMLConfig.USERSESSIONID);
				}
				else
				{%>
					<jsp:forward page="room.jsp" />
				<%}
			//}

		}
%>

<html>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="document.chat.submit()">
	<form method="post" action="<%= response.encodeURL("login.jsp") %>" name="chat">
		<input type="hidden" value="<%=request.getParameter("chatrooms")%>" name="chatrooms">
		<input type="hidden" value="<%=request.getParameter("name")%>" name="name">
	</form>
</body>
</html>