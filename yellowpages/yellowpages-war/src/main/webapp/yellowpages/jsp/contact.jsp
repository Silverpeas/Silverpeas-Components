<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.contact.model.CompleteContact" %>
<%@page import="org.silverpeas.core.contact.model.ContactDetail" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/yellowpages" prefix="yellowpagesTags" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="org.silverpeas.yellowpages.multilang.yellowpagesBundle" />

<%@ include file="checkYellowpages.jsp" %>

<%
  CompleteContact fullContact = (CompleteContact) request.getAttribute("Contact");
  ContactDetail contact = fullContact.getContactDetail();

  Form formView = fullContact.getViewForm();
  PagesContext context = (PagesContext) request.getAttribute("PagesContext");
%>

<c:set var="fullPage" value="${requestScope['FullPage']}"/>

<c:choose>
  <c:when test="${fullPage}">
    <view:sp-page>
    <view:sp-head-part withFieldsetStyle="true"/>
    <view:sp-body-part>
      <view:window popup="false">
        <yellowpagesTags:contactView contact="<%=contact%>" userLanguage="<%=resources.getLanguage()%>" formView="<%=formView%>" context="<%=context%>"/>
        <view:buttonPane>
          <fmt:message key="GML.back" var="labelBack"/>
          <view:button label="${labelBack}" action="javascript:history.back()"/>
        </view:buttonPane>
      </view:window>
    </view:sp-body-part>
    </view:sp-page>
  </c:when>
  <c:otherwise>
    <view:link href="/util/styleSheets/fieldset.css"/>
    <yellowpagesTags:contactView contact="<%=contact%>" userLanguage="<%=resources.getLanguage()%>" formView="<%=formView%>" context="<%=context%>"/>
  </c:otherwise>
</c:choose>