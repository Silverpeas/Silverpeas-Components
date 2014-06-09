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
<%@page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="org.silverpeas.attachment.model.SimpleDocument"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
TaskDetail	project = (TaskDetail) request.getAttribute("Project");
List<SimpleDocument> attachments = (List<SimpleDocument>) request.getAttribute("Attachments");

String nom 			= project.getNom();
String description 	= project.getDescription();
if (description == null || description.equals("null")) {
	description = "";
}
String dateDebut 	= resource.getOutputDate(project.getDateDebut());
String dateFin 		= resource.getOutputDate(project.getDateFin());
String orgaFullName	= project.getOrganisateurFullName();
%>

<html>
<head>
<view:looknfeel/>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setExtraInformation(resource.getString("projectManager.DefinirProjet"));

out.println(window.printBefore());

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("projectManager.Projet"), "ToProject", true);
tabbedPane.addTab(resource.getString("projectManager.Taches"), "Main", false);
tabbedPane.addTab(resource.getString("projectManager.Commentaires"), "ToComments", false);
tabbedPane.addTab(resource.getString("projectManager.Gantt"), "ToGantt", false);
out.println(tabbedPane.print());

out.println(frame.printBefore());
%>
<center>
<table border="0" cellspacing="5"><tr><td width="100%" valign="top">
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
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetNom")%> <%=resource.getString("projectManager.Action")%> :</td>
    <td><%=nom%></td>
</tr>
<tr>
	<td class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</td>
  <td><%=EncodeHelper.javaStringToHtmlParagraphe(description)%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateDebut")%> :</td>
    <td><%=dateDebut%></td>
</tr>
<tr>
	<td class="txtlibform"><%=resource.getString("projectManager.ProjetDateFin")%> :</td>
    <td><%=dateFin%></td>
</tr>
</table>
<%
out.println(board.printAfter());
%>
</td>
<td valign="top">
	<% 
	  	if (attachments != null && ! attachments.isEmpty()) {
	  		out.println(board.printBefore());
	  		out.println("<table width=\"200\">");
	      out.println("<tr><td align=\"center\"><img src=\""+m_context+"/util/icons/attachedFiles.gif\"></td></tr>");	      
        for(SimpleDocument attachmentDetail : attachments) {
            String title = attachmentDetail.getTitle();
            String info = attachmentDetail.getDescription();
            String author = "";
            if (attachmentDetail.getAuthor() != null && attachmentDetail.getAuthor().length() > 0) {
              author = "<br/><i>" + attachmentDetail.getAuthor() + "</i>";
            }
            out.println("<tr>");
            out.println("<td><img alt=\"\" src=\"" + FileRepositoryManager.
                getFileIcon(attachmentDetail.getFilename()) + "\" width=20>&nbsp;");
            out.println("<a href=\"" + m_context + attachmentDetail.getAttachmentURL()
                + "\" target=\"_blank\">");
            if (StringUtil.isDefined(title)) {
              out.println(title);
            } else {
              out.println(attachmentDetail.getFilename());
            }
            out.println("</a> " + author + "<br/>");
            if (title != null && title.length() > 0) {
              out.println(attachmentDetail.getFilename());
              out.println(" / ");
            }
            out.println(FileRepositoryManager.formatFileSize(attachmentDetail.getSize()) + " / "
                + FileRepositoryManager.getFileDownloadTime(attachmentDetail.getSize()) + "<br/>");
            if (info != null && info.length() > 0) {
              out.println("<i>" + info + "</i>");
            }
            out.println("</td></tr>");
            out.println("<tr><td>&nbsp;</td></tr>");
            author = "";
          }
          out.println("</table>");
          out.println(board.printAfter());
	    }
	%>
</td>
</tr></table>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>