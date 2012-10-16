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
<%@ page import="org.silverpeas.resourcemanager.model.Reservation"%>
<%@ page import="java.util.List" %>
<%@ page import="org.silverpeas.resourcemanager.model.ResourceStatus" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<% 
//Recuperation des details de l'ulisateur
List<Resource> listResourcesofReservation = (List<Resource>)request.getAttribute("listResourcesofReservation");
String reservationId = (String)request.getAttribute("reservationId");
Reservation maReservation = (Reservation)request.getAttribute("reservation");
String objectView = (String) request.getAttribute("objectView");
String event = maReservation.getEvent();
String place = maReservation.getPlace();
String reason = EncodeHelper.javaStringToHtmlParagraphe(maReservation.getReason());
String dateEnd = resource.getOutputDateAndHour(maReservation.getEndDate());
String dateBegin = resource.getOutputDateAndHour(maReservation.getBeginDate());
String flag = (String)request.getAttribute("Profile");

Board	board		 = gef.getBoard();
Button cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeReservation"), "Calendar?objectView="+objectView,false);
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(cancelButton);

boolean isOwner = "admin".equals(flag) || maReservation.getUserId().equals(resourcesManagerSC.getUserId());
String profileForAttachments = "admin";
if (!isOwner) {
  profileForAttachments = "user";
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">
function deleteReservation(){
	if(confirm("<%=resource.getString("resourcesManager.suppressionConfirmation")%>")){
		location.href="DeleteReservation?id="+<%=reservationId%>;	
	}
}

function getResource(resourceId, objectView){
	location.href="ViewResource?resourceId="+resourceId+"&provenance=reservation&reservationId="+<%=maReservation.getId()%> + "&objectView=" + objectView;
}

function valideResource(resourceId, objectView) {
	if(confirm("<%=resource.getString("resourcesManager.confirmValideResource")%>")){
       location.href="ValidateResource?ResourceId=" + resourceId + "&reservationId=" + <%=reservationId%> + "&objectView=" + objectView;
    }
}

function refuseResource(resourceId, resourceName, objectView) {
    window.location.href = "ForRefuseResource?ResourceId=" + resourceId + "&ResourceName=" + resourceName+ "&reservationId=" + <%=reservationId%> + "&objectView=" + objectView;
}

function AddAttachment() {
	SP_openWindow("/silverpeas/attachment/jsp/addAttFiles.jsp", "test", "600", "240","scrollbars=no, resizable, alwaysRaised");
}
</script>
</head>
	<body id="resourcesManager">
	<%
	if (isOwner)
	{
		operationPane.addOperation(resource.getIcon("resourcesManager.updateBig"), resource.getString("resourcesManager.modifierReservation"),"EditReservation?id="+reservationId);
		operationPane.addOperationOfCreation("#", resource.getString("resourcesManager.addFile"), "javaScript:AddAttachment()");
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("resourcesManager.basketDelete"), resource.getString("resourcesManager.supprimerReservation"),"javascript:deleteReservation();");
	}	
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel,"Main");
		browseBar.setPath(resource.getString("resourcesManager.recapitulatifReservation"));
				
		out.println(window.printBefore());
  %>
<view:frame>
<view:areaOfOperationOfCreation/>
<table width="100%">
	<tr>
		<td valign="top">
		<view:board>
			<table cellpadding="3" cellspacing="0" border="0" width="100%">
				<tr>
					<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> :</td>
					<td width="100%"><%=event%></td>
				</tr>
				<tr>
				<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateBegin"));%> :</td> 
				<td> <%=dateBegin%></td>
				</tr>
				
				<tr>
				<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("GML.dateEnd"));%> :</td>
				<td><%=dateEnd%></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap="nowrap"><%=resource.getString("resourcesManager.bookedBy") %> :</td>
					<td><%=maReservation.getUserName()%></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.creationDate") %> :</td>
					<td><%=resource.getOutputDateAndHour(maReservation.getCreationDate())%></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.updateDate") %> :</td>
					<td><%=resource.getOutputDateAndHour(maReservation.getUpdateDate())%></td>
				</tr>
				<tr>
				<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.raisonReservation"));%> :</td> 
				<td><%=reason%></td>
				</tr>
				
				<tr>
				<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> :</td>
				<td><%=place%></td>
				</tr>
			</table>
		</view:board>
		<br/>
		<view:board>
      <table cellpadding="3" cellspacing="0" border="0" width="100%">
        <tr>
          <td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.resourcesReserved"));%> :</td>
          <td width="100%"><%
          for(Resource maResource : listResourcesofReservation){
            String resourceId = maResource.getId();
            String resourceName = maResource.getName();
            // afficher les icones de validation et refus si la ressource est en etat a valider
            // et si l'utilisateur est le responsable de cette ressource
            String currentUser = resourcesManagerSC.getUserId() ;
            List<String> managers = resourcesManagerSC.getManagerIds(resourceId);
            if (maResource.isValidationRequired()) { %>
             <a style="color:red" href="javascript:getResource(<%=resourceId%>, '<%=objectView%>')"><%=resourceName%></a> 
            <% } else if (maResource.isRefused()) { %>
              <a style="color:grey" href="javascript:getResource(<%=resourceId%>, '<%=objectView%>')"><%=resourceName%></a>
            <% } else {%>
              <a style="color:black" href="javascript:getResource(<%=resourceId%>, '<%=objectView%>')"><%=resourceName%></a> 
             <% } 
            if (maResource.isValidationRequired() &&  managers != null && !managers.isEmpty() && managers.contains(currentUser)) { %>
              <a href="javascript:valideResource(<%=resourceId%>, '<%=objectView%>')">
              <img src="<%=m_context%>/util/icons/ok.gif" align="middle" border="0" alt="<%=resource.getString("resourcesManager.valideResource")%>" title="<%=resource.getString("resourcesManager.valideResource")%>"/>
              </a>&nbsp;
              <a href="javascript:refuseResource(<%=resourceId%>, '<%=resourceName%>', '<%=objectView%>')">
              <img src="<%=m_context%>/util/icons/wrong.gif" align="middle" border="0" alt="<%=resource.getString("resourcesManager.refuseResource")%>" title="<%=resource.getString("resourcesManager.refuseResource")%>"/>
              </a>&nbsp;
            <% }  %>
            <br/>
        <% } %>
          </td>
        </tr>
      </table>
      </view:board>
      <div class="inlineMessage">
            <%=resource.getString("resourcesManager.explain") %>
	  </div>
		</td>
		<td valign="top">
    <%
    out.flush();
    // traitement des fichiers joints
    getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachedFiles.jsp?Id="+reservationId+"&ComponentId="+componentId+"&Alias=&Context=Images&AttachmentPosition=right&ShowIcon=true&ShowTitle=&ShowFileSize=true&ShowDownloadEstimation=&ShowInfo=&UpdateOfficeMode=&Language="+resourcesManagerSC.getLanguage()+"&Profile="+profileForAttachments+"&CallbackUrl="+URLManager.getURL("useless",componentId)+"ViewReservation?reservationId="+reservationId+"&IndexIt=").include(request, response);
    %>
    <br/>
		</td>
	</tr>
</table>
<br/>
<%=buttonPane.print() %>
</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>