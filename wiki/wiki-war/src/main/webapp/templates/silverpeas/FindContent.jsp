<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.lang.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<wiki:TabbedSection>
<wiki:Tab id="findcontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "find.tab")%>' accesskey="s">

<form action="<wiki:Link format='url' jsp='Search.jsp'/>"
       class="wikiform"
          id="searchform2"
         accept-charset="<wiki:ContentEncoding/>">

  <h4><fmt:message key="find.input" /></h4>
  <p>
    <input type="text"
           name="query" id="query2" 
          value="<c:out value='${query}'/>" 
           size="32" />

    <input type="checkbox" name="details" id="details" <c:if test='${param.details == "on"}'>checked='checked'</c:if> />
    <fmt:message key="find.details" />

    <select name="scope" id="scope" > 
      <option value="" <c:if test="${empty param.scope}">selected="selected"</c:if> ><fmt:message key='find.scope.all' /></option>
      <option value="author:" <c:if test='${param.scope eq "author:"}'>selected="selected"</c:if> ><fmt:message key='find.scope.authors' /></option>
      <option value="name:" <c:if test='${param.scope eq "name:"}'>selected="selected"</c:if> ><fmt:message key='find.scope.pagename' /></option>
      <option value="contents:" <c:if test='${param.scope eq "contents:"}'>selected="selected"</c:if> ><fmt:message key='find.scope.content' /></option>
      <option value="attachment:" <c:if test='${param.scope eq "attachment:"}'>selected="selected"</c:if> ><fmt:message key='find.scope.attach' /></option>       
    </select>

    <c:set var="findButton"><fmt:message key="find.submit.find"/></c:set>
    <c:set var="goButton"><fmt:message key="find.submit.go"/></c:set>
	<input type="submit" name="ok" id="ok" value="${findButton}" />
	<input type="submit" name="go" id="go" value="${goButton}" />
    <input type="hidden" name="start" id="start" value="0" />
    <input type="hidden" name="maxitems" id="maxitems" value="20" />

    <span id="spin" class="spin" style="position:absolute;display:none;"></span>
  </p>
</form>

<div id="searchResult2" ><wiki:Include page="AJAXSearch.jsp"/></div>

</wiki:Tab>

<wiki:PageExists page="SearchPageHelp">
<wiki:Tab id="findhelp" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "find.tab.help")%>' accesskey="h">
  <wiki:InsertPage page="SearchPageHelp"/>
</wiki:Tab>
</wiki:PageExists>

</wiki:TabbedSection>