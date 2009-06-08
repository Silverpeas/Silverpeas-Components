<%@ page isELIgnored ="false" %> 
<%@ page errorPage="/Error.jsp" %>
<%@ page import="java.util.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ page import="com.ecyrd.jspwiki.preferences.*" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
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