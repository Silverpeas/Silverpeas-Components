<%@ tag import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ tag import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ tag import="org.silverpeas.components.kmelia.SearchContext" %>
<%@ tag import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ tag import="org.silverpeas.core.admin.user.model.User" %><%--
  Copyright (C) 2000 - 2022 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="enabled" required="true"
              type="java.lang.Boolean"
              description="Display this searching zone" %>

<%
  SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
  String query = "";
  PagesContext formContext = new PagesContext();
  final User currentRequester = User.getCurrentRequester();
  formContext.setUserId(currentRequester.getId());
  formContext.setLanguage(currentRequester.getUserPreferences().getLanguage());
  if (searchContext != null) {
    query = searchContext.getQuery();
    formContext = searchContext.getFormContext();
  }

  Form searchForm = (Form) request.getAttribute("ExtraForm");
%>

<fmt:message var="labelSearchButton" key="GML.search"/>
<fmt:message var="labelSearch" key="kmelia.SearchInTopics"/>

<c:if test="${enabled}">
  <div id="searchZone">
    <view:board>
      <table id="searchLine">
        <tr><td><div id="searchLabel">${labelSearch}</div>&nbsp;<input type="text" id="topicQuery" size="50" value="<%=query %>" onkeydown="checkSubmitToSearch(event)"/></td>
          <td><view:button label="${labelSearchButton}" action="javascript:onClick=searchInTopic();"/></td>
        </tr>
      </table>
      <form id="extraFormSearch" action="javaScript:void(0)">
        <% if (searchForm != null) {
          searchForm.display(out, formContext);
        } %>
      </form>
    </view:board>
  </div>
</c:if>
