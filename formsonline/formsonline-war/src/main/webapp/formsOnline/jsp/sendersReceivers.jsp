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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<%!	
	ResourceLocator resourceJSPP = new ResourceLocator("com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasIcons", "");
%>
<view:looknfeel/>
</head>
<body>

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
%>	

	<%=window.printBefore()%>
	<%=tabbedPane.print()%>
	<%=frame.printBefore()%>

	<%=board.printBefore()%>
	<table width="70%" align="center" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td colspan="2" class="txttitrecol"><%=resource.getString("formsOnline.senders")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>" alt=""/></td>
		</tr>
		<tr>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.type")%></td>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.name")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>" alt=""/></td>
		</tr>
		
		<%
		// La boucle sur les groupes 
		Iterator groups = m_listGroupSenders.iterator();
		Group group = null;
		while (groups.hasNext())
		{
			group = (Group) groups.next();
			out.println("<tr>");
			if (group.isSynchronized())
				out.println("<td align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.scheduledGroup")+"\"></td>");
			else
				out.println("<td align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.group")+"\"></td>");
			out.println("<td align=\"center\">"+group.getName()+"</td>");
			out.println("</tr>");
		}
		
		// La boucle sur les users
		Iterator users = m_listUserSenders.iterator();
		UserDetail user = null;
		while (users.hasNext()) {
			user = (UserDetail) users.next(); %>
			<tr>
			<td align="center"><img src="<%=m_context+resourceJSPP.getString("JSPP.user") %>"/></td>
			<td align="center"><view:username userId="<%=user.getId()%>"/></td>
			</tr>
		<% } %>	
		<tr>
			<td colspan="2" align="center" class="intfdcolor"  height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>" alt=""/></td>
		</tr>
	</table>
    <%=board.printAfter()%>
	<br/>
	<%=board.printBefore()%>
	<table width="70%" align="center" border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td colspan="2" class="txttitrecol"><%=resource.getString("formsOnline.receivers")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>" alt=""/></td>
		</tr>
		<tr>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.type")%></td>
			<td align="center" class="txttitrecol"><%=resource.getString("GML.name")%></td>
		</tr>
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>" alt=""/></td>
		</tr>
		
		<%
		// La boucle sur les groupes 
		groups = m_listGroupReceivers.iterator();
		group = null;
		while (groups.hasNext())
		{
			group = (Group) groups.next();
			out.println("<tr>");
			if (group.isSynchronized())
				out.println("<td align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.scheduledGroup")+"\"></td>");
			else
				out.println("<td align=\"center\"><IMG SRC=\""+m_context+resourceJSPP.getString("JSPP.group")+"\"></td>");
			out.println("<td align=\"center\">"+group.getName()+"</td>");
			out.println("</tr>");
		}
		
		// La boucle sur les users
		users = m_listUserReceivers.iterator();
		user = null;
		while (users.hasNext()) {
			user = (UserDetail) users.next(); %>
			<tr>
			<td align="center"><img src="<%=m_context+resourceJSPP.getString("JSPP.user") %>"/></td>
			<td align="center"><view:username userId="<%=user.getId()%>"/></td>
			</tr>
		<% } %>	
		<tr>
			<td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=m_context%><%=resourceJSPP.getString("JSPP.px")%>" alt=""/></td>
		</tr>
	</table>
	
    <%=board.printAfter()%>
    <%=frame.printAfter()%>
  	<%=window.printAfter()%>  

</body>
</html>