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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
		    document.forms["newParticipant"].action = "ViewProject";
		    document.forms["newParticipant"].submit();
		}
	</script>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<view:window>
		<view:frame>
			<view:board>
				<form name="newParticipant" action="" method="post">
					<table width="100%" border="0" cellspacing="0" cellpadding="4">
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.nom"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>${FilterLib}&nbsp;</td>
						</tr>
						<tr>
							<td width="40%" nowrap><span class=txtlibform><fmt:message key="crm.fonction"/>&nbsp;:&nbsp;</span></td>
							<td nowrap>
								<c:forEach items="${Functions}" var="func">
									<c:if test="${func[0] eq participantFunction}">${func[1]}</c:if>
								</c:forEach>
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
							<td nowrap><input type="checkbox" name="participantActif" value=""<c:if test="${participantActif eq '1'}"> checked</c:if> disabled="disabled">&nbsp;</td>
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