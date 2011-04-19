<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<html>
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.Chat.Filter.*,java.util.*" %>
<%@ include file="../checkChat.jsp" %>
<jsp:useBean id="SystemProcessor" class="jChatBox.Service.SystemProcessor" scope="session" />
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String jspDisplay = SystemProcessor.execute(request,session,application);
	if (jspDisplay != null)
	{
		response.sendRedirect(jspDisplay);
	}
	else
	{
		ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
	}

browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(resource.getString("chat.openChatroom"));

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("chat.chatRoomList"),"Main",false);
tabbedPane.addTab(resource.getString("chat.administration"),"#",true);
/*boolean isPdcUsed = "yes".equals( (String) request.getAttribute("isPdcUsed") );
if (isPdcUsed)
{
	tabbedPane.addTab(resource.getString("PdcClassification"),"pdcPositions.jsp",false);
}*/

Board board = gef.getBoard();
%>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript"><!--
function validate()
{
	if (document.chatroom.name.value.length != 0)
	{
		if (document.chatroom.subject.value.length != 0)
		{
			document.chatroom.todo.value = "openchatroom";
			document.chatroom.submit();
		}
		else
		{
			alert("<%=resource.getString("chat.subjectMissing")%>");
		}
	}
	else
	{
		alert("<%=resource.getString("chat.nameMissing")%>");
	}
}
function cancel()
{
	location.href="menu.jsp";
}
function manage(opt)
{
	if (opt != "")
	{
		location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id="+opt;
	}
}
function logout()
{
	location.href="index.jsp?todo=logout&rand=<%= System.currentTimeMillis() %>";
}
//--></script>
<title>jChatBox Manager</title>
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" link="#9999CC" alink="#9999CC" vlink="#9999CC">
<form method="post" action="menu.jsp" name="chatroom">
<%
if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMNAMEMISSING)) out.print(resource.getString("chat.nameMissing"));
	else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMSUBJECTMISSING)) out.print(resource.getString("chat.subjectMissing"));
%>
<%
	out.println(window.printBefore());
	if ( request.getAttribute( "chat_isAdmin" ).equals("yes") )
	{
	//if admin
		out.println(tabbedPane.print());
	}
	out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.chatroomName")%> : <% if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMNAMEMISSING)) out.print("*"); %> 
		</td>
		<td align=left valign="baseline">
			<input type="text" name="name" size="50" maxlength="100">&nbsp;<img border="0" src="images/mandatoryField.gif" width="5" height="5">					
		</td>
	</tr>
	<tr>			
		<td valign="baseline" align=left  class="txtlibform">
			<%=resource.getString("chat.subject")%> : <% if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMSUBJECTMISSING)) out.print("<font color=#ff0000>*</font>"); %>
		</td>
		<td align=left valign="baseline">
			<input type="text" name="subject" size="50" maxlength="500">&nbsp;<img border="0" src="images/mandatoryField.gif" width="5" height="5">
		</td>
	</tr>
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.maxUser")%> : 
		</td>
		<td align=left valign="baseline">
		 <select name="maxusers">
				<option value="5">5</option>
				<option value="10">10</option>
				<option value="15">15</option>
				<option value="20" selected>20</option>
				<option value="25">25</option>
				<option value="30">30</option>
				<option value="35">35</option>
				<option value="40">40</option>
				<option value="45">45</option>
				<option value="50">50</option>
				<option value="55">55</option>
				<option value="60">60</option>
				<option value="65">65</option>
				<option value="70">70</option>
				<option value="75">75</option>
				<option value="80">80</option>
				<option value="85">85</option>
				<option value="90">90</option>
				<option value="95">95</option>
				<option value="100">100</option>
				<option value="150">150</option>
				<option value="200">200</option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.history")%> : 
		</td>
		<td align=left valign="baseline">
			<select name="history">
				<option value="20">20</option>
				<option value="30">30</option>
				<option value="35">35</option>
				<option value="40" selected>40</option>
				<option value="45">45</option>
				<option value="50">50</option>
				<option value="55">55</option>
				<option value="60">60</option>
				<option value="70">70</option>
				<option value="80">80</option>
				<option value="90">90</option>
				<option value="100">100</option>
				<option value="120">120</option>
				<option value="150">150</option>
			</select>
		</td>
	</tr>				
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.refreshTime")%> : 
		</td>
		<td align=left valign="baseline">
			<select name="refreshlimit">
				<option value="3">3 <%=resource.getString("chat.seconds")%></option>
				<option value="5">5 <%=resource.getString("chat.seconds")%></option>
				<option value="8">8 <%=resource.getString("chat.seconds")%></option>
				<option value="10">10 <%=resource.getString("chat.seconds")%></option>
				<option value="12">12 <%=resource.getString("chat.seconds")%></option>
				<option value="15" selected>15 <%=resource.getString("chat.seconds")%></option>
				<option value="18">18 <%=resource.getString("chat.seconds")%></option>
				<option value="20">20 <%=resource.getString("chat.seconds")%></option>
				<option value="25">25 <%=resource.getString("chat.seconds")%></option>
				<option value="30">30 <%=resource.getString("chat.seconds")%></option>
				<option value="40">40 <%=resource.getString("chat.seconds")%></option>
				<option value="50">50 <%=resource.getString("chat.seconds")%></option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.refreshMode")%> : 
		</td>
		<td align=left valign="baseline">
			<select name="refreshmodel">
				<option value="1"><font face="Verdana, Arial, Helvetica, sans-serif" size="-1"><%=resource.getString("chat.time-constant")%></font></option>
				<option value="2" selected><font face="Verdana, Arial, Helvetica, sans-serif" size="-1"><%=resource.getString("chat.action-tracker")%></font></option>
				<option value="3"><font face="Verdana, Arial, Helvetica, sans-serif" size="-1"><%=resource.getString("chat.room-load")%></font></option>
			</select>
		</td>
	</tr>
	<tr>
		<td class="txtlibform" valign="top">
			<%=resource.getString("chat.privateMessages")%> : 
		</td>
		<td align=left valign="baseline">
			<table width="100%" border="0" cellspacing="4" cellpadding="0">
				<tr>
					<td align="left">
						<input type="radio" name="private" value="yes" checked>
					</td>
					<td width="100%"><%=resource.getString("GML.yes")%></td>
				</tr>
				<tr>
					<td align="left">
						<input type="radio" name="private" value="no">
					</td>
					<td><%=resource.getString("GML.no")%></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr> 
		<td colspan="2">(<img border="0" src="images/mandatoryField.gif" width="5" height="5"> 
				: <%=resource.getString("GML.requiredField")%>)
		</td>
	</tr>
</table>
<%
      out.println(board.printAfter());
%>
<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:validate()", false));
			buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:cancel()", false));
		  out.println(buttonPane.print());
%>
<br>
<input type="hidden" name="todo" value="openchatroom">
</form>
</center>
<%out.println(frame.printAfter());%>
<%out.println(window.printAfter());%>
</body>
</body>
</html>
