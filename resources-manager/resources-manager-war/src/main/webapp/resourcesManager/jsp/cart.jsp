<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.resourcemanager.model.Category"%>
<%@ page import="org.silverpeas.resourcemanager.model.Resource"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceReservableDetail"%>
<%@ page import="org.silverpeas.resourcemanager.model.Reservation"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>
<% 
//R�cup�ration des d�tails de l'ulisateur
List 			list 						= (List) request.getAttribute("listResourcesReservable");
int 				nbCategories 				= ((Integer)request.getAttribute("nbCategories")).intValue();
Reservation reservation 				= (Reservation) request.getAttribute("reservation");
List 			listResourcesProblem 		= (List) request.getAttribute("listResourcesProblem");
List 			listResourceEverReserved 	= (List) request.getAttribute("listResourceEverReserved");
String 				idModifiedReservation 		= (String)request.getAttribute("idReservation");

String evenement = reservation.getEvent();
String raison = EncodeHelper.javaStringToHtmlParagraphe(reservation.getReason());
String lieu = reservation.getPlace();

// boutons de validation du formulaire
Board	board		 = gef.getBoard();
ButtonPane buttonPane = gef.getButtonPane();
Button validateButton = gef.getFormButton(resource.getString("GML.validate"), "javaScript:verification()", false);
Button cancelButton = gef.getFormButton(resource.getString("GML.cancel"), "Main",false);
buttonPane.addButton(validateButton);
buttonPane.addButton(cancelButton);

//String qui permet de recuperer la liste des ids des ressources reservees
boolean noResource = true;

// Permet de recuperer l'id de la categorie courante
String idTemoin="";
%>
<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	
	<script language=JavaScript>
	
	function ajouterRessource(resourceId, categoryId) {
		var elementResource = document.getElementById(resourceId);
		var elementlisteReservation = document.getElementById("listeReservation");
		var theImage = "image"+resourceId ;
		document.images[theImage].src = "<%=m_context%>/util/icons/delete.gif";
		
		elementlisteReservation.appendChild(elementResource);
	}
	
	function enleverRessource(resourceId, categoryId) {
		var elementResource = document.getElementById(resourceId);
		var elementCategory = document.getElementById(categoryId);
		var theImage = "image"+resourceId ;
		document.images[theImage].src = "<%=m_context%>/util/icons/ok.gif";		

		elementCategory.appendChild(elementResource);
	}
	
	function switchResource(resourceId, categoryId)
	{
		if (isResourceReservee(resourceId))
		{
			clearCategory(categoryId);			
			enleverRessource(resourceId, categoryId);			
		}
		else
		{
			ajouterRessource(resourceId, categoryId);			
			if (isCategoryEmpty(categoryId))
				{
          addEmptyResource(categoryId);
        }
		}
	}
	
	function addEmptyResource(categoryId)
	{
		var emptyElement = document.createElement("div");
		emptyElement.id = "-1";
		emptyElement.innerHTML = "<span class=\"noRessource\"><center><%=resource.getString("resourcesManager.noResource")%></center></span>";
		var elementCategory = document.getElementById(categoryId);
		elementCategory.appendChild(emptyElement);
	}
			
	
	function clearCategory(categoryId)
	{
		var category = document.getElementById(categoryId);
		var resources = category.childNodes;
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV' && resources[r].id == "-1")
				{
          category.removeChild(resources[r]);
        }
		}
	}
	
	function isCategoryEmpty(categoryId)
	{
		var category = document.getElementById(categoryId);
		var resources = category.childNodes;
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV')
				{
          return false;
        }
		}
		return true;
	}
	
	function isResourceReservee(resourceId)
	{
		var listeReservation = document.getElementById("listeReservation");
		var resources = listeReservation.childNodes;
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV' && resources[r].id == resourceId)
				{
          return true;
        }
		}
		return false;
	}
	
	function getResourcesReservees()
	{
		var listeReservation = document.getElementById("listeReservation");
		var resources = listeReservation.childNodes;
		var resourceIds = "";
		for (var r=0; r<resources.length; r++)
		{
			if (resources[r].nodeName == 'DIV')
				{
          resourceIds += resources[r].id + ",";
        }
		}
		resourceIds = resourceIds.substring(0, resourceIds.length-1);
		return resourceIds;
	}
	
	function verification(){
		document.frmResa.listeResa.value = getResourcesReservees();
		document.frmResa.submit();
	}
	function retour() {
		window.history.back();
		}

	$(document).ready(function(){
		$('#accordion').accordion();
	});

	</script>
 	</head>
	<body>
	<%
	browseBar.setPath("<a href=\"javascript:retour()\">"+resource.getString("resourcesManager.reservationParametre")+"</a>");
	browseBar.setExtraInformation(resource.getString("resourcesManager.resourceSelection"));

		out.println(window.printBefore());
		out.println(tabbedPane.print());
		out.println(frame.printBefore());
		
		if(listResourcesProblem != null)
		{
			out.println(board.printBefore());
			out.println("<h4>"+resource.getString("resourcesManager.resourceUnReservable")+"</h4>");
			for(int i=0;i<listResourcesProblem.size();i++)
			{ 
				Resource resourceProblem = (Resource)listResourcesProblem.get(i);
				out.println(resource.getString("resourcesManager.ressourceNom")+" : "+resourceProblem.getName()+"<br/>");
			}
			out.println(board.printAfter());
			out.println("<br />");
		}
		
		out.println(board.printBefore());
	%>
		 
