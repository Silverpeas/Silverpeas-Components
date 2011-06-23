<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>

<html>
<head>
	<title><fmt:message key="GML.popupTitle"/></title>
	<view:looknfeel/>
	<script type="text/javascript">		
		function cancelForm() {
		    document.forms["newContact"].action = "ViewClient";
		    document.forms["newContact"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<view:frame>
			<view:board>
				<form name="newContact" action="" method="post">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.nom"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${contactName}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.fonction"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${contactFunction}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.tel"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${contactTel}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.email"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${contactEmail}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.adresse"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${contactAddress}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actif"/>&nbsp;:&nbsp;</span></td>
							<td nowrap><input type="checkbox" name="contactActif" value=""<c:if test="${contactActif eq '1'}"> checked</c:if> disabled="disabled">&nbsp;</td>
						</tr>
					</table>
				</form>
			</view:board>
			<center>
				<view:buttonPane>
					<fmt:message key="GML.back" var="cancelLabel"/>
					<view:button label="${cancelLabel}" action="javascript:cancelForm();"/>
				</view:buttonPane>
			</center>
		</view:frame>
	</view:window>
</body>
</html>