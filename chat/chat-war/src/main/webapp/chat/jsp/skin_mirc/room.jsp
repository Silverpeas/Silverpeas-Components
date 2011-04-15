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

<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.Chat.Filter.*,java.util.*,java.net.*" %>
<%@ include file="../checkChat.jsp" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%

	String banned = (String) session.getValue("chat_banned");

	if ( ( banned != null ) && banned.equals("true") )
	{
		session.removeValue("chat_banned");
%>
<html>
<head>
<script language="Javascript">
function auto_quit_chatroom()
{
		alert('<%=resource.getString("chat.bannedMessage")%>');
		location.href='quitChatroom.jsp?action=open';
}
</script>
</head>
<body onLoad="auto_quit_chatroom()">
</body>
</html>
<%
	}
	else
	{
%>
<html>
<frameset rows="38,*" frameborder="NO" border="0" framespacing="0">
	<frame src="head.jsp" name="header" id="header" frameborder="0" scrolling="No" noresize marginwidth="0" marginheight="0">
	<frameset cols="*,70" frameborder="NO" border="0" framespacing="0">
		<frame src="roomFrameset.jsp" name="room" id="room" frameborder="0" scrolling="Auto" marginwidth="0" marginheight="0">
		<frame src="actions.jsp" name="actions" id="actions" scrolling="No" marginwidth="0" marginheight="0">
	</frameset>
</frameset>
<noframes>
<body bgcolor="#FFFFFF">
Frames support needed to run jChatBox !
</body>
</noframes>
</html>
<% } %>