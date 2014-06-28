<%@ page import="com.silverpeas.gallery.GalleryComponentSettings" %>
<%@ page import="com.silverpeas.gallery.model.Media" %>
<%@ page import="com.silverpeas.gallery.model.Photo" %>
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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>

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
<fmt:message var="commentTab" key="gallery.comments"/>

<c:set var="curPhoto" value="${requestScope.Media}"/>
<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="photoResourceType" value="${curPhoto.contributionType}"/>
<c:set var="photoId" value="${curPhoto.id}"/>
<c:set var="callback">function( event ) { if (event.type === 'listing') { commentCount = event.comments.length; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + event.comments.length + ')'); } else if (event.type === 'deletion') { commentCount--; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } else if (event.type === 'addition') { commentCount++; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } }</c:set>


<fmt:message var="permalinkIcon" key='gallery.link' bundle='${icons}'/>
<c:url var="permalinkIconUrl" value="${permalinkIcon}"/>
<fmt:message var="previousIcon" key='gallery.previous' bundle='${icons}'/>
<c:url var="previousIconUrl" value="${previousIcon}"/>
<fmt:message var="nextIcon" key='gallery.next' bundle='${icons}'/>
<c:url var="nextIconUrl" value="${nextIcon}"/>
<fmt:message var="downloadIcon" key='gallery.image.download' bundle='${icons}'/>
<c:url var="downloadIconUrl" value="${downloadIcon}"/>
<fmt:message var="downloadWatermarkIcon" key='gallery.image.dowloadWatermark' bundle='${icons}'/>
<c:url var="downloadWatermarkIconUrl" value="${downloadWatermarkIcon}"/>
<fmt:message var="downloadForbiddenIcon" key='gallery.image.download.forbidden' bundle='${icons}'/>
<c:url var="downloadForbiddenIconUrl" value="${downloadForbiddenIcon}"/>

<%
  // récupération des paramètres :
  Photo photo = (Photo) request.getAttribute("Media");
  List<NodeDetail> path = (List<NodeDetail>) request.getAttribute("Path");
  String profile = (String) request.getAttribute("Profile");
  Integer rang = (Integer) request.getAttribute("Rang");
  Integer albumSize = (Integer) request.getAttribute("NbMedia");
  boolean viewMetadata = (Boolean) request.getAttribute("IsViewMetadata");
  boolean watermark = (Boolean) request.getAttribute("IsWatermark");
  boolean updateAllowed = (Boolean) request.getAttribute("UpdateMediaAllowed");
  String sizeParam = (String) request.getAttribute("PreviewSize");
  boolean isBasket = (Boolean) request.getAttribute("IsBasket");
  boolean isPrivateSearch = (Boolean) request.getAttribute("IsPrivateSearch");

  // paramètres du formulaire
  Form xmlForm = (Form) request.getAttribute("XMLForm");
  DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");

  // déclaration des variables :
  String nomRep = GalleryComponentSettings.getMediaFolderNamePrefix() + photo.getMediaPK().getId();
  String name = "";
  if (photo.getFileName() != null && !photo.getFileName().equals("")) {
    name = photo.getFileName();
  }
  String namePreview = photo.getId() + "_" + sizeParam + ".jpg";
  String preview_url = FileServerUtils.getUrl(componentId, namePreview, photo.getFileMimeType(),
      nomRep);
  String title = photo.getTitle();
  long size = photo.getFileSize();
  int height = photo.getResolutionH();
  int width = photo.getResolutionW();
  String photoId = photo.getMediaPK().getId();
  String lien = FileServerUtils.getUrl(componentId, URLEncoder.encode(name, "UTF-8"), photo.
      getFileMimeType(), nomRep);
  String lienWatermark = "";
  Collection<String> metaDataKeys = null;
  if (viewMetadata) {
    metaDataKeys = photo.getMetaDataProperties();
  }

  // si le paramètre watermark est actif, récupérer l'image avec le watermark
  if (watermark) {
    // image avec le watermarkOther pour le téléchargement
    File fileWatermark = new File(FileRepositoryManager.getAbsolutePath(componentId) + nomRep + File.separator + photo.
        getId() + "_watermark.jpg");

    if (fileWatermark.exists()) {
      lienWatermark = FileServerUtils.getUrl(componentId, photo.getId() + "_watermark.jpg", photo.
          getFileMimeType(), nomRep);
    }
  }

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<view:includePlugin name="wysiwyg"/>
<view:includePlugin name="messageme"/>
<view:includePlugin name="invitme"/>
<view:includePlugin name="userZoom"/>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">

