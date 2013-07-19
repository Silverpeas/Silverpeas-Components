<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@page import="com.silverpeas.kmelia.SearchContext"%>

<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
String		rootId				= "0";

String 	profile			= (String) request.getAttribute("Profile");
String  translation 	= (String) request.getAttribute("Language");
boolean displayNBPublis = ((Boolean) request.getAttribute("DisplayNBPublis")).booleanValue();
Boolean rightsOnTopics  = (Boolean) request.getAttribute("RightsOnTopicsEnabled");
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");
int		currentPageIndex = (Integer) request.getAttribute("PageIndex");

SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
String query = "";
if (searchContext != null) {
  query = searchContext.getQuery();
}

String id 		= (String) request.getAttribute("CurrentFolderId");

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String language = kmeliaScc.getLanguage();

if (id == null) {
	id = rootId;
}

//For Drag And Drop
boolean dragAndDropEnable = kmeliaScc.isDragAndDropEnable();

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

String userId = kmeliaScc.getUserId();

String httpServerBase = GeneralPropertiesManager.getString("httpServerBase", m_sAbsolute);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/browseBarComplete.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript" src="<%=m_context%>/kmelia/jsp/javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js" />"></script>
<view:includePlugin name="userZoom"/>
<view:includePlugin name="datepicker" />
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<view:includePlugin name="notifier"/>
<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript" src="javaScript/searchInTopic.js"></script>
<script type="text/javascript" src="javaScript/publications.js"></script>

<style type="text/css">
.invisibleTopic {
	color: #BBB;
}
</style>

<script type="text/javascript">

  <%--The below triggered function has to be defined as soon as possible in HTML code in order to
  increase chances to perform the treatment when "menuRender" event is fired --%>
  $(document).ready(function() {
    $(document).bind('menuRendered', function(){

      $.i18n.properties({
        name: 'kmeliaBundle',
        path: webContext + '/services/bundles/com/silverpeas/kmelia/multilang/',
        language: '<%=language%>',
        mode: 'map'
      });

      displayTopicContent(<%=id%>);

      <% if (settings.getBoolean("DisplayDnDOnLoad", false)) { %>
      showDnD();
      <% } %>
      <% if (displaySearch.booleanValue()) { %>
      document.getElementById("topicQuery").focus();
      <% } %>
    });
  });

function topicGoTo(id) {
    closeWindows();
    displayTopicContent(id);
}

