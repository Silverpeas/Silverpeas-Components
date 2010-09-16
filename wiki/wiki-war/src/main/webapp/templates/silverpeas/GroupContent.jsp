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
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="java.security.Principal" %>
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.PrincipalComparator" %>
<%@ page import="com.ecyrd.jspwiki.auth.authorize.Group" %>
<%@ page import="com.ecyrd.jspwiki.auth.authorize.GroupManager" %>
<%@ page import="org.apache.log4j.*" %>
<%@ page errorPage="/Error.jsp" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%!
  Logger log = Logger.getLogger("JSPWiki");
%>

<%
  WikiContext c = WikiContext.findContext( pageContext );

  // Extract the group name and members
  String name = request.getParameter( "group" );
  Group group = (Group)pageContext.getAttribute( "Group",PageContext.REQUEST_SCOPE );
  Principal[] members = null;
  String modified = "";
  String created = "";
  String modifier = "";
  String creator = "";

  if ( group != null )
  {
    name = group.getName();
    members = group.members();
    Arrays.sort( members, new PrincipalComparator() );
    creator = group.getCreator();
    if ( group.getCreated() != null )
    {
      created = group.getCreated().toString();
    }
    modifier = group.getModifier();
    if ( group.getLastModified() != null )
    {
      modified = group.getLastModified().toString();
    }
  }
  name = TextUtil.replaceEntities(name);
%>

<wiki:TabbedSection defaultTab="${param.tab}">
  <wiki:Tab id="viewgroup" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "group.tab")%>'>
  <h3><%=name%></h3>

<%
  if ( group == null )
  {
    if ( c.getWikiSession().getMessages( GroupManager.MESSAGES_KEY ).length == 0 )
    {
%>
    <fmt:message key="group.doesnotexist"/>
    <wiki:Permission permission="createGroups">
      <fmt:message key="group.createsuggestion">
        <fmt:param><wiki:Link jsp="NewGroup.jsp">
                      <wiki:Param name="group" value="<%=name%>" />
                      <wiki:Param name="group" value="<%=name%>" />
                      <fmt:message key="group.createit"/>
                   </wiki:Link>
        </fmt:param>
      </fmt:message>
    </wiki:Permission>
<%
    }
    else
    {
%>
       <wiki:Messages div="error" topic="<%=GroupManager.MESSAGES_KEY%>" prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"group.errorprefix")%>'/>
<%
    }
  }
  else
  {
%>
 <table class="wikitable">
    <tr>
      <th><fmt:message key="group.name"/></th>
      <td>
        <fmt:message key="group.groupintro">
          <fmt:param><em><%=name%></em></fmt:param>
        </fmt:message>
      </td>
    </tr>
    <!-- Members -->
    <tr>
      <th><fmt:message key="group.members"/>
      </th>
      <td><%
            for ( int i = 0; i < members.length; i++ )
            {
              out.println( members[i].getName().trim() );
              if ( i < ( members.length - 1 ) )
              {
                out.println( "<br/>" );
              }
            }
          %></td>
          <%--fmt:message key="group.membership"/--%>
      </tr>
      <tr>
        <td colspan="2">
        <fmt:message key="group.modifier">
           <fmt:param><%=modifier%></fmt:param>
           <fmt:param><%=modified%></fmt:param>
        </fmt:message>
        </td>
      </tr>
      <tr>
        <td colspan="2">
        <fmt:message key="group.creator">
           <fmt:param><%=creator%></fmt:param>
           <fmt:param><%=created%></fmt:param>
        </fmt:message>
        </td>
      </tr>
    </table>
<%
  }
%>

  <wiki:Permission permission="deleteGroup"> 
  <form action="<wiki:Link format='url' jsp='DeleteGroup.jsp'/>"
         class="wikiform"
            id="deleteGroup"
        onsubmit="return( confirm('<fmt:message key="group.areyousure"><fmt:param>${param.group}</fmt:param></fmt:message>') && Wiki.submitOnce(this) );"
        method="POST" accept-charset="UTF-8">
      <input type="submit" name="ok" value="<fmt:message key="actions.deletegroup"/>" />
      <input type="hidden" name="group" value="${param.group}" />
  </form>
  </wiki:Permission>

</wiki:Tab>

<wiki:Permission permission="editGroup">
  <wiki:Tab id="editgroup" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "actions.editgroup")%>'
           url='<%=c.getURL(WikiContext.NONE, "EditGroup.jsp", "group="+request.getParameter("group") ) %>'
           accesskey="e" >
  </wiki:Tab>
</wiki:Permission>

</wiki:TabbedSection>
