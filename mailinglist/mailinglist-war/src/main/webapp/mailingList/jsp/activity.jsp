<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@ include file="check.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_MailingList_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="mailingList.icons.attachmentSmall" var="attachmentIcon" bundle="${icons}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title><fmt:message key="mailingList.tab.activity.title" /></title>
<c:if test="${requestScope['mailinglistRss'] != null}">
  <link rel="alternate" type="application/rss+xml" title="<c:out value="${requestScope.currentList.description}:${requestScope['currentList'].subscribedAddress}" />"
    href="<c:url value="${requestScope['mailinglistRss']}" />" />
</c:if>
<script type="text/javascript">
function subscribe(){
    document.subscribe.method='GET';
    document.subscribe.action='<c:url value="/Rmailinglist/${componentId}/subscription/${componentId}" />';
    document.subscribe.submit();
}
function unsubscribe(){
    document.subscribe.action='<c:url value="/Rmailinglist/${componentId}/subscription/delete" />';
    document.subscribe.submit();
}
</script>
<view:looknfeel />
</head>
<body>
<fmt:message key="mailingList.tab.list.title" var="listTabTitle" />
<fmt:message key="mailingList.tab.activity.title" var="activityTabTitle" />
<c:url var="browseBarLink" value="Main" />
<view:browseBar>
  <view:browseBarElt link="Main" label="${activityTabTitle}" />
</view:browseBar>
<c:if test="${!requestScope.currentUserIsAdmin &&  !requestScope.currentUserIsModerator}">
  <form name="subscribe" id="subscribe" method="POST" /><c:choose>
    <c:when test="${requestScope.currentUserIsSubscriber}">
      <fmt:message key="mailingList.icons.unsubscribe.alt" var="unsubscribeAlt" />
      <fmt:message key="mailingList.icons.unsubscribe" var="unsubscribeIconPath" bundle="${icons}" />
      <view:operationPane>
        <view:operation altText="${unsubscribeAlt}" icon="${unsubscribeIconPath}" action="javascript: unsubscribe();" />
      </view:operationPane>
    </c:when>
    <c:otherwise>
      <fmt:message key="mailingList.icons.subscribe.alt" var="subscribeAlt" />
      <fmt:message key="mailingList.icons.subscribe" var="subscribeIconPath" bundle="${icons}" />
      <view:operationPane>
        <view:operation altText="${subscribeAlt}" icon="${subscribeIconPath}" action="javascript: subscribe();" />
      </view:operationPane>
    </c:otherwise>
  </c:choose>
