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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="requests" value="${requestScope['Requests']}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
	<script type="text/javascript">
    function removeRequests() {
      if (window.confirm("<fmt:message key="formsOnline.requests.action.delete.confirm"/>")) {
        document.deleteForm.submit();
      }
    }
	</script>
</head>
<body>
<fmt:message var="browseBarAll" key="formsOnline.requests.all.breadcrumb"/>
<view:browseBar extraInformations="${browseBarAll}"/>
<view:operationPane>
  <fmt:message var="deleteReq" key="formsOnline.removeFormInstance"/>
  <view:operationOfCreation action="javascript:removeRequests()" icon="" altText="${deleteReq}"/>
</view:operationPane>
<view:window>
<form name="deleteForm" action="DeleteRequests">
  <view:arrayPane var="myForms" routingAddress="InBox" numberLinesPerPage="20">
    <fmt:message var="colStatus" key="GML.status"/>
    <fmt:message var="colDate" key="formsOnline.sendDate"/>
    <fmt:message var="colSender" key="formsOnline.sender"/>
    <fmt:message var="colForm" key="formsOnline.Form"/>
    <fmt:message var="colValidator" key="formsOnline.receiver"/>
    <fmt:message var="colOp" key="GML.operations"/>
    <view:arrayColumn title="${colStatus}"/>
    <view:arrayColumn title="${colDate}"/>
    <view:arrayColumn title="${colForm}"/>
    <view:arrayColumn title="${colSender}"/>
    <view:arrayColumn title="${colValidator}"/>
    <view:arrayColumn title="${colOp}"/>
    <c:forEach items="${requests.all}" var="request">
    <view:arrayLine>
      <c:choose>
        <c:when test="${request.read}">
          <fmt:message var="statusRead" key="formsOnline.stateRead"/>
          <view:arrayCellText text="${statusRead}"/>
        </c:when>
        <c:when test="${request.validated}">
          <fmt:message var="statusValidated" key="formsOnline.stateValidated"/>
          <view:arrayCellText text="${statusValidated}"/>
        </c:when>
        <c:when test="${request.denied}">
          <fmt:message var="statusDenied" key="formsOnline.stateRefused"/>
          <view:arrayCellText text="${statusDenied}"/>
        </c:when>
        <c:when test="${request.archived}">
          <fmt:message var="statusArchived" key="formsOnline.stateArchived"/>
          <view:arrayCellText text="${statusArchived}"/>
        </c:when>
        <c:otherwise>
          <fmt:message var="statusUnread" key="formsOnline.stateUnread"/>
          <view:arrayCellText text="${statusUnread}"/>
        </c:otherwise>
      </c:choose>
      <c:set var="creationDate" value="${silfn:formatDate(request.creationDate, lang)}"/>
      <view:arrayCellText text="<!-- ${request.creationDate} -->${creationDate}"/>
      <view:arrayCellText text="<a href=\"ViewRequest?Id=${request.id}&Origin=InBox\">${request.form.title}</a>"/>
      <view:arrayCellText text="${request.creator.displayedName}"/>
      <view:arrayCellText text="${request.validator.displayedName}"/>
      <c:set var="checkbox"><input type="checkbox" name="Id" value="${request.id}"/></c:set>
        <c:choose>
          <c:when test="${request.archived}">
            <view:arrayCellText text="${checkbox}"/>
          </c:when>
          <c:otherwise>
            <view:arrayCellText text=""/>
          </c:otherwise>
        </c:choose>
    </view:arrayLine>
    </c:forEach>
  </view:arrayPane>
</form>
</view:window>
</body>
</html>