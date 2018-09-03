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
<%@ page import="org.silverpeas.components.mydb.model.predicates.Equality" %>
<%@ page import="org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.publisher"/>
<view:setConstant var="tableView" constant="org.silverpeas.components.mydb.web.MyDBWebController.TABLE_VIEW"/>
<view:setConstant var="allTables" constant="org.silverpeas.components.mydb.web.MyDBWebController.ALL_TABLES"/>
<view:setConstant var="comparingColumn" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_COLUMN"/>
<view:setConstant var="comparingOperator" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_OPERATOR"/>
<view:setConstant var="comparingValue" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_VALUE"/>
<view:setConstant var="comparingOperators" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_OPERATORS"/>
<view:setConstant var="nothing" constant="org.silverpeas.components.mydb.web.TableRowsFilter.FIELD_NONE"/>

<c:set var="componentId"       value="${requestScope.browseContext[3]}"/>
<c:set var="columnToCompare"   value="${requestScope[comparingColumn]}"/>
<c:set var="comparators"       value="${requestScope[comparingOperators]}"/>
<c:set var="currentComparator" value="${requestScope[comparingOperator]}"/>
<c:set var="columnValue"       value="${requestScope[comparingValue]}"/>
<c:set var="currentTable"      value="${requestScope[tableView]}"/>
<c:set var="tableNames"        value="${requestScope[allTables]}"/>
<c:set var="nullValue"         value="<%=AbstractColumnValuePredicate.NULL%>"/>
<jsp:useBean id="currentTable" type="org.silverpeas.components.mydb.web.TableView"/>

<fmt:message var="windowTitle"   key="mydb.mainTitle"/>
<fmt:message var="crumbTitle"    key="mydb.tableView"/>
<fmt:message var="resultTab"     key="mydb.tableView"/>
<fmt:message var="dataSourceTab" key="mydb.dataSourceSetting"/>
<fmt:message var="all"           key="mydb.all"/>
<fmt:message var="includes"      key="mydb.include"/>
<fmt:message var="buttonOk"      key="GML.ok"/>

<fmt:message var="columnField"         key="mydb.column"/>
<fmt:message var="criterionField"      key="mydb.criterion"/>
<fmt:message var="valueCriterionField" key="mydb.criterionValue"/>

<fmt:message var="filterValueInfo" key="mydb.criterionValueExplanation"/>

