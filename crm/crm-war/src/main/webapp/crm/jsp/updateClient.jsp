<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>

<html>
<head>
	<title><fmt:message key="GML.popupTitle"/></title>
	<view:looknfeel/>
	<script type="text/javascript">
		function submitForm() {
			if (document.forms["updateClient"].elements["clientName"].value == "") {
				alert("<fmt:message key="crm.fieldNameRequired"/>");
			} else {
				document.forms["updateClient"].action = "ChangeClient";
				document.forms["updateClient"].submit();
			}
		}
		
		function cancelForm() {
		    document.forms["updateClient"].action = "Main";
		    document.forms["updateClient"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<view:frame>
			<view:board>
				<form name="updateClient" action="" method="post">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.client"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="text" name="clientName" value="${clientName}" size="25">&nbsp;</td>
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
		document.forms["updateClient"].elements["clientName"].focus();
	</script>
</body>
</html>