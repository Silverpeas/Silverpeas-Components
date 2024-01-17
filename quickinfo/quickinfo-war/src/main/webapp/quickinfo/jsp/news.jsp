<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page import="org.silverpeas.core.admin.user.model.SilverpeasRole" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.notification.user.NotificationContext" %>
<%@ page import="org.silverpeas.core.web.selection.BasketSelectionUI" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="news" value="${requestScope['News']}"/>
<jsp:useBean id="news" type="org.silverpeas.components.quickinfo.model.News"/>
<c:set var="index" value="${requestScope['Index']}"/>
<c:set var="role" value="${requestScope['Role']}"/>
<jsp:useBean id="role" type="java.lang.String"/>
<c:set var="highestUserRole" value="<%=SilverpeasRole.fromString(role)%>"/>
<jsp:useBean id="highestUserRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:set var="contributor" value="${role == 'admin' || role == 'publisher'}"/>
<c:set var="userId" value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="appSettings" value="${requestScope['AppSettings']}"/>
<c:set var="viewOnly" value="${not empty requestScope['ViewOnly'] ? requestScope['ViewOnly'] : false}"/>
<jsp:useBean id="viewOnly" type="java.lang.Boolean"/>

<c:set var="extraPath" value=""/>
<c:choose>
  <c:when test="${news.draft}">
    <fmt:message key="quickinfo.home.drafts.breadcrumb" var="extraPath"/>
  </c:when>
  <c:when test="${news.notYetVisible}">
    <fmt:message key="quickinfo.home.notYetVisibles.breadcrumb" var="extraPath"/>
  </c:when>
  <c:when test="${news.noMoreVisible}">
    <fmt:message key="quickinfo.home.noMoreVisibles.breadcrumb" var="extraPath"/>
  </c:when>
</c:choose>

<%@ include file="checkQuickInfo.jsp" %>
<%
pageContext.setAttribute("componentURL", URLUtil.getFullApplicationURL(request)+
		URLUtil.getURL("useless", componentId), PageContext.PAGE_SCOPE);
%>
<c:set var="componentURL" value="${pageScope.componentURL}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>QuickInfo - View</title>
<view:looknfeel/>
<view:includePlugin name="basketSelection"/>
<script type="text/javascript" src="js/quickinfo.js"></script>
<script type="text/javascript">
function notify() {
	sp.messager.open('${news.componentInstanceId}', {<%= NotificationContext.CONTRIBUTION_ID %>: '${news.id}', <%= NotificationContext.PUBLICATION_ID %>: '${news.publicationId}'});
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

function onDelete(id) {
  location.href="Main";
}

function putNewsInBasket() {
  const basketManager = new BasketManager();
  basketManager.putContributionInBasket('${news.identifier.asString()}');
}
</script>
</head>
<body class="quickInfo actuality" id="${news.componentInstanceId}">
<view:browseBar extraInformations="${extraPath}"/>
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
		<view:operation altText="${deleteMsg}" action="javascript:onclick=confirmDelete('${news.id}', '${news.componentInstanceId}', '${deleteConfirmMsg}', onDelete)"/>
		<view:operationSeparator/>
		<c:if test="${appSettings.delegatedNewsEnabled && (not news.draft) && (empty news.delegatedNews || news.delegatedNews.denied)}">
			<fmt:message var="submitOnHomepage" key="quickinfo.news.delegated.operation"/>
			<view:operation altText="${submitOnHomepage}" action="javascript:onclick=submitOnHomepage()"/>
		</c:if>
	</c:if>
	<c:if test="${appSettings.notificationAllowed}">
		<fmt:message var="notifyMsg" key="GML.notify"/>
		<view:operation altText="${notifyMsg}" action="javascript:notify()"/>
	</c:if>
  <fmt:message var="printMsg" key="GML.print"/>
  <view:operation altText="${printMsg}" action="javascript:window.print()"/>
	<fmt:message var="putInSelectionBasketMsg" key="GML.putInBasket"/>
  <c:if test="${not news.draft}">
    <c:if test="<%=BasketSelectionUI.displayPutIntoBasketSelectionShortcut()%>">
      <view:operationSeparator/>
      <view:operation altText="${putInSelectionBasketMsg}" action="javascript:onclick=putNewsInBasket()"/>
    </c:if>
  </c:if>
</view:operationPane>
</c:if>
<view:window popup="${viewOnly}">

<!--INTEGRATION  UNE ACTU -->
<div class="rightContent">

  <c:if test="${not empty index && not viewOnly}">
    <viewTags:displayIndex nbItems="${index.nbItems}" index="${index.currentIndex}" />
  </c:if>

	<c:if test="${not empty news.thumbnail}">
		<div id="illustration"><view:image src="${news.thumbnail.URL}" alt="" size="350x"/></div>
	</c:if>

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

    <c:url var="permalink" value="/Publication/${news.publicationId}"/>
    <fmt:message var="permalinkHelp" key="quickinfo.news.permalink"/>
    <viewTags:displayPermalinkInfo link="${permalink}" help="${permalinkHelp}"/>
	</div>

  <%-- Attachments --%>
  <c:set var="callbackUrl"><%=
	URLUtil.getURL("useless", news.getComponentInstanceId()) + (viewOnly ? "ViewOnly" : "View") + "?Id=" + news.getId()%></c:set>
  <c:set var="highestUserRoleForAttachments" value="<%= SilverpeasRole.READER_ROLES.contains(highestUserRole) ? SilverpeasRole.USER : SilverpeasRole.ADMIN %>"/>
  <viewTags:displayAttachments componentInstanceId="${news.componentInstanceId}"
                               resourceId="${news.publicationId}"
                               resourceType="${news.publication.contributionType}"
                               highestUserRole="${highestUserRoleForAttachments}"
                               reloadCallbackUrl="${callbackUrl}"/>
                          
  <viewTags:displayLastUserCRUD createDate="${news.creationDate}" createdById="${news.creatorId}" updateDate="${news.updateDate}" updatedById="${news.updaterId}" publishDate="${news.onlineDate}" publishedById="${news.publishedBy}"/>
    
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
					<div class="inlineMessage-nok"><fmt:message key="quickinfo.news.delegated.denied.help"/></div>
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
    <div id="richContent" class="rich-content">
		${news.content}
    </div>

  <viewTags:viewAttachmentsAsContent componentInstanceId="${news.componentInstanceId}"
                                     resourceType="${news.publication.contributionType}"
                                     resourceId="${news.publicationId}"
                                     highestUserRole="${highestUserRoleForAttachments}"/>
    
    <c:if test="${appSettings.commentsEnabled && not viewOnly}">
		<view:comments userId="${userId}" componentId="${news.componentInstanceId}" resourceType="${news.contributionType}" resourceId="${news.id}" indexed="true"/>
	</c:if>
</div>
<!-- /INTEGRATION UNE ACTU -->

<form name="newsForm" id="newsForm" action="" method="post">
<input type="hidden" name="Id" value="${news.id}"/>
</form>
</view:window>
</body>
</html>
