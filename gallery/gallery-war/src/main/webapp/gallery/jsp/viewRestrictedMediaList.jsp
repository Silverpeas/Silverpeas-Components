<%--
  Copyright (C) 2000 - 2014 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="userRole" constant="com.stratelia.webactiv.SilverpeasRole.user"/>

<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<jsp:useBean id="greaterUserRole" type="com.stratelia.webactiv.SilverpeasRole"/>

<fmt:message key="gallery.updateSelectedMedia" var="updateSelectedMediaLabel"/>
<fmt:message key="gallery.updateSelectedMedia" var="updateSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${updateSelectedMediaIcon}" var="updateSelectedMediaIcon"/>
<fmt:message key="gallery.allSelect" var="allSelectMediaLabel"/>
<fmt:message key="gallery.allSelect" var="allSelectMediaIcon" bundle="${icons}"/>
<c:url value="${allSelectMediaIcon}" var="allSelectMediaIcon"/>
<fmt:message key="gallery.addToBasketSelectedMedia" var="addToBasketSelectedMediaLabel"/>
<fmt:message key='gallery.addToBasketSelectedMedia' var="addToBasketSelectedMediaIcon" bundle='${icons}'/>
<c:url var="addToBasketSelectedMediaIcon" value="${addToBasketSelectedMediaIcon}"/>

<c:set var="mediaList" value="${requestScope.MediaList}"/>
<jsp:useBean id="mediaList" type="java.util.List<com.silverpeas.gallery.model.Media>"/>
<c:set var="mediaResolution" value="${requestScope.MediaResolution}"/>
<jsp:useBean id="mediaResolution" type="com.silverpeas.gallery.constant.MediaResolution"/>
<c:set var="nbMediaPerPage" value="${requestScope.NbMediaPerPage}"/>
<c:set var="currentPageIndex" value="${requestScope.CurrentPageIndex}"/>
<c:set var="firstMediaIndex" value="${nbMediaPerPage * currentPageIndex}"/>
<c:set var="lastMediaIndex" value="${firstMediaIndex + nbMediaPerPage - 1}"/>
<c:set var="searchKeyWord" value="${requestScope.SearchKeyWord}"/>
<c:set var="isViewMetadata" value="${requestScope.IsViewMetadata}"/>
<c:set var="isViewList" value="${requestScope.IsViewList}"/>
<c:set var="selectedIds" value="${requestScope.SelectedIds}"/>
<c:set var="isViewNotVisible" value="${requestScope.ViewNotVisible}"/>
<c:set var="isBasket" value="${requestScope.IsBasket}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>

  <script type="text/javascript">

var albumWindow = window;

function sendData() {
  // envoi des photos sélectionnées pour la modif par lot
  document.mediaForm.SelectedIds.value = getMediaIds(true);
  document.mediaForm.NotSelectedIds.value = getMediaIds(false);

  document.mediaForm.submit();
}

function sendToBasket() {
  // envoi des photos sélectionnées dans le panier
  document.mediaForm.SelectedIds.value = getMediaIds(true);
  document.mediaForm.NotSelectedIds.value = getMediaIds(false);
  document.mediaForm.action = "BasketAddMediaList";
  document.mediaForm.submit();
}
  </script>
</head>
<body>
<gallery:browseBar isViewNotVisible="${isViewNotVisible}" searchKeyword="${searchKeyWord}"/>
<view:operationPane>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
    <view:operation action="AllSelected" altText="${allSelectMediaLabel}" icon="${allSelectMediaIcon}"/>
    <view:operation action="javascript:onClick=sendData()" altText="${updateSelectedMediaLabel}" icon="${updateSelectedMediaIcon}"/>
  </c:if>
  <c:if test="${greaterUserRole eq userRole and isBasket}">
    <view:operation action="AllSelected" altText="${allSelectMediaLabel}" icon="${allSelectMediaIcon}"/>
    <view:operation action="javascript:onClick=sendToBasket()" altText="${addToBasketSelectedMediaLabel}" icon="${addToBasketSelectedMediaIcon}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <gallery:displayAlbumContent searchKeyword="${searchKeyWord}"
                                 mediaList="${mediaList}"
                                 selectedIds="${selectedIds}"
                                 isViewMetadata="${isViewMetadata}"
                                 mediaResolution="${mediaResolution}"
                                 nbMediaPerPage="${nbMediaPerPage}"
                                 currentPageIndex="${currentPageIndex}"
                                 isViewList="${isViewList}"
                                 greaterUserRole="${greaterUserRole}"
                                 isBasket="${isBasket}"/>
    <c:if test="${empty mediaList}">
      <view:board>
        <c:choose>
          <c:when test="${isViewNotVisible}">
            <center><fmt:message key="gallery.empty.data"/></center>
          </c:when>
          <c:otherwise>
            <center>
              <fmt:message key="gallery.search.empty.begin"/>
              <span> <b>${ searchKeyWord}</b> </span>
              <fmt:message key="gallery.search.empty.end"/></center>
          </c:otherwise>
        </c:choose>
        <br/>
        <view:buttonPane>
          <fmt:message key="GML.back" var="backLabel"/>
          <view:button label="${backLabel}" action="${isViewNotVisible ? 'Main' : 'SearchAdvanced'}"/>
        </view:buttonPane>
      </view:board>
    </c:if>
  </view:frame>
</view:window>
</body>
</html>