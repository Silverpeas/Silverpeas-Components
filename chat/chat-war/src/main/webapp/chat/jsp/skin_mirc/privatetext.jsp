<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<html>
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.Chat.Filter.*,java.util.*,java.text.SimpleDateFormat" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%@ include file="../checkChat.jsp" %>
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String chatroomName = "", chatroomSubject = "", chatroomDate = "", to = "all";
	String jspDisplay = UserProcessor.execute(request,response,session,application);
	if (jspDisplay != null)
	{
		response.sendRedirect(response.encodeRedirectURL(jspDisplay));
	}
	else
	{
		ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
		ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
		Chatroom chatroom = null;
		if (cUser != null)
		{
			int cID = cUser.getParams().getChatroom();
			try
			{
				chatroom = ChatroomManager.getChatroom(cID);
				chatroomName = chatroom.getParams().getName();
				chatroomSubject = chatroom.getParams().getSubject();
				chatroomDate = (new SimpleDateFormat("yyyyy/MM/dd HH:mm")).format(chatroom.getDate());
				to = request.getParameter("to");
				if (to==null) to = "ALL";
       			Vector filters = chatroom.getParams().getFilters();
       			jChatBox.Chat.Filter.Filter filter = null;
       			/** Filter username for non-SYSTEM users */
               	if (!(to).equals(XMLConfig.SYSTEMLOGIN))
				{
					for (int f=0;f<filters.size();f++)
					{
						filter = (jChatBox.Chat.Filter.Filter) filters.elementAt(f);
               			to = filter.process(to);
               		}
                }

			} catch (ChatException ce)
			  {
			  	/** Chatroom not found */
				response.sendRedirect(response.encodeRedirectURL(Conf.JSPUSERLOGIN));
			  }
		}
	}
%>
<%
String path = "";
	path += chatroomName + " > " + resource.getString("chat.privateMessageTo") + " " + to;
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(path);

Board board = gef.getBoard();
%>

<head>
<title>Private Message</title>
<script language="JavaScript"><!--
function ready()
{
	document.chat.msg.focus();
}
function SendMessage()
{
	document.chat.submit();
	document.chat.msg.value = "";
	document.chat.reset();
	document.chat.msg.focus();
}
function chat()
{
	document.chat.todo.value="chat";
	SendMessage();
}
//--></script>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onLoad="ready()">
<form method="get" action="<%= response.encodeURL("content.jsp") %>" name="chat" target="content" OnSubmit='SendMessage();return false;'>
<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
%>
<center>
<%
	out.println(board.printBefore());
%>
  <table width="98%" border="0" cellspacing="1" cellpadding="0">
    <tr>
      <td>
        <table width="98%" border="0" cellspacing="1" cellpadding="1" align="center">
          <tr>
            <td nowrap class="txtlibform"><%=resource.getString("chat.Message")%>&nbsp;:&nbsp;
              <input type="text" name="msg" size="48" maxlength="120">
              <input type="hidden" name="to" value="<%= to %>">
            </td>
						<td align="left">
<%
						ButtonPane bouton = gef.getButtonPane();
						bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:chat()", false));
						out.println(bouton.print());
%>
						</td><input type="hidden" name="todo" value="chat">
						<td width="100%">&nbsp;</td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
<%
	out.println(board.printAfter());
%>
<br><br>
<%
	ButtonPane boutonClose = gef.getButtonPane();
	boutonClose.addButton((Button) gef.getFormButton(resource.getString("GML.close"), "javascript:window.close();", false));
	out.println(boutonClose.print());
%>
<input type="hidden" name="todo" value="chat">
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</form>
</body>
</html>