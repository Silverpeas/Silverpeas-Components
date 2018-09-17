<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ include file="head.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="paramColumns" constant="org.silverpeas.components.mydb.web.MyDBWebController.ALL_COLUMNS"/>
<view:setConstant var="paramRow" constant="org.silverpeas.components.mydb.web.MyDBWebController.ROW"/>
<view:setConstant var="paramError" constant="org.silverpeas.components.mydb.web.MyDBWebController.ERROR_MESSAGE"/>

<c:set var="error" value="${requestScope[paramError]}"/>

<fmt:message var="modifyRow" key="mydb.modifyRow"/>

<c:choose>
  <c:when test="${silfn:isDefined(error)}">
    <div id="error"><span>${error}</span></div>
  </c:when>
  <c:otherwise>
    <c:set var="row" value="${requestScope[paramRow]}"/>
    <c:set var="columns" value="${requestScope[paramColumns]}"/>
    <jsp:useBean id="row" type="org.silverpeas.components.mydb.model.TableRow"/>
    <jsp:useBean id="columns" type="java.util.List<org.silverpeas.components.mydb.model.DbColumn>"/>
    <fieldset id="row-edition" class="skinFieldset">
      <div class="fields oneFieldPerLine">
        <c:forEach var="field" items="${columns}">
          <c:if test="${not field.primaryKey}">
            <c:set var="fieldValue" value="${row.getFieldValue(field.name)}"/>
            <jsp:useBean id="fieldValue" type="org.silverpeas.components.mydb.model.TableFieldValue"/>
            <div class="field" id="field-${field.name}">
              <c:set var="fieldType" value="${field.typeName}"/>
              <c:if test="${field.ofTypeText}">
                <c:set var="fieldType" value="${field.typeName}(${field.size})"/>
              </c:if>
              <label for="field-${field.name}-value" class="txtlibform">${field.name}&nbsp;<span><small>(<i>${fieldType}</i>)</small></span></label>
              <div class="champs">
                <c:choose>
                  <c:when test="${field.ofTypeText and field.size / 100 > 1}">
                    <textarea id="field-${field.name}-value" name="${field.name}" cols="100" rows="${field.size / 100}" maxlength="${field.size}">${fieldValue}</textarea>
                  </c:when>
                  <c:when test="${not field.ofTypeBinary}">
                    <input id="field-${field.name}-value" name="${field.name}" type="text" maxlength="${field.size}" value="${fieldValue}"/>
                  </c:when>
                </c:choose>
              </div>
            </div>
          </c:if>
        </c:forEach>
      </div>
    </fieldset>
  </c:otherwise>
</c:choose>