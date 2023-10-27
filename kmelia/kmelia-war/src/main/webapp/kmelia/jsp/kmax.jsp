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

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkKmelia.jsp" %>
<%@ include file="kmax_axisReport.jsp" %>

<% 
String 	action 		= request.getParameter("Action");
String 	profile 	= request.getParameter("Profile");
String 	translation = (String) request.getAttribute("Language");

//Icons
String publicationAddSrc 	= m_context + "/util/icons/create-action/add-publication.png";
String publicationSrc 		= m_context + "/util/icons/publication.gif";
String unbalancedSrc 		= m_context + "/util/icons/kmelia_declassified.gif";
String topicBasketSrc		= m_context + "/util/icons/pubTrash.gif";
String pubToValidateSrc		= m_context + "/util/icons/publicationstoValidate.gif";
String exportComponentSrc	= m_context + "/util/icons/exportComponent.gif";

if (action == null) {
	action = "KmaxView";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.kmax">
<head>
<title></title>
<view:looknfeel/>
<view:includePlugin name="toggle"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>
<script type="text/javascript" src="javaScript/publications.js"></script>
<script type="text/javascript">
<!--
var favoriteWindow = window;
var topicUpdateWindow = window;
var topicAddWindow = window;
var exportComponentWindow = window;

function search() {
    var criterias = "";
    var timeCriteria = "X";
	<% if (kmeliaScc.isTimeAxisUsed()) { %>
	    timeCriteria = $("#timeAxis").val();
	<% } %>
  $(".axis").each(function() {
    var val = $(this).val();
    if (val.length != 0) {
      if (criterias.length != 0) {
        criterias += ",";
      }
      truc = val.split("|");
      criterias += truc[0];
    }
  });
    if (criterias.length == 0) {
      jQuery.popup.error("Vous devez sélectionnez au moins un axe !");
    } else {
      document.managerForm.TimeCriteria.value = timeCriteria;
      document.managerForm.SearchCombination.value = criterias;
      document.managerForm.action = "KmaxSearch";
      document.managerForm.submit();
    }
}

function publicationAdd(){
	if (favoriteWindow.name=="favoriteWindow")
		favoriteWindow.close();
	if (topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();
	if (topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
	document.pubForm.action = "NewPublication";
	document.pubForm.submit();
}

function publicationGoTo(id){
	if (favoriteWindow.name=="favoriteWindow")
		favoriteWindow.close();
	if (topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();
	if (topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
	document.pubForm.action = "ViewPublication";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function viewUnbalanced() {
  document.managerForm.action = "KmaxViewUnbalanced";
  document.managerForm.submit();
}

function viewBasket() {
  document.managerForm.action = "KmaxViewBasket";
  document.managerForm.submit();
}

function viewToValidate() {
	document.managerForm.action = "KmaxViewToValidate";
	document.managerForm.submit();
}

function doPagination(index, nbItemsPerPage) {
	var selectedPublicationIds = getSelectedPublicationIds();
	var notSelectedPublicationIds = getNotSelectedPublicationIds();
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:index,NbItemsPerPage:nbItemsPerPage,ComponentId:'<%=componentId%>',SelectedPubIds:selectedPublicationIds,NotSelectedPubIds:notSelectedPublicationIds,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function sortGoTo(selectedIndex) {
	if (selectedIndex != 0 && selectedIndex != 1) {
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:0,Sort:sort,ComponentId:'<%=componentId%>',IEFix:ieFix}, 
			function(data){
				$('#pubList').html(data);
			},"html");
		return;
	}
}

function init()
{
	var toValidate = "0";
	<% if ("KmaxViewToValidate".equals(action)) { %>
		toValidate = "1";
	<% } %>
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:0,ComponentId:'<%=componentId%>',ToValidate:toValidate,IEFix:ieFix}, 
			function(data){
				$('#pubList').html(data);
			},"html");
}

function exportComponent() {
	exportComponentWindow = SP_openWindow("ExportTopic","exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

function exportPublications() {
	exportComponentWindow = SP_openWindow("ExportTopic?TopicId=dummy","exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}
-->
</script>
</head>
<body id="<%=componentId %>" class="kmax" onload="init()">
<%
Window window = gef.getWindow();

BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "KmaxMain");
browseBar.setI18N("KmaxMain", translation);

if (action.equals("KmaxView")) {
    
    //Display operations by profile
    if (profile.equals("admin") || profile.equals("publisher") || profile.equals("writer")) {
      OperationPane operationPane = window.getOperationPane();
      operationPane.addOperationOfCreation(publicationAddSrc, kmeliaScc.getString("PubCreer"),
          "javascript:onClick=publicationAdd()");
      operationPane.addLine();
      operationPane.addOperation(unbalancedSrc, kmeliaScc.getString("PubDeclassified"),
          "javascript:onClick=viewUnbalanced()");
      operationPane.addOperation(topicBasketSrc, kmeliaScc.getString("PubBasket"),
          "javascript:onClick=viewBasket()");

      if (profile.equals("admin") || profile.equals("publisher")) {
        operationPane.addLine();
        operationPane.addOperation(pubToValidateSrc, kmeliaScc.getString("ToValidate"),
            "javascript:onClick=viewToValidate()");
      }

      if (kmeliaScc.isExportApplicationAllowed(kmeliaScc.getHighestSilverpeasUserRole()) ||
          kmeliaScc.isExportPublicationAllowed(kmeliaScc.getHighestSilverpeasUserRole())) {
        operationPane.addLine();
      }
      if (kmeliaScc.isExportApplicationAllowed(kmeliaScc.getHighestSilverpeasUserRole())) {
        operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportComponent()");
      }
      if (kmeliaScc.isExportPublicationAllowed(kmeliaScc.getHighestSilverpeasUserRole())) {
        operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmelia.operation.exportSelection"),
            "javascript:onclick=exportPublications()");
      }
    }
    TabbedPane tabbedPane = gef.getTabbedPane();
    if (profile.equals("admin")) {
        tabbedPane.addTab(kmeliaScc.getString("Consultation"), "#", true);
        tabbedPane.addTab(kmeliaScc.getString("Management"), "KmaxAxisManager", false);
    }

    out.println(window.printBefore());
%>
<view:componentInstanceIntro componentId="<%=componentId%>" language="<%=translation%>"/>
<%
    if (profile.equals("admin")) {
    	out.println(tabbedPane.print());
  	}
%>
<view:areaOfOperationOfCreation/>
<%    
    out.println(displayAxisToUsers(kmeliaScc, gef, translation));

    out.println(window.printAfter());

} else if (action.equals("KmaxSearchResult")) {

    browseBar.setI18N("KmaxSearchResult", translation);

	List combination = kmeliaScc.getSessionCombination();
	String timeCriteria = kmeliaScc.getSessionTimeCriteria();
		  
    //Display operations by profile
    if (profile.equals("admin") || profile.equals("publisher") || profile.equals("writer")) {
			OperationPane operationPane = window.getOperationPane();
			operationPane.addOperationOfCreation(publicationAddSrc, kmeliaScc.getString("PubCreer"), "javascript:onClick=publicationAdd()");
			operationPane.addLine();
			operationPane.addOperation(unbalancedSrc, kmeliaScc.getString("PubDeclassified"), "javascript:onClick=viewUnbalanced()");
			//Basket
			operationPane.addOperation(topicBasketSrc, kmeliaScc.getString("PubBasket"), "javascript:onClick=viewBasket()");
			
			if (profile.equals("admin") || profile.equals("publisher")) {
		    	operationPane.addLine();
		        operationPane.addOperation(pubToValidateSrc, kmeliaScc.getString("ToValidate"), "javascript:onClick=viewToValidate()");
		    }
			if (kmeliaScc.isExportPublicationAllowed(kmeliaScc.getHighestSilverpeasUserRole()))
			{
				operationPane.addLine();
				operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmax.ExportPublicationsFound"), "javascript:onClick=exportPublications()");
			}
			
    }
      
      TabbedPane tabbedPane = gef.getTabbedPane();
      if (profile.equals("admin")) {
        tabbedPane.addTab(kmeliaScc.getString("Consultation"), "KmaxView", true);
        tabbedPane.addTab(kmeliaScc.getString("Management"), "KmaxAxisManager", false);
      }

      out.println(window.printBefore());
      
      if (profile.equals("admin")) {
          out.println(tabbedPane.print());
      }
%>
	  <view:areaOfOperationOfCreation/>
	  <view:frame>
<%            
      out.println(displayAxisCombinationToUsers(kmeliaScc, gef, combination, timeCriteria, translation));
%>
	<div id="pubList">
	<br/>
	<view:board>
      <br/><center><%=resources.getString("kmelia.inProgressPublications") %><br/><br/><img src="<%=resources.getIcon("kmelia.progress") %>"/></center>
	</view:board>
	</div>
	</view:frame>
<%
      out.println(window.printAfter());

} else if ("KmaxViewUnbalanced".equals(action) || "KmaxViewBasket".equals(action) || "KmaxViewToValidate".equals(action)) {
	  
  	if ("KmaxViewUnbalanced".equals(action)) {
  		browseBar.setExtraInformation(kmeliaScc.getString("PubDeclassified"));
  	} else if ("KmaxViewBasket".equals(action)) {
  	  	browseBar.setExtraInformation(kmeliaScc.getString("PubBasket"));
  	} else {
  	  	browseBar.setExtraInformation(kmeliaScc.getString("ToValidate"));
  	}
  	  
	browseBar.setI18N(action, translation);

	Frame frame = gef.getFrame();
	out.println(window.printBefore());
	out.println(frame.printBefore());
	
	Board board = gef.getBoard();
	 out.println("<div id=\"pubList\">");
	 out.println("<br/>");
	 out.println(board.printBefore());
	 out.println("<br/><center>"+resources.getString("kmelia.inProgressPublications")+"<br/><br/><img src=\""+resources.getIcon("kmelia.progress")+"\"/></center><br/>");
	 out.println(board.printAfter());
    out.println("</div>");
	
	out.println(frame.printAfter());
	out.println(window.printAfter());
}
%>
<form name="managerForm" action="KmaxAxisManager" method="post">
	<input type="hidden" name="AxisId"/>
	<input type="hidden" name="AxisName"/>
	<input type="hidden" name="AxisDescription"/>
	<input type="hidden" name="ComponentId"/>
	<input type="hidden" name="ComponentName"/>
	<input type="hidden" name="ComponentDescription"/>
	<input type="hidden" name="SearchCombination"/>
	<input type="hidden" name="TimeCriteria"/>
	<input type="hidden" name="Id"/>
</form>

<form name="pubForm" method="GET" action="">
	<input type="hidden" name="PubId"/>
</form>

<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.kmax', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>