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
<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<fmt:message key="GML.subscribe" var="actionLabelSubscribe"/>
<fmt:message key="GML.unsubscribe" var="actionLabelUnsubscribe"/>

<c:set var="listOfNews" value="${requestScope['ListOfNews']}"/>
<c:set var="appSettings" value="${requestScope['AppSettings']}"/>
<c:set var="role" value="${requestScope['Role']}"/>
<c:set var="isSubscriberUser" value="${requestScope.IsSubscriberUser}"/>
<fmt:message var="deleteConfirmMsg" key="supprimerQIConfirmation"/>

<%@ include file="checkQuickInfo.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - Home</title>
<view:looknfeel/>
<script type="text/javascript" src="js/quickinfo.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function openPDCSetup() {
  var url = webContext+'/RpdcUtilization/jsp/Main?ComponentId=${componentId}';
  SP_openWindow(url, 'utilizationPdc1', '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function successSubscribe() {
  // changing label and href of operation on-the-fly
  $("a[href='javascript:subscribe();']").first().attr('href', "javascript:unsubscribe();").text("${actionLabelUnsubscribe}");
}

function subscribe() {
  $.post('<c:url value="/services/subscribe/${componentId}" />', successSubscribe(), 'json');
}

function successUnsubscribe() {
  //changing label and href of operation on-the-fly
  $("a[href='javascript:unsubscribe();']").first().attr('href', "javascript:subscribe();").text("${actionLabelSubscribe}");
}

function unsubscribe() {
  $.post('<c:url value="/services/unsubscribe/${componentId}" />', successUnsubscribe(), 'json');
}
</script>
</head>
<body class="quickInfo" id="${componentId}">
<view:browseBar />
<view:operationPane>
  <c:if test="${role == 'admin' && appSettings.taxonomyEnabled}">
  	<fmt:message var="pdcMsg" key="GML.PDCParam"/>
  	<view:operation altText="${pdcMsg}" action="javascript:onclick=openPDCSetup()"/>
  </c:if>
  <c:if test="${role == 'admin' || role == 'publisher'}">
	  <fmt:message var="addMsg" key="creation"/>
	  <c:url var="addIcon" value="/util/icons/create-action/add-news.png"/>
	  <view:operationOfCreation altText="${addMsg}" icon="${addIcon}" action="Add"></view:operationOfCreation>
  </c:if>
  <c:if test="${isSubscriberUser != null}">
    <c:choose>
      <c:when test="${isSubscriberUser}">
        <view:operation altText="${actionLabelUnsubscribe}" action="javascript:unsubscribe();"/>
      </c:when>
      <c:otherwise>
        <view:operation altText="${actionLabelSubscribe}" action="javascript:subscribe();"/>
      </c:otherwise>
    </c:choose>
  </c:if>
</view:operationPane>
<view:window>
	<!--INTEGRATION HOME quickInfo -->
	<h2 class="quickInfo-title"><%=componentLabel %></h2>
	<c:if test="${not empty appSettings.description}">
		<p class="quickInfo-description">
		   ${appSettings.description}
		</p>
	</c:if>
	<div id="menubar-creation-actions"></div>
    <ul id="list-news">
    	<c:forEach items="${listOfNews}" var="news">
		<li>
			<c:if test="${not empty news.thumbnail}">
			  <img class="news-illustration" alt="" src="${news.thumbnail.URL}" />
			</c:if>
			<h3 class="news-title"><a href="View?Id=${news.id}">${news.title}</a></h3>
			<p class="news-teasing"><view:encodeHtmlParagraph string="${news.description}"/></p>
			<div class="news-info-fonctionality">
				<c:if test="${appSettings.commentsEnabled}">
					<a href="View?Id=${news.id}&Anchor=comments" class="news-nb-comments"><img src="/silverpeas/util/icons/talk2user.gif" alt="commentaire" /> ${news.numberOfComments}</a>
				</c:if> 
				<span class="sep"> | </span> <span class="creationInfo" ><fmt:message key="GML.createdAt"/> ${silfn:formatDateAndHour(news.creationDate, _language)} </span><span class="lastModificationInfo" >- <fmt:message key="GML.updatedAt"/> ${silfn:formatDate(news.updateDate, _language)} </span>
				<span class="news-broadcast">
					<c:if test="${news.important}">
						<span class="news-broadcast-important"><fmt:message key="quickinfo.news.broadcast.mode.major"/></span> 
					</c:if>
					<c:if test="${news.mandatory}">
						<span class="news-broadcast-blocking"><fmt:message key="quickinfo.news.broadcast.mode.blocking"/></span>
					</c:if>
					<c:if test="${news.ticker}">
						<span class="news-broadcast-ticker"><fmt:message key="quickinfo.news.broadcast.mode.ticker"/></span>
					</c:if>
				</span>
				<c:if test="${role == 'admin' || role == 'publisher'}">
					<div class="operation">
						<a title="<fmt:message key="GML.modify"/>" href="Edit?Id=${news.id}"><img border="0" title="<fmt:message key="GML.modify"/>" alt="<fmt:message key="GML.modify"/>" src="/silverpeas/util/icons/update.gif" /></a>
						<a title="<fmt:message key="GML.delete"/>" href="javascript:onclick=confirmDelete('${news.id}', '${deleteConfirmMsg}')"><img border="0" title="<fmt:message key="GML.delete"/>" alt="<fmt:message key="GML.delete"/>" src="/silverpeas/util/icons/delete.gif" /></a>
					</div>
				</c:if>
			</div>
		</li>
		</c:forEach>
	</ul>
    <!-- /INTEGRATION HOME quickInfo -->
</view:window>
<form name="newsForm" action="" method="post">
  <input type="hidden" name="Id"/>
</form>
</body>
</html>