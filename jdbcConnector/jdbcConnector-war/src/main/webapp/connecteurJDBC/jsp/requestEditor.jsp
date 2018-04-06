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

<view:setConstant var="adminRole"          constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole"      constant="org.silverpeas.core.admin.user.model.SilverpeasRole.publisher"/>
<view:setConstant var="comparingOperators" constant="org.silverpeas.components.jdbcconnector.control.JdbcConnectorWebController.COMPARING_OPERATORS"/>
<view:setConstant var="nothing"            constant="org.silverpeas.components.jdbcconnector.control.TableRowsFilter.FIELD_NONE"/>

<c:if test="${not requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="comparators" value="${requestScope[comparingOperators]}"/>
<c:set var="tables" value="${requestScope.tables}"/>
<jsp:useBean id="tables" type="java.util.Map<java.lang.String, java.lang.String>"/>

<fmt:message var="windowTitle"   key="titrePopup"/>
<fmt:message var="crumbTitle"    key="titrePopup"/>
<fmt:message var="queryField"    key="champRequete"/>
<fmt:message var="buttonNext"    key="boutonSuivant"/>
<fmt:message var="buttonDone"    key="boutonTerminer"/>
<fmt:message var="buttonCancel"  key="boutonAnnuler"/>
<fmt:message var="requestError"  key="erreurChampsTropLong"/>
<fmt:message var="requestEditor" key="operationPaneRequete"/>
<fmt:message var="includes"      key="contient"/>
<fmt:message var="selectSome"    key="selectSome"/>
<fmt:message var="selectAll"     key="selectAll"/>
<fmt:message var="unselectSome"  key="unselectSome"/>
<fmt:message var="unselectAll"   key="unselectAll"/>
<fmt:message var="addCriterion"  key="addCriterion"/>

<c:url var="editorIcon" value="/util/icons/connecteurJDBC_request.gif"/>
<c:url var="selectSomeIcon" value="/util/icons/formButtons/arrowRight.gif"/>
<c:url var="unselectSomeIcon" value="/util/icons/formButtons/arrowLeft.gif"/>
<c:url var="selectAllIcon" value="/util/icons/formButtons/arrowDoubleRight.gif"/>
<c:url var="unselectAllIcon" value="/util/icons/formButtons/arrowDoubleLeft.gif"/>
<c:url var="addCriterionIcon" value="/util/icons/add.gif"/>

<div class="fields">
  <form name="requestEditor" action="javascript:setUpRequest();" method="post">
    <div id="step1" class="field entireWidth">
      <label class="txtlibform" for="table"><fmt:message key="popupSelection1"/>&nbsp;: </label>
      <div class="champs">
        <select id="table" name="table" style="width: 30em;">
          <c:forEach var="tableName" items="${tables.keySet()}">
            <option value="${tableName}">${tableName}</option>
          </c:forEach>
        </select>
      </div>
    </div>

    <div id="step2">
      <div class="field">
        <label class="txtlibform" for="table-columns"><fmt:message key="popupSelection2"/>&nbsp;: </label>
        <div class="champs">
          <select id="table-columns" name="table-columns" multiple size="10"></select>
        </div>
      </div>
      <div class="field">
        <label class="txtlibform" for="columns"><fmt:message key="popupSelected"/>&nbsp;: </label>
        <div class="champs">
          <select id="columns" name="columns" multiple size="10"></select>
        </div>
      </div>
      <div class="field entireWidth">
        <div class="champs sp_buttonPane">
          <a title="${selectSome}" href="javascript:selectOne();"><img src="${selectSomeIcon}"/></a>
          <a title="${selectAll}" href="javascript:selectAll();"><img src="${selectAllIcon}"/></a>
          <a title="${unselectSome}" href="javascript:unselectOne();"><img src="${unselectSomeIcon}"/></a>
          <a title="${unselectAll}" href="javascript:unselectAll();"><img src="${unselectAllIcon}"/></a>
        </div>
      </div>
    </div>
    <div id="step3" class="field entireWidth">
      <label class="txtlibform"><fmt:message key="popupSelection3"/>&nbsp;: </label>
      <div id="criteria">
        <select id="criterion-column" name="criterion-column" size="1">
          <option value="" selected="selected"></option>
        </select>
        <select id="criterion-comparator" name="criterion-comparator" size="1">
          <c:forEach var="comparator" items="${comparators}">
            <c:set var="comparatorLabel" value="${comparator}"/>
            <c:if test="${not comparator.equals(nothing)}">
              <c:if test="${comparator.equals('including')}">
                <c:set var="comparatorLabel" value="${includes}"/>
              </c:if>
              <option value="${comparator}">${comparatorLabel}</option>
            </c:if>
          </c:forEach>
        </select>
        <input id="criterion-value" type="text" name="criterion-value" size="30" value="">
        <a title="${addCriterion}" href="javascript:addCriterion();"><img src="${addCriterionIcon}" border="0"></a>
      </div>
      <div id="criteria-statement">
        <span></span>
      </div>
    </div>
    <div class="break-line"></div>
    <view:buttonPane>
      <view:button label="${buttonDone}" action="javascript:setUpSQLRequest();"/>
      <view:button label="${buttonCancel}" action="javascript:close();"/>
    </view:buttonPane>
  </form>
