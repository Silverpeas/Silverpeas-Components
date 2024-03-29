<%--

    Copyright (C) 2000 - 2024 Silverpeas

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

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
			response.setHeader("Pragma", "no-cache"); //HTTP 1.0
			response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%-- Import area --%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%@page import="org.silverpeas.core.web.treemenu.process.TreeHandler"%>
<%@page import="org.silverpeas.core.web.treemenu.model.MenuConstants"%>

<%@ taglib uri="http://www.silverpeas.com/tld/menuTree" prefix="menuTree"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String language = (String) session.getAttribute("WYSIWYG_Language");
LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.wysiwyg.multilang.wysiwygBundle", language);
String contextName = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
%>

<%@ page import="org.silverpeas.kernel.bundle.LocalizationBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="<%=contextName %>/kmelia/jsp/styleSheets/kmelia.css">
<view:looknfeel/>
<%-- load the css and js file used by tree menu --%>
<menuTree:head displayCssFile="true" displayJavascriptFile="true" displayIconsStyles="false" contextName="<%=contextName%>"></menuTree:head>

<script language="JavaScript" type="text/javascript">
function returnHtmlEditor() {
  window.close();
}

var context ='<%=contextName %>';
var currentNodeId;
var currentNodeIndex;
var currentComponent;
var menuType='<%=MenuConstants.THEME_MENU_TYPE%>';
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
   mes =YAHOO.lang.JSON.stringify(<%=TreeHandler.processMenu(request,MenuConstants.THEME_MENU_TYPE)%>);
    mes = YAHOO.lang.JSON.parse(mes);
   }catch(x){
      notyError("JSON Parse failed: "+x);
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

function showPublicationOperations() {
  // not implemented but necessary to prevent javascript errors
}

function hidePublicationOperations () {
  // not implemented but necessary to prevent javascript errors
}

function doPagination(index, nbItemsPerPage){
	var ieFix = new Date().getTime();
	$.get('<%=contextName%>/RAjaxPublicationsListServlet', {Index:index,NbItemsPerPage:nbItemsPerPage,ComponentId:currentComponent,attachmentLink:1,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function displayPublications(CompoId,topicId){
	var ieFix = new Date().getTime();
	$.get('<%=contextName%>/RAjaxPublicationsListServlet', {ComponentId:CompoId,Id:topicId,attachmentLink:1,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}



function displayHomeMessage(){
	document.getElementById('pubList').innerHTML = '<p align="center" ><%=message.getString("storageFile.home.title")%></p> <p align="center"> <br><br><%=message.getString("storageFile.home.description")%> ';
}

function selectAttachment(url,img,label){
	<%if (request.getParameter("fieldname")!=null){ %>
		window.opener.insertAttachmentLink<%=Encode.forUriComponent(request.getParameter("fieldname"))%>(url,img,label);
	<%} else {%>
		window.opener.insertAttachmentLink(url,img,label);
	<%} %>
	window.close();
}

</script>

</head>
<body class="yui-skin-sam">

<table class="dimensionTable">
	<tr valign="top">
		<td class="firstTd">
		<div id="treeDiv1" class="treeDivDisplay"></div>
		</td>
		<td class="secondTd">
		<div id="pubList" class="publistDisplay">
		<p align="center"><%=message.getString("storageFile.home.title")%></p>
		<p align="center"><br>
		<br><%=message.getString("storageFile.home.description")%></p>
		</div>
		<div align="center">
		<%
		  Window window = gef.getWindow();
					Frame frame = gef.getFrame();
					ButtonPane buttonPane = gef.getButtonPane();
					Button button = gef.getFormButton(message.getString("Close"),
							"javascript:onClick=returnHtmlEditor()", false);
					buttonPane.addButton(button);
					out.println("<center><br>" + buttonPane.print() + "</center>");
		%>
		</div>
		</td>
	</tr>
</table>
</body>
</html>