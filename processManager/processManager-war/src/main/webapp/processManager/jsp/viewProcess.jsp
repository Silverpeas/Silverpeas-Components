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
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.processmanager.CurrentState" %>
<%@ page import="org.silverpeas.core.util.CollectionUtil" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.workflow.api.user.Replacement" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.silverpeas.processmanager.LockVO" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="tab" uri="http://www.silverpeas.com/tld/viewGenerator" %>

<%@ include file="checkProcessManager.jsp" %>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<jsp:useBean id="userLanguage" type="java.lang.String"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="isPrintButtonEnabled" value="${silfn:booleanValue(requestScope.isPrintButtonEnabled)}"/>
<c:set var="isCurrentRoleSupervisor" value="${'supervisor' eq fn:toLowerCase(currentRole)}"/>
<c:set var="process" value="${requestScope.process}"/>
<c:set var="backToLabel" value='<%=resource.getString("processManager.backTo")%>'/>
<c:set var="isProcessIdVisible" value="${silfn:booleanValue(requestScope.isProcessIdVisible)}"/>
<c:set var="isAttachmentTabEnabled" value="${silfn:booleanValue(requestScope.isAttachmentTabEnabled)}"/>
<c:set var="isReturnEnabled" value="${silfn:booleanValue(requestScope.isReturnEnabled)}"/>
<c:set var="isCurrentUserIsLockingUser" value="${silfn:booleanValue(requestScope.isCurrentUserIsLockingUser)}"/>
<c:set var="isHistoryTabEnabled" value="${silfn:booleanValue(requestScope.isHistoryTabEnabled)}"/>
<c:set var="locks" value="${requestScope.locks}"/>
<c:set var="hasLockingUsers" value="${silfn:booleanValue(not empty locks)}"/>

<c:set var="nbEntriesAboutQuestions" value="${requestScope.nbEntriesAboutQuestions}"/>
<c:set var="deleteAction" value="${requestScope.deleteAction}"/>

<fmt:message var="confirmDeleteMessage" key="processManager.confirmDelete"/>
<fmt:message var="reassignLabel" key="processManager.reassign"/>
<fmt:message var="deleteLabel" key="processManager.delete"/>
<fmt:message var="printLabel" key="processManager.print"/>

<fmt:message var="headerLabel" key="processManager.details"/>
<fmt:message var="historyLabel" key="processManager.history"/>
<fmt:message var="errorsLabel" key="processManager.errors"/>
<fmt:message var="attachmentsLabel" key="processManager.attachments"/>

<fmt:message var="actionInProgressLabel" key="processManager.actionInProgress"/>
<fmt:message var="youHaveAnActionToFinishLabel" key="processManager.youHaveAnActionToFinish"/>
<fmt:message var="instanceLockedByLabel" key="processManager.instanceLockedBy"/>
<fmt:message var="sinceLabel" key="processManager.since"/>
<fmt:message var="activeStatesLabel" key="processManager.activeStates"/>
<fmt:message var="noActiveStateLabel" key="processManager.noActiveState"/>

<fmt:message var="ERR_PROCESS_IN_ERROR" key="processManager.ERR_PROCESS_IN_ERROR"/>
<fmt:message var="noTaskLabel" key="processManager.noTask"/>

<%
  ProcessInstance process = (ProcessInstance) request.getAttribute("process");
  Form form = (Form) request.getAttribute("form");
  PagesContext context = (PagesContext) request.getAttribute("context");
  DataRecord data = (DataRecord) request.getAttribute("data");
  List<CurrentState> activeStates = (List<CurrentState>) request.getAttribute("activeStates");
  List<LockVO> locks = (List<LockVO>) request.getAttribute("locks");
  boolean hasLockingUsers = CollectionUtil.isNotEmpty(locks);
  boolean isCurrentUserIsLockingUser = (Boolean) request.getAttribute("isCurrentUserIsLockingUser");
  boolean isReturnEnabled = (Boolean) request.getAttribute("isReturnEnabled");
  String currentRoleLabel = (String) request.getAttribute("currentRoleLabel");
  Replacement currentReplacement = (Replacement) request.getAttribute("currentReplacement");

%>
<c:set var="processTitle" value="<%=process.getTitle(currentRole, language)%>"/>
<c:set var="processId" value="${process.instanceId}"/>
<c:set var="hasErrorStatus" value="${silfn:booleanValue(process.errorStatus)}"/>

<c:if test="${isProcessIdVisible}">
  <c:set var="path" value="# ${processId} > ${processTitle}"/>
</c:if>
<c:if test="${not isProcessIdVisible}">
  <c:set var="path" value="${processTitle}"/>
</c:if>

