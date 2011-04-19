<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<a class="txtlibform" id="legendLabelId"><fmt:message key="projectManager.gantt.legend" /></a>
<ul id="legende">
  <li><div class="task not_started">&nbsp;</div><fmt:message key="projectManager.gantt.view.tasks.notstarted" /></li>
  <li><div class="task in_progress">&nbsp;</div><fmt:message key="projectManager.gantt.view.tasks.inprogress" /></li>
  <li><div class="task done">&nbsp;</div><fmt:message key="projectManager.gantt.view.tasks.done" /></li>
  <li><div class="task frost">&nbsp;</div><fmt:message key="projectManager.gantt.view.tasks.frozen" /></li>
  <li><div class="task lost">&nbsp;</div><fmt:message key="projectManager.gantt.view.tasks.cancel" /></li>
  <li><div class="task warning">&nbsp;</div><fmt:message key="projectManager.gantt.view.tasks.alert" /></li>    
</ul>
