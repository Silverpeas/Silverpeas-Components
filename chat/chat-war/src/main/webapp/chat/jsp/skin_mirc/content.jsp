<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<!-- Chunk -->
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.Chat.Filter.*,java.util.*,java.net.*" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String chatroomName = "", buffering = null, todo = null;
	int chatroomMaxUsers = -1, chatroomTotalUsers = -1, refreshValue = 20, dMode = -1;
	Chatroom chatroom = null;
	ChatroomUser cUser = null;

	String kickoff = (String) session.getAttribute("chat_kickoff");

	String banned = (String) session.getAttribute("chat_banned");

	if ( "true".equals(kickoff) )
	{
		session.removeAttribute("chat_kickoff");

	}
	else if ( ( banned != null ) && banned.equals("true") )
	{
		session.removeAttribute("chat_banned");
	}
	else
	{
    request.setCharacterEncoding("UTF-8");
		String jspDisplay = UserProcessor.execute(request,response,session,application);
		if (jspDisplay != null)
		{
			if ( "quit".equals(request.getParameter("todo")) )
			{
				response.sendRedirect(response.encodeRedirectURL(((String)request.getAttribute("myComponentURL"))+"Main"));
			}
			else {
				response.sendRedirect(response.encodeRedirectURL(jspDisplay));
      }
			return;
		}
		else
		{
			ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
			cUser = (ChatroomUser) session.getAttribute(XMLConfig.USERSESSIONID);
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
		}

	}

%>
<%
/**----------------------------*/
/** No action means blank page */
/**----------------------------*/
if (todo == null)
{%>
<html>
<head>
<script language="Javascript">

function auto_quit_chatroom()
{
<%
	if ( (kickoff != null ) && kickoff.equals("true") )
	{
		out.println("alert('Le gestionnaire vous a exclu du salon !');");
		out.println("location.href='quitChatroom.jsp?action=open';");
	}
	else if ( ( banned != null ) && banned.equals("true") )
	{
		out.println("alert('Vous avez été banni de ce salon');");
		out.println("location.href='quitChatroom.jsp?action=open';");
	}

%>
}
</script>
</head>
<body onLoad="auto_quit_chatroom()">
</body>
</html>
<%}
/**--------------------------------------------------------------------------------------*/
/** BUFFEREDFRAMED mode. Content of chatroom has been buffered in a JavaScript variable. */
/**--------------------------------------------------------------------------------------*/
else if ( (dMode == Conf.BUFFEREDFRAMED) && (buffering == null) )
{%>
<html>
<meta http-equiv=Refresh content="<%= refreshValue %>;URL=<%= response.encodeURL("content.jsp?todo=refresh") %>">
<head>
<title><%= chatroomName %></title>
<script language="JavaScript"><!--
	var thechat = '<%= URLEncoder.encode((String)session.getAttribute("bufferedChat"), "ISO-8859-1").replace('+',' ') %>';
	function execute()
	{
		if (typeof(self.parent.dbcontent) != "undefined")
		{
			self.parent.dbcontent.document.open("text/html");
			self.parent.dbcontent.document.writeln(decodeURIComponent(thechat));
			self.parent.dbcontent.document.write();
			self.parent.dbcontent.document.close();
		}
	}
//--></script>
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="execute()">
</body>
</html>
<%}
/**------------------&---*/
/** Content of chatroom. */
/**----------------------*/
else
{%><%@ include file="chatroom_display.jsp.inc" %><%}%>