<c:url var="infoIcon" value="/util/icons/info.gif"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>${windowTitle}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <script type="application/javascript">
    function filterTableRows() {
      var table = $('#table').val();
      var column = $('#table-filter-column').val();
      var comparator = $('#table-filter-comparator').val();
      var value = $('#table-filter-value').val()
      if (table === null || table === '${nothing}') {
        notyError('<fmt:message key="mydb.error.noSelectedTable"/>');
      } else  if (column === null || column === '${nothing}') {
        notyError('<fmt:message key="mydb.error.noSelectedColumn"/>');
      } else  if (comparator === null || comparator === '${nothing}') {
        notyError('<fmt:message key="mydb.error.noSelectedComparator"/>');
      } else if (value === null || value === '') {
        notyError('<fmt:message key="mydb.error.noValue"/>');
      } else {
        $('#table-filter').submit();
      }
    }

    function selectTable() {
      var table = $('#table').val();
      if (table === null || table === '${nothing}') {
        notyError('<fmt:message key="mydb.error.noSelectedTable"/>');
      } else {
        $('#table-selection').submit();
      }
    }
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}" extraInformations="${crumbTitle}"/>
<view:window>
  <view:componentInstanceIntro componentId="${componentId}" language="${currentUserLanguage}"/>
  <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
    <view:tabs>
      <view:tab label="${resultTab}" action="Main" selected="true"/>
      <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(adminRole)}">
        <view:tab label="${dataSourceTab}" action="ConnectionSetting" selected="false"/>
      </c:if>
    </view:tabs>
  </c:if>
  <view:frame>
    <div id="selection" style="padding-bottom: 10px">
      <form id="table-selection" name="table-selection" action="SetTable" method="post">
        <span class="intfdcolor selectNS" style="padding: 2px">
        <select id="table" name="${tableView}" size="1">
          <c:if test="${not currentTable.defined}">
            <option value="${nothing}" selected>&nbsp;</option>
          </c:if>
          <c:forEach var="tableName" items="${tableNames}">
            <c:choose>
              <c:when test="${tableName.equals(currentTable.name)}">
                <option value="${tableName}" selected>${tableName}</option>
              </c:when>
              <c:otherwise>
                <option value="${tableName}">${tableName}</option>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </select>
        </span>
        <span class="intfdcolor selectNS">
          <view:button label="${buttonOk}" action="javascript:onclick=selectTable()"/>
        </span>
      </form>
    </div>
    <div id="filtering" style="padding-bottom: 10px">
      <form id="table-filter" name="table-filter" action="FilterTable" method="post">
        <span class="intfdcolor selectNS" style="padding: 2px">
          <select id="table-filter-column" name="${comparingColumn}" size="1">
            <c:choose>
              <c:when test="${nothing.equals(columnToCompare)}">
                <option value="${nothing}" selected>${all}</option>
              </c:when>
              <c:otherwise>
                <option value="${nothing}">${all}</option>
              </c:otherwise>
            </c:choose>
            <c:forEach var="column" items="${currentTable.columns}">
              <c:choose>
                <c:when test="${column.name.equals(columnToCompare)}">
                  <option value="${column.name}" selected>${column.name}</option>
                </c:when>
                <c:otherwise>
                  <option value="${column.name}">${column.name}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </span>
        <span class="intfdcolor selectNS" style="padding: 2px">
          <select id="table-filter-comparator" name="${comparingOperator}" size="1">
            <c:forEach var="comparator" items="${comparators}">
              <c:set var="comparatorLabel" value="${comparator}"/>
              <c:if test="${comparator.equals(nothing)}">
                <c:set var="comparatorlabel" value="${all}"/>
              </c:if>
              <c:if test="${comparator.equals('including')}">
                <c:set var="comparatorLabel" value="${includes}"/>
              </c:if>
              <c:choose>
                <c:when test="${comparator.equals(currentComparator)}">
                  <option value="${comparator}" selected>${comparatorLabel}</option>
                </c:when>
                <c:otherwise>
                  <option value="${comparator}">${comparatorLabel}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </span>
        <span class="intfdcolor selectNS" style="padding: 2px">
          ${valueCriterionField}&nbsp;: <input id="table-filter-value" type="text" name="${comparingValue}" size="30" value="${columnValue}"/>
        </span>
        <span class="intfdcolor selectNS">
          <img class="filter-info-button" src="${infoIcon}" alt="info"/>
          <view:button label="${buttonOk}" action="javascript:onclick=filterTableRows()"/>
        </span>
      </form>
    </div>
    <div id="table-view">
      <c:set var="columns" value="${currentTable.columns}"/>
      <view:arrayPane var="Table${componentId}" routingAddress="ViewTable" export="false" numberLinesPerPage="25">
        <c:forEach var="field" items="${columns}">
          <view:arrayColumn title="${field.name}" compareOn="${(r, i) -> r.getFieldValue(columns[i].name)}"/>
        </c:forEach>
        <view:arrayLines var="row" items="${currentTable.rows}">
          <view:arrayLine>
            <c:forEach var="field" items="${columns}">
              <c:set var="currentValue" value="${row.getFieldValue(field.name)}"/>
              <view:arrayCellText text="${currentValue == null ? nullValue : currentValue}" nullStringValue="${nullValue}"/>
            </c:forEach>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
    </div>
  </view:frame>
</view:window>
<script type="text/javascript">
  whenSilverpeasReady(function() {
    TipManager.simpleHelp(".filter-info-button", "${filterValueInfo}");
    sp.arrayPane.ajaxControls('#table-view');
  });
</script>
</body>