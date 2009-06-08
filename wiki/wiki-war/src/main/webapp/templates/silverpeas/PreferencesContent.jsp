<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="com.ecyrd.jspwiki.ui.*" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<%
	WikiContext context = WikiContext.findContext( pageContext ); 
  TemplateManager.addResourceRequest( context, "script", "scripts/jspwiki-prefs.js" );
%>
<view:frame>
  <wiki:Include page="PreferencesTab.jsp" />
</view:frame>