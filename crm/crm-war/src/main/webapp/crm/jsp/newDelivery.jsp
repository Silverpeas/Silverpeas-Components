<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<html>
<head>
	<title><fmt:message key="GML.popupTitle"/></title>
	<view:looknfeel/>
    <view:includePlugin name="datepicker"/>
	<script type="text/javascript" src="${context}/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="${context}/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function openSPWindow(url, windowName) {
			var form = document.forms["newDelivery"];
			url += "?deliveryId=" + form.elements["deliveryId"].value
				+ "&deliveryDate=" + form.elements["deliveryDate"].value
				+ "&deliveryElement=" + form.elements["deliveryElement"].value
				+ "&deliveryVersion=" + form.elements["deliveryVersion"].value
				+ "&FilterLib=" + form.elements["FilterLib"].value
				+ "&FilterId=" + form.elements["FilterId"].value
				+ "&deliveryContact=" + form.elements["deliveryContact"].value
				+ "&deliveryMedia=" + form.elements["deliveryMedia"].value;
			SP_openWindow(url, windowName, "750", "550", "scrollbars=yes, menubar=yes, resizable, alwaysRaised");
		}

		function isCorrectForm() {
			var errorMsg = "";
			var errorNb = 0;
			var result = false;

			var form = document.forms["newDelivery"];
			var deliveryElement = stripInitialWhitespace(form.elements["deliveryElement"].value);
			var deliveryVersion = stripInitialWhitespace(form.elements["deliveryVersion"].value);
			var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
			var beginDate = form.elements["deliveryDate"].value;
			
			var yearBegin = extractYear(beginDate, '${resources.language}');
			var monthBegin = extractMonth(beginDate, '${resources.language}');
			var dayBegin = extractDay(beginDate, '${resources.language}');
			
			var beginDateOK = true;
		
			if (form.elements["deliveryContact"].value == "") {
				errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryContactName"/>' <fmt:message key="crm.MustContainText"/>\n";
				errorNb++;
			}
		
			if (form.elements["FilterLib"].value == "") {
				errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryIntervenant"/>' <fmt:message key="crm.MustContainText"/>\n";
				errorNb++;
			}
		
			if (isWhitespace(deliveryElement)) {
				errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryElement"/>' <fmt:message key="crm.MustContainText"/>\n";
				errorNb++;
		    }
		    if (isWhitespace(deliveryVersion)) {
				errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryVersion"/>' <fmt:message key="crm.MustContainText"/>\n";
				errorNb++;
		    }
		
		    if (isWhitespace(beginDate)) {
				errorMsg +="  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryDate"/>' <fmt:message key="crm.MustContainText"/>\n";
				errorNb++;
		    } else {
		        if (beginDate.replace(re, "OK") != "OK") {
		            errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryDate"/>' <fmt:message key="crm.MustContainCorrectDate"/>\n";
		            errorNb++;
		            beginDateOK = false;
		        } else {
		            if (!isCorrectDate(yearBegin, monthBegin, dayBegin)) {
						errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.deliveryDate"/>' <fmt:message key="crm.MustContainCorrectDate"/>\n";
						errorNb++;
						beginDateOK = true;
		            }
		        }
		    }
		
		    switch(errorNb) {
				case 0 :
				    result = true;
				    break;
				case 1 :
				    errorMsg = "<fmt:message key="crm.ThisFormContains"/> 1 <fmt:message key="crm.Error"/> : \n" + errorMsg;
				    window.alert(errorMsg);
				    result = false;
				    break;
				default :
				    errorMsg = "<fmt:message key="crm.ThisFormContains"/> " + errorNb + " <fmt:message key="crm.Errors"/> :\n" + errorMsg;
				    window.alert(errorMsg);
				    result = false;
				    break;
		    }
		    return result;
		}
		
		
		function submitForm() {
			if (isCorrectForm()) {
				document.forms["newDelivery"].action = "ChangeDelivery";
				document.forms["newDelivery"].submit();
			}
		}
		
		function cancelForm() {
		    document.forms["newDelivery"].action = "ViewDelivrable";
		    document.forms["newDelivery"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<c:if test="${not empty deliveryId}">
			<view:tabs>
				<fmt:message key="crm.header" var="headerLabel"/>
				<view:tab label="${headerLabel}" selected="true" action="${myComponentURL}NewDelivery?deliveryId=${deliveryId}"/>
				<fmt:message key="crm.attachment" var="attachmentLabel"/>
				<view:tab label="${attachmentLabel}" selected="" action="attachmentManager.jsp?elmtId=${deliveryId}&elmtType=DELIVERY&returnAction=NewDelivery&returnId=deliveryId"/>
			</view:tabs>
		</c:if>
		<view:frame>
			<view:board>
				<form name="newDelivery" action="" method="post">
					<input type="hidden" name="deliveryId" value="${deliveryId}">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryDate"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="deliveryDate" class="dateToPick" size="14" maxlength="10" value="${deliveryDate}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;
								<span class="txtnote">(<fmt:message key="crm.dateFormat"/>)</span></td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryElement"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="deliveryElement" size="25" value="${deliveryElement}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryVersion"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="deliveryVersion" size="25" value="${deliveryVersion}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryIntervenant"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="FilterLib" size="25" value="${FilterLib}" disabled="disabled">&nbsp;
								<input type="hidden" name="FilterId" value="${FilterId}">
								<a href="javascript:openSPWindow('CallUserPanelDelivery', '')"><img src="${context}<fmt:message key="crm.userPanel" bundle="${icons}"/>"
									alt="<fmt:message key="crm.openUserPanelPeas"/>" border="0" title="<fmt:message key="crm.openUserPanelPeas"/>"></a>&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryContactName"/>&nbsp;:&nbsp;</span></td>
							<td>
								<select name="deliveryContact">
									<c:forEach items="${Contacts}" var="contact">
										<option value="${contact[0]}"<c:if test="${contact[0] eq deliveryContact}"> selected</c:if>>${contact[1]}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryMedia"/>&nbsp;:&nbsp;</span></td>
							<td>
								<select name="deliveryMedia">
									<c:forEach items="${Medias}" var="media">
										<option value="${media[0]}"<c:if test="${media[0] eq deliveryMedia}"> selected</c:if>>${media[1]}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr>
							<td class="intfdcolor4" colspan="2"><span class="txt">(<img
								src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5"> :
								<fmt:message key="GML.requiredField"/>)</span></td>
						</tr>
					</table>
				</form>
			</view:board>
			<center>
				<view:buttonPane>
					<fmt:message key="GML.validate" var="validateLabel"/>
					<view:button label="${validateLabel}" action="javascript:submitForm();"/>
					<fmt:message key="GML.cancel" var="cancelLabel"/>
					<view:button label="${cancelLabel}" action="javascript:cancelForm();"/>
				</view:buttonPane>
			</center>
		</view:frame>
	</view:window>
	<script type="text/javascript">
		document.forms["newDelivery"].elements["deliveryDate"].focus();
	</script>
</body>
</html>