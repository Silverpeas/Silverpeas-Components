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
<%@ taglib prefix="gallery" tagdir="/WEB-INF/tags/silverpeas/gallery" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Request attributes --%>
<c:set var="selectedMediaIds" value="${requestScope.SelectedMediaIds}"/>
<c:set var="albumId" value="${requestScope.AlbumId}"/>
<c:set var="albumPath" value="${requestScope.Path}"/>
<jsp:useBean id="albumPath" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>
<c:set var="searchKeyWord" value="${requestScope.SearchKeyWord}"/>

<%-- Actions --%>
<c:set var="validateAction" value="javascript:onClick=sendData();"/>
<c:set var="cancelAction" value="${(not empty albumId and albumId != 'null') ? 'GoToCurrentAlbum?AlbumId='.concat(albumId) : 'SearchKeyWord?SearchKeyWord='.concat(searchKeyWord)}"/>

<%-- Labels --%>
<fmt:message key="GML.validate" var="validateLabel"/>
<fmt:message key="GML.cancel" var="cancelLabel"/>

<%
  // Pas de passage de l'objectId dans le contexte car on est en traitement par lot,
  // ce passage se fera lors de la validation du formulaire
  Form formUpdate = (Form) request.getAttribute("Form");
  DataRecord formData = (DataRecord) request.getAttribute("Data");
  PagesContext context =
      new PagesContext("mediaForm", "0", resource.getLanguage(), false, componentId,
          gallerySC.getUserId(),
          gallerySC.getAlbum(gallerySC.getCurrentAlbumId()).getNodePK().getId());
  context.setBorderPrinted(false);
  context.setCurrentFieldIndex("10");
  context.setUseBlankFields(true);
  context.setUseMandatory(false);
%>

<c:set var="formUpdate" value="<%=formUpdate%>"/>
<c:set var="formData" value="<%=formData%>"/>

<html>
<head>
  <view:looknfeel/>
  <view:includePlugin name="datepicker"/>
  <view:includePlugin name="wysiwyg"/>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
  <c:if test="${not empty formUpdate}">
    <%formUpdate.displayScripts(out, context);%>
  </c:if>
  <script type="text/javascript">

    // fonctions de contrôle des zones du formulaire avant validation
    function sendData() {
      <c:choose>
      <c:when test="${not empty formUpdate}">
      if (isCorrectHeaderForm() && isCorrectForm()) {
        document.mediaForm.submit();
      }
      </c:when>
      <c:otherwise>
      if (isCorrectHeaderForm()) {
        document.mediaForm.submit();
      }
      </c:otherwise>
      </c:choose>
    }

    function isCorrectHeaderForm() {
      var errorMsg = "";
      var errorNb = 0;
      var title = stripInitialWhitespace(document.mediaForm.Media$Title.value);
      var descr = document.mediaForm.Media$Description.value;

      if (title.length > 255) {
        errorMsg +=
            "<li>'<%=resource.getString("GML.title")%>'  <%=resource.getString("gallery.MsgSize")%></li>";
        errorNb++;
      }
      if (descr.length > 255) {
        errorMsg +=
            "<li>'<%=resource.getString("GML.description")%>'  <%=resource.getString("gallery.MsgSize")%></li>";
        errorNb++;
      }

      // Download period
      var beginDownloadDate = {dateId : 'beginDownloadDate'};
      var endDownloadDate = {dateId : 'endDownloadDate', defaultDateHour : '23:59'};
      var dateErrors = isPeriodEndingInFuture(beginDownloadDate, endDownloadDate);
      $(dateErrors).each(function(index, error) {
        errorMsg += "<li>" + error.message + "</li>";
        errorNb++;
      });
      // Visibility period
      var beginVisibilityDate = {dateId : 'beginVisibilityDate'};
      var endVisibilityDate = {dateId : 'endVisibilityDate', defaultDateHour : '23:59'};
      dateErrors = isPeriodEndingInFuture(beginVisibilityDate, endVisibilityDate);
      $(dateErrors).each(function(index, error) {
        errorMsg += "<li>" + error.message + "</li>";
        errorNb++;
      });

      var result = false;
      switch (errorNb) {
        case 0 :
          result = true;
          break;
        case 1 :
          errorMsg =
              "<b><%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : </b><ul>" +
              errorMsg + "</ul>";
          notyError(errorMsg);
          break;
        default :
          errorMsg = "<b><%=resource.getString("GML.ThisFormContains")%> " + errorNb +
              " <%=resource.getString("GML.errors")%> :</b><ul>" + errorMsg + "</ul>";
          notyError(errorMsg);
          break;
      }
      return result;
    }
  </script>

