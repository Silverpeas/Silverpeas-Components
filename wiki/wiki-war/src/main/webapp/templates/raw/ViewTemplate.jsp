<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<fmt:setBundle basename="com.silverpeas.wiki.settings.wikiIcons" var="icons" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<% response.setContentType("text/plain; charset=UTF-8"); %>
<html>
  <head>
    <view:looknfeel />
  </head>
  <body>
    <c:set var="pageName"><wiki:PageName /></c:set>
    <view:browseBar link="#" path="${pageName}" />
    <%@ include file="../silverpeas/PageActionsTop.jsp"%>
    <view:window>  
      <pre>
        <wiki:InsertPage mode="plain"/>
      </pre>
    </view:window>  
  </body>
</html>