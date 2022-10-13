<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

<view:setConstant var="paramColumns" constant="org.silverpeas.components.mydb.web.MyDBWebController.ALL_COLUMNS"/>
<view:setConstant var="paramRow" constant="org.silverpeas.components.mydb.web.MyDBWebController.ROW"/>
<view:setConstant var="paramError" constant="org.silverpeas.components.mydb.web.MyDBWebController.ERROR_MESSAGE"/>
<view:setConstant var="nullValue" constant="org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate.NULL_VALUE"/>
<view:setConstant var="emptyString" constant="org.silverpeas.components.mydb.model.predicates.AbstractColumnValuePredicate.EMPTY_VALUE"/>

<c:set var="error" value="${requestScope[paramError]}"/>

<fmt:message var="modifyRow" key="mydb.modifyRow"/>

<fmt:message bundle="${icons}" var="mandatoryIcon" key="mydb.icons.mandatory"/>
<c:url var="mandatoryIcon" value="${mandatoryIcon}"/>
<fmt:message bundle="${icons}" var="primaryKeyIcon" key="mydb.icons.primaryKey"/>
<c:url var="primaryKeyIcon" value="${primaryKeyIcon}"/>
<fmt:message bundle="${icons}" var="foreignKeyIcon" key="mydb.icons.foreignKey"/>
<c:url var="foreignKeyIcon" value="${foreignKeyIcon}"/>
<fmt:message var="emptyInputExplanation" key="mydb.emptyInputExplanation"/>
<fmt:message var="nullExplanation" key="mydb.nullExplanation"/>
<fmt:message bundle="${icons}" var="nullIcon" key="mydb.icons.null"/>
<c:url var="nullIcon" value="${nullIcon}"/>
<fmt:message var="emptyStringExplanation" key="mydb.emptyStringExplanation"/>
<fmt:message bundle="${icons}" var="emptyIcon" key="mydb.icons.empty"/>
<c:url var="emptyIcon" value="${emptyIcon}"/>

