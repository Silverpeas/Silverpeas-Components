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
<jsp:useBean id="SystemProcessor" class="jChatBox.Service.SystemProcessor" scope="session" />
<%@ include file="../checkChat.jsp" %>
<%

//PHiL: Add kickoff support.
  String todo = request.getParameter("todo");
//PHiL
	
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	jChatBox.Chat.Monitor Monitor = null;
	String ID = "";
	Chatroom aChatroom = null;
	int id = -1;

//PHiL: Add kickoff support
	if (todo.equals("kickoff") || todo.equals("ban"))
	{
	  String username2 = request.getParameter("username");
	  String id2 = request.getParameter("id");
	  String reason = request.getParameter("reason");

	  if ( (id2 != null) && (!id2.equals("")) )
	  {
		int ID2 = Integer.parseInt(id2);
		if ( (username2 != null) && (!username2.equals("")) )
		{
		  // Gets Monitor.
		  Monitor monitor2 = Monitor.getInstance();
		  if (monitor2 == null)
		  {
			//setSysMessage(Conf.SYSTEMERROR);
			//return Conf.JSPMENU;
		  }
		  else
		  {
			Hashtable table2 = monitor2.getTable();
			Enumeration e = table2.keys();
			ChatroomUser cUser = null;
			HttpSession cSession = null;
			int cID = -1;
			String cUsername = null;
			//setSysMessage(Conf.NAMENOTFOUND);
			while (e.hasMoreElements())
			{
			  cUser = (ChatroomUser) e.nextElement();
			  cSession = (HttpSession) table2.get(cUser);
			  cID = cUser.getParams().getChatroom();
			  cUsername = cUser.getName();
			  if ( (ID2 == cID) && (cUsername.equals(username2)) )
			  {
				// Cannot kick off or ban SYSTEM user.
				if (cUser.getType() == User.SYSTEM)
				{
				  break;
				}
				if (todo.equals("kickoff"))
				{
					cSession.putValue("chat_kickoff","true");
					cSession.removeValue(XMLConfig.USERSESSIONID);
				}
				else
				{ // banned
					cSession.putValue("chat_banned","true");
					cSession.removeValue(XMLConfig.USERSESSIONID);
				}
				break;
			  }
			}
			//return null;
		  }
		}
		else
		{
		  // Username missing.
		  //setSysMessage(Conf.USERNAMEMISSING);
		  //return null;
		}
	  }
	  else
	  {
		  // Chatroom id missing.
		  //setSysMessage(Conf.CHATROOMIDMISSING);
		  //return null;
	  }
	}
	else
	{
//PHiL
		String jspDisplay = SystemProcessor.execute(request,session, application);
		if (jspDisplay != null)
		{
			response.sendRedirect(jspDisplay);
		}
//PHiL
	}
//PHiL
		//else
		{
			ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
			Monitor = jChatBox.Chat.Monitor.getInstance();
			ID = request.getParameter("id");
			if ( (ID != null) && (!ID.equals("")) )
			{
				id = Integer.parseInt(ID);
				aChatroom = ChatroomManager.getChatroom(id);

				/** Manager Users and Blacklist "windows" */
				String sub = request.getParameter("sub");
				if ( (sub != null) && (!sub.equals("")) )
				{
					if (sub.equals("viewusers")) session.putValue("winusers","open");
					else if (sub.equals("closeusers")) session.putValue("winusers","close");
					else if (sub.equals("viewblacklist")) session.putValue("winblacklist","open");
					else if (sub.equals("closeblacklist")) session.putValue("winblacklist","close");
				}
			}
			else
			{
				ID = (String) session.getValue("ID");
				if (ID == null) ID="";
			}
		}
%>
<%
String chatroomName = "";
if (aChatroom != null){ 
	chatroomName = (aChatroom.getParams().getName());
	}
String path = "";
path += "<a href=\"GoAdmin\">";
path += resource.getString("chat.administration");
path += "</a>&nbsp;>&nbsp;";
path += chatroomName;

browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(path);

operationPane.addOperation(resource.getIcon("chat.chatroomClear"),resource.getString("chat.clearChatroom"),"javascript:clear()");
operationPane.addOperation(resource.getIcon("chat.chatroomDelete"),resource.getString("chat.closeChatroom"),"javascript:close()");

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("chat.chatRoomList"),"Main",false);
tabbedPane.addTab(resource.getString("chat.administration"),"#",true);

