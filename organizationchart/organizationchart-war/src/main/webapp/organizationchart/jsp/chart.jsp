<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ page isELIgnored="false"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.Iterator"%>

<%@ page import="com.silverpeas.components.organizationchart.model.OrganizationalChart"%>
<%@ page import="com.silverpeas.components.organizationchart.model.OrganizationalUnit"%>
<%@ include file="check.jsp"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="organizationChart.userDetails" var="userDetailsTitle"/>

<html>
  <head>
	<link type="text/css" href="<c:url value="/organizationchart/css/organizationchart.css" />" rel="StyleSheet"/>
    <view:looknfeel />
    <script type="text/javascript">
    var organizationchartPath = '<%=request.getContextPath()%>/organizationchart/';
    </script>
    <script type="text/javascript" src="<c:url value="/organizationchart/js/organizationchart.js" />" ></script>
    <script language="javascript">
	    function userDetail(url) {
	    	$("#userDetailDialog").dialog("option", "title", "${userDetailsTitle}");
	    	$("#userDetailDialog").dialog("option", "width", 850);
	    	$("#userDetailDialog").load(url).dialog("open");
	    }

        var dialogOpts = {
                modal: true,
                autoOpen: false,
                height: "auto"
        };

        $("#userDetailDialog").dialog(dialogOpts);    //end dialog

    </script>
  </head>
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" onload="chartinit();">
  <div id="userDetailDialog"/>

	<fmt:message key="organizationchart.icons.print" var="printIcon" bundle="${icons}" />
	<fmt:message key="organizationchart.message.print" var="printMessageAltText" />
	<c:url var="printIconUrl" value="${printIcon}" />

	<view:operationPane>
		<view:operation altText="${printMessageAltText}" icon="${printIconUrl}" action="javascript: window.print();" />
	</view:operationPane>

    <view:window>
      <view:frame>
        <view:board>
          <c:out value="${error}"/>
          <div align="center" style="overflow: visible;">
             <div id="chart" border="2px"></div>
             <div id="chartInvisible" border="2px"></div>
		     <script type="text/javascript">
		     <c:choose>
                <c:when test="${organigramme.chartType==0}">
                	<jsp:include page="chartUnit.jsp"/>
       			</c:when>
       			<c:when test="${organigramme.chartType==1}">
       				<jsp:include page="chartPersonns.jsp"/>
       			</c:when>
       	  	</c:choose>
   	      </script>
	      </div>
        </view:board>
      </view:frame>
    </view:window>
  </body>
</html>