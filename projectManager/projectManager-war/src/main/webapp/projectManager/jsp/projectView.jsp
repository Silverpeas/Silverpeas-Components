<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ include file="check.jsp" %>

<%
TaskDetail	project = (TaskDetail) request.getAttribute("Project");
String 	role 		= (String) request.getAttribute("Role");

String nom 			= project.getNom();
String description 	= project.getDescription();
String dateDebut 	= resource.getOutputDate(project.getDateDebut());
String dateFin 		= resource.getOutputDate(project.getDateFin());
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
</head>
<body>
<%
if (role.equals("admin")) {
  operationPane.addOperation(null, resource.getString("GML.update"), "ToUpdateProject");
}

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", true);
tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
if ("admin".equals(role)) {
  tabbedPane.addTab(resource.getString("projectManager.Calendrier"), "ToCalendar", false);
}
out.println(tabbedPane.print());
%>

<div class="rightContent">
  <viewTags:displayAttachments componentInstanceId="<%=componentId%>"
                               resourceId="<%=String.valueOf(project.getId())%>"
                               resourceType="<%=project.getContributionType()%>"
                               highestUserRole="<%=SilverpeasRole.fromString(role)%>"/>
</div>

<div class="principalContent">
<table cellpadding="5">
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetChef")%> :</td>
    <td><view:username userId="<%=String.valueOf(project.getOrganisateurId())%>" /></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetNom")%> :</td>
    <td><%=nom%></td>
</tr>
  <% if (StringUtil.isDefined(description)) { %>
<tr>
	<td class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</td>
  <td><%=WebEncodeHelper.javaStringToHtmlParagraphe(description)%></td>
</tr>
  <% } %>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateDebut")%> :</td>
    <td><%=dateDebut%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateFin")%> :</td>
    <td><%=dateFin%></td>
</tr>
</table>
  <viewTags:displayComments componentId="<%=componentId%>"
                            resourceType="ProjectManager"
                            resourceId="-1"
                            indexed="true"/>
</div>

<%
out.println(window.printAfter());
%>
</body>
</html>
