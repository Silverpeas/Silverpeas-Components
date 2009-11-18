<%@ page isELIgnored="false"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<html>
  <head>
    <view:looknfeel />
  </head>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle bundle="${requestScope.resources.multilangBundle}" />
  <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
    <view:window>
      <view:frame>
        <view:board>
          <c:out value="${error}"/>
         <table align="center">
         	<c:forEach var="child" items="${person}">
	    	  		<tr><td><c:out value="${child}"/></td></tr>
			</c:forEach>
		  </table>
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>