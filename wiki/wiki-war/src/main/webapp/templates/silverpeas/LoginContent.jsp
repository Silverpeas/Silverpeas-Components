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
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.auth.*" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<view:setBundle basename="templates.default"/>
<%
    String postURL = "";
    WikiContext ctx = WikiContext.findContext( pageContext );
    AuthenticationManager mgr = ctx.getEngine().getAuthenticationManager();

    if( mgr.isContainerAuthenticated() )
    {
        postURL = "j_security_check";
    }
    else
    {
        String redir = (String)ctx.getVariable("redirect");
        if( redir == null ) redir = ctx.getEngine().getFrontPage();
        postURL = ctx.getURL( WikiContext.LOGIN, redir );
    }

    boolean supportsCookieAuthentication = mgr.allowsCookieAuthentication();
%>
<wiki:TabbedSection defaultTab="${param.tab}">

<%-- Login functionality --%>
<wiki:UserCheck status="notauthenticated">
<wiki:Tab id="logincontent" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "login.tab")%>'>
<%--<wiki:Include page='LoginTab.jsp'/>--%>

<form action="<%=postURL%>"
          id="login"
       class="wikiform"
    onsubmit="return Wiki.submitOnce(this);"
      method="post" accept-charset="<wiki:ContentEncoding />" >

<div class="center">

  <h3><fmt:message key="login.heading.login"><fmt:param><wiki:Variable var="applicationname" /></fmt:param></fmt:message></h3>

  <div class="formhelp"><fmt:message key="login.help"></fmt:message></div>

  <table>
    <tr>
      <td colspan="2" class="formhelp">
        <wiki:Messages div="error" topic="login"
                    prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"login.errorprefix")%>' />
      </td>
    </tr>
    <tr>
      <td><label for="j_username"><fmt:message key="login.login"/></label></td>
      <td><input type="text" size="24" value="<wiki:Variable var='uid' default='' />"
                 name="j_username" id="j_username" /></td>
    </tr>
    <tr>
      <td><label for="j_password"><fmt:message key="login.password"/></label></td>
      <td><input type="password" size="24"
                 name="j_password" id="j_password" /></td>
    </tr>
    <% if( supportsCookieAuthentication ) { %>
    <tr>
      <td><label for="j_remember"><fmt:message key="login.remember"/></label></td>
      <td><input type="checkbox"
                 name="j_remember" id="j_remember" /></td>
    </tr>
    <% } %>
    <tr>
      <td>&nbsp;</td>
      <td>
        <input type="hidden" name="redirect" value="<wiki:Variable var='redirect' default='' />" />
        <input type="submit" name="submitlogin" value="<fmt:message key='login.submit.login'/>" />
      </td>
    </tr>
    </table>

    <div class="formhelp">
      <fmt:message key="login.lostpw"/>
      <a href="#" onclick="$('menu-lostpassword').fireEvent('click');"
                    title="<fmt:message key='login.lostpw.title'/>" >
        <fmt:message key="login.lostpw.getnew"/>
      </a>
    </div>
    <div class="formhelp">
      <fmt:message key="login.nopassword"/>
      <a href="#" onclick="$('menu-profile').fireEvent('click');"
                    title="<fmt:message key='login.registernow.title'/>" >
        <fmt:message key="login.registernow">
          <fmt:param><wiki:Variable var="applicationname" /></fmt:param>
        </fmt:message>
      </a>
    </div>

</div>
</form>

</wiki:Tab>

<%-- Lost pasword functionality --%>
<wiki:Tab id="lostpassword" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "login.lostpw.tab")%>'>

<div class="center">
<form action="<wiki:Link jsp='LostPassword.jsp' format='url'><wiki:Param name='tab' value='lostpassword'/></wiki:Link>"
          id="lostpw"
       class="wikiform"
    onsubmit="return Wiki.submitOnce(this);"
      method="post" accept-charset="<wiki:ContentEncoding />" >

  <h3><fmt:message key="login.lostpw.heading" /></h3>

  <c:choose>
  <c:when test="${passwordreset == 'done' }">
      <wiki:Messages div="information" topic="resetpw" prefix="" />
      <p>
        <fmt:message key="login.lostpw.reset.login">
          <fmt:param><a href="<wiki:Link jsp='Login.jsp' />"><fmt:message key="login.lostpw.reset.clickhere"/></a></fmt:param>
        </fmt:message>
      </p>
  </c:when>
  <c:otherwise>

  <div class="formhelp"><fmt:message key="login.lostpw.help"></fmt:message></div>

  <table>
    <c:if test="${param.tab eq 'lostpassword'}" >
    <tr>
      <td colspan="2" class="formhelp">
        <wiki:Messages div="error" topic="resetpw"
                    prefix='<%=LocaleSupport.getLocalizedMessage(pageContext,"login.errorprefix")%>' />
      </td>
    </tr>
    </c:if>
    <tr>
      <td><label for="name"><fmt:message key="login.lostpw.name"/></label></td>
      <td><input type="text" size="24" name="name" id="name" /></td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>
        <input type="hidden" name="action" value="resetPassword"/>
        <input type="submit" name="Submit" value="<fmt:message key='login.lostpw.submit'/>" />
      </td>
    </tr>
  </table>

  <div class="formhelp">
    <fmt:message key="login.invite"/>
    <a href="#" onclick="$('menu-logincontent').fireEvent('click');"
                  title="<fmt:message key='login.title'/>" >
      <fmt:message key="login.heading.login"><fmt:param><wiki:Variable var="applicationname" /></fmt:param></fmt:message>
    </a>
  </div>
  <div class="formhelp">
    <fmt:message key="login.nopassword"/>
    <%--<a href="UserPreferences.jsp?tab=profile">--%>
    <a href="#" onclick="$('menu-profile').fireEvent('click');"
                  title="<fmt:message key='login.registernow.title'/>" >
      <fmt:message key="login.registernow">
        <fmt:param><wiki:Variable var="applicationname" /></fmt:param>
      </fmt:message>
    </a>
  </div>

  </c:otherwise>
  </c:choose>

</form>
</div>

</wiki:Tab>
</wiki:UserCheck>

<%-- Register new user profile --%>
<wiki:Permission permission='editProfile'>
<wiki:Tab id="profile" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "login.register.tab")%>' >
  <wiki:Include page='ProfileTab.jsp'/>
</wiki:Tab>
</wiki:Permission>

<wiki:Tab id="loginhelp" title='<%=LocaleSupport.getLocalizedMessage(pageContext,"login.tab.help")%>' >
  <wiki:InsertPage page="LoginHelp" />

  <wiki:NoSuchPage page="LoginHelp">
  <div class="error">
    <fmt:message key="login.loginhelpmissing">
       <fmt:param><wiki:EditLink page="LoginPageHelp">LoginHelp</wiki:EditLink></fmt:param>
    </fmt:message>
  </div>
  </wiki:NoSuchPage>

</wiki:Tab>

</wiki:TabbedSection>