<TABLE ALIGN="CENTER" CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> :</td>
		<td width="100%"><%=evenement%></td>
	</tr>
		
	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateBegin"));%> :</TD>
		<td><%=resource.getOutputDateAndHour(reservation.getBeginDate())%></td>
	</tr>

	<tr>
	<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateEnd"));%> :</td> 
		<td><%=resource.getOutputDateAndHour(reservation.getEndDate())%></td>	
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.raisonReservation"));%> :</td> 
		<td><%=raison%></TD>
	</tr>

	<tr>
		<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> :</td>
		<td><%=lieu%></TD>
	</tr>
</TABLE>
<%out.println(board.printAfter());%>		
<br />

		  <table width="100%" align="center" border="0" cellspacing="5">
		  <tr>
		  <td width="50%" valign="top">
		  <div class="titrePanier"><center><%=resource.getString("resourcesManager.clickReservation")%></center></div>
		  <div id="accordion">
			<%
			for (int r=0; r<list.size(); r++)
			{
				ResourceReservableDetail maResource = (ResourceReservableDetail)list.get(r);
				String resourceId = maResource.getResourceId();
				if(!idTemoin.equals(maResource.getCategoryId()))
				{
					if (r != 0)
					{
						if (noResource)
						{%>
							<div id="-1" class="noRessource">
								<center><%=resource.getString("resourcesManager.noResource")%></center>
							</div>
						<%
						}
						out.println("</div>");
						noResource = true;
					}
					%>
					<h3><a href="#"><%=maResource.getCategoryName()%></a></h3>
						<div id="categ<%=maResource.getCategoryId()%>">
					<%
				}
				if(!"0".equals(resourceId)) {
					
					//on entre dans ce if, donc il y a une ressource au moins disponible dans la category, donc on ne veut pas 
					// afficher "pas de ressource disponible dans cette categorie"
					noResource = false;%>
					<div id="<%=resourceId%>" onClick="switchResource(<%=resourceId%>,'categ<%=maResource.getCategoryId()%>');" style="cursor: pointer;">
 						<table width="100%" cellspacing="0" cellpadding="0" border="0">
 							<tr>
 								<td width="80%" nowrap>&nbsp;-&nbsp;<%=maResource.getResourceName()%></td>
 								<td><img src="<%=m_context %>/util/icons/ok.gif" id="image<%=resourceId%>" align="middle"/></td>
 							</tr>
 						</table>
					</div>
				<%}
				idTemoin = maResource.getCategoryId();
			}
			if (noResource)
			{%>
				<div id="-1" class="noRessource">			
					<%=resource.getString("resourcesManager.noResource")%>
				</div>
			<%}			
			%> 
			  </div>
		  </div>
		  </td>
		  <td valign="top" width="50%">
		  	  <div class="titrePanier"><% out.println(resource.getString("resourcesManager.resourcesReserved"));%></div>
		      <div id="listeReservation">
		      <%if (listResourceEverReserved != null){ 
		    	  
		  			// la suppression ayant ete faite, cette boucle permet d'afficher les resources qui n'ont pas pose probleme
		  			for (int i=0;i<listResourceEverReserved.size();i++){
		  						Resource maRessource =(Resource)listResourceEverReserved.get(i);
		  		  				String NomResource = maRessource.getName();
		  		  				String resourceId = maRessource.getId();
		  		  				String categoryId = maRessource.getCategoryId();
		  		  			%>
			  					<div id="<%=resourceId%>" onClick="switchResource(<%=resourceId%>,'categ<%=categoryId%>');" style="cursor: pointer;">
			  						<table width="100%" cellspacing="0" cellpadding="0" border="0">
			  							<tr>
			  								<td width="80%" nowrap>&nbsp;-&nbsp;<%=NomResource%> </td>
			  								<td><img src="<%=m_context %>/util/icons/delete.gif" id="image<%=resourceId%>" align="middle"/></td>
			  							</tr>
			  						</table>
			  					</div>
			  				<%
		  				}
		  			}
		  			%>
		      </div>
	</td></tr></table>
	<%
    out.println("<br/><center>"+buttonPane.print()+"</center><br/>");
	out.println(frame.printAfter());
	out.println(window.printAfter());		
	%>
<form name="frmResa" method="post" action="FinalReservation">
	<input type="hidden" name="listeResa" value="">
	<input type="hidden" name="newResourceReservation" value="">
	<%if(idModifiedReservation != null){ %>	
		<input type="hidden" name="idModifiedReservation" value="<%=idModifiedReservation%>">
	<%}%>
</form>	
</body>
</html>