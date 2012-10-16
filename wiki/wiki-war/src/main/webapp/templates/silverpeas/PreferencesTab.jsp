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
<%@ page isELIgnored ="false" %> 
<%@ page errorPage="/Error.jsp" %>
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.preferences.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%
  /* see commonheader.jsp */
  String prefDateFormat = Preferences.getPreference(pageContext,"DateFormat");
  String prefTimeZone   = Preferences.getPreference(pageContext,"TimeZone");

  WikiContext c = WikiContext.findContext( pageContext );
  pageContext.setAttribute( "skins", c.getEngine().getTemplateManager().listSkins(pageContext, c.getTemplate() ) );
%>

<h3><fmt:message key="prefs.heading"><fmt:param><wiki:Variable var="applicationname"/></fmt:param></fmt:message></h3>

<c:if test="${param.tab eq 'prefs'}" >
  <div class="formhelp">
    <wiki:Messages div="error" topic="prefs" prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"prefs.errorprefix.prefs")%>'/>
  </div>
</c:if>

<form action="<wiki:Link jsp='UserPreferences.jsp' format='url'><wiki:Param name='tab' value='prefs'/></wiki:Link>" 
       class="wikiform" 
          id="setCookie"
      method="post" accept-charset="<wiki:ContentEncoding />"
    onsubmit="Wiki.savePrefs(); return Wiki.submitOnce(this);" >
<table>

  <tr>
  <td><label for="assertedName"><fmt:message key="prefs.assertedname"/></label></td>
  <td> 
  <input readonly="readonly" type="text" id="assertedName" name="assertedName" size="20" value="<wiki:UserProfile property='wikiname' />" />
  </td>
  </tr>
  <wiki:UserCheck status="anonymous">
  <tr>
  <td>&nbsp;</td>
  <td>
  <div class="formhelp">
    <fmt:message key="prefs.assertedname.description">
      <fmt:param><wiki:Variable var="applicationname" /></fmt:param>
      <fmt:param>
        <a href="<wiki:Link jsp='Login.jsp' format='url'><wiki:Param name='tab' value='register'/></wiki:Link>">
          <fmt:message key="prefs.assertedname.create"/>
        </a>
      </fmt:param>
    </fmt:message>
  </div>
  </td>
  </tr>
  </wiki:UserCheck>

  <tr>
  <td><label for="editor"><fmt:message key="edit.chooseeditor"/></label></td>
  <td>
    <select id="editor" name="editor">
      <wiki:EditorIterator id="edt">
        <option <%=edt.isSelected()%> value="<%=edt.getName()%>"><%=edt.getName()%></option>
      </wiki:EditorIterator>
  </select>
  </td>
  </tr>
  
 <tr>
  <td>&nbsp;</td>
  <td>
    <input type="submit" name="ok" value="<fmt:message key='prefs.save.prefs.submit'/>" 
      accesskey="s" />
    <input type="hidden" name="redirect" value="<wiki:Variable var='redirect' default='' />" />
    <input type="hidden" name="action" value="setAssertedName" />
    <div class="formhelp"><fmt:message key='prefs.cookies'/></div>
  </td>
  </tr>

</table>
</form>