<%@ page isELIgnored="false"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<html>
  <head>
    <view:looknfeel />
    <script type="text/javascript">
    var organizationchartPath = '<%=request.getContextPath()%>/organizationchart/';
    </script>"
    <script type="text/javascript" src="<c:url value="/organizationchart/js/vertdtree.js" />" ></script>
    <link type="text/css" href="<c:url value="/organizationchart/css/dtree.css" />" rel="StyleSheet"/>
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
         	<tr>
         	<td align="left"><A href="javascript:;" onClick="mytree.closeAll();">tout réduire</A></td>
         	<td align="right"><A href="javascript:;" onClick="mytree.resizeon(-2);">retour à la racine</A></td>
         	</tr>
         	<tr><td>
	    	<script type="text/javascript">
	    		mytree = new dTree('mytree');
	    		<c:forEach var="child" items="${organigramme}">
	    	  		mytree.add(<c:out value="${child.id}"/>, 
	    	  			<c:out value="${child.parentId}"/>,
		      			'<c:out value="${child.name}"/><c:if test="${child.fonction != ''}"><br></c:if>' +
		      			'<c:out value="${child.fonction}"/>' +
		      			'<c:if test="${child.tel != ''}"><br></c:if><c:out value="${child.tel}"/>', 
		      			<c:choose>
		      				<c:when test="${child.detailed}">'id<c:out value="${child.id}"/>'</c:when>
		      				<c:otherwise>''</c:otherwise>
		      			</c:choose>
						,'<c:out value="${child.description}"/>', 
		      			'popup', '', '', true, 
		      			<c:choose>
	      					<c:when test="${child.firstLevel}">true</c:when>
	      					<c:otherwise>false</c:otherwise>
			      		</c:choose>
		      			, '<c:out value="${child.color}"/>');
				</c:forEach>
				
				document.write(mytree); 
				mytree.closeAll();
			</script> 
		  </td></tr></table>
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>