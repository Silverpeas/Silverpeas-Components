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
	<c:if test="${admin}">
		<script type="text/javascript">
			function editDelivery(deliveryId) {
				submitForm(deliveryId, "NewDelivery");
			}
			
			function deleteDelivery(deliveryId) {
				if (window.confirm("<fmt:message key="crm.confirmDelete"/>")) {
			    	submitForm(deliveryId, "DeleteDeliverys");
			    }
			}
	
			function submitForm(deliveryId, action) {
				document.forms["deliveryForm"].action = action;
		    	document.forms["deliveryForm"].elements["deliveryId"].value = deliveryId;
		    	document.forms["deliveryForm"].submit();
			}
		</script>
	</c:if>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<c:if test="${admin}">
		<view:operationPane>
			<fmt:message key="crm.newDelivery" var="newDeliveryLabel"/>
			<fmt:message key="crm.newDelivery" var="newDeliveryIcon" bundle="${icons}"/>
			<c:url var="newDeliveryIconUrl" value="${newDeliveryIcon}"/>
			<view:operation altText="${newDeliveryLabel}" icon="${newDeliveryIconUrl}" action="NewDelivery"/>
		</view:operationPane>
	</c:if>
	<view:window>
		<view:tabs>
			<fmt:message key="crm.projet" var="projetLabel"/>
			<view:tab label="${projetLabel}" selected="" action="${myComponentURL}ViewProject"></view:tab>
			<fmt:message key="crm.client" var="clientLabel"/>
			<view:tab label="${clientLabel}" selected="" action="${myComponentURL}ViewClient"></view:tab>
			<fmt:message key="crm.delivrable" var="delivrableLabel"/>
			<view:tab label="${delivrableLabel}" selected="true" action="${myComponentURL}ViewDelivrable"></view:tab>
			<fmt:message key="crm.journal" var="journalLabel"/>
			<view:tab label="${journalLabel}" selected="" action="${myComponentURL}ViewJournal"></view:tab>
		</view:tabs>
		<view:frame>
			<view:board>
				<view:arrayPane var="" routingAddress="ViewDelivrable">
					<fmt:message key="crm.attachment" var="attachmentLabel"/>
					<view:arrayColumn title="${attachmentLabel}" sortable="false"/>
					<fmt:message key="crm.deliveryDate" var="deliveryDateLabel"/>
					<view:arrayColumn title="${deliveryDateLabel}" sortable="true"/>
					<fmt:message key="crm.deliveryElement" var="deliveryElementLabel"/>
					<view:arrayColumn title="${deliveryElementLabel}" sortable="true"/>
					<fmt:message key="crm.deliveryVersion" var="deliveryVersionLabel"/>
					<view:arrayColumn title="${deliveryVersionLabel}" sortable="true"/>
					<fmt:message key="crm.deliveryIntervenant" var="deliveryIntervenantLabel"/>
					<view:arrayColumn title="${deliveryIntervenantLabel}" sortable="true"/>
					<fmt:message key="crm.deliveryContactName" var="deliveryContactNameLabel"/>
					<view:arrayColumn title="${deliveryContactNameLabel}" sortable="true"/>
					<fmt:message key="crm.deliveryMedia" var="deliveryMediaLabel"/>
					<view:arrayColumn title="${deliveryMediaLabel}" sortable="true"/>
					<c:if test="${admin}">
						<fmt:message key="GML.operation" var="operationLabel"/>
						<view:arrayColumn title="${operationLabel}" sortable="false"/>
					</c:if>
					<c:forEach items="${deliveryVOs}" var="deliveryVO">
						<view:arrayLine>
							<view:arrayCellText text="${deliveryVO.attachments}"/>
							<view:arrayCellText text="${deliveryVO.deliveryDate}"/>
							<view:arrayCellText text="${deliveryVO.delivery.element}"/>
							<view:arrayCellText text="${deliveryVO.delivery.version}"/>
							<view:arrayCellText text="${deliveryVO.delivery.deliveryName}"/>
							<view:arrayCellText text="${deliveryVO.delivery.contactName}"/>
							<view:arrayCellText text="${deliveryVO.delivery.media}"/>
							<c:if test="${admin}">
								<view:arrayCellText text="${deliveryVO.operations}"/>
							</c:if>
						</view:arrayLine>
					</c:forEach>
				</view:arrayPane>
			</view:board>
			<c:if test="${admin}">
				<form name="deliveryForm" action="" method="post">
					<input type="hidden" name="deliveryId" value="">
				</form>
			</c:if>
		</view:frame>
	</view:window>
</body>
</html>