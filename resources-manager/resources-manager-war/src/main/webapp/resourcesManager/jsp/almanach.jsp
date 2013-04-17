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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%@ include file="check.jsp"%>

<%
  List<Resource> listResourcesofCategory = (List) request.getAttribute("listResourcesofCategory");
  List<Category> listOfCategories = (List) request.getAttribute("listOfCategories");

  //identifiant de l'utilisateur dont on regarde le planning
  String firstNameUser = (String) request.getAttribute("firstNameUser");
  String lastName = (String) request.getAttribute("lastName");

  MonthCalendar monthC = (MonthCalendar) request.getAttribute("monthC");
  String objectView = String.valueOf(request.getAttribute("idCategory"));
  if (!StringUtil.isDefined(objectView)) {
    objectView = "myReservation";
  }

  Long idResourceFromRR = (Long) request.getAttribute("resourceId");
  String personalReservation = "myReservation";

// identifiant du role de l'utilisateur en cours
  String flag = (String) request.getAttribute("Profile");
  boolean isResponsible = (Boolean) request.getAttribute("IsResponsible");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
<script type="text/javascript" src="<c:url value="/util/javaScript/overlib.js" />"></script>
<script type="text/javascript">

function nextMonth(object)
{
    document.almanachForm.action = "NextMonth";
    <%if(idResourceFromRR != null){%>
		document.almanachForm.resourceId.value = <%=idResourceFromRR%>;
	<%}%>
    document.almanachForm.objectView.value = object;
    document.almanachForm.submit();
}

function previousMonth(object)
{
	document.almanachForm.action = "PreviousMonth";
	<%if(idResourceFromRR != null){%>
		document.almanachForm.resourceId.value = <%=idResourceFromRR%>;
	<%}%>
	document.almanachForm.objectView.value = object;
	document.almanachForm.submit();
}
function goToDay(object)
{
	document.almanachForm.action = "GoToday";
	<%if(idResourceFromRR != null){%>
		document.almanachForm.resourceId.value = <%=idResourceFromRR%>;
	<%}%>
	document.almanachForm.objectView.value = object;
	document.almanachForm.submit();
}

function getReservationsOfCategory(select){
	if (select.value.length == 0)
	{
		location.href = "Main";
	}
	else
	{
		document.almanachForm.action = "Calendar";
		document.almanachForm.objectView.value = select.value;
		document.almanachForm.submit();
	}
}

function getReservationsOfResource(select){
	if (select.value.length == 0)
	{
		getReservationsOfCategory(document.getElementById("selectCategory"));
	}
	else
	{
		document.almanachForm.action = "Calendar";
		document.almanachForm.objectView.value = document.getElementById("selectCategory").value;
		document.almanachForm.resourceId.value = select.value;
	  document.almanachForm.submit();
	}
}

function clickEvent(idEvent, date, componentId){
    viewEvent(idEvent);
}

function clickDay(day){
   location.href="NewReservation?Day="+day;
}

function viewEvent(id)
{
	<% if(objectView.equals(personalReservation) || "PlanningOtherUser".equals(objectView) ||
      "viewForValidation".equals(objectView)){%>
		location.href="ViewReservation?reservationId="+id+"&objectView=<%=objectView%>";
	<%}else {%>
		location.href="ViewResource?resourceId="+id+"&provenance=calendar";
	<%}%>
	
}
function viewOtherPlanning()
{
	SP_openWindow('ChooseOtherPlanning','ChooseOtherPlanning','750','550','scrollbars=yes, resizable, alwaysRaised');
}

</script>
</head>
<body id="resourcesManager">
<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
<%
  	browseBar.setPath(resource.getString("resourcesManager.accueil"));
	if(!"user".equals(flag)){
		operationPane.addOperationOfCreation(resource.getIcon("resourcesManager.createReservation"), resource.getString("resourcesManager.creerReservation"),"NewReservation");
		operationPane.addOperation(resource.getIcon("resourcesManager.viewMyReservations"), resource.getString("resourcesManager.Reservation"),"Calendar?objectView="+personalReservation);
		operationPane.addOperation(resource.getIcon("resourcesManager.viewUserReservation"), resource.getString("resourcesManager.viewUserReservation"), "javascript:onClick=viewOtherPlanning()");
		if(isResponsible){
			operationPane.addLine();
			operationPane.addOperation(resource.getIcon("resourcesManager.viewReservationForValidation"), resource.getString("resourcesManager.viewReservationForValidation"),"Calendar?objectView=viewForValidation");
		}
		if("admin".equals(flag)){
      		operationPane.addLine();
      		operationPane.addOperationOfCreation(resource.getIcon("resourcesManager.gererCategorie"), resource.getString("resourcesManager.gererCategorieRessource"),"ViewCategories");
    	}
	}
  	out.println(window.printBefore());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<!-- AFFICHAGE HEADER -->
