<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%-- Set resource bundle --%>
<c:set var="_language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${_language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="componentLabel" value="${requestScope.browseContext[1]}"/>
<c:set var="instanceId" value="${requestScope.browseContext[3]}"/>
<c:set var="profile" value="${requestScope.Profile}"/>
<c:set var="orders" value="${requestScope.Orders}"/>
<c:set var="nbOrdersProcess" value="${requestScope.NbOrdersProcess}"/>

<html>
<head>
<view:looknfeel/>
</head>
<body id="${instanceId}" class="gallery gallery-orders">

<fmt:message var="orderLabel" key="gallery.viewOrderList" />
<view:browseBar path="${orderLabel}" />
<view:operationPane>
</view:operationPane>

<view:window>
<view:frame>

<c:if test="${profile eq 'admin'}">
<view:board>
  <table>
    <tr>
      <td class="txtlibform" nowrap><fmt:message key="gallery.nbOrders"/> :</td>
      <td>${fn:length(orders)}</td>
    </tr>
    <tr>
      <td class="txtlibform" nowrap><fmt:message key="gallery.nbOrdersProcess"/> :</td>
      <td>${nbOrdersProcess}</td>
    </tr>
    <tr>
      <td class="txtlibform" nowrap><fmt:message key="gallery.nbOrdersWait"/> :</td>
      <td>${fn:length(orders) - nbOrdersProcess}</td>
    </tr>
  </table>
</view:board>
</c:if>

<c:if test="${not empty orders }">
  <view:arrayPane var="orderList" routingAddress="OrderViewList">
    <fmt:message key="gallery.descriptionOrder" var="descCol" />
    <view:arrayColumn title="${descCol}" />
    <c:if test="${profile eq 'admin'}">
      <fmt:message key="gallery.orderOf" var="orderOfCol" />
      <view:arrayColumn title="${orderOfCol}" />
    </c:if>
    <fmt:message key="gallery.orderDate" var="orderDateCol" />
    <view:arrayColumn title="${orderDateCol}" />
    <fmt:message key="gallery.nbRows" var="nbRowsCol" />
    <view:arrayColumn title="${nbRowsCol}" sortable="false" />
    <fmt:message key="GML.status" var="statusCol" />
    <view:arrayColumn title="${statusCol}" />
    <c:forEach var="order" items="${orders}">
      <view:arrayLine>
        <c:set var="orderCellText"><a href="OrderView?OrderId=${order.orderId}">${order.orderId}</a></c:set>
        <view:arrayCellText text="${orderCellText}" />
        <c:if test="${profile eq 'admin'}">
          <c:set var="usernameCellText"><a href="OrderView?OrderId=${order.orderId}">${order.userName}</a></c:set>
          <view:arrayCellText text="${usernameCellText}" />
        </c:if>
        <c:set var="orderDate">
          <c:if test="${not empty order.creationDate}">
            <view:formatDateTime value="${order.creationDate}" language="${_language}" />
          </c:if>
        </c:set>
        <view:arrayCellText text="${orderDate}" />
        <view:arrayCellText text="${order.nbRows}" />

        <c:if test="${empty order.processDate}">
          <fmt:message key="gallery.wait" var="waitMsg" />
          <view:arrayCellText text="${waitMsg}" />
        </c:if>
        <c:if test="${not empty order.processDate}">
          <fmt:message key="gallery.processDate" var="processDateMsg" />
          <c:set var="processDate"><view:formatDateTime value="${order.processDate}" language="${_language}" /></c:set>
          <view:arrayCellText text="${processDateMsg} ${processDate}" />
        </c:if>
      </view:arrayLine>
    </c:forEach>
  </view:arrayPane>

</c:if>

</view:frame>
</view:window>
</body>
</html>