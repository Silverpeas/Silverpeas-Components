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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

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

<c:set var="classified" value="${requestScope.Classified}" />
<c:set var="userName" value="${requestScope.UserName}" />
<c:set var="userEmail" value="${requestScope.UserEmail}" />
<c:set var="userId" value="${requestScope.UserId}" />

<c:set var="formUpdate" value="${requestScope.Form}" />
<c:set var="data" value="${requestScope.Data}" />
<c:set var="fieldKey" value="${requestScope.FieldKey}" />
<c:set var="fieldName" value="${requestScope.FieldName}" />

<c:set var="action" value="${(not empty classified) ? 'UpdateClassified' : 'CreateClassified' }" />
<c:set var="creatorName" value="${(not empty classified) ? classified.creatorName : userName }" />
<c:set var="creatorEmail" value="${(not empty classified) ? classified.creatorEmail : userEmail }" />

<c:set var="language" value="${sessionScope[sessionController].language}" />

<c:if test="${not empty classified}">
	<c:set var="classifiedId" value="${classified.classifiedId}" />
	<c:set var="title" value="${classified.title}" />
	<c:set var="instanceId" value="${classified.instanceId}" />
	<c:set var="creatorId" value="${classified.creatorId}" />
	<c:set var="status" value="${classified.status}" />
	<c:set var="validatorId" value="${classified.validatorId}" />
	<c:set var="validatorName" value="${classified.validatorName}" />
	<c:set var="creationDate" value="${classified.creationDate}" />
	<c:set var="validateDate" value="${classified.validateDate}" />
	<c:set var="updateDate" value="${classified.updateDate}" />
</c:if>

<%
	String language = (String) pageContext.getAttribute("language");
	String instanceId = (String) pageContext.getAttribute("instanceId");
	Form formUpdate = (Form) pageContext.getAttribute("formUpdate");
	DataRecord data = (DataRecord) pageContext.getAttribute("data");

	PagesContext context = new PagesContext("classifiedForm", "11", language, false, instanceId, null, null);
	  context.setIgnoreDefaultValues(true);
%>

<html>
<head>
<view:looknfeel/>
<c:if test="${not empty formUpdate}">
	<%
  	formUpdate.displayScripts(out, context);
	%>
</c:if>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/animation.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>

<fmt:message var="GML_title" key="GML.title"/>
<fmt:message var="GML_MustBeFilled" key="GML.MustBeFilled"/>
<fmt:message var="GML_msgSize" key="GML.msgSize"/>
<fmt:message var="GML_ThisFormContains" key="GML.ThisFormContains"/>
<fmt:message var="GML_error" key="GML.error"/>
<fmt:message var="GML_errors" key="GML.errors"/>

<script type="text/javascript">

	// form validation
	function sendData()
	{
		<c:if test="${not empty formUpdate}">
			if (isCorrectForm() && isCorrectLocalForm()) {
		    	document.classifiedForm.submit();
		    }
		 </c:if>
		<c:if test="${empty formUpdate}">
				if (isCorrectLocalForm()) {
					document.classifiedForm.submit();
		    	}
		</c:if>
	}

	function isCorrectLocalForm()
	{
	   	var errorMsg = "";
	   	var errorNb = 0;
	   	var title = stripInitialWhitespace(document.classifiedForm.Title.value);

		if (title == "")
		{
			errorMsg+="  - '${GML_title}'  ${GML_MustBeFilled}\n";
		    errorNb++;
		}
	   	if (title.length > 255)
	   	{
			errorMsg+="  - '${GML_title}'  ${GML_msgSize}\n";
	       	errorNb++;
	   	}
	   	switch(errorNb)
	   	{
	       	case 0 :
	           	result = true;
	           	break;
	       	case 1 :
	           	errorMsg = "${GML_ThisFormContains} 1 ${GML_error} : \n" + errorMsg;
	           	window.alert(errorMsg);
	           	result = false;
	           	break;
	       	default :
	           	errorMsg = "${GML_ThisFormContains} " + errorNb + " ${GML_errors} :\n" + errorMsg;
	           	window.alert(errorMsg);
	           	result = false;
	           	break;
	   	}
	   	return result;
	}

	function setData()
	{
		<c:if test="${not empty fieldName}">
	      document.classifiedForm.${fieldName}.value = ${fieldKey};
	    </c:if>
	}

