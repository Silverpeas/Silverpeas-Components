<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%@ include file="check.jsp" %>

<%@page import="java.util.List"%>
<%@page import="com.silverpeas.formsonline.model.FormDetail"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.silverpeas.publicationTemplate.PublicationTemplate"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.stratelia.webactiv.beans.admin.Group"%>

<html>
<head>
<%!	
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang("");
	ResourceLocator resourceJSPP = new ResourceLocator("com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasIcons", "");
%>

<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
    FormDetail form = (FormDetail) request.getAttribute("currentForm");
    List m_listGroupSenders = (List) request.getAttribute("sendersAsGroup");
    List m_listUserSenders = (List) request.getAttribute("sendersAsUser");
    List m_listGroupReceivers = (List) request.getAttribute("receiversAsGroup");
    List m_listUserReceivers = (List) request.getAttribute("receiversAsUser");
    
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel);
    
    TabbedPane tabbedPane = gef.getTabbedPane(1);
    tabbedPane.addTab(resource.getString("formsOnline.Form"), "Main", false,1);  
    tabbedPane.addTab(resource.getString("formsOnline.SendersReceivers"), "SendersReceivers", true,1);
    tabbedPane.addTab(resource.getString("formsOnline.Preview"), "Preview", false,1);
    
    operationPane.addOperation(resourceJSPP.getString("JSPP.userManage"), resource.getString("formsOnline.ModifySenders") , "ModifySenders");
    operationPane.addOperation(resourceJSPP.getString("JSPP.userManage"), resource.getString("formsOnline.ModifyReceivers") , "ModifyReceivers");
    
    Board board = gef.getBoard();
%>	

	<%=window.printBefore()%>
	<%=tabbedPane.print()%>
	<%=frame.printBefore()%>

	<%=board.printBefore()%>
	<TABLE width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<TR>
			<TD colspan="2" class="txttitrecol"><%=resource.getString("formsOnline.senders")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=generalMessage.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=generalMessage.getString("GML.name")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>"></TD>
		</TR>
		
		<%
		// La boucle sur les groupes 
		Iterator groups = m_listGroupSenders.iterator();
		Group group = null;
		while (groups.hasNext())
		{
			group = (Group) groups.next();
			out.println("<TR>");
			if (group.isSynchronized())
				out.println("<TD align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.scheduledGroup")+"\"></TD>");
			else
				out.println("<TD align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.group")+"\"></TD>");
			out.println("<TD align=\"center\">"+group.getName()+"</TD>");
			out.println("</TR>");
		}
		
		// La boucle sur les users
		Iterator users = m_listUserSenders.iterator();
		UserDetail user = null;
		while (users.hasNext())
		{
			user = (UserDetail) users.next();
			out.println("<TR>");
			out.println("<TD align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.user")+"\"></TD>");
			out.println("<TD align=\"center\">"+user.getLastName() + " " + user.getFirstName()+"</TD>");
			out.println("</TR>");
		}
		%>			
		<TR>
			<TD colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>"></TD>
		</TR>
	</TABLE>
    <%=board.printAfter()%>
	<br>
	<%=board.printBefore()%>
	<TABLE width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
		<TR>
			<TD colspan="2" class="txttitrecol"><%=resource.getString("formsOnline.receivers")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol"><%=generalMessage.getString("GML.type")%></TD>
			<TD align="center" class="txttitrecol"><%=generalMessage.getString("GML.name")%></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>"></TD>
		</TR>
		
		<%
		// La boucle sur les groupes 
		groups = m_listGroupReceivers.iterator();
		group = null;
		while (groups.hasNext())
		{
			group = (Group) groups.next();
			out.println("<TR>");
			if (group.isSynchronized())
				out.println("<TD align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.scheduledGroup")+"\"></TD>");
			else
				out.println("<TD align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.group")+"\"></TD>");
			out.println("<TD align=\"center\">"+group.getName()+"</TD>");
			out.println("</TR>");
		}
		
		// La boucle sur les users
		users = m_listUserReceivers.iterator();
		user = null;
		while (users.hasNext())
		{
			user = (UserDetail) users.next();
			out.println("<TR>");
			out.println("<TD align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.user")+"\"></TD>");
			out.println("<TD align=\"center\">"+user.getLastName() + " " + user.getFirstName()+"</TD>");
			out.println("</TR>");
		}
		%>			
		<TR>
			<TD colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>"></TD>
		</TR>
	</TABLE>
	
    <%=board.printAfter()%>
    <%=frame.printAfter()%>
  	<%=window.printAfter()%>  

</body>
</html>