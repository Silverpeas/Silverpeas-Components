<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<%-- Set resource bundle --%>
<c:set var="_language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${_language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant var="TINY_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.TINY"/>
<view:setConstant var="MEDIUM_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.MEDIUM"/>

<c:set var="instanceId" value="${requestScope.browseContext[3]}"/>
<c:set var="profile" value="${requestScope.Profile}"/>
<c:set var="order" value="${requestScope.Order}"/>
<jsp:useBean id="order" type="com.silverpeas.gallery.model.Order"/>

<%
  // paramètres du formulaire
  Form xmlForm = (Form) request.getAttribute("XMLForm");
  DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");

  PagesContext context =
      new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
  context.setBorderPrinted(false);
%>

<html>
<head>
<view:looknfeel/>
<view:includePlugin name="qtip"/>
<style type="text/css">
  .photoPreviewTip {
    max-width: none;
    max-height: none;
  }
</style>
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>
<gallery:handlePhotoPreview jquerySelector=".imagePreview"/>
<script type="text/javascript">

var albumWindow = window;

function getObjects(selected) {
  var items = "";
  try {
    var boxItems = document.orderForm.SelectMedia;
    if (boxItems != null) {
      // au moins une checkbox exist
      var nbBox = boxItems.length;
      if ((nbBox == null) && (boxItems.checked == selected)) {
        // il n'y a qu'une checkbox non selectionnée
        items += boxItems.value + ",";
      } else {
        // search not checked boxes
        for (var i = 0; i < boxItems.length; i++) {
          if (boxItems[i].checked == selected) {
            items += boxItems[i].value + ",";
          }
        }
      }
    }
  } catch (e) {
    //Checkboxes are not displayed
  }
  return items;
}

function doPagination(index) {
  document.orderForm.SelectedIds.value = getObjects(true);
  document.orderForm.NotSelectedIds.value = getObjects(false);
  document.orderForm.Index.value = index;
  document.orderForm.action = "OrderPagination";
  document.orderForm.submit();
}

var orderWindow = window;

function download(photoId) {
  var url = "OrderDownloadMedia?MediaId=" + photoId;
  windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
  if (!orderWindow.closed && orderWindow.name == "orderWindow")
    orderWindow.close();
  orderWindow = SP_openWindow(url, "orderWindow", "740", "600", windowParams);
}

function updateOrder() {
  if (isCorrectForm()) {
    if (window.confirm("<fmt:message key="gallery.confirmValidOrder"/> ")) {
      document.orderForm.action = "OrderUpdate";
      document.orderForm.submit();
    }
  } else {
    var errorMsg = "<fmt:message key="gallery.checkAll"/>";
    window.alert(errorMsg);
  }
}

function isCorrectForm() {
  <c:set var="elementIds" value=""/>
  <c:forEach var="item" items="${order.rows}">
  <c:if test="${not empty elementIds}">
  <c:set var="elementIds" value="${elementIds},"/>
  </c:if>
  <c:set var="elementIds" value='${elementIds}"DownloadType${item.internalMedia.id}"'/>
  </c:forEach>

  var elementIds = [${elementIds}];

  var selectItem;
  var nbErrors = 0;
  for (var i = 0; i < elementIds.length; i++) {
    selectItem = document.getElementById(elementIds[i]);
    if (selectItem != null && selectItem.value == "0") {
      nbErrors++;
    }
  }

  return nbErrors <= 0;
}
</script>
</head>
<body id="${instanceId}" class="gallery gallery-order">

<fmt:message var="orderLabel" key="gallery.order"/>
<view:browseBar extraInformations="${orderLabel}">
  <fmt:message var="orderListLabel" key="gallery.viewOrderList"/>
  <view:browseBarElt label="${orderListLabel}" link="OrderViewList"/>
</view:browseBar>

<view:window>
<view:frame>

