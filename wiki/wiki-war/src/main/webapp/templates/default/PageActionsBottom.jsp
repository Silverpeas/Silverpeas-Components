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
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  WikiPage wikipage = c.getPage();
%>
<wiki:CheckRequestContext context='view|diff|edit|upload|info'>
<div id='actionsBottom' class="pageactions"> 
  <wiki:PageExists>  

    <a href="#top" 
      class="action quick2top" 
      title="<fmt:message key='actions.gototop'/>" >&laquo;</a>

    <wiki:CheckVersion mode="latest">
       <fmt:message key="info.lastmodified">
          <fmt:param><wiki:PageVersion /></fmt:param>
          <fmt:param><wiki:DiffLink version="latest" newVersion="previous"><wiki:PageDate format='${prefs["DateFormat"]}'/></wiki:DiffLink></fmt:param>
          <fmt:param><wiki:Author /></fmt:param>
       </fmt:message>
    </wiki:CheckVersion>

    <wiki:CheckVersion mode="notlatest">
      <fmt:message key="actions.publishedon">
         <fmt:param><wiki:PageDate format='${prefs["DateFormat"]}'/></fmt:param>
         <fmt:param><wiki:Author /></fmt:param>
      </fmt:message>
    </wiki:CheckVersion>

    <a href="<wiki:Link format='url' jsp='rss.jsp'>
               <wiki:Param name='page' value='<%=wikipage.getName()%>'/>
               <wiki:Param name='mode' value='wiki'/>
             </wiki:Link>"
      title="<fmt:message key='info.rsspagefeed.title'>
               <fmt:param><wiki:PageName /></fmt:param>
             </fmt:message>" >
      <img src="<wiki:Link jsp='images/xml.png' format='url'/>" alt="[RSS]"/>
    </a>
  
  </wiki:PageExists>

  <wiki:NoSuchPage><fmt:message key="actions.notcreated"/></wiki:NoSuchPage> 
</div>
</wiki:CheckRequestContext>