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
<%@ page import="java.security.Principal" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.PrincipalComparator" %>
<%@ page import="com.ecyrd.jspwiki.auth.authorize.Group" %>
<%@ page import="com.ecyrd.jspwiki.auth.authorize.GroupManager" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<view:setBundle basename="templates.default"/>
<%
  // Extract the group name and members
  String name = request.getParameter( "group" );

  Group group = (Group)pageContext.getAttribute( "Group",PageContext.REQUEST_SCOPE );
  Principal[] members = null;

  if ( group != null )
  {
    name = group.getName();
    members = group.members();
    Arrays.sort( members, new PrincipalComparator() );
  }

  // FIXME : find better way to i18nize default group name
  if ( "MyGroup".equals(name) )
  {
	  name = LocaleSupport.getLocalizedMessage(pageContext, "newgroup.defaultgroupname");
  }

  name = TextUtil.replaceEntities(name);
%>

<wiki:TabbedSection defaultTab="${param.tab}">
  <wiki:Tab id="logincontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "newgroup.heading.create")%>'>

<h3><fmt:message key="newgroup.heading.create"/></h3>

<wiki:Messages div='error' topic='<%=GroupManager.MESSAGES_KEY%>' prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"newgroup.errorprefix")%>' />

  <form id="createGroup" action="<wiki:Link format="url" jsp="NewGroup.jsp"/>"
    method="POST" accept-charset="UTF-8">

  <div class="formhelp">
     <fmt:message key="newgroup.instructions.start"/>
  </div>

  <table class="wikitable">
    <!-- Name -->
    <tr>
      <th><label><fmt:message key="newgroup.name"/></label></th>
      <td><input type="text" name="group" size="30" value="<%=name%>" />
      <div class="formhelp">
        <fmt:message key="newgroup.name.description"/>
      </div>
      </td>
    </tr>

    <!-- Members -->
    <%
      StringBuffer s = new StringBuffer();
      for ( int i = 0; i < members.length; i++ )
      {
        s.append( members[i].getName().trim() );
        s.append( '\n' );
      }
    %>
    <tr>
      <th><label><fmt:message key="group.members"/></label></th>
      <td><textarea id="members" name="members" rows="20" cols="40"><%=TextUtil.replaceEntities(s.toString())%></textarea>
      <div class="formhelp">
        <fmt:message key="newgroup.members.description"/>
      </div>
      </td>
    </tr>
    </table>
    <input type="submit" name="ok" value="<fmt:message key="newgroup.creategroup"/>" />
    <input type="hidden" name="action" value="save" />
    <div class="formhelp">
         <fmt:message key="newgroup.instructions.end"/>
    </div>
  </form>


</wiki:Tab>
</wiki:TabbedSection>