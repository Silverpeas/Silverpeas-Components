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

<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator"
	prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
			response.setHeader("Pragma", "no-cache"); //HTTP 1.0
			response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<c:set var="sessionController"
	value="Silverpeas_classifieds_${requestScope.InstanceId}" />

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />

<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="formSearch" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />
<c:set var="nbTotal" value="${requestScope.NbTotal}" />
<c:set var="language" value="${sessionScope[sessionController].language}"/>

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

	function sendData() {
		document.classifiedForm.submit();
	}
</script>

</head>

<body>
	<fmt:message var="classifiedPath" key="classifieds.classifiedsResult" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="" />
	</view:browseBar>

	<view:window>
		<view:frame>
			<br />
			<FORM Name="classifiedForm" action="SearchClassifieds" Method="POST"
				ENCTYPE="multipart/form-data">
				<c:if test="${not empty formSearch}">
					<center>
						<div id="search">
							<!-- AFFICHAGE du formulaire -->
							<view:board>
								<%
								String language = (String) pageContext.getAttribute("language");
								String instanceId = (String) pageContext.getAttribute("instanceId");
								Form formSearch = (Form) pageContext.getAttribute("formSearch");
								DataRecord data = (DataRecord) pageContext.getAttribute("data");

								PagesContext context = new PagesContext("myForm", "0", language, false, instanceId, null, null);
							    context.setIgnoreDefaultValues(true);
							    context.setUseMandatory(false);
								formSearch.display(out, context, data);
								%>

								<view:buttonPane>
									<fmt:message var="searchLabel" key="classifieds.searchButton">
										<fmt:param value="${nbTotal}" />
									</fmt:message>
									<view:button label="${searchLabel}"
										action="javascript:onClick=sendData();" />
								</view:buttonPane>
							</view:board>
						</div>
					</center>
				</c:if>
			</FORM>
			<br />

			<view:board>
				<table>
					<c:if test="${not empty classifieds}">
						<c:forEach items="${classifieds}" var="classified">
							<tr>
								<td>
									<p>
										&nbsp; &#149; &nbsp;&nbsp;
										<b>
											<a href="ViewClassified?ClassifiedId=${classified.classifiedId}">
												${classified.title}
											</a>
										</b> <br />
										&nbsp;&nbsp;&nbsp;${classified.creatorName} - <fmt:formatDate value="${classified.creationDate}" />
									</p>
								</td>
							</tr>
						</c:forEach>
					</c:if>
					<c:if test="${empty classifieds}">
						<tr>
							<td colspan="5" valign="middle" align="center" width="100%">
								<br />
								<fmt:message key="classifieds.noResult" /> <br />
							</td>
						</tr>
					</c:if>
				</table>
			</view:board>
		</view:frame>
	</view:window>

</body>
</html>