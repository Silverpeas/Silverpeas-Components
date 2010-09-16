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
<%@ page import="com.ecyrd.jspwiki.tags.InsertDiffTag" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<% 
  WikiContext c = WikiContext.findContext( pageContext );  
  List history = c.getEngine().getVersionHistory(c.getPage().getName());
  pageContext.setAttribute( "history", history );
  pageContext.setAttribute( "diffprovider", c.getEngine().getVariable(c,"jspwiki.diffProvider"));
 %>

<wiki:PageExists>
<form action="<wiki:Link jsp='Diff.jsp' format='url' />" method="get" accept-charset="UTF-8">
<div class="collapsebox" id="diffcontent">
  <h4>
       <input type="hidden" name="page" value="<wiki:Variable var='pagename' />" />
       <fmt:message key="diff.difference">
         <fmt:param>
           <select id="r1" name="r1" onchange="this.form.submit();" >
           <c:forEach items="${history}" var="i">
             <option value="<c:out value='${i.version}'/>" <c:if test="${i.version == olddiff}">selected="selected"</c:if> ><c:out value="${i.version}"/></option>
           </c:forEach>
           </select>
         </fmt:param>
         <fmt:param>
           <select id="r2" name="r2" onchange="this.form.submit();" >
           <c:forEach items="${history}" var="i">
             <option value="<c:out value='${i.version}'/>" <c:if test="${i.version == newdiff}">selected="selected"</c:if> ><c:out value="${i.version}"/></option>
           </c:forEach>
           </select>
         </fmt:param>
       </fmt:message>
  </h4>

  <c:if test='${diffprovider eq "ContextualDiffProvider"}' >
    <div class="diffnote">
      <a href="#change-1" title="<fmt:message key='diff.gotofirst.title'/>" class="diff-nextprev" >
         <fmt:message key="diff.gotofirst"/>
      </a>&raquo;&raquo;
    </div>
  </c:if>

  <div class="diffbody">
    <wiki:InsertDiff><i><fmt:message key="diff.nodiff"/></i></wiki:InsertDiff> 
  </div>
</div>
</form>
</wiki:PageExists>