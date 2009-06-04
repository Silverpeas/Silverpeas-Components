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