</head>
<body class="yui-skin-sam" onLoad="document.mediaForm.Media$Title.focus();">
<fmt:message key="gallery.updateSelectedMedia" var="updateSelectedMediaLabel"/>
<gallery:browseBar albumPath="${albumPath}" additionalElements="${updateSelectedMediaLabel} (${fn:length(selectedMediaIds)})@#"/>
<view:window>
  <view:frame>
    <form name="mediaForm" action="UpdateSelectedMedia" method="POST" enctype="multipart/form-data">
      <view:board>
        <table cellpadding="5">
          <tr>
            <td class="txtlibform"><fmt:message key="GML.title"/> :</td>
            <td><input type="text" name="Media$Title" size="60" maxlength="150" value="">
              <input type="hidden" name="Im$SearchKeyWord" value="${searchKeyWord}"></td>
          </tr>
          <tr>
            <td class="txtlibform"><fmt:message key="GML.description"/> :</td>
            <td>
              <input type="text" name="Media$Description" size="60" maxlength="150" value="">
            </td>
          </tr>
          <tr>
            <td class="txtlibform"><fmt:message key="GML.author"/> :</td>
            <td><input type="text" name="Media$Author" size="60" maxlength="150" value="">
            </td>
          </tr>
          <tr>
            <td class="txtlibform"><fmt:message key="gallery.keyword"/> :</td>
            <td><input type="text" name="Media$KeyWord" size="60" maxlength="150" value="">
            </td>
          </tr>
          <tr>
            <td class="txtlibform"><fmt:message key="gallery.download"/> :</td>
            <td>
              <input type="checkbox" name="Media$DownloadAuthorized" value="true">
            </td>
          </tr>
          <tr>
            <td class="txtlibform">
              <fmt:message key="gallery.beginDownloadDate" var="tmpDateLabel">
                <fmt:param value="${1}"/>
              </fmt:message>
              <label for="beginDownloadDate">${tmpDateLabel} :</label>
            </td>
            <td>
              <input type="text" class="dateToPick" id="beginDownloadDate" name="Media$BeginDownloadDate" size="12" maxlength="10" value=""/>
            </td>
          </tr>
          <tr>
            <td class="txtlibform">
              <fmt:message key="gallery.endDownloadDate" var="tmpDateLabel">
                <fmt:param value="${1}"/>
              </fmt:message>
              <label for="endDownloadDate">${tmpDateLabel} :</label>
            </td>
            <td>
              <input type="text" class="dateToPick" id="endDownloadDate" name="Media$EndDownloadDate" size="12" maxlength="10" value=""/>
            </td>
          </tr>
          <tr>
            <td class="txtlibform">
              <fmt:message key="gallery.beginDate" var="tmpDateLabel">
                <fmt:param value="${1}"/>
              </fmt:message>
              <label for="beginVisibilityDate">${tmpDateLabel} :</label>
            </td>
            <td>
              <input type="text" class="dateToPick" id="beginVisibilityDate" name="Media$BeginVisibilityDate" size="12" maxlength="10" value=""/>
            </td>
          </tr>
          <tr>
            <td class="txtlibform">
              <fmt:message key="gallery.endDate" var="tmpDateLabel">
                <fmt:param value="${1}"/>
              </fmt:message>
              <label for="endVisibilityDate">${tmpDateLabel} :</label>
            </td>
            <td>
              <input type="text" class="dateToPick" id="endVisibilityDate" name="Media$EndVisibilityDate" size="12" maxlength="10" value=""/>&nbsp;
            </td>
          </tr>
        </table>
      </view:board>

      <c:if test="${not empty formUpdate}">
        <!-- Affichage du formulaire XML -->
        <br/>
        <view:board>
          <table>
            <tr>
              <td>
                <%
                  formUpdate.display(out, context, formData);
                %>
              </td>
            </tr>
          </table>
        </view:board>
      </c:if>
    </form>

    <view:buttonPane>
      <view:button label="${validateLabel}" action="${validateAction}"/>
      <view:button label="${cancelLabel}" action="${cancelAction}"/>
    </view:buttonPane>
  </view:frame>
</view:window>
</body>
</html>