<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%!
String getStatusIcon(int statut, ResourcesWrapper resource)
{
	String icon = "";
	switch (statut)
	{
		case 0 : 	icon = resource.getIcon("projectManager.enCours");
					break;
		case 1 : 	icon = resource.getIcon("projectManager.gelee");
					break;
		case 2 : 	icon = resource.getIcon("projectManager.abandonnee");
					break;
		case 3 : 	icon = resource.getIcon("projectManager.realisee");
					break;
		case 4 : 	icon = resource.getIcon("projectManager.alerte");
					break;    						     						     						 
		default : 	icon = resource.getIcon("projectManager.nondemarree");
					break;
	}
	return "<img src=\""+icon+"\" border=\"0\" align=\"absmiddle\">";
}

boolean otherActionOnSameLevel(List tasks, TaskDetail task, int debut)
{
	TaskDetail otherAction = null;
	for (int a=debut; a<tasks.size(); a++)
	{
		otherAction = (TaskDetail) tasks.get(a);
		if (otherAction.getLevel() == task.getLevel() && otherAction.getMereId() == task.getMereId())
			return true;
	}
	return false;
}

boolean otherActionOnLowerLevel(List tasks, TaskDetail task, int debut)
{
	TaskDetail otherAction = null;
	for (int a=debut; a<tasks.size(); a++)
	{
		otherAction = (TaskDetail) tasks.get(a);
		if (otherAction.getLevel() < task.getLevel())
			return true;
	}
	return false;
}

ArrayLine fillArrayLine(ArrayLine arrayLine, TaskDetail task, String iconeLiaison, int userId, String role, ResourcesWrapper resource, GraphicElementFactory gef)
{
	ArrayCellText cellStatut = arrayLine.addArrayCellText(getStatusIcon(task.getStatut(), resource));
	cellStatut.setAlignement("center");
	cellStatut.setCompareOn(new Integer(task.getStatut()));
	ArrayCellText cellChrono = arrayLine.addArrayCellText(task.getChrono());
	cellChrono.setCompareOn(new Integer(task.getChrono()));
	String nom = "<a href=javascript:onClick=goToTask('"+task.getId()+"')>"+task.getNom()+"</a>";

	if (task.getEstDecomposee() == 1) {
		String arbo = "<a href=\"UnfoldTask?Id="+task.getId()+"\"><img src=\""+resource.getIcon("projectManager.treePlus")+"\" border=\"0\" align=\"absmiddle\"></a>&nbsp;";
		if (task.isUnfold())
			arbo = "<a href=\"CollapseTask?Id="+task.getId()+"\"><img src=\""+resource.getIcon("projectManager.treeMinus")+"\" border=\"0\" align=\"absmiddle\"></a>&nbsp;";
		
		nom = iconeLiaison + arbo + nom;
	} else {
		nom = iconeLiaison + "&nbsp;" + nom;
	}
	
	ArrayCellText cellNom = null;
	if (task.getAttachments().size()>0)
		cellNom = arrayLine.addArrayCellText(nom+"&nbsp<img src=\""+resource.getIcon("projectManager.attachedFile")+"\" border=\"0\" align=\"absmiddle\">");
	else
		cellNom = arrayLine.addArrayCellText(nom);
	cellNom.setCompareOn(task.getNom());

	arrayLine.addArrayCellText(task.getResponsableFullName());
	
	ArrayCellText cellDebut = arrayLine.addArrayCellText(resource.getOutputDate(task.getDateDebut()));
	cellDebut.setCompareOn(task.getDateDebut());
	
	ArrayCellText cellFin = arrayLine.addArrayCellText(resource.getOutputDate(task.getDateFin()));
	cellFin.setCompareOn(task.getDateFin());
	
	ArrayCellText cellCharge = arrayLine.addArrayCellText(task.getCharge());
	cellCharge.setCompareOn(new Float(task.getCharge()));
	
	ArrayCellText cellConsomme = arrayLine.addArrayCellText(task.getConsomme());
	cellConsomme.setCompareOn(new Float(task.getConsomme()));
	
	ArrayCellText cellRaf = arrayLine.addArrayCellText(task.getRaf());
	cellRaf.setCompareOn(new Float(task.getRaf()));
	
	
	return arrayLine;
}
%>

