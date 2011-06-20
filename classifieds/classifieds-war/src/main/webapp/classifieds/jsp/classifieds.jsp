<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator"
	prefix="view"%>

<%@page import="com.silverpeas.util.StringUtil"%>

<c:set var="sessionController" value="Silverpeas_classifieds_${requestScope.InstanceId}" />

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />
<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="title" value="${requestScope.TitlePath}" />
<c:set var="extra" value="${requestScope.Extra}" />
<c:set var="profile" value="${requestScope.Profile}" />


<html>
<head>
<view:looknfeel />
<script type="text/javascript"
	src="${pageContext.request.contextPath}/util/javaScript/animation.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>

<script language="javascript">
	var classifiedWindow = window;

	function openSPWindow(fonction, windowName) {
		pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600',
				'400', 'scrollbars=yes, resizable, alwaysRaised');
	}
</script>
</head>

<body>

	<fmt:message var="classifiedPath"
		key="${ (empty title) ? 'classifieds.myClassifieds' : title}" />
	<view:browseBar extraInformations="${extra}">
		<view:browseBarElt label="${classifiedPath}" link="" />
	</view:browseBar>

	<c:if
		test="${(profile.name == 'manager') || (profile.name == 'publisher') }">
		<view:operationPane>
			<fmt:message var="addOp" key="classifieds.addClassified" />
			<fmt:message var="addIcon" key="classifieds.addClassified"
				bundle="${icons}" />
			<view:operation action="NewClassified" altText="${addOp}"
				icon="${addIcon}" />
		</view:operationPane>
	</c:if>

	<view:window>
		<view:frame>
			<view:board>
				<table>
					<c:if test="${not empty classifieds}">
						<c:forEach items="${classifieds}" var="classified">
							<tr>
								<td>
									<p>
										&nbsp; &#149; &nbsp;&nbsp;<b><a
											href="ViewClassified?ClassifiedId=${classified.classifiedId}">${classified.title}</a>
										</b>
										<c:choose>
											<c:when test="${classified.status == 'Draft'}">
												<fmt:message key="classifieds.draft" />
											</c:when>

											<c:when test="${classified.status == 'ToValidate'}">
												<fmt:message key="classifieds.toValidate" />
											</c:when>
											<c:when test="${classified.status == 'Unvalidate'}">
												<fmt:message key="classifieds.refuse" />
											</c:when>
											<c:when test="${classified.status == 'Unpublished'}">
												<fmt:message key="classifieds.unpublished" />
											</c:when>
										</c:choose>

										<br /> &nbsp;&nbsp;&nbsp;
										<c:out value="${classified.validateDate}" />
									</p></td>
							</tr>
						</c:forEach>
					</c:if>

					<c:if test="${empty classifieds}">
						<tr>
							<td colspan="5" valign="middle" align="center" width="100%">
								<br /> <fmt:message key="classifieds.CategoryEmpty" /> <br /></td>
						</tr>
					</c:if>
				</table>
			</view:board>
		</view:frame>
	</view:window>
</body>
</html>