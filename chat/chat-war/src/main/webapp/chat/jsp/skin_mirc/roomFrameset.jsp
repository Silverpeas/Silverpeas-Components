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
