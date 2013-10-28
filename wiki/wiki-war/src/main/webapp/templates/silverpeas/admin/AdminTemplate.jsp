<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.admin.*" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<html>
<head>
<title>JSPWiki administration</title>
  <wiki:Include page="commonheader.jsp"/>
  <link rel="stylesheet" media="screen, projection, print" type="text/css" 
        href="<wiki:Link format='url' templatefile='admin/admin.css'/>"/>
</head>
<body class="view">
<div id="wikibody">
<h1>JSPWiki Administration</h1>
<div class="information">Not all things can be configured here.  Some things need to be configured
in your <tt>jspwiki.properties</tt> file.</div>

<%
    WikiEngine wiki = WikiEngine.getInstance( getServletConfig() );
    WikiContext ctx = WikiContext.findContext(pageContext);
    AdminBeanManager mgr = wiki.getAdminBeanManager();
 %>

<wiki:TabbedSection defaultTab="${param['tab-admin']}">

<wiki:Tab id="core" title="Core">
<p>Contains core setup options.</p>
   <wiki:TabbedSection defaultTab="${param['tab-core']}">

     <wiki:AdminBeanIterator type="core" id="ab">
      <wiki:Tab id="${ab.id}" title="${ab.title}">
      
      <div class="formcontainer">
      <form action="Admin.jsp" method="post" accept-charset="UTF-8">
        <input type="hidden" name="tab-admin" value="core"/>
        <input type="hidden" name="tab-core" value="${ab.title}" />
        <input type="hidden" name="bean" value="${ab.id}" />
        <%
         out.write( ab.doGet(ctx) );
         %>
       </form>
       </div>
      </wiki:Tab>
     </wiki:AdminBeanIterator>
   </wiki:TabbedSection>
</wiki:Tab>

<wiki:Tab id="users" title="Users">
   <wiki:Include page="admin/UserManagement.jsp"/>
</wiki:Tab>

<wiki:Tab id="groups" title="Groups">
   <div>
   <p>This is a list of all groups in this wiki.  If you click on the group name,
   you will be taken to the administration page of that particular group.</p>
   <p>
   <wiki:Plugin plugin="Groups"/>
   </p>
   </div>
</wiki:Tab>


<wiki:Tab id="editors" title="Editors">
   <wiki:TabbedSection defaultTab="${param['tab-editors']}">
     <wiki:AdminBeanIterator type="editors" id="ab">
      <wiki:Tab id="${ab.id}" title="${ab.title}">
      
      <div class="formcontainer"> 
      <form action="Admin.jsp" method="post" accept-charset="UTF-8">
         <input type="hidden" name="tab-admin" value="editors"/>
         <input type="hidden" name="tab-editors" value="${ab.title}" />
         <%
         out.write( ab.doGet(ctx) );
         %>
       </form>
       </div>
      </wiki:Tab>
     </wiki:AdminBeanIterator>
   </wiki:TabbedSection>
</wiki:Tab>
<wiki:Tab id="filters" title="Filters">
<p>There will be more filter stuff here</p>
</wiki:Tab>
</wiki:TabbedSection>

</div>
</body>