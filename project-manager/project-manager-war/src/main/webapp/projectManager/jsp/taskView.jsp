<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ include file="check.jsp" %>

<%
Boolean		ableToAddSubTask 	= (Boolean) request.getAttribute("AbleToAddSubTask");
TaskDetail 	task 				= (TaskDetail) request.getAttribute("Task");
String		role				= (String) request.getAttribute("Role");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<SCRIPT LANGUAGE="JAVASCRIPT">
function callUserPanel()
{
	SP_openWindow('ToUserPanel','', '750', '550','scrollbars=yes, resizable, alwaysRaised');
}
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setExtraInformation(task.getNom());

if (ableToAddSubTask.booleanValue()) {
	operationPane.addOperation(resource.getIcon("projectManager.addTache"), resource.getString("projectManager.CreerTache"), "ToAddTask");
}

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Definition"), "#", true);
if (ableToAddSubTask.booleanValue()) {
	tabbedPane.addTab(resource.getString("GML.attachments"), "ToTaskAttachments", false);
}
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToTaskComments", false);

out.println(tabbedPane.print());
out.println(frame.printBefore());
%>
<center>
<table border="0" cellspacing="5"><tr><td width="100%">
<%
Board board = gef.getBoard();
out.println(board.printBefore());
%>
<table CELLPADDING=5>
<FORM Name="actionForm" Action="UpdateTask" Method="POST">
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheNumero")%> :</TD>
    <TD><%=task.getChrono()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheOrganisateur")%> :</TD>
    <TD><%=task.getOrganisateurFullName()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheResponsable")%> :</TD>
    <TD><%=task.getResponsableFullName()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheNom")%> <%=resource.getString("projectManager.Tache")%> :</TD>
    <TD><%=task.getNom()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TachePrecedente")%> :</TD>
    <TD>
    <%
    	if (task.getPreviousTaskId() != -1)
    		out.println(task.getPreviousTaskName());
    %>
    </TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheCharge")%> :</TD>
    <TD><%=task.getCharge()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheConsomme")%> :</TD>
    <TD><%=task.getConsomme()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheResteAFaire")%> :</TD>
    <TD><%=task.getRaf()%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheAvancement")%> :</TD>
    <TD><%=task.getAvancement()%> %</TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheStatut")%> :</TD>
    <TD>
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
    	<img src="<%=icon%>" border="0" align="absmiddle">&nbsp;<%=statut%>
    </TD>
</TR>
<TR>
	<TD class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</TD>
    <TD><%=Encode.javaStringToHtmlParagraphe(task.getUiDescription())%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheDateDebut")%> :</TD>
    <TD><%=resource.getOutputDate(task.getDateDebut())%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.TacheDateFin")%> :</TD>
    <TD><%=resource.getOutputDate(task.getDateFin())%></TD>
</TR>
<!-- affichage des ressources --> 
<TR>
<TD class="txtlibform"><%=resource.getString("projectManager.TacheResources")%> :</TD>
<TD> 
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
	</TD>
</TR>
</FORM>
</table>
<%
out.println(board.printAfter());
%>
</td>
<td valign="top">
	<% 
	    out.flush();
	  	getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+task.getId()+"&ComponentId="+componentId).include(request, response);
	%>
</td>
</tr></table>
<center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>