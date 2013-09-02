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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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