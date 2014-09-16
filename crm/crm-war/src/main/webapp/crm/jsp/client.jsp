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

<view:setBundle basename="com.silverpeas.crm.multilang.crmBundle"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<html>
<head>
	<title><fmt:message key="GML.popupTitle"/></title>
	<view:looknfeel/>
	<script type="text/javascript" src="${context}/util/javaScript/checkForm.js"></script>
	<c:if test="${admin}">
		<script type="text/javascript">
			function editContact(contactId) {
				submitForm(contactId, "NewContact");
			}
			
			function deleteContact(contactId) {
				if (window.confirm("<fmt:message key="crm.confirmDelete"/>")) {
			    	submitForm(contactId, "DeleteContacts");
			    }
			}
	
			function submitForm(contactId, action) {
				document.forms["contactForm"].action = action;
		    	document.forms["contactForm"].elements["contactId"].value = contactId;
		    	document.forms["contactForm"].submit();
			}

			function openSPWindow(url, windowName){
				SP_openWindow(url, windowName, "600", "400", "scrollbars=yes, resizable, alwaysRaised");
			}
		</script>
	</c:if>
</head>

<body marginheight="5" marginwidth="5" leftmargin="5" topmargin="5">
	<c:if test="${admin}">
		<view:operationPane>
			<fmt:message key="crm.update" var="updateLabel"/>
			<fmt:message key="crm.updateClient" var="updateClientIcon" bundle="${icons}"/>
			<c:url var="updateClientIconUrl" value="${updateClientIcon}"/>
			<view:operation altText="${updateLabel}" icon="${updateClientIconUrl}" action="UpdateClient"/>
			<view:operationSeparator/>
			<fmt:message key="crm.newContact" var="newContactLabel"/>
			<fmt:message key="crm.newContact" var="newContactIcon" bundle="${icons}"/>
			<c:url var="newContactIconUrl" value="${newContactIcon}"/>
			<view:operation altText="${newContactLabel}" icon="${newContactIconUrl}" action="NewContact"/>
		</view:operationPane>
	</c:if>
	<view:window>
		<view:tabs>
			<fmt:message key="crm.projet" var="projetLabel"/>
			<view:tab label="${projetLabel}" selected="" action="${myComponentURL}ViewProject"></view:tab>
			<fmt:message key="crm.client" var="clientLabel"/>
			<view:tab label="${clientLabel}" selected="true" action="${myComponentURL}ViewClient"></view:tab>
			<fmt:message key="crm.delivrable" var="delivrableLabel"/>
			<view:tab label="${delivrableLabel}" selected="" action="${myComponentURL}ViewDelivrable"></view:tab>
			<fmt:message key="crm.journal" var="journalLabel"/>
			<view:tab label="${journalLabel}" selected="" action="${myComponentURL}ViewJournal"></view:tab>
		</view:tabs>
		<view:frame>
			<view:board>
  				<table width="100%" border="0" cellspacing="0" cellpadding="4">
  					<tr>
  						<td><span class=txtlibform><fmt:message key="crm.client"/>&nbsp;:&nbsp;</span>${clientName}</td>
  					</tr>
  				</table>
				<fmt:message key="crm.contacts" var="contactsLabel"/>
				<view:arrayPane var="" title="${contactsLabel}" routingAddress="ViewClient">
					<fmt:message key="crm.attachment" var="attachmentLabel"/>
					<view:arrayColumn title="${attachmentLabel}" sortable="false"/>
					<fmt:message key="crm.nom" var="nomLabel"/>
					<view:arrayColumn title="${nomLabel}" sortable="true"/>
					<fmt:message key="crm.fonction" var="fonctionLabel"/>
					<view:arrayColumn title="${fonctionLabel}" sortable="true"/>
					<fmt:message key="crm.tel" var="telLabel"/>
					<view:arrayColumn title="${telLabel}" sortable="true"/>
					<fmt:message key="crm.email" var="emailLabel"/>
					<view:arrayColumn title="${emailLabel}" sortable="true"/>
					<fmt:message key="crm.adresse" var="adresseLabel"/>
					<view:arrayColumn title="${adresseLabel}" sortable="true"/>
					<fmt:message key="crm.actif" var="actifLabel"/>
					<view:arrayColumn title="${actifLabel}" sortable="false"/>
					<c:if test="${admin}">
						<fmt:message key="GML.operation" var="operationLabel"/>
						<view:arrayColumn title="${operationLabel}" sortable="false"/>
					</c:if>
					<c:forEach items="${contactVOs}" var="contactVO">
						<view:arrayLine>
							<view:arrayCellText text="${contactVO.attachments}"/>
							<view:arrayCellText text="${contactVO.contact.name}"/>
							<view:arrayCellText text="${contactVO.contact.functionContact}"/>
							<view:arrayCellText text="${contactVO.contact.tel}"/>
							<view:arrayCellText text="${contactVO.contact.email}"/>
							<view:arrayCellText text="${contactVO.contact.address}"/>
							<view:arrayCellText text="${contactVO.active}"/>
							<c:if test="${admin}">
								<view:arrayCellText text="${contactVO.operations}"/>
							</c:if>
						</view:arrayLine>
					</c:forEach>
				</view:arrayPane>
			</view:board>
			<c:if test="${admin}">
				<form name="contactForm" action="" method="post">
					<input type="hidden" name="contactId" value="">
				</form>
			</c:if>
		</view:frame>
	</view:window>
</body>
</html>