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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%
  WikiContext c = WikiContext.findContext(pageContext);
%>
<div class="userbox">

  <wiki:UserCheck status="anonymous">
    <span class="username anonymous">
      <fmt:message key="fav.greet.anonymous" />
    </span>
  </wiki:UserCheck>
  <wiki:UserCheck status="asserted">
    <span class="username asserted">
      <fmt:message key="fav.greet.asserted">
      <fmt:param><wiki:Translate>[<wiki:UserName />]</wiki:Translate></fmt:param>
    </fmt:message>
    </span>
  </wiki:UserCheck>
  <wiki:UserCheck status="authenticated">
    <span class="username authenticated">
      <fmt:message key="fav.greet.authenticated">
        <fmt:param><wiki:Translate>[<wiki:UserName />]</wiki:Translate></fmt:param>
      </fmt:message>
    </span>
  </wiki:UserCheck>

  <%-- action buttons --%>
  <wiki:UserCheck status="notAuthenticated">
  <wiki:CheckRequestContext context='!login'>
    <wiki:Permission permission="login">
      <a href="<wiki:Link jsp='Login.jsp' format='url'><wiki:Param 
         name='redirect' value='<%=c.getPage().getName()%>'/></wiki:Link>" 
        class="action login"
        title="<fmt:message key='actions.login.title'/>"><fmt:message key="actions.login"/></a>
    </wiki:Permission>
  </wiki:CheckRequestContext>
  </wiki:UserCheck>
  
  <wiki:UserCheck status="authenticated">
   <a href="<wiki:Link jsp='Logout.jsp' format='url' />" 
     class="action logout"
     title="<fmt:message key='actions.logout.title'/>"><fmt:message key="actions.logout"/></a>
   <%--onclick="return( confirm('<fmt:message key="actions.confirmlogout"/>') && (location=this.href) );"--%>
  </wiki:UserCheck>

  <wiki:CheckRequestContext context='!prefs'>
  <wiki:CheckRequestContext context='!preview'>
    <a href="<wiki:Link jsp='UserPreferences.jsp' format='url' ><wiki:Param name='redirect'
      value='<%=c.getPage().getName()%>'/></wiki:Link>"
      class="action prefs" accesskey="p"
      title="<fmt:message key='actions.prefs.title'/>"><fmt:message key="actions.prefs" />
    </a>
  </wiki:CheckRequestContext>
  </wiki:CheckRequestContext>

  <div class="clearbox"></div>

</div>