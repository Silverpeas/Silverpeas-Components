<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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

<%@ page import="org.silverpeas.components.organizationchart.model.OrganizationalChart"%>
<%@ page import="org.silverpeas.components.organizationchart.model.OrganizationalUnit"%>
<%@ include file="check.jsp"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="organizationChart.userDetails" var="userDetailsTitle"/>

<c:set var="displayLabels" value="${requestScope['DisplayLabels']}"/>
<c:set var="breadcrumb" value="${requestScope['Breadcrumb']}"/>

<html>
  <head>
	<view:link href="/organizationchart/css/organizationchart.css"/>
    <view:looknfeel />
    <style type="text/css">
    <c:if test="${not displayLabels}">
    	span.role,
    	span.attribute {
    		display: none;
    	}
    </c:if>
    </style>
    
    <script type="text/javascript">
    var organizationchartPath = '<%=request.getContextPath()%>/organizationchart/';
    </script>
    <view:script src="/organizationchart/js/organizationchart.js"/>
    <script type="text/javascript">
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
        
        whenSilverpeasReady().then(function() {
          chartinit();
          $("a").click(function() {
            if ($(this).attr("target") != "_blank" && !$(this).attr("href").startsWith("javascript:")) {
              $.progressMessage();
            }
          });
        });
        
        function activateUserZoom() {
          $('.userToZoom').each(function() {
            var $this = $(this);
            if ($this.data('userZoom') == null)
              $this.userZoom({
                id: $this.attr('rel')
              });
          });
        }
    </script>
  </head>
  <body>
  <div id="userDetailDialog"></div>

	<fmt:message key="organizationchart.icons.print" var="printIcon" bundle="${icons}" />
	<fmt:message key="organizationchart.message.print" var="printMessageAltText" />
	<c:url var="printIconUrl" value="${printIcon}" />

	<view:operationPane>
		<view:operation altText="${printMessageAltText}" icon="${printIconUrl}" action="javascript:window.print();" />
	</view:operationPane>
	
	<view:browseBar>
		<c:forEach items="${breadcrumb}" var="element">
			<view:browseBarElt label="${element.name}" link="${element.url}"/>
		</c:forEach>
	</view:browseBar>

    <view:window>
          <c:out value="${error}"/>
          <div align="center" style="overflow: visible;">
             <div id="chart"></div>
             <div id="chartInvisible"></div>
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
    </view:window>
    
    <view:progressMessage/>
  </body>
</html>