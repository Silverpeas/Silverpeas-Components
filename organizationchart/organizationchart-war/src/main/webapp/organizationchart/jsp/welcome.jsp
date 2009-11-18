<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<html>
  <head>
    <view:looknfeel />
  </head>
  <c:set var="componentId" value="${requestScope.componentId}" />
  <c:set var="sessionController">Silverpeas_OrganizationChart_<c:out value="${componentId}" /></c:set>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle bundle="${requestScope.resources.multilangBundle}" />
  <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
  <c:set var="browseContext" value="${requestScope.browseContext}" />
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
    <view:window>
      <view:frame>
        Bienvenue sur le composant organizationchart.
      </view:frame>
      <view:frame>
        <view:board>
          Cette instance s'appele <b><c:out value="${browseContext[1]}" /></b>.<br/>
          Elle se situe dans l'espace <b><c:out value="${browseContext[0]}" /></b>.
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>