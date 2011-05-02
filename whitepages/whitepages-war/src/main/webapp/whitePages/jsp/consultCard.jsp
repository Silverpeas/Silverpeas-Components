<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
	browseBar.setPath(resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.consultCard"));
	
	UserFull userFull = (UserFull) request.getAttribute("userFull");
  
	Card card = (Card) request.getAttribute("card");
	String userCardId = card.getPK().getId();
	
	boolean isAdmin = ( (Boolean) request.getAttribute("isAdmin")).booleanValue();
	
	Collection whitePagesCards = (Collection) request.getAttribute("whitePagesCards");
	Form viewForm = (Form) request.getAttribute("Form");
	PagesContext context = (PagesContext) request.getAttribute("context"); 
	DataRecord data = (DataRecord) request.getAttribute("data"); 
	HashMap pdcPositions = (HashMap)request.getAttribute("pdcPositions");
	
	if (! card.readReadOnly()) {
		operationPane.addOperation(resource.getIcon("whitePages.editCard"), resource.getString("whitePages.op.editUser"), "javascript:onClick=B_UPDATE_ONCLICK('"+userCardId+"');");
		operationPane.addLine();
		
		operationPane.addOperation(resource.getString("whitePages.PdcClassification"), resource.getString("whitePages.op.editPdc"), routerUrl+"ViewPdcPositions?userCardId="+userCardId);
		operationPane.addLine();

		if (isAdmin) {
			operationPane.addOperation(resource.getIcon("whitePages.delCard"), resource.getString("whitePages.op.deleteUser"), "javascript:onClick=B_DELETE_ONCLICK('"+userCardId+"');");
			operationPane.addLine();
			
			if (card.getHideStatus() == 0) {//Visible
				operationPane.addOperation(resource.getIcon("whitePages.hideCard"), resource.getString("whitePages.op.hideCard"), "javascript:onClick=B_REVERSEHIDE_ONCLICK('"+userCardId+"');");	
			}
			else {//Masque
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
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.back"), routerUrl+"Main", false));
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" var="LML" />
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML" />
<c:set var="browseContext" value="${requestScope.browseContext}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

var targetUserId = -1;

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

	function OpenPopup(userId, name){
	    $("#directoryDialog").dialog("option", "title", name);
	    targetUserId = userId;
		$("#directoryDialog").dialog("open");
	  }

	var targetUserId = -1;  

	function sendNotification(userId) {
	      var title = stripInitialWhitespace($("#txtTitle").val());
	      var errorMsg = "";
	      if (isWhitespace(title)) {
	          errorMsg = "<fmt:message key="GML.thefield" bundle="${GML}"/>"+ " <fmt:message key="whitePages.object" bundle="${LML}"/>"+ " <fmt:message key="GML.isRequired" bundle="${GML}"/>";
	      }
	      if (errorMsg == "") {
	      	$.getJSON("<%=m_context%>/DirectoryJSON",
	              	{ 
	      				IEFix: new Date().getTime(),
	      				Action: "SendMessage",
	      				Title: $("#txtTitle").val(),
	      				Message: $("#txtMessage").val(),
	      				TargetUserId: targetUserId
	              	},
	      			function(data){
	          			if (data.success) {
	              			closeDialog();
	          			} else {
	              			alert(data.error);
	          			}
	      			});
	      } else {
	        window.alert(errorMsg);
	      }
	  }

	function closeDialog() {
	  	$("#directoryDialog").dialog("close");
	  	$("#txtTitle").val("");
	  	$("#txtMessage").val("");
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

		    var dialogOpts = {
	                modal: true,
	                autoOpen: false,
	                height: 250,
	                width: 600
	        };
	
	        $("#directoryDialog").dialog(dialogOpts);    //end dialog
	       
      });
</script>
</head>
<body id="whitePagesSheet">
<%
out.println(window.printBefore());
out.println(frame.printBefore());

UserRecord userRecord = card.readUserRecord();
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
        	<a onclick="OpenPopup(<%=card.getUserId()%>,'<%=lastName + " " + firstName%>');" class="link notification" href="#"><fmt:message key="whitePages.sendNotif" bundle="${LML}"/></a>
        </div> <!-- /action  -->              

        <!-- profilPhoto  -->  
		<div class="profilPhoto">
			<img class="defaultAvatar" alt="viewUser" src="<%=m_context + userRecord.getUserDetail().getAvatar()%>"/>
        </div>
             
        <p class="statut">
        <%
        if (card.getHideStatus() == 1) {// hide card
        %>
        	<img title="Masque" alt="Masque" src="<%=m_context%>/util/icons/masque.gif" />
        <%
        }else{
        %>
        	<img title="Visible" alt="Visible" src="<%=m_context%>/util/icons/visible.gif" />
        <%
        }
        %>	
        </p>
         
        <br clear="all" />
 	</div><!-- /info  -->          
    
    <!-- pdcPosition  -->        
    <div class="pdcPosition">
        <h3><fmt:message key="whitePages.pdc" bundle="${LML}"/></h3>
        <ul>
        <%
        if(pdcPositions != null && !pdcPositions.isEmpty()){
          Set keysStart = pdcPositions.keySet();
          String[] keys = new String[keysStart.size()];
          Iterator iter = keysStart.iterator();
          int i = 0;
          while(iter.hasNext()){
            keys[i] = (String)iter.next();
            i++;
          }
          Arrays.sort(keys);
          
          for(int iKey=0;iKey<keys.length;iKey++){
	          String key = keys[iKey];
	          %>
			  <li><%=key%>	
		      <%
              	List positions = (List)pdcPositions.get(key);
              	Iterator iterPositions = positions.iterator();
              	while(iterPositions.hasNext()){
	              	ClassifyValue value	= (ClassifyValue)iterPositions.next();
	    	        List pathValues	= value.getFullPath();
		      		for(int j= 0; j < pathValues.size(); j++){
		      		  	Value term = (Value) pathValues.get(j);
		      		  	if(j!=0){
		      		%>
		            <ul>
	        			<li><%=term.getName(language)%></li>
	                <%
		      		  	}
						if(j == pathValues.size() -1){
					  		for(int k= 1; k < pathValues.size(); k++){
					%>
						</ul>	
		            <%
					  		}
		      		  	}
		          	  }
		        	}
              	  	%>
		  			</li>
	  			<%
	        }
        }
        %>
        </ul>
	</div><!-- /pdcPosition  -->      
      
</div><!-- /userProfil  -->      

<!-- theSheets  -->   
 <div id="theSheets" class="sheet">

	<!-- sousNav  --> 
	<div class="sousNavBulle">
		<p><fmt:message key="whitePages.showPart" bundle="${LML}"/> : 
        	<a href="#" 
               id="link_sheetIdentity"
               class="active linkSee">
             <fmt:message key="whitePages.idpart" bundle="${LML}"/>
             </a>  
             
            <a href="#" 
            	id="link_sheetExpert" 
                class="linkSee">
             <fmt:message key="whitePages.expertpart" bundle="${LML}"/>
             </a>
             
        &nbsp;&nbsp;-&nbsp;&nbsp; <img alt="Annuaire" title="Autres annuaires" src="<%=m_context%>/util/icons/component/whitePagesSmall.gif"/><fmt:message key="whitePages.others" bundle="${LML}"/> :
        
        <%
        if (whitePagesCards != null) {
			Iterator i = whitePagesCards.iterator();
			while (i.hasNext()) {
				WhitePagesCard whitePagesCard = (WhitePagesCard) i.next();
				long id = whitePagesCard.getUserCardId();
				String instanceId = whitePagesCard.getInstanceId();
				if(!card.getPK().getId().equals(String.valueOf(id))){
					String label = whitePagesCard.readInstanceLabel();
					String url = URLManager.getApplicationURL() + URLManager.getURL("whitePages", spaceId, instanceId);
					out.println("<a href=\"javascript:changerChoice('"+id+"','"+url+"')\">"+label+"</a>");
				}
    		}
         }
        %>
	</div><!-- /sousNav  --> 

<div class="divSee active" id="sheetIdentity">

<table width="100%" cellspacing="0" cellpadding="5" border="0" class="contourintfdcolor">
<tbody>
<%
if (userFull != null) {
      	//  récupérer toutes les propriétés de ce User
          String[] properties = userFull.getPropertiesNames();

          String property = null;
          for (int p = 0; p < properties.length; p++) {
           property = properties[p];
           if (StringUtil.isDefined(userFull.getValue(property))) {
          %>
              <tr align="center">
                <td valign="top" align="left" class="intfdcolor4">
	               	<span class="txtlibform"><%=userFull.getSpecificLabel(language, property)%> :</span>
                </td>
                <td valign="baseline" align="left" class="intfdcolor4">
					<%=userFull.getValue(property)%>
				</td>
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

</div><!-- /theSheets  -->

<%=buttonPane.print()%>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>

<form name="choixFiche" method="post">
	<input type="hidden" name="userCardId" />
</form>
<!-- Dialog to notify a user -->
	<div id="directoryDialog">
		<view:board>
        <form name="notificationSenderForm" action="SendMessage" method="post">
        	<table>
          <tr>
            <td class="txtlibform">
              <fmt:message key="whitePages.object" bundle="${LML}" /> :
            </td>
            <td>
              <input type="text" name="txtTitle" id="txtTitle" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" size="50" value=""/>
              <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" alt="mandatoryField" />
            </td>
          </tr>
          <tr>
            <td class="txtlibform">
              <fmt:message key="whitePages.message" bundle="${LML}" /> :
            </td>
            <td>
              <textarea name="txtMessage" id="txtMessage" cols="49" rows="4"></textarea>
            </td>
          </tr>
          <tr>
            <td colspan="2">
	    (<img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" alt="mandatoryField" /> <fmt:message key="GML.requiredField" bundle="${GML}"/>)
            </td>
          </tr>
          </table>
        </form>
        </view:board>
        <div align="center">
          <%
			ButtonPane buttonPanePopup = gef.getButtonPane();
          	buttonPanePopup.addButton((Button) gef.getFormButton("Envoyer", "javascript:sendNotification('" + card.getUserId() + "')", false));
          	buttonPanePopup.addButton((Button) gef.getFormButton("Cancel", "javascript:closeDialog()", false));
			out.println(buttonPanePopup.print());
          %>
        </div>
	</div>
	<view:progressMessage/>
</body>
</html>