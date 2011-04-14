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

<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,jChatBox.*,java.util.*,java.text.SimpleDateFormat,com.stratelia.silverpeas.silvertrace.*,com.stratelia.silverpeas.chat.control.*" %>
<%@ include file="../configureme.jsp" %>
<%@ include file="../checkChat.jsp" %>
<jsp:useBean id="UserLogin" class="jChatBox.Service.UserLogin" scope="page" />
<%!
	// Overides jspDestroy method to backup jChatBox.
	public void jspDestroy()
	{
		jChatBox.Util.Debug.log(0,"jChatBox skinDestroy() ...","");
		jChatBox.Chat.ChatroomManager chatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
		chatroomManager.destroy();
		jChatBox.Util.Debug.destroy();
	}
%>
<%

//PHiL:
/* REMOVED: Autologin function, but can be used for futur improvment, DO NOT DELETE!
		int cID = 0 ;
		ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
		if ( cUser != null )
		{ // the user is already using the chat
			if ( cUser.getName().equals("system") )
			{
				// the user was in admin mode, remove it before continu
				session.removeValue(XMLConfig.USERSESSIONID);
			}
			else
			{
				cID = cUser.getParams().getChatroom();
			}
		}
*/
//PHiL:


	jChatBox.Chat.ChatroomManager ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
	String chatroomTotalUsers = "", chatroomMaxUsers = "", chatroomName = "", chatroomSubject = "", chatroomDate = "";
	String chatroomID = request.getParameter("id");
	int ID = -1;
	if ( (chatroomID != null) && (!chatroomID.equals("")) )
	{
		try
		{
			ID = Integer.parseInt(chatroomID);
			Chatroom chatroom = ChatroomManager.getChatroom(ID);
			chatroomName = chatroom.getParams().getName();
			chatroomSubject = chatroom.getParams().getSubject();
			chatroomMaxUsers = ""+chatroom.getParams().getMaxUsers();
			chatroomTotalUsers = ""+chatroom.getTotalUsers();
			chatroomDate = resource.getOutputDateAndHour(chatroom.getDate());
		} catch (Exception e)
		  {}
	}
	else
	{
		Vector vChat = ChatroomManager.getChatrooms();
		if (vChat.size() > 0)
		{
			Chatroom chatroom = (Chatroom) vChat.elementAt(0);
			ID = chatroom.getParams().getID();
			chatroomName = chatroom.getParams().getName();
			chatroomSubject = chatroom.getParams().getSubject();
			chatroomMaxUsers = ""+chatroom.getParams().getMaxUsers();
			chatroomTotalUsers = ""+chatroom.getTotalUsers();
			chatroomDate = resource.getOutputDateAndHour(chatroom.getDate());
		}
	}
	String jspDisplay = UserLogin.doLogin(request, session);
	if (jspDisplay != null)
	{
		if (jspDisplay.equals("room.jsp") )
		{
			//jChatBox.Chat.ChatroomManager ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
			ChatroomUser cUser2 = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);

			if (cUser2 != null)
			{
				// look for the chatroom ID he joined
				int cID2 = cUser2.getParams().getChatroom();
				ChatSessionController chatSC = (ChatSessionController) request.getAttribute("chatSC");
				// see if he is banned or not
				if ( chatSC.RetreiveBanned( (String)request.getAttribute("chat_fullName") , cID2  ) )
				{
					session.putValue("chat_banned" , "true" );
				}
			}
		}
		//response.sendRedirect(response.encodeRedirectURL(jspDisplay));
		%><jsp:forward page="<%= response.encodeURL(jspDisplay) %>" /><%
	}
%>
<html>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(resource.getString("chat.chatRoomList"));

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("chat.chatRoomList"),"#",true);
tabbedPane.addTab(resource.getString("chat.administration"),"javascript:manageWindow('goAdmin','GoAdmin','popupChat','1', '1', 'menubar=no,scrollbars=no,statusbar=no')",false);
%>

<head>
<script language="Javascript">

function auto_open_chatroom()
{

<%	
	//PHiL
	// if the user came from the search engine, autolog him to the chatroom he choose.
	Integer cID_search;
	String tmp = (String)request.getAttribute("chat_id_search");
	if (tmp != null)
	{
		cID_search = Integer.valueOf(tmp);
		out.println("manageWindow('open','redirect.jsp?chatrooms=" + cID_search + "&name=" + request.getAttribute("chat_fullName") + "','popupChat','650', '400', 'menubar=no,scrollbars=no,statusbar=no'); ");
	}
	//PHiL

%>
}