<c:choose>
  <c:when test="${silfn:isDefined(error)}">
    <div id="error"><span>${error}</span></div>
  </c:when>
  <c:otherwise>
    <c:set var="displayMandatoryLegend" value="false"/>
    <c:set var="displayPrimaryKeyLegend" value="false"/>
    <c:set var="displayForeignKeyLegend" value="false"/>
    <c:set var="columns" value="${requestScope[paramColumns]}"/>
    <jsp:useBean id="columns" type="java.util.List<org.silverpeas.components.mydb.model.DbColumn>"/>
    <c:set var="row" value="${requestScope[paramRow]}"/>
    <c:set var="creationMode" value="${row == null}"/>
    <c:if test="${!creationMode}">
      <jsp:useBean id="row" type="org.silverpeas.components.mydb.web.TableRowUIEntity"/>
    </c:if>
    <c:set var="formatFieldValue" value="${(f, v) ->
                  (not f.nullable
                  ? v
                  : (f.ofTypeText and f.size > 1 and v.isEmpty()
                    ? (creationMode ? nullValue : emptyString)
                    : (creationMode ? nullValue : v)))}"/>
    <fieldset id="row-edition" class="skinFieldset">
      <legend style="display: none;"></legend>
      <div class="fields oneFieldPerLine">
        <c:forEach var="field" items="${columns}">
          <c:if test="${not field.autoValued}">
            <div class="field" id="field-${field.name}">
              <c:set var="fieldType" value="${field.typeName}"/>
              <c:if test="${field.ofTypeText}">
                <c:set var="fieldType" value="${field.typeName}(${field.size})"/>
              </c:if>
              <c:set var="fieldInputId" value="field-${field.name}-value"/>
              <label for="${fieldInputId}" class="txtlibform">${field.name}&nbsp;<span><small>(<em>${fieldType}</em>)</small></span></label>
              <div class="champs">
                <c:set var="foreignKeyOpening" value=""/>
                <c:set var="readOnlyAttr" value=""/>
                <c:set var="fieldClass"   value="field-value-input"/>
                <c:set var="fieldValue" value=""/>
                <c:set var="fieldRefColumn" value=""/>
                <c:choose>
                  <c:when test="${creationMode}">
                    <c:if test="${field.defaultValueDefined}">
                      <c:set var="fieldValue" value="${field.defaultValue}"/>
                    </c:if>
                  </c:when>
                  <c:otherwise>
                    <c:set var="fieldValue" value="${row.data.getFieldValue(field.name)}"/>
                    <jsp:useBean id="fieldValue" type="org.silverpeas.components.mydb.model.TableFieldValue"/>
                  </c:otherwise>
                </c:choose>
                <c:if test="${field.foreignKey}">
                  <c:set var="readOnlyAttr" value="readonly"/>
                  <c:set var="foreignKeyName" value="${field.foreignKeyName}"/>
                  <c:set var="foreignKeyOpening" value="window.openForeignKey('${foreignKeyName}', '${field.referencedTable}', '${field.referencedColumn}')"/>
                  <c:set var="displayForeignKeyLegend" value="true"/>
                  <c:set var="fieldClass" value="${fieldClass} foreign-key-value field-fk-${foreignKeyName}"/>
                  <c:set var="fieldRefColumn" value="field-fk-refcolumn-${field.referencedColumn}"/>
                </c:if>
                <c:if test="${not field.nullable}">
                  <c:set var="fieldClass" value="${fieldClass} mandatory"/>
                  <c:set var="displayMandatoryLegend" value="true"/>
                </c:if>
                <c:choose>
                  <c:when test="${field.ofTypeText and field.size / 100 > 1}">
                    <textarea id="${fieldInputId}" name="${field.name}" class="${fieldClass}"
                              cols="100" rows="${field.size / 100}"
                              maxlength="${field.size}" ${readOnlyAttr}
                              onclick="${foreignKeyOpening}"
                              rel="${fieldRefColumn}">${formatFieldValue(field, fieldValue)}</textarea>
                  </c:when>
                  <c:when test="${not field.ofTypeBinary}">
                    <input id="${fieldInputId}" name="${field.name}" class="${fieldClass}"
                           type="text"
                           maxlength="${field.size}" ${readOnlyAttr}
                           value="${formatFieldValue(field, fieldValue)}"
                           onclick="${foreignKeyOpening}"
                           rel="${fieldRefColumn}"/>
                  </c:when>
                </c:choose>
                <c:if test="${field.foreignKey}">
                  <a href="javascript:${foreignKeyOpening}" title="${field.referencedTable}"><img src="${foreignKeyIcon}" width="10" height="10" alt=""/></a>
                </c:if>
                <c:if test="${field.primaryKey}">
                  <c:set var="displayPrimaryKeyLegend" value="true"/>
                  <span><img src="${primaryKeyIcon}" width="10" height="10" alt=""/></span>
                </c:if>
                <c:if test="${not field.nullable}">
                  <span><img src="${mandatoryIcon}" width="5" height="5" alt=""/></span>
                </c:if>
                <c:if test="${field.nullable}">
                  <span><a href="javascript:window.setNullValue('${field.name}')" title="${nullExplanation}"><img src="${nullIcon}" width="10" height="10" alt="${nullExplanation}"/></a></span>
                  <c:if test="${field.ofTypeText and field.size > 1}">
                    <span><a href="javascript:window.setEmptyValue('${field.name}')" title="${emptyStringExplanation}"><img src="${emptyIcon}" width="10" height="10" alt="${emptyStringExplanation}"/></a></span>
                  </c:if>
                </c:if>
              </div>
            </div>
          </c:if>
        </c:forEach>
      </div>
    </fieldset>
    <div class="legend">
      <span><small>${emptyInputExplanation}</small></span>
      <c:if test="${displayMandatoryLegend}">
        <div>
          <img alt="mandatory" src="${mandatoryIcon}" width="5" height="5"/>&nbsp;
          <fmt:message key='GML.requiredField'/>
        </div>
      </c:if>
      <c:if test="${displayPrimaryKeyLegend}">
        <div>
          <img alt="primary key" src="${primaryKeyIcon}" width="10" height="10"/>&nbsp;
          <fmt:message key='mydb.primaryKey'/>
        </div>
      </c:if>
      <c:if test="${displayForeignKeyLegend}">
        <div>
          <img alt="foreign key" src="${foreignKeyIcon}" width="10" height="10"/>&nbsp;
          <fmt:message key='mydb.foreignKey'/>
        </div>
      </c:if>
    </div>
  </c:otherwise>
</c:choose>