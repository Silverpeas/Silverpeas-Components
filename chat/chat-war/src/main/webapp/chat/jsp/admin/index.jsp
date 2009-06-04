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
