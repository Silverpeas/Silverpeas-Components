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
<%@ page import="com.silverpeas.whitePages.html.WhitePagesHtmlTools"%>
<%@ page import="com.silverpeas.whitePages.model.Card"%>
<%@ page import="com.silverpeas.whitePages.model.SearchField"%>
<%@ page import="com.silverpeas.whitePages.model.SearchFieldsType"%>
<%@ page import="com.silverpeas.whitePages.record.UserRecord"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.SearchAxis"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.Value"%>
<%@ page import="com.stratelia.silverpeas.pdc.model.SearchContext"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>

<%@ include file="checkWhitePages.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="browseContext" value="${requestScope.browseContext}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
<%
boolean main = false;
if(request.getAttribute("Main") != null){
  main = StringUtil.getBooleanValue((String)request.getAttribute("Main"));  
}
boolean isAdmin = false;
if(request.getAttribute("isAdmin") != null){
  isAdmin = ((Boolean)request.getAttribute("isAdmin")).booleanValue();
}
if(isAdmin){
%>
function B_CREATE_ONCLICK() {
	location.href = "<%=routerUrl%>createQuery";
}

function listCheckedCard(nbCard) {
		var listeCard = "";
		
        if (nbCard > 0) {
          if (nbCard == 1) {
                if (document.liste_card.checkedCard.checked)
                        listeCard += document.liste_card.checkedCard.value + ",";
          }
          
          else {  
            for (i=0; i<nbCard; i++) {
                if (document.liste_card.checkedCard[i] != null) {
                    if (document.liste_card.checkedCard[i].checked)
                        listeCard += document.liste_card.checkedCard[i].value + ",";   
                }
                else break;
            }
          }
		}    
		
		return listeCard;
}

function B_DELETE_ONCLICK(nbCard) {
          if (listCheckedCard(nbCard) != "") {   //on a coch� au - une fiche
			if (window.confirm("<%=resource.getString("whitePages.messageSuppressions")%>")) { 
				document.liste_card.action = "<%=routerUrl%>delete";
				document.liste_card.submit();
			}
		  }
}

function B_HIDE_ONCLICK(nbCard) {    
          if (listCheckedCard(nbCard) != "") {   //on a coch� au - une fiche
				document.liste_card.action = "<%=routerUrl%>hide";
				document.liste_card.submit();
		  }
}

function B_SHOW_ONCLICK(nbCard) {
          if (listCheckedCard(nbCard) != "") {   //on a coch� au - une fiche
				document.liste_card.action = "<%=routerUrl%>unHide";
				document.liste_card.submit();
		  }
}
<%}
if(main){%>
function B_SEARCH_ONCLICK() {
	location.href = "<%=routerUrl%>searchInWhitePage?componentId=<%=scc.getComponentId()%>";
}
<%}%>

function openSPWindow(fonction, windowName){
	SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}
function consult(idCard) {
	location.href = "<%=routerUrl%>consultCard?userCardId="+idCard;
}

function defineSearchFields(){
	location.href = "<%=routerUrl%>dynamicFieldsChoice?componentId=<%=scc.getComponentId()%>";
}

function dynamicSearchLaunch(){
	$.progressMessage();
	document.searchform.submit();
}

function OpenPopup(userId, name){
	initNotification(userId, name);
}
</script>
<script type="text/javascript">
     
      $(document).ready(function(){
			
			$(".linkMore").click(function() {
				
				var divAAfficher = this.id.substring(5);
				$('#'+this.id).hide();
				$('#'+divAAfficher).show('slow');
				});
	       
      });
    </script>
</head>
<body id="whitePages">
<%
List cards = (List)request.getAttribute("cards");

browseBar.setDomainName(spaceLabel);
browseBar.setPath(resource.getString("whitePages.usersList"));

