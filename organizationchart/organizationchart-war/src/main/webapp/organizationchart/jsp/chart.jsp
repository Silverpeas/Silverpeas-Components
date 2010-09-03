<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<html>
  <head>
	<link type="text/css" href="<c:url value="/organizationchart/css/dtree.css" />" rel="StyleSheet"/>
    <view:looknfeel />
    <script type="text/javascript">
    var organizationchartPath = '<%=request.getContextPath()%>/organizationchart/';
    </script>
    <script type="text/javascript" src="<c:url value="/organizationchart/js/vertdtree.js" />" ></script>
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
		      			, '<c:out value="${child.style}"/>');
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