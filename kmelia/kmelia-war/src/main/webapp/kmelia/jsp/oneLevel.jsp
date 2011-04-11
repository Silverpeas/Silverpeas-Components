<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>

<%
String		rootId				= "0";

String 	profile			= (String) request.getAttribute("Profile");
String  translation 	= (String) request.getAttribute("Language");
Boolean rightsOnTopics  = (Boolean) request.getAttribute("RightsOnTopicsEnabled");
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");

TopicDetail currentTopic 		= (TopicDetail) request.getAttribute("CurrentTopic");

String 		pathString 			= (String) request.getAttribute("PathString");

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String id = currentTopic.getNodeDetail().getNodePK().getId();
String language = kmeliaScc.getLanguage();

if (id == null) {
	id = rootId;
}

//For Drag And Drop
boolean dragAndDropEnable = kmeliaScc.isDragAndDropEnable();

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

String userId = kmeliaScc.getUserId();

ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8"/>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript" src="<%=m_context%>/kmelia/jsp/javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript" src="javaScript/searchInTopic.js"></script>

<style type="text/css">
.invisibleTopic {
	color: #BBB;
}
</style>

<script type="text/javascript">
function topicGoTo(id) {
    closeWindows();
    displayTopicContent(id);
}

function showDnD() {
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
	if (profile.equals("publisher") || profile.equals("writer")) { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeNormal_<%=language%>.html','<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&Draft=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } else { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeNormal_<%=language%>.html','<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&Draft=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } %>
}

function getWebContext() {
	return "<%=m_context%>";
}

function getComponentId() {
	return "<%=componentId%>";
}

function getLanguage() {
	return "<%=language%>"; 
}

function getPubIdToHighlight() {
	return "<%=pubIdToHighlight%>";
}

