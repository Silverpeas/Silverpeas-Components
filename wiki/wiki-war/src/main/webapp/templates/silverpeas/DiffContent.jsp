<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<wiki:Include page="InfoContent.jsp"/>
