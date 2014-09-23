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

<%@page import="com.stratelia.webactiv.util.DateUtil"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="org.silverpeas.components.quickinfo.model.News"%>
<%@page import="java.util.List"%>
<%@page import="javax.portlet.RenderRequest"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.silverpeas.portlets.FormNames" %>

<%@ page import="com.stratelia.webactiv.publication.model.PublicationDetail" %>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="appSettings" value="${requestScope['AppSettings']}"/>
<c:set var="fromApp" value="${not empty appSettings}"/>

<c:choose>
<c:when test="${fromApp}">
	<c:set var="allNews" value="${requestScope['infos']}"/>
	<c:set var="displayMode" value="list"/>
	<c:set var="slideshow" value="${displayMode == 'slideshow'}"/>
	<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<title>QuickInfo - Home</title>
	<view:looknfeel/>
	</head>
	<body id="quickinfo-app-portlet">
	<div class="portlet-content">
</c:when>
<c:otherwise>
	<portlet:defineObjects/>
	<c:set var="pRequest" value="${requestScope['javax.portlet.request']}"/>
	<c:set var="allNews" value="${pRequest.getAttribute('QuickInfos')}"/>
	<c:set var="displayMode" value="${pRequest.preferences.map['displayMode']}"/>
	<c:set var="slideshow" value="${displayMode[0] == 'slideshow'}"/>
	</c:otherwise>
</c:choose>



<c:if test="${slideshow}">
<script src="<%=m_sContext %>/portlets/jsp/quickInfos/js/responsiveslides.min.js" type="text/javascript"></script>
<link rel="stylesheet" href="<%=m_sContext %>/portlets/jsp/quickInfos/css/responsiveslides.css" />
<link rel="stylesheet" href="<%=m_sContext %>/portlets/jsp/quickInfos/css/themes.css" />
<script type="text/javascript" >
$(document).ready(function() {
	$(".slider-actuality-portlet").responsiveSlides({
			auto: true,
			pager: false,
			nav: true,
			speed: 500,
			pause: true,
			timeout: 6000,
			namespace: "centered-btns"
		});
});
</script>
	<div class="rslides">	
	<ul class="slider-actuality-portlet">
</c:if>
<c:if test="${not slideshow}">
	<ul class="listing-actuality-portlet">
</c:if>

	<c:forEach items="${allNews}" var="news">
			<c:choose>
				<c:when test="${not empty news.thumbnail}">
					<li>
					<div class="content-actuality-illustration"><view:image src="${news.thumbnail.URL}" alt="" size="350x" css="actuality-illustration"/></div>
				</c:when>
				<c:otherwise>
					<li class="actuality-without-illustration">
				</c:otherwise>
			</c:choose>
			<h3 class="actuality-title"><a href="${news.permalink}">${news.title}</a></h3>
			<div class="actuality-info-fonctionality">
				<span class="actuality-date">${silfn:formatDate(news.updateDate, _language)}</span>
				<a href="${news.permalink}#commentaires" class="actuality-nb-commentaires"><img src="/silverpeas/util/icons/talk2user.gif" alt="commentaire" /> ${news.numberOfComments}</a> 
			</div>
			<p class="actuality-teasing">${news.description}</p>
		</li>
	</c:forEach>

<c:if test="${slideshow}">
	</ul>
	</div>
</c:if>
<c:if test="${not slideshow}">
	</ul>
	<br clear="both" />
</c:if>

<c:if test="${fromApp}">
</div>
</body>
</html>
</c:if>