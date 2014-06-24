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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="news" value="${requestScope['News']}"/>
<c:set var="role" value="${requestScope['Role']}"/>
<c:set var="contributor" value="${role == 'admin' || role == 'publisher'}"/>
<c:set var="userId" value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="appSettings" value="${requestScope['AppSettings']}"/>
<c:set var="viewOnly" value="${requestScope['ViewOnly']}"/>

<c:url var="SilverpeasAnimationJS" value="/util/javaScript/animation.js"/>

<%@ include file="checkQuickInfo.jsp" %>
<%
pageContext.setAttribute("componentURL", URLManager.getFullApplicationURL(request)+URLManager.getURL("useless", componentId), PageContext.PAGE_SCOPE);
%>
<c:set var="componentURL" value="${pageScope.componentURL}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - View</title>
<view:looknfeel/>
<script type="text/javascript" src="${SilverpeasAnimationJS}"></script>
<script type="text/javascript" src="js/quickinfo.js"></script>
<script type="text/javascript">
function notify() {
  var url = "${componentURL}/Notify?Id=${news.id}";
  var windowName = "userPanelWindow";
  var windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
  SP_openWindow(url, windowName, "740", "600", windowParams, false);
}

function publish() {
  $("#newsForm").attr("action", "Publish");
  $("#newsForm").submit();
}

function update() {
  $("#newsForm").attr("action", "Edit");
  $("#newsForm").submit();
}

function submitOnHomepage() {
  $("#newsForm").attr("action", "SubmitOnHomepage");
  $("#newsForm").submit();
}
</script>
</head>
<body class="quickInfo actuality" id="${news.componentInstanceId}">
<c:if test="${viewOnly}">
<view:browseBar clickable="false"/>
</c:if>
<c:if test="${not viewOnly}">
<view:operationPane>
	<c:if test="${contributor}">
	  	<c:if test="${news.draft}">
			<fmt:message var="publishMsg" key="GML.publish"/>
		  	<view:operation altText="${publishMsg}" action="javascript:onclick=publish()"></view:operation>
		</c:if>
		<fmt:message var="updateMsg" key="GML.modify"/>
		<view:operation altText="${updateMsg}" action="javascript:onclick=update()"></view:operation>
		<fmt:message var="deleteMsg" key="GML.delete"/>
		<fmt:message var="deleteConfirmMsg" key="supprimerQIConfirmation"/>
		<view:operation altText="${deleteMsg}" action="javascript:onclick=confirmDelete('${news.id}', '${deleteConfirmMsg}')"/>
		<view:operationSeparator/>
		<c:if test="${appSettings.delegatedNewsEnabled && (empty news.delegatedNews || news.delegatedNews.denied)}">
			<fmt:message var="submitOnHomepage" key="quickinfo.news.delegated.operation"/>
			<view:operation altText="${submitOnHomepage}" action="javascript:onclick=submitOnHomepage()"/>
		</c:if>
	</c:if>
	<c:if test="${appSettings.notificationAllowed}">
		<fmt:message var="notifyMsg" key="GML.notify"/>
		<view:operation altText="${notifyMsg}" action="javascript:notify()"/>
	</c:if>
</view:operationPane>
</c:if>
<view:window>

<!--INTEGRATION  UNE ACTU -->
<div class="rightContent">
	<div id="illustration"><img alt="" src="${news.thumbnail.URL}" /></div>
	<div class="bgDegradeGris" id="actualityInfoPublication">
		<c:if test="${not news.draft}">
			<p id="statInfo">
				<b>${news.nbAccess} <fmt:message key="GML.stats.views"/></b>
			</p>
		</c:if>
		<c:if test="${appSettings.commentsEnabled}">
		<p id="commentInfo">
			<fmt:message key="GML.comment.number"/><br />
			<a href="#commentaires">${news.numberOfComments}</a>
		</p>
		</c:if>
									
		<p id="permalinkInfo">
			<a title="Pour copier le lien vers cette publication : Clique droit puis 'Copier le raccourci'" href="/silverpeas/Publication/18040"><img alt="Pour copier le lien vers cette publication : Clique droit puis 'Copier le raccourci'" src="/silverpeas/util/icons/link.gif" /></a> Permalien <br />
			<input type="text" value="${pageContext.request.scheme}://${header['host']}<c:url value="/Publication/${news.publicationId}"/>" onmouseup="return false" onfocus="select();" />
		</p>
	</div>
                          
    <viewTags:displayLastUserCRUD createDate="${news.createDate}" createdById="${news.createdBy}" updateDate="${news.updateDate}" updatedById="${news.updaterId}" publishDate="${news.publishDate}" publishedById="${news.publishedBy}"/>
    
    <view:pdcClassificationPreview componentId="${news.componentInstanceId}" contentId="${news.publicationId}" />
</div>

<div class="principalContent" >
	<c:if test="${contributor}">
		<c:if test="${not empty news.delegatedNews}">
			<c:choose>
				<c:when test="${news.delegatedNews.waitingForValidation}">
					<div class="inlineMessage"><fmt:message key="quickinfo.news.delegated.tovalidate.help"/></div>
				</c:when>
				<c:when test="${news.delegatedNews.validated}">
					<div class="inlineMessage-ok"><fmt:message key="quickinfo.news.delegated.validated.help"/></div>
				</c:when>
				<c:when test="${news.delegatedNews.denied}">
					<div class="inlineMessage-nok"><fmt:message key="quickinfo.news.delegated.refused.help"/></div>
				</c:when>
			</c:choose>
		</c:if>
		<c:if test="${news.draft}">
			<div class="inlineMessage"><fmt:message key="quickinfo.news.draft.info"/></div>
		</c:if>
	</c:if>
	<h2 class="actuality-title">${news.title}</h2>
	<c:if test="${not empty news.description}">
		<p class="publiDesc text2"><view:encodeHtmlParagraph string="${news.description}"/></p>
	</c:if>
    <div id="richContent">
		${news.content}
	</div>
    
    <c:if test="${appSettings.commentsEnabled}">
		<view:comments userId="${userId}" componentId="${news.componentInstanceId}" resourceType="<%=News.CONTRIBUTION_TYPE %>" resourceId="${news.publicationId}" indexed="true"/>
	</c:if>
</div>
<!-- /INTEGRATION UNE ACTU -->

<form name="newsForm" id="newsForm" action="" method="post">
<input type="hidden" name="Id" value="${news.id}"/>
</form>
</view:window>
</body>
</html>