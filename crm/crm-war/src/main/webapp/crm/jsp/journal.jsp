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
			function editEvent(eventId) {
				submitForm(eventId, "NewEvent");
			}
			
			function deleteEvent(eventId) {
			    if (window.confirm("<fmt:message key="crm.confirmDelete"/>")) {
			    	submitForm(eventId, "DeleteEvents");
			    }
			}
	
			function submitForm(eventId, action) {
				document.forms["eventForm"].action = action;
				document.forms["eventForm"].elements["eventId"].value = eventId;
				document.forms["eventForm"].submit();
			}
		</script>
	</c:if>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<c:if test="${admin}">
		<view:operationPane>
			<fmt:message key="crm.newEvent" var="newEventLabel"/>
			<fmt:message key="crm.newEvent" var="newEventIcon" bundle="${icons}"/>
			<c:url var="newEventIconUrl" value="${newEventIcon}"/>
			<view:operation altText="${newEventLabel}" icon="${newEventIconUrl}" action="NewEvent"/>
		</view:operationPane>
	</c:if>
	<view:window>
		<view:tabs>
			<fmt:message key="crm.projet" var="projetLabel"/>
			<view:tab label="${projetLabel}" selected="" action="${myComponentURL}ViewProject"></view:tab>
			<fmt:message key="crm.client" var="clientLabel"/>
			<view:tab label="${clientLabel}" selected="" action="${myComponentURL}ViewClient"></view:tab>
			<fmt:message key="crm.delivrable" var="delivrableLabel"/>
			<view:tab label="${delivrableLabel}" selected="" action="${myComponentURL}ViewDelivrable"></view:tab>
			<fmt:message key="crm.journal" var="journalLabel"/>
			<view:tab label="${journalLabel}" selected="true" action="${myComponentURL}ViewJournal"></view:tab>
		</view:tabs>
		<view:frame>
			<view:board>
				<view:arrayPane var="" routingAddress="ViewJournal">
					<fmt:message key="crm.attachment" var="attachmentLabel"/>
					<view:arrayColumn title="${attachmentLabel}" sortable="false"/>
					<fmt:message key="crm.date" var="dateLabel"/>
					<view:arrayColumn title="${dateLabel}" sortable="true"/>
					<fmt:message key="crm.evenement" var="evenementLabel"/>
					<view:arrayColumn title="${evenementLabel}" sortable="true"/>
					<fmt:message key="crm.action" var="actionLabel"/>
					<view:arrayColumn title="${actionLabel}" sortable="true"/>
					<fmt:message key="crm.personne" var="personneLabel"/>
					<view:arrayColumn title="${personneLabel}" sortable="true"/>
					<fmt:message key="crm.quand" var="quandLabel"/>
					<view:arrayColumn title="${quandLabel}" sortable="true"/>
					<fmt:message key="crm.etat" var="etatLabel"/>
					<view:arrayColumn title="${etatLabel}" sortable="true"/>
					<c:if test="${admin}">
						<fmt:message key="GML.operation" var="operationLabel"/>
						<view:arrayColumn title="${operationLabel}" sortable="false"/>
					</c:if>
					<c:forEach items="${eventVOs}" var="eventVO">
						<view:arrayLine>
							<view:arrayCellText text="${eventVO.attachments}"/>
							<view:arrayCellText text="${eventVO.eventDate}"/>
							<view:arrayCellText text="${eventVO.event.eventLib}"/>
							<view:arrayCellText text="${eventVO.event.actionTodo}"/>
							<view:arrayCellText text="${eventVO.event.userName}"/>
							<view:arrayCellText text="${eventVO.actionDate}"/>
							<view:arrayCellText text="${eventVO.event.state}"/>
							<c:if test="${admin}">
								<view:arrayCellText text="${eventVO.operations}"/>
							</c:if>
						</view:arrayLine>
					</c:forEach>
				</view:arrayPane>
			</view:board>
			<c:if test="${admin}">
				<form name="eventForm" action="" method="post">
					<input type="hidden" name="eventId" value="">
				</form>
			</c:if>
		</view:frame>
	</view:window>
</body>
</html>
