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

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.publisher"/>
<view:setConstant var="tableView" constant="org.silverpeas.components.mydb.web.MyDBWebController.TABLE_VIEW"/>
<view:setConstant var="allTables" constant="org.silverpeas.components.mydb.web.MyDBWebController.ALL_TABLES"/>
<view:setConstant var="comparingColumn" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_COLUMN"/>
<view:setConstant var="comparingOperator" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_OPERATOR"/>
<view:setConstant var="comparingValue" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_VALUE"/>
<view:setConstant var="comparingOperators" constant="org.silverpeas.components.mydb.web.MyDBWebController.COMPARING_OPERATORS"/>
<view:setConstant var="nothing" constant="org.silverpeas.components.mydb.web.TableRowsFilter.FIELD_NONE"/>
<view:setConstant var="rowIndex" constant="org.silverpeas.components.mydb.web.MyDBWebController.ROW_INDEX"/>
<view:setConstant var="nullValue" constant="org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate.NULL_VALUE"/>

<c:set var="componentId"       value="${requestScope.browseContext[3]}"/>
<c:set var="columnToCompare"   value="${requestScope[comparingColumn]}"/>
<c:set var="comparators"       value="${requestScope[comparingOperators]}"/>
<c:set var="currentComparator" value="${requestScope[comparingOperator]}"/>
<c:set var="columnValue"       value="${requestScope[comparingValue]}"/>
<c:set var="currentTable"      value="${requestScope[tableView]}"/>
<c:set var="tableNames"        value="${requestScope[allTables]}"/>
<jsp:useBean id="currentTable" type="org.silverpeas.components.mydb.web.TableView"/>

<c:set var="columns" value="${currentTable.columns}"/>
<c:set var="rows" value="${currentTable.rows}"/>

<fmt:message var="windowTitle" key="mydb.mainTitle"/>
<fmt:message var="crumbTitle" key="mydb.tableView"/>
<fmt:message var="resultTab" key="mydb.tableView"/>
<fmt:message var="dataSourceTab" key="mydb.dataSourceSetting"/>
<fmt:message var="all" key="mydb.all"/>
<fmt:message var="includes" key="mydb.include"/>
<fmt:message var="buttonOk" key="GML.ok"/>
<fmt:message var="columnField" key="mydb.column"/>
<fmt:message var="criterionField" key="mydb.criterion"/>
<fmt:message var="valueCriterionField" key="mydb.criterionValue"/>
<fmt:message var="filterValueInfo" key="mydb.criterionValueExplanation"/>
<fmt:message var="operations" key="GML.operations"/>
<fmt:message var="deletion" key="GML.delete"/>
<fmt:message var="deletionConfirm" key="mydb.deletion.confirmation"/>
<fmt:message var="modify" key="GML.modify"/>
<fmt:message var="noSelectedTable" key="mydb.error.noSelectedTable"/>
<fmt:message var="noSelectedColumn" key="mydb.error.noSelectedColumn"/>
<fmt:message var="noSelectedComparator" key="mydb.error.noSelectedComparator"/>
<fmt:message var="noValue" key="mydb.error.noValue"/>
<fmt:message var="modifyRow" key="mydb.modifyRow"/>
<fmt:message var="newRow" key="mydb.insertRow"/>
<fmt:message var="nullForbidden" key="mydb.error.mandatory"/>

