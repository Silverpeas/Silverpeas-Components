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
<%@page import="org.silverpeas.components.classifieds.control.SearchContext"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@page import="org.silverpeas.core.contribution.content.form.PagesContext"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/classifieds" prefix="classifiedsTags" %>

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
<c:set var="currentFirstItemIndex" value="${requestScope.CurrentFirstItemIndex}" />
<c:set var="nbPerPage" value="${requestScope.NbPerPage}" />
<c:set var="nbResults" value="${requestScope.NbResults}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="${language}">
<head>
<view:looknfeel />
	<title></title>
<script type="text/javascript">
function sendData() {
	document.searchForm.submit();
}

function viewClassifieds(fieldNumber, fieldValue) {
	var id = $("#searchForm select").get(fieldNumber).id;
	$("#searchForm #"+id+" option[value='"+fieldValue+"']").prop('selected',true);
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
								<%
								String language = (String) pageContext.getAttribute("language");
								String instanceId = (String) pageContext.getAttribute("instanceId");
								SearchContext searchContext = (SearchContext) pageContext.getAttribute("searchContext");
								Form formSearch = searchContext.getForm();

								PagesContext context = new PagesContext("myForm", "0", language, false, instanceId, null, null);
								context.setIgnoreDefaultValues(true);
								context.setBorderPrinted(false);
								formSearch.display(out, context, searchContext.getData());
								%>
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

      <fmt:message key="classifieds.noResult" var="emptyListMessage"/>
      <classifiedsTags:listOfClassifieds classifieds="${classifieds}" language="${language}" emptyListMessage="${emptyListMessage}"/>

      <view:pagination currentPage="${currentFirstItemIndex}" totalNumberOfItems="${nbResults}" nbItemsPerPage="${nbPerPage}" action="Pagination?ItemIndex=" />
			
		</view:frame>
	</view:window>
</body>
</html>