function manageWindow(action,page,nom,largeur,hauteur,options)
{
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	if (action=="open")
	{
		fenetre=window.open(page+'&action='+action,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
		return fenetre;
	}
	if (action=="goAdmin")
	{
<% // ajouter test cUser existe

	ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
	if ( cUser != null )
	{
	
			if ( cUser.getName().equals("system") )
			{
				// the user was in admin mode, remove it before continu
				session.removeValue(XMLConfig.USERSESSIONID);
				out.print("location.href='GoAdmin';");
			}
			else
			{
			%>
				if (window.confirm('<%=resource.getString("chat.quitChatroom")%>') )
				{
					top = 2000;
					left = 2000;
					fenetre=window.open('quitChatroom.jsp?action='+action,'popupChat','top='+top+',left='+left);
					location.href="GoAdmin";
				}
			<%
			}
	}
	else
	{
	%>
		location.href="GoAdmin";
	<%
	}
	%>
	}
}

/*
function ready()
{
	if (typeof(self.parent.content) != "undefined")
	{
		self.parent.location.href="login.jsp";
	}
}
*/

function enterPopup(identifiant)
{
	document.chat.chatrooms.value=identifiant;
	manageWindow('open','redirect.jsp?chatrooms='+identifiant+'&name=<%= request.getAttribute("chat_fullName") %>','popupChat','650', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
</script>
<title>Chat Login</title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" onLoad="auto_open_chatroom()">
<%

	out.println(window.printBefore());

	
	if ( request.getAttribute( "chat_isAdmin" ).equals("yes") )
	{
	//if admin
		out.println(tabbedPane.print());
	}
		out.println(frame.printBefore());
%>

<form method="post" action="<%= response.encodeURL("login.jsp") %>" name="chat">

<%// Hidden field that contains user's login %>
<input type="Hidden" value="<%= request.getAttribute("chat_fullName") %>" name="name">
<input type="Hidden" value="" name="chatrooms">

<%// Message for exceptionnal cases
 	if (UserLogin.getSysMessage()==Conf.CHATROOMNOTFOUND) out.print(resource.getString("chat.roomNotFound"));
 	else if (UserLogin.getSysMessage()==Conf.CLOSED) out.print(resource.getString("chat.roomClosed"));
 	else if (UserLogin.getSysMessage()==Conf.NOVACANCIES) out.print(resource.getString("chat.roomFull"));
 	else if (UserLogin.getSysMessage()==Conf.NAMENOTAVAILABLE) out.print(resource.getString("chat.unavailableName"));
 	else if (UserLogin.getSysMessage()==Conf.BANNED) out.print(resource.getString("chat.youreBanned"));
%>

<%
// Tableau
  ArrayPane arrayPane = gef.getArrayPane("List", "Main", request,session);
  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);
  arrayPane.addArrayColumn(resource.getString("chat.chatRoom"));
  arrayPane.addArrayColumn(resource.getString("chat.user"));
  arrayPane.addArrayColumn(resource.getString("chat.openingDate"));
  ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("chat.subject"));
  arrayColumn.setSortable(false);

// From login.jsp
	if (ChatroomManager != null)
    {
	  	Vector chatrooms = ChatroomManager.getChatrooms();
	  	Chatroom chatroom = null;
	  	String selection = "";
		Vector listChatRoom = (Vector)request.getAttribute( "chat_listChatRoom" );
	  	for (int i=0;i<chatrooms.size();i++)
	  	{
			chatroom = (Chatroom) chatrooms.elementAt(i);
			if ( listChatRoom.contains( String.valueOf( chatroom.getParams().getID() ) ) )
			{
				ArrayLine arrayLine = arrayPane.addArrayLine();
				IconPane iconPane1 = gef.getIconPane();
				Icon debIcon = iconPane1.addIcon();
				debIcon.setProperties(resource.getIcon("chat.chatIcon"),resource.getString("chat.chatRoom"), "javascript:enterPopup('"+new Integer(chatroom.getParams().getID()).toString()+"')");
				arrayLine.addArrayCellIconPane(iconPane1);	
				arrayLine.addArrayCellLink(chatroom.getParams().getName(), "javascript:enterPopup('"+new Integer(chatroom.getParams().getID()).toString()+"')"); 
				arrayLine.addArrayCellText(chatroom.getTotalUsers()+"/"+chatroom.getParams().getMaxUsers());
				ArrayCellText cell = arrayLine.addArrayCellText(resource.getOutputDateAndHour(chatroom.getDate()));
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