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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%!
String getStatusIcon(int statut, MultiSilverpeasBundle resource)
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
	return "<img src=\""+icon+"\" align=\"absmiddle\"/>";
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

ArrayLine fillArrayLine(ArrayLine arrayLine, TaskDetail task, String iconeLiaison, int userId, String role, MultiSilverpeasBundle resource, GraphicElementFactory gef)
{
	ArrayCellText cellStatut = arrayLine.addArrayCellText(getStatusIcon(task.getStatut(), resource));
	cellStatut.setAlignement("center");
	cellStatut.setCompareOn(new Integer(task.getStatut()));
	ArrayCellText cellChrono = arrayLine.addArrayCellText(task.getChrono());
	cellChrono.setCompareOn(new Integer(task.getChrono()));
	String nom = "<a href=\"ViewTask?Id="+task.getId()+"\">"+task.getNom()+"</a>";

	if (task.getEstDecomposee() == 1) {
		String arbo = "<a href=\"UnfoldTask?Id="+task.getId()+"\"><img src=\""+resource.getIcon("projectManager.treePlus")+"\" border=\"0\" align=\"absmiddle\"></a>&nbsp;";
		if (task.isUnfold())
			arbo = "<a href=\"CollapseTask?Id="+task.getId()+"\"><img src=\""+resource.getIcon("projectManager.treeMinus")+"\" border=\"0\" align=\"absmiddle\"></a>&nbsp;";
		
		nom = iconeLiaison + arbo + nom;
	} else {
		nom = iconeLiaison + "&nbsp;" + nom;
	}
	
	ArrayCellText cellNom = null;
	if (!task.getAttachments().isEmpty())
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
	
	if (task.isDeletionAvailable() || task.isUpdateAvailable())
	{
		IconPane iconPane = gef.getIconPane();
		if (task.isUpdateAvailable())
		{
		    Icon updateIcon = iconPane.addIcon();
		    updateIcon.setProperties(resource.getIcon("projectManager.update"), resource.getString("projectManager.ModifierAction"), "ToUpdateTask?Id="+task.getId());
		}
		if (task.isDeletionAvailable())
		{
		    Icon deleteIcon = iconPane.addIcon();
		    deleteIcon.setProperties(resource.getIcon("projectManager.delete"), resource.getString("projectManager.SupprimerAction"),"javascript:onClick=deleteTask('"+task.getId()+"')");
		}
		arrayLine.addArrayCellIconPane(iconPane);
	} else if (role.equals("responsable")){
		arrayLine.addArrayEmptyCell();
	}
	return arrayLine;
}
%>

<%
List 	tasks 	= (List) request.getAttribute("Tasks");
String 	role 		= (String) request.getAttribute("Role");
Boolean filtreActif = (Boolean) request.getAttribute("FiltreActif");
Filtre	filtre		= (Filtre) request.getAttribute("Filtre");
Integer	userId		= new Integer((String) request.getAttribute("UserId"));

String actionFrom 		= "";
String actionTo			= "";
String actionNom		= "";
String statut			= "-1";
String dateDebutFrom	= "";
String dateDebutTo		= "";
String dateFinFrom		= "";
String dateFinTo		= "";
String retard			= "-1";
String avancement		= "-1";
String responsableName	= "";
String responsableId	= "";
if (filtre != null)
{
	actionFrom 		= filtre.getActionFrom();
	if (actionFrom == null || "null".equals(actionFrom))
		actionFrom = "";
	actionTo		= filtre.getActionTo();
	if (actionTo == null || "null".equals(actionTo))
		actionTo = "";
	actionNom		= filtre.getActionNom();
	if (actionNom == null || "null".equals(actionNom))
		actionNom = "";
	statut			= filtre.getStatut();
	dateDebutFrom	= filtre.getDateDebutFromUI();
	dateDebutTo		= filtre.getDateDebutToUI();
	dateFinFrom		= filtre.getDateFinFromUI();
	dateFinTo		= filtre.getDateFinToUI();
	retard			= filtre.getRetard();
	avancement		= filtre.getAvancement();
	responsableName	= filtre.getResponsableName();
	responsableId	= filtre.getResponsableId();
}

