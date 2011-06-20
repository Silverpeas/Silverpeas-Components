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
<%@ taglib uri="http://www.silverpeas.com/tld/formTemplate"
	prefix="form"%>

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

<c:set var="classified" value="${requestScope.ClassifiedToRefuse}" />
<c:set var="classifiedId" value="${classified.classifiedId}" />

<HTML>
<HEAD>
<TITLE><fmt:message key="GML.popupTitle" />
</TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<view:looknfeel />
<script type="text/javascript"
	src="${pageContext.request.contextPath}/util/javaScript/checkForm.js"></script>
<script LANGUAGE="JavaScript" TYPE="text/javascript">
	function sendData() {
		if (isCorrectForm()) {
			document.refusalForm.submit();
			window.close();
		}
	}

	function isCorrectForm() {
		var errorMsg = "";
		var errorNb = 0;
		var motive = stripInitialWhitespace(document.refusalForm.Motive.value);
		if (isWhitespace(motive)) {
			errorMsg += "  - '<fmt:message key="classifieds.refusalMotive"/>' <fmt:message key="GML.MustBeFilled"/>\n";
			errorNb++;
		}
		switch (errorNb) {
		case 0:
			result = true;
			break;
		case 1:
			errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n"
					+ errorMsg;
			window.alert(errorMsg);
			result = false;
			break;
		default:
			errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb
					+ " <fmt:message key="GML.errors"/> :\n" + errorMsg;
			window.alert(errorMsg);
			result = false;
			break;
		}
		return result;
	}
</script>
</HEAD>

<BODY>

	<fmt:message var="classifiedPath" key="classifieds.refused" />
	<view:browseBar>
		<view:browseBarElt label="${classifiedPath}" link="" />
	</view:browseBar>

	<view:window>
		<view:frame>

			<c:set var="displayedId">
				<view:encodeHtml string="${classifiedId}" />
			</c:set>
			<c:set var="displayedTitle">
				<view:encodeHtml string="${classified.title}" />
			</c:set>

			<FORM NAME="refusalForm" Action="RefusedClassified" Method="POST">
				<TABLE>
					<tr>
						<td>
							<TABLE>
								<TR>
									<TD class="txtlibform"><fmt:message
											key="classifieds.number" /> :</TD>
									<td>${displayedId} <input type="hidden"
										name="ClassifiedId" value="${displayedId}"></TD>
								</TR>
								<TR>
									<TD class="txtlibform"><fmt:message key="GML.title" /> :</TD>
									<TD valign="top">${displayedTitle}</TD>
								<TR>
									<TD class="txtlibform" valign=top><fmt:message
											key="classifieds.refusalMotive" /> :</TD>
									<TD><textarea name="Motive" rows="5" cols="60"></textarea>&nbsp;<img
										border="0"
										src="<fmt:message key="classifieds.mandatory" bundle="${icons}"/>"
										width="5" height="5"></TD>
								</TR>
								<TR>
									<TD colspan="2">( <img border="0"
										src="<fmt:message key="classifieds.mandatory" bundle="${icons}"/>"
										width="5" height="5"> : <fmt:message
											key="GML.requiredField" /> )</TD>
								</TR>
							</TABLE></td>
					</tr>
				</TABLE>

			</FORM>

			<view:buttonPane>
				<fmt:message var="validateLabel" key="GML.validate" />
				<fmt:message var="cancelLabel" key="GML.cancel" />

				<view:button label="${validateLabel}"
					action="javascript:onClick=sendData();" />
				<view:button label="${cancelLabel}"
					action="javascript:onClick=window.close();" />
			</view:buttonPane>

		</view:frame>
	</view:window>

</BODY>
</HTML>