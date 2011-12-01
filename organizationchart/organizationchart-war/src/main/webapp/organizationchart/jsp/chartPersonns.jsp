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
		<c:forEach items="${organigramme.rootOrganization.mainActors}" var="mainActor" varStatus="loopInfo">
			{role : "${mainActor.role}", userFullName: "${mainActor.fullName}", login : "${mainActor.login}"} ${(not loopInfo.last) ? ',' : ''}
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
	cellType : CELL_TYPE_ORGANIZATION,
	commonUserURL : "Details?login=",
	usersIcon : "${usersIcon}"
});

<%-- CATEGORIES --%>
<c:forEach items="${organigramme.categories}" var="category">
	currentCategoryIndex = cellIndex;
	jCells[cellIndex] = new JCell( {
		id : cellIndex,
		title: "${(category.name eq 'Personnel') ? '' : category.name}",
		level : 1,
		className : 5,
		cellType : CELL_TYPE_CATEGORY,
		commonUserURL : "Details?login=",
		innerUsers : new Array(
			<c:forEach items="${category.users}" var="user" varStatus="mainLoopInfo">
					{login : "${user.login}", userFullName: "${user.fullName}",
					userAttributes: new Array(
					<c:forEach items="${user.details}" var="detail" varStatus="loopInfo">
						{label : "${detail.key}", value: "${detail.value}"} ${(not loopInfo.last) ? ',' : ''}
					</c:forEach>
					)
					} ${(not mainLoopInfo.last) ? ',' : ''}
			</c:forEach>
			)
	});
	jLinks[linkIndex++] = new JLink(0,cellIndex++, 0, ORIENTATION_HORIZONTAL);
</c:forEach>