String imgCollapse 	= "<img src=\""+resource.getIcon("projectManager.treePlus")+"\">";
String imgUnfold 	= "<img src=\""+resource.getIcon("projectManager.treeMinus")+"\">";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript">
function exportTasks(){
	SP_openWindow('Export', 'exportTasks', '750', '550','scrollbars=yes, menubar=yes, resizable, alwaysRaised');
}
function deleteTask(id) {
    if(window.confirm("<%=resource.getString("projectManager.SupprimerTacheConfirmation")%>")){
      document.listForm.Id.value = id;
      document.listForm.submit();
    }
}
function callUserPanel()
{
	SP_openWindow('ToUserPanel','', '750', '550','scrollbars=yes, resizable, alwaysRaised');
}
function editDate(indiceElem)
{
		chemin = "<%=m_context%><%=URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp?indiceForm=0&indiceElem="+indiceElem;
		largeur = "180";
		hauteur = "200";
		SP_openWindow(chemin,"Calendrier_Todo",largeur,hauteur,"");
}
function isCorrectDate(input)
{
	var date = input.value;
    if (!isWhitespace(date)) {
    	if (!isDateOK(date, '<%=resource.getLanguage()%>')) {
    		return false;
    	}
    }
    return true;
}
function isCorrectForm() {
     var errorMsg 			= "";
     var errorNb 			= 0;
     
     if (!isCorrectDate(document.actionForm.DateDebutFrom))
     {
     	errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDatePV")%> (<%=resource.getString("projectManager.Du")%>)' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
        errorNb++;
     }
     if (!isCorrectDate(document.actionForm.DateDebutTo))
     {
     	errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDatePV")%> (<%=resource.getString("projectManager.Au")%>)' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
        errorNb++;
     }
     if (!isCorrectDate(document.actionForm.DateFinFrom))
     {
     	errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDatePV")%> (<%=resource.getString("projectManager.Du")%>)' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
        errorNb++;
     }
     if (!isCorrectDate(document.actionForm.DateFinTo))
     {
     	errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDatePV")%> (<%=resource.getString("projectManager.Au")%>)' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
        errorNb++;
     }
     
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

function sendFilterData() {
    if (isCorrectForm()) {
         document.actionForm.submit();
     }
}
</script>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");

operationPane.addOperation(resource.getIcon("projectManager.exportTaches"), resource.getString("projectManager.Export"), "javaScript:exportTasks()");
if ("admin".equals(role)) {
	operationPane.addOperationOfCreation(resource.getIcon("projectManager.addTache"), resource.getString("projectManager.CreerTache"), "ToAddTask");
}

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", false);
tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", true);
if ("admin".equals(role)) {
	tabbedPane.addTab(resource.getString("GML.attachments"), "ToAttachments", false);
}
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
if ("admin".equals(role)) {
	tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
}
out.println(tabbedPane.print());
%>
<view:frame>
<!---------------------------------------------------------------------------------->
<!--------------------------- FILTRE ----------------------------------------------->
<!---------------------------------------------------------------------------------->
<center>
<form name="actionForm" method="post" action="ToFilterTasks">
<table cellpadding=0 cellspacing=2 border=0 width="98%" class=intfdcolor>
	<tr>
		<td class=intfdcolor4 nowrap>
			<table cellpadding=0 cellspacing=0 border=0 width="100%">
				<tr>
					<td class="intfdcolor" rowspan="2" nowrap width="100%">
						<img border="0" src="<%=resource.getIcon("projectManager.px") %>" width="5"/>
						<span class="txtNav"><%=resource.getString("projectManager.Filtre") %></span>
					</td>
					<td class="intfdcolor"><img border="0" height="10" src="<%=resource.getIcon("projectManager.px") %>"/></td>
					<td class="intfdcolor"><img border="0" height="10" src="<%=resource.getIcon("projectManager.px") %>"/></td>
				</tr>
				<tr>
					<td height="0" class="intfdcolor" align="right" valign="bottom"><img border="0" src="<%=resource.getIcon("projectManager.boxAngleLeft") %>"/></td>
					<td align="center" valign="bottom" nowrap>
					<%if (!filtreActif.booleanValue()) {
						out.println("<a href=\"FilterShow\"><img border=\"0\" src=\""+resource.getIcon("projectManager.boxDown")+"\"/></a>");
					}
					else{
						out.println("<a href=\"FilterHide\"><img border=\"0\" src=\""+resource.getIcon("projectManager.boxUp")+"\"/></a>");
					}
					%>
					<img border="0" height="1" width="3" src="<%=resource.getIcon("projectManager.px") %>"/>
					</td>
				</tr>
			</table>
			<% if (filtreActif.booleanValue()) { %>
				<table cellpadding="5" cellspacing="0" border="0" width="100%">
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheNumero")%> <%=resource.getString("projectManager.Tache")%></td>
						<td><%=resource.getString("projectManager.De")%> <input type="text" name="TaskFrom" size="6" value="<%=actionFrom%>"/> <%=resource.getString("projectManager.A")%> <input type="text" name="TaskTo" size="6" value="<%=actionTo%>"/></td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheNom")%> <%=resource.getString("projectManager.Tache")%></td>
						<td><input type="text" name="TaskNom" size="60" value="<%=actionNom%>"/></td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheStatut")%></td>
						<td>
							<select name="Statut" size="1">
							<%
								String 	label 	= "";
					    		String 	statusSelected;
					    		int 	iStatut = new Integer(statut).intValue();
					    		for (int s=-1; s<6; s++)
					    		{
					    			switch (s)
					    			{
					    				case -1 : label = "&nbsp;";
					    						 break;
					    				case 0 : label = resource.getString("projectManager.TacheStatutEnCours");
					    						 break;
					    				case 1 : label = resource.getString("projectManager.TacheStatutGelee");
					    						 break;
					    				case 2 : label = resource.getString("projectManager.TacheStatutAbandonnee");
					    						 break;
					    				case 3 : label = resource.getString("projectManager.TacheStatutRealisee");
					    						 break;
					    				case 4 : label = resource.getString("projectManager.TacheStatutEnAlerte");
					    						 break;    						     						     						 
					    				case 5 : label = resource.getString("projectManager.TacheAvancementND");
					    						 break;    						     						     						 
					    			}
					    			statusSelected = "";
					    			if (s==iStatut)
					    				statusSelected = "selected";
					    				
					   				out.println("<option value=\""+s+"\" "+statusSelected+">"+label+"</option>");
					    		}
					    	%>
					    	</select>
						</td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheDateDebut")%></td>
						<td><%=resource.getString("projectManager.Du")%> <input type="text" name="DateDebutFrom" size="12" maxlength="10" value="<%=dateDebutFrom%>"/>&nbsp;<a href="javascript:onClick=editDate(4)"><img src="<%=resource.getIcon("projectManager.calendrier")%>" border=0 valign=absmiddle align="middle" alt="<%=resource.getString("GML.viewCalendar")%>"/></a>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span>&nbsp;&nbsp;&nbsp;&nbsp;<%=resource.getString("projectManager.Au")%> <input type="text" name="DateDebutTo" size="12" maxlength="10" value="<%=dateDebutTo%>"/>&nbsp;<a href="javascript:onClick=editDate(5)"><img src="<%=resource.getIcon("projectManager.calendrier")%>"  border=0 valign=absmiddle align="middle" alt="<%=resource.getString("GML.viewCalendar")%>"/></a>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span></td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheDateFin")%></td>
						<td><%=resource.getString("projectManager.Du")%> <input type="text" name="DateFinFrom" size="12" maxlength="10" value="<%=dateFinFrom%>"/>&nbsp;<a href="javascript:onClick=editDate(6)"><img src="<%=resource.getIcon("projectManager.calendrier")%>" border=0 valign=absmiddle align="middle" alt="<%=resource.getString("GML.viewCalendar")%>"/></a>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span>&nbsp;&nbsp;&nbsp;&nbsp;<%=resource.getString("projectManager.Au")%> <input type="text" name="DateFinTo" size="12" maxlength="10" value="<%=dateFinTo%>"/>&nbsp;<a href="javascript:onClick=editDate(7)"><img src="<%=resource.getIcon("projectManager.calendrier")%>"  border=0 valign=absmiddle align="middle" alt="<%=resource.getString("GML.viewCalendar")%>"/></a>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span></td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.Retard")%></td>
						<td>
							<%
								String retardOui 	= "";
								String retardNon 	= "";
								String retardToutes	= "";
								if (retard.equals("1"))
									retardOui = "checked";
								else if (retard.equals("0"))
									retardNon = "checked";
								else
									retardToutes = "checked";
							%>
							<input type="radio" name="Retard" value="1" <%=retardOui%>/> <%=resource.getString("GML.yes")%> <input type="radio" name="Retard" value="0" <%=retardNon%>/> <%=resource.getString("GML.no")%> <input type="radio" name="Retard" value="-1" <%=retardToutes%>/> <%=resource.getString("GML.allFP")%>
						</td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheAvancement")%></td>
						<td>
							<%
								String av100 	= "";
								String av50 	= "";
								String avToutes	= "";
								if (avancement.equals("1"))
									av100 = "checked";
								else if (avancement.equals("0"))
									av50 = "checked";
								else
									avToutes = "checked";
							%>
							<input type="radio" name="Avancement" value="1" <%=av100%>/> 100% <input type="radio" name="Avancement" value="0" <%=av50%>/> <100% <input type="radio" name="Avancement" value="-1" <%=avToutes%>/> <%=resource.getString("GML.allFP")%>
						</td>
					</tr>
					<tr>
						<td class="txtlibform"><%=resource.getString("projectManager.TacheResponsable")%></td>
						<td><input type="text" name="Responsable" value="<%=responsableName%>" size="60" disabled/><input type="hidden" name="ResponsableName" value="<%=responsableName%>"/><input type="hidden" name="ResponsableId" value="<%=responsableId%>"/>&nbsp;<a href="javascript:callUserPanel()"><img src="<%=resource.getIcon("projectManager.userPanel")%>" alt="<%=resource.getString("projectManager.SelectionnerResponsable")%>" border=0 align="absmiddle"/></a></td>
					</tr>
					<tr>
						<td colspan="2" align="center">
						<%
							ButtonPane buttonPane = gef.getButtonPane();
							buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendFilterData()", false));
							buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "Main", false));
							out.println(buttonPane.print());
						%>
						</td>
					</tr>
					<tr>
						<td colspan="2" align="right"><a href="FilterHide"><img border="0" src="<%=resource.getIcon("projectManager.boxUp") %>"/></a><img border="0" width="3" src="<%=resource.getIcon("projectManager.px") %>"/></td>
					</tr>
				</table>
			<% } else { %>
					<table border="0" cellpadding="0" cellspacing="0"><tr><td class="intfdcolor4"><img border="0" src="<%=resource.getIcon("projectManager.px") %>"/></td></tr></table>
			<% } %>
		</td>
	</tr>
