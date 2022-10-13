<%@ tag import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ tag import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ tag import="org.silverpeas.components.kmelia.SearchContext" %>
<%@ tag import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ tag import="org.silverpeas.core.admin.user.model.User" %>
<%@ tag import="org.silverpeas.core.util.DateUtil" %><%--
  Copyright (C) 2000 - 2021 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="newsletters" required="true"
              type="java.util.List<org.silverpeas.components.infoletter.model.InfoLetterPublication>"
              description="List of newsletter" %>

<%@ attribute name="readonly" required="false"
              type="java.lang.Boolean"
              description="If read only, no modification action are provided. False by default." %>
<c:if test="${readonly == null}">
  <c:set var="readonly" value="${false}"/>
</c:if>

<fmt:message key="GML.delete" var="deleteLabel"/>

<ul class="list-infoletter">
  <c:forEach var="newsletter" items="${newsletters}" varStatus="status">
    <jsp:useBean id="newsletter" type="org.silverpeas.components.infoletter.model.InfoLetterPublication"/>
    <c:set var="id" value="${newsletter.getPK().id}"/>
    <c:set var="openCallback" value="open${newsletter._isValid() ? 'View' : 'Edit'}Parution('${id}')"/>
    <li onclick="${openCallback}">
      <a class="title" href="javascript:${openCallback}">
          ${newsletter.title}
        <c:if test="${not empty newsletter.parutionDate}">
          <c:set var="parutionDate" value="<%=DateUtil.parse(newsletter.getParutionDate())%>"/>
          <span class="date ng-binding">${silfn:formatDate(parutionDate, userLanguage)}</span>
        </c:if>
      </a>
      <div class="newsletter-thumb">
        <iframe src="ViewInlinedCssHtml?id=${id}" width="500" height="1000"></iframe>
      </div>
      <c:if test="${not readonly}">
        <a class="delete-button"
           href="javascript:void(0)"
           onclick="event.stopPropagation();deleteNewsletter('${id}')" title="${deleteLabel}">${deleteLabel}</a>
      </c:if>
    </li>
  </c:forEach>
</ul>