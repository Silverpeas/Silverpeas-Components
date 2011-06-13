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
		    document.forms["newEvent"].action = "ViewJournal";
		    document.forms["newEvent"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<view:frame>
			<view:board>
				<form name="newEvent" action="" method="post">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.eventDate"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${eventDate}</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.eventLib"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${eventLib}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actionTodo"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${actionTodo}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.personne"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${FilterLib}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.actionDate"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${actionDate}</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.eventState"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>
								<c:forEach items="${States}" var="state">
									<c:if test="${state[0] eq eventState}">${state[1]}</c:if>
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