<fmt:message bundle="${icons}" var="infoIcon" key="mydb.icons.info"/>
<fmt:message bundle="${icons}" var="deleteIcon" key="mydb.icons.deleteLine"/>
<fmt:message bundle="${icons}" var="modifyIcon" key="mydb.icons.updateLine"/>
<fmt:message bundle="${icons}" var="createIcons" key="mydb.icons.addRecord"/>
<fmt:message bundle="${icons}" var="primaryKeyIcon" key="mydb.icons.primaryKey"/>
<c:url var="primaryKeyIcon" value="${primaryKeyIcon}"/>
<c:url var="infoIcon" value="${infoIcon}"/>
<c:url var="deleteIcon" value="${deleteIcon}"/>
<c:url var="modifyIcon" value="${modifyIcon}"/>
<c:url var="createIcons" value="${createIcons}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>${windowTitle}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel withCheckFormScript="true" withFieldsetStyle="true"/>
  <script type="application/javascript">
    /**
     * Filters the rows of the default table currently displayed by applying the comparator on the
     * field against a given value.
     */
    function filterTableRows() {
      var table = $('#table').val();
      var column = $('#table-filter-column').val();
      var comparator = $('#table-filter-comparator').val();
      var value = $('#table-filter-value').val();
      if (table === null || table === '${nothing}') {
        notyError('${noSelectedTable}');
      } else if (column !== '${nothing}' && comparator !== '${nothing}' &&
          (value === null || value === '')) {
        notyError('${noValue}');
      } else {
        $('#table-filter').submit();
      }
    }

    /**
     * Selects as the default one a database table.
     */
    function selectTable() {
      var table = $('#table').val();
      if (table === null || table === '${nothing}') {
        notyError('${noSelectedTable}');
      } else {
        $('#table-selection').submit();
      }
    }

    /**
     * Deletes the row at the specified index in the view of the default table and then refreshes
     * the view.
     */
    function deleteRow(rowIndex) {
      $.popup.confirm('${deletionConfirm}', function() {
        sp.ajaxRequest('DeleteRow').withParam('${rowIndex}', rowIndex)
            .byPostMethod().send().then(rowsPane.refreshFromRequestResponse);
      });
    }

    /**
     * Renders in a popup the form for editing a row in the currently displayed table.
     */
    function renderRowForm(title, xhr, formSender) {
      var form = $('<div>').html(xhr.responseText);
      if (form.find('#error').length > 0) {
        notyError(form.find('#error').html());
      } else {
        form.popup('validation', {
          title : title, callback : function() {
            var row = {};
            var error = '';
            form.find('[id*="value"]').each(function(i, input) {
              var elt = $(input);
              var val = elt.val();
              if (val === null || val.length === 0) {
                error += elt.attr('name') + ': ${noValue}<br/>';
              } else if (elt.hasClass('mandatory') && val === 'null') {
                error += elt.attr('name') + ': ${nullForbidden}<br/>';
              } else {
                row[elt.attr('name')] = elt.val();
              }
            });
            if (error.length > 0) {
              notyError(error);
            } else {
              formSender(row);
            }
          },
          callbackOnClose: function() {
            form.remove();
          }
        });
      }
    }

    /**
     * Updates the row at the specified index in the view of the default table with the values
     * entered in given the row form.
     */
    function updateRow(rowIndex, row) {
      row['${rowIndex}'] = rowIndex;
      sp.ajaxRequest('UpdateRow').withParams(row)
          .byPostMethod().send().then(rowsPane.refreshFromRequestResponse);
    }

    /**
     * Opens the form to modify the row at the specified index in the view of the default table.
     */
    function modifyRow(rowIndex) {
      sp.ajaxRequest('GetRow').withParam('${rowIndex}', rowIndex).send().then(function(response) {
        var sender = function(row) {
          updateRow(rowIndex, row);
        };
        renderRowForm('${modifyRow}', response, sender)
      });
    }

    /**
     * Adds the new specified row into the default table and refreshes the view on that table.
     */
    function addRow(row) {
      for (var field in row) {
        if (row[field].length === 0) {
          row[field] = null;
        } else if (row[field] === '@empty@') {
          row[field] = '';
        }
      }
      sp.ajaxRequest('AddRow').withParams(row)
          .byPostMethod().send().then(rowsPane.refreshFromRequestResponse);
    }

    /**
     * Opens the form to edit a new row to add into the default table.
     */
    function newRow() {
      sp.ajaxRequest('NewRow').send().then(function(response) {
        renderRowForm('${newRow}', response, addRow)
      });
    }

    /**
     * The unique identifier of a row in another table to use as foreign key in a row currently
     * edited.
     */
    var fkRowId = null;

    /**
     * Open the specified table to select a row as foreign key when inserting a new row in the
     * current table. This function is invoked by the JSP rendered into the popup of row adding.
     */
    function openForeignKey(tableName, fieldName) {
      fkRowId = null;
      sp.ajaxRequest('ViewTargetTable').withParam('${tableView}', tableName).send().then(
          function(response) {
            renderRowForm('', response, function(row) {
              $('#field-' + fieldName + '-value').val(fkRowId);
            })
          });
    }

    /**
     * A row in a table has been selected as a foreign key. This function is invoked by the JSP
     * rendered into the popup of the row selection for foreign key setting.
     */
    function selectFk(rowIndex, rowId) {
      $('#fk-table-view td.selected').removeClass('selected');
      $('#fk-table-view #fk-row-' + rowIndex).children().addClass('selected');
      fkRowId = rowId;
    }
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}" extraInformations="${crumbTitle}"/>
<c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
<view:operationPane>
  <view:operationOfCreation action="javascript:newRow()" icon="${createIcons}" altText="${newRow}"/>