boolean isPdcUsed = "yes".equals( (String) request.getAttribute("isPdcUsed") );
if (isPdcUsed)
{
	tabbedPane.addTab( resource.getString("PdcClassification"), "pdcPositions.jsp?Action=ViewPdcPositions&PubId="+ID+"", false);
}
Board board = gef.getBoard();
%>
<head>
<LINK REL=STYLESHEET TYPE="text/css" HREF="styles/admin.css">
<script language="JavaScript">
<!--
function viewusers()
{
	location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id=<%= ID %>&sub=viewusers";
}
function closeusers()
{
	location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id=<%= ID %>&sub=closeusers";
}
function viewblacklist()
{
	location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id=<%= ID %>&sub=viewblacklist";
}
function closeblacklist()
{
	location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id=<%= ID %>&sub=closeblacklist";
}
function update()
{
	if (document.chatroom.subject.value.length > 0)
	{
		document.chatroom.todo.value="updatechatroom";
		document.chatroom.submit();
	}
	else
	{
		alert("<%=resource.getString("chat.subjectMissing")%>");
	}
}
function close()
{
	location.href="menu.jsp?todo=closechatroom&id=<%= ID %>&rand=<%= System.currentTimeMillis() %>";
}
function clear()
{
	location.href="menu.jsp?todo=clearchatroom&id=<%= ID %>&rand=<%= System.currentTimeMillis() %>";
}
function generate()
{
	if (document.chatroom.filename.value.length > 0)
	{
		document.chatroom.todo.value="generatetranscript";
		document.chatroom.submit();
	}
	else
	{
		alert("<%=resource.getString("chat.nameMissing")%>");
	}
}
function logout()
{
	location.href="index.jsp?todo=logout&rand=<%= System.currentTimeMillis() %>";
}
function manage(opt)
{
	if (opt != "")
	{
		location.href="chatroom.jsp?todo=manage&rand=<%= System.currentTimeMillis() %>&id="+opt;
	}
}
function ban(name)
{
	document.chatroom.username.value=name;
	document.chatroom.todo.value="ban";
	document.chatroom.submit();
}
function kickoff(name)
{
	document.chatroom.username.value=name;
	document.chatroom.todo.value="kickoff";
	document.chatroom.submit();
}
function remove(name)
{
	document.chatroom.ip.value=name;
	document.chatroom.todo.value="remove";
	document.chatroom.submit();
}
function clearblacklist()
{
	document.chatroom.todo.value="clear";
	document.chatroom.submit();
}
//-->
</script>
<title>Chatroom Manager</title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form method="post" action="chatroom.jsp" name="chatroom">
<input type="hidden" name="todo">
<input type="hidden" name="id" value="<%= ID %>">
<input type="hidden" name="username">
<input type="hidden" name="ip">
<input type="hidden" name="rand" value="<%= System.currentTimeMillis() %>">
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
 		if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMUPDATED)) out.print(resource.getString("chat.updateSuccess"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMTRANSCRIPTED)) out.print(resource.getString("chat.transSuccess"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMSUBJECTMISSING)) out.print(resource.getString("chat.subjectMissing"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMIDMISSING)) out.print(resource.getString("chat.idMissing"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMNOTFOUND)) out.print(resource.getString("chat.roomMissing")); 
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.SYSTEMERROR)) out.print(resource.getString("chat.errorExecution")); 
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.USERNAMEMISSING)) out.print(resource.getString("chat.usernameMissing")); 
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.NAMENOTFOUND)) out.print(resource.getString("chat.usernameNotFound")); 
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.KICKEDOFF)) out.print(resource.getString("chat.kickedUser"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.BANNED)) out.print(resource.getString("chat.bannedUser"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CHATROOMBLACKLISTCLEARED)) out.print(resource.getString("chat.clearedBlackList"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.IPREMOVED)) out.print(resource.getString("chat.exitBlackList"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.CANNOTKICKOFFORBAN)) out.print(resource.getString("chat.rootUser"));
 		else if ((SystemProcessor.getSysMessage(session)).equals(Conf.NAMENOTAVAILABLE)) out.print(resource.getString("chat.nameAlreadyUsed"));
 		else out.print(SystemProcessor.getSysMessage(session));
    %>
