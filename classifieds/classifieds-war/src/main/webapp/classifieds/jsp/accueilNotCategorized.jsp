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
<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="componentLabel" value="${browseContext[1]}" />
<c:set var="profile" value="${requestScope.Profile}" />
<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="nbTotal" value="${requestScope.NbTotal}" />
<c:set var="validation" value="${requestScope.Validation}" />
<c:set var="componentInstanceId" value="${requestScope.InstanceId}" />

<c:set var="formSearch" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />
<c:set var="isWysiwygHeaderEnabled" value="${requestScope.isWysiwygHeaderEnabled}"/>
<c:set var="wysiwygHeader" value="${requestScope.wysiwygHeader}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/animation.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>

<script type="text/javascript">
	var subscriptionWindow = window;

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
		<div id="${componentInstanceId}">

			<view:browseBar />

			<view:operationPane>
				<c:if test="${profile.name != 'anonymous'}">
					<c:if test="${(profile.name == 'admin') && (isWysiwygHeaderEnabled)}">
						<fmt:message var="updateWysiwygLabel" key="classifieds.updateWysiwygClassified" />
						<fmt:message var="updateWysiwygIcon" key="classifieds.updateWysiwygClassified" bundle="${icons}" />
						<view:operation action="ToWysiwygHeader" altText="${updateWysiwygLabel}" icon="${updateWysiwygIcon}" />
					</c:if>

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

				<div id="header_classifieds">
				${wysiwygHeader}
				</div>

				<view:frame>
					<view:areaOfOperationOfCreation/>
					<jsp:include page="subscriptionManager.jsp"/>
					<form name="classifiedForm" action="SearchClassifieds" method="post" enctype="multipart/form-data">
						<c:if test="${not empty formSearch}">
								<div id="search" >
									<!-- Search Form -->
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

          <ul id="classifieds_rich_list">
            <c:if test="${not empty classifieds}">
              <c:forEach items="${classifieds}" var="classified"
                varStatus="loopStatus">
                <li onclick="location.href='ViewClassified?ClassifiedId=${classified.classifiedId}'">
                  <div class="classified_thumb"><img src="images/cat.png" alt=""/></div>
                    <div class="classified_info">
                      <h4><a href="ViewClassified?ClassifiedId=${classified.classifiedId}">${classified.title}</a></h4>
                        <div class="classified_type"><a href="http://www.silverpeas.org">${classified.searchValue1}</a> <a href="http://www.silverpeas.org">${classified.searchValue2}</a></div>
                    </div>
                    
                      <c:if test="${classified.price > 0}">
                        <div class="classified_price">
                          ${classified.price} &euro;
                        </div>
                      </c:if>
                    <div class="classified_creationInfo">
                      <c:if test="${not empty classified.validateDate}">
                         <view:formatDateTime value="${classified.validateDate}" language="${language}"/>
                      </c:if>
                      <c:if test="${empty classified.validateDate}">
                        <c:if test="${not empty classified.updateDate}">
                           <view:formatDateTime value="${classified.updateDate}" language="${language}"/>
                        </c:if>
                        <c:if test="${empty classified.updateDate}">
                           <view:formatDateTime value="${classified.creationDate}" language="${language}"/>
                        </c:if>
                      </c:if>
                    </div>
                </li>
              </c:forEach>
            </c:if>
          </ul>

						<!-- legal notice -->
						<div id="infos" class="tableBoard">
							<fmt:message key="classifieds.infos" />
						</div>

              <div id="pagination">
                <div class="pageNav">
                  <div class="pageNavContent">
                    <div class="pageOn">1</div>
                    <div class="pageOff"> <a href="javascript:onClick=doPagination(20)" title="Aller à la page 2" class="ArrayNavigation">2</a></div>
                    <div class="pageOff"> <a href="javascript:onClick=doPagination(40)" title="Aller à la page 3" class="ArrayNavigation">3</a></div>
                    <div class="pageOff"> <a href="javascript:onClick=doPagination(60)" title="Aller à la page 4" class="ArrayNavigation">4</a></div>
                    <div class="pageOff"> <a href="javascript:onClick=doPagination(80)" title="Aller à la page 5" class="ArrayNavigation">5</a></div>
                    <div class="pageOff"> <a href="javascript:onClick=doPagination(100)" title="Aller à la page 6" class="ArrayNavigation">6</a></div>
                    <div class="pageOff"> <a href="javascript:onClick=doPagination(20)" title="Page suivante" class="ArrayNavigation"><img border="0" align="absmiddle" alt="Page suivante" src="/silverpeas/util/viewGenerator/icons/arrows/arrowRight.gif"></a></div>
                  </div>
                </div>
              </div>
              
				</view:frame>
			</view:window>
</body>
</html>