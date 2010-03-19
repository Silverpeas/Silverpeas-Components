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
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="templates.default"/>
<view:setBundle basename="com.silverpeas.wiki.settings.wikiIcons" var="icons" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<% response.setContentType("text/plain; charset=UTF-8"); %>
<html>
  <head>
    <view:looknfeel />
  </head>
  <body>
    <c:set var="pageName"><wiki:PageName /></c:set>
    <%
      WikiContext c = WikiContext.findContext(pageContext); %>
    <c:set var="wiki_link_raw"><%=c.getURL(WikiContext.VIEW, "")%></c:set>
    <c:set var="wiki_link" value="${fn:substringBefore(wiki_link_raw, '?') }" />
    <view:browseBar>
      <view:browseBarElt link="${wiki_link}" label="${pageName}" />
    </view:browseBar>
    <%@ include file="../silverpeas/PageActionsTop.jsp"%>
    <view:window>  
      <pre>
        <wiki:InsertPage mode="plain"/>
      </pre>
    </view:window>  
  </body>
</html>