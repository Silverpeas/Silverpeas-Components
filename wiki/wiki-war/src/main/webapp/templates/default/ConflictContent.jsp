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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<view:setBundle basename="templates.default"/>
<wiki:TabbedSection >

<wiki:Tab id="conflict" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "conflict.oops.title")%>'>
  <div class="error"><fmt:message key="conflict.oops" /></div>
  <fmt:message key="conflict.goedit" >
    <fmt:param><wiki:EditLink><wiki:PageName /></wiki:EditLink></fmt:param>
  </fmt:message>
</wiki:Tab>
 
<wiki:Tab id="conflictOther" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "conflict.modified")%>' >
  <tt><%=pageContext.getAttribute("conflicttext",PageContext.REQUEST_SCOPE)%></tt>      
</wiki:Tab>
 
<wiki:Tab id="conflictOwn" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "conflict.yourtext")%>' >
  <tt><%=pageContext.getAttribute("usertext",PageContext.REQUEST_SCOPE)%></tt>
</wiki:Tab>

</wiki:TabbedSection>