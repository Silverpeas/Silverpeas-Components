<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ page import="javax.servlet.jsp.jstl.fmt.*" %>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<wiki:TabbedSection >

<wiki:Tab id="conflict" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "conflict.oops.title")%>'>
  <div class="error"><fmt:message key="conflict.oops" /></div>
  <fmt:message key="conflict.goedit" >
    <fmt:param><wiki:EditLink><wiki:PageName /></wiki:EditLink></fmt:param>
  </fmt:message>
</wiki:Tab>
 
<wiki:Tab id="conflictOther" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "conflict.modified")%>' >
  <tt><%=pageContext.getAttribute("conflicttext",PageContext.REQUEST_SCOPE)%></tt>      
</wiki:Tab>
 
<wiki:Tab id="conflictOwn" title='<%=LocaleSupport.getLocalizedMessage(pageContext, "conflict.yourtext")%>' >
  <tt><%=pageContext.getAttribute("usertext",PageContext.REQUEST_SCOPE)%></tt>
</wiki:Tab>

</wiki:TabbedSection>