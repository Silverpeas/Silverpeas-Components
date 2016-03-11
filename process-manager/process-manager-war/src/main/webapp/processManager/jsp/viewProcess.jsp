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

<%@ include file="checkProcessManager.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
	ProcessInstance 			process 					= (ProcessInstance) request.getAttribute("process");
	com.silverpeas.form.Form 	form 						= (com.silverpeas.form.Form) request.getAttribute("form");
	PagesContext 				context 					= (PagesContext) request.getAttribute("context");
	DataRecord 					data 						= (DataRecord) request.getAttribute("data");
	String[] 					deleteAction 				= (String[]) request.getAttribute("deleteAction");
	String[] 					activeStates 				= (String[]) request.getAttribute("activeStates");
	String[] 					activeRoles 				= (String[]) request.getAttribute("activeRoles");
	Boolean 					isActiveUser 				= (Boolean) request.getAttribute("isActiveUser");
	Boolean 					isAttachmentTabEnable 		= (Boolean) request.getAttribute("isAttachmentTabEnable");
	Boolean 					isHistoryTabEnable 			= (Boolean) request.getAttribute("isHistoryTabEnable");
	boolean 					isProcessIdVisible 			= ((Boolean) request.getAttribute("isProcessIdVisible")).booleanValue();
	boolean 					isPrintButtonEnabled 		= ((Boolean) request.getAttribute("isPrintButtonEnabled")).booleanValue();
	List	 					locks		 				= ((List) request.getAttribute("locks"));
	boolean						hasLockingUsers				= (locks != null) && (locks.size()>0);
	boolean						isCurrentUserIsLockingUser 	= ((Boolean) request.getAttribute("isCurrentUserIsLockingUser")).booleanValue();
	boolean						isReturnEnabled 			= ((Boolean) request.getAttribute("isReturnEnabled")).booleanValue();
	String 						versionning 				= (String) request.getAttribute("isVersionControlled");
	boolean isVersionControlled = "1".equals(versionning);

	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"listProcess");

	String processId = "";
	if (isProcessIdVisible)
		processId = "#"+process.getInstanceId()+" > ";
	browseBar.setPath(processId+process.getTitle(currentRole, language));

	if (isPrintButtonEnabled)
	{
		operationPane.addOperation(resource.getIcon("processManager.print"),
			resource.getString("processManager.print"),
			"javascript:printProcess()");
	}
	tabbedPane.addTab(resource.getString("processManager.details"), "#", true, true);

	if ("supervisor".equalsIgnoreCase(currentRole))
	{
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("processManager.reassign"),
				resource.getString("processManager.reassign"),
				"adminReAssign?processId="+process.getInstanceId());

		operationPane.addOperation(resource.getIcon("processManager.remove"),
				resource.getString("processManager.remove"),
				"adminRemoveProcess?processId="+process.getInstanceId());

		tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
		tabbedPane.addTab(resource.getString("processManager.errors"), "adminViewErrors?processId=" + process.getInstanceId(), false, true);
	}
	else
	{
		if (deleteAction != null)
		{
			operationPane.addOperation(resource.getIcon("processManager.remove"),
										deleteAction[2],
										"editAction?state="+deleteAction[1]+"&action="+deleteAction[0]);
		}

		if (isAttachmentTabEnable.booleanValue() && isActiveUser != null && isActiveUser.booleanValue())
			tabbedPane.addTab(resource.getString("processManager.attachments"), "attachmentManager?processId=" + process.getInstanceId(), false, true);
		if (!hasLockingUsers || isCurrentUserIsLockingUser)
		  tabbedPane.addTab(resource.getString("processManager.actions"), "listTasks", false, true);

		if (isReturnEnabled) {
			tabbedPane.addTab(resource.getString("processManager.questions"), "listQuestions?processId=" + process.getInstanceId(), false, true);
		}
		if (isHistoryTabEnable.booleanValue())
			tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
	}

%>

<%@page import="java.util.Iterator"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript">
function printProcess() {
    url = "printProcessFrameset";
    windowName = "printProcess";
    larg = "600";
    haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
    SP_openWindow(url, windowName, larg , haut, windowParams);
}
</script>
</head>
<body class="yui-skin-sam">
<div id="<%=componentId%>">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
%>
<view:frame>
			<% if (hasLockingUsers) {%>
			
		<div class="inlineMessage un">	
			<p class="txtnav"><%=resource.getString("processManager.actionInProgress") %> </p>
					
			
				<c:choose>
					<c:when test="${isCurrentUserIsLockingUser}">

						<p class="textePetitBold">
						<%=resource.getString("processManager.youHaveAnActionToFinish") %>
						</p>

					</c:when>

					<c:otherwise>
						<c:forEach items="${locks}" var="userlock">

								<p class="textePetitBold">
									<%=resource.getString("processManager.instanceLockedBy")%> ${userlock.user.fullName}
									<%=resource.getString("processManager.since")%> <fmt:formatDate value="${userlock.lockDate}" pattern="dd MMM yyyy"/>
									<c:if test="${currentRole eq 'supervisor'}">
										<c:if test="${userlock.removableBySupervisor}">
											<c:url value="/util/icons/delete.gif" var="removeIconUrl" />
											<a href="removeLock?processId=${process.instanceId}&stateName=${userlock.state}&userId=${userlock.user.userId}">
												<img alt="" src="${removeIconUrl}" border="0"/>
											</a>
										</c:if>
									</c:if>
								</p>
	
						</c:forEach>
					</c:otherwise>
				</c:choose>
			</div>

			<br/>
			<%
			}
			%>
			
		<div class="inlineMessage deux">
			<p class="txtnav"><%=resource.getString("processManager.activeStates") %> </p>
					
					<%
						if (activeStates==null || activeStates.length==0)
						{
							%>

							<p class="textePetitBold">
							<%=resource.getString("processManager.noActiveState") %>
							</p>

							<%
						}
						else
						{
							for (int i=0; i<activeStates.length; i++)
							{
							%>

							   <p>
								 <span class="textePetitBold">&#149;&nbsp;
								<%=activeStates[i]%></span>
								<% if (activeRoles != null && i<activeRoles.length && activeRoles[i] != null && activeRoles[i].length() > 0) { %>
								   (<%=activeRoles[i]%>)
								<% } %>
							   </p>

							<%
							}
						}
					%>
		</div>	
<br/>

<table width="100%">
<tr><td>
<%
	context.setBorderPrinted(false);
   	form.display(out, context, data);
%>
</td><td valign="top">

<%
	out.flush();
	if (!isVersionControlled) {
  		getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+process.getInstanceId()+"&ComponentId="+componentId+"&Context=attachment").include(request, response);
	} else {
		getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+process.getInstanceId()+"&ComponentId="+componentId+"&Context=attachment").include(request, response);
	}
%>
</td></tr></table>

</view:frame>
<%
   out.println(window.printAfter());
%>
</div>
</body>
</html>