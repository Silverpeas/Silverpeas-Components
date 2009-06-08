<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<%
  WikiContext c = WikiContext.findContext(pageContext);
  String frontpage = c.getEngine().getFrontPage(); 
%>

<div id="header">
  <div class="titlebox"><wiki:InsertPage page="TitleBox"/></div>  
  <div class="breadcrumbs"><fmt:message key="header.yourtrail"/><wiki:Breadcrumbs /></div>
</div>