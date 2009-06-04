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