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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.processManager.multilang.processManagerBundle"/>

<fmt:message var="addReplacementLabel" key="processManager.replacements.add"/>
<fmt:message var="modifyReplacementLabel" key="processManager.replacements.modify"/>
<c:set var="deleteMessage"><fmt:message key='processManager.replacements.delete.confirmation'>
  <fmt:param value="{{replacement.incumbent ? replacement.incumbent.fullName : ''}}"/>
  <fmt:param value="{{replacement.substitute ? replacement.substitute.fullName : ''}}"/>
</fmt:message></c:set>

<div>
  <!-- CREATE POPIN -->
  <silverpeas-popin
      v-on:api="addPopinApi = $event"
      title="${addReplacementLabel}">
    <silverpeas-form-pane v-on:api="addFormApi = $event"
                          v-bind:manualActions="true">
      <workflow-replacement-form
          v-bind:replacement="replacement"></workflow-replacement-form>
    </silverpeas-form-pane>
  </silverpeas-popin>
  <!-- MODIFY POPIN -->
  <silverpeas-popin
      v-on:api="modifyPopinApi = $event"
      title="${modifyReplacementLabel}">
    <silverpeas-form-pane v-on:api="modifyFormApi = $event"
                          v-bind:manualActions="true">
      <workflow-replacement-form
          v-bind:replacement="replacement"></workflow-replacement-form>
    </silverpeas-form-pane>
  </silverpeas-popin>
  <!-- DELETE POPIN -->
  <silverpeas-popin
      v-on:api="deletePopinApi = $event"
      type="confirmation">
    <span>${deleteMessage}</span>
  </silverpeas-popin>
</div>