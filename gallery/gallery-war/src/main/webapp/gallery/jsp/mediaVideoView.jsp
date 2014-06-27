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
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="viewTags" %>

<%-- Set resource bundle --%>
<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<fmt:message var="commentTab" key="gallery.comments"/>

<c:set var="video" value="${requestScope.Media}"/>
<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="photoResourceType" value="${video.contributionType}"/>
<c:set var="photoId" value="${video.id}"/>
<c:set var="callback">function( event ) { if (event.type === 'listing') { commentCount = event.comments.length; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + event.comments.length + ')'); } else if (event.type === 'deletion') { commentCount--; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } else if (event.type === 'addition') { commentCount++; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } }</c:set>
<c:set var="albumPath" value="${requestScope.Path}" />
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>



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
<jsp:useBean id="now" class="java.util.Date" />

<%
  // récupération des paramètres :
  Media photo = (Media) request.getAttribute("Media");
  List<NodeDetail> path = (List<NodeDetail>) request.getAttribute("Path");
  String profile = (String) request.getAttribute("Profile");
  Integer rang = (Integer) request.getAttribute("Rang");
  Integer albumSize = (Integer) request.getAttribute("NbMedia");
  boolean updateAllowed = (Boolean) request.getAttribute("UpdateMediaAllowed");
  boolean isBasket = (Boolean) request.getAttribute("IsBasket");
  boolean isPrivateSearch = (Boolean) request.getAttribute("IsPrivateSearch");

  // paramètres du formulaire
  Form xmlForm = (Form) request.getAttribute("XMLForm");
  DataRecord xmlData = (DataRecord) request.getAttribute("XMLData");

  // déclaration des variables :
  String title = photo.getTitle();
  String photoId = photo.getMediaPK().getId();

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

