<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.components.whitepages.model.Card"%>
<%@ page import="org.silverpeas.components.whitepages.model.SearchField"%>
<%@ page import="org.silverpeas.components.whitepages.record.UserRecord"%>
<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.SortedSet"%>

<%@ include file="checkWhitePages.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${requestScope.resources.language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="browseContext" value="${requestScope.browseContext}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.whitePages">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="toggle"/>
<view:includePlugin name="pdc"/>
<script type="text/javascript">
<%
boolean main = false;
if(request.getAttribute("Main") != null){
  main = StringUtil.getBooleanValue((String)request.getAttribute("Main"));
}
boolean isAdmin = false;
if(request.getAttribute("isAdmin") != null){
  isAdmin = (Boolean)request.getAttribute("isAdmin");
}
if(isAdmin){
%>
function B_CREATE_ONCLICK() {
	sp.formRequest("<%=routerUrl%>createQuery").byPostMethod().submit();
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
    var label = "<%=resource.getString("whitePages.messageSuppressions")%>";
    jQuery.popup.confirm(label, function() {
      document.liste_card.action = "<%=routerUrl%>delete";
      document.liste_card.submit();
    });
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
  var values = $('#used_pdc').pdc('selectedValues');
  if (values.length > 0) {
    document.searchform.AxisValueCouples.value = values.flatten();
  }
	$.progressMessage();
	document.searchform.submit();
}
</script>
<script type="text/javascript">

  $(document).ready(function(){

		$(".linkMore").click(function() {
			var divAAfficher = this.id.substring(5);
			$('#'+this.id).hide();
			$('#'+divAAfficher).show('slow');
      });

    $('#used_pdc').pdc('used', {
      workspace: 'useless',
      component: '<%=scc.getComponentId()%>',
      withSecondaryAxis: true
    });
  });
    </script>
</head>
<body id="whitePages">
<%
List<Card> cards = (List<Card>)request.getAttribute("cards");

browseBar.setPath(resource.getString("whitePages.usersList"));

if(isAdmin){
	operationPane.addOperation(pdcUtilizationSrc, resource.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+scc.getComponentId()+"','utilizationPdc1')");
	operationPane.addOperation(resource.getIcon("whitePages.defineSearch"), resource.getString("whitePages.definesearchfields"), "javascript:onClick=defineSearchFields()");
	operationPane.addLine();
	operationPane.addOperationOfCreation(resource.getIcon("whitePages.newCard"), resource.getString("whitePages.op.createUser"), "javascript:onClick=B_CREATE_ONCLICK();");
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

%>
<view:componentInstanceIntro componentId="<%=componentId%>" language="<%=resource.getLanguage()%>"/>
<%

SortedSet<SearchField> searchFields = (SortedSet<SearchField>) request.getAttribute("searchFields");
boolean searchDone = StringUtil.getBooleanValue((String)request.getAttribute("searchDone"));

String queryValue = request.getAttribute("query") != null ?  (String)request.getAttribute("query") : "";
if(!main){
  // no form search in Main
%>

<div class="zoneNavAndSearch">
          <div id="search">
<form id="searchform" name="searchform" method="post" action="<%=routerUrl + "getSearchResult"%>" >
	<div>
	<label class="txtlibform" for="query"><fmt:message key="whitePages.keywords"/></label>
        <input type="text" id="query" value="<%=queryValue%>" size="60" name="query" class="ac_input champTexte"/>
        <p class="txtexform"><fmt:message key="whitePages.keywordssample"/></p>
    </div>

  <div id="used_pdc"></div>
  <input type="hidden" name="AxisValueCouples"/>

<%
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
	for (SearchField searchField : searchFields) {
		String fieldName = searchField.getLabel();
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
<% } %>
<div  class="sp_buttonPane" id="btnValidSearch">
  <a  class="sp_button" href="javascript:dynamicSearchLaunch()"><fmt:message key="whitePages.button.search"/></a>
</div>
</form>
</div>
</div>
<% } %>
<% if(isAdmin) { %>
<view:areaOfOperationOfCreation/>
<% } %>
<div class="listinUsers">
<form name="liste_card" action="">
  <ol class="message_list aff_colonnes">
<%
if(cards != null && !cards.isEmpty()){
  for (Card card : cards) {
    UserRecord userRecord = card.readUserRecord();
	String lastName = userRecord.getField("LastName").getValue(language);
	String firstName = userRecord.getField("FirstName").getValue(language);
	String email = userRecord.getField("Mail").getValue(language);
%>
           <li class="intfdcolor">
           <% if(isAdmin){ %>
			 <input type="checkbox" value="<%=card.getPK().getId()%>" name="checkedCard" class="check" />
           <% } %>
                 <div class="profilPhoto">
			<a href="javascript:consult(<%=card.getPK().getId()%>)">
			<view:image css="defaultAvatar" alt="viewUser" src="<%=userRecord.getUserDetail().getAvatar()%>" type="avatar"/>
			</a>
                 </div>
                 <div class="info">
                   <ul>
                     <li class="userName"><a href="javascript:consult(<%=card.getPK().getId()%>)"><%=lastName%>&nbsp;<%=firstName%></a></li>
                     <li class="infoConnection">
                     <% if(userRecord.isConnected()) { %>
			<img alt="connected" src="<%=m_context%>/util/icons/online.gif" />
                     <% } else { %>
			<img alt="deconnected" src="<%=m_context%>/util/icons/offline.gif" />
                     <% } %>
                     </li>
                     <li class="infoVisibility">
                     <%
                     if (card.getHideStatus() == 1) {// hide card
                     %>
			<img title="Masque" alt="Masque" src="<%=m_context%>/util/icons/masque.gif" />
                     <% } else { %>
			<img title="Visible" alt="Visible" src="<%=m_context%>/util/icons/visible.gif" />
                     <% } %>
                     </li>
                     <li class="userMail">
			<a href="#" class="notification" rel="<%=card.getUserId()%>,<%=lastName + " " + firstName%>"><%=email%></a>
                     </li>
                   </ul>
                 </div>
                <div class="action">
			  <a onclick="sp.messager.open(null, {recipientUsers: <%=card.getUserId()%>, recipientEdition: false});" class="link notification" href="#"><fmt:message key="whitePages.sendNotif"/></a> <a onclick="javascript:consult(<%=card.getPK().getId()%>)" class="link goToWhitepages" href="javascript:consult(<%=card.getPK().getId()%>)"><fmt:message key="whitePages.seeCard"/></a>
                </div>
               <br clear="all" />
            </li><%
  }
}else if(searchDone){
%>
<div class="inlineMessage">
	<fmt:message key="whitePages.nosearchresults"/>
</div>
<% } %>
		</ol>
	</form>
</div>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
	<view:progressMessage/>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.whitePages', ['silverpeas.services', 'silverpeas.directives']);
</script>

</body>
</html>
