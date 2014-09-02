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

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="userRole" constant="com.stratelia.webactiv.SilverpeasRole.user"/>

<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<jsp:useBean id="greaterUserRole" type="com.stratelia.webactiv.SilverpeasRole"/>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<fmt:message key="gallery.addSubAlbum" var="addAlbumLabel"/>
<fmt:message key="gallery.addAlbum" var="addAlbumIcon" bundle="${icons}"/>
<c:url value="${addAlbumIcon}" var="addAlbumIcon"/>
<fmt:message key="gallery.updateAlbum" var="updateAlbumLabel"/>
<fmt:message key="gallery.updateAlbum" var="updateAlbumIcon" bundle="${icons}"/>
<c:url value="${updateAlbumIcon}" var="updateAlbumIcon"/>
<fmt:message key="gallery.deleteThisAlbum" var="deleteAlbumLabel"/>
<fmt:message key="gallery.deleteAlbum" var="deleteAlbumIcon" bundle="${icons}"/>
<c:url value="${deleteAlbumIcon}" var="deleteAlbumIcon"/>
<fmt:message key="gallery.copyAlbum" var="copyAlbumLabel"/>
<fmt:message key="gallery.copy" var="copyAlbumIcon" bundle="${icons}"/>
<c:url value="${copyAlbumIcon}" var="copyAlbumIcon"/>
<fmt:message key="gallery.cutAlbum" var="cutAlbumLabel"/>
<fmt:message key="gallery.cut" var="cutAlbumIcon" bundle="${icons}"/>
<c:url value="${cutAlbumIcon}" var="cutAlbumIcon"/>
<fmt:message key="gallery.export.album" var="exportAlbumLabel"/>
<fmt:message key="gallery.exported.album" var="exportedAlbumLabel"/>
<fmt:message key="gallery.updateSelectedMedia" var="updateSelectedMediaLabel"/>
<fmt:message key="gallery.updateSelectedMedia" var="updateSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${updateSelectedMediaIcon}" var="updateSelectedMediaIcon"/>
<fmt:message key="gallery.deleteSelectedMedia" var="deleteSelectedMediaLabel"/>
<fmt:message key="gallery.deleteSelectedMedia" var="deleteSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${deleteSelectedMediaIcon}" var="deleteSelectedMediaIcon"/>
<fmt:message key="gallery.categorizeSelectedMedia" var="categorizedSelectedMediaLabel"/>
<fmt:message key="gallery.categorizeSelectedMedia" var="categorizedSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${categorizedSelectedMediaIcon}" var="categorizedSelectedMediaIcon"/>
<fmt:message key="gallery.addPathForSelectedMedia" var="addPathForSelectedMediaLabel"/>
<fmt:message key="gallery.addPathForSelectedMedia" var="addPathForSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${addPathForSelectedMediaIcon}" var="addPathForSelectedMediaIcon"/>
<fmt:message key="gallery.allSelect" var="allSelectMediaLabel"/>
<fmt:message key="gallery.allSelect" var="allSelectMediaIcon" bundle="${icons}"/>
<c:url value="${allSelectMediaIcon}" var="allSelectMediaIcon"/>
<fmt:message key="gallery.media.selected.copy" var="copySelectedMediaLabel"/>
<fmt:message key="gallery.copy" var="copySelectedMediaIcon" bundle="${icons}"/>
<c:url value="${copySelectedMediaIcon}" var="copySelectedMediaIcon"/>
<fmt:message key="gallery.media.selected.cut" var="cutSelectedMediaLabel"/>
<fmt:message key="gallery.cut" var="cutSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${cutSelectedMediaIcon}" var="cutSelectedMediaIcon"/>
<fmt:message key="GML.paste" var="pasteSelectedMediaLabel"/>
<fmt:message key="gallery.paste" var="pasteSelectedMediaIcon" bundle="${icons}"/>
<c:url value="${pasteSelectedMediaIcon}" var="pasteSelectedMediaIcon"/>
<fmt:message key="gallery.photo.add" var="addPhotoLabel"/>
<fmt:message key="gallery.photo.add" var="addPhotoIcon" bundle="${icons}"/>
<c:url value="${addPhotoIcon}" var="addPhotoIcon"/>
<c:set var="addPhotoAction" value="AddMedia?type=Photo"/>
<fmt:message key="gallery.video.add" var="addVideoLabel"/>
<fmt:message key='gallery.video.add' var="addVideoIcon" bundle='${icons}'/>
<c:url var="addVideoIcon" value="${addVideoIcon}"/>
<c:set var="addVideoAction" value="AddMedia?type=Video"/>
<fmt:message key="gallery.sound.add" var="addSoundLabel"/>
<fmt:message key='gallery.sound.add' var="addSoundIcon" bundle='${icons}'/>
<c:url var="addSoundIcon" value="${addSoundIcon}"/>
<c:set var="addSoundAction" value="AddMedia?type=Sound"/>
<fmt:message key="gallery.streaming.add" var="addStreamingLabel"/>
<fmt:message key='gallery.streaming.add' var="addStreamingIcon" bundle='${icons}'/>
<c:url var="addStreamingIcon" value="${addStreamingIcon}"/>
<c:set var="addStreamingAction" value="AddMedia?type=Streaming"/>
<fmt:message key="gallery.addToBasketSelectedMedia" var="addToBasketSelectedMediaLabel"/>
<fmt:message key='gallery.addToBasketSelectedMedia' var="addToBasketSelectedMediaIcon" bundle='${icons}'/>
<c:url var="addToBasketSelectedMediaIcon" value="${addToBasketSelectedMediaIcon}"/>
<fmt:message key="gallery.viewBasket" var="viewBasketLabel"/>
<fmt:message key='gallery.viewBasket' var="viewBasketIcon" bundle='${icons}'/>
<c:url var="viewBasketIcon" value="${viewBasketIcon}"/>
<fmt:message key="gallery.diaporama" var="diaporamaLabel"/>
<fmt:message key='gallery.startDiaporama' var="diaporamaIcon" bundle='${icons}'/>
<c:url var="diaporamaIcon" value="${diaporamaIcon}"/>
<fmt:message key="gallery.addFavorite" var="addFavoriteLabel"/>
<fmt:message key='gallery.addFavorite' var="addFavoriteIcon" bundle='${icons}'/>
<c:url var="addFavoriteIcon" value="${addFavoriteIcon}"/>
<fmt:message key="gallery.lastResult" var="lastResultLabel"/>
<fmt:message key='gallery.lastResult' var="lastResultIcon" bundle='${icons}'/>
<c:url var="lastResultIcon" value="${lastResultIcon}"/>