if(isAdmin){
	operationPane.addOperation(resource.getIcon("whitePages.defineSearch"), resource.getString("whitePages.definesearchfields"), "javascript:onClick=defineSearchFields()");
	operationPane.addLine();
	operationPane.addOperation(pdcUtilizationSrc, resource.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+scc.getComponentId()+"','utilizationPdc1')");
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("whitePages.newCard"), resource.getString("whitePages.op.createUser"), "javascript:onClick=B_CREATE_ONCLICK();");
	if(cards != null && cards.size() > 0){
		operationPane.addOperation(resource.getIcon("whitePages.delCard"), resource.getString("whitePages.op.deleteUser"), "javascript:onClick=B_DELETE_ONCLICK('"+cards.size()+"');");
		operationPane.addOperation(resource.getIcon("whitePages.hideCard"), resource.getString("whitePages.op.hideCard"), "javascript:onClick=B_HIDE_ONCLICK('"+cards.size()+"');");
		operationPane.addOperation(resource.getIcon("whitePages.showCard"), resource.getString("whitePages.op.showCard"), "javascript:onClick=B_SHOW_ONCLICK('"+cards.size()+"');");
	}
}
if(main){
    operationPane.addLine();
	operationPane.addOperation(resource.getIcon("whitePages.searchCard"), resource.getString("whitePages.op.searchCard"), "javascript:onClick=B_SEARCH_ONCLICK();");
}

out.println(window.printBefore());
out.println(frame.printBefore());

SortedSet searchFields = (SortedSet) request.getAttribute("searchFields");
List primaryPdcFields = (List)request.getAttribute("primaryPdcFields");
List secondaryPdcFields = (List)request.getAttribute("secondaryPdcFields");
boolean searchDone = StringUtil.getBooleanValue((String)request.getAttribute("searchDone"));

String queryValue = request.getAttribute("query") != null ?  (String)request.getAttribute("query") : "";
if(!main){
  // no form search in Main
%>
<br/><fmt:message key="whitePages.op.searchCard"/><br/>
<div class="zoneNavAndSearch">
          <div id="search">
<form id="searchform" name="searchform" method="post" action="<%=routerUrl + "getSearchResult"%>" >
	<div>
    	<label class="txtlibform" for="query"><fmt:message key="whitePages.keywords"/></label>
        <input type="text" id="query" value="<%=queryValue%>" size="60" name="query" class="ac_input champTexte"/>
        <p class="txtexform"><fmt:message key="whitePages.keywordssample"/></p>
    </div>
<%
if (primaryPdcFields != null && !primaryPdcFields.isEmpty()) {   
%>
<div id="primaryAxe" class="arbre">
    <img title="Axe primaire" alt="primaire" src="<%=m_context%>/pdcPeas/jsp/icons/primary.gif"/>
<%
	out.println(WhitePagesHtmlTools.generateHtmlForPdc(primaryPdcFields, language, request));
%> 
    </div> 	
<%
}
if (secondaryPdcFields != null && !secondaryPdcFields.isEmpty()) {   
%>
<a href="#" id="link_secondaryAxe" 
                			title="<fmt:message key="whitePages.secondary"/>" 
                            class="linkMore">
              		<fmt:message key="whitePages.secondary"/>
</a>

<div  id="secondaryAxe" class="arbre">
<img title="<fmt:message key="whitePages.secondary" />" alt="<fmt:message key="whitePages.secondary" />" src="<%=m_context%>/pdcPeas/jsp/icons/secondary.gif"/> 	
<%
	out.println(WhitePagesHtmlTools.generateHtmlForPdc(secondaryPdcFields, language, request));
%> 
    </div> 	
<%
}
if (searchFields != null && !searchFields.isEmpty()) {
%>
<a href="#" id="link_additionalElements" 
            title="<fmt:message key="whitePages.suppl"/>" 
            class="linkMore">
<fmt:message key="whitePages.suppl"/>
</a>

<div id="additionalElements" class="arbre">
  <img title="<fmt:message key="whitePages.suppl"/>" alt="<fmt:message key="whitePages.suppl"/>" src="<%=m_context%>/whitePages/jsp/icons/contactCard.gif"/> 
<%
	Iterator i = searchFields.iterator();
	while (i.hasNext()) {
		SearchField searchField = (SearchField) i.next();
		String fieldName = searchField.getFieldId().substring(4,searchField.getFieldId().length());
		String fieldId = searchField.getFieldId();
		String fieldValue = request.getAttribute(fieldId) != null ?  (String)request.getAttribute(fieldId) : "";
%>
  <div>
     <label class="txtlibform" for="nom"><%=fieldName%></label>
     <input class="champTexte" type="text" id="<%=fieldId%>" name="<%=fieldId%>" value="<%=fieldValue%>"/>
  </div>
<%	
  }
%>
</div> 
<%	
}
%>
<div id="btnValidSearch">
  <table cellspacing="0" cellpadding="0" border="0">
      <tbody><tr>
          <td align="left" class="gaucheBoutonV5"><img src="<%=m_context%>/util/viewGenerator/icons/px.gif" alt="" /></td>
          <td nowrap="nowrap" class="milieuBoutonV5"><a href="javascript:dynamicSearchLaunch()"><fmt:message key="whitePages.button.search"/></a></td>
          <td align="right" class="droiteBoutonV5"><img src="<%=m_context%>/util/viewGenerator/icons/px.gif" alt="" /></td>
       </tr></tbody>
  </table>  
</div>
</form>
</div> 	
</div>
<%
}
%>
<div class="listinUsers">
<form name="liste_card" action="">
  <ol class="message_list aff_colonnes">
