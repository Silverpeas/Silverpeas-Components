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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="../checkProcessManager.jsp" %>

<%
	ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	WorkflowError[] errors = (WorkflowError[])  request.getAttribute("errors");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"adminListProcess");
	browseBar.setPath(process.getTitle(currentRole, language));

	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
	tabbedPane.addTab(resource.getString("processManager.errors"), "", true, false);
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
</HEAD>
<BODY>
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<CENTER>
<% 
	for (int i=0; i<errors.length; i++) // boucle sur toutes les erreurs
	{
%>
		<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="98%" CLASS=intfdcolor>
			<tr>
				<td CLASS=intfdcolor4 NOWRAP>
					<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
						<tr>
							<td class="intfdcolor" nowrap width="100%">
								<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
								<span class="txtNav">
									<%=errors[i].getErrorMessage()%> - (<%=errors[i].getUser().getFullName()%>)
								</span>
							</td>
						</tr>
					</table>
					<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
						<tr>
							<td>
								<span class="textePetit">
									<%=errors[i].getStackTrace()%>
								</span>
							</td>
						</tr>
						<tr><td colspan=3><img src="<%=resource.getIcon("processManager.px") %>"></td></tr>
					</table>
				</td>
			</tr>	
		</table>

<%
	}
%>



</CENTER>
<%
   out.println(frame.printAfter());
   out.println(window.printAfter());
%>
</BODY>
