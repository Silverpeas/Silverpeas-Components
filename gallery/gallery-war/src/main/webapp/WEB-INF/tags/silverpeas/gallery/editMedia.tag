<%@ tag import="com.stratelia.webactiv.beans.admin.UserDetail" %>
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
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/gallery" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>
<view:setConstant var="MediaTypeStreaming" constant="com.silverpeas.gallery.constant.MediaType.Streaming"/>
<c:set var="mandatoryIcon"><fmt:message key='gallery.mandatory' bundle='${icons}'/></c:set>

<%-- Media --%>
<%@ attribute name="mediaBean" required="false" type="com.silverpeas.gallery.model.Media"
              description="A media bean (Media.java). The label of the current value is handled." %>
<%@ attribute name="mediaType" required="true" type="java.lang.String"
              description="A type of media to create/update." %>

  <fieldset id="${mediaType}Info" class="skinFieldset">
    <legend><fmt:message key="GML.bloc.information.principals"/>  </legend>
    <div class="fields">
      <div class="field" id="fileArea">
        <label for="WAIMGVAR0" class="txtlibform"><fmt:message key="gallery.media" /></label>
        <div class="champs">
          <%--TODO choose if media is Streaming display input type text else display input type file --%>
          <c:choose>
            <c:when test="${mediaType eq 'Streaming'}">
          <input id="fileId" type="file" name="streaming" size="60" />&nbsp;<img alt="<fmt:message key="GML.mandatory" />" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
            </c:when>
            <c:otherwise>
          <input id="fileId" type="file" name="WAIMGVAR0" size="60" />&nbsp;<img alt="<fmt:message key="GML.mandatory" />" src="<c:url value='${mandatoryIcon}'/>" width="5" height="5"/>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
      <div class="field" id="fileNameArea">
        <label for="fileName" class="txtlibform"><fmt:message key="gallery.fileName" /></label>
        <div class="champs">
          ${mediaBean.fileName}
        </div>
      </div>
      <div class="field" id="titleArea">
        <label for="title" class="txtlibform"><fmt:message key="GML.title"/></label>
        <div class="champs">
          <c:if test="${mediaBean.title != mediaBean.fileName}">
            <c:set var="mediaTitle" value="${mediaBean.title}"/>
          </c:if>
          <input id="title" type="text" name="SP$$MediaTitle" size="60" maxlength="150" value="${mediaTitle}"/>&nbsp;
        </div>
      </div>
      <div class="field" id="descriptionArea">
        <label for="description" class="txtlibform"><fmt:message key="GML.description"/></label>
        <div class="champs">
          <input id="description" type="text" name="SP$$MediaDescription" size="60" maxlength="150" value="<c:out value='${mediaBean.description}'/>"/>&nbsp;
        </div>
      </div>
      <div class="field" id="authorArea">
        <label for="author" class="txtlibform"><fmt:message key="GML.author"/></label>
        <div class="champs">
          <input id="author" type="text" name="SP$$MediaAuthor" size="60" maxlength="150" value="<c:out value='${mediaBean.author}'/>"/>&nbsp;
        </div>
      </div>
      <div class="field" id="keywordArea">
        <label for="keyword" class="txtlibform"><fmt:message key="gallery.keyword"/></label>
        <div class="champs">
          <input id="keyword" type="text" name="SP$$MediaKeyWord" size="60" maxlength="150" value="<c:out value='${mediaBean.keyWord}'/>"/>&nbsp;
        </div>
      </div>
    </div>
  </fieldset>

  <fieldset class="skinFieldset" id="${mediaType}Options">
    <legend><fmt:message key="gallery.options" /></legend>
    <div class="fields">

    <c:if test="${not empty mediaBean.internalMedia or (empty mediaBean and mediaType ne 'Streaming')}">
      <div class="field" id="downloadArea">
        <label for="download" class="txtlibform">
          <c:choose>
            <c:when test="${mediaType eq 'Photo'}">
              <fmt:message key="gallery.download"/>
            </c:when>
            <c:otherwise>
              <fmt:message key="gallery.video.download"/>
            </c:otherwise>
          </c:choose>
        </label>
        <div class="champs">
          <c:set var="downloadChecked" value=""/>
          <c:if test="${mediaBean.downloadable}">
            <c:set var="downloadChecked" value="checked=\"checked\""/>
          </c:if>
          <input id="download" type="checkbox" name="SP$$MediaDownloadAuthorized" value="true" ${downloadChecked} />
        </div>
      </div>

      <c:set var="beginDownloadDate">
        <c:if test="${mediaBean.internalMedia.downloadPeriod.beginDatable.defined}">
          <view:formatDate value="${mediaBean.internalMedia.downloadPeriod.beginDate}" language="${_language}" />
        </c:if>
      </c:set>

      <div class="field" id="beginDownloadDateArea">
        <label for="beginDownloadDate" class="txtlibform"><fmt:message key="gallery.beginDownloadDate"/></label>
        <div class="champs">
          <input id="beginDownloadDate" type="text" class="dateToPick" name="SP$$MediaBeginDownloadDate" size="12" maxlength="10" value="${beginDownloadDate}"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>

      <c:set var="endDownloadDate">
        <c:if test="${mediaBean.internalMedia.downloadPeriod.endDatable.defined}">
          <view:formatDate value="${mediaBean.internalMedia.downloadPeriod.endDate}" language="${_language}" />
        </c:if>
      </c:set>

      <div class="field" id="endDownloadDateArea">
        <label for="endDownloadDate" class="txtlibform"><fmt:message key="GML.toDate"/></label>
        <div class="champs">
          <input id="endDownloadDate" type="text" class="dateToPick" name="SP$$MediaEndDownloadDate" size="12" maxlength="10" value="${endDownloadDate}"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
    </c:if>

      <c:set var="beginDate">
        <c:if test="${mediaBean.visibilityPeriod.beginDatable.defined}">
          <view:formatDate value="${mediaBean.visibilityPeriod.beginDate}" language="${_language}" />
        </c:if>
      </c:set>
      <div class="field" id="beginDateArea">
        <label for="beginDate" class="txtlibform"><fmt:message key="gallery.beginDate"/></label>
        <div class="champs">
          <input id="beginDate" type="text" class="dateToPick" name="SP$$MediaBeginVisibilityDate" size="12" maxlength="10" value="${beginDate}"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>

      <c:set var="endDate">
        <c:if test="${mediaBean.visibilityPeriod.endDatable.defined}">
          <view:formatDate value="${mediaBean.visibilityPeriod.endDate}" language="${_language}" />
        </c:if>
      </c:set>
      <div class="field" id="endDateArea">
        <label for="endDate" class="txtlibform"><fmt:message key="GML.toDate"/></label>
        <div class="champs">
          <input id="endDate" type="text" class="dateToPick" name="SP$$MediaEndVisibilityDate" size="12" maxlength="10" value="${endDate}"/>
          <span class="txtnote">(<fmt:message key='GML.dateFormatExemple'/>)</span>
        </div>
      </div>
    </div>
  </fieldset>
