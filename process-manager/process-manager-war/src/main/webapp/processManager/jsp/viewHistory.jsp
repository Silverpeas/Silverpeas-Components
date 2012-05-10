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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkProcessManager.jsp" %>

<%
	ProcessInstance process 				= (ProcessInstance) request.getAttribute("process");
	List 			steps 					= (List) request.getAttribute("steps");
	String   		enlightedStep 			= (String) request.getAttribute("enlightedStep");
	List			stepContents			= (List) request.getAttribute("StepsContent");
	Boolean 		isActiveUser 			= (Boolean) request.getAttribute("isActiveUser");
	Boolean 		isAttachmentTabEnable 	= (Boolean) request.getAttribute("isAttachmentTabEnable");
	boolean 		isProcessIdVisible 		= ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();
    boolean			isReturnEnabled = ((Boolean) request.getAttribute("isReturnEnabled")).booleanValue();

	com.silverpeas.form.Form form  = (com.silverpeas.form.Form) request.getAttribute("form");
	PagesContext context = (PagesContext) request.getAttribute("context");
	DataRecord data = (DataRecord) request.getAttribute("data");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));
	
	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	if ("supervisor".equalsIgnoreCase(currentRole))
	{
		tabbedPane.addTab(resource.getString("processManager.history"), "", true, false);
		tabbedPane.addTab(resource.getString("processManager.errors"), "adminViewErrors?processId=" + process.getInstanceId(), false, true);
	}
	else
	{
		if (isAttachmentTabEnable.booleanValue() && isActiveUser != null && isActiveUser.booleanValue())
			tabbedPane.addTab(resource.getString("processManager.attachments"), "attachmentManager?processId=" + process.getInstanceId(), false, true);
		tabbedPane.addTab(resource.getString("processManager.actions"), "listTasks", false, true);
		if (isReturnEnabled) {
			tabbedPane.addTab(resource.getString("processManager.questions"), "listQuestions?processId=" + process.getInstanceId(), false, true);
		}
		tabbedPane.addTab(resource.getString("processManager.history"), "", true, false);
	}
	
	operationPane.addOperation(resource.getIcon("processManager.print"), resource.getString("GML.print"), "javascript:window.print();");
%>

<%@page import="com.silverpeas.util.StringUtil"%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
</HEAD>
<BODY class="yui-skin-sam">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
	out.println(frame.printBefore());
%>
<table CELLPADDING="0" CELLSPACING="2" BORDER="0" WIDTH="98%">
<tr><td align="right">
	<% if ("all".equalsIgnoreCase(enlightedStep)) { %>
		<a href="viewHistory"><%=resource.getString("processManager.collapseAll") %></a>
	<% } else { %>
		<a href="viewHistory?enlightedStep=all"><%=resource.getString("processManager.expandAll") %></a>
	<% } %>
</td></tr>
</table>
<CENTER>
<% 
	for (int i=0; i<steps.size(); i++) // boucle sur tous les process
	{
	  StepVO step = (StepVO) steps.get(i);
%>
<table CELLPADDING="0" CELLSPACING="2" BORDER="0" WIDTH="98%" CLASS="intfdcolor">
<form name="formCollapse" action="viewHistory">
<input type="Hidden" name="enlightedStep" value="<%=enlightedStep %>">
	<tr>
		<td CLASS=intfdcolor4 NOWRAP>
			<table CELLPADDING=0 CELLSPACING=0 BORDER=0 WIDTH="100%">
				<tr>
					<td class="intfdcolor" rowspan="2" nowrap width="100%">
						<img border="0" src="<%=resource.getIcon("processManager.px") %>" width="5">
						<span class="txtNav">
						<% 
						if ( StringUtil.isDefined( step.getActivity() ) )
							out.println(step.getActivity()+" - ");
						%>
						<%= step.getActionName()%> (<%=step.getActorFullName()%> - <%=step.getStepDate()%>)
						</span>
					</td>
					<td class="intfdcolor"><img border="0" height="10" src="<%=resource.getIcon("processManager.px") %>"></td>
					<td class="intfdcolor"><img border="0" height="10" src="<%=resource.getIcon("processManager.px") %>"></td>
				</tr>
				<tr>
					<td height="0" class="intfdcolor" align="right" valign="bottom"><img border="0" src="<%=resource.getIcon("processManager.boxAngleLeft") %>"></td>
					<td align="center" valign="bottom" nowrap>
						<%
						if ( (step.isVisible()) || ("supervisor".equalsIgnoreCase(currentRole)) )
						{
							if (step.getContent() == null) {
								out.println("<A href=\"viewHistory?enlightedStep="+step.getStepId()+"\"><img border=\"0\" src=\""+resource.getIcon("processManager.boxDown")+"\"></a>");
							}
							else{
								out.println("<A href=\"viewHistory\"><img border=\"0\" src=\""+resource.getIcon("processManager.boxUp")+"\"></a>");
							}
						}
						%>
					<img border="0" height="1" width="3" src="<%=resource.getIcon("processManager.px") %>">
					</td>
				</tr>
			</table>
			<%
			if (step.getContent() != null) 
			{
				form = step.getContent().getForm();
				context = step.getContent().getPageContext();
				data = step.getContent().getRecord();
				
				if (form == null || data == null || ( !step.isVisible() && !("supervisor".equalsIgnoreCase(currentRole))) )
				{
				%>
					<table border="0" cellpadding="0" cellspacing="0"><tr><td class="intfdcolor4"></td><img border="0" src="<%=resource.getIcon("processManager.px") %>"></tr></table>
				<%	
				}
				else
				{
				%>
					<table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
						<tr>
							<td align="center">
							<%
								context.setBorderPrinted(true);
								form.display(out, context, data); 
							%>
							</td>
						</tr>
						<tr>
							<td colspan="2" align="right"><a href="viewHistory"><img border="0" src="<%=resource.getIcon("processManager.boxUp") %>"></a><img border="0" width="3" src="<%=resource.getIcon("processManager.px") %>"></td>
						</tr>
					</table>
				<%
				}
			}
			else
			{
			%>
				<table border="0" cellpadding="0" cellspacing="0"><tr><td class="intfdcolor4"></td><img border="0" src="<%=resource.getIcon("processManager.px") %>"></tr></table>
			<%
			}
			%>
		</td>
	</tr>
</form>	
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