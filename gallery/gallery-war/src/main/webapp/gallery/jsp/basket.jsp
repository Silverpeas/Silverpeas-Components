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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="componentLabel" value="${requestScope.browseContext[1]}"/>
<c:set var="instanceId" value="${requestScope.browseContext[3]}"/>
<c:set var="medias" value="${requestScope.MediaList}" />
<c:set var="userSelectionAlert" value="${requestScope.MediaTypeAlert}" />

<html>
<head>
<view:looknfeel/>

<view:includePlugin name="qtip"/>
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>
<script type="text/javascript">
var albumWindow = window;

function sendDataDelete()
{
	//Remove selected photo media from basket confirm message
	if(window.confirm("<fmt:message key="gallery.confirmDeleteMedias"/> "))
	{
		// envoi des photos selectionnees pour la suppression
		document.mediaForm.SelectedIds.value 	= getObjects(true);
		document.mediaForm.NotSelectedIds.value = getObjects(false);
		document.mediaForm.action				= "BasketDeleteSelectedMedia";
		document.mediaForm.submit();
	}
}

function getObjects(selected)
{
	var  items = "";
	try
	{
		var boxItems = document.mediaForm.SelectMedia;
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == selected) ){
				// il n'y a qu'une checkbox non selectionnee
				items += boxItems.value+",";
			} else{
				// search not checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == selected){
						items += boxItems[i].value+",";
					}
				}
			}
		}
	}
	catch (e)
	{
		//Checkboxes are not displayed
	}
	return items;
}

function doPagination(index)
{
	document.mediaForm.SelectedIds.value 	= getObjects(true);
	document.mediaForm.NotSelectedIds.value = getObjects(false);
	document.mediaForm.Index.value 			= index;
	document.mediaForm.action				= "BasketPagination";
	document.mediaForm.submit();
}

function deleteConfirm(id) {
	// Delete a media from basket confirm message
	if(window.confirm("<fmt:message key="gallery.confirmDeleteMedia"/>  ?"))
	{
			document.mediaFormDelete.action = "BasketDeleteMedia";
			document.mediaFormDelete.MediaId.value = id;
			document.mediaFormDelete.submit();
	}
}

$(document).ready(function() {
  $('.imagePreview').qtip({
    style: { classes: 'qtip-bootstrap' },
    content: {
      text: function(api) {
          return "<img src='" + $(this).attr("rel") + "' />";
      },
      title: {text: '<fmt:message key="gallery.preview" />'}
    }
  });
  <c:if test="${userSelectionAlert}">
  <fmt:message var="msgAlert" key="gallery.basket.media.type.alert"/>
  notyWarning('${msgAlert}');
  </c:if>
});

</script>
</head>
<body class="gallery gallery-basket" id="${instanceId}">
<fmt:message var="basketLabel" key="gallery.basket" />
<view:browseBar path="${basketLabel}" />
<view:operationPane>
  <c:if test="${not empty medias}">
    <c:if test="${requestScope.IsOrder}">
      <fmt:message var="addOrderLabel" key="gallery.addOrder" />
      <fmt:message var="addOrderIcon" key="gallery.AddOrder" bundle="${icons}" />
      <c:url var="addOrderIcon" value="${addOrderIcon}" />
      <view:operation altText="${addOrderLabel}" action="OrderAdd" icon="${addOrderIcon}" />
    </c:if>
    <fmt:message var="deleteSelectedLabel" key="gallery.deleteSelectedMedia" />
    <fmt:message var="deleteSelectedIcon" key="gallery.deleteSelectedMedia" bundle="${icons}" />
    <c:url var="deleteSelectedIcon" value="${deleteSelectedIcon}" />
    <view:operation altText="${deleteSelectedLabel}" action="javascript:onClick=sendDataDelete();" icon="${deleteSelectedIcon}" />

    <fmt:message var="deleteBasketLabel" key="gallery.deleteBasket" />
    <fmt:message var="deleteBasketIcon" key="gallery.deleteBasket" bundle="${icons}" />
    <c:url var="deleteBasketIcon" value="${deleteBasketIcon}" />
    <view:operation altText="${deleteBasketLabel}" action="BasketDelete" icon="${deleteBasketIcon}"/>
  </c:if>
</view:operationPane>

<view:window>
<view:frame>
<form name="mediaForm">
  <input type="hidden" name="SelectedIds">
  <input type="hidden" name="NotSelectedIds">

<c:if test="${empty medias}">
  <fmt:message key="gallery.emptyBasket" />
</c:if>

<c:if test="${not empty medias}">
  <view:arrayPane var="basketList" routingAddress="BasketView">
    <fmt:message key="gallery.media" var="mediaCol" />
    <view:arrayColumn title="${mediaCol}" />
    <fmt:message key="gallery.operation" var="operationCol" />
    <view:arrayColumn title="${operationCol}" sortable="false" />

    <fmt:message var="deleteIcon" key="gallery.deleteSrc" bundle="${icons}" />
    <c:url var="deleteIcon" value="${deleteIcon}" />
    <c:url var="photoSvcUrl" value="/services/gallery/${instanceId}/photos/" />
    <c:forEach items="${medias}" var="media">
      <view:arrayLine>
        <c:set var="mediaTitle"><view:encodeHtml string="${media.title}"/></c:set>
        <c:set var="photoCellText"><a class="imagePreview" href="MediaView?MediaId=${media.id}" rel="${photoSvcUrl}${media.id}/content?resolution=MEDIUM"><img src="${photoSvcUrl}${media.id}/content?resolution=TINY" title="${mediaTitle}" alt="${mediaTitle}" /></a></c:set>
        <view:arrayCellText text="${photoCellText}" />
        <c:set var="mediaSelected" value=""/>
        <c:set var="selChecked" value="" />
        <c:forEach var="selectedId" items="${requestScope.SelectedIds}">
          <c:if test="${id == media.id}">
            <c:set var="selChecked" value="checked" />
          </c:if>
        </c:forEach>
        <c:set var="operationLabel"><a href="#" onclick="javaScript:deleteConfirm('${media.id}')">
        <img src="${deleteIcon}" alt="<fmt:message key="GML.delete"/>" align="absmiddle" /></a>
        <input type="checkbox" name="SelectMedia" value="${media.id}" ${selChecked} />
        </c:set>
        <view:arrayCellText text="${operationLabel}"></view:arrayCellText>
      </view:arrayLine>
    </c:forEach>
  </view:arrayPane>

</c:if>

</form>

<form name="mediaFormDelete" action="" method="POST">
  <input type="hidden" name="MediaId">
  <input type="hidden" name="Name">
  <input type="hidden" name="Description">
</form>

<form name="favorite" action="" method="POST">
  <input type="hidden" name="Id">
</form>

</view:frame>
</view:window>

</body>
</html>