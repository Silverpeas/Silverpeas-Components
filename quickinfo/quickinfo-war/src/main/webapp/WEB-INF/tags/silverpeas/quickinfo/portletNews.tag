<%--
  Copyright (C) 2000 - 2022 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="news" required="true" type="org.silverpeas.components.quickinfo.model.News"
              description="A news" %>

<%@ attribute name="userLanguage" required="true" type="java.lang.String"
              description="User language" %>

<%@ attribute name="slideshow" required="true" type="java.lang.Boolean"
              description="Enable slide show mode" %>

<%@ attribute name="fromApp" required="true" type="java.lang.Boolean"
              description="Indicate if coming from application" %>

<c:url var="talk2userIconUrl" value="/util/icons/talk2user.gif"/>

<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="org.silverpeas.quickinfo.multilang.quickinfo" />

<c:choose>
  <c:when test="${not empty news.thumbnail}">
    <li onclick="spWindow.loadPermalink('${news.permalink}')">
    <div class="content-actuality-illustration">
      <view:image src="${news.thumbnail.URL}" alt="" size="350x" css="actuality-illustration"/></div>
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test="${slideshow}">
        <li onclick="spWindow.loadPermalink('${news.permalink}')">
        <div class="content-actuality-illustration">
          <view:image src="/quickinfo/jsp/icons/defaultThumbnail.jpg" alt="" size="350x" css="actuality-illustration default-illustration"/></div>
      </c:when>
      <c:otherwise>
        <li onclick="spWindow.loadPermalink('${news.permalink}')" class="actuality-without-illustration">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<h3 class="actuality-title"><a class="sp-permalink" href="${news.permalink}">${news.title}</a></h3>
<div class="actuality-info-fonctionality">
  <span class="actuality-publishing">
    <span class="actuality-date"><span class="actuality-date-label"><fmt:message key="GML.publishedAt"/></span> ${silfn:formatDate(news.updateDate, userLanguage)}</span>
  <c:if test="${not fromApp}">
    <span class="actuality-source"><fmt:message key="GML.by"/>&nbsp;</span><view:componentPath componentId="${news.componentInstanceId}"/>
  </c:if>
  </span>
  <view:componentParam var="isCommentEnabled" componentId="${news.componentInstanceId}" parameter="comments"/>
  <c:if test="${silfn:booleanValue(isCommentEnabled) && news.numberOfComments > 0}">
    <a class="sp-permalink" href="${news.permalink}#commentaires" class="actuality-nb-commentaires"><img src="${talk2userIconUrl}" alt="commentaire"/> ${news.numberOfComments}
    </a>
  </c:if>
</div>
<p class="actuality-teasing">${news.description}</p>
</li>