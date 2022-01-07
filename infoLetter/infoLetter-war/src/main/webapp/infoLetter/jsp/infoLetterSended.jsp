<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="templateSend" value="${silfn:booleanValue(requestScope.TemplateSend)}"/>
<c:set var="emailErrors" value="${requestScope.EmailErrors}"/>
<jsp:useBean id="emailErrors" type="java.lang.String[]"/>
<c:set var="returnUrl" value="${requestScope.ReturnUrl}"/>
<c:if test="${empty returnUrl}">
  <c:set var="returnUrl" value="Accueil"/>
</c:if>

<fmt:message key="infoLetter.emailErrors" var="emailErrorsMsg"/>
<fmt:message key="GML.ok" var="okLabel"/>

<view:sp-page>
  <view:sp-head-part>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:window>
      <view:frame>
        <div class="inlineMessage">
          <fmt:message var="messageSent" key="${templateSend ? 'infoLetter.templateSended' : 'infoLetter.sended'}"/>
          <c:out value="${messageSent}"/>
        </div>
        <c:if test="${fn:length(emailErrors) > 0}">
          <div class="inlineMessage-nok">
              ${silfn:escapeHtml(emailErrorsMsg)}
            <ul>
              <c:forEach var="email" items="${emailErrors}">
                <li>${silfn:escapeHtml(email)}</li>
              </c:forEach>
            </ul>
          </div>
        </c:if>
        <view:buttonPane>
          <view:button label="${okLabel}" action="${returnUrl}"/>
        </view:buttonPane>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>