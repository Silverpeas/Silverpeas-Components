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
	operationPane.addOperation(resource.getIcon("chat.chatroomRefresh"),resource.getString("chat.refresh"),"javascript:refresh()");
	if ( request.getAttribute( "chat_isPublisher" ).equals("yes") || request.getAttribute( "chat_isAdmin" ).equals("yes") )
	{
		operationPane.addOperation(resource.getIcon("chat.chatroomNotify"),resource.getString("chat.notify"),"javascript:notify()");
	}
	operationPane.addOperation(resource.getIcon("chat.chatroomQuit"),resource.getString("chat.logout"),"javascript:fct_redirect()");
%>
<html>
<head>
	<title></title>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<script language='javascript1.2'>
function fct_redirect()
{
	location.href='quitChatroom.jsp?action=open';	// close the current windows + refresh list of chatroom
}

function refresh(){
parent.room.content.location='<%= response.encodeURL("content.jsp?todo=refresh") %>';
}

function notify()
{
	chemin = "ToUserPanel";
	largeur = "740";
	hauteur = "700";
	SP_openWindow(chemin,"Notification",largeur,hauteur,"resizable=1,scrollbars=1");

}

</script>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body  bgcolor="#ffffff" leftmargin="7" topmargin="0" marginwidth="7" marginheight="0">
<form name="formRefresh" id="formRefresh">
</form>
<%
out.println(operationPane.print());
%>
</body>
</html>
