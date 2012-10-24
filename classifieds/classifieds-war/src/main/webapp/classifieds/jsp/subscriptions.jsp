<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />

<c:set var="subscribes" value="${requestScope.Subscribes}" />

<html>
<head>
<view:looknfeel />
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/animation.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>
</head>
<body>
	<fmt:message var="classifiedPath" key="classifieds.mySubscriptions" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="#" />
	</view:browseBar>

	<view:operationPane>
		<fmt:message var="iconAddSubscription" key="classifieds.subscriptionsAdd" bundle="${icons}" />
		<c:url var="iconAddSubscriptionUrl" value="${iconAddSubscription}"/>
		<fmt:message var="addSubscriptionLabel"	key="classifieds.addSubscription" />
		<view:operation icon="${iconAddSubscriptionUrl}" action="javaScript:addSubscription()" 	altText="${addSubscriptionLabel}" />
	</view:operationPane>

	<view:window>
		<view:frame>
			<br />
			<view:board>

				<jsp:include page="subscriptionManager.jsp" />

				<table>
					<c:if test="${not empty subscribes}">
						<c:forEach items="${subscribes}" var="subscribe">
							<tr>
								<td>
									<p>
										&nbsp; &#149; &nbsp;&nbsp;<b>${subscribe.fieldName1} - ${subscribe.fieldName2}</b>
										<a href="DeleteSubscription?SubscribeId=${subscribe.subscribeId}">
											<fmt:message var="iconDelete" key="classifieds.smallDelete" bundle="${icons}" />
											<c:url var="iconDeleteUrl" value="${iconDelete}"/>
											<fmt:message var="deleteLabel" key="GML.delete" />
											<img src="${iconDeleteUrl}" border="0" alt="${deleteLabel}" title="${deleteLabel}" align="absmiddle" />
										</a>
									</p>
								</td>
							</tr>
						</c:forEach>
					</c:if>
					<c:if test="${empty subscribes}">
						<tr>
							<td colspan="5" valign="middle" align="center" width="100%">
								<br /> <fmt:message key="classifieds.SubscribeEmpty" /> <br /></td>
						</tr>
					</c:if>
				</table>
			</view:board>
		</view:frame>
	</view:window>
</body>
</html>