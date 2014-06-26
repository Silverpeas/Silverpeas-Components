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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.whitePages.model.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page import="com.silverpeas.whitePages.record.UserRecord"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.ClassifyValue"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.Value"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.ClassifyPosition"%>

<%@ include file="checkWhitePages.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
	browseBar.setDomainName(spaceLabel);
	
	UserFull userFull = (UserFull) request.getAttribute("userFull");
  
	Card card = (Card) request.getAttribute("card");
	String userCardId = card.getPK().getId();
	UserRecord userRecord = card.readUserRecord();
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ userRecord.getUserDetail().getDisplayedName());
	
	boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
	
	Collection<WhitePagesCard> whitePagesCards = (Collection<WhitePagesCard>) request.getAttribute("whitePagesCards");
	Form viewForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	context.setBorderPrinted(false);
	DataRecord data = (DataRecord) request.getAttribute("data"); 
	Map<String, Set<ClassifyValue>> pdcPositions = (Map<String, Set<ClassifyValue>>)request.getAttribute("pdcPositions");
	
	boolean anotherCard = false;
	if (whitePagesCards != null) {
		for (WhitePagesCard whitePagesCard: whitePagesCards) {
			long id = whitePagesCard.getUserCardId();
			if(!card.getPK().getId().equals(String.valueOf(id))){
			  anotherCard = true;
			}
		}
	}
	
	if (! card.readReadOnly()) {
		operationPane.addOperation(resource.getIcon("whitePages.editCard"), resource.getString("whitePages.op.editUser"), "javascript:onClick=B_UPDATE_ONCLICK('"+userCardId+"');");		
		operationPane.addOperation(resource.getString("whitePages.PdcClassification"), resource.getString("whitePages.op.editPdc"), "javascript:onclick=displayPDC()");
		operationPane.addLine();

		if (isAdmin) {
			operationPane.addOperation(resource.getIcon("whitePages.delCard"), resource.getString("whitePages.op.deleteUser"), "javascript:onClick=B_DELETE_ONCLICK('"+userCardId+"');");
			
			if (card.getHideStatus() == 0) {//Visible
				operationPane.addOperation(resource.getIcon("whitePages.hideCard"), resource.getString("whitePages.op.hideCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");	
			} else {//Masque
				operationPane.addOperation(resource.getIcon("whitePages.showCard"), resource.getString("whitePages.op.showCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");
			}
		}

		if (containerContext != null)
		{
			URLIcone classify = containerContext.getClassifyURLIcone();
			String classifyURL = null;
			if (contentId != null)
			{
				classifyURL = containerContext.getClassifyURLWithParameters(
					componentId, contentId);
			}
			if (classifyURL != null)
			{
				operationPane.addOperation(classify.getIconePath(),
													"Classer",
													"javascript:openSPWindow('"+m_context+classifyURL+"','classify')");
			}
		}
	}
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
%>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="browseContext" value="${requestScope.browseContext}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<view:includePlugin name="messageme"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function openSPWindow(fonction,windowName){
		SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
	
	function changerChoice(idCard, urlInstance) {
        document.choixFiche.userCardId.value = idCard;
        if (idCard == "0") {
        	document.choixFiche.action = urlInstance + "createCard";
        }
        else 
        	document.choixFiche.action = urlInstance + "consultCard";
        document.choixFiche.submit();	
	}
	
/*****************************************************************************/
	function B_UPDATE_ONCLICK(idCard) {
		 theURL = "<%=routerUrl%>updateCard?userCardId="+idCard;
         winName = "updateCard";
         larg ="600";
         haut = "400";
         windowParams = "scrollbars=yes, resizable, alwaysRaised";
         dico = SP_openWindow(theURL, winName, larg, haut, windowParams);
	}

/*****************************************************************************/
	function B_DELETE_ONCLICK(idCard) {
		if (window.confirm("<%=resource.getString("whitePages.messageSuppression")%>")) { 
			location.href = "<%=routerUrl%>delete?checkedCard="+idCard;
		}
	}	
	
/*****************************************************************************/
	function B_REVERSEHIDE_ONCLICK(idCard) {
		location.href = "<%=routerUrl%>reverseHide?returnPage=consultCard&userCardId="+idCard;
	}
	
	function displayPDC() {
		$(".divSee").hide();
		$("#expert-classification").show();
	}
   
    $(document).ready(function(){
			
			$(".divSee").hide();
			$(".active").show();
			
			$(".linkSee").click(function() {
				var divAAfficher = this.id.substring(5);
				$(".linkSee").removeClass('active');
				$('#'+this.id).addClass('active');
				$(".divSee").hide();
				$('#'+divAAfficher).show();
			});
			
			if ($.trim($("#sheetIdentity").text()).length == 0) {
				$(".linkSee").removeClass('active');
				$('#link_sheetExpert').addClass('active');
				$('#sheetExpert').show();
			}
      });
</script>
</head>
<body id="whitePagesSheet">
<%
out.println(window.printBefore());
out.println(frame.printBefore());

String lastName = userRecord.getField("LastName").getValue(language);
String firstName = userRecord.getField("FirstName").getValue(language);
%>

<!-- userProfil  -->  
<div class="userProfil">
  
	<!-- info  -->           
  	<div class="info tableBoard">
    	<h2 class="userName"><%=lastName%><br/><%=firstName%></h2>
        <p class="infoConnection">
        <%if(userRecord.isConnected()){%>
          	<img alt="connected" src="<%=m_context%>/util/icons/online.gif" />
        <%}else{%>
        	<img alt="deconnected" src="<%=m_context%>/util/icons/offline.gif" />
        <%}%>	
		</p>  
               
	    <!-- action  -->
        <div class="action">
        	<a rel="<%=card.getUserId()%>,<%=lastName + " " + firstName%>" class="link notification" href="#"><fmt:message key="whitePages.sendNotif"/></a>
        </div> <!-- /action  -->              

        <!-- profilPhoto  -->  
		<div class="profilPhoto">
			<view:image css="defaultAvatar" alt="viewUser" src="<%=userRecord.getUserDetail().getAvatar()%>" type="avatar"/>
        </div>
             
        <p class="statut">
        <% if (card.getHideStatus() == 1) {// hide card %>
        	<img title="Masque" alt="Masque" src="<%=m_context%>/util/icons/masque.gif" />
        <% } else { %>
        	<img title="Visible" alt="Visible" src="<%=m_context%>/util/icons/visible.gif" />
        <% } %>	
        </p>
         
        <br clear="all" />
 	</div><!-- /info  -->
    
    <% if(pdcPositions != null && !pdcPositions.isEmpty()){ %>  
    <!-- pdcPosition  -->
    <div class="pdcPosition">
        <h3><fmt:message key="whitePages.pdc"/></h3>
        <ul>
        <%
          Set<String> keysStart = pdcPositions.keySet();
          String[] keys = keysStart.toArray(new String[keysStart.size()]);
          Arrays.sort(keys);
          for(String key: keys) {
	          %>
			  <li><%=key%>	
		      <%
              	Set<ClassifyValue> values = pdcPositions.get(key);
                for(ClassifyValue value: values) {
                  List<Value> path = value.getFullPath();
                  for(int i = 1; i < path.size(); i++) {
                    String term = path.get(i).getName(language);
              %>
              <ul>
                <li><%= term %></li>
              <%
                  }
                  for (int i = 1; i < path.size(); i++) {
              %>
              </ul>	
              <%  
                  }
                }
              %>
              </li>
              <%
	        }
        %>
        </ul>
	</div><!-- /pdcPosition  -->
	<% } %> 
      
</div><!-- /userProfil  -->      

<!-- theSheets  -->   
 <div id="theSheets" class="sheet">

	<!-- sousNav  --> 
	<div class="sousNavBulle">
		<p><fmt:message key="whitePages.showPart"/> : 
        	<a href="#" 
               id="link_sheetIdentity"
               class="active linkSee">
             <fmt:message key="whitePages.idpart"/>
             </a>  
             
            <a href="#" 
            	id="link_sheetExpert" 
                class="linkSee">
             <fmt:message key="whitePages.expertpart"/>
             </a>
        
        <% if (anotherCard) { %>
	        &nbsp;&nbsp;-&nbsp;&nbsp; <img alt="Annuaire" title="Autres annuaires" src="<%=m_context%>/util/icons/component/whitePagesSmall.gif"/><fmt:message key="whitePages.others"/> :
    	<%    
          for(WhitePagesCard whitePagesCard: whitePagesCards) {
				long id = whitePagesCard.getUserCardId();
				String instanceId = whitePagesCard.getInstanceId();
				if(!card.getPK().getId().equals(String.valueOf(id))){
					String label = whitePagesCard.getInstanceLabel();
					String url = URLManager.getApplicationURL() + URLManager.getURL("whitePages", spaceId, instanceId);
					out.println("<a href=\"javascript:changerChoice('"+id+"','"+url+"')\">"+label+"</a>");
				}
    		}
         }
        %>
	</div><!-- /sousNav  --> 

<div class="divSee active" id="sheetIdentity">

<table cellspacing="0" cellpadding="5" border="0">
<tbody>
<%
if (userFull != null) {
      	  // getting all user properties
          String[] properties = userFull.getPropertiesNames();
          for (String property : properties) {
          	if (StringUtil.isDefined(userFull.getValue(property))) {
          %>
              <tr id="property-<%=property%>">
                <td class="txtlibform"><%=userFull.getSpecificLabel(language, property)%> : </td>
                <td><%=userFull.getValue(property)%></td>
              </tr>
  		<%
            }
          }
}
%>
</tbody>
</table>

</div>

<div class="divSee" id="sheetExpert">
<form name="myForm" method="post" action="<%=routerUrl%>effectiveCreate">
<%	
	viewForm.display(out, context, data);
%>
</form>
</div>

<br/>

<div class="divSee" id="expert-classification">
	<view:pdcClassification componentId="<%= componentId %>" contentId="<%= card.getPK().getId() %>" editable="true" />
</div>

</div><!-- /theSheets  -->
<br clear="all"/>
<center>
<%=buttonPane.print()%>
</center>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

<form name="choixFiche" method="post">
	<input type="hidden" name="userCardId" />
</form>
	<view:progressMessage/>
</body>
</html>