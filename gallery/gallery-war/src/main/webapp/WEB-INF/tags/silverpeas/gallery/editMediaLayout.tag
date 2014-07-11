<%@ tag import="com.silverpeas.form.DataRecord" %>
<%@ tag import="com.silverpeas.form.Form" %>
<%@ tag import="com.silverpeas.form.PagesContext" %>
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
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="gallery" tagdir="/WEB-INF/tags/silverpeas/gallery" %>

<c:set var="_userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="_userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<jsp:useBean id="componentId" type="java.lang.String"/>
<c:set var="userId" value="${sessionScope.SilverSessionController.userId}"/>
<jsp:useBean id="userId" type="java.lang.String"/>

<%-- Fragments --%>
<%@ attribute name="headerBloc" fragment="true"
              description="Fragment to put additional things into HTML HEAD tag" %>
<%@ attribute name="mediaPreviewBloc" required="true" fragment="true"
              description="Fragment to put the display of the media" %>

<%-- Attributes --%>
<%@ attribute name="mediaType" required="true" type="com.silverpeas.gallery.constant.MediaType"
              description="A type of media to create/update." %>
<%@ attribute name="supportedMediaMimeTypes" required="true"
              type="java.util.Set"
              description="Supported media types." %>
<jsp:useBean id="supportedMediaMimeTypes" type="java.util.Set<com.silverpeas.gallery.constant.MediaMimeType>"/>

<%-- Request attributes --%>
<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>
<c:set var="media" value="${requestScope.Media}" scope="request"/>
<jsp:useBean id="media" type="com.silverpeas.gallery.model.Media" scope="request"/>
<c:set var="isNewMediaCase" value="${empty media.id}" scope="request"/>
<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>

<c:set var="action" value="CreateMedia"/>
<c:set var="bodyCss" value="createMedia"/>
<c:if test="${not isNewMediaCase}">
  <c:set var="action" value="UpdateInformation"/>
  <c:set var="bodyCss" value="editMedia"/>
</c:if>
<c:set var="albumPath" value="${requestScope.Path}"/>
<jsp:useBean id="albumPath" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>
<c:set var="albumId" value="${albumPath[fn:length(albumPath)-1].nodePK.id}"/>
<jsp:useBean id="albumId" type="java.lang.String"/>

<%-- Variables --%>
<c:set value="${media.applicationOriginalUrl}" var="mediaUrl" scope="request"/>

<%-- Actions --%>
<c:set var="viewMediaAction" value="MediaView?MediaId=${media.id}"/>

<%
  // paramètres pour le formulaire
  Form formUpdate = (Form) request.getAttribute("Form");
  DataRecord data = (DataRecord) request.getAttribute("Data");

  PagesContext context =
      new PagesContext("myForm", "0", _userLanguage, false, componentId, null);
  context.setBorderPrinted(false);
  context.setCurrentFieldIndex("11");
  context.setIgnoreDefaultValues(true);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title></title>
  <view:looknfeel/>
  <view:includePlugin name="qtip"/>
  <link type="text/css" href="<c:url value="/util/styleSheets/fieldset.css" />" rel="stylesheet"/>
  <%
    if (formUpdate != null) {
      formUpdate.displayScripts(out, context);
    }
  %>
  <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
  <jsp:invoke fragment="headerBloc"/>
</head>
<body class="gallery ${bodyCss} yui-skin-sam" id="${instanceId}">
<c:choose>
  <c:when test="${isNewMediaCase}">
    <fmt:message key="gallery.${fn:toLowerCase(mediaType)}.add" var="browseBarLabel"/>
    <c:set var="additionalBrowseBarElements" value="${browseBarLabel}@#"/>
  </c:when>
  <c:otherwise>
    <fmt:message key="GML.modify" var="modifyLabel"/>
    <c:set var="additionalBrowseBarElements" value="${silfn:truncate(media.title, 50)}@${viewMediaAction}"/>
    <c:set var="additionalBrowseBarElements" value="${additionalBrowseBarElements}|${modifyLabel}@#"/>
  </c:otherwise>
</c:choose>
<c:if test="${not isNewMediaCase}">
</c:if>
<gallery:browseBar albumPath="${albumPath}" additionalElements="${additionalBrowseBarElements}" />
<view:window>
  <c:if test="${not isNewMediaCase}">
    <view:tabs>
      <fmt:message key="gallery.media" var="mediaViewLabel"/>
      <view:tab label="${mediaViewLabel}" action="${viewMediaAction}" selected="false"/>
      <fmt:message key="gallery.info" var="mediaEditLabel"/>
      <view:tab label="${mediaEditLabel}" action="#" selected="true"/>
      <fmt:message key="gallery.accessPath" var="accessLabel"/>
      <view:tab label="${accessLabel}" action="AccessPath?MediaId=${media.id}" selected="false"/>
    </view:tabs>
  </c:if>
  <view:frame>
    <form name="mediaForm" action="${action}" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
      <input type="hidden" name="MediaId" value="${media.id}"/>
      <input type="hidden" name="type" value="${mediaType}"/>
      <input type="hidden" name="Positions"/>

      <table cellpadding="5" width="100%">
        <tr>
          <td valign="top">
            <c:if test="${not isNewMediaCase}">
              <jsp:invoke fragment="mediaPreviewBloc"/>
            </c:if>
          </td>
          <td valign="top">

            <gallery:editMedia media="${media}"
                               mediaType="${mediaType}"
                               supportedMediaMimeTypes="${supportedMediaMimeTypes}"
                               formUpdate="<%=formUpdate%>"
                               isUsePdc="${requestScope.IsUsePdc}"/>

            <c:if test="${requestScope.IsUsePdc}">
              <%-- Display PDC form --%>
              <c:choose>
                <c:when test="${not isNewMediaCase}">
                  <view:pdcClassification componentId="${instanceId}" contentId="${media.id}" editable="true"/>
                </c:when>
                <c:otherwise>
                  <view:pdcNewContentClassification componentId="${instanceId}"/>
                </c:otherwise>
              </c:choose>
            </c:if>

            <br/>
            <% if (formUpdate != null) { %>
              <%-- Display XML form --%>
            <fieldset id="formInfo" class="skinFieldset">
              <legend><fmt:message key="GML.bloc.further.information"/></legend>
              <%
                formUpdate.display(out, context, data);
              %>
            </fieldset>
            <% } %>
          </td>
        </tr>
      </table>
    </form>

    <fmt:message key="GML.validate" var="validateLabel"/>
    <fmt:message key="GML.cancel" var="cancelLabel"/>
    <view:buttonPane>
      <view:button action="javascript:onClick=sendData();" label="${validateLabel}"/>
      <c:choose>
        <c:when test="${not isNewMediaCase}">
          <view:button action="MediaView?MediaId=${media.id}" label="${cancelLabel}"/>
        </c:when>
        <c:otherwise>
          <view:button action="GoToCurrentAlbum" label="${cancelLabel}"/>
        </c:otherwise>
      </c:choose>
    </view:buttonPane>

  </view:frame>
</view:window>
<div id="tipDiv" style="position:absolute; visibility:hidden; z-index:100000"></div>
</body>
</html>