<%
out.println(board.printBefore());
%>
<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.chatroomName")%> : 
		</td>
		<td align=left valign="baseline">
			<input type="text" name="chatroomName" size="50" maxlength="100" VALUE="<% if (aChatroom != null) out.print(aChatroom.getParams().getName()); %>" readonly>						
		</td>
	</tr>
	<tr>			
		<td valign="baseline" align=left  class="txtlibform">
			<%=resource.getString("chat.subject")%>
		</td>
		<td align=left valign="baseline">
			<input type="text" name="subject" size="50" maxlength="500" VALUE="<% if (aChatroom != null) out.print(aChatroom.getParams().getSubject()); %>"> 
		</td>
	</tr>
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.maxUser")%> : 
		</td>
		<td align=left valign="baseline">
			<input type="text" name="maxUser" size="50" maxlength="5" VALUE="<% if (aChatroom != null) out.print(aChatroom.getParams().getMaxUsers()); %>"> 
		</td>
	</tr>
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.history")%> : 
		</td>
		<td align=left valign="baseline">
			<select name="history" class="SystemBox">
				<%
					if (aChatroom != null)
					{
						int[] historyList = {20,30,35,40,45,50,55,60,70,80,90,100,120,150};
						int history = aChatroom.getParams().getHistory();
						String hStr = null;
						String Selected = null;
						for (int h=0;h<historyList.length;h++)
						{
							 hStr = ""+historyList[h];
							 if (historyList[h]==history) Selected = " selected";
							 else Selected = "";
							 %>
						<option value="<%= hStr %>"<%= Selected %>><font face="Verdana, Arial, Helvetica, sans-serif" size="-1"><%= hStr %></font></option>
						<%}
					}
				%>
			</select> 
		</td>
	</tr>				
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.refreshTime")%> : 
		</td>
		<td align=left valign="baseline">
			<select name="refreshlimit" class="SystemBox">
				<%
				if (aChatroom != null)
				{
					int[] refreshList = {3,5,8,10,12,15,18,20,25,30,40,50};
					int refresh = aChatroom.getParams().getRefreshLimit();
					String rStr = null;
					String Select = null;
					for (int r=0;r<refreshList.length;r++)
					{
						 rStr = ""+refreshList[r];
						 if (refreshList[r]==refresh) Select = " selected";
						 else Select = "";
						 %>
					<option value="<%= rStr %>"<%= Select %>><%= rStr %> <%=resource.getString("chat.seconds")%></option>
				<%}
				}
			 %>
			</select>
		</td>
	</tr>
	<tr>
		<td class="txtlibform">
			<%=resource.getString("chat.refreshMode")%> : 
		</td>
		<td align=left valign="baseline">
			<select name="refreshmodel" class="SystemBox">
				<%
				if (aChatroom != null)
				{
					int[] refreshModel = {1,2,3};
					String[] refreshModelList = {resource.getString("chat.time-constant"),resource.getString("chat.action-tracker"),resource.getString("chat.room-load")};
					int refresh = aChatroom.getParams().getRefreshModel();
					String rStr = null;
					String Select = null;
					for (int r=0;r<refreshModel.length;r++)
					{
						 rStr = refreshModelList[r];
						 if (refreshModel[r]==refresh) Select = " selected";
						 else Select = "";
						 %>
				<option value="<%= refreshModel[r] %>"<%= Select %>><%= rStr %></option>
				<%}
				}
			 %>
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
						<input type="radio" name="private" value="yes" <% if ( (aChatroom != null) && (aChatroom.getParams().getPrivateStatus() == true) ) out.print("checked"); %>>
					</td>
					<td width="100%"><%=resource.getString("GML.yes")%></td>
				</tr>
				<tr>
					<td align="left">
						<input type="radio" name="private" value="no" <% if ( (aChatroom != null) && (aChatroom.getParams().getPrivateStatus() == false) ) out.print("checked"); %>>
					</td>
					<td><%=resource.getString("GML.no")%></td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<%
      out.println(board.printAfter());
%>
<br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:update()", false));
		  out.println(buttonPane.print());