<view:sp-page>
  <view:sp-head-part>
    <link type="text/css" rel="stylesheet" href='<c:url value="/processManager/jsp/styleSheets/print.css" />' media="print"/>
    <view:includePlugin name="wysiwyg"/>
    <% form.displayScripts(out, context); %>
    <script type="text/javascript">
      function printProcess() {
        window.print();
      }
      function removeProcessInstance(id) {
        $('#removeProcessInstanceConfirmation').popup('confirmation', {
          //title : "Title of the popup",
          callback : function() {
            spProgressMessage.show();
            window.location.href = "adminRemoveProcess?processId=" + id;
            return true;
          }
        });
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="yui-skin-sam processManager-main currentProfile_${currentRole} page_process-detail">
    <view:operationPane>
      <c:if test="${isPrintButtonEnabled}">
        <fmt:message key="processManager.print" var="opIcon" bundle="${icons}"/>
        <c:url var="opIcon" value="${opIcon}"/>
        <view:operation action="javascript:printProcess()" altText="${printLabel}" icon="${opIcon}"/>
        <view:operationSeparator/>
      </c:if>

      <c:if test="${isCurrentRoleSupervisor}">
        <fmt:message key="processManager.reassign" var="opIcon" bundle="${icons}"/>
        <c:url var="opIcon" value="${opIcon}"/>
        <view:operation action="adminReAssign?processId=${processId}" altText="${reassignLabel}" icon="${opIcon}"/>
        <fmt:message key="processManager.remove" var="opIcon" bundle="${icons}"/>
        <c:url var="opIcon" value="${opIcon}"/>
        <view:operation action="javascript:removeProcessInstance(${processId})" altText="${deleteLabel}" icon="${opIcon}"/>

      </c:if>
      <c:if test="${not isCurrentRoleSupervisor && deleteAction ne null}">
        <fmt:message key="processManager.remove" var="opIcon" bundle="${icons}"/>
        <c:url var="opIcon" value="${opIcon}"/>
        <view:operation action="editAction?state=${deleteAction[1]}&action=${deleteAction[0]}" altText="${deleteAction[2]}" icon="${opIcon}"/>
      </c:if>
    </view:operationPane>

    <view:browseBar componentId="${componentId}" path="${path}" />

    <view:window>
      <view:tabs>
        <view:tab action="#" label="${headerLabel}" selected="true"/>
        <c:if test="${isAttachmentTabEnabled}">
          <view:tab action="attachmentManager?processId=${processId}" label="${attachmentsLabel}" selected="false"/>
        </c:if>

        <c:if test="${isReturnEnabled && nbEntriesAboutQuestions>0}">
          <c:set var="completeQuestionsLabel" value="${questionsLabel} (${nbEntriesAboutQuestions})"/>
          <view:tab action="listQuestions?processId=${processId}" label="${completeQuestionsLabel}" selected="false"/>
        </c:if>

        <c:if test="${isHistoryTabEnable}">
          <view:tab action="viewHistory?processId=${processId}" label="${historyLabel}" selected="false"/>
        </c:if>

        <c:if test="${isCurrentRoleSupervisor}">
          <view:tab action="adminViewErrors?processId=${processId}" label="${errorsLabel}" selected="false"/>
        </c:if>

      </view:tabs>
      <view:frame>

        <% if (currentReplacement != null) {
          List<String> labelParams = new ArrayList<>();
          labelParams.add(currentReplacement.getIncumbent().getFullName());
          labelParams.add(currentRoleLabel);
        %>
        <div class="inlineMessage-neutral">
          <%=resource.getStringWithParams("processManager.replacements.process.replacement", labelParams.toArray(new String[0]))%>
        </div>
        <% } %>

        <c:if test="${hasLockingUsers}">
          <div class="inlineMessage" id="actionsInProgress">
            <p class="txtnav">${actionInProgressLabel}</p>
            <c:choose>
              <c:when test="${isCurrentUserIsLockingUser}">
                <p class="textePetitBold">
                    ${youHaveAnActionToFinishLabel}
                </p>
              </c:when>
              <c:otherwise>
                <c:forEach items="${locks}" var="userlock">
                  <p class="textePetitBold">
                      ${instanceLockedByLabel} ${userlock.user.fullName}
                      ${sinceLabel} <fmt:formatDate value="${userlock.lockDate}" pattern="dd MMM yyyy"/>
                    <c:if test="${currentRole eq 'supervisor'}">
                      <c:if test="${userlock.removableBySupervisor}">
                        <c:url value="/util/icons/delete.gif" var="removeIconUrl" />
                        <a href="removeLock?processId=${processId}&stateName=${userlock.state}&userId=${userlock.user.userId}">
                          <img alt="" src="${removeIconUrl}"/>
                        </a>
                      </c:if>
                    </c:if>
                  </p>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </div>
          <br/>
        </c:if>

        <div class="inlineMessage" id="activeStates">
          <p class="txtnav">${activeStatesLabel}</p>

          <% if (CollectionUtil.isEmpty(activeStates))  { %>
            <p class="textePetitBold">
                ${noActiveStateLabel}
            </p>
          <% } else {
            for (CurrentState currentState : activeStates) {
          %>
          <p class="activeState">
            <c:set var="currentState" value="<%=currentState%>"/>
            <span class="textePetitBold">&#149;&nbsp;${currentState.label}</span>
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
                <c:forEach var="backStep" items="${currentState.backSteps}">
                  <c:set var="realUser" value="${backStep.user}"/>
                  <c:if test="${realUser != null}">
                    <a href="editQuestion?state=${currentState.name}&stepId=${backStep.id}" class="button">
                      <span>${backToLabel} ${realUser.fullName}</span>
                    </a>
                  </c:if>
                </c:forEach>
            <% }
             } %>
          </p>
          <%
              }
            }
          %>
        </div>

        <c:if test="${hasErrorStatus}">
          <div class="inlineMessage-nok">
            ${ERR_PROCESS_IN_ERROR}
            ${noTaskLabel}
          </div>
        </c:if>
        <br/>

        <table width="100%">
          <tr><td>
            <%
              context.setBorderPrinted(false);
              form.display(out, context, data);
            %>
          </td>
            <td>
              <c:import url="/attachment/jsp/displayAttachedFiles.jsp?Id=${process.instanceId}&ComponentId=${componentId}&Context=attachment"/>
            </td>
          </tr>
        </table>
        <script type="text/javascript">
          whenSilverpeasReady(function() {
          });
        </script>
      </view:frame>
    </view:window>
    <div id="removeProcessInstanceConfirmation" style="display: none">
        ${confirmDeleteMessage}
    </div>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>