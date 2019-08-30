<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@page import="org.silverpeas.core.contribution.content.form.DataRecord"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/classifieds" prefix="classifiedsTags" %>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="profile" value="${requestScope.Profile}" />
<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="nbTotal" value="${requestScope.NbTotal}" />
<c:set var="validation" value="${requestScope.Validation}" />
<c:set var="componentInstanceId" value="${requestScope.InstanceId}" />

<c:set var="formSearch" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />
<c:set var="currentFirstItemIndex" value="${requestScope.CurrentFirstItemIndex}" />
<c:set var="nbPerPage" value="${requestScope.NbPerPage}" />
<c:set var="portletMode" value="${requestScope.PortletMode}" />
<c:set var="modeView" value=""/>
<c:if test="${portletMode}">
  <c:set var="modeView" value="classifieds-portletView"/>
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.classifieds" xml:lang="${language}">
<head>
<view:looknfeel />
	<title></title>
<view:includePlugin name="toggle"/>
<script type="text/javascript">
function sendData() {
	document.searchForm.submit();
}

function viewClassifieds(fieldNumber, fieldValue) {
	var id = $("#searchForm select").get(fieldNumber).id;
	$("#searchForm #"+id+" option[value='"+fieldValue+"']").prop('selected', true);
	sendData();
}
</script>
</head>
<body id="classifieds" class="${modeView}">
		<div id="${componentInstanceId}">

			<view:browseBar />

			<view:operationPane>
				<c:if test="${profile.name != 'anonymous'}">
					<c:if
						test="${(profile.name == 'admin') || (profile.name == 'publisher')}">
						<fmt:message var="addOp" key="classifieds.addClassified" />
						<fmt:message var="addIcon" key="classifieds.addClassified" bundle="${icons}" />
						<c:url var="addIcon" value="${addIcon}"/>
						<view:operationOfCreation action="NewClassified" altText="${addOp}" icon="${addIcon}" />
					</c:if>

					<fmt:message var="myOp" key="classifieds.myClassifieds" />
					<fmt:message var="myIcon" key="classifieds.myClassifieds" bundle="${icons}" />
					<view:operation action="ViewMyClassifieds" altText="${myOp}" icon="${myIcon}" />

					<view:operationSeparator />

					<fmt:message var="subAddOp" key="classifieds.addSubscription" />
					<fmt:message var="subAddIcon" key="classifieds.subscriptionsAdd" bundle="${icons}" />
					<view:operation action="javascript:addSubscription()" altText="${subAddOp}" icon="${subAddIcon}" />

					<fmt:message var="mySubOp" key="classifieds.mySubscriptions" />
					<fmt:message var="mySubIcon" key="classifieds.mySubscriptions" bundle="${icons}" />
					<view:operation action="ViewMySubscriptions" altText="${mySubOp}" icon="${mySubIcon}" />
				</c:if>

				<c:if test="${(profile.name == 'admin') && (validation)}">
					<view:operationSeparator />
					<fmt:message var="toValidateOp" key="classifieds.viewClassifiedToValidate" />
					<fmt:message var="toValidateIcon" key="classifieds.viewClassifiedToValidate" bundle="${icons}" />
					<view:operation action="ViewClassifiedToValidate" altText="${toValidateOp}" icon="${toValidateIcon}" />
				</c:if>
			</view:operationPane>

			<view:window>
				<view:frame>
          <view:componentInstanceIntro componentId="${instanceId}" language="${language}"/>
					<view:areaOfOperationOfCreation/>
					<jsp:include page="subscriptionManager.jsp"/>
					<form id="searchForm" name="searchForm" action="SearchClassifieds" method="post" target="MyMain" enctype="multipart/form-data">
						<c:if test="${not empty formSearch}">
              <div id="search" >
                <!-- Search Form -->
                  <%
                    String language = (String) pageContext.getAttribute("language");
                    String instanceId = (String) pageContext.getAttribute("instanceId");
                    Form formSearch = (Form) pageContext.getAttribute("formSearch");
                    DataRecord data = (DataRecord) pageContext.getAttribute("data");

                    PagesContext context = new PagesContext("myForm", "0", language, false, instanceId, null, null);
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
                      action="javascript:onclick=sendData();" />
                  </view:buttonPane>
                  </div>
              </div>
              <br/>
            </c:if>
					</form>

          <classifiedsTags:listOfClassifieds classifieds="${classifieds}" language="${language}"/>

          <view:pagination currentPage="${currentFirstItemIndex}" totalNumberOfItems="${nbTotal}" nbItemsPerPage="${nbPerPage}" action="Main?ItemIndex=" />
            
			<!-- legal notice -->
			<div id="infos" class="inlineMessage">
				<fmt:message key="classifieds.infos" />
			</div>

	</view:frame>
</view:window>
</div>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.classifieds', ['silverpeas.services', 'silverpeas.directives']);
</script>

</body>
</html>