</script>

</head>
<body onload="setData()">
	<fmt:message var="classifiedPath"
		key="${ (action eq 'CreateClassified') ? 'classifieds.addClassified' : 'classifieds.updateClassified'}" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="" />
	</view:browseBar>

<view:window>
<view:frame>

<c:set var="displayedTitle"><view:encodeHtml string="${title}" /></c:set>
<c:set var="displayedId"><view:encodeHtml string="${classifiedId}" /></c:set>
<c:set var="displayedEmail"><view:encodeHtml string="${creatorEmail}" /></c:set>
<fmt:formatDate var="displayedCreationDate" value="${creationDate}" />
<fmt:formatDate var="displayedUpdateDate" value="${updateDate}" />
<fmt:formatDate var="displayedValidateDate" value="${validateDate}" />

<FORM Name="classifiedForm" action="${action}" Method="POST" ENCTYPE="multipart/form-data" onsubmit="sendData();return false;">
<table CELLPADDING="5" WIDTH="100%">
<tr>
	<td>
		<view:board>
		<table cellpadding="5">
			<c:if test="${action eq 'UpdateClassified'}">
				<tr>
					<td class="txtlibform"><fmt:message key="classifieds.number"/> :</td>
					<TD>${displayedId}</TD>
				</tr>
			</c:if>
			<tr>
				<td class="txtlibform"><fmt:message key="GML.title"/> :</td>
				<td><input type="text" name="Title" size="60" maxlength="150" value="${displayedTitle}">
					<img src="<fmt:message key="classifieds.mandatory"/>" width="5" height="5" border="0"/>
					<input type="hidden" name="ClassifiedId" value="${displayedId}">
				</td>
			</tr>
			<tr>
				<td class="txtlibform"><fmt:message key="classifieds.creationDate"/> :</td>
				<TD>${displayedCreationDate} <span class="txtlibform"><fmt:message key="classifieds.by"/></span> ${creatorName} ( ${displayedEmail} )</TD>
			</tr>
			<c:if test="${not empty updateDate}">
				<tr>
					<td class="txtlibform"><fmt:message key="classifieds.updateDate"/> :</td>
					<td>${displayedUpdateDate}</td>
				</tr>
			</c:if>
			<c:if test="${(not empty validateDate) && (not empty validatorName)}">
				<tr>
					<td class="txtlibform"><fmt:message key="classifieds.validateDate"/> :</td>
					<TD>${displayedValidateDate} <span class="txtlibform"><fmt:message key="classifieds.by"/></span> ${validatorName}</TD>
				</tr>
			</c:if>
			<tr><td colspan="2">( <img border="0" src=<fmt:message key="classifieds.mandatory" bundle="${icons}" /> width="5" height="5"> : <fmt:message key="classifieds.mandatory"/> )</td></tr>
		</table>
		</view:board>
		<br/>
			<c:if test="${not empty formUpdate}">
	  				<view:board>
					<!-- AFFICHAGE du formulaire -->
					<table>
					<tr>
						<td>
							<%
							formUpdate.display(out, context, data);
							%>
						</td>
					</tr>
					</table>
					</view:board>
			</c:if>
	</td>
</tr>
</table>
</form>
<view:buttonPane>
	<fmt:message var="validateLabel" key="GML.validate"/>
	<fmt:message var="cancelLabel" key="GML.cancel"/>

	<view:button label="${validateLabel}" action="javascript:onClick=sendData();" />
	<view:button label="${cancelLabel}" action="Main" />
</view:buttonPane>
</view:frame>
</view:window>
</body>
</html>