<c:set var="currentAlbum" value="${requestScope.CurrentAlbum}"/>
<jsp:useBean id="currentAlbum" type="com.silverpeas.gallery.model.AlbumDetail"/>
<c:set var="albums" value="${requestScope.Albums}"/>
<jsp:useBean id="albums" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>

<c:set var="maximumFileSize" value="<%=FileRepositoryManager.getUploadMaximumFileSize()%>"/>

<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="path" value="${requestScope.Path}"/>
<jsp:useBean id="path" type="java.util.List<com.stratelia.webactiv.util.node.model.NodeDetail>"/>
<c:set var="nbMediaPerPage" value="${requestScope.NbMediaPerPage}"/>
<c:set var="currentPageIndex" value="${requestScope.CurrentPageIndex}"/>
<c:set var="mediaResolution" value="${requestScope.MediaResolution}"/>
<jsp:useBean id="mediaResolution" type="com.silverpeas.gallery.constant.MediaResolution"/>
<c:set var="dragAndDropEnable" value="${requestScope.DragAndDropEnable}"/>
<c:set var="isViewMetadata" value="${requestScope.IsViewMetadata}"/>
<c:set var="isViewList" value="${requestScope.IsViewList}"/>
<c:set var="selectedIds" value="${requestScope.SelectedIds}"/>
<c:set var="isPdcUsed" value="${requestScope.IsUsePdc}"/>
<c:set var="isPrivateSearch" value="${requestScope.IsPrivateSearch}"/>
<c:set var="isBasket" value="${requestScope.IsBasket}"/>
<c:set var="isGuest" value="${requestScope.IsGuest}"/>
<c:set var="isExportEnable" value="${requestScope.IsExportEnable}"/>

