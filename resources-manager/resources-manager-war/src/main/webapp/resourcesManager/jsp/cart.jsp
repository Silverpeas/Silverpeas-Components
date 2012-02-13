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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.resourcemanager.model.Category"%>
<%@ page import="org.silverpeas.resourcemanager.model.Resource"%>
<%@ page import="org.silverpeas.resourcemanager.model.Reservation"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<%
Map<String, List<Resource>> resourcesReservables = (Map<String, List<Resource>> ) request.getAttribute("mapResourcesReservable");
Reservation reservation = (Reservation) request.getAttribute("reservation");
List<Resource> listResourcesProblem = (List<Resource>) request.getAttribute("listResourcesProblem");
List<Resource> listResourceEverReserved = (List<Resource>) request.getAttribute("listResourceEverReserved");
String idModifiedReservation = (String)request.getAttribute("idReservation");

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
    <view:looknfeel />
    <script language=JavaScript>
	
      function ajouterRessource(resourceId, categoryId) {
        var elementResource = document.getElementById(resourceId);
        var elementlisteReservation = document.getElementById("listeReservation");
        var theImage = "image"+resourceId ;
        document.images[theImage].src = "<c:url value="/util/icons/delete.gif" />";
        elementlisteReservation.appendChild(elementResource);
      }
	
      function enleverRessource(resourceId, categoryId) {
        var elementResource = document.getElementById(resourceId);
        var elementCategory = document.getElementById(categoryId);
        var theImage = "image"+resourceId ;
        document.images[theImage].src = "<c:url value="/util/icons/ok.gif" />";		

        elementCategory.appendChild(elementResource);
      }
	
      function switchResource(resourceId, categoryId) {
        if (isResourceReservee(resourceId)) {
          clearCategory(categoryId);			
          enleverRessource(resourceId, categoryId);			
        }
        else {
          ajouterRessource(resourceId, categoryId);			
          if (isCategoryEmpty(categoryId)) {
            addEmptyResource(categoryId);
          }
        }
      }
	
      function addEmptyResource(categoryId)
      {
        var emptyElement = document.createElement("div");
        emptyElement.id = "-1";
        emptyElement.innerHTML = "<span class=\"noRessource\"><center><fmt:message key="resourcesManager.noResource" /></center></span>";
        var elementCategory = document.getElementById(categoryId);
        elementCategory.appendChild(emptyElement);
      }
			
	
      function clearCategory(categoryId)
      {
        var category = document.getElementById(categoryId);
        var resources = category.childNodes;
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV' && resources[r].id == "-1") {
            category.removeChild(resources[r]);
          }
        }
      }
	
      function isCategoryEmpty(categoryId) {
        var category = document.getElementById(categoryId);
        var resources = category.childNodes;
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV') {
            return false;
          }
        }
        return true;
      }
	
      function isResourceReservee(resourceId)
      {
        var listeReservation = document.getElementById("listeReservation");
        var resources = listeReservation.childNodes;
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV' && resources[r].id == resourceId) {
            return true;
          }
        }
        return false;
      }
	
      function getResourcesReservees() {
        var listeReservation = document.getElementById("listeReservation");
        var resources = listeReservation.childNodes;
        var resourceIds = "";
        for (var r=0; r<resources.length; r++) {
          if (resources[r].nodeName == 'DIV') {
            resourceIds += resources[r].id + ",";
          }
        }
        resourceIds = resourceIds.substring(0, resourceIds.length-1);
        return resourceIds;
      }
	
      function verification(){
        document.frmResa.listeResa.value = getResourcesReservees();
        if(getResourcesReservees() == "") {
          $( "#dialog-message" ).dialog( "open" );
        } else {
          document.frmResa.submit();
        }
      }
    
      function retour() {
        window.history.back();
      }
    
      $(function() {
        $('#accordion').accordion();
        $("#dialog-message" ).dialog({
          modal: true,
          autoOpen: false,
          width: 350,
          resizable: false,
          buttons: {
            Ok: function() {
              $( this ).dialog( "close" );
            }
          }
        });
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
		
        if(listResourcesProblem != null) {
            out.println(board.printBefore());
            out.println("<h4>"+resource.getString("resourcesManager.resourceUnReservable")+"</h4>");
            for(Resource resourceProblem : listResourcesProblem) { 
                out.println(resource.getString("resourcesManager.ressourceNom")+" : "+resourceProblem.getName()+"<br/>");
            }
            out.println(board.printAfter());
            out.println("<br />");
        }
    %>
    <view:board>

      <table ALIGN="CENTER" CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> :</td>
          <td width="100%"><%=evenement%></td>
        </tr>

        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateBegin"));%> :</TD>
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
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> :</td>
          <td><%=lieu%></TD>
        </tr>
      </table></view:board>
      <br />

      <table width="100%" align="center" border="0" cellspacing="5">
        <tr>
          <td width="50%" valign="top">
            <div class="titrePanier"><center><fmt:message key="resourcesManager.clickReservation" /></center></div>
            <div id="accordion">
              <c:forEach items="${requestScope.categories}" var="category">
                <h3><a href="#"><c:out value ="${category.name}" /></a></h3>
              <div id="categ<c:out value ="${category.id}" />">
                <c:choose>
                  <c:when test="${requestScope.mapResourcesReservable[category.id] != null}">
                    <c:forEach items="${requestScope.mapResourcesReservable[category.id]}" var="maResource">
                      <div id="<c:out value ="${maResource.id}" />" onClick="switchResource(<c:out value ="${maResource.id}" />,'categ<c:out value ="${category.id}" />');" style="cursor: pointer;">
                      <table width="100%" cellspacing="0" cellpadding="0" border="0">
                        <tr>
                          <td width="80%" nowrap>&nbsp;-&nbsp;<c:out value ="${maResource.name}" /></td>
                          <td><img src="<c:url value="/util/icons/ok.gif" />" id="image<c:out value ="${maResource.id}" />" align="middle"/></td>
                        </tr>
                      </table>
                    </div>
                    </c:forEach>
                  </c:when>
                  <c:otherwise>
                    <div id="-1" class="noRessource">			
                      <fmt:message key="resourcesManager.noResource" />
                    </div>
                  </c:otherwise>
                </c:choose>
              </div>
              </c:forEach>
            </div>
        </td>
        <td valign="top" width="50%">
          <div class="titrePanier"><% out.println(resource.getString("resourcesManager.resourcesReserved"));%></div>
          <div id="listeReservation">
            <%if (listResourceEverReserved != null){ 
		    	  
                  // la suppression ayant ete faite, cette boucle permet d'afficher les resources qui n'ont pas pose probleme
                  for (Resource maRessourceAlreadyReserved : listResourceEverReserved) {
                              String NomResource = maRessourceAlreadyReserved.getName();
                              String resourceId = maRessourceAlreadyReserved.getId();
                              String categoryId = maRessourceAlreadyReserved.getCategoryId();
            %>
            <div id="<%=resourceId%>" onClick="switchResource(<%=resourceId%>,'categ<%=categoryId%>');" style="cursor: pointer;">
              <table width="100%" cellspacing="0" cellpadding="0" border="0">
                <tr>
                  <td width="80%" nowrap>&nbsp;-&nbsp;<%=NomResource%> </td>
                  <td><img src="<c:url value="/util/icons/delete.gif" />" id="image<%=resourceId%>" align="middle"/></td>
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
    <div id="dialog-message" title="<fmt:message key="resourcesManager.form.validation.error.title" />">
      <fmt:message key="resourcesManager.noReservedResource" />
    </div>
  </body>
</html>