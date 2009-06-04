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
