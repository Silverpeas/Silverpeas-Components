<%--
  Copyright (C) 2000 - 2021 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Attributes --%>
<%@ attribute name="validation" required="true"
              type="org.silverpeas.components.formsonline.model.FormInstanceValidation"
              description="A validation" %>

<%@ attribute name="nextStep" required="true"
              type="java.lang.Boolean"
              description="Is the step is after the pending one ?" %>

<fmt:message var="labelStep" key="formsOnline.validation.finale"/>
<c:choose>
  <c:when test="${validation.validationType.hierarchical}">
    <fmt:message var="labelStep" key="formsOnline.validation.boss"/>
  </c:when>
  <c:when test="${validation.validationType.intermediate}">
    <fmt:message var="labelStep" key="formsOnline.validation.inter"/>
  </c:when>
</c:choose>

<c:set var="decisionClass" value="inlineMessage"/>
<c:set var="decisionLabel" value=""/>
<c:choose>
  <c:when test="${validation.validated}">
    <c:set var="decisionClass" value="inlineMessage-ok"/>
    <fmt:message var="decisionLabel" key="formsOnline.stateValidated"/>
  </c:when>
  <c:when test="${validation.refused}">
    <c:set var="decisionClass" value="inlineMessage-nok"/>
    <fmt:message var="decisionLabel" key="formsOnline.stateRefused"/>
  </c:when>
</c:choose>

<c:set var="nextStepClass" value=""/>
<c:if test="${nextStep}">
  <c:set var="nextStepClass" value="next-step"/>
</c:if>

<li class="step-OnlineForm ${validation.validationType.name()} ${validation.status.name()} ${nextStepClass}">
  <div class="header-step-onlineForm">

      <c:choose>
        <c:when test="${not empty validation.validator}">
        <div class="validator avatar">
          <view:image src="${validation.validator.avatar}" alt="" type="avatar" />
        </div>
        </c:when>
        <c:otherwise>
          <div class="validator">
            <img alt="" src="${silfn:applicationURL()}/formsOnline/jsp/icons/multiple-users.png" />
          </div>
        </c:otherwise>
      </c:choose>
    <div class="title-step-OnlineForm">${labelStep}</div>
    <div class="date-step-OnlineForm">
      <c:choose>
        <c:when test="${not empty validation.date}">
          <fmt:message key="GML.date.the"/> <view:formatDateTime value="${validation.date}"/>
        </c:when>
        <c:otherwise>
          <fmt:message key="formsOnline.home.requests.mine.toValidate"/>
        </c:otherwise>
      </c:choose>
    </div>
    <div class="actor-step-OnlineForm">
      <c:if test="${not empty validation.validator}">
        ${decisionLabel} <fmt:message key="GML.by"/> <view:username user="${validation.validator}"/>
      </c:if>
    </div>
  </div>
  <div class="comment-step-OnlineForm ${decisionClass}"><div>${silfn:escapeHtmlWhitespaces(validation.comment)}</div></div>
</li>