<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@page import="com.silverpeas.classifieds.control.SearchContext"%>
<%@page import="org.silverpeas.util.viewGenerator.html.pagination.Pagination"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="org.silverpeas.attachment.model.SimpleDocument"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="com.silverpeas.form.Form"%>
<%@page import="com.silverpeas.form.PagesContext"%>
<%@page import="com.silverpeas.form.DataRecord"%>

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

<c:set var="classifieds" value="${requestScope.Classifieds}" />
<c:set var="searchContext" value="${requestScope.SearchContext}" />
<c:set var="instanceId" value="${requestScope.InstanceId}" />
<c:set var="nbTotal" value="${requestScope.NbTotal}" />

<%
Pagination pagination = (Pagination) request.getAttribute("Pagination");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript">
function sendData() {
	document.searchForm.submit();
}

function viewClassifieds(fieldNumber, fieldValue) {
	var id = $("#searchForm select").get(fieldNumber).id;
	$("#searchForm #"+id+" option[value='"+fieldValue+"']").attr('selected','selected');
	sendData();
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
			<form id="searchForm" name="searchForm" action="SearchClassifieds" method="post" enctype="multipart/form-data">
					
						<div id="search">
							<!-- AFFICHAGE du formulaire -->
							<view:board>
								<div align="center">
								<%
								String language = (String) pageContext.getAttribute("language");
								String instanceId = (String) pageContext.getAttribute("instanceId");
								SearchContext searchContext = (SearchContext) pageContext.getAttribute("searchContext");
								Form formSearch = searchContext.getForm();

								PagesContext context = new PagesContext("myForm", "0", language, false, instanceId, null, null);
								context.setIgnoreDefaultValues(true);
								context.setUseMandatory(false);
								context.setBorderPrinted(false);
								formSearch.display(out, context, searchContext.getData());
								%>
								</div>
								<br/>
								<view:buttonPane>
									<fmt:message var="searchLabel" key="classifieds.searchButton">
										<fmt:param value="${nbTotal}" />
									</fmt:message>
									<view:button label="${searchLabel}" action="javascript:onclick=sendData();" />
								</view:buttonPane>
							</view:board>
						</div>
					
			</form>
			<br />

			<view:board>
				
					<c:if test="${not empty classifieds}">
						<ul id="classifieds_rich_list">
			              <c:forEach items="${classifieds}" var="classified"
			                varStatus="loopStatus">
			                <li onclick="location.href='ViewClassified?ClassifiedId=${classified.classifiedId}'">
			                  <c:if test="${not empty classified.images}">
					                <div class="classified_thumb">
					                <c:forEach var="image" items="${classified.images}" begin="0" end="0">
					                <%
					                SimpleDocument simpleDocument = (SimpleDocument) pageContext.getAttribute("image");
					                String url = URLManager.getApplicationURL() +  simpleDocument.getAttachmentURL();
					                %>
					                  <a href="#"><img src="<%=url%>"/></a>
					                </c:forEach>
					                </div>
			                  </c:if>
			                  
			                  <div class="classified_info">
			                   <h4><a href="ViewClassified?ClassifiedId=${classified.classifiedId}">${classified.title}</a></h4>
			                   <div class="classified_type">
			                    <a href="javascript:viewClassifieds(0, '${classified.searchValueId1}');">${classified.searchValue1}</a> 
			                    <a href="javascript:viewClassifieds(1, '${classified.searchValueId2}');">${classified.searchValue2}</a>
			                   </div>
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
			          </ul>
					</c:if>
					<c:if test="${empty classifieds}">
						<p class="message_noResult">
							<fmt:message key="classifieds.noResult" />
						</p>
					</c:if>
			
			</view:board>
			
			<% out.println(pagination.printIndex()); %>
			
		</view:frame>
	</view:window>
</body>
</html>