<%
if(cards != null && cards.size() > 0){
  Iterator iterCards = cards.iterator();
  while(iterCards.hasNext()){
    Card card = (Card)iterCards.next();
    UserRecord userRecord = card.readUserRecord();
	String lastName = userRecord.getField("LastName").getValue(language);
	String firstName = userRecord.getField("FirstName").getValue(language);
	String email = userRecord.getField("Mail").getValue(language);
%>
           <li class="intfdcolor">
           <%
           if(isAdmin){
           %>
           		 <input type="checkbox" value="<%=card.getPK().getId()%>" name="checkedCard" class="check" />
           <%
           }
           %>
                 <div class="profilPhoto">
                   	<a href="javascript:consult(<%=card.getPK().getId()%>)">
                   	<img class="defaultAvatar" alt="viewUser" src="<%=m_context + userRecord.getUserDetail().getAvatar()%>"/>
                   	</a>
                 </div>
                 <div class="info">
                   <ul>
                     <li class="userName"><a href="javascript:consult(<%=card.getPK().getId()%>)"><%=lastName%>&nbsp;<%=firstName%></a></li>
                     <li class="infoConnection">
                     <%if(userRecord.isConnected()){%>
                       	<img alt="connected" src="<%=m_context%>/util/icons/online.gif" />
                     <%}else{%>
                     	<img alt="deconnected" src="<%=m_context%>/util/icons/offline.gif" />
                     <%}%>
                     </li>
                     <li class="infoVisibility">
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
                     </li>
                     <li class="userMail">
                     	<a href="#" onclick="OpenPopup(<%=card.getUserId()%>,'<%=lastName + " " + firstName%>')"><%=email%></a>
                     </li>                     
                   </ul>
                 </div>
                <div class="action">
                	  <a onclick="OpenPopup(<%=card.getPK().getId()%>,'admin ')" class="link notification" href="#"><fmt:message key="whitePages.sendNotif"/></a> <a onclick="javascript:consult(<%=card.getPK().getId()%>)" class="link goToWhitepages" href="javascript:consult(<%=card.getPK().getId()%>)"><fmt:message key="whitePages.seeCard"/></a>
                </div>
               <br clear="all" />
            </li><%
  } 
}else if(searchDone){
%>
<li class="intfdcolor">
	<fmt:message key="whitePages.nosearchresults"/>
</li>
<%
}
%>
		</ol>
	</form>
</div>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
	<%@include file="../../socialNetwork/jsp/notificationDialog.jsp" %>
	<view:progressMessage/>
</body>
</html>
