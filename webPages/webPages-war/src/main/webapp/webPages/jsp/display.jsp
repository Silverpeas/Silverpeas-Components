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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="check.jsp" %>
<%@page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN"/>

<fmt:message key="webPages.edit" var="editLabel"/>
<fmt:message key="GML.manageSubscriptions" var="manageSubscriptionLabel"/>
<fmt:message key="GML.notify" var="notifyLabel"/>
<fmt:message key="GML.print" var="printLabel"/>
<fmt:message key="webPages.emptyPage" var="emptyPageMessage"/>

<fmt:message key="webPages.underConstruction" var="underConstructionPath" bundle="${icons}"/>
<c:url var="underConstructionPath" value="${underConstructionPath}"/>

<c:set var="action" value="${requestScope.Action}"/>
<c:if test="${empty action}">
  <c:set var="action" value="Display"/>
</c:if>
<c:set var="subscriptionEnabled" value="${requestScope.SubscriptionEnabled}"/>
<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>
<c:set var="haveGotContent" value="${requestScope.haveGotContent}"/>
<c:set var="isAnonymous" value="${requestScope.AnonymousAccess}"/>
<c:set var="operationsVisibles" value="${not (action eq 'Portlet') and not isAnonymous}"/>
<jsp:useBean id="operationsVisibles" type="java.lang.Boolean"/>

<%
  Form form = (Form) request.getAttribute("Form");
  Form otherForm = (Form) request.getAttribute("OtherForm");
  PagesContext context = new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, "useless");
  context.setObjectId("0");
  context.setBorderPrinted(false);
%>

<view:sp-page angularJsAppName="silverpeas.webPage">
  <view:sp-head-part>
    <link type="text/css" rel="stylesheet" href="styleSheets/webPages-print.css" media="print"/>
    <view:includePlugin name="popup"/>
    <view:includePlugin name="preview"/>
    <view:includePlugin name="toggle"/>
    <view:includePlugin name="subscription"/>
    <script type="text/javascript">
      SUBSCRIPTION_PROMISE.then(function() {
        window.spSubManager = new SilverpeasSubscriptionManager('<%=componentId%>');
      });

      function toNotify() {
        sp.messager.open('<%= componentId %>');
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <c:if test="${operationsVisibles}">
      <view:operationPane>
        <c:if test="${action eq 'Preview'}">
          <view:operation action="Edit" altText="${editLabel}"/>
          <c:if test="${subscriptionEnabled and highestUserRole.isGreaterThanOrEquals(adminRole)}">
            <view:operation action="ManageSubscriptions" altText="${manageSubscriptionLabel}"/>
          </c:if>
          <view:operationSeparator/>
        </c:if>
        <c:if test="${subscriptionEnabled}">
          <view:operation action="javascript:spSubManager.switchUserSubscription()" altText="<span id='subscriptionMenuLabel'></span>"/>
        </c:if>
        <view:operation action="javascript:toNotify()" altText="${notifyLabel}"/>
        <view:operation action="javascript:print()" altText="${printLabel}"/>
      </view:operationPane>
    </c:if>
    <%
      if (operationsVisibles) {
        out.println(window.printBefore());
      }
    %>
    <view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resource.getLanguage()%>"/>
    <div id="richContent">
      <c:choose>
        <c:when test="${haveGotContent}">
          <%
            if (form != null) {
              form.display(out, context);
              if (otherForm != null) {
                otherForm.display(out, context);
              }
            } else {
          %>
          <div class="rich-content">
          <view:displayWysiwyg objectId="<%=componentId%>" componentId="<%=componentId %>" language="<%=I18NHelper.DEFAULT_LANGUAGE %>"/>
          </div>
          <%
            }
          %>
        </c:when>
        <c:otherwise>
          <img src="${underConstructionPath}" alt=""/>
          <span class="txtnav">${emptyPageMessage}</span>
          <img src="${underConstructionPath}" alt=""/>
        </c:otherwise>
      </c:choose>
    </div>
    <%
      if (operationsVisibles) {
        out.println(window.printAfter());
      }
    %>
  </view:sp-body-part>
</view:sp-page>
