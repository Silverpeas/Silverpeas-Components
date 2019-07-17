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

<%@ page import="org.silverpeas.components.jdbcconnector.service.comparators.Equality" %>

<%@ include file="head.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="adminRole"          constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole"      constant="org.silverpeas.core.admin.user.model.SilverpeasRole.publisher"/>
<view:setConstant var="comparingColumn"    constant="org.silverpeas.components.jdbcconnector.control.JdbcConnectorWebController.COMPARING_COLUMN"/>
<view:setConstant var="comparingOperator"  constant="org.silverpeas.components.jdbcconnector.control.JdbcConnectorWebController.COMPARING_OPERATOR"/>
<view:setConstant var="comparingValue"     constant="org.silverpeas.components.jdbcconnector.control.JdbcConnectorWebController.COMPARING_VALUE"/>
<view:setConstant var="comparingOperators" constant="org.silverpeas.components.jdbcconnector.control.JdbcConnectorWebController.COMPARING_OPERATORS"/>
<view:setConstant var="nothing"            constant="org.silverpeas.components.jdbcconnector.control.TableRowsFilter.FIELD_NONE"/>

<c:set var="componentId"       value="${requestScope.browseContext[3]}"/>
<c:set var="columnToCompare"   value="${requestScope[comparingColumn]}"/>
<c:set var="comparators"       value="${requestScope[comparingOperators]}"/>
<c:set var="currentComparator" value="${requestScope[comparingOperator]}"/>
<c:set var="columnValue"       value="${requestScope[comparingValue]}"/>
<c:set var="queryResult"       value="${requestScope.queryResult}"/>
<jsp:useBean id="queryResult" type="org.silverpeas.components.jdbcconnector.control.QueryResult"/>
<c:set var="nullValue"         value="<%=Equality.NULL%>"/>

<fmt:message var="windowTitle"   key="windowTitleMain"/>
<fmt:message var="crumbTitle"    key="titreExecution"/>
<fmt:message var="reload"        key="reloadRequest"/>
<fmt:message var="resultTab"     key="tabbedPaneConsultation"/>
<fmt:message var="queryTab"      key="tabbedPaneRequete"/>
<fmt:message var="dataSourceTab" key="tabbedPaneParametresJDBC"/>
<fmt:message var="all"           key="comboTous"/>
<fmt:message var="includes"      key="contient"/>
<fmt:message var="buttonOk"      key="GML.ok"/>

<fmt:message var="columnField"           key="arrayPaneColonne"/>
<fmt:message var="crtierionField"        key="arrayPaneCritere"/>
<fmt:message var="valueCriterionField"   key="champValeur"/>

<fmt:message var="filterValueInfo"       key="filter.value.info"/>

<c:url var="editorIcon" value="/util/icons/connecteurJDBC_request.gif"/>
<c:url var="infoIcon" value="/util/icons/info.gif"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.jdbcConnector">
<head>
  <title>${windowTitle}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="toggle"/>
  <script type="application/javascript">
    function restrictResults() {
      $('#result-filter').submit();
    }
    whenSilverpeasReady(function() {
      TipManager.simpleHelp(".filter-info-button", "${filterValueInfo}");
    });
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}" extraInformations="${crumbTitle}"/>
<view:operationPane>
  <view:operation action="Main?reload=true" altText="${reload}" icon="${editorIcon}"/>
</view:operationPane>
<view:window>
  <view:componentInstanceIntro componentId="${componentId}" language="${currentUserLanguage}"/>
  <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
    <view:tabs>
      <view:tab label="${resultTab}" action="Main" selected="true"/>
      <view:tab label="${queryTab}" action="ParameterRequest" selected="false"/>
      <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(adminRole)}">
        <view:tab label="${dataSourceTab}" action="ParameterConnection" selected="false"/>
      </c:if>
    </view:tabs>
  </c:if>
  <view:frame>
    <div id="filtering" style="padding-bottom: 10px">
      <form id="result-filter" name="result-set_filtering" action="DoRequest" method="post">
        <div class="intfdcolor selectNS" style="padding: 2px">
          <select id="result-filter-column" name="${comparingColumn}" size="1">
            <c:choose>
              <c:when test="${nothing.equals(columnToCompare)}">
                <option value="${nothing}" selected>${all}</option>
              </c:when>
              <c:otherwise>
                <option value="${nothing}">${all}</option>
              </c:otherwise>
            </c:choose>
            <c:forEach var="column" items="${queryResult.fieldNames}">
              <c:choose>
                <c:when test="${column.equals(columnToCompare)}">
                  <option value="${column}" selected>${column}</option>
                </c:when>
                <c:otherwise>
                  <option value="${column}">${column}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </div>
        <div class="intfdcolor selectNS" style="padding: 2px">
          <select id="result-filter-comparator" name="${comparingOperator}" size="1">
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
        </div>
        <div class="intfdcolor selectNS" style="padding: 2px">
          ${valueCriterionField}&nbsp;: <input id="result-filter-value" type="text" name="${comparingValue}" size="30" value="${columnValue}"/>
        </div>
        <div class="intfdcolor selectNS">
          <img class="filter-info-button" src="${infoIcon}" alt="info"/>
          <view:button classes="linked-to-input" label="${buttonOk}" action="javascript:onclick=restrictResults()"/>
        </div>
      </form>
    </div>
    <div id="result-set">
      <c:set var="fieldNames" value="${queryResult.fieldNames}"/>
      <view:arrayPane var="ResultSet${componentId}" routingAddress="ViewResultSet" export="true" numberLinesPerPage="25">
        <c:forEach var="fieldName" items="${fieldNames}">
          <view:arrayColumn title="${fieldName}" compareOn="${(r, i) -> r.getFieldValue(fieldNames[i])}"/>
        </c:forEach>
        <view:arrayLines var="row" items="${queryResult.filteredRows}">
          <view:arrayLine>
            <c:forEach var="fieldName" items="${fieldNames}">
              <c:set var="currentValue" value="${row.getFieldValue(fieldName)}"/>
              <view:arrayCellText text="${currentValue == null ? nullValue : currentValue}" nullStringValue="${nullValue}"/>
            </c:forEach>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.arrayPane.ajaxControls('#result-set');
        });
      </script>
    </div>
  </view:frame>
</view:window>
<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.jdbcConnector', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>