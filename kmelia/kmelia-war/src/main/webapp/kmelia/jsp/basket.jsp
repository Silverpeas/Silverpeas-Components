<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkKmelia.jsp" %>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<fmt:message var="emptyBasket" key="EmptyBasket"/>
<fmt:message var="basketLabel" key="kmelia.basket"/>
<c:set var="translation" value="${requestScope.Language}"/>
<c:set var="componentId" value="${request.browseContext[3]}"/>

<!DOCTYPE>
<html lang="${translation}">
<head>
	<title></title>
<view:looknfeel/>
<script type="text/javascript">
function displayPublications(id)
{
	//display publications of topic
	const ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Id:id,ComponentId:'<%=componentId%>',IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function doPagination(index, nbItemsPerPage)
{
	const ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:index,NbItemsPerPage:nbItemsPerPage,ComponentId:'<%=componentId%>',IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
}

function emptyTrash()
{
	const label = "<%=kmeliaScc.getString("ConfirmFlushTrashBean")%>";
	jQuery.popup.confirm(label, function() {
		$.progressMessage();
		$.get('<%=m_context%>/KmeliaAJAXServlet', {ComponentId:'<%=componentId%>',Action:'EmptyTrash'},
				function(data){
					$.closeProgressMessage();
					if (data === "ok")
					{
						displayPublications("1");
					}
					else
					{
						notyError(data);
					}
				}, 'text');
	});
}

$(document).ready(function() {
	displayPublications("1");
});
</script>
</head>
<body id="kmelia" onUnload="closeWindows()" class="yui-skin-sam">
<div id="<%=componentId %>">
<%
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setI18N("GoToCurrentTopic", (String) request.getAttribute("Language"));
	browseBar.setExtraInformation(resources.getString("kmelia.basket"));

	//Display operations
	OperationPane operationPane = window.getOperationPane();
	if (kmeliaScc.isSuppressionAllowed(kmeliaScc.getProfile())) {
		operationPane.addOperation("useless", resources.getString("EmptyBasket"), "javascript:onClick=emptyTrash()");
	}
    out.println(window.printBefore());
%>
	<view:frame>
		<div id="pubList">
			<br/>
			<view:board>
				<div class="center"><fmt:message key="kmelia.inProgressPublications"/></div>
				<br/><br/>
				<img alt=progression" src="${requestScope.resources.getIcon('kmelia.progress')}"/>
			</view:board>
			<br/>
		</div>

	</view:frame>
	<%
		out.println(window.printAfter());
	%>

<form name="pubForm" action="ViewPublication" method="GET">
	<input type="hidden" name="PubId"/>
	<input type="hidden" name="CheckPath" value="1"/>
</form>
</div>
<view:progressMessage/>
</body>
</html>