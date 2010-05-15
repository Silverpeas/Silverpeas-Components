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
TaskDetail	project			= (TaskDetail) request.getAttribute("Project");
Vector		vAttachments	= (Vector) request.getAttribute("Attachments");
Iterator	attachments		= vAttachments.iterator();

String nom 			= project.getNom();
String description 	= project.getDescription();
if (description == null || description.equals("null"))
	description = "";
String dateDebut 	= resource.getOutputDate(project.getDateDebut());
String dateFin 		= resource.getOutputDate(project.getDateFin());
String orgaFullName	= project.getOrganisateurFullName();
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
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
<table CELLPADDING=5>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.ProjetChef")%> :</TD>
    <TD><%=orgaFullName%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.ProjetNom")%> <%=resource.getString("projectManager.Action")%> :</TD>
    <TD><%=nom%></TD>
</TR>
<TR>
	<TD class="txtlibform" valign="top"><%=resource.getString("projectManager.TacheDescription")%> :</TD>
    <TD><%=Encode.javaStringToHtmlParagraphe(description)%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.ProjetDateDebut")%> :</TD>
    <TD><%=dateDebut%></TD>
</TR>
<TR>
	<TD class="txtlibform"><%=resource.getString("projectManager.ProjetDateFin")%> :</TD>
    <TD><%=dateFin%></TD>
</TR>
</table>
<%
out.println(board.printAfter());
%>
</td>
<td valign="top">
	<% 
	  	if (attachments.hasNext())
	  	{
	  		out.println(board.printBefore());
	  		out.println("<TABLE width=\"200\">");
	        out.println("<TR><TD align=\"center\"><img src=\""+m_context+"/util/icons/attachedFiles.gif\"></td></TR>");
	        
	        AttachmentDetail attachmentDetail = null;
	        String author 	= "";
	        String title	= "";
	        String info		= "";
	        while (attachments.hasNext()) {
	            attachmentDetail = (AttachmentDetail) attachments.next();
				title	= attachmentDetail.getTitle();
				info	= attachmentDetail.getInfo();
				if (attachmentDetail.getAuthor() != null && attachmentDetail.getAuthor().length() > 0) {
					author = "<BR><i>"+attachmentDetail.getAuthor()+"</i>";
				}
                out.println("<TR>");
                out.println("<TD><IMG alt=\"\" src=\""+attachmentDetail.getAttachmentIcon()+"\" width=20>&nbsp;");
				out.println("<A href=\""+m_context+attachmentDetail.getAttachmentURL()+"\" target=\"_blank\">");
				if (title != null && title.length()>0)
					out.println(title);
				else
					out.println(attachmentDetail.getLogicalName());
				out.println("</A> "+author+"<BR>");
				if (title != null && title.length()>0) {
					out.println(attachmentDetail.getLogicalName());
					out.println(" / ");
				}
                out.println(attachmentDetail.getAttachmentFileSize()+" / "+attachmentDetail.getAttachmentDownloadEstimation()+"<br>");
				if (info != null && info.length()>0)
					out.println("<i>"+info+"</i>");
				out.println("</TD></TR>");
				out.println("<TR><TD>&nbsp;</TD></TR>");
				author = "";
	        }
	        out.println("</TABLE>");
	        out.println(board.printAfter());
	    }
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