var notifyWindow = window;

function deleteConfirm(id,nom)
{
	if(window.confirm("<%=resource.getString("gallery.confirmDeletePhoto")%> '"+nom+"' ?"))
	{
		document.mediaForm.action = "DeleteMedia?MediaId="+id;
		document.mediaForm.submit();
	}
}

function goToNotify(url)
{
	windowName = "notifyWindow";
	larg = "740";
	haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
        notifyWindow.close();
    notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

	function clipboardCopy() {
	    top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>copy?Object=Image&Id=<%=photo.getId()%>';
	}

	function clipboardCut() {
	    top.IdleFrame.location.href = '../..<%=gallerySC.getComponentUrl()%>cut?Object=Image&Id=<%=photo.getId()%>';
	}

  $(window).keydown(function(e){
    var keyCode = eval(e.keyCode);
    if (37 == keyCode || keyCode == 39) {
      e.preventDefault();
      var button;
      if (37 == keyCode) {
        // Previous
        button = $('#previousButton').get(0);
      } else if (39 == keyCode) {
        // Next
        button = $('#nextButton').get(0);
      }
      if (button) {
        button.click();
      }
      return true;
    }
  });

</script>
<%@include file="diaporama.jsp" %>
</head>
<body class="gallery gallery-fiche-image yui-skin-sam" id="${instanceId}">
  <%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    displayPath(path, browseBar);

    String url = "ToAlertUser?MediaId=" + photoId;
    operationPane.addOperation(resource.getIcon("gallery.alert"), resource.getString("GML.notify"),
        "javaScript:onClick=goToNotify('" + url + "')");
    operationPane.addLine();

    if (updateAllowed) {
      operationPane.addOperation(resource.getIcon("GML.delete"), resource.getString(
          "GML.delete"), "javaScript:deleteConfirm('" + photoId + "','" + EncodeHelper.
          javaStringToHtmlString(EncodeHelper.javaStringToJsString(title)) + "')");
    }
    if ("admin".equals(profile)) {
      operationPane.addOperation(resource.getIcon("gallery.copy"), resource.getString("GML.copy"),
          "javascript:onClick=clipboardCopy()");
      operationPane.addOperation(resource.getIcon("gallery.cut"), resource.getString("GML.cut"),
          "javascript:onClick=clipboardCut()");
      operationPane.addLine();
    }
    if (albumSize.intValue() > 1) {
      // diaporama
      operationPane.addOperation(resource.getIcon("gallery.startDiaporama"), resource.getString(
          "gallery.diaporama"), "javascript:startSlideshow('"+photoId+"')");
    }

    if ("user".equals(profile) && isBasket) {
      operationPane.addLine();
      // ajouter la photo au panier
      operationPane.addOperation(resource.getIcon("gallery.addMediaToBasket"), resource.getString(
          "gallery.addMediaToBasket"), "BasketAddMedia?MediaId=" + photoId);
    }

    if (isPrivateSearch) {
      // derniers résultat de la recherche
      operationPane.addLine();
      operationPane.addOperation(resource.getIcon("gallery.lastResult"), resource.getString(
          "gallery.lastResult"), "LastResult");
    }

    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("gallery.media"), "#", true);
    if (updateAllowed) {
      tabbedPane.addTab(resource.getString("gallery.info"), "EditInformation?MediaId=" + photoId,
          false);
    }
    if (updateAllowed) {
      tabbedPane.addTab(resource.getString("gallery.accessPath"), "AccessPath?MediaId=" + photoId,
          false);
    }

    out.println(window.printBefore());
    out.println(tabbedPane.print());
  %>
<view:frame>
<form name="mediaForm" method="post" accept-charset="UTF-8">