<table width="80%">
  <tr>
    <td class="txtlibform" nowrap><fmt:message key="gallery.descriptionOrder"/> :</td>
    <td>${order.orderId}</td>
  </tr>
  <tr>
    <td class="txtlibform" nowrap><fmt:message key="gallery.orderOf"/> :</td>
    <td>${order.userName}</td>
  </tr>
  <tr>
    <td class="txtlibform" nowrap><fmt:message key="gallery.orderDate"/> :</td>
    <c:set var="orderDate">
      <c:if test="${not empty order.creationDate}">
        <view:formatDateTime value="${order.creationDate}" language="${_language}"/>
      </c:if>
    </c:set>
    <td>${orderDate}</td>
  </tr>
  <tr>
    <td class="txtlibform" nowrap><fmt:message key="gallery.nbRows"/> :</td>
    <td>${order.nbRows}</td>
  </tr>
  <tr>
    <td class="txtlibform" nowrap><fmt:message key="GML.status"/> :</td>
    <c:if test="${not empty order.processDate}">
      <fmt:message key="gallery.processDate" var="processDateMsg"/>
      <c:set var="processDate"><view:formatDateTime value="${order.processDate}" language="${_language}"/></c:set>
    </c:if>
    <td>${processDateMsg} ${processDate}</td>
  </tr>
</table>


<%
  // formulaire
  if (xmlForm != null) {
%>
<br/>

<table border="0" width="50%">
  <tr>
    <td colspan="2">
      <%
        PagesContext xmlContext =
            new PagesContext("myForm", "0", resource.getLanguage(), false, componentId, null);
        xmlContext.setBorderPrinted(false);

        xmlForm.display(out, xmlContext, xmlData);
      %>
    </td>
  </tr>
</table>
<br/>
<% }
%>
<table>
  <tr>
    <td><input type="checkbox" checked="checked" disabled="disabled" name="CheckCharte"/></td>
    <td><fmt:message key="gallery.validCharte"/></td>
  </tr>
</table>


<form name="orderForm" method="POST" accept-charset="UTF-8">
<input type="hidden" name="SelectedIds">
<input type="hidden" name="NotSelectedIds">
<input type="hidden" name="OrderId" value="${order.orderId}">