</view:operationPane>
</c:if>
<view:window>
  <view:componentInstanceIntro componentId="${componentId}" language="${currentUserLanguage}"/>
  <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:tabs>
      <view:tab label="${resultTab}" action="Main" selected="true"/>
      <view:tab label="${dataSourceTab}" action="ConnectionSetting" selected="false"/>
    </view:tabs>
  </c:if>
  <view:frame>
    <view:areaOfOperationOfCreation/>
    <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
    <div id="selection" style="padding-bottom: 10px">
      <form id="table-selection" name="table-selection" action="SetTable" method="post">
        <label for="table"><fmt:message key="mydb.tables"/></label>
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
    </c:if>
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
      <view:arrayPane var="Table${componentId}" routingAddress="ViewTable" export="false" numberLinesPerPage="25">
        <c:forEach var="column" items="${columns}">
          <c:set var="columnName" value="${column.name}"/>
          <c:if test="${column.primaryKey}">
            <c:set var="columnName">${columnName}
              <img alt="primary key" src="${primaryKeyIcon}" width="10" height="10"/></c:set>
          </c:if>
          <view:arrayColumn title="${columnName}" compareOn="${(r, i) -> r.getFieldValue(columns[i].name)}"/>
        </c:forEach>
        <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
          <view:arrayColumn title="${operations}" sortable="false"/>
        </c:if>
        <c:if test="${rows.size() > 0}">
          <view:arrayLines var="rowIndex" begin="0" end="${rows.size() - 1}">
            <c:set var="row" value="${rows[rowIndex]}"/>
          <view:arrayLine>
            <c:forEach var="field" items="${columns}">
              <c:set var="currentValue" value="${row.getFieldValue(field.name)}"/>
              <view:arrayCellText text="${currentValue}" nullStringValue="${nullValue}"/>
            </c:forEach>
            <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
              <view:arrayCellText>
                <a href="javascript:deleteRow(${rowIndex});"><img src="${deleteIcon}" alt="${deletion}"/></a>
                <a href="javascript:modifyRow(${rowIndex});"><img src="${modifyIcon}" alt="${modify}"/></a>
              </view:arrayCellText>
            </c:if>
          </view:arrayLine>
        </view:arrayLines>
        </c:if>
      </view:arrayPane>
    </div>
  </view:frame>
</view:window>
<script type="text/javascript">
  whenSilverpeasReady(function() {
    TipManager.simpleHelp(".filter-info-button", "${filterValueInfo}");
    rowsPane = sp.arrayPane.ajaxControls('#table-view');
  });
</script>
</body>