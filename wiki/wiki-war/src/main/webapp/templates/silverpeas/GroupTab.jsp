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
<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="java.security.Principal" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.WikiContext" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.PrincipalComparator" %>
<%@ page import="com.ecyrd.jspwiki.auth.authorize.Group" %>
<%@ page import="com.ecyrd.jspwiki.auth.authorize.GroupManager" %>
<%@ page import="org.apache.log4j.*" %>
<%@ page errorPage="/Error.jsp" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%!
  String printWikiGroupPutGroup( Group group, String name, boolean cursor, PageContext pageContext)
  {
    Principal[] m = group.members();
    java.util.Arrays.sort( m, new PrincipalComparator() );

    String delim = "\", \"";
      
    StringBuffer ss = new StringBuffer();
    MessageFormat mf = null;
    Object[] args = null;
      
      ss.append( "WikiGroup.putGroup( \"" );
      
      ss.append( name );
      ss.append( delim );
      
      for( int j=0; j < m.length; j++ ) { ss.append( m[j].getName().trim()+"\\n" ); }
      
      ss.append( delim );
      mf = new MessageFormat(LocaleSupport.getLocalizedMessage(pageContext, "grp.createdon") );
      args = new Object[]{(group.getCreated()==null) ? "" : group.getCreated().toString(), group.getCreator()};
      ss.append( mf.format( args ) );
      
      mf = new MessageFormat(LocaleSupport.getLocalizedMessage(pageContext, "grp.lastmodified") );
      args = new Object[]{(group.getCreated()==null) ? "" : group.getCreated().toString(), group.getModifier()};
      ss.append( mf.format( args ) );
      
      ss.append( "\", " );
      ss.append( ( cursor ) ? "true" : "false" );
      
      ss.append( ");\n" );


    return ss.toString();
  }
%>

<wiki:Messages div="error" topic="<%=GroupManager.MESSAGES_KEY%>" prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"group.errorprefix")%>'/>

<table id='wikigroups' class='wikitable' >
<tr>
  <th><fmt:message key="group.name" /></th>
  <th><fmt:message key="group.members" /></th>
</tr>
<tr>
  <td id="groupnames" rowspan="2">
    <div id="grouptemplate" 
            style="display:none; " 
            title='<fmt:message key="grp.groupnames.title"/>'
          onclick="WikiGroup.toggle(); WikiGroup.onMouseOverGroup(this);"
      onmouseover="WikiGroup.onMouseOverGroup(this);" ></div>

    <wiki:Permission permission="createGroups">
    <div id="groupfield" 
      onmouseover="WikiGroup.onMouseOverGroup(this);" >
      <input type="text" size="30" 
               id="newgroup"
            value='<fmt:message key="grp.newgroupname"/>'
           onblur="if( this.value == '' ) { this.value = this.defaultValue; }; " 
          onfocus="if( this.value == this.defaultValue ) { this.value = ''; WikiGroup.onClickNew(); }; "/>
    </div>
    </wiki:Permission>
  </td>
  <td id="groupmembers">
    <div style="float:left;">
    <textarea rows="8" cols="30" disabled="disabled"
              name="membersfield" id="membersfield" ></textarea>
    </div>
    <form action="<wiki:Link format='url' jsp='Group.jsp'/>" 
              id="groupForm" 
          method="post" accept-charset="<wiki:ContentEncoding />" >
      <div>
      <input type="hidden" name="group"   value="" />
      <input type="hidden" name="members" value="" />
      <input type="hidden" name="action"  value="save" />
      <input type="button" disabled="disabled"
             name="saveButton" id="saveButton" 
            value='<fmt:message key="grp.savegroup"/>' 
          onclick="WikiGroup.onSubmit( this.form, '<wiki:Link format='url' jsp='EditGroup.jsp' />' );" /></div>

      <wiki:Permission permission="createGroups">
      <div>
      <input type="button" disabled="disabled"  
             name="createButton" id="createButton"
            value='<fmt:message key="grp.savenewgroup"/>' 
            style="display:none; "
          onclick="WikiGroup.onSubmitNew( this.form, '<wiki:Link format='url' jsp='NewGroup.jsp' />' );" /></div>
      </wiki:Permission>

      <div>
      <input type="button" disabled="disabled"
             name="cancelButton" id="cancelButton" 
            value='<fmt:message key="grp.cancel"/>' 
          onclick="WikiGroup.toggle();" /></div>

      <wiki:Permission permission="deleteGroup">
      <div>
      <c:set var="deleteConfirm"><fmt:message key="grp.deletegroup.confirm"/></c:set>
      <input type="button" disabled="disabled" 
             name="deleteButton" id="deleteButton"
            value='<fmt:message key="grp.deletegroup"/>' 
          onclick="confirm( '${deleteConfirm}' ) 
                && WikiGroup.onSubmit( this.form, '<wiki:Link format='url' jsp='DeleteGroup.jsp' />' );" /></div>
      </wiki:Permission>
    </form>
  </td>
  </tr>
  <tr valign="top">
  <td>
    <div class="formhelp"><fmt:message key="grp.formhelp"/></div>
    <p id="groupinfo" class="formhelp"></p>
  </td>
  </tr>
</table>

<h3><fmt:message key="grp.allgroups"/></h3>
<p><wiki:Translate>[{Groups}]</wiki:Translate></p>


<%
  String groupname = request.getParameter( "group" );
%>
 
<script type="text/javascript">
//<![CDATA[
<%
  WikiContext c = WikiContext.findContext( pageContext );
  Principal[] roles = c.getWikiSession().getRoles();

  for( int i = 0; i < roles.length; i++ )
  {
    if ( roles[i] instanceof GroupPrincipal ) /* bugfix */
    {
      String name = roles[i].getName();
      Group group = c.getEngine().getGroupManager().getGroup( name );

      %><%= printWikiGroupPutGroup( group, name, name.equals( groupname ), pageContext )  %><%
    }
  }
%>
//]]>
</script>

<%--
WikiGroup.putGroup( "Group1qsdf qsdf qsdf qsdf qsdffsdfq en nog een beetje langer he", "Member1\nMember2\nMember3\nMember4\nMember5\nMember6", "createdon", "createdby", "changedon", "changedby" );
--%>