</table>
</form> 
</center>
<br/>
<view:board>
	<center>
	<table><tr>
	<td><img src="<%=resource.getIcon("projectManager.nondemarree")%>" border="0" align="absmiddle"/></td><td class="txtNav"><%=resource.getString("projectManager.TacheAvancementND")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"/></td>
	<td><img src="<%=resource.getIcon("projectManager.enCours")%>" border="0" align="absmiddle"/></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutEnCours")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"/></td>
	<td><img src="<%=resource.getIcon("projectManager.gelee")%>" border="0" align="absmiddle"/></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutGelee")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"/></td>
	<td><img src="<%=resource.getIcon("projectManager.realisee")%>" border="0" align="absmiddle"/></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutRealisee")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"/></td>
	<td><img src="<%=resource.getIcon("projectManager.abandonnee")%>" border="0" align="absmiddle"/></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutAbandonnee")%></td>
	<td><img src="<%=resource.getIcon("projectManager.px")%>" border="0" align="absmiddle" width="20"/></td>
	<td><img src="<%=resource.getIcon("projectManager.alerte")%>" border="0" align="absmiddle"/></td><td class="txtNav"><%=resource.getString("projectManager.TacheStatutEnAlerte")%></td>
	</tr></table>
	</center>
</view:board>
<!---------------------------------------------------------------------------------->
<!--------------------------- FILTRE (FIN) ----------------------------------------->
<!---------------------------------------------------------------------------------->
<view:areaOfOperationOfCreation/>
<br/>
<%

