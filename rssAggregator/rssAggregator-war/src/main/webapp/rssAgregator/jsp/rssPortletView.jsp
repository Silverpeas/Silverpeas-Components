<%--

    Copyright (C) 2000 - 2020 Silverpeas

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

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="_userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_userLanguage}"/>

<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="rssItems" value="${requestScope.items}"/>
<c:set var="rssChannels" value="${requestScope.allChannels}"/>

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript">
$(document).ready(function(){
	$('.description-item-rssNews a').attr( "target" , "_blank"  ) ;

	$('.deploy-item').click(function() {
		$(this).parent().children('.itemDeploy').toggle();
	});

<c:forEach var="channel" items="${rssChannels}">
  <c:if test="${channel.displayImage == 0}">$(".channel_${channel.PK.id} .img-item-rssNews").hide();</c:if>
</c:forEach>

});
</script>
</head>
<body class="rssAgregator portlet">
<view:window>
<c:if test="${not empty rssItems}">
  <div id="rssNews">

	  <ul>

  <c:forEach var="item" items="${rssItems}">
    <jsp:useBean id="item" type="org.silverpeas.components.rssaggregator.model.RSSItem"/>
    <c:set var="channelName" value="${item.channelTitle}"/>

		<c:if test="${fn:length(channelName) gt 40}">
		  <c:set var="channelName" value="${fn:substring(item.channelTitle, 0, 39)}..." />
		</c:if>


		<!-- Commentaire AuroreA. Sur le li mettre la classe channel_$idDuChannel -->
		<li class="item-channel channel_${item.channelId} ">

			<a class="deploy-item" title="deploy item" href="#" onclick = "return false;" ><img class="deploy-item-rssNews" src="${ctxPath}<fmt:message key="rss.openChannel" bundle="${icons}"/>" border="0" /></a>
			<a class="deploy-item itemDeploy" title="bend item" href="#" onclick = "return false;" ><img class="deploy-item-rssNews" src="${ctxPath}<fmt:message key="rss.closeChannel" bundle="${icons}"/>" border="0" /></a>
			<h3 class="title-item-rssNews">
				<c:if test="${not empty item.channelImage}">
				<a href="${item.channelImage.link }" target="_blank"><img class="img-item-rssNews" src="${item.channelImage.url}" border="0" /></a>
				</c:if>
				<span  class="channelName-rssNews"><c:out value="${channelName}"/> </span>
				<a href="<c:out value="${item.itemLink}"/>" target="_blank"><c:out value="${item.itemTitle}"/> </a>
			</h3>
			<div class="lastUpdate-item-rssNews"><c:out value="${silfn:formatDateAndHour(item.itemDate, _userLanguage)}"/></div>
			<div class="itemDeploy" >
				<div class="description-item-rssNews"><c:out value="${item.itemDescription}" escapeXml="false"/></div>
				<br class="clear"/>
			</div>
		</li>

  </c:forEach>
	  </ul>
  </div>
</c:if>

</view:window>

<view:progressMessage/>

</body>
</html>