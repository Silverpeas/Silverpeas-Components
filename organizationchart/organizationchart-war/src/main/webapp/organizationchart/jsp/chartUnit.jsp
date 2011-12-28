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

<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="organizationchart.icons.users" var="usersIcon" bundle="${icons}" />

<%
// ------------------------------------------------------------------------------
// ORGANIGRAMME DE TYPE UNIT
// ------------------------------------------------------------------------------
%>

var jCells = new Array('jCells');
var jLinks = new Array('jLinks');
var cellIndex=0;
var linkIndex=0;
var levelOffset = 0;


<%-- ROOT ORGANIZATION --%>
jCells[cellIndex] = new JCell( {
	id : cellIndex++,
	title: "${organigramme.rootOrganization.name}",
	roles : new Array(
		<c:forEach items="${organigramme.rootOrganization.mainActors}" var="mainActor" varStatus="mainLoopInfo">
			{role : "${mainActor.role}", userFullName: "${mainActor.fullName}", login : "${mainActor.login}",
				userAttributes: new Array(
					<c:forEach items="${mainActor.details}" var="detail" varStatus="loopInfo">
						{label : "${detail.key}", value: "${detail.value}"} ${(not loopInfo.last) ? ',' : ''}
					</c:forEach>
				)
			} 
			${(not mainLoopInfo.last) ? ',' : ''}
		</c:forEach>
	),
	userAttributes : new Array(
		<c:forEach items="${organigramme.rootOrganization.details}" var="detail" varStatus="loopInfo">
			{label : "${detail.key}", value: "${detail.value}"} ${(not loopInfo.last) ? ',' : ''}
		</c:forEach>
	),
	parentURL : "${organigramme.rootOrganization.parentUrl}",
	level : 0,
	className : 0,
	extraClassName : "${organigramme.rootOrganization.specificCSSClass}",
	cellType : CELL_TYPE_ORGANIZATION,
	showCenterLink : false,
	showDetailsLink : ${organigramme.rootOrganization.detailLinkActive},
	detailsURL : "${organigramme.rootOrganization.url}",
	commonUserURL : "Details?login=",
	usersIcon : "${usersIcon}"
});

<%-- RIGHT ROLE --%>
<c:if test="${not empty organigramme.rightRole}">
jCells[cellIndex] = new JCell( {
	id: cellIndex,
	title: "${organigramme.rightRole.name}",
	innerUsers : new Array(
		<c:forEach items="${organigramme.rightRole.users}" var="user" varStatus="mainLoopInfo">
			{userFullName: "${user.fullName}", login : "${user.login}",
				userAttributes: new Array(
					<c:forEach items="${user.details}" var="detail" varStatus="loopInfo">
						{label : "${detail.key}", value: "${detail.value}"} ${(not loopInfo.last) ? ',' : ''}
					</c:forEach>
				)} ${(not mainLoopInfo.last) ? ',' : ''}
		</c:forEach>
	),
	level : 1,
	className : 3,
	extraClassName : "${organigramme.rootOrganization.specificCSSClass}",
	cellType : CELL_TYPE_CATEGORY,
	showCenterLink : false,
	showDetailsLink : false,
	commonUserURL : "Details?login="
});
jLinks[linkIndex++] = new JLink(0,cellIndex++, 0, ORIENTATION_RIGHT);
levelOffset = 1;
</c:if>

<%-- LEFT ROLE --%>
<c:if test="${not empty organigramme.leftRole}">
jCells[cellIndex] = new JCell( {
	id: cellIndex,
	title: "${organigramme.leftRole.name}",
	innerUsers : new Array(
		<c:forEach items="${organigramme.leftRole.users}" var="user" varStatus="mainLoopInfo">
			{userFullName: "${user.fullName}", login : "${user.login}",
				userAttributes: new Array(
					<c:forEach items="${user.details}" var="detail" varStatus="loopInfo">
						{label : "${detail.key}", value: "${detail.value}"} ${(not loopInfo.last) ? ',' : ''}
					</c:forEach>
				)} ${(not mainLoopInfo.last) ? ',' : ''}
		</c:forEach>
	),
	level : 1,
	className : 4,
	extraClassName : "${organigramme.rootOrganization.specificCSSClass}",
	cellType : CELL_TYPE_CATEGORY,
	showCenterLink : false,
	showDetailsLink : false,
	commonUserURL : "Details?login="
});
jLinks[linkIndex++] = new JLink(0,cellIndex++, 0, ORIENTATION_LEFT);
levelOffset = 1;
</c:if>

<%-- SUB UNITS --%>
<c:forEach items="${organigramme.subOrganizations}" var="organization">
jCells[cellIndex] = new JCell( {
	id : cellIndex,
	title: "${organization.name}",
	roles : new Array(
		<c:forEach items="${organization.mainActors}" var="mainActor" varStatus="loopInfo">
			{role : "${mainActor.role}", userFullName: "${mainActor.fullName}", login : "${mainActor.login}"} ${(not loopInfo.last) ? ',' : ''}
		</c:forEach>
	),
	userAttributes : new Array(
		<c:forEach items="${organization.details}" var="detail" varStatus="loopInfo">
			{label : "${detail.key}", value: "${detail.value}"} ${(not loopInfo.last) ? ',' : ''}
		</c:forEach>
	),
	level : 1+levelOffset,
	className : 1,
	extraClassName : "${organization.specificCSSClass}",
	cellType : CELL_TYPE_ORGANIZATION,
	showCenterLink : ${organization.centerLinkActive},
	showDetailsLink : ${organization.detailLinkActive},
	commonUserURL : "Details?login=",
	detailsURL : "${organization.url}",
	onClickURL : "${organization.url}",
	usersIcon : "${usersIcon}"
});
jLinks[linkIndex++] = new JLink(0,cellIndex++, 0, ORIENTATION_HORIZONTAL);
</c:forEach>

//alert("links :"+jLinks);