ArrayPane arrayPane = gef.getArrayPane("actionsList", "Main", request, session);
arrayPane.setCellsConfiguration(0, 0, 0);
ArrayColumn arrayColumn1 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheStatut"));
arrayColumn1.setAlignement("center");
ArrayColumn arrayColumn2 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheNumero"));
ArrayColumn arrayColumn3 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheNom"));
ArrayColumn arrayColumn5 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheResponsable"));
ArrayColumn arrayColumn6 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheDebut"));
ArrayColumn arrayColumn7 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheFin"));
ArrayColumn arrayColumn8 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheCharge"));
ArrayColumn arrayColumn9 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheConso"));
ArrayColumn arrayColumn10 	= arrayPane.addArrayColumn(resource.getString("projectManager.TacheReste"));
if (role.equals("admin") || role.equals("responsable"))
{
	ArrayColumn arrayColumn11 	= arrayPane.addArrayColumn(resource.getString("projectManager.Operations"));
	arrayColumn11.setSortable(false);
}

TaskDetail 	task 			= null;
TaskDetail	actionSuivante	= null;
ArrayLine 		arrayLineRoot	= null;
ArrayLine 		arrayLine 		= null;
String			nom				= null;
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
		arrayLineRoot = fillArrayLine(arrayLineRoot, task, "", userId.intValue(), role, resource, gef);
	} else {
		arrayLine = new ArrayLine(arrayPane);
		
		imgIndent = m_context+"/util/icons/colorPix/15px.gif";
		if (otherActionOnLowerLevel(tasks, task, a+1))
		{
			imgIndent = resource.getIcon("projectManager.treeI");
		}
		
		for (int i=1; i<task.getLevel(); i++)
		{
			indent += "<img src=\""+imgIndent+"\" border=\"0\" align=\"absmiddle\"/>";
		}

		ilt = indent+"<img src=\""+resource.getIcon("projectManager.treeL")+"\" border=\"0\" align=\"absmiddle\"/>";
		//il reste � savoir s'il on met un T ou un L
		//on regarde l'task suivante
		if (a+1 < tasks.size())
		{
			actionSuivante = (TaskDetail) tasks.get(a+1);
			if (actionSuivante.getLevel() == task.getLevel())
			{
				ilt = indent+"<img src=\""+resource.getIcon("projectManager.treeT")+"\" border=\"0\" align=\"absmiddle\"/>";
			}
			else {
				if (otherActionOnSameLevel(tasks, task, a+1))
				{
					ilt = indent+"<img src=\""+resource.getIcon("projectManager.treeT")+"\" border=\"0\" align=\"absmiddle\"/>";
				}
			}
		}
		
		arrayLine = fillArrayLine(arrayLine, task, ilt, userId.intValue(), role, resource, gef);
			
		arrayLineRoot.addSubline(arrayLine);
	}
}
out.println(arrayPane.print());
%>
</view:frame>
<%
out.println(window.printAfter());
%>
<form name="listForm" action="RemoveTask" method="post">
<input type="hidden" name="Id"/>
</form>
</body>
</html>