function showDnD() {
	<%
	ResourceLocator uploadSettings = new ResourceLocator("org.silverpeas.util.uploads.uploadSettings", "");
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

function getComponentLabel() {
	return "<%=EncodeHelper.javaStringToJsString(componentLabel)%>";
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

function getToValidateFolderId() {
	return "<%=KmeliaHelper.SPECIALFOLDER_TOVALIDATE%>";
}
</script>
</head>
<body id="kmelia" onunload="closeWindows()">
<div id="<%=componentId %>" class="<%=profile%>">
<%
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setI18N("GoToCurrentTopic", translation);
	
	//Display operations - following lines are mandatory to init menu correctly
	OperationPane operationPane = window.getOperationPane();
	operationPane.addOperation("", resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addCurrentNodeAsFavorite()");

    out.println(window.printBefore());
%>
<view:frame>
					<div id="subTopics"></div>
					
					<% if (displaySearch.booleanValue()) {
						Button searchButton = gef.getFormButton(resources.getString("GML.search"), "javascript:onClick=searchInTopic();", false); %>
						<div id="searchZone">
						<view:board>
						<table id="searchLine">
						<tr><td><div id="searchLabel"><%=resources.getString("kmelia.SearchInTopics") %></div>&nbsp;<input type="text" id="topicQuery" size="50" value="<%=query%>" onkeydown="checkSubmitToSearch(event)"/></td><td><%=searchButton.print() %></td></tr>
						</table>
						</view:board>
						</div>
					<% } %>
					
					<div id="topicDescription"></div>
					<view:areaOfOperationOfCreation/>
					
				<% if (dragAndDropEnable) { %>
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
				<% } %>
					<div id="pubList">
					<br/>
					<view:board>
					<br/><center><%=resources.getString("kmelia.inProgressPublications") %><br/><br/><img src="<%=resources.getIcon("kmelia.progress") %>"/></center><br/>
					</view:board>
					</div>
					<div id="footer" class="txtBaseline"></div>
		</view:frame>
	<%
		out.println(window.printAfter());
	%>

<form name="topicDetailForm" method="post">
	<input type="hidden" name="Id" value="<%=id%>"/>
	<input type="hidden" name="ChildId"/>
	<input type="hidden" name="Status"/>
	<input type="hidden" name="Recursive"/>
</form>

<form name="pubForm" action="ViewPublication" method="post">
	<input type="hidden" name="PubId"/>
	<input type="hidden" id="CheckPath" name="CheckPath"/>
</form>

<form name="fupload" action="fileUpload.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
	<input type="hidden" name="Action" value="initial"/>
</form>

<form name="updateChain" action="UpdateChainInit">
</form>
<script type="text/javascript">
var icons = new Object();
icons["permalink"] = "<%=resources.getIcon("kmelia.link")%>";
icons["operation.addTopic"] = "<%=resources.getIcon("kmelia.operation.addTopic")%>";
icons["operation.addPubli"] = "<%=resources.getIcon("kmelia.operation.addPubli")%>";
icons["operation.wizard"] = "<%=resources.getIcon("kmelia.operation.wizard")%>";
icons["operation.importFile"] = "<%=resources.getIcon("kmelia.operation.importFile")%>";
icons["operation.importFiles"] = "<%=resources.getIcon("kmelia.operation.importFiles")%>";
icons["operation.subscribe"] = "<%=resources.getIcon("kmelia.operation.subscribe")%>";
icons["operation.favorites"] = "<%=resources.getIcon("kmelia.operation.favorites")%>";

var params = new Object();
params["rightsOnTopic"] = <%=rightsOnTopics.booleanValue()%>;
params["i18n"] = <%=I18NHelper.isI18N%>;
params["nbPublisDisplayed"] = <%=displayNBPublis%>;

var searchInProgress = <%=searchContext != null%>;
var searchFolderId = "<%=id%>";

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
	changeStatus(getCurrentNodeId(), getCurrentTopicStatus());
}

function updateUIStatus(nodeId, newStatus) {
	setCurrentTopicStatus(newStatus);
	displayOperations(nodeId);
}

function displayTopicContent(id) {
	if (id != searchFolderId) {
		// search session is over
		searchInProgress = false;
	}
	
	if (!searchInProgress) {
		clearSearchQuery();
	}
	
	setCurrentNodeId(id);

	if (id == getToValidateFolderId() || id == "1") {
		$("#DnD").css({'display':'none'}); //hide dropzone
		$("#footer").css({'visibility':'hidden'}); //hide footer
		$("#searchZone").css({'display':'none'}); //hide search
		$("#subTopics").empty();

		if (id == getToValidateFolderId())	{
			hideOperations();
			displayPublications(id);

			//update breadcrumb
            removeBreadCrumbElements();
            addBreadCrumbElement("#", "<%=resources.getString("ToValidate")%>");
		} else {
			displayPublications(id);
			displayPath(id);
			displayOperations(id);
		}
	} else {
		if (searchInProgress) {
			doPagination(<%=currentPageIndex%>);
		} else {
			displayPublications(id);
		}
		displayPath(id);
		displayOperations(id);
		$("#searchZone").css({'display':'block'});
		displaySubTopics(id);
	}

	//display topic information
	displayTopicInformation(id);

	//display topic rich description
	displayTopicDescription(id);
	
	// location.href = location.href + "#pubList";
}
	
function displaySubTopics(id) {
	var sUrl = "<%=m_context%>/services/folders/<%=componentId%>/"+id+"/children?lang="+getTranslation();
	$.ajax(sUrl, {
		 type: 'GET', 
		 dataType : 'json',
		 async : false,
		 cache : false,
		 success : function(data){
			$("#subTopics").empty();
			$("#subTopics").append("<ul>");
			var basket = "";
			var tovalidate = "";
			$.each(data, function(i, folder) {
					var folderId = folder.attr["id"];
					if (folderId == "1") {
						basket = getSubFolder(folder);
					} else if (folderId == getToValidateFolderId()) {
						tovalidate = getSubFolder(folder);
					} else if (folderId != "2") {
						$("#subTopics ul").append(getSubFolder(folder));
					}
			});
			if (id == "0") {
				$("#subTopics ul").append(tovalidate);
				$("#subTopics ul").append(basket);
			}
			$("#subTopics").append("</ul>");
			$("#subTopics").append("<br clear=\"all\">");
		}
	});
}

function getSubFolder(folder) {
	var id = folder.attr["id"];
	var nbItems = folder.attr["nbItems"];
	var name = folder.data;
	var desc = folder.attr["description"];
	var str = '<li id="topic_'+id+'">';
	str += '<a href="#" onclick="topicGoTo(\''+id+'\')" ';
	if (id == getToValidateFolderId()) {
		str += 'class="toValidate"';
	} else if (id == "1") {
		str += 'class="trash"';
	}
	str += '>';
	str += '<strong>'+name+' ';
	if (typeof(nbItems) != "undefined") {
		str += '<span>'+nbItems+'</span>';
	}
	str += '</strong>';
	if (typeof(desc) != "undefined" && desc.length > 0) {
		str += '<span title="'+desc+'">'+desc+'</span>';
	}
	str += '</a>';
	str += '</li>';
	return str;
}

function getString(key) {
	return $.i18n.prop(key)
}
</script>
</div>
<div id="visibleInvisible-message" style="display: none;">
	<p>
	</p>
</div>
<div id="addOrUpdateNode" style="display: none;">
	<form name="topicForm" action="AddTopic" method="post">
       <table cellpadding="5" width="100%">
         <tr><td class="txtlibform"><fmt:message key="TopicPath"/> :</td>
           <td valign="top" id="path"></td>
         </tr>
         <%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
         <input type="hidden" id="<%=I18NHelper.HTMLHiddenRemovedTranslationMode %>" name="<%=I18NHelper.HTMLHiddenRemovedTranslationMode %>" value="false"/>
         <tr>
           <td class="txtlibform"><fmt:message key="TopicTitle"/> :</td>
           <td><input type="text" name="Name" id="folderName" size="60" maxlength="60"/>
           <input type="hidden" name="ParentId" id="parentId"/>
           <input type="hidden" name="ChildId" id="topicId"/>&nbsp;<img border="0" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
         </tr>
           
         <tr>
           <td class="txtlibform"><fmt:message key="TopicDescription" /> :</td>
           <td><input type="text" name="Description" id="folderDescription" size="60" maxlength="200"/></td>
         </tr>
           
         <% if (kmeliaScc.isNotificationAllowed()) { %>
           <tr>
             <td class="txtlibform" valign="top"><fmt:message key="TopicAlert" /> :</td>
             <td valign="top">
               <select name="AlertType">
                 <option value="NoAlert" selected="selected"><fmt:message key="NoAlert" /></option>
                 <option value="Publisher"><fmt:message key="OnlyPubsAlert" /></option>
                 <option value="All"><fmt:message key="AllUsersAlert" /></option>
               </select>
             </td>
           </tr>
         <% } %>
         <tr>
           <td colspan="2">( <img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField"/> )</td>
         </tr>
       </table>
     </form>
</div>
<view:progressMessage/>
</body>
</html>