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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<%@ include file="check.jsp" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<view:setConstant var="userRole" constant="com.stratelia.webactiv.SilverpeasRole.user"/>

<view:setConstant var="SMALL_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.SMALL"/>

<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<jsp:useBean id="greaterUserRole" type="com.stratelia.webactiv.SilverpeasRole"/>
<c:set var="isPdcUsed" value="${requestScope.IsUsePdc}"/>
<c:set var="isPrivateSearch" value="${requestScope.IsPrivateSearch}"/>
<c:set var="isBasket" value="${requestScope.IsBasket}"/>
<c:set var="isOrder" value="${requestScope.IsOrder}"/>
<c:set var="isGuest" value="${requestScope.IsGuest}"/>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="albumList" value="${requestScope.Albums}"/>
<jsp:useBean id="albumList" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>
<c:set var="mediaList" value="${requestScope.MediaList}"/>
<jsp:useBean id="mediaList" type="java.util.List<com.silverpeas.gallery.model.Media>"/>

<c:set var="nbPerLine" value="${5}"/>

<c:set var="Silverpeas_Album_ComponentId" value="${componentId}" scope="session"/>

<fmt:message key="GML.PDCParam" var="pdcLabel"/>
<fmt:message key="gallery.pdcUtilizationSrc" var="pdcIcon" bundle="${icons}"/>
<c:url value="${pdcIcon}" var="pdcIcon"/>
<fmt:message key="gallery.addAlbum" var="addAlbumLabel"/>
<fmt:message key="gallery.addAlbum" var="addAlbumIcon" bundle="${icons}"/>
<c:url value="${addAlbumIcon}" var="addAlbumIcon"/>
<fmt:message key="gallery.viewNotVisible" var="viewNotVisibleLabel"/>
<fmt:message key="gallery.viewNotVisible" var="viewNotVisibleIcon" bundle="${icons}"/>
<c:url value="${viewNotVisibleIcon}" var="viewNotVisibleIcon"/>
<fmt:message key="gallery.viewBasket" var="viewBasketLabel"/>
<fmt:message key="gallery.viewBasket" var="viewBasketIcon" bundle="${icons}"/>
<c:url value="${viewBasketIcon}" var="viewBasketIcon"/>
<fmt:message key="gallery.viewOrderList" var="viewOrderListLabel"/>
<fmt:message key="gallery.viewOrderList" var="viewOrderListIcon" bundle="${icons}"/>
<c:url value="${viewOrderListIcon}" var="viewOrderListIcon"/>
<fmt:message key="gallery.askMedia" var="askMediaLabel"/>
<fmt:message key="gallery.askMedia" var="askMediaIcon" bundle="${icons}"/>
<c:url value="${askMediaIcon}" var="askMediaIcon"/>
<fmt:message key="GML.paste" var="pasteLabel"/>
<fmt:message key="GML.paste" var="pasteIcon" bundle="${icons}"/>
<c:url value="${pasteIcon}" var="pasteIcon"/>
<fmt:message key="gallery.lastResult" var="lastResultLabel"/>
<fmt:message key="gallery.lastResult" var="lastResultIcon" bundle="${icons}"/>
<c:url value="${lastResultIcon}" var="lastResultIcon"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/lucene/luceneQueryValidator.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/util/javaScript/jquery/jquery.cookie.js"/>"></script>
  <script type="text/javascript">
<c:if test="${greaterUserRole.isGreaterThanOrEquals(adminRole)}">
$(document).ready(function() {
  showAlbumsHelp();
});
</c:if>

var albumsHelpAlreadyShown = false;

function showAlbumsHelp() {
  var albumsCookieName = "Silverpeas_GALLERY_AlbumsHelp";
  var albumsCookieValue = $.cookie(albumsCookieName);
  if (!albumsHelpAlreadyShown && "IKnowIt" != albumsCookieValue) {
    albumsHelpAlreadyShown = true;
    $("#albums-message").dialog({
      modal : true,
      resizable : false,
      width : 400,
      dialogClass : 'help-modal-message',
      buttons : {
        "<fmt:message key="gallery.help.albums.buttons.ok"/>" : function() {
          $.cookie(albumsCookieName, "IKnowIt", { expires : 3650, path : '/' });
          $(this).dialog("close");
        },
        "<fmt:message key="gallery.help.albums.buttons.remind"/>" : function() {
          $(this).dialog("close");
        }
      }
    });
  }
}

function clipboardPaste() {
  $.progressMessage();
  document.albumForm.action = "paste";
  document.albumForm.submit();
}

var albumWindow = window;
var askWindow = window;

function openSPWindow(fonction, windowName) {
  pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400',
      'scrollbars=yes, resizable, alwaysRaised');
}

function deleteConfirm(id, nom) {
  if (window.confirm("<fmt:message key="gallery.confirmDeleteAlbum"/> '" + nom + "' ?")) {
    document.albumForm.action = "DeleteAlbum";
    document.albumForm.Id.value = id;
    document.albumForm.submit();
  }
}

function askMedia() {
  windowName = "askWindow";
  larg = "570";
  haut = "250";
  windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
  if (!askWindow.closed && askWindow.name == "askWindow")
    askWindow.close();
  askWindow = SP_openWindow("AskMedia", windowName, larg, haut, windowParams);
}

function sendData() {
  var query = stripInitialWhitespace($("#searchQuery").val());
  if (checkLuceneQuery(query)) {
    setTimeout("document.searchForm.submit();", 500);
  }
}

