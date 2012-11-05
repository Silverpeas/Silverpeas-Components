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
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %><%--CHECK why is this needed --%>
<view:setBundle basename="templates.default"/>
<%
	WikiContext c = WikiContext.findContext( pageContext );
   	WikiPage p = c.getPage();
	String pagename = p.getName();

	/* check possible permalink (blogentry) pages */
	String blogcommentpage="";
	String mainblogpage="";
	if( pagename.indexOf("_blogentry_") != -1 )
	{
		blogcommentpage = TextUtil.replaceString( pagename, "blogentry", "comments" );
		mainblogpage = pagename.substring(0, pagename.indexOf("_blogentry_"));
	}
%>

<%-- If the page is an older version, then offer a note and a possibility
     to restore this version as the latest one. --%>
<wiki:CheckVersion mode="notlatest">
  <form action="<wiki:Link format='url' jsp='Wiki.jsp'/>" 
        method="get"  accept-charset='UTF-8'>

    <input type="hidden" name="page" value="<wiki:Variable var='pagename' />" />     
    <div class="warning">
      <fmt:message key="view.oldversion">
        <fmt:param>
          <%--<wiki:PageVersion/>--%>
          <select id="version" name="version" onchange="this.form.submit();" >
<% 
   int latestVersion = c.getEngine().getPage( pagename, WikiProvider.LATEST_VERSION ).getVersion();
   int thisVersion = p.getVersion();

   if( thisVersion == WikiProvider.LATEST_VERSION ) thisVersion = latestVersion; //should not happen
     for( int i = 1; i <= latestVersion; i++) 
     {
%> 
          <option value="<%= i %>" <%= ((i==thisVersion) ? "selected='selected'" : "") %> ><%= i %></option>
<%
     }    
%>
          </select>
        </fmt:param>
      </fmt:message>  
      <br />
      <wiki:LinkTo><fmt:message key="view.backtocurrent"/></wiki:LinkTo>&nbsp;&nbsp;
      <wiki:EditLink version="this"><fmt:message key="view.restore"/></wiki:EditLink>
    </div>

  </form>
</wiki:CheckVersion>

<%-- Inserts no text if there is no page. --%>
<wiki:InsertPage />

<%-- Inserts blogcomment if appropriate 
<% if( !blogpage.equals("") ) { %>
--%>

<% if( ! mainblogpage.equals("") ) { %>
<wiki:PageExists page="<%= mainblogpage%>">

  <% if( ! blogcommentpage.equals("") ) { %>
  <wiki:PageExists page="<%= blogcommentpage%>">
	<div class="weblogcommentstitle"><fmt:message key="blog.commenttitle"/></div>
    <div class="weblogcomments"><wiki:InsertPage page="<%= blogcommentpage%>" /></div>
  </wiki:PageExists>
  <% }; %>
  <div class="information">	
	<wiki:Link page="<%= mainblogpage %>"><fmt:message key="blog.backtomain"/></wiki:Link>&nbsp; &nbsp;
	<wiki:Link context="comment" page="<%= blogcommentpage%>" ><fmt:message key="blog.addcomments"/></wiki:Link>
  </div>

</wiki:PageExists>
<% }; %>

<wiki:NoSuchPage>
  <%-- FIXME: Should also note when a wrong version has been fetched. --%>
  <div class="information" >
  <fmt:message key="common.nopage">
    <fmt:param><wiki:EditLink><fmt:message key="common.createit"/></wiki:EditLink></fmt:param>
  </fmt:message>
  </div>
</wiki:NoSuchPage>