<view:setConstant var="PREVIEW_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.PREVIEW"/>
<view:setConstant var="ORIGINAL_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.ORIGINAL"/>

<c:set var="Silverpeas_Album_ComponentId" value="${componentId}" scope="session"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <view:includePlugin name="qtip"/>
  <view:includePlugin name="popup"/>
  <view:progressMessage/>
  <script type="text/javascript" src="<c:url value="/gallery/jsp/javaScript/dragAndDrop.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/util/javaScript/upload_applet.js"/>"></script>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>
  <script type="text/javascript">
var currentGallery = {
  'id' : "${currentAlbum.id}",
  'name' : "${silfn:escapeJs(currentAlbum.name)}",
  'description' : "${silfn:escapeJs(currentAlbum.description)}"
};

var albumWindow = window;

function addFavorite(name, description, url) {
  postNewLink(name, url, description);
}

<c:if test="${greaterUserRole eq adminRole or userId eq currentAlbum.creatorId}">
function deleteConfirm(id, nom) {
  // confirmation de suppression de l'album
  if (window.confirm("<fmt:message key="gallery.confirmDeleteAlbum"/> '" + $('<span>').html(nom).text() + "' ?")) {
    $.progressMessage();
    document.albumForm.action = "DeleteAlbum";
    document.albumForm.Id.value = id;
    document.albumForm.submit();
  }
}
</c:if>

<c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
  <c:if test="${dragAndDropEnable}">
function uploadCompleted(s) {
  location.href =
      "<c:url value="${silfn:componentURL(componentId)}ViewAlbum?Id=${currentAlbum.id}"/>";
}

function showDnD() {
  var url = "<c:url value="${silfn:fullApplicationURL(pageContext.request)}/RgalleryDragAndDrop/jsp/Drop?UserId=${userId}&ComponentId=${componentId}&AlbumId=${currentAlbum.id}"/>";
  var message = "<c:url value="${silfn:fullApplicationURL(pageContext.request)}/upload/Gallery_${userLanguage}.html"/>";
  showHideDragDrop(url, message, '<fmt:message key="GML.applet.dnd.alt"/>',
      '${maximumFileSize}', '<c:url value="/"/>', '<fmt:message key="GML.DragNDropExpand"/>',
      '<fmt:message key="GML.DragNDropCollapse"/>');
}
  </c:if>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">

function sendData() {
  // envoi des photos selectionnees pour la modif par lot
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    document.mediaForm.SelectedIds.value = selectedPhotos;
    document.mediaForm.NotSelectedIds.value = getMediaIds(false);
    document.mediaForm.submit();
  }
}

function sendDataDelete() {
  //confirmation de suppression de l'album
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    if (window.confirm("<fmt:message key="gallery.confirmDeleteMedias"/> ")) {
      $.progressMessage();
      // envoi des photos selectionnees pour la modif par lot
      document.mediaForm.SelectedIds.value = selectedPhotos;
      document.mediaForm.NotSelectedIds.value = getMediaIds(false);
      document.mediaForm.action = "DeleteSelectedMedia";
      document.mediaForm.submit();
    }
  }
}
    <c:if test="${isPdcUsed}">
function sendDataCategorize() {
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    var selectedIds = selectedPhotos;
    var notSelectedIds = getMediaIds(false);

    urlWindow = "CategorizeSelectedMedia?SelectedIds=" + selectedIds + "&NotSelectedIds=" +
        notSelectedIds;
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!albumWindow.closed && albumWindow.name == "albumWindow") {
      albumWindow.close();
    }
    albumWindow = SP_openWindow(urlWindow, "albumWindow", "550", "250", windowParams);
  }
}
    </c:if>
  </c:if>
</c:if>

function sendToBasket() {
  // envoi des photos selectionnees dans le panier
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    document.mediaForm.SelectedIds.value = selectedPhotos;
    document.mediaForm.NotSelectedIds.value = getMediaIds(false);
    document.mediaForm.action = "BasketAddMediaList";
    document.mediaForm.submit();
  }
}

