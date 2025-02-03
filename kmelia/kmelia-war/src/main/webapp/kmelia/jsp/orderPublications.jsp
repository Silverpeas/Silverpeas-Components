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

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="org.silverpeas.components.kmelia.model.KmeliaPublication" %>
<%@ page import="org.owasp.encoder.Encode" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="publications" value="${requestScope.Publications}"/>
<c:set var="currentLang" value="${requestScope.Language}"/>
<c:set var="infoIcon" value="${requestScope.resources.getIcon('kmelia.info')}"/>
<c:set var="pubIcon" value="${requestScope.resources.getIcon('kmelia.publication')}"/>
<c:set var="buttonLabel" value="${requestScope.resources.getString('kmelia.SortItemsSave')}"/>

<view:sp-page>
<view:sp-head-part>
<style>
li {
	list-style: none;
}
</style>
<script type="text/javascript">
	$(document).ready(function() {
		$("#publis").sortable({opacity: 0.4}); 
	});

function sendData() {
	const reg = new RegExp("publi", "g");

	let data = $('#publis').sortable('serialize');
	data += "#";
	const tableau = data.split(reg);
	let param = "";
	for (let i=0; i<tableau.length; i++) {
		if (i !== 0)
			param += ","
				
		param += tableau[i].substring(3, tableau[i].length-1);
	}
	document.sortForm.sortedIds.value = param;
	document.sortForm.submit();
}

function topicGoTo(id) {
	location.href="GoToTopic?Id="+id;
}

</script>
</view:sp-head-part>
<view:sp-body-part>
<fmt:message key="kmelia.OrderPublications" var="browseBarXtra"/>
<view:browseBar extraInformations="${browseBarXtra}" path="${requestScope['Path']}"/>
<view:window>
<view:frame>
<div class="inlineMessage">
<img src="${infoIcon}" style="vertical-align: middle"
	 alt="${requestScope.resources.getString("kmelia.OrderPublicationsHelp")}"/>
	${requestScope.resources.getString("kmelia.OrderPublicationsHelp")}<br/><br/>
<view:buttonPane>
	<view:button label="${buttonLabel}" action="javascript:sendData()"/>
</view:buttonPane>
</div>
<br/>
<view:board>
<table>
	<th></th>
<tr class="intfdcolor">
	<td style="width: 20px">
		<img src="${pubIcon}" alt="publication"/>
	</td>
	<td style="text-align: left" class="ArrayNavigation">
		<c:choose>
			<c:when test="${fn:length(publications) > 1}">
				${fn:length(publications)} ${requestScope.resources.getString("GML.publications")}
			</c:when>
			<c:otherwise>
				${fn:length(publications)} ${requestScope.resources.getString("GML.publication")}
			</c:otherwise>
		</c:choose>
	</td>
</tr>
</table>
<br/>
<table>
	<th></th>
	<tr>
		<td>
<ul id="publis" style="cursor: hand; cursor: pointer;">
	<c:forEach var="kmeliaPub" items="${publications}">
		<c:set var="pub" value="${kmeliaPub.detail}"/>
		<jsp:useBean id="pub"
					 type="org.silverpeas.core.contribution.publication.model.PublicationDetail"/>
	<li id="publi_${pub.id}">&#8226;&#160;
		<b>${silfn:escapeHtml(pub.getName(currentLang))}</b><br/>
		<c:if test="${silfn:isDefined(pub.getDescription(currentLang))}">
			${silfn:escapeHtmlWhitespaces(pub.getDescription(currentLang))}
		</c:if>
	</li>
	<br/>

	</c:forEach>
</ul>
</td></tr>
</table>
</view:board>
</view:frame>
</view:window>
<form name="sortForm" method="post" action="OrderPublications">
<input type="hidden" name="sortedIds"/>
</form>
</view:sp-body-part>
</view:sp-page>