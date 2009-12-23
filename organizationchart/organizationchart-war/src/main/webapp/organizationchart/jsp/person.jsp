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
         <table align="center"><tr>
         	<c:set var="i" value="${0}"/>
         	<c:forEach var="child" items="${person}">
	    	  	<td align="left"><c:out value="${child}"/>&nbsp;&nbsp;</td>
	    	  	<c:if test="${i == 1}">
	    	  		</tr><tr>
	    	  		<c:set var="i" value="${-1}"/>
	    	  	</c:if>	
	    	  	<c:set var="i" value="${i + 1}" />
			</c:forEach>
			</tr>
		  </table>
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>