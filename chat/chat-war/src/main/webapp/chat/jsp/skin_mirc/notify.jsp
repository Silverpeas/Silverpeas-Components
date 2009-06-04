<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,java.util.*,java.text.SimpleDateFormat,com.stratelia.silverpeas.silvertrace.*" %>
<%
		Integer cID ;
		ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
		if ( cUser != null )
		{ 
			session.removeValue(XMLConfig.USERSESSIONID);
		}
%>

<html>
<head>
	<title></title>
<script>
function refreshList(){
<%
	if ( request.getParameter("action").equals("open") )
	{%>
		top.window.opener.location='login.jsp';
		top.window.close();
	<%}
	else if ( request.getParameter("action").equals("goAdmin") )
	{%>
			top.window.close();
	<%}
%>
}
</script>
</head>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="refreshList()">
</body>
</html>