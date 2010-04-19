<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
			response.setHeader("Pragma", "no-cache"); //HTTP 1.0
			response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%-- Import area --%>
<%@ page
	import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page
	import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page
	import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page
	import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page
	import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page
	import="com.stratelia.silverpeas.wysiwyg.control.WysiwygController"%>
<%@page import="java.util.List"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@page import="com.silverpeas.treeMenu.process.TreeHandler"%>
<%@page import="com.silverpeas.treeMenu.model.MenuConstants"%>

<%@ taglib uri="/WEB-INF/menuTree.tld" prefix="menuTree"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String language = (String) session.getAttribute("WYSIWYG_Language");
String userId = (String) session.getAttribute("WYSIWYG_UserId");
ResourceLocator message = new ResourceLocator(
					"com.stratelia.silverpeas.wysiwyg.multilang.wysiwygBundle",
					language);
String contextName = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<%@page import="com.stratelia.webactiv.beans.admin.ComponentInstLight"%><html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="<%=contextName %>/kmelia/jsp/styleSheets/kmelia.css">
<%
  out.println(gef.getLookStyleSheet());
%>
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
   mes =YAHOO.lang.JSON.stringify(<%=TreeHandler.ProcessMenu(request,MenuConstants.THEME_MENU_TYPE)%>);
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
	$.get('<%=contextName%>/RAjaxPublicationsListServlet', {Index:index,ComponentId:currentComponent,attachmentLink:1,IEFix:ieFix}, 
			function(data){
				$('#pubList').html(data);
			},"html");
}

function displayPublications(CompoId,topicId){
	var ieFix = new Date().getTime();
	$.get('<%=contextName%>/RAjaxPublicationsListServlet', {ComponentId:CompoId,TopicToLinkId:topicId,attachmentLink:1,IEFix:ieFix}, 
			function(data){
				$('#pubList').html(data);
			},"html");
}



function displayHomeMessage(){
	document.getElementById('pubList').innerHTML = '<p align="center" ><%=message.getString("storageFile.home.title")%></p> <p align="center"> <br><br><%=message.getString("storageFile.home.description")%> ';
}

function selectAttachment(url,img,label){
	<%if (request.getParameter("fieldname")!=null){ %>
		window.opener.insertAttachmentLink<%=request.getParameter("fieldname")%>(url,img,label);
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