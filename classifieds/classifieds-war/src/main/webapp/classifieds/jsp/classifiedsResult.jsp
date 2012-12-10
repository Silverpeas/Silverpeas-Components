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

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />

<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="formSearch" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />
<c:set var="nbTotal" value="${requestScope.NbTotal}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/animation.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
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
<body id="classifieds">
	<fmt:message var="classifiedPath" key="classifieds.classifiedsResult" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="#" />
	</view:browseBar>

	<view:window>
		<view:frame>
			<br />
			<form name="classifiedForm" action="SearchClassifieds" method="post" enctype="multipart/form-data">
				<c:if test="${not empty formSearch}">
					
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
							  context.setBorderPrinted(false);
								formSearch.display(out, context, data);
								%>
								<br/>
								<div class="center">
								<view:buttonPane>
									<fmt:message var="searchLabel" key="classifieds.searchButton">
										<fmt:param value="${nbTotal}" />
									</fmt:message>
									<view:button label="${searchLabel}"
										action="javascript:onClick=sendData();" />
								</view:buttonPane>
								</div>
							</view:board>
						</div>
					
				</c:if>
			</form>
			<br />

			<view:board>
				
					<c:if test="${not empty classifieds}">
						<ul class="list_result_classifieds">
						<c:forEach items="${classifieds}" var="classified">
							<li>
								<a class="title_result_classifieds" href="ViewClassified?ClassifiedId=${classified.classifiedId}">${classified.title}</a>
								<c:if test="${classified.price > 0}">
                  ${classified.price} € - 
                 </c:if>
								<span class="creatorName_result_classifieds">${classified.creatorName}</span><span class="sep_creatorName_result_classifieds"> - </span>
								<c:if test="${not empty classified.updateDate}">
									<span class="date_result_classifieds updateDate"><view:formatDateTime value="${classified.updateDate}" language="${language}"/></span>
								</c:if>
								<c:if test="${empty classified.updateDate}">
									<span class="date_result_classifieds creationDate"><view:formatDateTime value="${classified.creationDate}" language="${language}"/></span>
								</c:if>
							</li>
						</c:forEach>
						</ul>
					</c:if>
					<c:if test="${empty classifieds}">
						<p class="message_noResult">
								<fmt:message key="classifieds.noResult" />
						</p>
					</c:if>
			
			</view:board>
		</view:frame>
	</view:window>

</body>
</html>