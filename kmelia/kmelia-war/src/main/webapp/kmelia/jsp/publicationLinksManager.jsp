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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="com.silverpeas.treeMenu.process.TreeHandler"%>
<%@page import="com.silverpeas.treeMenu.model.MenuConstants"%>
<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/menuTree" prefix="menuTree"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<% 
String pubId = kmeliaScc.getSessionPublication().getDetail().getPK().getId();
String pubComponentId = kmeliaScc.getSessionPublication().getDetail().getPK().getComponentName();
Button closeButton = gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
Button linkButton = gef.getFormButton(resources.getString("GML.linkTo"), "javaScript:linkTo();", false);
String closeWindow="";
if(request.getAttribute("NbLinks")!=null){
  closeWindow ="onload=\"closeAndReturn('"+pubId+"');\"";
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<style type="text/css">
#pubList .selection input {
	display: block;
}
</style>
<%-- load the css and js file used by tree menu --%>
<menuTree:head displayCssFile="true" displayJavascriptFile="true" displayIconsStyles="true" contextName="<%=m_context%>"></menuTree:head>
<%-- personalizable Javascript for  YUI treeView menu --%>
<script type="text/javascript" src="<%=m_context %>/util/javaScript/treeMenu/menu.js"></script>

<Script language="JavaScript">
var context ='<%=m_context %>';
var currentNodeId;
var currentNodeIndex;
var currentComponent;
var menuType='<%=MenuConstants.SEE_ALSO_MENU_TYPE%>';
function buildTree() {
    //create a new tree:
    tree = new YAHOO.widget.TreeView("treeDiv1");
    //turn dynamic loading on for entire tree:
    tree.setDynamicLoad(loadNodeData, 0);
    //get root node for tree:
    var root = tree.getRoot();
   
    //add child nodes for tree; our top level nodes are  
   try{
   var mes = [];
   mes =YAHOO.lang.JSON.stringify(<%=TreeHandler.ProcessMenu(request,MenuConstants.SEE_ALSO_MENU_TYPE)%>);
    mes = YAHOO.lang.JSON.parse(mes);
   }catch(x){
      alert("JSON Parse failed: "+x);
	  return;
	}
   for (var i=0, j=mes.length; i<j; i++) {
         var tempNode = new YAHOO.widget.TextNode(mes[i], root, false);
			tempNode.multiExpand =false;
    } 
    //render tree with these toplevel nodes; all descendants of these nodes
    //will be generated as needed by the dynamic loader.
    tree.draw();
    //action when a user click on a node
    tree.subscribe('clickEvent',function(oArgs) { 
		currentComponent = oArgs.node.data.componentId;
		// highlight selected node
		$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'normal'});
		setCurrentNodeId(oArgs.node.data.id);
		currentNodeIndex = oArgs.node.index;
		$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'bold'});
        //if node is a theme display the publications
        if(oArgs.node.data.componentId!='undefined' && oArgs.node.data.nodeType=='THEME'){
            displayPublications(oArgs.node.data.componentId,getCurrentNodeId());
            //displays the publication localized at the root of a theme tracker
        }else if (oArgs.node.data.nodeType=='COMPONENT' && oArgs.node.data.id.indexOf('kmelia')==0){
    		displayPublications(oArgs.node.data.id,"0");
        }else{
        	displayHomeMessage();
        }
	});
}


function getCurrentNodeId(){
	return currentNodeId;
}

function setCurrentNodeId(id){
	currentNodeId = id;
}

function doPagination(index){
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {PubId:<%=pubId%>,PubComponentName:'<%=pubComponentId%>',Index:index,ComponentId:currentComponent,ToLink:1,IEFix:ieFix}, 
			function(data){
				$('#pubList').html(data);
			},"html");
}

function closeAndReturn(pubId) {
    window.opener.location.replace("SeeAlso?PubId="+pubId);
    window.close();
}

function displayPublications(CompoId,topicId){
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {PubId:<%=pubId%>,PubComponentName:'<%=pubComponentId%>',ComponentId:CompoId,TopicToLinkId:topicId,ToLink:1,IEFix:ieFix}, 
			function(data){
				$('#pubList').html(data);
			},"html");
}

function sendPubId(pubId,checked){

	var action;
	
	if(checked){
		action="Action=bindToPub";
	}else{
		action="Action=unbindFromPub";
	}
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/KmeliaAJAXServlet?'+action, {TopicToLinkId:pubId,IEFix:ieFix});
}

function linkTo(){
	location.href="AddLinksToPublication?PubId=<%=pubId%>"
}

function displayHomeMessage(){
	document.getElementById('pubList').innerHTML = '<p align="center" ><%= kmeliaScc.getString("kmelia.linkManager.home.title")%></p> <p align="center"> <br><br><%=kmeliaScc.getString("kmelia.linkManager.home.description") %>';
}

</script>
</head>
<body class="yui-skin-sam" <%=closeWindow%> > 
<table class="dimensionTable">
	<tr valign="top" >
		<td class="firstTd">
			<div id="treeDiv1" class="treeDivDisplay"> </div>
		</td>
		<td class="secondTd">	
			<div id="pubList" class="publistDisplay"><p align="center" ><%= kmeliaScc.getString("kmelia.linkManager.home.title")%></p> <p align="center"> <br><br><%=kmeliaScc.getString("kmelia.linkManager.home.description") %> </p></div>
			<div align="center">  
			<% ButtonPane buttonPane = gef.getButtonPane();
              buttonPane.addButton(closeButton);
              buttonPane.addButton(linkButton);
              out.println(buttonPane.print());%>          
              </div>
		</td>
	</tr>
</table>
</body>
</html>