<center>
  <table width="98%" border="0" cellspacing="0" cellpadding="1">
    <tr>
      <td>
        <table cellpadding="0" cellspacing="0" border="0" width="50%" bgcolor="000000">
          <tr> 
            <td>
              <table cellpadding="2" cellspacing="1" border="0" width="100%">
                  <tr> 
                    <td class="intfdcolor" align="center" nowrap="nowrap" width="100%" height="24"> 
                      <select id="selectCategory" name="selectCategory" onchange="getReservationsOfCategory(this)" class="selectNS">
                        <% if(listOfCategories != null){
                        	%><option value=""><%=resource.getString("resourcesManager.categories")%></option>
                        	<option value="">-----------------</option>
                        	<%
                            for (Category category : listOfCategories) {
                              String categoryName = category.getName();%>
                        <option value="<%=category.getId()%>" <%if((category.getId().toString()).equals(objectView)){%>selected="selected"<%}%> ><%=categoryName%>
                        </option>
                        <%
                            }
                        }%>
                      </select>
                    </td>
                  </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
	  <% if(listResourcesofCategory != null){ %>
      <td>
      	<table cellpadding="0" cellspacing="0" border="0" width="50%" bgcolor="000000">
      	  <tr>
      		<td> 
              <table cellpadding="2" cellspacing="1" border="0" width="100%">    
                  <tr> 
                   	<td class="intfdcolor" align="center" nowrap="nowrap" width="100%" height="24"> 
                      <select name="selectResource" onchange="getReservationsOfResource(this)">
                      <option value=""><%=resource.getString("resourcesManager.allResources")%></option>
                      	<option value="">-----------------</option>
                      	<% for (Resource reservation : listResourcesofCategory) {
                          String resourceName = reservation.getName();%>
                        <option value="<%=reservation.getId()%>" <%if((reservation.getId() != null) && reservation.getId().equals(idResourceFromRR)){%>selected="selected"<%}%> ><%=resourceName%>
                        </option>
                        <% } %>
                       </select>
                    </td>
                  </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
	  <% } %>
        
    <!-- affichage et traitement du bouton Aujourd hui -->  
      <td> 
        <table cellpadding=0 cellspacing=0 border=0 width=50% bgcolor=000000>
          <tr> 
            <td> 
              <table cellpadding=2 cellspacing=1 border=0 width="100%" >
                <tr> 
                  <td class=intfdcolor align=center nowrap="nowrap" width="100%" height="24"><a href="javascript:onClick=goToDay('<%=objectView%>')" onfocus="this.blur()" class=hrefComponentName><%=resource.getString("resourcesManager.auJour")%></a></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
      
      <!-- affichage et traitement des boutons < et > -->
      <td width="100%"> 
        <table cellpadding=0 cellspacing=0 border=0 width=50% bgcolor=000000>
          <tr> 
            <td> 
              <table cellpadding=0 cellspacing=1 border=0 width="100%" >
                <tr> 
                  <td class=intfdcolor><a href="javascript:onClick=previousMonth('<%=objectView%>')" onfocus="this.blur()"><img src="<%=resource.getIcon("resourcesManager.arrLeft")%>" border="0" alt=""/></a></td>
                  <td class=intfdcolor align=center nowrap="nowrap" width="100%" height="24"><span class="txtnav"><%=resource.getString("GML.mois" + resourcesManagerSC.getCurrentDay().get(Calendar.MONTH))%> <%=String.valueOf(resourcesManagerSC.getCurrentDay().get(Calendar.YEAR))%></span></td>
                  <td class=intfdcolor><a href="javascript:onClick=nextMonth('<%=objectView%>')" onfocus="this.blur()"><img src="<%=resource.getIcon("resourcesManager.arrRight")%>" border="0" alt=""/></a></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
      <%if((firstNameUser != null) || (lastName != null)){%>
	      <td> 
	        <table cellpadding=0 cellspacing=0 border=0 width=50%>
	          <tr> 
	            <td> 
	              <table cellpadding=2 cellspacing=1 border=0 width="100%" >
	                <tr> 
	                  <td align=center nowrap="nowrap" width="100%" height="24"><%=resource.getString("resourcesManager.planningFrom") + " " + firstNameUser + " " + lastName %></td>
	                </tr>
	              </table>
	            </td>
	          </tr>
	        </table>
	      </td>
	<%} %>
      
    </tr>
  </table>
  <br/>
<%=monthC.print()%>

</center>
</view:frame>
<%		
	out.println(window.printAfter());
%>
<form name="almanachForm" action="" method="post">
  <input type="hidden" name="objectView" value=""/>
  <input type="hidden" name="resourceId" value=""/>
  <input type="hidden" name="idUser" value=""/>
  <input type="hidden" name="firstNameUser" value=""/>
  <input type="hidden" name="lastName" value=""/>
</form>
</body>
</html>