<%
List 	tasks 	= (List) request.getAttribute("Tasks");
String 	role 		= (String) request.getAttribute("Role");
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script language="javascript">

function goToTask(id) {
    document.taskForm.Id.value = id;
    document.taskForm.submit();
}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
out.println(frame.printBefore());
%>
<br>
<center>
<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH="90%"><tr><td>
<% 
	Board board = gef.getBoard();
	out.println(board.printBefore());
%>

	<center>
	<table><tr>
	<td><img src="<%=resource.getIcon("projectManager.nondemarree")%>" border="0" align="absmiddle"></td><td class="txtNav"><%=resource.getString("projectManager.TacheAvancementND")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"></td>
	<td><img src="<%=resource.getIcon("projectManager.enCours")%>" border="0" align="absmiddle"></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutEnCours")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"></td>
	<td><img src="<%=resource.getIcon("projectManager.gelee")%>" border="0" align="absmiddle"></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutGelee")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"></td>
	<td><img src="<%=resource.getIcon("projectManager.realisee")%>" border="0" align="absmiddle"></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutRealisee")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"></td>
	<td><img src="<%=resource.getIcon("projectManager.abandonnee")%>" border="0" align="absmiddle"></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutAbandonnee")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"></td>
	<td><img src="<%=resource.getIcon("projectManager.alerte")%>" border="0" align="absmiddle"></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutEnAlerte")%></td>
	</tr></table>
	</center>
<%
	out.println(board.printAfter());
%>
</td></tr></table>
</center>

<br>
<%

ArrayPane arrayPane = gef.getArrayPane("actionsList", "Main", request, session);
arrayPane.setCellsConfiguration(0, 0, 0);
ArrayColumn arrayColumn1 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheStatut"));
arrayColumn1.setAlignement("center");
arrayPane.addArrayColumn(resource.getString("projectManager.TacheNumero"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheNom"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheResponsable"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheDebut"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheFin"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheCharge"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheConso"));
arrayPane.addArrayColumn(resource.getString("projectManager.TacheReste"));


TaskDetail 	task 			= null;
TaskDetail	actionSuivante	= null;
ArrayLine 		arrayLineRoot	= null;
ArrayLine 		arrayLine 		= null;
String			indent			= "";
String			imgIndent		= "";
String			ilt				= "";
for (int a=0; a<tasks.size(); a++)
{
	task = (TaskDetail) tasks.get(a);
	indent = "";
	
	if (task.getLevel()==0)
	{
		arrayLineRoot = arrayPane.addArrayLine();
		arrayLineRoot = fillArrayLine(arrayLineRoot, task, "", 0, role, resource, gef);
	} else {
		arrayLine = new ArrayLine(arrayPane);
		
		imgIndent = m_context+"/util/icons/colorPix/15px.gif";
		if (otherActionOnLowerLevel(tasks, task, a+1))
		{
			imgIndent = resource.getIcon("projectManager.treeI");
		}
		
		for (int i=1; i<task.getLevel(); i++)
		{
			indent += "<img src=\""+imgIndent+"\" border=\"0\" align=\"absmiddle\">";
		}

		ilt = indent+"<img src=\""+resource.getIcon("projectManager.treeL")+"\" border=\"0\" align=\"absmiddle\">";
		//il reste � savoir s'il on met un T ou un L
		//on regarde l'task suivante
		if (a+1 < tasks.size())
		{
			actionSuivante = (TaskDetail) tasks.get(a+1);
			if (actionSuivante.getLevel() == task.getLevel())
			{
				ilt = indent+"<img src=\""+resource.getIcon("projectManager.treeT")+"\" border=\"0\" align=\"absmiddle\">";
			}
			else {
				if (otherActionOnSameLevel(tasks, task, a+1))
				{
					ilt = indent+"<img src=\""+resource.getIcon("projectManager.treeT")+"\" border=\"0\" align=\"absmiddle\">";
				}
			}
		}
		
		arrayLine = fillArrayLine(arrayLine, task, ilt, 0, role, resource, gef);
			
		arrayLineRoot.addSubline(arrayLine);
	}
}
out.println(arrayPane.print());
out.println(frame.printAfter());

%>
<form name="taskForm" action="ViewTask" Method="POST" target="MyMain">
	<input type="hidden" name="Id">
</form>

</body>
</html>
