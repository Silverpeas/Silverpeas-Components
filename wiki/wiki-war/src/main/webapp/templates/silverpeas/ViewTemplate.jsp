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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="templates.default"/>
<view:setBundle basename="com.silverpeas.wiki.settings.wikiIcons" var="icons" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html id="top" xmlns="http://www.w3.org/1999/xhtml">

  <head>
    <view:looknfeel />
    <title>
      <fmt:message key="view.title.view">
        <fmt:param><wiki:Variable var="ApplicationName" /></fmt:param>
        <fmt:param><wiki:PageName /></fmt:param>
      </fmt:message>
    </title>
    <wiki:Include page="commonheader.jsp"/>
    <wiki:CheckVersion mode="notlatest">
      <meta name="robots" content="noindex,nofollow" />
    </wiki:CheckVersion>
    <wiki:CheckRequestContext context="diff|info">
      <meta name="robots" content="noindex,nofollow" />
    </wiki:CheckRequestContext>
    <wiki:CheckRequestContext context="!view">
      <meta name="robots" content="noindex,follow" />
    </wiki:CheckRequestContext>
  </head>

  <body class="view">
    <%
  WikiContext c = WikiContext.findContext(pageContext); %>
    <c:set var="wiki_link_raw"><%=c.getURL(WikiContext.VIEW, "")%></c:set>
    <c:set var="wiki_link" value="${fn:substringBefore(wiki_link_raw, '?') }" />
    <c:set var="deleteConfirm"><fmt:message key="info.confirmdelete" /></c:set>
    <form action="<wiki:Link format='url' context='<%=WikiContext.DELETE%>' />"
          class="wikiform"
          id="deleteForm"
          method="post" accept-charset="<wiki:ContentEncoding />"
          onsubmit="return( confirm('${deleteConfirm}') && Wiki.submitOnce(this) );">
          <input type="hidden" name="delete-all" value="1"/>
    </form>

    <c:set var="pageName"><wiki:PageName /></c:set>
    <view:browseBar>
      <view:browseBarElt link="${wiki_link}" label="${pageName}" />
    </view:browseBar>
    <%@ include file="PageActionsTop.jsp"%>
    <view:window>
      <div id="wikibody" class="${prefs['orientation']}">

        <wiki:Include page="Header.jsp" />
        <div id="content">
          <div id="page">
            <wiki:Content/>
            <wiki:Include page="PageActionsBottom.jsp"/>
          </div>
          <wiki:Include page="Favorites.jsp"/>
          <div class="clearbox"></div>
        </div>
      </div>
      <wiki:Include page="Footer.jsp" />
    </view:window>
  </body>
</html>