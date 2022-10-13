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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/infoLetter" prefix="infoLetterTags" %>
<%@ include file="check.jsp" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>
<%@ page import="org.silverpeas.components.infoletter.model.InfoLetterPublication" %>
<%@ page import="java.util.stream.Collectors" %>

<c:set var="resources" value="${requestScope.resources}"/>
<jsp:useBean id="resources" type="org.silverpeas.core.util.MultiSilverpeasBundle"/>
<c:set var="userLanguage" value="${resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${resources.multilangBundle}"/>
<view:setBundle bundle="${resources.iconsBundle}" var="icons"/>

<c:set var="showHeader" value="${requestScope.showHeader}"/>
<jsp:useBean id="showHeader" type="java.lang.Boolean"/>
<c:set var="isSuscriber" value="${requestScope.userIsSuscriber}"/>
<jsp:useBean id="isSuscriber" type="java.lang.Boolean"/>

<c:set var="publications" value="${requestScope.listParutions}"/>
<jsp:useBean id="publications" type="java.util.List<org.silverpeas.components.infoletter.model.InfoLetterPublication>"/>
<c:set var="sentPublications" value="<%=publications.stream().filter(InfoLetterPublication::_isValid).collect(Collectors.toList())%>"/>
<jsp:useBean id="sentPublications" type="java.util.List<org.silverpeas.components.infoletter.model.InfoLetterPublication>"/>

<c:set var="letterName" value="${requestScope.letterName}"/>
<c:set var="letterDescription" value="${requestScope.letterDescription}"/>
<c:set var="letterFrequence" value="${requestScope.letterFrequence}"/>

<fmt:message key="infoLetter.listParutions" var="portletTitle"/>

<c:set var="componentId" value="<%=componentId%>"/>

<view:sp-page>
  <view:sp-head-part withCheckFormScript="true">
    <script type="text/javascript">
      function goToComponentInstance() {
        top.spWindow.loadComponent('${componentId}');
      }

      function openViewParution(par) {
        const url = sp.url.format(webContext + '/RinfoLetter/${componentId}/View', {
          'parution' : par
        });
        top.spWindow.loadLink(url);
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar componentId="${componentId}" componentJsCallback="goToComponentInstance">
      <view:browseBarElt label="${portletTitle}" link="javascript:goToComponentInstance()"/>
    </view:browseBar>
    <view:window>
      <view:frame>
        <c:if test="${showHeader}">
          <div class="headerInfoLetter">
            <h2 class="name">${silfn:escapeHtml(letterName)}</h2>
            <div class="frequence">${silfn:escapeHtml(letterFrequence)}</div>
            <p class="description componentInstanceIntro">${silfn:escapeHtml(letterDescription)}</p>
          </div>
        </c:if>
        <fmt:message key="infoLetter.name" var="nameLabel"/>
        <fmt:message key="GML.date" var="dateLabel"/>
        <fmt:message key="infoLetter.minicone" var="newsletterIcon" bundle="${icons}"/>
        <c:url var="newsletterIcon" value="${newsletterIcon}"/>
        <fmt:message key="infoLetter.permalink" var="permlinkIcon" bundle="${icons}"/>
        <c:url var="permlinkIcon" value="${permlinkIcon}"/>
        <fmt:message key="infoLetter.nonParu" var="notReleaseLabel"/>
        <fmt:message key="infoLetter.nonvisible" var="notReleaseIcon" bundle="${icons}"/>
        <c:url var="notReleaseIcon" value="${notReleaseIcon}"/>
        <fmt:message key="infoLetter.paru" var="releaseLabel"/>
        <fmt:message key="infoLetter.visible" var="releaseIcon" bundle="${icons}"/>
        <c:url var="releaseIcon" value="${releaseIcon}"/>
        <div id="newsletter-list">
          <view:arrayPane var="InfoLetter" routingAddress="portlet">
            <view:arrayColumn title="" sortable="false"/>
            <view:arrayColumn title="${nameLabel}" compareOn="${n -> n.title}"/>
            <view:arrayColumn title="${dateLabel}" compareOn="${n -> n.parutionDate}"/>
            <view:arrayLines var="pub" items="${sentPublications}">
              <jsp:useBean id="pub" type="org.silverpeas.components.infoletter.model.InfoLetterPublication"/>
              <c:set var="pubId" value="${pub.getPK().id}"/>
              <c:set var="accessUrl" value="javascript:openViewParution('${pubId}')"/>
              <view:arrayLine>
                <view:arrayCellText>
                  <a href="${accessUrl}">
                    <img src="${newsletterIcon}" alt=""/>
                  </a>
                </view:arrayCellText>
                <view:arrayCellText>
                  <a href="${accessUrl}">${silfn:escapeHtml(pub.title)}</a>
                  <a href="${pub._getPermalink()}" class="sp-permalink">
                    <img src="${permlinkIcon}" alt=""/>
                  </a>
                </view:arrayCellText>
                <view:arrayCellText>
                  <c:if test="${pub._isValid()}">
                    <c:set var="parutionDate" value="<%=DateUtil.parse(pub.getParutionDate())%>"/>
                    ${silfn:formatDate(parutionDate, userLanguage)}
                  </c:if>
                </view:arrayCellText>
              </view:arrayLine>
            </view:arrayLines>
          </view:arrayPane>
          <script type="text/javascript">
            whenSilverpeasReady(function() {
              sp.arrayPane.ajaxControls('#newsletter-list');
            });
          </script>
        </div>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>
