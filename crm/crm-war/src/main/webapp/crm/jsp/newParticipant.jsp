<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<html>
<head>
	<title><fmt:message key="GML.popupTitle"/></title>
	<view:looknfeel/>
	<script type="text/javascript" src="${context}/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="${context}/util/javaScript/checkForm.js"></script>
	<script type="text/javascript" src="${context}/util/javaScript/animation.js"></script>
	<script type="text/javascript" src="${context}/util/javaScript/checkForm.js"></script>
	<script type="text/javascript">
		function openSPWindow(url, windowName) {
			var form = document.forms["newParticipant"];
			url += "?participantId=" + form.elements["participantId"].value
				+ "&participantFunction=" + form.elements["participantFunction"].value
				+ "&FilterLib=" + form.elements["FilterLib"].value
				+ "&FilterId=" + form.elements["FilterId"].value
				+ "&participantEmail=" + form.elements["participantEmail"].value
				+ "&participantActif=" + form.elements["participantActif"].value;
			SP_openWindow(url, windowName, "750", "550", "scrollbars=yes, menubar=yes, resizable, alwaysRaised");
		}
		
		function submitForm() {
			var form = document.forms["newParticipant"];
			if (form.elements["FilterLib"].value == "") {
				alert("<fmt:message key="crm.fieldNameRequired"/>");
			} else if (form.elements["participantFunction"].value == "") {
				alert("<fmt:message key="crm.fieldFunctionRequired"/>");
			} else {
				form.action = "ChangeParticipant";
				form.submit();
			}
		}
		
		function cancelForm() {
			document.forms["newParticipant"].action = "ViewProject";
			document.forms["newParticipant"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<c:if test="${not empty participantId}">
			<view:tabs>
				<fmt:message key="crm.header" var="headerLabel"/>
				<view:tab label="${headerLabel}" selected="true" action="${myComponentURL}NewParticipant?participantId=${participantId}"/>
				<fmt:message key="crm.attachment" var="attachmentLabel"/>
				<view:tab label="${attachmentLabel}" selected="" action="attachmentManager.jsp?elmtId=${participantId}&elmtType=PARTICIPANT&returnAction=NewParticipant&returnId=participantId"/>
			</view:tabs>
		</c:if>
		<view:frame>
			<view:board>
				<form name="newParticipant" action="" method="post">
					<input type="hidden" name="participantId" value="${participantId}">
					<input type="hidden" name="participantEmail" value="${participantEmail}">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.nom"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="FilterLib" size="25" value="${FilterLib}" disabled="disabled">&nbsp;
								<input type="hidden" name="FilterId" value="${FilterId}">
								<a href="javascript:openSPWindow('CallUserPanelParticipant', '')"><img src="${context}<fmt:message key="crm.userPanel" bundle="${icons}"/>"
									alt="<fmt:message key="crm.openUserPanelPeas"/>" border="0" title="<fmt:message key="crm.openUserPanelPeas"/>"></a>&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.fonction"/>&nbsp;:&nbsp;</span></td>
							<td>
								<select name="participantFunction">
									<c:forEach items="${Functions}" var="func">
										<option value="${func[0]}"<c:if test="${func[0] eq participantFunction}"> selected</c:if>>${func[1]}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<c:if test="${not empty participantEmail}">
							<tr>
								<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.email"/>&nbsp;:&nbsp;</span></td>
								<td nowrap>${participantEmail}&nbsp;</td>
							</tr>
						</c:if>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actif"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="checkbox" name="participantActif" value="1"<c:if test="${participantActif eq '1'}"> checked</c:if>>&nbsp;</td>
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
</body>
</html>