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

<fmt:message var="theLabel" key='GML.date.the'/>
<fmt:message var="fromLabel" key='GML.date.from'/>
<fmt:message var="toLabel" key='GML.to'/>

<div class="replacement-item">
  <div class="substitute">{{replacement.substitute.fullName}}</div>
  <div class="incumbent">{{replacement.incumbent.fullName}}</div>
  <div class="period">
    <span v-if="isOneDay" key="on-day">${theLabel} </span>
    <span v-else key="on-day">${fromLabel} </span>
    <span class="date">{{replacement.startDate | displayAsDate}}</span>
    <span v-if="!isOneDay">${toLabel} </span>
    <span v-if="!isOneDay" class="date">{{replacement.endDate | displayAsDate}}</span>
  </div>
</div>
