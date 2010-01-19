<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html id="top" xmlns="http://www.w3.org/1999/xhtml">

  <head>
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

    <div id="wikibody" class="${prefs['orientation']}">

      <wiki:Include page="Header.jsp" />
      <div id="content">
        <div id="page">
          <wiki:Include page="PageActionsTop.jsp"/>
          <wiki:Content/>
          <wiki:Include page="PageActionsBottom.jsp"/>
        </div>
        <wiki:Include page="Favorites.jsp"/>
        <div class="clearbox"></div>
      </div>
    </div>
    <wiki:Include page="Footer.jsp" />
  </body>
</html>