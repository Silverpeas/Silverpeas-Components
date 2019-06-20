<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
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
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message bundle="${icons}" var="primaryKeyIcon" key="mydb.icons.primaryKey"/>
<c:url var="primaryKeyIcon" value="${primaryKeyIcon}"/>

<view:setConstant var="paramTable" constant="org.silverpeas.components.mydb.web.MyDBWebController.TABLE_VIEW"/>
<view:setConstant var="paramError" constant="org.silverpeas.components.mydb.web.MyDBWebController.ERROR_MESSAGE"/>
<view:setConstant var="nullValue" constant="org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate.NULL_VALUE"/>

<c:set var="error" value="${requestScope[paramError]}"/>

<c:choose>
  <c:when test="${silfn:isDefined(error)}">
    <div id="error"><span>${error}</span></div>
  </c:when>
  <c:otherwise>
    <c:set var="table" value="${requestScope[paramTable]}"/>
    <jsp:useBean id="table" type="org.silverpeas.components.mydb.web.TableView"/>
    <c:set var="columns" value="${table.columns}"/>
    <c:set var="rows" value="${table.rows}"/>
    <div id="fk-table-view">
      <view:arrayPane var="Table${table.name}" routingAddress="ViewTargetTable" export="false" numberLinesPerPage="25">
        <c:forEach var="column" items="${columns}">
          <c:set var="columnName" value="${column.name}"/>
          <c:if test="${column.primaryKey}">
            <c:set var="columnName">${columnName} <img alt="primary key" src="${primaryKeyIcon}" width="10" height="10"/></c:set>
          </c:if>
          <view:arrayColumn title="${columnName}" sortable="false"/>
        </c:forEach>
        <view:arrayLines var="rowIndex" begin="0" end="${rows.size() - 1}">
          <view:arrayLine id="fk-row-${rowIndex}">
            <c:set var="selectionLink" value=""/>
            <c:forEach var="field" items="${columns}">
              <c:set var="currentValue" value="${rows[rowIndex].getFieldValue(field.name)}"/>
              <c:if test="${field.primaryKey and not silfn:isDefined(selectionLink)}">
                <c:set var="selectionLink"><a href="javascript:window.selectFk('${rowIndex}', '${currentValue}');"></c:set>
              </c:if>
              <c:set var="valueToRender">${selectionLink}${currentValue}</a></c:set>
              <view:arrayCellText text="${valueToRender}" nullStringValue="${nullValue}"/>
            </c:forEach>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
    </div>
  </c:otherwise>
</c:choose>

