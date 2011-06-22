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
		    document.forms["newDelivery"].action = "ViewDelivrable";
		    document.forms["newDelivery"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<view:frame>
			<view:board>
				<form name="newDelivery" action="" method="post">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryDate"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${deliveryDate}</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryElement"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${deliveryElement}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryVersion"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${deliveryVersion}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryIntervenant"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${FilterLib}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryContactName"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>
								<c:forEach items="${Contacts}" var="contact">
									<c:if test="${contact[0] eq deliveryContact}">${contact[1]}</c:if>
								</c:forEach>
							</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.deliveryMedia"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>
								<c:forEach items="${Medias}" var="media">
									<c:if test="${media[0] eq deliveryMedia}">${media[1]}</c:if>
								</c:forEach>
							</td>
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