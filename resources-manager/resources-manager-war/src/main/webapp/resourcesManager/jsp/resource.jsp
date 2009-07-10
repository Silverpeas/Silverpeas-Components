<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="java.util.List" %>

<%@ include file="check.jsp" %>
<% 
// Récupération des détails de l'ulisateur
	CategoryDetail category = (CategoryDetail)request.getAttribute("category");
	String idcategory = category.getId();
	ResourceDetail maResource = (ResourceDetail)request.getAttribute("resource");
	String provenance = (String)request.getAttribute("provenance");
	String flag = (String)request.getAttribute("Profile");
//	 récupération des paramètres du formulaire
	Form xmlForm = (Form) request.getAttribute("XMLForm");
	DataRecord	xmlData = (DataRecord) request.getAttribute("XMLData");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	Boolean showComments = (Boolean) request.getAttribute("ShowComments");
	
	String name=maResource.getName();
	String responsibleId=maResource.getResponsibleId();
	String description=Encode.javaStringToHtmlParagraphe(maResource.getDescription());
	boolean bookable=maResource.getBookable();
	String resourceId=maResource.getId();
	Button cancelButton = null;
	
//creation des boutons Annuler
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel,"Main");
	if(provenance.equals("resources")){
		// on vient de resources
		cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeResource"), "ViewResources?id="+idcategory,false);
		String chemin = "<a href=\"ViewCategories\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.listCategorie"))+"</a>";
		String chemin2 ="<a href=\"ViewResources?id="+ idcategory + "\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.categorie"))+"</a>";
		chemin = chemin + " > " + chemin2;
		browseBar.setPath(chemin);
	}
	else if (provenance.equals("calendar")){
		// on vient de l'almanach
		cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeReservation"), "Calendar?objectView="+idcategory+"&resourceId="+resourceId,false);
		
	}
	else if (provenance.equals("reservation")){
		// on vient du récapitulatif de la réservation
		cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourReservation"), "ViewReservation",false);
		String chemin ="<a href=\"ViewReservation\">" + Encode.javaStringToHtmlString(resource.getString("resourcesManager.recapitulatifReservation"))+"</a>";
		browseBar.setPath(chemin);
	}
	browseBar.setExtraInformation(resource.getString("resourcesManager.informationResource") + " " + name);
	%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
Board board = gef.getBoard();

out.println(window.printBefore());

if (showComments.booleanValue())
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
			<%if(bookable == true) 
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
		
		<!--<tr>
			<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.responsable"));%> :</td>
			<td>
				<% if ((responsibleId != null) && (responsibleId.equals("0")))
						out.println(resource.getString("resourcesManager.ressourceresponsable"));
					else
						out.println(responsibleId);
				%>
			</td>
		</tr>-->
		<input type="HIDDEN" name="resourceId" value=<%=resourceId%> >
	</TABLE>
	<%out.println(board.printAfter()); %>
	
	<br/>
	<!-- AFFICHAGE du formulaire -->
	  <%if (xmlForm != null){
 			out.println(board.printBefore());	         
         	xmlForm.display(out, context, xmlData);		
			out.println(board.printAfter());
	  }
out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>