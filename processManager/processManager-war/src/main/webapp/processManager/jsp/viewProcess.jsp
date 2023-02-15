<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkProcessManager.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="backToLabel" value='<%=resource.getString("processManager.backTo")%>'/>

<%
	ProcessInstance process = (ProcessInstance) request.getAttribute("process");
	Form form = (Form) request.getAttribute("form");
	PagesContext context = (PagesContext) request.getAttribute("context");
	DataRecord data = (DataRecord) request.getAttribute("data");
	String[] deleteAction = (String[]) request.getAttribute("deleteAction");
	List<CurrentState> activeStates = (List<CurrentState>) request.getAttribute("activeStates");
	boolean isAttachmentTabEnabled = (Boolean) request.getAttribute("isAttachmentTabEnabled");
	boolean isHistoryTabEnable = (Boolean) request.getAttribute("isHistoryTabEnable");
	boolean isProcessIdVisible = (Boolean) request.getAttribute("isProcessIdVisible");
	boolean isPrintButtonEnabled = (Boolean) request.getAttribute("isPrintButtonEnabled");
	List<LockVO> locks = (List<LockVO>) request.getAttribute("locks");
	boolean hasLockingUsers = CollectionUtil.isNotEmpty(locks);
	boolean isCurrentUserIsLockingUser = (Boolean) request.getAttribute("isCurrentUserIsLockingUser");
	boolean isReturnEnabled = (Boolean) request.getAttribute("isReturnEnabled");
	String versioning = (String) request.getAttribute("isVersionControlled");
	boolean isVersionControlled = "1".equals(versioning);
	int nbEntriesAboutQuestions = (Integer) request.getAttribute("NbEntriesAboutQuestions");
	String currentRoleLabel = (String) request.getAttribute("currentRoleLabel");
  Replacement currentReplacement = (Replacement) request.getAttribute("currentReplacement");

	browseBar.setComponentName(componentLabel,"listProcess");

	String processId = "";
	if (isProcessIdVisible) {
    processId = "#" + process.getInstanceId() + " > ";
  }
	browseBar.setPath(processId+process.getTitle(currentRole, language));

	if (isPrintButtonEnabled) {
		operationPane.addOperation(resource.getIcon("processManager.print"),
			resource.getString("processManager.print"),
			"javascript:printProcess()");
	}
	tabbedPane.addTab(resource.getString("processManager.details"), "#", true, true);

	if ("supervisor".equalsIgnoreCase(currentRole)) {
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("processManager.reassign"),
				resource.getString("processManager.reassign"),
				"adminReAssign?processId="+process.getInstanceId());

		operationPane.addOperation(resource.getIcon("processManager.remove"),
				resource.getString("processManager.remove"),
				"adminRemoveProcess?processId="+process.getInstanceId());

		tabbedPane.addTab(resource.getString("processManager.history"), "viewHistory?processId=" + process.getInstanceId(), false, true);
		tabbedPane.addTab(resource.getString("processManager.errors"), "adminViewErrors?processId=" + process.getInstanceId(), false, true);
	} else {
		if (deleteAction != null && deleteAction.length > 0) {
			operationPane.addOperation(resource.getIcon("processManager.remove"), deleteAction[2],
          "editAction?state="+deleteAction[1]+"&action="+deleteAction[0]);
		}

		if (isAttachmentTabEnabled) {
      tabbedPane.addTab(resource.getString("processManager.attachments"),
          "attachmentManager?processId=" + process.getInstanceId(), false, true);
    }
		if (isReturnEnabled && nbEntriesAboutQuestions > 0) {
			tabbedPane.addTab(resource.getString("processManager.questions") + " ("+nbEntriesAboutQuestions+")", "listQuestions?processId=" + process.getInstanceId(), false, true);
		}
		if (isHistoryTabEnable) {
      tabbedPane.addTab(resource.getString("processManager.history"),
          "viewHistory?processId=" + process.getInstanceId(), false, true);
    }
	}

%>

<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.processmanager.CurrentState" %>
<%@ page import="org.silverpeas.core.util.CollectionUtil" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.workflow.api.user.Replacement" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.silverpeas.processmanager.LockVO" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<link type="text/css" rel="stylesheet" href='<c:url value="/processManager/jsp/styleSheets/print.css" />' media="print"/>
<view:includePlugin name="wysiwyg"/>
<script type="text/javascript">
function printProcess() {
    window.print();
}
</script>
</head>
<body class="yui-skin-sam currentProfile_<%=currentRole%> page_process">
<div id="<%=componentId%>">
<%
	out.println(window.printBefore());
	out.println(tabbedPane.print());
%>
<view:frame>

    <% if (currentReplacement != null) {
      List<String> labelParams = new ArrayList<String>();
      labelParams.add(currentReplacement.getIncumbent().getFullName());
      labelParams.add(currentRoleLabel);
      %>
      <div class="inlineMessage-neutral">
        <%=resource.getStringWithParams("processManager.replacements.process.replacement", labelParams.toArray(new String[0]))%>
      </div>
    <% } %>

			<% if (hasLockingUsers) {%>
		<div class="inlineMessage" id="actionsInProgress">
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
			<% } %>
			
		<div class="inlineMessage" id="activeStates">
			<p class="txtnav"><%=resource.getString("processManager.activeStates") %> </p>
					
					<% if (CollectionUtil.isEmpty(activeStates))  { %>
							<p class="textePetitBold">
							<%=resource.getString("processManager.noActiveState") %>
							</p>
          <% } else {
							for (CurrentState currentState : activeStates) {
							%>
                <p class="activeState">
                 <c:set var="currentState" value="<%=currentState%>"/>
								 <span class="textePetitBold">&#149;&nbsp;<%=currentState.getLabel()%></span>
								<% if (StringUtil.isDefined(currentState.getWorkingUsersAsString())) { %>
                  <span class="workingUsers">(<%=currentState.getWorkingUsersAsString()%>)</span>
								<% } %>
                <%
                   if (!process.getErrorStatus() && (!hasLockingUsers || isCurrentUserIsLockingUser)) {
                     Action[] actions = currentState.getActions();
                     for (Action action : actions) {
                       %>
                      <a href="<%="editAction?state=" + currentState.getName() + "&action=" +
                               action.getName() + "&processId=" + process.getInstanceId()%>" class="button"><span><%=action.getLabel(currentRole, language)%></span></a>
                   <%
                     }
                     if (isReturnEnabled) { %>
                      <c:forEach var="backStep" items="<%=currentState.getBackSteps()%>">
                        <c:set var="realUser" value="${backStep.user}"/>
                        <c:if test="${realUser != null}">
                          <a href="editQuestion?state=${currentState.name}&stepId=${backStep.id}" class="button">
                            <span>${backToLabel} ${realUser.fullName}</span>
                          </a>
                        </c:if>
                      </c:forEach>
                   <%}
                   }
                %>
                </p>
							<%
							}
						}
					%>
		</div>

    <% if (process.getErrorStatus()) { %>
      <div class="inlineMessage-nok">
          <%=resource.getString("processManager.ERR_PROCESS_IN_ERROR")%>
          <%=resource.getString("processManager.noTask")%>
      </div>
    <% } %>
<br/>

<table width="100%">
<tr><td>
<%
	context.setBorderPrinted(false);
  form.display(out, context, data);
  form.displayScripts(out, context);
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