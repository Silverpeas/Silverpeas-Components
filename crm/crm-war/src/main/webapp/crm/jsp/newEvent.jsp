<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
	<script type="text/javascript" src="${context}/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function openSPWindow(url, windowName) {
			var form = document.forms["newEvent"];
			url += "?eventId=" + form.elements["eventId.value"]
				+ "&eventState=" + form.elements["eventState"].value
				+ "&FilterLib=" + form.elements["FilterLib"].value
				+ "&FilterId=" + form.elements["FilterId"].value
				+ "&eventLib=" + form.elements["eventLib"].value
				+ "&eventDate=" + form.elements["eventDate"].value
				+ "&actionTodo=" + form.elements["actionTodo"].value
				+ "&actionDate=" + form.elements["actionDate"].value;
			SP_openWindow(url, windowName, "750", "550", "scrollbars=yes, menubar=yes, resizable, alwaysRaised");
		}
		
		function isCorrectForm() {
			var errorMsg = "";
			var errorNb = 0;
			var eventLib = stripInitialWhitespace(document.newEvent.eventLib.value);
			var actionTodo = stripInitialWhitespace(document.newEvent.actionTodo.value);
			var re = /(\d\d\/\d\d\/\d\d\d\d)/i;

			var form = document.forms["newEvent"];
			var beginDate = form.elements["eventDate"].value;
			var endDate = form.elements["actionDate"].value;
			var yearBegin = extractYear(beginDate, '${resources.language}');
			var monthBegin = extractMonth(beginDate, '${resources.language}');
			var dayBegin = extractDay(beginDate, '${resources.language}');
			var yearEnd = extractYear(endDate, '${resources.language}');
			var monthEnd = extractMonth(endDate, '${resources.language}');
			var dayEnd = extractDay(endDate, '${resources.language}');
			
			var beginDateOK = true;
		
			if (form.elements["FilterLib"].value == "") {
				errorMsg+="  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.personne"/>' <fmt:message key="crm.MustContainText"/>\n";
				errorNb++;
			}
		
		     if (isWhitespace(eventLib)) {
		           errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.eventLib"/>' <fmt:message key="crm.MustContainText"/>\n";
		           errorNb++;
		     }
		
		     if (isWhitespace(actionTodo)) {
		           errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.actionTodo"/>' <fmt:message key="crm.MustContainText"/>\n";
		           errorNb++;
		     }
		
		     if (isWhitespace(beginDate)) {
		           errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.eventDate"/>' <fmt:message key="crm.MustContainText"/>\n";
		           errorNb++;
		     } else {
		             if (beginDate.replace(re, "OK") != "OK") {
		                 errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.eventDate"/>' <fmt:message key="crm.MustContainCorrectDate"/>\n";
		                 errorNb++;
		                 beginDateOK = false;
		             } else {
		                 if (!isCorrectDate(yearBegin, monthBegin, dayBegin)) {
		                   errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.eventDate"/>' <fmt:message key="crm.MustContainCorrectDate"/>\n";
		                   errorNb++;
		                   beginDateOK = true;
		                 }
		             }
		     }
		     if (isWhitespace(endDate)) {
		           errorMsg += "  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.actionDate"/>' <fmt:message key="crm.MustContainText"/>\n";
		           errorNb++;
		     } else {
		           if (endDate.replace(re, "OK") != "OK") {
		                    errorMsg+="  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.actionDate"/>' <fmt:message key="crm.MustContainCorrectDate"/>\n";
		                    errorNb++;
		           } else {
		                 if (!isCorrectDate(yearEnd, monthEnd, dayEnd)) {
		                     errorMsg+="  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.actionDate"/>' <fmt:message key="crm.MustContainCorrectDate"/>\n";
		                     errorNb++;
		                 } else {
		                     if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
		                           if (beginDateOK && !isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)) {
		                                  errorMsg+="  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.actionDate"/>' <fmt:message key="crm.MustContainPostDateToBeginDate"/>\n";
		                                  errorNb++;
		                          }
		                     } else {
		                           if ((isWhitespace(beginDate)) && (!isWhitespace(endDate))) {
		                               if (!isFutureDate(yearEnd, monthEnd, dayEnd)) {
		                                      errorMsg+="  - <fmt:message key="crm.TheField"/> '<fmt:message key="crm.actionDate"/>' <fmt:message key="crm.MustContainPostDate"/>\n";
		                                      errorNb++;
		                               }
		                           }
		                     }
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
				document.forms["newEvent"].action = "ChangeEvent";
				document.forms["newEvent"].submit();
			}
		}
		
		function cancelForm() {
		    document.forms["newEvent"].action = "ViewJournal";
		    document.forms["newEvent"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<c:if test="${not empty eventId}">
			<view:tabs>
				<fmt:message key="crm.header" var="headerLabel"/>
				<view:tab label="${headerLabel}" selected="true" action="${myComponentURL}NewEvent?eventId=${eventId}"/>
				<fmt:message key="crm.attachment" var="attachmentLabel"/>
				<view:tab label="${attachmentLabel}" selected="" action="attachmentManager.jsp?elmtId=${eventId}&elmtType=EVENT&returnAction=NewEvent&returnId=eventId"/>
			</view:tabs>
		</c:if>
		<view:frame>
			<view:board>
				<form name="newEvent" action="" method="post">
					<input type="hidden" name="eventId" value="${eventId}">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.eventDate"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="eventDate" class="dateToPick" size="14" maxlength="10" value="${eventDate}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;
								<span class="txtnote">(<fmt:message key="crm.dateFormat"/>)</span></td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.eventLib"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="eventLib" size="40" value="${eventLib}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actionTodo"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="actionTodo" size="40" value="${actionTodo}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.personne"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="FilterLib" size="25" value="${FilterLib}" disabled="disabled">&nbsp;
								<input type="hidden" name="FilterId" value="${FilterId}">
								<a href="javascript:openSPWindow('CallUserPanelEvent', '')"><img src="${context}<fmt:message key="crm.userPanel" bundle="${icons}"/>"
									alt="<fmt:message key="crm.openUserPanelPeas"/>" border="0" title="<fmt:message key="crm.openUserPanelPeas"/>"></a>&nbsp;</td>
						</tr>
 						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actionDate"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="actionDate" class="dateToPick" size="14" maxlength="10" value="${actionDate}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;
								<span class="txtnote">(<fmt:message key="crm.dateFormat"/>)</span></td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.eventState"/>&nbsp;:&nbsp;</span></td>
							<td>
								<select name="eventState">
									<c:forEach items="${States}" var="state">
										<option value="${state[0]}"<c:if test="${state[0] eq eventState}"> selected</c:if>>${state[1]}</option>
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
		document.forms["newEvent"].elements["eventDate"].focus();
	</script>
</body>
</html>