</div>

<script type="application/javascript">
  var tables = {};
  <c:forEach var="table" items="${tables.entrySet()}">
  tables['${table.key}'] = '${table.value}'.split(',');
  </c:forEach>

  var criteria = [];

  function cleanUpCriteria() {
    $('#criterion-column').children().remove();
    $('#criterion-comparator').find('option').first().prop('selected', true);
    $('input#criterion-value:first').val('');
    $('#criteria-statement span').replaceWith($('<span>').html(''));
    criteria = [];
  }

  function reloadColumns(tableName) {
    var $columns = $('#table-columns');
    $columns.children().remove();
    $('#columns').children().remove();
    var columns = tables[tableName];
    for (var i = 0; i < columns.length; i++) {
      $('<option>', {'value' : columns[i]}).val(columns[i]).text(columns[i]).appendTo($columns);
    }
  }

  $('#table').on('change', function() {
    cleanUpCriteria();
    reloadColumns(this.value);
  });

  function setUpSQLRequest() {
    var query = 'select ';
    var columns = [];
    $('#columns').find('option').each(function() {
      var opt = $(this);
      columns.push(opt.val());
    });
    if (columns.length > 0) {
      query += columns.join(',');
    } else {
      query += '*';
    }
    query += ' from ' + $('#table').find('option:selected').first().val();
    if (criteria.length > 0) {
      query += ' where ' + criteria.join(' and ');
    }
    $('#SQLReq').val(query.trim());
    closeSingleFreePopup();
  }

  function close() {
    closeSingleFreePopup();
  }

  function addCriterion() {
    var criterion = $('#criterion-column').find('option:selected').first();
    if (criterion !== null && criterion !== undefined) {
      var column = criterion.val();
      var comparator = $('#criterion-comparator').find('option:selected').first().val();
      var value = $('input#criterion-value:first').val();
      if (comparator === 'include') {
        criteria.push(column + " like '%" + value + "%'")
      } else {
        if (isNaN(value)) {
          value = "'" + value + "'";
        }
        criteria.push(column + ' ' + comparator + ' ' + value);
      }
      $('#criteria-statement span').replaceWith($('<span>').html(criteria.join(' and ')));
    }
  }

  function selectOne() {
    var selectedColumns = $('#columns');
    var criterionColumns = $('#criterion-column');
    $('#table-columns').find('option:selected').each(function() {
      var opt = $(this);
      $('<option>').val(opt.val()).text(opt.text()).appendTo(selectedColumns);
      $('<option>').val(opt.val()).text(opt.text()).appendTo(criterionColumns);
      opt.remove();
    });
  }

  function unselectOne() {
    var columns = $('#table-columns');
    $('#columns').find('option:selected').each(function() {
      var opt = $(this);
      $('<option>').val(opt.val()).text(opt.text()).appendTo(columns);
      opt.remove();
      $('#criterion-column').find('option [value=' + opt.val() + ']').remove();
    });
    cleanUpCriteria();
  }

  function selectAll() {
    var selectedColumns = $('#columns');
    var criterionColumns = $('#criterion-column');
    $('#table-columns').find('option').each(function() {
      var opt = $(this);
      $('<option>').val(opt.val()).text(opt.text()).appendTo(selectedColumns);
      $('<option>').val(opt.val()).text(opt.text()).appendTo(criterionColumns);
      opt.remove();
    });
  }

  function unselectAll() {
    var selectedColumns = $('#table-columns');
    $('#columns').find('option').each(function() {
      var opt = $(this);
      $('<option>').val(opt.val()).text(opt.text()).appendTo(selectedColumns);
      opt.remove();
      $('#criterion-column').find('option [value=' + opt.val() + ']').remove();
    });
    cleanUpCriteria();
  }

  (function() {
    var selectedTable = $('#table').find('option:first');
    selectedTable.prop('selected', true);
    reloadColumns(selectedTable.val());
    $('#criterion-comparator').find('option:first').prop('selected', true);
  })();

</script>