function getTranslation() {
	return "<%=translation%>";
}
</script>
</head>
<body id="kmelia" onunload="closeWindows()">
<div id="<%=componentId %>">
<%
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setI18N("GoToCurrentTopic", translation);
	
	//Display operations - following lines are mandatory to init menu correctly
	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation("", resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addCurrentNodeAsFavorite()");

	Frame frame = gef.getFrame();

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
					<div id="subTopics"></div>
					
					<% if (displaySearch.booleanValue()) {
					  	Board board = gef.getBoard();
						Button searchButton = gef.getFormButton(resources.getString("GML.search"), "javascript:onClick=searchInTopic();", false);
						out.println("<div id=\"searchZone\">");
						out.println(board.printBefore());
						out.println("<table id=\"searchLine\">");
						out.println("<tr><td><div id=\"searchLabel\">"+resources.getString("kmelia.SearchInTopics")+"</div>&nbsp;<input type=\"text\" id=\"topicQuery\" size=\"50\" onkeydown=\"checkSubmitToSearch(event)\"/></td><td>"+searchButton.print()+"</td></tr>");
						out.println("</table>");
						out.println(board.printAfter());
						out.println("</div>");
					} %>
					
					<div id="topicDescription"></div>
					
				<%
					  if (dragAndDropEnable)
					  {
						%>
						<div id="DnD">
						<table width="98%" cellpadding="0" cellspacing="0"><tr><td align="right">
						<a href="javascript:showDnD()" id="dNdActionLabel"><%=resources.getString("GML.DragNDropExpand")%></a>
						</td></tr></table>
						<table width="100%" border="0" id="DropZone">
						<tr>
						<%
							boolean appletDisplayed = false;
							if (kmeliaScc.isDraftEnabled() && kmeliaScc.isPdcUsed() && kmeliaScc.isPDCClassifyingMandatory())
							{
								//Do not display applet in normal mode.
								//Only display applet in draft mode
							}
							else
							{
								appletDisplayed = true;
						%>
								<td>
									<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
								</td>
						<% } %>
						<% if (kmeliaScc.isDraftEnabled()) {
							if (appletDisplayed)
								out.println("<td width=\"5%\">&nbsp;</td>");
							%>
							<td>
								<div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px width:100%"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
							</td>
						<% } %>
						</tr></table>
						</div>
				<% }  %>
					<div id="pubList">
					<%
						 Board board = gef.getBoard();
						 out.println("<br/>");
						 out.println(board.printBefore());
						 out.println("<br/><center>"+resources.getString("kmelia.inProgressPublications")+"<br/><br/><img src=\""+resources.getIcon("kmelia.progress")+"\"/></center><br/>");
						 out.println(board.printAfter());
					 %>
					</div>
					<div id="footer" class="txtBaseline"></div>
	<%
		out.println(frame.printAfter());
		out.println(window.printAfter());
	%>

<form name="topicDetailForm" method="post">
	<input type="hidden" name="Id" value="<%=id%>"/>
	<input type="hidden" name="Path" value="<%=EncodeHelper.javaStringToHtmlString(pathString)%>"/>
	<input type="hidden" name="ChildId"/>
	<input type="hidden" name="Status"/>
	<input type="hidden" name="Recursive"/>
</form>

<form name="pubForm" action="ViewPublication" method="post">
	<input type="hidden" name="PubId"/>
	<input type="hidden" name="CheckPath"/>
</form>

<form name="fupload" action="fileUpload.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
	<input type="hidden" name="Action" value="initial"/>
</form>

<form name="updateChain" action="UpdateChainInit">
</form>
<script type="text/javascript">
var labels = new Object();
labels["ConfirmDeleteTopic"] = "<%=EncodeHelper.javaStringToJsString(resources.getString("ConfirmDeleteTopic"))%>";
labels["ConfirmFlushTrashBean"] = "<%=EncodeHelper.javaStringToJsString(kmeliaScc.getString("ConfirmFlushTrashBean"))%>";
labels["ToValidate"] = "<%=EncodeHelper.javaStringToJsString(resources.getString("ToValidate"))%>";
labels["topic.info"] = "<%=EncodeHelper.javaStringToJsString(resources.getString("kmelia.topic.info"))%>";

labels["operation.pdc"] = "<%=resources.getString("GML.PDCParam")%>";
labels["operation.templates"] = "<%=resources.getString("kmelia.ModelUsed")%>";
labels["operation.exportTopic"] = "<%=resources.getString("kmelia.ExportTopic")%>";
labels["operation.exportComponent"] = "<%=resources.getString("kmelia.ExportComponent")%>";
labels["operation.exportPDF"] = "<%=resources.getString("kmelia.ExportPDF")%>";
labels["operation.addTopic"] = "<%=resources.getString("CreerSousTheme")%>";
labels["operation.updateTopic"] = "<%=resources.getString("ModifierSousTheme")%>";
labels["operation.deleteTopic"] = "<%=resources.getString("SupprimerSousTheme")%>";
labels["operation.sortTopics"] = "<%=resources.getString("kmelia.SortTopics")%>";
labels["operation.copy"] = "<%=resources.getString("GML.copy")%>";
labels["operation.cut"] = "<%=resources.getString("GML.cut")%>";
labels["operation.paste"] = "<%=resources.getString("GML.paste")%>";
labels["operation.visible2invisible"] = "<%=resources.getString("TopicVisible2Invisible")%>";
labels["operation.invisible2visible"] = "<%=resources.getString("TopicInvisible2Visible")%>";
labels["operation.wysiwygTopic"] = "<%=resources.getString("TopicWysiwyg")%>";
labels["operation.addPubli"] = "<%=resources.getString("PubCreer")%>";
labels["operation.wizard"] = "<%=resources.getString("kmelia.Wizard")%>";
labels["operation.importFile"] = "<%=resources.getString("kmelia.ImportFile")%>";
labels["operation.importFiles"] = "<%=resources.getString("kmelia.ImportFiles")%>";
labels["operation.sortPublis"] = "<%=resources.getString("kmelia.OrderPublications")%>";
labels["operation.updateChain"] = "<%=resources.getString("kmelia.updateByChain")%>";
labels["operation.subscribe"] = "<%=resources.getString("SubscriptionsAdd")%>";
labels["operation.favorites"] = "<%=resources.getString("FavoritesAdd1")%> <%=resources.getString("FavoritesAdd2")%>";
labels["operation.emptyTrash"] = "<%=resources.getString("EmptyBasket")%>";

var icons = new Object();
icons["permalink"] = "<%=resources.getIcon("kmelia.link")%>";

var params = new Object();
params["rightsOnTopic"] = <%=rightsOnTopics.booleanValue()%>;

function getComponentPermalink() {
	return "<%=URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentId)%>";
}

function copyCurrentNode()	{
	top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+getCurrentNodeId();
}

function cutCurrentNode() {
	top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+getCurrentNodeId();
}

function changeCurrentTopicStatus() {
	changeStatus(getCurrentNodeId());
}

function displayTopicContent(id) {
	clearSearchQuery();
	setCurrentNodeId(id);

	if (id == "tovalidate" || id == "1") {
		$("#DnD").css({'display':'none'}); //hide dropzone
		$("#footer").css({'visibility':'hidden'}); //hide footer
		$("#searchZone").css({'display':'none'}); //hide search
		$("#subTopics").empty();

		if (id == "tovalidate")	{
			$("#menutoggle").css({'display':'none'}); //hide operations
			displayPublicationsToValidate();

			//update breadcrumb
            removeBreadCrumbElements();
            addBreadCrumbElement("#", labels["ToValidate"]);
		} else {
			displayPublications(id);
			displayPath(id);
			displayOperations(id);
		}
	} else {
		displayPublications(id);
		displayPath(id);
		displayOperations(id);
		if (id != "0" || <%=kmeliaScc.getNbPublicationsOnRoot() == 0%>) {
			$("#searchZone").css({'display':'block'});
		} else if (<%=kmeliaScc.getNbPublicationsOnRoot() != 0%>) {
			$("#searchZone").css({'display':'none'}); //hide search
		}
		displaySubTopics(id);
	}

	//display topic information
	displayTopicInformation(id);

	//display topic rich description
	displayTopicDescription(id);
}
	
function displaySubTopics(id) {
	var sUrl = "<%=m_context%>/KmeliaJSONServlet?Action=GetSubTopics&ComponentId=<%=componentId%>&Language=<%=language%>&IEFix="+new Date().getTime()+"&Id="+id;
	$.getJSON(sUrl, function(data){
		$("#subTopics").empty();
		$("#subTopics").append("<ul>");
		var basket = "";
		var tovalidate = "";
		$.each(data, function(i, topic) {
				if (topic.id == "1") {
					basket = getSubTopic(topic);
				} else if (topic.id == "tovalidate") {
					tovalidate = getSubTopic(topic);
				} else if (topic.id != "2") {
					$("#subTopics ul").append(getSubTopic(topic));
				}
		});
		if (id == "0") {
			$("#subTopics ul").append(tovalidate);
			$("#subTopics ul").append(basket);
		}
		$("#subTopics").append("</ul>");
		$("#subTopics").append("<br clear=\"all\">");
		
	});
}

function getSubTopic(topic) {
	var str = '<li>';
	str += '<a href="#" onclick="topicGoTo(\''+topic.id+'\')" ';
	if (topic.id == "tovalidate") {
		str += 'class="toValidate"';
	} else if (topic.id == "1") {
		str += 'class="trash"';
	}
	str += '>';
	str += '<strong>'+topic.name+' ';
	if (topic.nbObjects != -1) {
		str += '<span>'+topic.nbObjects+'</span>';
	}
	str += '</strong>';
	if (typeof(topic.description) != "undefined" && topic.description.length > 0) {
		str += '<span title="'+topic.description+'">'+topic.description+'</span>';
	}
	str += '</a>';
	str += '</li>';
	return str;
}
</script>
<script type="text/javascript">
$(document).ready(function() {

	displayTopicContent(<%=id%>);

	<% if (settings.getBoolean("DisplayDnDOnLoad", false)) { %>
		showDnD();
	<% } %>
	<% if (displaySearch.booleanValue()) { %>
		document.getElementById("topicQuery").focus();
    <% } %>

});
</script>
</div>
<view:progressMessage/>
</body>
</html>