<c:if test="${greaterUserRole eq adminRole}">
function sendDataForAddPath() {
  // envoi des photos selectionnees pour le placement par lot
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    document.mediaForm.SelectedIds.value = selectedPhotos;
    document.mediaForm.NotSelectedIds.value = getMediaIds(false);
    document.mediaForm.action = "AddAlbumForSelectedMedia";
    document.mediaForm.submit();
  }
}

function clipboardPaste() {
  $.progressMessage();
  document.albumForm.action = "paste";
  document.albumForm.submit();
}

function clipboardCopy() {
  top.IdleFrame.location.href =
      '<c:url value="${silfn:componentURL(componentId)}"/>copy?Object=Node&Id=${currentAlbum.id}';
}

function clipboardCut() {
  top.IdleFrame.location.href =
      '<c:url value="${silfn:componentURL(componentId)}"/>cut?Object=Node&Id=${currentAlbum.id}';
}

function exportAlbum() {
  // Open jquery dialog with user export options
  $("#album-export-dialog").dialog({
    autoOpen: true,
    title: "${exportAlbumLabel}",
    modal: true,
    minWidth: 350,
    buttons: {
      '<fmt:message key="GML.export"/>': function() {
        callExport();
        $( this ).dialog( "close" );
      },
      '<fmt:message key="GML.cancel"/>': function() {
        $( this ).dialog( "close" );
      }
    }
  });
}

//make an ajax call here and then display a waiting message until we receive the asynchronous response
function callExport() {
  $.get("<c:url value='/Rgallery/${componentId}/ExportAlbum'/>", { albumId:'${currentAlbum.id}',format:$("input[name=format]:checked").val()},
    function(data){
      $.closeProgressMessage();
      //alert('data = ' + data);
      $("#album-export-result-dialog").html(data);
      $("#album-export-result-dialog").dialog({
        autoOpen: true,
        title: "${exportedAlbumLabel}",
        modal: true,
        minWidth: 500,
        buttons: {
          '<fmt:message key="GML.close"/>': function() {
            $("#album-export-result-dialog").html("");
            $( this ).dialog( "close" );
          }
        }
      });
      $("#album-export-result-dialog").show();
    }, 'text');
  $.progressMessage();
}


function CopySelectedMedia() {
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    document.mediaForm.SelectedIds.value = selectedPhotos;
    document.mediaForm.NotSelectedIds.value = getMediaIds(false);
    document.mediaForm.action = "CopySelectedMedia";
    document.mediaForm.submit();
  }
}

function CutSelectedMedia() {
  var selectedPhotos = getMediaIds(true);
  if (selectedPhotos && selectedPhotos.length > 0) {
    document.mediaForm.SelectedIds.value = selectedPhotos;
    document.mediaForm.NotSelectedIds.value = getMediaIds(false);
    document.mediaForm.action = "CutSelectedMedia";
    document.mediaForm.submit();
  }
}
</c:if>
  </script>
<c:if test="${not empty currentAlbum.media}">
  <gallery:handlePhotoPreview jquerySelector="${'.mediaPreview'}" />
</c:if>
  <gallery:diaporama/>