%>
<br><br>
<%
out.println(board.printBefore());
%>
<table width="100%" border="0" cellspacing="0" cellpadding="2">
		<%
			if ( (session.getValue("winusers") != null) && ((session.getValue("winusers")).equals("open")) )
			{%>
	<tr class="intfdcolor">
		<td>
			<a href="javascript:closeusers()"><img src="images/x.gif" border="0" alt="Manage users" align="absmiddle" title="Manage users"></a>&nbsp;
			<a href="javascript:closeusers()" class="textePetitBold"><%=resource.getString("chat.user")%></a>&nbsp;(<% if (aChatroom != null) out.print(aChatroom.getTotalUsers()); %>/<% if (aChatroom != null) out.print(aChatroom.getParams().getMaxUsers()); %>)
			<%
			operationPane.addOperation(resource.getIcon("chat.userlistRefresh"),resource.getString("chat.refreshUserList"),"javascript:viewusers()");
			%>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<table width="100%" border="0" cellspacing="5" cellpadding="0">
				<tr align="center">
					<td class="txttitrecol"><%=resource.getString("GML.name")%></td>
          <td class="txttitrecol"><%=resource.getString("chat.action")%></td>
          <td class="txttitrecol"><%=resource.getString("chat.lastAcces")%></td>
				</tr>
        <%
					Hashtable table = new Hashtable();
					if (Monitor != null) table = Monitor.getTable();
					Enumeration e = table.keys();
					ChatroomUser cUser = null;
					HttpSession cSession = null;
					int cID = -1;
					Vector filters = aChatroom.getParams().getFilters();
					jChatBox.Chat.Filter.Filter filter = null;
					String username = null;
					while (e.hasMoreElements())
					{
				cUser = (ChatroomUser) e.nextElement();
				cSession = (HttpSession) table.get(cUser);
				if (cUser.getParams().getChatroom() == 	id)
				{
					username = cUser.getName();
							/** Filter username for non-SYSTEM users */
					if (cUser.getType() != User.SYSTEM)
					{
						for (int f=0;f<filters.size();f++)
						{
							filter = (jChatBox.Chat.Filter.Filter) filters.elementAt(f);
											username = filter.process(username);
										}
									}
				%>
				<tr align="center">
					<td nowrap><%= username %></td>
					<td><a href="javascript:kickoff('<%= cUser.getName() %>')" class="menulink"><%=resource.getString("chat.kickout")%></a>&nbsp;&nbsp;<a href="javascript:ban('<%= cUser.getName() %>')" class="menulink"><%=resource.getString("chat.ban")%></a></td>
					<td>(<%= (System.currentTimeMillis()-cSession.getLastAccessedTime())/1000 %>/<%= cSession.getMaxInactiveInterval() %>)</td>
				</tr>
				<%
					}
				}
				%>
			</table>
		</td>
	</tr>
	<%}
		else
	{%>
  <tr class="intfdcolor">
		<td>
			<a href="javascript:viewusers()"><img src="images/arrowdown.gif" border="0" alt="Manage users" align="absmiddle" title="Manage users"></a>&nbsp;
			<a href="javascript:viewusers()" class="textePetitBold"><%=resource.getString("chat.user")%></a>&nbsp;(<% if (aChatroom != null) out.print(aChatroom.getTotalUsers()); %>/<% if (aChatroom != null) out.print(aChatroom.getParams().getMaxUsers()); %>)
		</td>
	</tr>
  <tr>
		<td>&nbsp;
		</td>
  </tr>
  <%}
  %>
</table>
<%out.println(board.printAfter());%>
<br>
<%out.println(board.printBefore());%>
<table width="100%%" border="0" cellspacing="0" cellpadding="2">
		<%
			Vector vList = (Vector)request.getAttribute( "chat_listBanned" ); // retreive the list of banned users.

			if ( (session.getValue("winblacklist") != null) && ((session.getValue("winblacklist")).equals("open")) )
			{%>
	<tr class="intfdcolor">
		<td>
			<a href="javascript:closeblacklist()"><img src="images/x.gif" border="0" alt="Manage users" align="absmiddle" title="Manage users"></a>&nbsp;
			<a href="javascript:closeblacklist()" class="textePetitBold"><%=resource.getString("chat.blackList")%></a>&nbsp;(<% 

				if (aChatroom != null) 
					out.print(vList.size()); 
			%>)
		</td>
		<%
			operationPane.addOperation(resource.getIcon("chat.clearBlacklist"),resource.getString("chat.clearBlackList"),"javascript:clearblacklist()");
		%>
	</tr>
	<tr>
		<td colspan="2">
			<table width="100%" border="0" cellspacing="5" cellpadding="0">
				<tr align="center">
					<td class="txttitrecol"><%=resource.getString("GML.name")%></td>
          <td class="txttitrecol"><%=resource.getString("chat.action")%></td>
				</tr>
				<%
					if (aChatroom != null)
					{
						//PHiL:
						//Vector vList = aChatroom.getBlacklist().getList();
						//PHiL
					for (int l=0;l<vList.size();l++)
					{
				%>
				<tr align="center">
					<td><%= (String) vList.elementAt(l) %></td>
					<td><a href="javascript:remove('<%= (String) vList.elementAt(l) %>')"><%=resource.getString("GML.delete")%></a></td>
				</tr>
				<%
						}
					}
				%>
			</table>
		</td>
	</tr>
	<%}
		else
	{%>
  <tr class="intfdcolor">
		<td>
			<a href="javascript:viewblacklist()"><img src="images/arrowdown.gif" border="0" align="absmiddle"></a>&nbsp;
			<a href="javascript:viewblacklist()" class="textePetitBold"><%=resource.getString("chat.blackList")%></a>&nbsp;(<% if (aChatroom != null) out.print(vList.size()); %>)
		</td>
	</tr>
  <tr>
		<td>&nbsp;
		</td>
  </tr>
  <%}
  %>
</table>
<%out.println(board.printAfter());%>                       
</form>

</center>
	<%out.println(frame.printAfter());%>
<%out.println(window.printAfter());%>
</body>
</html>
