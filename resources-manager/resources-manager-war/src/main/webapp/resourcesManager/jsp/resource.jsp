<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.resourcemanager.model.Category"%>
<%@ page import="org.silverpeas.resourcemanager.model.Resource"%>
<%@ page import="java.util.List" %>

<%@taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="check.jsp" %>
<% 
// Recuperation des details de l'ulisateur
	Category category = (Category)request.getAttribute("category");
	Long idcategory = category.getId();
	Resource maResource = (Resource)request.getAttribute("resource");
	String provenance = (String)request.getAttribute("provenance");
	// recuperation des parametres du formulaire
	Form xmlForm = (Form) request.getAttribute("XMLForm");
	DataRecord	xmlData = (DataRecord) request.getAttribute("XMLData");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	Boolean showComments = (Boolean) request.getAttribute("ShowComments");
	List<UserDetail> managers  = (List<UserDetail>) request.getAttribute("Managers");
	String objectView = request.getParameter("objectView");
	
	String name=maResource.getName();
	String description=EncodeHelper.javaStringToHtmlParagraphe(maResource.getDescription());
	boolean bookable=maResource.isBookable();
	Long resourceId=maResource.getId();
	Button cancelButton = null;
	
//creation des boutons Annuler
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"Main");
	if("resources".equals(provenance)){
		// on vient de resources
		cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeResource"), "ViewResources?id="+idcategory+"&objectView="+objectView,false);
		String chemin = "<a href=\"ViewCategories\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.listCategorie"))+"</a>";
		String chemin2 ="<a href=\"ViewResources?id="+ idcategory + "\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.categorie"))+"</a>";
		chemin = chemin + " > " + chemin2;
		browseBar.setPath(chemin);
	}
	else if ("calendar".equals(provenance)){
		// on vient de l'almanach
		cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeReservation"), "Calendar?objectView="+idcategory+"&resourceId="+resourceId,false);
		
	}
	else if ("reservation".equals(provenance)){
		// on vient du recapitulatif de la reservation
		cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourReservation"), "ViewReservation?objectView="+objectView,false);
		String chemin ="<a href=\"ViewReservation\">" + EncodeHelper.javaStringToHtmlString(resource.getString("resourcesManager.recapitulatifReservation"))+"</a>";
		browseBar.setPath(chemin);
	}
	browseBar.setExtraInformation(resource.getString("resourcesManager.informationResource") + " " + name);
	%>
<html>
<head>
<view:looknfeel/>
</head>
<body>
<%
Board board = gef.getBoard();

out.println(window.printBefore());

if (showComments)
{
	tabbedPane.addTab(resource.getString("resourcesManager.resource"), "#", true);
	tabbedPane.addTab(resource.getString("resourcesManager.commentaires"), "Comments?resourceId="+resourceId+"&provenance="+provenance, false);
	out.println(tabbedPane.print());
}

out.println(frame.printBefore());
out.println(board.printBefore());

ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(cancelButton);

%>
	<TABLE width="100%" cellpadding="3" border="0">
		<tr>
			<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.nomcategorie"));%> :</td>	
			<td width="100%"><%=category.getName()%></TD>				
		</tr>
		
		<tr>
			<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.name"));%> :</TD>
			<td><%=name%></td>
		</tr>

		<tr>
			<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.description"));%> :</td> 
			<td><%=description%></TD>
		</tr>

		<tr>
			<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.reservable"));%> :</td> 
			<td> 
			<%if(bookable)
					out.println(resource.getString("resourcesManager.ressourcereservable"));
				else 
					out.println(resource.getString("resourcesManager.ressourceireservable"));
				%>
			</td>	
		</tr>

		<tr>
			<TD class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.creationDate")%> :</TD>
			<td><%=resource.getOutputDateAndHour(maResource.getCreationDate())%></td>
		</tr>

		<% if (!maResource.getCreationDate().equals(maResource.getUpdateDate())) { %>
		<tr>
			<TD class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.updateDate")%> :</TD>
			<td><%=resource.getOutputDateAndHour(maResource.getUpdateDate())%></td>
		</tr>
		<% } %>
		<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.responsable"));%> : </TD>
     
     <TD id="managers"> 
      <%        
        if (managers != null  && !managers.isEmpty()) {
          for(UserDetail manager : managers){ %>
          	<view:username userId="<%=manager.getId()%>"/>
            <br/>
          <% }
        } %>
      </TD>
      </tr>
		<input type="hidden" name="resourceId" value="<%=resourceId%>"/>
	</TABLE>
	<%out.println(board.printAfter()); %>
	
	<br/>
<!-- AFFICHAGE du formulaire -->
<%
  if (xmlForm != null) {
    out.println(board.printBefore());
    xmlForm.display(out, context, xmlData);
    out.println(board.printAfter());
  }
  out.println("<BR><center>" + buttonPane.print() + "</center><BR>");
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>