function checkLuceneQuery(query) {
  if(query != null && query.length > 0) {
    query = removeEscapes(query);
    // check question marks are used properly
    if(!checkQuestionMark(query)) {
      return false;
    }
    // check * is used properly
    if(!checkAsterisk(query)) {
      return false;
    }
    return true;
  }
  return false;
}
  </script>
</head>
<body>
<view:operationPane>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(adminRole) and isPdcUsed}">
    <c:url value="/RpdcUtilization/jsp/Main?ComponentId=${componentId}" var="tmpUrl"/>
    <view:operation action="javascript:onClick=openSPWindow('${tmpUrl}','utilizationPdc1')" altText="${pdcLabel}" icon="${pdcIcon}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
    <view:operationOfCreation action="javaScript:openGalleryEditor()" altText="${addAlbumLabel}" icon="${addAlbumIcon}"/>
    <view:operationSeparator/>
    <view:operation action="ViewNotVisible" altText="${viewNotVisibleLabel}" icon="${viewNotVisibleIcon}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${greaterUserRole eq userRole and isBasket}">
    <view:operation action="BasketView" altText="${viewBasketLabel}" icon="${viewBasketIcon}"/>
  </c:if>
  <c:if test="${(greaterUserRole eq adminRole or greaterUserRole eq userRole) and isOrder}">
    <view:operation action="OrderViewList" altText="${viewOrderListLabel}" icon="${viewOrderListIcon}"/>
  </c:if>
  <c:if test="${greaterUserRole != adminRole and not isGuest}">
    <view:operationOfCreation action="javaScript:askMedia()" altText="${askMediaLabel}" icon="${askMediaIcon}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:operation action="javascript:onClick=clipboardPaste()" altText="${pasteLabel}" icon="${pasteIcon}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${isPrivateSearch}">
    <view:operation action="LastResult" altText="${lastResultLabel}" icon="${lastResultIcon}"/>
  </c:if>
</view:operationPane>
<div id="${componentId}">
  <view:window>
    <view:frame>
      <c:if test="${isPrivateSearch}">
        <view:board>
          <center>
            <form name="searchForm" action="SearchKeyWord" method="post" onsubmit="javascript:sendData();" onkeypress="return event.keyCode != 13;">
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td valign="middle" align="left" class="txtlibform" width="30%">
                    <span style="line-height: 27px;"><fmt:message key="GML.search"/></span>
                  </td>
                  <td align="left" valign="middle">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <tr valign="middle">
                        <td valign="middle"><input type="text" name="SearchKeyWord" size="36" id="searchQuery"/></td>
                        <td valign="middle">&nbsp;</td>
                        <td valign="middle" align="left" width="100%">
                          <fmt:message key="GML.ok" var="tmpLabel"/>
                          <view:button label="${tmpLabel}" action="javascript:onClick=sendData();"/>
                        </td>
                        <td valign="middle">&nbsp;</td>
                        <td valign="middle"><a href="SearchAdvanced">
                          <fmt:message key="gallery.searchAdvanced"/>
                        </a></td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </form>
          </center>
        </view:board>
        <br/>
      </c:if>

      <view:areaOfOperationOfCreation/>
      <gallery:listSubAlbums subAlbumList="${albumList}"/>
      <br/>
      <view:board>
        <table width="100%" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr>
            <td colspan="5" align="center" class="ArrayNavigation">
              <fmt:message key="gallery.last.media"/>
            </td>
          </tr>
          <c:choose>
            <c:when test="${not empty mediaList}">
              <c:forEach var="media" items="${mediaList}" varStatus="loop">
                <c:set var="isNewLine" value="${loop.index % nbPerLine == 0}"/>
                <c:set var="isEndLine" value="${loop.last or loop.index % nbPerLine == (nbPerLine-1)}"/>
                <c:if test="${isNewLine}">
                  <tr>
                    <td colspan="${nbPerLine}">&#160;</td>
                  </tr>
                  <tr>
                </c:if>

                <td valign="middle" align="center">
                  <table border="0" width="10" align="center" cellspacing="1" cellpadding="0" class="fondPhoto">
                    <tr>
                      <td align="center">
                        <table cellspacing="1" cellpadding="3" border="0" class="cadrePhoto">
                          <tr>
                            <td bgcolor="#FFFFFF">
                              <a href="MediaView?MediaId=${media.id}">
                                <img src="${media.getApplicationThumbnailUrl(SMALL_RESOLUTION)}" border="0" alt="<c:out value='${media.title}'/>" title="<c:out value='${media.title}'/>"/>
                              </a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </td>

                <c:if test="${isEndLine}">
                  </tr>
                </c:if>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <tr>
                <td colspan="5" valign="middle" align="center" width="100%">
                  <br/>
                  <fmt:message key="gallery.empty.data"/>
                  <br/>
                </td>
              </tr>
            </c:otherwise>
          </c:choose>
        </table>

        <%@include file="albumManager.jsp" %>

      </view:board>
    </view:frame>
  </view:window>
  <form name="albumForm" action="" method="post">
    <input type="hidden" name="Id"/>
    <input type="hidden" name="Name"/>
    <input type="hidden" name="Description"/>
  </form>
</div>
<div id="albums-message" title="<fmt:message key="gallery.help.albums.title"/>" style="display: none;">
  <p>
    <fmt:message key="gallery.help.albums.content"/>
  </p>
</div>
<view:progressMessage/>
</body>
</html>