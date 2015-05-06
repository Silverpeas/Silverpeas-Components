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

<view:setConstant var="MediaTitleInputName" constant="com.silverpeas.gallery.ParameterNames.MediaTitle"/>
<view:setConstant var="MediaDescriptionInputName" constant="com.silverpeas.gallery.ParameterNames.MediaDescription"/>
<view:setConstant var="MediaBeginDownloadDateInputName" constant="com.silverpeas.gallery.ParameterNames.MediaBeginDownloadDate"/>
<view:setConstant var="MediaEndDownloadDateInputName" constant="com.silverpeas.gallery.ParameterNames.MediaEndDownloadDate"/>
<view:setConstant var="MediaBeginVisibilityDateInputName" constant="com.silverpeas.gallery.ParameterNames.MediaBeginVisibilityDate"/>
<view:setConstant var="MediaEndVisibilityDateInputName" constant="com.silverpeas.gallery.ParameterNames.MediaEndVisibilityDate"/>
<view:setConstant var="MediaTypePhoto" constant="com.silverpeas.gallery.constant.MediaType.Photo"/>
<view:setConstant var="MediaTypeStreaming" constant="com.silverpeas.gallery.constant.MediaType.Streaming"/>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<jsp:useBean id="componentId" type="java.lang.String"/>

<%-- Default values --%>
<c:set var="_formName" value="mediaForm"/>

<%@ attribute name="formName" required="false"
              type="java.lang.String"
              description="The name of the HTML form TAG ('mediaForm' by default)." %>
<c:if test="${formName != null}">
  <c:set var="_formName" value="${formName}"/>
</c:if>

<%-- Component parameters --%>
<view:componentParam var="defaultDownload" componentId="${componentId}" parameter="download"/>
<c:set var="defaultDownload" value="${silfn:booleanValue(defaultDownload)}"/>

<%-- Media --%>
<%@ attribute name="media" required="true" type="com.silverpeas.gallery.model.Media"
              description="A media bean (Media.java). The label of the current value is handled." %>
<%@ attribute name="mediaType" required="true" type="com.silverpeas.gallery.constant.MediaType"
              description="A type of media to create/update." %>
<%@ attribute name="isUsePdc" required="true" type="java.lang.Boolean"
              description="Indicates if PDC is used or not." %>
<%@ attribute name="formUpdate" required="true" type="com.silverpeas.form.Form"
              description="Instance of a form." %>
<%@ attribute name="supportedMediaMimeTypes" required="true"
              type="java.util.Set"
              description="Supported media types." %>
<%@ attribute name="isUpdateMediaFromAlbumCase" required="true"
              type="java.lang.Boolean"
              description="Indicates if the user comes directly from an album (instead the view of a media)." %>
<c:if test="${not empty supportedMediaMimeTypes}">
  <jsp:useBean id="supportedMediaMimeTypes" type="java.util.Set<com.silverpeas.gallery.constant.MediaMimeType>"/>
</c:if>

<c:set var="isNewMediaCase" value="${empty media.id}"/>
<c:set var="internalMedia" value="${media.internalMedia}"/>

