<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>

<fmt:message key="GML.manageSubscriptions" var="actionLabelManageSubscriptions"/>
<fmt:message key="quickinfo.news.broadcast.mode.major" var="labelModeMajor"/>
<fmt:message key="GML.attachments" var="labelFiles"/>
<fmt:message key="GML.comments" var="labelComments"/>

<c:set var="listOfNews" value="${requestScope['ListOfNews']}"/>
<c:set var="allOtherNews" value="${requestScope['NotVisibleNews']}"/>
<c:set var="appSettings" value="${requestScope['AppSettings']}"/>
<c:set var="role" value="${requestScope['Role']}"/>
<c:set var="isSubscriberUser" value="${requestScope.IsSubscriberUser}"/>
<c:set var="contributor" value="${role == 'admin' || role == 'publisher'}"/>

<fmt:message var="deleteConfirmMsg" key="supprimerQIConfirmation"/>

<%@ include file="checkQuickInfo.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.quickinfo" xml:lang="${lang}">
<head>
<title>QuickInfo - Home</title>
<view:looknfeel/>
  <view:includePlugin name="toggle"/>
  <view:includePlugin name="subscription"/>
<script type="text/javascript" src="js/quickinfo.js"></script>
<script type="text/javascript">
function openPDCSetup() {
  var url = webContext+'/RpdcUtilization/jsp/Main?ComponentId=${componentId}';
  SP_openWindow(url, 'utilizationPdc1', '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

SUBSCRIPTION_PROMISE.then(function() {
  window.spSubManager = new SilverpeasSubscriptionManager('${componentId}');
});

function onDelete(id) {
  $("#news-"+id).remove();
}
</script>
</head>
<body class="quickInfo" id="${componentId}">
<view:browseBar />
<view:operationPane>
  <c:if test="${role == 'admin'}">
    <c:if test="${appSettings.taxonomyEnabled}">
      <fmt:message var="pdcMsg" key="GML.PDCParam"/>
      <view:operation altText="${pdcMsg}" action="javascript:onclick=openPDCSetup()"/>
    </c:if>
    <view:operation altText="${actionLabelManageSubscriptions}" action="ManageSubscriptions"/>
  </c:if>
  <c:if test="${contributor}">
	  <fmt:message var="addMsg" key="creation"/>
	  <c:url var="addIcon" value="/util/icons/create-action/add-news.png"/>
	  <view:operationOfCreation altText="${addMsg}" icon="${addIcon}" action="Add"></view:operationOfCreation>
  </c:if>
  <c:if test="${isSubscriberUser != null}">
    <view:operation altText="<span id='subscriptionMenuLabel'></span>" action="javascript:spSubManager.switchUserSubscription()"/>
  </c:if>
</view:operationPane>
<view:window>
	<view:componentInstanceIntro componentId="${componentId}" language="${lang}"/>

	<c:if test="${contributor || !appSettings.mosaicViewForUsers}">
		<!-- Dedicated part for contributors -->
		<c:if test="${contributor}">
    <div id="my-quickInfo">
      <div id="menubar-creation-actions"></div>
      <div class="secteur-container my-quickInfo-draft">
        <div class="header">
          <h3 class="my-quickInfo-draft-title"><fmt:message key="quickinfo.home.drafts"/></h3>
        </div>
        <ul>
          <c:if test="${empty allOtherNews.drafts}">
            <li><span class="txt-no-content"><fmt:message key="quickinfo.home.drafts.none"/></span></li>
          </c:if>
          <c:forEach items="${allOtherNews.drafts}" var="news">
            <li><a href="View?Id=${news.id}">${news.title}</a></li>
          </c:forEach>
        </ul>
      </div>
      <c:if test="${not empty allOtherNews.notYetVisibles}">
      <div class="secteur-container my-quickInfo-futur">
        <div class="header">
          <h3 class="my-quickInfo-futur-title"><fmt:message key="quickinfo.home.notYetVisibles"/></h3>
        </div>
        <ul>
          <c:forEach items="${allOtherNews.notYetVisibles}" var="news">
            <li><a href="View?Id=${news.id}"><span class="date">${silfn:formatAsLocalDate(news.visibility.period.startDate, lookHelper.zoneId, lookHelper.language)}</span>${news.title}</a></li>
          </c:forEach>
        </ul>
      </div>
      </c:if>
      <c:if test="${not empty allOtherNews.noMoreVisibles}">
      <div class="secteur-container my-quickInfo-outOfDate">
        <div class="header">
          <h3 class="my-quickInfo-outOfDate-title"><fmt:message key="quickinfo.home.noMoreVisibles"/></h3>
        </div>
        <ul>
          <c:forEach items="${allOtherNews.noMoreVisibles}" var="news">
            <li><a href="View?Id=${news.id}"><span class="date">${silfn:formatAsLocalDate(news.visibility.period.endDate, lookHelper.zoneId, lookHelper.language)}</span>${news.title}</a></li>
          </c:forEach>
        </ul>
      </div>
      </c:if>
    </div>
    </c:if>

    <c:if test="${empty listOfNews}">
      <div class="inlineMessage${contributor ? ' forContributor' : ''}">
        <fmt:message key="quickinfo.news.none"/><br/>
        <c:choose>
          <c:when test="${contributor}"><fmt:message key="quickinfo.news.none.contributor"/></c:when>
          <c:otherwise><fmt:message key="quickinfo.news.none.reader"/></c:otherwise>
        </c:choose>
      </div>
    </c:if>

    <ul id="list-news" class="${contributor ? '' : 'reader'}">
      <c:forEach items="${listOfNews}" var="news">
		  <li class="showActionsOnMouseOver" id="news-${news.id}">
			<c:if test="${not empty news.thumbnail}">
			  <view:image css="news-illustration" alt="" src="${news.thumbnail.URL}" size="200x"/>
			</c:if>
			<h3 class="news-title"><a href="View?Id=${news.id}">${news.title}</a></h3>
			<p class="news-teasing"><view:encodeHtmlParagraph string="${news.description}"/></p>
			<div class="news-info-fonctionality">
        <c:set var="_isSeparator" value="${false}" />
        <c:set var="nbComments" value="${news.numberOfComments}"/>
				<c:if test="${appSettings.commentsEnabled && nbComments > 0}">
          <c:set var="_isSeparator" value="${true}" />
					<a href="View?Id=${news.id}#commentaires" class="news-nb-comments"><img src="../../util/icons/talk2user.gif" alt="${labelComments}" /> ${nbComments}</a>
				</c:if>
        <c:if test="${news.numberOfAttachments > 0}">
          <c:set var="_isSeparator" value="${true}" />
          <span class="news-nb-attached-files">
            <img src="../../util/icons/attachedFiles.gif" alt="${labelFiles}"> ${news.numberOfAttachments}
          </span>
        </c:if>

        <c:if test="${_isSeparator}"><span class="sep"> | </span></c:if>
				<span class="creationInfo" ><fmt:message key="GML.publishedAt"/> ${silfn:formatDateAndHour(news.onlineDate, _language)} </span>
				<c:if test="${news.updatedAfterBePublished}">
					<span class="lastModificationInfo" >- <fmt:message key="GML.updatedAt"/> ${silfn:formatDate(news.updateDate, _language)} </span>
				</c:if>
				<span class="news-broadcast">
					<c:if test="${news.important}">
						<span class="news-broadcast-important" title="${labelModeMajor}">${labelModeMajor}</span>
					</c:if>
					<c:if test="${contributor}">
						<c:if test="${appSettings.broadcastingByBlockingNews && news.mandatory}">
							<span class="news-broadcast-blocking"><fmt:message key="quickinfo.news.broadcast.mode.blocking"/></span>
						</c:if>
						<c:if test="${appSettings.broadcastingByTicker && news.ticker}">
							<span class="news-broadcast-ticker"><fmt:message key="quickinfo.news.broadcast.mode.ticker"/></span>
						</c:if>
						<c:if test="${not empty news.delegatedNews}">
							<c:if test="${news.delegatedNews.waitingForValidation}">
								<span class="news-delegated" title="<fmt:message key="quickinfo.news.delegated.tovalidate.help"/>"><fmt:message key="quickinfo.news.delegated.tovalidate"/></span>
							</c:if>
							<c:if test="${news.delegatedNews.validated}">
								<span class="news-delegated" title="<fmt:message key="quickinfo.news.delegated.validated.help"/>"><fmt:message key="quickinfo.news.delegated.validated"/></span>
							</c:if>
							<c:if test="${news.delegatedNews.denied}">
								<span class="news-delegated" title="<fmt:message key="quickinfo.news.delegated.denied.help"/>"><fmt:message key="quickinfo.news.delegated.denied"/></span>
							</c:if>
						</c:if>
					</c:if>
				</span>
        <c:if test="${contributor}">
          <div class="operation actionShownOnMouseOver">
            <a title="<fmt:message key="GML.modify"/>" href="Edit?Id=${news.id}"><img border="0" title="<fmt:message key="GML.modify"/>" alt="<fmt:message key="GML.modify"/>" src="/silverpeas/util/icons/update.gif" /></a>
            <a title="<fmt:message key="GML.delete"/>" href="javascript:onclick=confirmDelete('${news.id}', '${news.componentInstanceId}', '${deleteConfirmMsg}', onDelete)"><img border="0" title="<fmt:message key="GML.delete"/>" alt="<fmt:message key="GML.delete"/>" src="/silverpeas/util/icons/delete.gif" /></a>
          </div>
        </c:if>
      </div>
      </li>
		</c:forEach>
	</ul>
</c:if>
<!-- end for contributors -->

<!-- list for users -->
<c:if test="${not contributor && appSettings.mosaicViewForUsers}">
  <c:if test="${empty listOfNews}">
    <div class="inlineMessage">
      <fmt:message key="quickinfo.news.none"/><br/>
      <fmt:message key="quickinfo.news.none.reader"/>
    </div>
  </c:if>

  <ul id="list-news-lecteur-view">
    <c:forEach items="${listOfNews}" var="news">
      <li onclick="location.href='View?Id=${news.id}'">
        <c:if test="${empty news.thumbnail}">
          <div class="visuel-container"><view:image css="news-illustration default-illustration" alt="" src="/quickinfo/jsp/icons/defaultThumbnail.jpg" size="400x"/></div>
        </c:if>
        <c:if test="${not empty news.thumbnail}">
          <div class="visuel-container"><view:image css="news-illustration" alt="" src="${news.thumbnail.URL}" size="400x"/></div>
        </c:if>
        <h3 class="news-title"><a href="View?Id=${news.id}">${news.title}</a></h3>
        <p class="news-teasing"><view:encodeHtmlParagraph string="${news.description}"/></p>
        <div class="creationInfo">${silfn:formatDate(news.onlineDate, _language)}</div>
        <c:if test="${news.important}">
          <div class="news-broadcast"> <span class="news-broadcast-important" title="${labelModeMajor}">${labelModeMajor}</span> </div>
        </c:if>
        <div class="news-nb-attached-files-and-comments">
          <c:set var="nbFiles" value="${news.numberOfAttachments}"/>
          <c:if test="${nbFiles > 0}">
            <div class="news-nb-attached-files"> <img src="../../util/icons/attachedFiles.gif" alt="${labelFiles}"> ${nbFiles} </div>
          </c:if>
          <c:set var="nbComments" value="${news.numberOfComments}"/>
          <c:if test="${appSettings.commentsEnabled && nbComments > 0}">
            <div class="news-nb-comments"> <img src="../../util/icons/talk2user.gif" alt="${labelComments}"> ${nbComments} </div>
          </c:if>
        </div>
      </li>
    </c:forEach>
  </ul>
</c:if>

  <!-- /INTEGRATION HOME quickInfo -->
</view:window>
<form name="newsForm" action="" method="post">
  <input type="hidden" name="Id"/>
</form>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.quickinfo', ['silverpeas.services', 'silverpeas.directives']);
</script>

</body>
</html>