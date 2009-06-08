<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>

<%-- Inserts a string message. --%>

   <div class="error">
     <c:out value="${message}"/>
   </div>

   <br clear="all" />