<div class="rightContent">
  <!-- nom du fichier -->
  <div class="fileName">
    <c:choose>
      <c:when test="${requestScope.ViewLinkDownload or curPhoto.downloadable}">
        <a href="<%=lien%>" target="_blank">${curPhoto.fileName} <img src="${downloadIconUrl}" alt="<%=EncodeHelper.javaStringToHtmlString(resource.getString("gallery.download.photo"))%>" title="<fmt:message key='gallery.originale'/>"/></a>
        <% if (!lienWatermark.equals("")) { %>
          <a href="<%=lienWatermark%>" target="_blank"><img src="${downloadWatermarkIconUrl}" alt="<%=EncodeHelper.javaStringToHtmlString(resource.getString("gallery.download.photo"))%>" title="<fmt:message key='gallery.originaleWatermark'/>"/></a>
        <% } %>
      </c:when>
      <c:otherwise>
        ${curPhoto.fileName} <img src="${downloadForbiddenIconUrl}" alt="<fmt:message key='gallery.download.forbidden'/>" title="<fmt:message key='gallery.download.forbidden'/>" class="forbidden-download-file"/>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="fileCharacteristic  bgDegradeGris">
    <p>
      <span class="fileCharacteristicWeight"><fmt:message key="gallery.weight" /> <b><%=FileRepositoryManager.formatFileSize(size)%></b></span>
      <span class="fileCharacteristicSize"><fmt:message key="gallery.dimension" /> <b><%=width%> x <%=height%> <fmt:message key="gallery.pixels" /></b></span> <br class="clear" />
    </p>
  </div>

  <c:set var="createDate" value="${curPhoto.creationDate}"/>
  <c:set var="lastUpdateDate" value="${curPhoto.lastUpdateDate}"/>

  <div class="bgDegradeGris" id="suggestionInfoPublication">
  <c:if test="${not empty curPhoto.author}">
    <p id="authorInfo"><b><fmt:message key="GML.author"/></b> ${curPhoto.author}
    </p>
  </c:if>
  <c:if test="${not empty curPhoto.lastUpdater}">
    <div class="paragraphe" id="infoModification"> <b><fmt:message key="GML.updatedAt"/></b>${silfn:formatDate(lastUpdateDate, _language)} <fmt:message key="GML.by"/>
      <view:username userId="${curPhoto.lastUpdater.id}"/>
      <div class="profilPhoto"><img src='<c:url value="${curPhoto.lastUpdater.avatar}" />' alt="" class="defaultAvatar"/></div>
    </div>
  </c:if>

  <c:if test="${not empty curPhoto.creator}">
    <div class="paragraphe" id="infoCreation">
      <b><fmt:message key="GML.createdAt"/></b>${silfn:formatDate(createDate, _language)} <fmt:message key="GML.by"/>
      <view:username userId="${curPhoto.creator.id}"/>
      <div class="profilPhoto"><img src='<c:url value="${curPhoto.creator.avatar}" />' alt="" class="defaultAvatar"/></div>
    </div>
  </c:if>
    <p id="permalinkInfo">
      <fmt:message key="gallery.CopyPhotoLink" var="cpPhotoLinkAlt"/>
      <a title="${cpPhotoLinkAlt}" href="${curPhoto.permalink}">
        <img border="0" alt="${cpPhotoLinkAlt}" title="${cpPhotoLinkAlt}" src="${permalinkIconUrl}" />
      </a> <fmt:message key="GML.permalink"/> <br />
      <input type="text" value="${silfn:fullApplicationURL(pageContext.request)}}${curPhoto.permalink}" onfocus="select();" class="inputPermalink" />
    </p>
  </div>
  <c:if test="${curPhoto.downloadable and (curPhoto.visibilityPeriod.defined or curPhoto.downloadPeriod.defined)}">
    <div class="periode bgDegradeGris" id="periode">
      <div class="header bgDegradeGris">
        <h4 class="clean"><fmt:message key="GML.period"/></h4>
      </div>
      <c:if test="${curPhoto.visibilityPeriod.defined}">
        <div class="periode_visibility paragraphe">
          <fmt:message key="gallery.beginDate"/>
          <b>
            <c:if test="${curPhoto.visibilityPeriod.beginDatable.defined}">
              <view:formatDate value="${curPhoto.visibilityPeriod.beginDate}" language="${language}" />
            </c:if>
          </b>
          <b><fmt:message key="GML.toDate"/>
            <c:if test="${curPhoto.visibilityPeriod.endDatable.defined}">
              <view:formatDate value="${curPhoto.visibilityPeriod.endDate}" language="${language}" />
            </c:if>
          </b>
        </div>
      </c:if>
      <c:if test="${curPhoto.downloadPeriod.defined}">
        <div class="periode_download paragraphe">
          <fmt:message key="gallery.beginDownloadDate"/>
          <b>
            <c:if test="${curPhoto.downloadPeriod.beginDatable.defined}">
              <view:formatDate value="${curPhoto.downloadPeriod.beginDate}" language="${language}" />
            </c:if>
          </b>
          <b><fmt:message key="GML.toDate"/>
            <c:if test="${curPhoto.downloadPeriod.endDatable.defined}">
              <view:formatDate value="${curPhoto.downloadPeriod.endDate}" language="${language}" />
            </c:if>
          </b>
        </div>
      </c:if>
      <br class="clear"/>
    </div>
  </c:if>

  <c:if test="${requestScope.IsUsePdc}">
    <view:pdcClassificationPreview componentId="${instanceId}" contentId="${photoId}" />
  </c:if>

  <%
  // AFFICHAGE des métadonnées
  if (metaDataKeys != null && !metaDataKeys.isEmpty()) {
%>
  <div class="metadata bgDegradeGris" id="metadata">
    <div class="header bgDegradeGris">
      <h4 class="clean"><fmt:message key="GML.metadata"/></h4>
    </div>
    <div id="metadata_list">
    <%
          MetaData metaData;
          for (final String propertyLong : metaDataKeys) {
            metaData = photo.getMetaData(propertyLong);
            String mdLabel = metaData.getLabel();
            String mdValue = metaData.getValue();
            if (metaData.isDate()) {
              mdValue = resource.getOutputDateAndHour(metaData.getDateValue());
            }
            %>
        <p id="metadata_<%=mdLabel%>"><%=mdLabel%> <b><%=mdValue%></b></p>
            <%
          }
        %>
    </div>
  </div>
  <%
    }
    %>
