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

<view:setBundle basename="org.silverpeas.crm.multilang.crmBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<html>
<head>
	<title><fmt:message key="GML.popupTitle"/></title>
	<view:looknfeel/>
	<script type="text/javascript">
		function submitForm() {
			if (document.forms["newContact"].elements["contactName"].value == "") {
				alert("<fmt:message key="crm.fieldNameRequired"/>");
			} else if (document.forms["newContact"].elements["contactFunction"].value == "") {
				alert("<fmt:message key="crm.fieldFunctionRequired"/>");
			} else {
				document.forms["newContact"].action = "ChangeContact";
				document.forms["newContact"].submit();
			}
		}
		
		function cancelForm() {
		    document.forms["newContact"].action = "ViewClient";
		    document.forms["newContact"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<c:if test="${not empty contactId}">
			<view:tabs>
				<fmt:message key="crm.header" var="headerLabel"/>
				<view:tab label="${headerLabel}" selected="true" action="${myComponentURL}NewContact?contactId=${contactId}"/>
				<fmt:message key="crm.attachment" var="attachmentLabel"/>
				<view:tab label="${attachmentLabel}" selected="" action="attachmentManager.jsp?elmtId=${contactId}&elmtType=CONTACT&returnAction=NewContact&returnId=contactId"/>
			</view:tabs>
		</c:if>
		<view:frame>
			<view:board>
				<form name="newContact" action="" method="post">
					<input type="hidden" name="contactId" value="${contactId}">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.nom"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="contactName" size="40" value="${contactName}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.fonction"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="contactFunction" size="40" value="${contactFunction}">&nbsp;
								<img src="${context}<fmt:message key="crm.mandatory" bundle="${icons}"/>" width="5" height="5">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.tel"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="contactTel" size="25" value="${contactTel}">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.email"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="contactEmail" size="40" value="${contactEmail}">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.adresse"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="contactAddress" size="40" value="${contactAddress}">&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actif"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="checkbox" name="contactActif" value="1"<c:if test="${contactActif eq '1'}"> checked</c:if>>&nbsp;</td>
						</tr>
						<tr align=center>
							<td class="intfdcolor4" align="left" colspan="2"><span class="txt">(<img
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
		document.forms["newContact"].elements["contactName"].focus();
	</script>
</body>
</html>
