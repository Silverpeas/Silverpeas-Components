<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_MailingList_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="mailingList.icons.attachmentSmall" var="attachmentIcon" bundle="${icons}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title><fmt:message key="mailingList.tab.activity.title" /></title>
<view:looknfeel />
</head>
<body>
<fmt:message key="mailingList.tab.list.title" var="listTabTitle" />
<fmt:message key="mailingList.tab.activity.title" var="activityTabTitle" />
<view:window>
  <c:url var="listAction" value="/Rmailinglist/${componentId}/list/${componentId}" />
  <c:set var="currentList" value="${requestScope.currentList}" />
  <view:frame>
    <center>   
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
    </center>
  </view:frame>
</view:window>
</body>
</html>