</div>
<div class="principalContent">
  <div id="pagination">
    <c:if test="${requestScope.Rang ne 0}">
      <fmt:message var="previousPicture" key="gallery.previous"/>
      <a id="previousButton" href="PreviousMedia">
        <img alt="${previousPicture}" title="${previousPicture}" src="${previousIconUrl}" />
      </a>
    </c:if>
    <span class="txtnav"><span class="currentPage"><%=rang.intValue()+1%></span> / <%=albumSize.intValue()%></span>
    <c:if test="${requestScope.Rang ne (requestScope.NbMedia - 1)}">
      <fmt:message var="nextPicture" key="gallery.next"/>
      <a id="nextButton" href="NextMedia"><img alt="${nextPicture}" title="${nextPicture}" src="${nextIconUrl}"/></a>
    </c:if>
  </div>
  <div class="contentMedia">
    <div class="fondPhoto">
      <div class="cadrePhoto">
        <a href="#" onclick="javascript:startSlideshow('${curPhoto.id}')">
        <%
          if (!photo.isPreviewable()) {
            preview_url = m_context+"/gallery/jsp/icons/notAvailable_"+resource.getLanguage()+"_" + sizeParam + ".jpg";
          }
          if ( preview_url != null )
          {
            %>
          <img alt="${curPhoto.name}" src="<%=preview_url%>"/>
       <% } %>
        </a>
      </div>
      <c:if test="${curPhoto.title != curPhoto.fileName}">
        <h2 class="mediaTitle">${curPhoto.title}</h2>
      </c:if>
      <c:if test="${not empty curPhoto.keyWord}">
        <div class="motsClefs">
        <c:set var="listKeys" value="${fn:split(curPhoto.keyWord,' ')}"/>
        <c:forEach items="${listKeys}" var="keyword">
          <span><a href="SearchKeyWord?SearchKeyWord=${keyword}">${keyword}</a></span>
        </c:forEach>
        </div>
      </c:if>
      <c:if test="${not empty curPhoto.description}">
        <p class="description">${curPhoto.description}</p>
      </c:if>
    </div>
  </div>

  <%
  if (xmlForm != null) {
  %>
  <br/>
    <%
      PagesContext xmlContext = new PagesContext("myForm", "0", resource.
          getLanguage(), false, componentId, gallerySC.getUserId(), gallerySC.
          getAlbum(gallerySC.getCurrentAlbumId()).getNodePK().getId());
      xmlContext.setObjectId(photoId);
      xmlContext.setBorderPrinted(false);
      xmlContext.setIgnoreDefaultValues(true);

      xmlForm.display(out, xmlContext, xmlData);
    %>
<% } %>

</div>
</form>
<div class="principalContent">
  <c:if test="${requestScope.ShowCommentsTab}">
    <view:comments  userId="${userId}" componentId="${instanceId}"
                    resourceType="${photoResourceType}" resourceId="${photoId}" indexed="${callback}"/>
  </c:if>
</div>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>