</c:if>
<view:window>
  <c:url var="listAction" value="/Rmailinglist/${componentId}/list/${componentId}" />
  <view:tabs>
    <view:tab label="${activityTabTitle}" action="${'#'}" selected="true" />
    <view:tab label="${listTabTitle}" action="${listAction}" selected="false" />
    <c:if test="${(requestScope.currentUserIsAdmin || requestScope.currentUserIsModerator) && requestScope.currentListIsModerated}">
      <fmt:message key="mailingList.tab.moderation.title" var="moderationTabTitle" />
      <c:url var="moderationAction" value="/Rmailinglist/${componentId}/moderationList/${componentId}" />
      <view:tab label="${moderationTabTitle}" action="${moderationAction}" selected="false" />
    </c:if>
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.users.title" var="usersTabTitle" />
      <c:url var="usersAction" value="/Rmailinglist/${componentId}/users/${componentId}" />
      <view:tab label="${usersTabTitle}" action="${usersAction}" selected="false" />
    </c:if>
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.subscribers.title" var="subscribersTabTitle" />
      <c:url var="subscriberssAction" value="/Rmailinglist/${componentId}/subscription/${componentId}/subscription/put" />
      <view:tab label="${subscribersTabTitle}" action="${subscriberssAction}" selected="false" />
    </c:if>
  </view:tabs>
  <c:set var="currentList" value="${requestScope.currentList}" />
  <view:frame>
    <center>
    <table id="description" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
      <tr align="left">
        <td class="ArrayColumn"><fmt:message key="mailingList.activity.description.title" /></td>
      </tr>
      <c:if test="${currentList.description != ''}">
        <tr align="left">
          <td class="ArrayCell"><c:out value="${currentList.description}" /></td>
        </tr>
      </c:if>
      <tr align="left">
        <td class="ArrayCell"><c:choose>
          <c:when test="${currentList.moderated}">
            <fmt:message key="mailingList.activity.description.moderated" />
          </c:when>
          <c:otherwise>
            <fmt:message key="mailingList.activity.description.not.moderated" />
          </c:otherwise>
        </c:choose> <c:choose>
          <c:when test="${currentList.open}">
            <fmt:message key="mailingList.activity.description.open" />
          </c:when>
          <c:otherwise>
            <fmt:message key="mailingList.activity.description.not.open" />
          </c:otherwise>
        </c:choose> <c:choose>
          <c:when test="${currentList.notify}">
            <fmt:message key="mailingList.activity.description.notify" />
          </c:when>
          <c:otherwise>
            <fmt:message key="mailingList.activity.description.not.notify" />
          </c:otherwise>
        </c:choose></td>
      </tr>
      <c:if test="${not empty currentList.moderators  && currentList.moderated}">
        <tr align="left">
          <td class="ArrayCell"><fmt:message key="mailingList.activity.description.moderators" /> : <c:forEach items="${currentList.moderators}" var="moderator" varStatus="lineInfo">
            <c:out value="${moderator.name}" />
            <c:if test="${!lineInfo.last}">,&nbsp;</c:if>
          </c:forEach></td>
        </tr>
      </c:if>
    </table>
    <br />
    <c:set var="messages" value="${requestScope.currentListActivity.messages}" />
    <table id="messages" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
      <tr align="left">
        <td class="ArrayColumn"><fmt:message key="mailingList.activity.recentMessages.title" /></td>
      </tr>
      <c:forEach var="message" items="${messages}" varStatus="messageIndex">
        <c:set var="ArrayCell" value="peer" />
        <tr align="left">
          <td width="75%"><b><a href="<c:url value="destination/activity/message/${message.id}"/>"> <c:out value="${message.title}" /></a></b> <c:choose>
            <c:when test="${message.attachmentsSize <= 0}">&nbsp;</c:when>
            <c:otherwise>
              <img src="<c:url value="${attachmentIcon}" />" />
            </c:otherwise>
          </c:choose></td>
        </tr>
        <tr>
          <td class="txtBaseline"><c:out value="${message.sender}" /> - <fmt:formatDate value="${message.sentDate}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
        </tr>
        <tr>
          <td><c:out value="${message.summary}" /> <a href="destination/activity/message/<c:out value="${message.id}"/>">...</a></td>
        </tr>
      </c:forEach>
    </table>
    <br />
    <table id="activities" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
      <tr align="left">
        <td class="ArrayColumn" colspan="13"><fmt:message key="mailingList.activity.messageHistory.title" /></td>
      </tr>
      <tr align="center">
        <td valign="top" align="center" class="ArrayCell">&nbsp;</td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois0" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois1" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois2" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois3" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois4" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois5" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois6" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois7" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois8" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois9" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois10" /></td>
        <td valign="top" align="center" class="ArrayColumn"><fmt:message key="GML.mois11" /></td>
      </tr>
      <c:set var="map" value="${requestScope['currentActivityMap']}" />
      <c:forEach var="year" items="${requestScope['currentYears']}">
        <c:set var="yearActivities" value="${map[year]}" />
        <tr align="center">
          <td valign="top" align="center" class="ArrayColumn"><a href="<c:url value="list/${componentId}/currentYear/${year}" />"><c:out value="${year}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/0" />"><c:out value="${yearActivities['0']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/1" />"><c:out value="${yearActivities['1']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/2" />"><c:out value="${yearActivities['2']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/3" />"><c:out value="${yearActivities['3']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/4" />"><c:out value="${yearActivities['4']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/5" />"><c:out value="${yearActivities['5']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/6" />"><c:out value="${yearActivities['6']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/7" />"><c:out value="${yearActivities['7']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/8" />"><c:out value="${yearActivities['8']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/9" />"><c:out value="${yearActivities['9']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/10" />"><c:out value="${yearActivities['10']}" /></a></td>
          <td valign="top" align="center" class="ArrayCell"><a href="<c:url value="list/${componentId}/currentYear/${year}/currentMonth/11" />"><c:out value="${yearActivities['11']}" /></a></td>
        </tr>
      </c:forEach>
    </table>
    <br />
    <table id="subscribedAddress" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
      <tr>
        <td class="ArrayColumn"><fmt:message key="mailingList.activity.email.title" /></td>
      </tr>
      <tr>
        <td class="ArrayCell"><a href="<c:out value="list/${componentId}"/>"><c:out value="${requestScope['currentList'].subscribedAddress}" /></a></td>
      </tr>
    </table>

    <c:if test="${requestScope['mailinglistRss'] != null}">
      <br />
      <a href="<c:url value="${requestScope.mailinglistRss}" />"><img src="<c:url value="/util/icons/rss.gif" />" border="0" alt="rss"/></a>
    </c:if></center>
  </view:frame>
</view:window>
</body>
</html>