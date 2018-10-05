<%--

    Copyright (C) 2000 - 2018 Silverpeas

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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript">
function sendActionData() {
     var errorMsg 			= "";
     var errorNb 			= 0;
     var name 				= document.projectForm.Nom.value;
     var beginDate 			= document.projectForm.DateDebut.value;

     if (isWhitespace(name)) {
           errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.ProjetNom")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     if (isWhitespace(beginDate)) {
           errorMsg +="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDateDebut")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     } else {
	   if (!isDateOK(beginDate, '<%=resource.getLanguage()%>'))
	   {
				errorMsg+="  - <%=resource.getString("GML.theField")%> '<%=resource.getString("projectManager.TacheDateDebut")%>' <%=resource.getString("GML.MustContainsCorrectDate")%>\n";
		errorNb++;
		   }
     }
     switch(errorNb) {
        case 0 :
            document.projectForm.submit();
            break;
        case 1 :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
            jQuery.popup.error(errorMsg);
            break;
        default :
            errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
     }
}
</script>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");

out.println(window.printBefore());

if (project != null)
{
	TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", true);
	tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
	tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
	tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
	out.println(tabbedPane.print());
}

out.println(frame.printBefore());
%>
<form name="projectForm" action="<%=formAction%>" method="post">
<%
Board board = gef.getBoard();
out.println(board.printBefore());
%>
<table cellpadding="5">
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetChef")%> :</td>
    <td><%=orgaFullName%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetNom")%> :</td>
    <td><input type="text" name="Nom" value="<%=nom%>" size="60" maxlength="150">&nbsp;<img src="<%=resource.getIcon("projectManager.mandatoryField")%>" width="5" height="5" border="0"></td>
</tr>
<tr>
	<td class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</td>
    <td><textarea name="Description" rows="6" cols="70"><%=description%></textarea></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateDebut")%> :</td>
    <td><input type="text" name="DateDebut" size="12" maxlength="10" value="<%=dateDebut%>" class="dateToPick">&nbsp;<span class="txtnote">(<%=resource.getString("GML.dateFormatExemple")%>)</span></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateFin")%> :</td>
    <td><%=dateFin%></td>
</tr>
</table>
<%
out.println(board.printAfter());
%>
<view:fileUpload fieldset="true" jqueryFormSelector="form[name='projectForm']" />
</form>
<br/>
<%
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=sendActionData()", false));
buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "Main", false));
out.println(buttonPane.print());
%>
<br/>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>