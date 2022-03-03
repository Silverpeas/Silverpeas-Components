<%--

    Copyright (C) 2000 - 2022 Silverpeas

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

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/quickinfo" prefix="quickInfoTags" %>

<c:set var="appSettings" value="${requestScope['AppSettings']}"/>
<c:set var="fromApp" value="${not empty appSettings}"/>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="org.silverpeas.quickinfo.multilang.quickinfo" />


<c:choose>
<c:when test="${fromApp}">
	<c:set var="allNews" value="${requestScope['infos']}"/>
	<c:set var="displayMode" value="list"/>
	<c:set var="slideshow" value="${displayMode == 'slideshow'}"/>
	<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
	<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="${userLanguage}">
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
  <c:forEach items="${allNews}" var="news">
    <quickInfoTags:portletNews news="${news}" userLanguage="${userLanguage}" slideshow="${slideshow}" fromApp="${fromApp}" />
  </c:forEach>
</ul>
</div>
</c:if>

<c:if test="${not slideshow}">
<script type="text/javascript" >
  $(document).ready(function() {
    const step = 5;
    const $lis = $(".listing-actuality-portlet li");
    const $pag = $(".listing-actuality-portlet .list-pane-nav");
    const nbNews = $lis.length;
    if (nbNews <= step) {
      $('#portlet-nextNews').hide();
    } else {
      $lis.hide();
      $pag.hide();
      $lis.slice(0, step).show();
      let end = step;
      $('#portlet-nextNews').click(function() {
        <c:choose>
        <c:when test="${fromApp}">
        $lis.show();
        $pag.show();
        $('#portlet-nextNews').hide();
        </c:when>
        <c:otherwise>
        end += step;
        $lis.slice(0, end).show();
        $pag.show();
        if (end >= $lis.length) {
          $('#portlet-nextNews').hide();
        }
        </c:otherwise>
        </c:choose>
        return false;
      });
    }
  });
</script>
  <ul class="listing-actuality-portlet">
    <c:choose>
      <c:when test="${fromApp}">
        <view:listPane var="listOfNewsFromPortlet" routingAddress="portletPagination" numberLinesPerPage="10">
          <view:listItems items="${allNews}" var="news">
            <quickInfoTags:portletNews news="${news}" userLanguage="${userLanguage}" slideshow="${slideshow}" fromApp="${fromApp}"/>
          </view:listItems>
        </view:listPane>
        <script type="text/javascript">
          whenSilverpeasReady(function() {
            sp.listPane.ajaxControls('.listing-actuality-portlet');
          });
        </script>
      </c:when>
      <c:otherwise>
        <c:forEach items="${allNews}" var="news">
          <quickInfoTags:portletNews news="${news}" userLanguage="${userLanguage}" slideshow="${slideshow}" fromApp="${fromApp}"/>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </ul>
  <br clear="both"/>
  <a class="linkMore" href="#" id="portlet-nextNews"><span><fmt:message key="quickinfo.portlet.news.more"/></span></a>
</c:if>

<c:if test="${fromApp}">
</div>
</body>
</html>
</c:if>