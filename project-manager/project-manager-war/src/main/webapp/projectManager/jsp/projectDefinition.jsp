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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%
String 			orgaFullName 	= (String) request.getAttribute("Organisateur");
TaskDetail		project			= (TaskDetail) request.getAttribute("Project");

String nom 			= "";
String description 	= "";
String dateDebut 	= "";
String dateFin 		= "";
String formAction	= "CreateProject";
if (project != null)
{
	nom 			= project.getNom();
	description 	= project.getDescription();
	if (description == null || description.equals("null"))
		description = "";
	dateDebut 		= project.getUiDateDebut();
	dateFin 		= project.getUiDateFin();
	if (dateFin == null)
		dateFin = "";
	orgaFullName	= project.getOrganisateurFullName();
	formAction		= "UpdateProject";
}
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
function editDate(indiceElem)
{
		chemin = "<%=m_context%><%=URLManager.getURL(URLManager.CMP_AGENDA)%>calendar.jsp?indiceForm=0&indiceElem="+indiceElem;
		largeur = "180";
		hauteur = "200";
		SP_openWindow(chemin,"Calendrier_Todo",largeur,hauteur,"");
}
function isCorrectDate(input)
{
	var re 		= /(\d\d\/\d\d\/\d\d\d\d)/i;
    var date	= input.value;
    
    if (!isWhitespace(date)) {
           if (date.replace(re, "OK") != "OK") {
               return false;
           } else {
           		var year 	= extractYear(date, '<%=resource.getLanguage()%>'); 
    			var month	= extractMonth(date, '<%=resource.getLanguage()%>');
    			var day 	= extractDay(date, '<%=resource.getLanguage()%>');
               if (isCorrectDate(year, month, day)==false) {
                 return false;
               }
           }
     }
     return true;
}
function isCorrectForm() {

     var errorMsg 			= "";
     var errorNb 			= 0;
     var beginDateOK 		= true;
     var name 				= document.projectForm.Nom.value;
     
     var re 				= /(\d\d\/\d\d\/\d\d\d\d)/i;

     var beginDate 			= document.projectForm.DateDebut.value;
     var yearBegin 			= extractYear(beginDate, '<%=resource.getLanguage()%>'); 
     var monthBegin 		= extractMonth(beginDate, '<%=resource.getLanguage()%>');
     var dayBegin 			= extractDay(beginDate, '<%=resource.getLanguage()%>');

     if (isWhitespace(name)) {
           errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.ProjetNom")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     if (isWhitespace(beginDate)) {
           errorMsg +="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDateDebut")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     } else {
     	   if (!isCorrectDate(document.projectForm.DateDebut))
     	   {
   	   			errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDateDebut")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
               	errorNb++;
			   	beginDateOK = false;
     	   } 
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

function sendActionData() {
    if (isCorrectForm()) {
         document.projectForm.submit();
     }
}
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setExtraInformation(resource.getString("projectManager.DefinirProjet"));

out.println(window.printBefore());

if (project != null)
{
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", true);
	tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
	tabbedPane.addTab(resource.getString("GML.attachments"), "ToAttachments", false);
	tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
	tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
	tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
	out.println(tabbedPane.print());
}

out.println(frame.printBefore());

Board board = gef.getBoard();
out.println(board.printBefore());
%>
<form name="projectForm" action="<%=formAction%>" method="post">
<table cellpadding="5">
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetChef")%> :</td>
    <td><%=orgaFullName%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetNom")%> <%=resource.getString("projectManager.Action")%> :</td>
    <td><input type="text" name="Nom" value="<%=nom%>" size="60" maxlength="150">&nbsp;<img src="<%=resource.getIcon("projectManager.mandatoryField")%>" width="5" height="5" border="0"></td>
</tr>
<tr>
	<td class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</td>
    <td><textarea name="Description" rows="6" cols="70"><%=description%></textarea></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateDebut")%> :</td>
    <td><input type="text" name="DateDebut" size="12" maxlength="10" value="<%=dateDebut%>">&nbsp;<img src="<%=resource.getIcon("projectManager.mandatoryField")%>" width="5" height="5">&nbsp;&nbsp;<a href="javascript:onClick=editDate(2);"><img src="<%=resource.getIcon("projectManager.calendrier")%>"  border="0" align="top" alt="<%=resource.getString("GML.viewCalendar")%>"></a>&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateFin")%> :</td>
    <td><%=dateFin%></td>
</tr>
</table>
</form>
<%
out.println(board.printAfter());
%>
<center><br>
<%
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendActionData()", false));
buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "Main", false));
out.println(buttonPane.print());
%>
<br></center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>