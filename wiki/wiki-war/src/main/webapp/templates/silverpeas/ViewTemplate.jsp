<%@ page isELIgnored ="false" %> 
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="templates.default"/>
<fmt:setBundle basename="com.silverpeas.wiki.settings.wikiIcons" var="icons" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html id="top" xmlns="http://www.w3.org/1999/xhtml">

<head>
  <view:looknfeel />
  <title>
    <fmt:message key="view.title.view">
      <fmt:param><wiki:Variable var="ApplicationName" /></fmt:param>
      <fmt:param><wiki:PageName /></fmt:param>
    </fmt:message>
  </title>
  <wiki:Include page="commonheader.jsp"/>
  <wiki:CheckVersion mode="notlatest">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckVersion>
  <wiki:CheckRequestContext context="diff|info">
    <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckRequestContext>
  <wiki:CheckRequestContext context="!view">
    <meta name="robots" content="noindex,follow" />
  </wiki:CheckRequestContext>
</head>

<body class="view">

<form action="<wiki:Link format='url' context='<%=WikiContext.DELETE%>' />"
	class="wikiform"
	id="deleteForm"
	method="post" accept-charset="<wiki:ContentEncoding />"
	onsubmit="return( confirm('<fmt:message key="info.confirmdelete"/>') && Wiki.submitOnce(this) );">
	<input type="hidden" name="delete-all" value="1"/>
</form>

<c:set var="pageName"><wiki:PageName /></c:set>
<view:browseBar link="#" path="${pageName}" />
<%@ include file="PageActionsTop.jsp"%>
<view:window>
<div id="wikibody" class="${prefs['orientation']}">

  <wiki:Include page="Header.jsp" />

  <div id="content">
    <div id="page">
      <wiki:Content/>
      <wiki:Include page="PageActionsBottom.jsp"/>
    </div>
    <wiki:Include page="Favorites.jsp"/>
	<div class="clearbox"></div>
  </div>

  <wiki:Include page="Footer.jsp" />

</div>
</view:window>

</body>
</html>