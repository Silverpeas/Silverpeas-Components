<%--

    Copyright (C) 2000 - 2017 Silverpeas

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
<%@ include file="checkProcessManager.jsp" %>

<%
	ProcessInstance process 				= (ProcessInstance) request.getAttribute("process");
	List 			steps 					= (List) request.getAttribute("steps");
	String   		enlightedStep 			= (String) request.getAttribute("enlightedStep");
	Boolean 		isActiveUser 			= (Boolean) request.getAttribute("isActiveUser");
	Boolean 		isAttachmentTabEnable 	= (Boolean) request.getAttribute("isAttachmentTabEnable");
	boolean 		isProcessIdVisible 		= (Boolean) request.getAttribute("isProcessIdVisible");
  boolean			isReturnEnabled = (Boolean) request.getAttribute("isReturnEnabled");
  int nbEntriesAboutQuestions = (Integer) request.getAttribute("NbEntriesAboutQuestions");

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");
	
	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));
	
	tabbedPane.addTab(resource.getString("processManager.details"), "viewProcess?processId=" + process.getInstanceId()+"&force=true", false, true);
	if ("supervisor".equalsIgnoreCase(currentRole)) {
		tabbedPane.addTab(resource.getString("processManager.history"), "#", true, true);
		tabbedPane.addTab(resource.getString("processManager.errors"), "adminViewErrors?processId=" + process.getInstanceId(), false, true);
  } else {
		if (isAttachmentTabEnable && isActiveUser != null && isActiveUser)
			tabbedPane.addTab(resource.getString("processManager.attachments"), "attachmentManager?processId=" + process.getInstanceId(), false, true);
		if (isReturnEnabled & nbEntriesAboutQuestions > 0) {
			tabbedPane.addTab(resource.getString("processManager.questions")+" ("+nbEntriesAboutQuestions+")", "listQuestions?processId=" + process.getInstanceId(), false, true);
		}
		tabbedPane.addTab(resource.getString("processManager.history"), "#", true, true);
	}
	
	operationPane.addOperation(resource.getIcon("processManager.print"), resource.getString("GML.print"), "javascript:window.print();");
%>

<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.processmanager.StepVO" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<view:includePlugin name="preview"/>
</head>
<body class="yui-skin-sam">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
%>
<view:frame>


<div class="txt-align-right">
	<% if ("all".equalsIgnoreCase(enlightedStep)) { %>
	<a  class="bend-all-item btn-deploy-all-item bgDegradeGris " title="<%=resource.getString("processManager.collapseAll") %>"  href="viewHistory">
    <img class="deploy-item-rssNews" src="/silverpeas/util/icons/arrow/closed.gif"  alt=""/> <%=resource.getString("processManager.collapseAll") %></a>
	<% } else { %>
		<a class="deploy-all-item btn-deploy-all-item bgDegradeGris " title="<%=resource.getString("processManager.expandAll") %>" href="viewHistory?enlightedStep=all">
				<img class="deploy-item-rssNews" src="/silverpeas/util/icons/arrow/open.gif" alt=""/> <%=resource.getString("processManager.expandAll") %></a>

	<% } %>
</div>
<% 
	for (int i=0; i<steps.size(); i++) // boucle sur tous les process
	{
	  StepVO step = (StepVO) steps.get(i);
%>

<form name="formCollapse" action="viewHistory">
<input type="hidden" name="enlightedStep" value="<%=enlightedStep %>"/>

			<div class="bgDegradeGris ">
						<p class="txtnav">
						<% 
						if ( StringUtil.isDefined( step.getActivity() ) )
							out.println(step.getActivity()+" - ");
						%>
						<%= step.getActionName()%> (<%=step.getActorFullName()%> - <%=step.getStepDate()%>)
						</p>
						<%
						if ( (step.isVisible()) || ("supervisor".equalsIgnoreCase(currentRole)) )
						{
							if (step.getContent() == null) {
								out.println("<a class=\"deploy-item\" href=\"viewHistory?enlightedStep="+step.getStepId()+"\"><img  alt=\"\" src=\""+resource.getIcon("processManager.boxDown")+"\"></a>");
							}
							else{
								out.println("<a class=\"deploy-item\" href=\"viewHistory\"><img  alt=\"\" src=\""+resource.getIcon("processManager.boxUp")+"\"></a>");
							}
						}
						%>
					
			</div>
			<%
			if (step.getContent() != null) 	{
				Form form = step.getContent().getForm();
				PagesContext context = step.getContent().getPageContext();
				DataRecord data = step.getContent().getRecord();
				
				if (form == null || data == null || ( !step.isVisible() && !("supervisor".equalsIgnoreCase(currentRole))) ) {
				%>
					<%--nothing to do here--%>
				<%
				}
				else
				{
				%>
					<%
								form.display(out, context, data); 
							%>
					<a class="btn-closed" href="viewHistory"><img   alt="" border="0" src="<%=resource.getIcon("processManager.boxUp") %>"/></a>
				<%
				}
			}
			else
			{
			%>

			<%
			}
			%>

</form>	

<%
   }
%>
</view:frame>
<%
   out.println(window.printAfter());
%>
</body>
</html>