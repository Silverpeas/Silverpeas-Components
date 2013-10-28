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
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
Boolean		ableToAddSubTask 	= (Boolean) request.getAttribute("AbleToAddSubTask");
TaskDetail 	task 				= (TaskDetail) request.getAttribute("Task");
String		role				= (String) request.getAttribute("Role");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript">
function callUserPanel() {
	SP_openWindow('ToUserPanel','', '750', '550','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setExtraInformation(task.getNom());

if (ableToAddSubTask.booleanValue()) {
	operationPane.addOperationOfCreation(resource.getIcon("projectManager.addTache"), resource.getString("projectManager.CreerTache"), "ToAddTask");
}

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Definition"), "#", true);
if (ableToAddSubTask.booleanValue()) {
	tabbedPane.addTab(resource.getString("GML.attachments"), "ToTaskAttachments", false);
}
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToTaskComments", false);

out.println(tabbedPane.print());
%>
<view:areaOfOperationOfCreation/>
<view:frame>
<center>
<table border="0" cellspacing="5"><tr><td width="100%">
<view:board>
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
    <td><%=EncodeHelper.javaStringToHtmlParagraphe(task.getUiDescription())%></td>
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
		Iterator it = task.getResources().iterator();
		int cpt = 0;
		out.println("<table id=\"tableResources\">");
		while(it.hasNext())
		{
			TaskResourceDetail resourceDetail = (TaskResourceDetail) it.next();
			resourceName = resourceDetail.getUserName();
			resourceId = resourceDetail.getUserId();  
			resourceCharge = resourceDetail.getCharge();
			occupation = resourceDetail.getOccupation();
			String couleur = "green";
			if (occupation > 100)
				couleur = "red";
					
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
</view:board>
</td>
<td valign="top">
	<% 
	    out.flush();
	  	getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+task.getId()+"&ComponentId="+componentId).include(request, response);
	%>
</td>
</tr></table>
</center>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>