<script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js" />"></script>
<script type="text/javascript">

  // Form validation
  function sendData() {
    <c:choose>
    <c:when test="${formUpdate != null}">
    if (isCorrectForm() && isCorrectLocalForm()) {
      </c:when>
      <c:otherwise>
      if (isCorrectLocalForm()) {
        </c:otherwise>
        </c:choose>
        <c:if test="${isUsePdc and isNewMediaCase}">
        <view:pdcPositions setIn="document.${_formName}.Positions.value"/>
        </c:if>
        $.progressMessage();
        document.${_formName}.submit();
      }
    }

    function isCorrectLocalForm() {
      var errorMsg = "";
      var errorNb = 0;
      var title = stripInitialWhitespace(document.${_formName}.${MediaTitleInputName}.value);
      var descr = document.${_formName}.${MediaDescriptionInputName}.value;
      var media = stripInitialWhitespace(document.${_formName}.${mediaType eq MediaTypeStreaming ? 'SP$$StreamingHomepageUrl' : 'WAIMGVAR0'}.value);

      <c:choose>
      <c:when test="${mediaType ne MediaTypeStreaming}">

      <c:if test="${not empty supportedMediaMimeTypes}">
      <c:set var="supportedMediaTypeRegExpr" value=""/>
      <c:forEach var="supportedMediaType" items="${supportedMediaMimeTypes}">
      <c:forEach var="supportedMediaExtension" items="${supportedMediaType.extensions}">
      <c:if test="${not empty supportedMediaTypeRegExpr}">
      <c:set var="supportedMediaTypeRegExpr" value="${supportedMediaTypeRegExpr}|"/>
      </c:if>
      <c:set var="supportedMediaTypeRegExpr" value="${supportedMediaTypeRegExpr}${supportedMediaExtension}"/>
      </c:forEach>
      </c:forEach>
      if (media && media.length > 0) {
        var fileRegExprCheck = /[.](${supportedMediaTypeRegExpr})$/;
        if (fileRegExprCheck.exec(media.toLowerCase()) == null) {
          errorMsg +=
              "<li>'<fmt:message key="gallery.${fn:toLowerCase(mediaType)}"/>' <fmt:message key="gallery.format"/></li>";
          errorNb++;
        }
      }
      </c:if>

      </c:when>
      <c:otherwise>

      if (!detectSupportedProvider(media)) {
        errorMsg +=
            "<li>'<fmt:message key="gallery.${fn:toLowerCase(mediaType)}"/>' <fmt:message key="gallery.streaming.format"/></li>";
        errorNb++;
      }

      </c:otherwise>
      </c:choose>

      if (title.length > 255) {
        errorMsg +=
            "<li>'<fmt:message key="GML.title"/>'  <fmt:message key="gallery.MsgSize"/></li>";
        errorNb++;
      }
      if (descr.length > 255) {
        errorMsg +=
            "<li>'<fmt:message key="GML.description"/>'  <fmt:message key="gallery.MsgSize"/></li>";
        errorNb++;
      }
      <c:if test="${isNewMediaCase}">
      if (media == "") {
        errorMsg +=
            "<li>'<fmt:message key="gallery.${fn:toLowerCase(mediaType)}"/>'  <fmt:message key="GML.MustBeFilled"/></li>";
        errorNb++;
      }
      </c:if>

      var dateErrors;
      <c:if test="${mediaType ne MediaTypeStreaming}">
      // Download period
      var beginDownloadDate = {dateId : 'beginDownloadDate'};
      var endDownloadDate = {dateId : 'endDownloadDate', defaultDateHour : '23:59'};
      dateErrors = isPeriodEndingInFuture(beginDownloadDate, endDownloadDate);
      $(dateErrors).each(function(index, error) {
        errorMsg += "<li>" + error.message + "</li>";
        errorNb++;
      });
      </c:if>
      // Visibility period
      var beginVisibilityDate = {dateId : 'beginVisibilityDate'};
      var endVisibilityDate = {dateId : 'endVisibilityDate', defaultDateHour : '23:59'};
      dateErrors = isPeriodEndingInFuture(beginVisibilityDate, endVisibilityDate);
      $(dateErrors).each(function(index, error) {
        errorMsg += "<li>" + error.message + "</li>";
        errorNb++;
      });

      <c:if test="${isUsePdc and isNewMediaCase}">
      <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg" errorWebRender="true"/>;
      </c:if>

      var result = true;
      switch (errorNb) {
        case 0 :
          break;
        case 1 :
          errorMsg =
              "<b><fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : </b><ul>" +
              errorMsg + "</ul>";
          notyError(errorMsg);
          result = false;
          break;
        default :
          errorMsg = "<b><fmt:message key="GML.ThisFormContains"/> " + errorNb +
              " <fmt:message key="GML.errors"/> :</b><ul>" + errorMsg + "</ul>";
          notyError(errorMsg);
          result = false;
          break;
      }
      return result;
    }

</script>

<input type="hidden" name="isUpdateMediaFromAlbumCase" value="${isUpdateMediaFromAlbumCase}">
<fieldset id="${fn:toLowerCase(mediaType)}Info" class="skinFieldset">
  <legend><fmt:message key="GML.bloc.information.principals"/></legend>
  <div class="fields">
    <div class="field" id="${mediaType eq MediaTypeStreaming ? 'urlArea' : 'fileArea'}">
      <label for="fileId" class="txtlibform"><fmt:message key="gallery.${fn:toLowerCase(mediaType)}"/>${mediaType eq MediaTypeStreaming ? ' (URL)' : ''}</label>

      <div class="champs">
        <%--TODO choose if media is Streaming display input type text else display input type file --%>
        <c:choose>
          <c:when test="${mediaType eq MediaTypeStreaming}">
            <fmt:message var="fileIdHelp" key="gallery.streaming.field.help" />
            <input id="fileId" type="text" name="SP$$StreamingHomepageUrl" size="60" maxlength="150" value="${media.streaming.homepageUrl}" title="${fileIdHelp}" />
          </c:when>
          <c:otherwise>
            <input id="fileId" type="file" name="WAIMGVAR0" size="60"/>
          </c:otherwise>
        </c:choose>
        &nbsp;<img alt="<fmt:message key="GML.mandatory"/>" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="fileNameArea">
      <label class="txtlibform"><fmt:message key="gallery.fileName"/></label>

      <div class="champs">
        <c:out value="${not empty internalMedia ? internalMedia.fileName : ''}"/>
      </div>
    </div>
    <div class="field" id="titleArea">
      <label for="title" class="txtlibform"><fmt:message key="GML.title"/></label>

      <div class="champs">
        <c:if test="${media.title != (not empty internalMedia ? internalMedia.fileName : '')}">
          <c:set var="mediaTitle"><c:out value="${media.title}"/></c:set>
        </c:if>
        <input id="title" type="text" name="SP$$MediaTitle" size="60" maxlength="150" value="${mediaTitle}"/>&nbsp;
      </div>
    </div>
    <div class="field" id="descriptionArea">
      <label for="description" class="txtlibform"><fmt:message key="GML.description"/></label>

      <div class="champs">
        <input id="description" type="text" name="SP$$MediaDescription" size="60" maxlength="150" value="<c:out value='${media.description}'/>"/>&nbsp;
      </div>
    </div>
    <div class="field" id="authorArea">
      <label for="author" class="txtlibform"><fmt:message key="GML.author"/></label>

      <div class="champs">
        <input id="author" type="text" name="SP$$MediaAuthor" size="60" maxlength="150" value="<c:out value='${media.author}'/>"/>&nbsp;
      </div>
    </div>
    <div class="field" id="keywordArea">
      <label for="keyword" class="txtlibform"><fmt:message key="gallery.keyword"/></label>

      <div class="champs">
        <input id="keyword" type="text" name="SP$$MediaKeyWord" size="60" maxlength="150" value="<c:out value='${media.keyWord}'/>"/>&nbsp;
      </div>
    </div>
  </div>
</fieldset>

<fieldset class="skinFieldset" id="${fn:toLowerCase(mediaType)}Options">
  <legend><fmt:message key="gallery.options"/></legend>
  <div class="fields">

    <c:if test="${not empty internalMedia or (isNewMediaCase and mediaType ne MediaTypeStreaming)}">
      <div class="field" id="downloadArea">
        <label for="download" class="txtlibform">
          <fmt:message key="gallery.${fn:toLowerCase(mediaType)}.download"/>
        </label>

        <div class="champs">
          <c:set var="downloadChecked" value=""/>
          <c:if test="${media.internalMedia.downloadAuthorized || (isNewMediaCase && defaultDownload)}">
            <c:set var="downloadChecked" value="checked=\"checked\""/>
          </c:if>
          <input id="download" type="checkbox" name="SP$$MediaDownloadAuthorized" value="true" ${downloadChecked} />
        </div>
      </div>

      <c:set var="beginDownloadDate">
        <c:if test="${internalMedia.downloadPeriod.beginDatable.defined}">
          <view:formatDate value="${internalMedia.downloadPeriod.beginDate}" language="${_language}"/>
        </c:if>
      </c:set>

      <div class="field" id="beginDownloadDateArea">
        <fmt:message key="gallery.beginDownloadDate" var="tmpDateLabel">
          <fmt:param value="${1}"/>
        </fmt:message>
        <label for="beginDownloadDate" class="txtlibform">${tmpDateLabel}</label>

        <div class="champs">
          <input id="beginDownloadDate" type="text" class="dateToPick" name="SP$$MediaBeginDownloadDate" size="12" maxlength="10" value="${beginDownloadDate}"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>

      <c:set var="endDownloadDate">
        <c:if test="${internalMedia.downloadPeriod.endDatable.defined}">
          <view:formatDate value="${internalMedia.downloadPeriod.endDate}" language="${_language}"/>
        </c:if>
      </c:set>

      <div class="field" id="endDownloadDateArea">
        <fmt:message key="gallery.endDownloadDate" var="tmpDateLabel">
          <fmt:param value="${1}"/>
        </fmt:message>
        <label for="endDownloadDate" class="txtlibform">${tmpDateLabel}</label>

        <div class="champs">
          <input id="endDownloadDate" type="text" class="dateToPick" name="SP$$MediaEndDownloadDate" size="12" maxlength="10" value="${endDownloadDate}"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
    </c:if>

    <c:set var="beginVisibilityDate">
      <c:if test="${media.visibilityPeriod.beginDatable.defined}">
        <view:formatDate value="${media.visibilityPeriod.beginDate}" language="${_language}"/>
      </c:if>
    </c:set>
    <div class="field" id="beginDateArea">
      <fmt:message key="gallery.beginDate" var="tmpDateLabel">
        <fmt:param value="${1}"/>
      </fmt:message>
      <label for="beginVisibilityDate" class="txtlibform">${tmpDateLabel}</label>

      <div class="champs">
        <input id="beginVisibilityDate" type="text" class="dateToPick" name="SP$$MediaBeginVisibilityDate" size="12" maxlength="10" value="${beginVisibilityDate}"/>
        <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
      </div>
    </div>

    <c:set var="endVisibilityDate">
      <c:if test="${media.visibilityPeriod.endDatable.defined}">
        <view:formatDate value="${media.visibilityPeriod.endDate}" language="${_language}"/>
      </c:if>
    </c:set>
    <div class="field" id="endDateArea">
      <fmt:message key="gallery.endDate" var="tmpDateLabel">
        <fmt:param value="${1}"/>
      </fmt:message>
      <label for="endVisibilityDate" class="txtlibform">${tmpDateLabel}</label>

      <div class="champs">
        <input id="endVisibilityDate" type="text" class="dateToPick" name="SP$$MediaEndVisibilityDate" size="12" maxlength="10" value="${endVisibilityDate}"/>
        <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
      </div>
    </div>
  </div>
</fieldset>
<view:progressMessage/>