<%-- <viewTags:browseBar albumPath="${albumPath}"/>  --%>


  <%
    browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "Main");
    displayPath(path, browseBar);

    String url = "ToAlertUser?MediaId=" + photoId;
    operationPane.addOperation(resource.getIcon("gallery.alert"), resource.getString("GML.notify"),
        "javaScript:onClick=goToNotify('" + url + "')");
    operationPane.addLine();

    if (updateAllowed) {
      operationPane.addOperation(resource.getIcon("gallery.deletePhoto"), resource.getString(
          "gallery.deletePhoto"), "javaScript:deleteConfirm('" + photoId + "','" + EncodeHelper.
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
      operationPane.addOperation(resource.getIcon("gallery.addPhotoToBasket"), resource.getString(
          "gallery.addPhotoToBasket"), "BasketAddMedia?MediaId=" + photoId);
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
      <c:when test="${requestScope.ViewLinkDownload or video.downloadable}">
        <a href="${video.originalUrl}" target="_blank">${video.name} <img src="${downloadIconUrl}" alt="<fmt:message key='gallery.download.photo'/>" title="<fmt:message key='gallery.originale'/>"/></a>
      </c:when>
      <c:otherwise>
        ${video.fileName} <img src="${downloadForbiddenIconUrl}" alt="<fmt:message key='gallery.download.forbidden'/>" title="<fmt:message key='gallery.download.forbidden'/>" class="forbidden-download-file"/>
      </c:otherwise>
    </c:choose>
  </div>
  <%-- TODO display video characteristic dimension and bitrate -->
  <div class="fileCharacteristic  bgDegradeGris">
    <p>
      <span class="fileCharacteristicWeight"><fmt:message key="gallery.weight" /> <b>TODO</b></span>
      <span class="fileCharacteristicSize"><fmt:message key="gallery.dimension" /> <b>TODO <fmt:message key="gallery.pixels" /></b></span> <br class="clear" />
    </p>
  </div>
  --%>
  <c:set var="createDate" value="${video.creationDate}"/>
  <c:set var="lastUpdateDate" value="${video.lastUpdateDate}"/>

  <div class="bgDegradeGris" id="suggestionInfoPublication">
  <c:if test="${not empty video.author}">
    <p id="authorInfo"><b><fmt:message key="GML.author"/></b> ${video.author}
    </p>
  </c:if>
  <c:if test="${not empty video.lastUpdater}">
    <div class="paragraphe" id="infoModification"> <b><fmt:message key="GML.updatedAt"/></b>${silfn:formatDate(lastUpdateDate, _language)} <fmt:message key="GML.by"/>
      <view:username userId="${video.lastUpdater.id}"/>
      <div class="profilPhoto"><img src='<c:url value="${video.lastUpdater.avatar}" />' alt="" class="defaultAvatar"/></div>
    </div>
  </c:if>

  <c:if test="${not empty video.creator}">
    <div class="paragraphe" id="infoCreation">
      <b><fmt:message key="GML.createdAt"/></b>${silfn:formatDate(createDate, _language)} <fmt:message key="GML.by"/>
      <view:username userId="${video.creator.id}"/>
      <div class="profilPhoto"><img src='<c:url value="${video.creator.avatar}" />' alt="" class="defaultAvatar"/></div>
    </div>
  </c:if>
    <p id="permalinkInfo">
      <fmt:message key="gallery.CopyPhotoLink" var="cpPhotoLinkAlt"/>
      <a title="${cpPhotoLinkAlt}" href="${video.permalink}">
        <img border="0" alt='${cpPhotoLinkAlt}' title='${cpPhotoLinkAlt}' src="${permalinkIconUrl}" />
      </a> <fmt:message key="GML.permalink"/> <br />
      <input type="text" value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${video.permalink}" onfocus="select();" class="inputPermalink" />
    </p>
  </div>
  <c:if test="${video.downloadable and (video.visibilityPeriod.defined or video.downloadPeriod.defined)}">
    <div class="periode bgDegradeGris" id="periode">
      <div class="header bgDegradeGris">
        <h4 class="clean"><fmt:message key="GML.period"/></h4>
      </div>
      <c:if test="${video.visibilityPeriod.defined}">
        <div class="periode_visibility paragraphe">
          <fmt:message key="gallery.beginDate"/>
          <b>
            <c:if test="${video.visibilityPeriod.beginDatable.defined}">
              <view:formatDate value="${video.visibilityPeriod.beginDate}" language="${language}" />
            </c:if>
          </b>
          <b><fmt:message key="GML.toDate"/>
            <c:if test="${video.visibilityPeriod.endDatable.defined}">
              <view:formatDate value="${video.visibilityPeriod.endDate}" language="${language}" />
            </c:if>
          </b>
        </div>
      </c:if>
      <c:if test="${video.downloadPeriod.defined}">
        <div class="periode_download paragraphe">
          <fmt:message key="gallery.beginDownloadDate"/>
          <b>
            <c:if test="${video.downloadPeriod.beginDatable.defined}">
              <view:formatDate value="${video.downloadPeriod.beginDate}" language="${language}" />
            </c:if>
          </b>
          <b><fmt:message key="GML.toDate"/>
            <c:if test="${video.downloadPeriod.endDatable.defined}">
              <view:formatDate value="${video.downloadPeriod.endDate}" language="${language}" />
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

</div>
<div class="principalContent">
  <div id="pagination">
    <c:if test="${requestScope.Rang ne 0}">
      <fmt:message var="previousPicture" key="gallery.previous"/>
      <a id="previousButton" href="PreviousMedia">
        <img alt="${previousPicture}" title="${previousPicture}" src="${previousIconUrl}" />
      </a>
    </c:if>
    <span class="txtnav"><span class="currentPage">${requestScope.Rang + 1}</span> / ${requestScope.NbMedia}</span>
    <c:if test="${requestScope.Rang ne (requestScope.NbMedia - 1)}">
      <fmt:message var="nextPicture" key="gallery.next"/>
      <a id="nextButton" href="NextMedia"><img alt="${nextPicture}" title="${nextPicture}" src="${nextIconUrl}"/></a>
    </c:if>
  </div>
  <div class="contentMedia">
    <div class="fondPhoto">
      <div class="cadrePhoto">
        <c:url value="/services/gallery/${instanceId}/albums/${albumPath[fn:length(albumPath)-1].nodePK.id}/videos/${video.id}?_t=${now.time}" var="videoUrl"/>
        <view:video url="${videoUrl}"></view:video>
      </div>
      <c:if test="${video.title != video.name}">
        <h2 class="mediaTitle">${video.title}</h2>
      </c:if>
      <c:if test="${not empty video.keyWord}">
        <div class="motsClefs">
        <c:set var="listKeys" value="${fn:split(video.keyWord,' ')}"/>
        <c:forEach items="${listKeys}" var="keyword">
          <span><a href="SearchKeyWord?SearchKeyWord=${keyword}">${keyword}</a></span>
        </c:forEach>
        </div>
      </c:if>
      <c:if test="${not empty video.description}">
        <p class="description">${video.description}</p>
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


  <c:if test="${requestScope.ShowCommentsTab}">
    <view:comments  userId="${userId}" componentId="${instanceId}"
              resourceType="${photoResourceType}" resourceId="${photoId}" indexed="${callback}"/>
  </c:if>
</div>

</form>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>