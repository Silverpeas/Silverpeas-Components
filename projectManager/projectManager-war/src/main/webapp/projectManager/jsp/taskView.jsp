<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.core.admin.user.model.SilverpeasRole" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="check.jsp" %>

<%
Boolean		ableToAddSubTask 	= (Boolean) request.getAttribute("AbleToAddSubTask");
TaskDetail 	task 				= (TaskDetail) request.getAttribute("Task");
String		role				= (String) request.getAttribute("Role");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript">
function deleteTask() {
  var label = "<%=resource.getString("projectManager.SupprimerTacheConfirmation")%>";
  jQuery.popup.confirm(label, function() {
    document.deleteForm.submit();
  });
}
</script>
</head>
<body>
<%
browseBar.setExtraInformation(task.getNom());

if (ableToAddSubTask) {
  operationPane.addOperation(null, resource.getString("GML.update"), "ToUpdateTask?Id=" + task.getId());
  operationPane.addOperation(null, resource.getString("GML.delete"), "javascript:deleteTask()");
  operationPane.addLine();
	operationPane.addOperationOfCreation(resource.getIcon("projectManager.addTache"), resource.getString("projectManager.CreerTache"), "ToAddTask");
}

out.println(window.printBefore());
%>
<view:areaOfOperationOfCreation/>

<div class="rightContent">
  <c:set var="callbackUrl"><%=URLUtil.getURL("useless", componentId) + "ViewTask?Id=" + task.getId()%></c:set>
  <viewTags:displayAttachments componentInstanceId="<%=componentId%>"
                               resourceId="<%=String.valueOf(task.getId())%>"
                               resourceType="<%=task.getContributionType()%>"
                               highestUserRole="<%=SilverpeasRole.fromString(role)%>"
                               reloadCallbackUrl="${callbackUrl}"/>
</div>

<div class="principalContent">
<form name="actionForm" action="UpdateTask" method="post">
<table cellpadding="5">
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheNumero")%> :</td>
    <td><%=task.getChrono()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheOrganisateur")%> :</td>
    <td><%=task.getOrganisateurFullName()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheResponsable")%> :</td>
    <td><%=task.getResponsableFullName()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheNom")%> <%=resource.getString("projectManager.Tache")%> :</td>
    <td><%=task.getNom()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TachePrecedente")%> :</td>
    <td>
    <%
	if (task.getPreviousTaskId() != -1)
		out.println(task.getPreviousTaskName());
    %>
    </td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheCharge")%> :</td>
    <td><%=task.getCharge()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheConsomme")%> :</td>
    <td><%=task.getConsomme()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheResteAFaire")%> :</td>
    <td><%=task.getRaf()%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheAvancement")%> :</td>
    <td><%=task.getAvancement()%> %</td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheStatut")%> :</td>
    <td>
	<%
			String icon 	= "";
		String statut 	= "";
		switch (task.getStatut())
			{
				case 0 : statut = resource.getString("projectManager.TacheStatutEnCours");
						 icon = resource.getIcon("projectManager.enCours");
						 break;
				case 1 : statut = resource.getString("projectManager.TacheStatutGelee");
						 icon = resource.getIcon("projectManager.gelee");
						 break;
				case 2 : statut = resource.getString("projectManager.TacheStatutAbandonnee");
						 icon = resource.getIcon("projectManager.abandonnee");
						 break;
				case 3 : statut = resource.getString("projectManager.TacheStatutRealisee");
						 icon = resource.getIcon("projectManager.realisee");
						 break;
				case 4 : statut = resource.getString("projectManager.TacheStatutEnAlerte");
						 icon = resource.getIcon("projectManager.alerte");
						 break;
				case 5 : statut = resource.getString("projectManager.TacheAvancementND");
						 icon = resource.getIcon("projectManager.nondemarree");
						 break;

			}
		%>
	<img src="<%=icon%>" border="0" align="middle"/>&nbsp;<%=statut%>
    </td>
</tr>
<tr>
	<td class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</td>
    <td><%=WebEncodeHelper.javaStringToHtmlParagraphe(task.getUiDescription())%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheDateDebut")%> :</td>
    <td><%=resource.getOutputDate(task.getDateDebut())%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.TacheDateFin")%> :</td>
    <td><%=resource.getOutputDate(task.getDateFin())%></td>
</tr>
<!-- affichage des ressources -->
<tr>
<td class="txtlibform"><%=resource.getString("projectManager.TacheResources")%> :</td>
<td>
<%
	String resourceName = "";
	String resourceId = "";
	int resourceCharge = 0;
	int occupation = 0;

	if (task.getResources() != null) {
		int cpt = 0;
		out.println("<table id=\"tableResources\">");
		for (TaskResourceDetail resourceDetail : task.getResources()) {
			resourceName = resourceDetail.getUserName();
			resourceId = resourceDetail.getUserId();
			resourceCharge = resourceDetail.getCharge();
			occupation = resourceDetail.getOccupation();
			String couleur = "green";
			if (occupation > 100) {
				couleur = "red";
			}

			%>
			<tr>
				<td><%=resourceName%></td>
				<td>&nbsp;<%=resourceCharge%>%</td>
				<td>&nbsp;&nbsp;&nbsp;<span id="Occupation<%=cpt%>"><font color="<%=couleur%>"><%=occupation%>%</font></span></td>
			</tr>
			<%
			cpt++;
		}
		out.println("</table>");
	} %>
	</td>
</tr>
</table>
</form>
  <viewTags:viewAttachmentsAsContent componentInstanceId="<%= componentId %>"
                                   resourceType="Task"
                                   resourceId="<%=String.valueOf(task.getId())%>"
                                   highestUserRole="<%=SilverpeasRole.fromString(role)%>"/>

  <viewTags:displayComments componentId="<%=componentId%>"
														resourceType="<%=task.getContributionType()%>"
														resourceId="<%=String.valueOf(task.getId())%>" />

</div>
<%
out.println(window.printAfter());
%>
<form name="deleteForm" action="RemoveTask" method="post">
  <input type="hidden" name="Id" value="<%=task.getId()%>"/>
</form>
</body>
</html>
