<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%-- Resources --%>
<c:set var="_language" value="${requestScope.resources.language}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<fmt:setLocale value="${_language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<%-- Component parameters --%>
<view:componentParam var="pdcActivatedParameter" componentId="${componentId}" parameter="usePdc"/>
<c:set var="isPdcActivated" value="${silfn:booleanValue(pdcActivatedParameter)}"/>

<%
  Form form = (Form) request.getAttribute("Form");
  DataRecord data = (DataRecord) request.getAttribute("Data");
%>

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="keyword" value="${requestScope.KeyWord}"/>

<html>
<head>
  <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
  <view:includePlugin name="datepicker"/>
  <view:includePlugin name="pdc"/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/lucene/luceneQueryValidator.js"/>"></script>
  <script type="text/javascript">
    function sendData() {
      if (checkLuceneQuery()) {
        var values = $('#used_pdc').pdc('selectedValues');
        if (values.length > 0) {
          document.searchForm.AxisValueCouples.value = values.flatten();
        }
        setTimeout("document.searchForm.submit();", 500);
      }
    }

    // this method requires luceneQueryValidator.js
    function checkLuceneQuery() {
      var query = $("#searchQuery").val();
      if (query != null && query.length > 0) {
        query = removeEscapes(query);
        // check question marks are used properly
        if (!checkQuestionMark(query)) {
          return false;
        }
        // check * is used properly
        if (!checkAsterisk(query)) {
          return false;
        }
        return true;
      }
      return true;
    }

    $(document).ready(function(){
      <c:if test="${isPdcActivated}">
        $('#used_pdc').pdc('used', {
          workspace: 'useless',
          component: '${componentId}',
          withSecondaryAxis: true
        });
      </c:if>
    });

  </script>
</head>
<body id="${instanceId}" class="gallery gallery-search yui-skin-sam">

<fmt:message key="gallery.searchAdvanced" var="searchLabel"/>
<view:browseBar path="${searchLabel}">
</view:browseBar>

<view:window>
  <view:frame>

    <form id="advancedSearchForm" name="searchForm" action="Search" method="POST" enctype="multipart/form-data" accept-charset="UTF-8">

      <fieldset id="generalFieldset" class="skinFieldset">
        <div class="fields">
          <div class="field" id="generalArea">
            <label class="txtlibform" for="SearchKeyWord"><fmt:message key="GML.search"/></label>

            <div class="champs">
              <fmt:message key="gallery.search.field.keyword.help" var="searchTitle"/>
              <input type="submit" class="hide"/>
              <input id="searchQuery" type="text" name="SearchKeyWord" value="${keyword}" size="36" title="${searchTitle}"/>
              <a class="sp_button linked-to-input" href="javascript:onClick=sendData();"><fmt:message key="GML.search"/></a>
            </div>
          </div>
        </div>
      </fieldset>

      <c:set var="metadataKeys" value="${requestScope.MetaDataKeys}"/>
      <c:if test="${not empty metadataKeys}">
        <fieldset id="metadataFieldset" class="skinFieldset">
          <legend><fmt:message key="GML.metadata"/></legend>
          <div class="fields">
            <c:forEach var="metaData" items="${metadataKeys}">
              <div class="field" id="metadata_${metaData.property}_area">
                <label class="txtlibform" for="metadata_${metaData.property}">${metaData.label}</label>

                <div class="champs">
                  <c:choose>
                    <c:when test="${metaData.date}">
                      <c:set var="parsedBeginDate" value=""/>
                      <c:set var="parsedEndDate" value=""/>
                      <c:if test="${not empty metaData.value}">
                        <c:set var="beginDate" value="${fn:substring(metaData.value, 1, 9)}"/>
                        <fmt:parseDate var="parsedBeginDate" value="${beginDate}" pattern="yyyyMMdd"/>
                        <c:set var="endDate" value="${fn:substring(metaData.value, 13, fn:length(metaData.value) - 1)}"/>
                        <fmt:parseDate var="parsedEndDate" value="${endDate}" pattern="yyyyMMdd"/>
                      </c:if>
                      <input type="text" class="dateToPick" id="metadonnee_${metaData.property}_Begin" name="${metaData.property}_Begin" size="12" value="<view:formatDate value="${parsedBeginDate}" language="${_language}" />"/>
                      <input type="text" class="dateToPick" id="metadonnee_${metaData.property}_End" name="${metaData.property}_End" size="12" value="<view:formatDate value="${parsedEndDate}" language="${_language}" />"/>
                    </c:when>
                    <c:otherwise>
                      <fmt:message var="metadataTitle" key="gallery.search.field.metadata.help">
                        <fmt:param value="${metaData.label}"/>
                      </fmt:message>
                      <input title="${metadataTitle}" id="metadata_${metaData.property}" type="text" name="${metaData.property}" value="${metaData.value}" size="36"/>
                    </c:otherwise>
                  </c:choose>
                </div>
              </div>
            </c:forEach>
          </div>
        </fieldset>

      </c:if>

      <% if (form != null && data != null) { %>
      <fieldset id="formInfo" class="skinFieldset">
        <legend><fmt:message key="GML.bloc.further.information"/></legend>
              <%
                PagesContext xmlContext =
                    new PagesContext("myForm", "0", resource.getLanguage(), false, componentId,
                        null);
                xmlContext.setBorderPrinted(false);
                form.display(out, xmlContext, data);
              %>
      </fieldset>
      <% } %>

      <c:if test="${isPdcActivated}">
        <fieldset id="pdcFieldset" class="skinFieldset">
          <legend><fmt:message key="GML.PDC"/></legend>
          <div id="used_pdc"></div>
          <input type="hidden" name="AxisValueCouples"/>
        </fieldset>
      </c:if>

      <view:buttonPane>
        <fmt:message key="GML.search" var="searchButtonLabel"/>
        <view:button label="${searchButtonLabel}" action="javascript:onClick=sendData();"/>
        <fmt:message key="gallery.search.reset" var="resetButtonLabel"/>
        <view:button label="${resetButtonLabel}" action="ClearSearch"/>
      </view:buttonPane>
    </form>

    <script type="text/javascript">
      $(document).ready(function() {
        $('#advancedSearchForm').on("submit", function() {
          sendData();
          return false;
        });
      });
    </script>

  </view:frame>
</view:window>
</body>
</html>