<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="checkProcessManager.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/workflowFunctions" prefix="workflowfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>


<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<jsp:useBean id="userLanguage" type="java.lang.String"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="manageReplacementLabel" key="processManager.replacements.manage"/>
<fmt:message var="addReplacementLabel" key="processManager.replacements.add"/>

<c:set var="jsUserRoles" value="${requestScope.jsUserRoles}"/>
<c:set var="jsComponentInstanceRoles" value="${requestScope.jsComponentInstanceRoles}"/>
<c:set var="currentRole" value="${requestScope.currentRole}"/>
<c:set var="currentRoleLabel" value="${requestScope.currentRoleLabel}"/>
<c:set var="isCurrentRoleSupervisor" value="${'supervisor' eq fn:toLowerCase(currentRole)}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <view:includePlugin name="datepicker"/>
  <view:includePlugin name="listOfUsersAndGroups"/>
  <view:script src="/processManager/jsp/javaScript/vuejs/replacement.service.js"/>
  <view:script src="/processManager/jsp/javaScript/vuejs/replacement.js"/>
  <script type="text/javascript">
    var componentInstanceRoles = ${jsComponentInstanceRoles};
    var userRoles = ${jsUserRoles};
    window.replacementHandledRoles = {};
    for (var roleName in componentInstanceRoles) {
      if ((${isCurrentRoleSupervisor} || userRoles[roleName]) && roleName !== 'supervisor') {
        window.replacementHandledRoles[roleName] = componentInstanceRoles[roleName];
      }
    }
  </script>
</head>
<body>
<view:progressMessage/>
<view:browseBar>
  <view:browseBarElt link="manageReplacements" label="${manageReplacementLabel}"/>
</view:browseBar>
<view:operationPane>
  <fmt:message key="processManager.replacements.add" var="opIcon" bundle="${icons}"/>
  <c:url var="opIcon" value="${opIcon}"/>
  <view:operationOfCreation altText="${addReplacementLabel}" action="javascript:vm.api.add()" icon="${opIcon}"/>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="replacement-module">
      <workflow-replacement-module v-on:api="api = $event">
        <template slot="header">
          <silverpeas-operation-creation-area></silverpeas-operation-creation-area>
        </template>
      </workflow-replacement-module>
    </div>
  </view:frame>
</view:window>
<script type="text/javascript">
  window.vm = new Vue({
    el : '#replacement-module',
    provide : function() {
      return {
        context: this.context,
        replacementService: new ReplacementService(this.context)
      }
    },
    data : {
      context : {
        currentUser : extendsObject({
          role : '${currentRole}',
          roleLabel : componentInstanceRoles['${currentRole}'].label,
          isSupervisor : ${isCurrentRoleSupervisor}
        }, currentUser),
        componentInstanceId : '${componentId}',
        replacementHandledRoles : replacementHandledRoles
      },
      api : undefined
    }
  });
</script>
</body>
</html>