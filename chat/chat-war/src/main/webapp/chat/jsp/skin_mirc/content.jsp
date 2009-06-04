<!-- Chunk -->
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.Chat.Filter.*,java.util.*,java.net.*" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String chatroomName = "", buffering = null, todo = null;
	int chatroomMaxUsers = -1, chatroomTotalUsers = -1, refreshValue = 20, dMode = -1;
	Chatroom chatroom = null;
	ChatroomUser cUser = null;

	String kickoff = (String) session.getValue("chat_kickoff");

	String banned = (String) session.getValue("chat_banned");

	if ( ( kickoff != null ) && kickoff.equals("true") )
	{

		// process the kickoff action (remove session var kickoff)
		//System.out.println("KICKOFF!!!!!");
		session.removeValue("chat_kickoff");

	}
	else if ( ( banned != null ) && banned.equals("true") )
	{
		session.removeValue("chat_banned");
	}
	else
	{
		String jspDisplay = UserProcessor.execute(request,response,session,application);
		if (jspDisplay != null)
		{
			if ( (request.getParameter("todo") != null ) && ( request.getParameter("todo").equals("quit") ) )
			{
				response.sendRedirect(response.encodeRedirectURL(((String)request.getAttribute("myComponentURL"))+"Main"));
			}
			else
				response.sendRedirect(response.encodeRedirectURL(jspDisplay));
			return;
		}
		else
		{
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
	var thechat = '<%= URLEncoder.encode((String)session.getValue("bufferedChat")).replace('+',' ') %>';
	function execute()
	{
		if (typeof(self.parent.dbcontent) != "undefined")
		{
			self.parent.dbcontent.document.open("text/html");
			self.parent.dbcontent.document.writeln(unescape(thechat));
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
/**----------------------*/
/** Content of chatroom. */
/**----------------------*/
else
{%><%@ include file="chatroom_display.jsp.inc" %><%}%>