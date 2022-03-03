<%--
  Copyright (C) 2000 - 2022 Silverpeas
  
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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Constants --%>
<view:setConstant var="MEDIA_DEFINITIONS" constant="org.silverpeas.components.gallery.constant.MediaResolution.ALL"/>
<jsp:useBean id="MEDIA_DEFINITIONS" type="java.util.Set<org.silverpeas.components.gallery.constant.MediaResolution>"/>
<view:setConstant var="MEDIA_SORTS" constant="org.silverpeas.components.gallery.web.MediaSort.ALL"/>
<jsp:useBean id="MEDIA_SORTS" type="java.util.Set<org.silverpeas.components.gallery.web.MediaSort>"/>

<%-- Default values --%>
<c:set var="_formName" value="mediaForm"/>

<%-- Request attributes --%>
<c:set var="currentUserSort" value="${requestScope.Sort}"/>

<%@ attribute name="formName" required="false"
              type="java.lang.String"
              description="The name of the HTML form TAG ('mediaForm' by default)." %>
<c:if test="${formName != null}">
  <c:set var="_formName" value="${formName}"/>
</c:if>

<%@ attribute name="currentMediaResolution" required="true"
              type="org.silverpeas.components.gallery.constant.MediaResolution"
              description="The current displayed mediaResolution." %>

<%@ attribute name="currentSearchKeyWord" required="false"
              type="java.lang.String"
              description="The current search keywords." %>

<select name="ChoiceSize" onchange="__choiceGoTo(this.selectedIndex);">
  <option selected="selected"><fmt:message key="gallery.selectSize"/></option>
  <option>-------------------------------</option>
  <c:forEach var="definition" items="${MEDIA_DEFINITIONS}">
    <c:if test="${definition.displayed}">
      <option value="${definition.label}" ${currentMediaResolution eq definition ? 'selected' : ''}>${definition.label}</option>
    </c:if>
  </c:forEach>
</select>
<select name="SortBy" onchange="__sortGoTo(this.selectedIndex);">
  <option selected><fmt:message key="gallery.orderBy"/></option>
  <option>-------------------------------</option>
  <c:forEach var="mediaSort" items="${MEDIA_SORTS}">
    <c:if test="${mediaSort.displayed}">
      <option value="${mediaSort.name}" ${currentUserSort eq mediaSort ? 'selected' : ''}>
        <fmt:message key="${mediaSort.bundleKey}"/></option>
    </c:if>
  </c:forEach>
</select>

<c:set var="albumListDisplaySelectorForms">
  <view:form name="ChoiceSelectForm" action="ChoiceSize" method="POST">
    <input type="hidden" name="Choice">
    <input type="hidden" name="SearchKeyWord" value="${currentSearchKeyWord}">
  </view:form>
  <view:form name="OrderBySelectForm" action="SortBy" method="POST">
    <input type="hidden" name="Sort">
    <input type="hidden" name="SearchKeyWord" value="${currentSearchKeyWord}">
  </view:form>
</c:set>

<script type="text/javascript">
  function __choiceGoTo(selectedIndex) {
    if (selectedIndex != 0 && selectedIndex != 1) {
      document.ChoiceSelectForm.Choice.value =
          document.${_formName}.ChoiceSize[selectedIndex].value;
      document.ChoiceSelectForm.submit();
    }
  }
  function __sortGoTo(selectedIndex) {
    if (selectedIndex != 0 && selectedIndex != 1) {
      document.OrderBySelectForm.Sort.value = document.${_formName}.SortBy[selectedIndex].value;
      document.OrderBySelectForm.submit();
    }
  }

  $(document).ready(function() {
    $(document.body).append("${silfn:escapeJs(albumListDisplaySelectorForms)}");
  });
</script>

