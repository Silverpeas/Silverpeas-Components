<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>
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