<view:arrayPane var="order" routingAddress="OrderViewPagin">


  <fmt:message key="gallery.media" var="mediaCol"/>
  <view:arrayColumn title="${mediaCol}"/>


  <c:set var="viewValidation" value="true"/>
  <c:choose>
    <c:when test="${profile eq 'admin'}">
      <fmt:message key="gallery.choiceDownload" var="choiceDownloadCol"/>
      <view:arrayColumn title="${choiceDownloadCol}"/>

      <c:forEach var="row" items="${order.rows}">
        <c:set var="media" value="${row.internalMedia}"/>
        <jsp:useBean id="media" type="com.silverpeas.gallery.model.InternalMedia"/>
        <view:arrayLine>
          <c:set var="mediaTitle"><c:out value="${media.title}"/></c:set>
          <c:set var="photoCellText"><a class="imagePreview" href="MediaView?MediaId=${media.id}" tipTitle="${mediaTitle}" tipUrl="${media.getApplicationThumbnailUrl(MEDIUM_RESOLUTION)}"><img src="${media.getApplicationThumbnailUrl(TINY_RESOLUTION)}" alt=""/></a></c:set>
          <view:arrayCellText text="${photoCellText}"/>

          <c:choose>
            <c:when test="${row.downloadDecision eq 'T'}">
              <c:set var="viewValidation" value="false"/>
              <c:set var="downloadTxt"><fmt:message key="gallery.downloadDate"/>
                <view:formatDateTime value="${row.downloadDate}"/></c:set>
            </c:when>
            <c:otherwise>
              <c:if test="${not empty order.processUserId}">
                <c:set var="viewValidation" value="false"/>
                <c:choose>
                  <c:when test="${row.downloadDecision eq 'R'}">
                    <fmt:message var="downloadTxt" key="gallery.refused"/>
                  </c:when>
                  <c:when test="${row.downloadDecision eq 'D'}">
                    <fmt:message var="downloadTxt" key="gallery.downloadOk"/>
                  </c:when>
                  <c:when test="${row.downloadDecision eq 'DW'}">
                    <fmt:message var="downloadTxt" key="gallery.downloadWithWatermark"/>
                  </c:when>
                </c:choose>
              </c:if>
              <c:if test="${empty order.processUserId}">
                <c:set var="downloadTxt">
                  <select name="DownloadType${media.id}" id="DownloadType${media.id}" onChange="javascript:downloadGoTo(this.selectedIndex);">
                    <option value="0" selected>
                      <fmt:message key="gallery.choiceDownload"/></option>
                    <option value="R"><fmt:message key="gallery.refused"/></option>
                    <option value="D"><fmt:message key="gallery.downloadOk"/></option>
                    <option value="DW">
                      <fmt:message key="gallery.downloadWithWatermark"/></option>
                  </select>
                </c:set>
              </c:if>
            </c:otherwise>
          </c:choose>

          <view:arrayCellText text="${downloadTxt}"/>
        </view:arrayLine>
      </c:forEach>

    </c:when>
    <c:otherwise>
      <fmt:message key="gallery.downloadDate" var="downloadDateCol"/>
      <view:arrayColumn title="${downloadDateCol}"/>

      <c:forEach var="row" items="${order.rows}">
        <c:set var="mediaWhenNotAdmin" value="${row.internalMedia}"/>
        <jsp:useBean id="mediaWhenNotAdmin" type="com.silverpeas.gallery.model.InternalMedia"/>
        <view:arrayLine>
          <c:set var="mediaTitle"><c:out value="${mediaWhenNotAdmin.title}"/></c:set>
          <c:set var="photoCellText"><a class="imagePreview" href="MediaView?MediaId=${mediaWhenNotAdmin.id}" tipTitle="${mediaTitle}" tipUrl="${mediaWhenNotAdmin.getApplicationThumbnailUrl(MEDIUM_RESOLUTION)}"><img src="${mediaWhenNotAdmin.getApplicationThumbnailUrl(TINY_RESOLUTION)}" alt=""/></a></c:set>
          <view:arrayCellText text="${photoCellText}"/>


          <fmt:message var="downloadTxt" key="gallery.wait"/>
          <c:choose>
            <c:when test="${row.downloadDecision eq 'R'}">
              <fmt:message var="downloadTxt" key="gallery.refused"/>
            </c:when>
            <c:when test="${row.downloadDecision eq 'D' or row.downloadDecision eq 'DW'}">
              <c:set var="downloadTxt">
                <a href="OrderDownloadMedia?MediaId=${mediaWhenNotAdmin.id}&OrderId=${row.orderId}" target="_blank"><fmt:message key="gallery.download.photo"/> </a>
              </c:set>
            </c:when>
            <c:when test="${row.downloadDecision eq 'T' and not empty row.downloadDate}">
              <c:set var="downloadTxt"><fmt:message key="gallery.downloadDate"/>
                <view:formatDateTime value="${row.downloadDate}"/></c:set>
            </c:when>
          </c:choose>
          <view:arrayCellText text="${downloadTxt}"/>
        </view:arrayLine>

      </c:forEach>
    </c:otherwise>
  </c:choose>
</view:arrayPane>


<fmt:message key="GML.validate" var="validateLabel"/>
<fmt:message key="GML.cancel" var="cancelLabel"/>
<fmt:message key="GML.back" var="backLabel"/>
<view:buttonPane>
  <c:choose>
    <c:when test="${profile eq 'admin' and viewValidation eq 'true'}">
      <view:button action="javascript:updateOrder()" label="${validateLabel}"/>
      <view:button action="OrderViewList" label="${cancelLabel}"/>
    </c:when>
    <c:otherwise>
      <view:button action="OrderViewList" label="${backLabel}"/>
    </c:otherwise>
  </c:choose>
</view:buttonPane>

</form>

</view:frame>
</view:window>

</body>
</html>