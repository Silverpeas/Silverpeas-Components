<html>
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,java.util.*,java.text.SimpleDateFormat" %>
<jsp:useBean id="SystemProcessor" class="jChatBox.Service.ModeratorProcessor" scope="application" />
<%@ include file="../checkChat.jsp" %>
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String jspDisplay = SystemProcessor.execute(request, session, application);
	if (jspDisplay != null)
	{
		response.sendRedirect(jspDisplay);
	}
	else
	{
		ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
	}
%>

<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(resource.getString("chat.administration"));

boolean isPdcUsed = ( "yes".equals( (String) request.getAttribute("isPdcUsed") ) );

if (isPdcUsed)
{
	operationPane.addOperation(resource.getIcon("chat.pdcUtilization"), resource.getString("PdcClassification"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
	operationPane.addLine();
}
operationPane.addOperation(resource.getIcon("chat.chatroomAdd"),resource.getString("chat.openChatroom"),"open.jsp");

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("chat.chatRoomList"),"Main",false);
tabbedPane.addTab(resource.getString("chat.administration"),"#",true);
%>

<head>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

function manage(opt)
{
	if (opt != "")
	{
		location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id="+opt;
	}
}

function logout()
{
	location.href="index.jsp?todo=quit&rand=<%= System.currentTimeMillis() %>";
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

</script>
<title>jChatBox Manager</title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form method="post" action="menu.jsp" name="chat">
<%

	out.println(window.printBefore());

	
	if ( request.getAttribute( "chat_isAdmin" ).equals("yes") )
	{
	//if admin
		out.println(tabbedPane.print());
	}
	
	out.println(frame.printBefore());
%>

<%
// Tableau
  ArrayPane arrayPane = gef.getArrayPane("List", "menu.jsp", request,session);
  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);
  arrayPane.addArrayColumn(resource.getString("chat.chatRoom"));
  arrayPane.addArrayColumn(resource.getString("chat.user"));
  arrayPane.addArrayColumn(resource.getString("chat.openingDate"));
  ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("chat.subject"));
  arrayColumn.setSortable(false);

  if (ChatroomManager != null)
  {
	Vector chatrooms = ChatroomManager.getChatrooms();
	Chatroom chatroom = null;
	Vector listChatRoom = (Vector)request.getAttribute( "chat_listChatRoom" );
    for (int i=0;i<chatrooms.size();i++)
    {
		chatroom = (Chatroom) chatrooms.elementAt(i);
		if ( listChatRoom.contains( String.valueOf( chatroom.getParams().getID() ) ) )
		{
			ArrayLine arrayLine = arrayPane.addArrayLine();
			IconPane iconPane1 = gef.getIconPane();
			Icon debIcon = iconPane1.addIcon();
			debIcon.setProperties(resource.getIcon("chat.chatIcon"),resource.getString("chat.chatRoom"),  "javascript:manage('"+new Integer(chatroom.getParams().getID()).toString()+"')");
			arrayLine.addArrayCellIconPane(iconPane1);	
			arrayLine.addArrayCellLink(chatroom.getParams().getName(), "javascript:manage('"+new Integer(chatroom.getParams().getID()).toString()+"')"); 
			arrayLine.addArrayCellText(chatroom.getTotalUsers()+"/"+chatroom.getParams().getMaxUsers());
			ArrayCellText cell = arrayLine.addArrayCellText(resource.getOutputDate(chatroom.getDate()));
			cell.setCompareOn(chatroom.getDate());
			arrayLine.addArrayCellText(chatroom.getParams().getSubject());
		}
    }
  }

	out.println(arrayPane.print());
%>
	</form>
<%out.println(frame.printAfter());%>
<%out.println(window.printAfter());%>
</body>
</html>