</head>
<body>
<gallery:browseBar albumPath="${path}"/>
<view:operationPane>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
    <%-- Actions on album --%>
    <view:operationOfCreation action="javaScript:openGalleryEditor()" altText="${addAlbumLabel}" icon="${addAlbumIcon}"/>
    <c:if test="${isExportEnable}">
      <view:operation action="javascript:onClick=exportAlbum()" altText="${exportAlbumLabel}" />
    </c:if>
    <c:if test="${greaterUserRole eq adminRole or userId eq currentAlbum.creatorId}">
      <view:operation action="javaScript:openGalleryEditor(currentGallery)" altText="${updateAlbumLabel}" icon="${updateAlbumIcon}"/>
      <c:set var="tmpLabel"><c:out value="${currentAlbum.name}"/></c:set>
      <view:operation action="javaScript:deleteConfirm('${currentAlbum.id}','${silfn:escapeHtml(silfn:escapeJs(tmpLabel))}')" altText="${deleteAlbumLabel}" icon="${deleteAlbumIcon}"/>
    </c:if>
    <view:operationSeparator/>
    <%-- Copy/Cut of albums --%>
    <c:if test="${greaterUserRole eq adminRole}">
      <view:operation action="javascript:onClick=clipboardCopy()" altText="${copyAlbumLabel}" icon="${copyAlbumIcon}"/>
      <view:operation action="javascript:onClick=clipboardCut()" altText="${cutAlbumLabel}" icon="${cutAlbumIcon}"/>
      <view:operationSeparator/>
    </c:if>
  </c:if>
  <%-- Manage media by massively way --%>
  <c:if test="${not (greaterUserRole eq userRole and not isBasket)}">
    <view:operation action="AllSelected" altText="${allSelectMediaLabel}" icon="${allSelectMediaIcon}"/>
  </c:if>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
    <view:operation action="javascript:onClick=sendData()" altText="${updateSelectedMediaLabel}" icon="${updateSelectedMediaIcon}"/>
    <view:operation action="javascript:onClick=sendDataDelete()" altText="${deleteSelectedMediaLabel}" icon="${deleteSelectedMediaIcon}"/>
    <c:if test="${isPdcUsed}">
      <view:operation action="javascript:onClick=sendDataCategorize()" altText="${categorizedSelectedMediaLabel}" icon="${categorizedSelectedMediaIcon}"/>
    </c:if>
  </c:if>
  <c:if test="${greaterUserRole eq adminRole}">
    <view:operation action="javascript:onClick=sendDataForAddPath()" altText="${addPathForSelectedMediaLabel}" icon="${addPathForSelectedMediaIcon}"/>
    <view:operation action="javascript:onClick=CopySelectedMedia()" altText="${copySelectedMediaLabel}" icon="${copySelectedMediaIcon}"/>
    <view:operation action="javascript:onClick=CutSelectedMedia()" altText="${cutSelectedMediaLabel}" icon="${cutSelectedMediaIcon}"/>
    <view:operation action="javascript:onClick=clipboardPaste()" altText="${pasteSelectedMediaLabel}" icon="${pasteSelectedMediaIcon}"/>
  </c:if>
  <%-- Manage one media --%>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
    <view:operationSeparator/>
    <view:operationOfCreation action="${addPhotoAction}" altText="${addPhotoLabel}" icon="${addPhotoIcon}"/>
    <view:operationOfCreation action="${addVideoAction}" altText="${addVideoLabel}" icon="${addVideoIcon}"/>
    <view:operationOfCreation action="${addSoundAction}" altText="${addSoundLabel}" icon="${addSoundIcon}"/>
    <view:operationOfCreation action="${addStreamingAction}" altText="${addStreamingLabel}" icon="${addStreamingIcon}"/>
  </c:if>
  <%-- Basket for users --%>
  <c:if test="${greaterUserRole eq userRole and isBasket or isExportEnable}">
    <view:operationSeparator/>
    <view:operation action="javascript:onClick=sendToBasket()" altText="${addToBasketSelectedMediaLabel}" icon="${addToBasketSelectedMediaIcon}"/>
    <view:operation action="BasketView" altText="${viewBasketLabel}" icon="${viewBasketIcon}"/>
  </c:if>
  <%-- Diaporama --%>
  <c:if test="${not empty currentAlbum.media and fn:length(currentAlbum.media) > 1}">
    <view:operationSeparator/>
    <view:operation action="javascript:startSlideshow()" altText="${diaporamaLabel}" icon="${diaporamaIcon}"/>
  </c:if>
  <%-- Favorites --%>
  <c:if test="${not isGuest}">
    <view:operationSeparator/>
    <c:set var="tmpDesc"><c:out value="${currentAlbum.description}"/></c:set>
    <view:operation action="javaScript:addFavorite($('#breadCrumb').text(),'${silfn:escapeJs(tmpDesc)}','${currentAlbum.link}')" altText="${addFavoriteLabel}" icon="${addFavoriteIcon}"/>
  </c:if>
  <%-- Private search --%>
  <c:if test="${isPrivateSearch}">
    <view:operationSeparator/>
    <view:operation action="LastResult" altText="${lastResultLabel}" icon="${lastResultIcon}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <view:areaOfOperationOfCreation/>
    <table width="100%">
      <tr>
        <td>
          <gallery:listSubAlbums subAlbumList="${albums}"/>
        </td>
      </tr>
    </table>
    <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole) and dragAndDropEnable}">
      <!-- Displaying the Drag&Drop area -->
      <center>
        <table width="98%">
          <tr>
            <td align="right"><a href="javascript:showDnD()"
                                 id="dNdActionLabel"><%=resource.getString("GML.DragNDropExpand")%>
            </a>

              <div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding: 0" valign="top"></div>
            </td>
          </tr>
        </table>
      </center>
    </c:if>
    <gallery:displayAlbumContent currentAlbum="${currentAlbum}"
                                 mediaList="${currentAlbum.media}"
                                 selectedIds="${selectedIds}"
                                 isViewMetadata="${isViewMetadata}"
                                 mediaResolution="${mediaResolution}"
                                 nbMediaPerPage="${nbMediaPerPage}"
                                 currentPageIndex="${currentPageIndex}"
                                 isViewList="${isViewList}"
                                 greaterUserRole="${greaterUserRole}"
                                 isBasket="${isBasket}"/>
    <c:choose>
      <c:when test="${empty currentAlbum.media and empty albums and greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
        <c:set var="templateUserRole" value="${publisherRole}"/>
      </c:when>
      <c:when test="${empty currentAlbum.media and greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <c:set var="templateUserRole" value="${writerRole}"/>
      </c:when>
      <c:when test="${empty currentAlbum.media and greaterUserRole.isGreaterThanOrEquals(userRole)}">
        <c:set var="templateUserRole" value="${userRole}"/>
      </c:when>
    </c:choose>
    <c:if test="${not empty templateUserRole}">
      <div id="folder-empty" class="inlineMessage">
        <view:applyTemplate locationBase="components:gallery" name="galleryEmptyAlbum">
          <view:templateParam name="dragAndDropEnable" value="${dragAndDropEnable}"/>
          <view:templateParam name="albumPart" value="${templateUserRole eq publisherRole}"/>
          <view:templateParam name="mediaPart" value="${templateUserRole eq publisherRole or templateUserRole eq writerRole}"/>
          <view:templateParam name="albumOperation" value="${addAlbumLabel}"/>
          <view:templateParam name="albumUrl" value="javaScript:openGalleryEditor()"/>
          <view:templateParam name="photoOperation" value="${addPhotoLabel}"/>
          <view:templateParam name="photoUrl" value="${addPhotoAction}"/>
          <view:templateParam name="videoOperation" value="${addVideoLabel}"/>
          <view:templateParam name="videoUrl" value="${addVideoAction}"/>
          <view:templateParam name="soundOperation" value="${addSoundLabel}"/>
          <view:templateParam name="soundUrl" value="${addSoundAction}"/>
          <view:templateParam name="streamingOperation" value="${addStreamingLabel}"/>
          <view:templateParam name="streamingUrl" value="${addStreamingAction}"/>
        </view:applyTemplate>
      </div>
    </c:if>

<div id="album-export-dialog" style="display: none;">
  <form id="exportForm" action="ExportAlbum" target="_blank">
    <fieldset>
      <legend><fmt:message key="gallery.export.format" /></legend>
      <input type="radio" name="format" value="${ORIGINAL_RESOLUTION.label}" checked="checked" /><fmt:message key="gallery.export.format.original"/>
      <input type="radio" name="format" value="${PREVIEW_RESOLUTION.label}" /><fmt:message key="gallery.export.format.preview"/>
    </fieldset>
  </form>
</div>
<div id="album-export-result-dialog" style="display: none;">
</div>

    <%@include file="albumManager.jsp" %>
  </view:frame>
</view:window>
<form name="albumForm" action="" method="post">
  <input type="hidden" name="Id"/>
  <input type="hidden" name="Name"/>
  <input type="hidden" name="Description"/>
</form>
<form name="favorite" action="" method="post">
  <input type="hidden" name="Id"/>
</form>
<view:progressMessage/>
</body>
</html>
