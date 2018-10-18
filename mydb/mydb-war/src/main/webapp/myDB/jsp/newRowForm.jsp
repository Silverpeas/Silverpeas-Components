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
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant var="paramColumns" constant="org.silverpeas.components.mydb.web.MyDBWebController.ALL_COLUMNS"/>
<view:setConstant var="paramError" constant="org.silverpeas.components.mydb.web.MyDBWebController.ERROR_MESSAGE"/>

<c:set var="error" value="${requestScope[paramError]}"/>

<fmt:message var="modifyRow" key="mydb.modifyRow"/>

<fmt:message bundle="${icons}" var="mandatoryIcon" key="mydb.icons.mandatory"/>
<c:url var="mandatoryIcon" value="${mandatoryIcon}"/>
<fmt:message bundle="${icons}" var="primaryKeyIcon" key="mydb.icons.primaryKey"/>
<c:url var="primaryKeyIcon" value="${primaryKeyIcon}"/>
<fmt:message bundle="${icons}" var="foreignKeyIcon" key="mydb.icons.foreignKey"/>
<c:url var="foreignKeyIcon" value="${foreignKeyIcon}"/>

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
    <fieldset id="row-edition" class="skinFieldset">
      <div class="fields oneFieldPerLine">
        <c:forEach var="field" items="${columns}">
          <c:if test="${not field.autoValued}">
            <div class="field" id="field-${field.name}">
              <c:set var="fieldType" value="${field.typeName}"/>
              <c:if test="${field.ofTypeText}">
                <c:set var="fieldType" value="${field.typeName}(${field.size})"/>
              </c:if>
              <label for="field-${field.name}-value" class="txtlibform">${field.name}&nbsp;<span><small>(<i>${fieldType}</i>)</small></span></label>
              <div class="champs">
                <c:set var="readOnlyAttr" value=""/>
                <c:set var="fieldClass"   value=""/>
                <c:set var="fieldValue"   value=""/>
                <c:if test="${field.foreignKey}">
                  <c:set var="readOnlyAttr" value="readonly"/>
                </c:if>
                <c:if test="${not field.nullable}">
                  <c:set var="fieldClass" value='class="mandatory"'/>
                  <c:set var="displayMandatoryLegend" value="true"/>
                </c:if>
                <c:if test="${field.defaultValueDefined}">
                  <c:set var="fieldValue" value="${field.defaultValue}"/>
                </c:if>
                <c:choose>
                  <c:when test="${field.ofTypeText and field.size / 100 > 1}">
                    <textarea id="field-${field.name}-value" name="${field.name}" ${fieldClass} cols="100" rows="${field.size / 100}" maxlength="${field.size}" ${readOnlyAttr}>${fieldValue}</textarea>
                  </c:when>
                  <c:when test="${not field.ofTypeBinary}">
                    <input id="field-${field.name}-value" name="${field.name}" ${fieldClass} type="text" maxlength="${field.size}" ${readOnlyAttr} value="${fieldValue}"/>
                  </c:when>
                </c:choose>
                <c:if test="${field.foreignKey}">
                  <c:set var="displayForeignKeyLegend" value="true"/>
                  <a href="javascript:window.openForeignKey('${field.referencedTable}', '${field.name}')"><img border="0" src="${foreignKeyIcon}" width="10" height="10"/></a>
                </c:if>
                <c:if test="${field.primaryKey}">
                  <c:set var="displayPrimaryKeyLegend" value="true"/>
                  <span><img border="0" src="${primaryKeyIcon}" width="10" height="10"/></span>
                </c:if>
                <c:if test="${not field.nullable}">
                  <c:set var="displayMandatoryLegend" value="true"/>
                  <span><img border="0" src="${mandatoryIcon}" width="5" height="5"/></span>
                </c:if>
              </div>
            </div>
          </c:if>
        </c:forEach>
      </div>
    </fieldset>
    <div class="legend">
      <div>
        <span><small><code>@empty@</code></small></span>
      <span><fmt:message key="mydb.emptyStringExplanation"/></span>
      </div>
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
        <img alt="foreign key" src="${foreignKeyIcon}" width="10" height="10"/>&nbsp;
        <fmt:message key='mydb.foreignKey'/>
      </c:if>
    </div>
  </c:otherwise>
</c:choose>