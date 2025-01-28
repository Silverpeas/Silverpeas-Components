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
<%@page import="org.silverpeas.core.web.treemenu.process.TreeHandler"%>
<%@page import="org.silverpeas.core.web.treemenu.model.MenuConstants"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/menuTree" prefix="menuTree"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="fmr" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${sessionScope.WYSIWYG_Language}"/>
<view:setBundle basename="org.silverpeas.wysiwyg.multilang.wysiwygBundle"/>
<c:set value="${silfn:applicationURL()}" var="webContext"/>
<fmt:message key="Close" var="closeLabel"/>

<%@ page import="org.owasp.encoder.Encode" %>
<view:sp-page>
<view:sp-head-part>
<link rel="stylesheet" type="text/css" href="${webContext}/kmelia/jsp/styleSheets/kmelia.css">
<%-- load the css and js file used by tree menu --%>
<menuTree:head displayCssFile="true"
			   displayJavascriptFile="true"
			   displayIconsStyles="false"
			   contextName="${webContext}"/>

<script>
function returnHtmlEditor() {
  window.close();
}

const context = '${webContext}';
let currentNodeId;
let currentNodeIndex;
let currentComponent;
const menuType = '<%=MenuConstants.THEME_MENU_TYPE%>';

function buildTree() {
    let mes;
//create a new tree:
    tree = new YAHOO.widget.TreeView("treeDiv1");
    //turn dynamic loading on for entire tree:
    tree.setDynamicLoad(loadNodeData, 0);
    //get root node for tree:
	const root = tree.getRoot();

	//add child nodes for tree; our top level nodes are
   try {
	mes = [];
	mes = YAHOO.lang.JSON.stringify(<%=TreeHandler.processMenu(request,MenuConstants.THEME_MENU_TYPE, false)%>);
	mes = YAHOO.lang.JSON.parse(mes);
   } catch(x){
      notyError("JSON Parse failed: "+x);
	  return;
   }
   for (let i=0, j=mes.length; i<j; i++) {
	   const tempNode = new YAHOO.widget.TextNode(mes[i], root, false);
	   tempNode.multiExpand =false;
   }
    //render tree with these toplevel nodes; all descendants of these nodes
    //will be generated as needed by the dynamic loader.
    tree.draw();
  //action when a user click on a node
    tree.subscribe('clickEvent',function(oArgs) {
		currentComponent = oArgs.node.data.componentId;
		// highlight selected node
		const $currentNode = $("#ygtvcontentel"+currentNodeIndex);
		$currentNode.css({'font-weight':'normal'});
		setCurrentNodeId(oArgs.node.data.id);
		currentNodeIndex = oArgs.node.index;
		$currentNode.css({'font-weight':'bold'});
        //if node is a theme display the publications
        if (oArgs.node.data.componentId !== undefined && oArgs.node.data.nodeType === 'THEME') {
            displayPublications(oArgs.node.data.componentId,getCurrentNodeId());
        } else {
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
	const ieFix = new Date().getTime();
	$.get('${webContext}/RAjaxPublicationsListServlet', {
		Index: index,
		NbItemsPerPage: nbItemsPerPage,
		ComponentId: currentComponent,
		attachmentLink: 1,
		IEFix: ieFix
	}, function(data) {
		$('#pubList').html(data);
	}, "html");
}

function displayPublications(CompoId,topicId){
	const ieFix = new Date().getTime();
	$.get('${webContext}/RAjaxPublicationsListServlet', {
		ComponentId: CompoId,
		Id: topicId,
		attachmentLink: 1,
		IEFix: ieFix
	}, function(data) {
		$('#pubList').html(data);
	}, "html");
}

function displayHomeMessage(){
	document.getElementById('pubList').innerHTML =
			'<p align="center" ><fmt:message key="storageFile.home.title"/></p>' +
			'<p	align="center"><br><br><fmt:message key="storageFile.home.description"/> ';
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

</view:sp-head-part>
<view:sp-body-part cssClass="yui-skin-sam">

	<table class="dimensionTable">
		<th></th>
		<tr style="vertical-align: top">
			<td class="firstTd">
				<div id="treeDiv1" class="treeDivDisplay"></div>
			</td>
			<td class="secondTd">
				<div id="pubList" class="publistDisplay">
					<p style="text-align: center"><fmt:message key="storageFile.home.title"/></p>
					<p style="text-align: center"><br><br>
						<fmt:message key="storageFile.home.description"/>
					</p>
				</div>
				<div style="text-align: center">
					<view:buttonPane>
						<view:button label="${closeLabel}"
									 action="javascript:onClick=returnHtmlEditor()"/>
					</view:buttonPane>
				</div>
			</td>
		</tr>
	</table>

</view:sp-body-part>
</view:sp-page>