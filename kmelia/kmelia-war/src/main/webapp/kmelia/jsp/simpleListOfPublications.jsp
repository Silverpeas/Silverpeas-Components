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

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkKmelia.jsp" %>

<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.stratelia.webactiv.SilverpeasRole"%>
<%@page import="com.silverpeas.kmelia.SearchContext"%>

<%
String id = "0";

// création du nom pour les favoris
String namePath = spaceLabel + " > " + componentLabel;

//R?cup?ration des param?tres
String 	profile			= (String) request.getAttribute("Profile");
String  translation 	= (String) request.getAttribute("Language");
boolean	isGuest			= (Boolean) request.getAttribute("IsGuest");
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");
boolean updateChain		= (Boolean) request.getAttribute("HaveDescriptor");
int		currentPageIndex = (Integer) request.getAttribute("PageIndex");

SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
String query = "";
if (searchContext != null) {
  query = searchContext.getQuery();
}

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String language = kmeliaScc.getLanguage();

String urlTopic	= URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentId, true);;

//For Drag And Drop
boolean dragAndDropEnable = kmeliaScc.isDragAndDropEnable();

String userId = kmeliaScc.getUserId();

boolean userCanCreatePublications = SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile) || SilverpeasRole.writer.isInRole(profile);
boolean userCanValidatePublications = SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile);

boolean userCanSeeStats =
  SilverpeasRole.publisher.isInRole(profile) || SilverpeasRole.supervisor.isInRole(profile) ||
  SilverpeasRole.admin.isInRole(profile) && !KmeliaHelper.isToolbox(kmeliaScc.getComponentId());

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<view:includePlugin name="userZoom"/>
<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<script type="text/javascript" src="javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript" src="javaScript/searchInTopic.js"></script>
<script type="text/javascript" src="javaScript/publications.js"></script>
<script type="text/javascript">
<% if (!profile.equals("user")) { %>
function updateChain() {
    document.updateChain.submit();
}
<% } %>
  
$.i18n.properties({
  name: 'kmeliaBundle',
  path: webContext + '/services/bundles/com/silverpeas/kmelia/multilang/',
  language: '<%=language%>',
  mode: 'map'
});
    
function getString(key) {
	return $.i18n.prop(key);
}

function getCurrentNodeId() {
	return "0";
}

function getCurrentUserId() {
  return "<%=userId%>";
}

function getWebContext() {
	return "<%=m_context%>";
}

function getComponentId() {
	return "<%=componentId%>";
}

function showDnD() {
	<%
	ResourceLocator uploadSettings = new ResourceLocator("org.silverpeas.util.uploads.uploadSettings", "");
	String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
	%>
	showHideDragDrop('<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeNormal_<%=language%>.html','<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&Draft=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
}

function fileUpload() {
    document.fupload.submit();
}

function displayPublications(id) {
	//display publications of topic
	var pubIdToHighlight = "<%=pubIdToHighlight%>";
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Id:id,ComponentId:'<%=componentId%>',PubIdToHighlight:pubIdToHighlight,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function topicWysiwyg() {
	closeWindows();
	document.topicDetailForm.action = "ToTopicWysiwyg";
	document.topicDetailForm.ChildId.value = "0";
	document.topicDetailForm.submit();
}

function pasteFromOperations() {
	$.progressMessage();
	var ieFix = new Date().getTime();
	var url = getWebContext()+'/KmeliaAJAXServlet';
	$.get(url, {ComponentId:getComponentId(),Action:'Paste',Id:'0',IEFix:ieFix},
			function(data){
				$.closeProgressMessage();
				if (data === "ok") {
					displayPublications("0");
				} else {
					alert(data);
				}
			}, 'text');
}

var searchInProgress = <%=searchContext != null%>;

$(document).ready(function() {
	if (searchInProgress) {
		doPagination(<%=currentPageIndex%>);
	} else {
		displayPublications("<%=id%>");
	}
	displayTopicDescription("0");
});
</script>
</head>
<body id="kmelia" onunload="closeWindows()" class="yui-skin-sam">
<div id="<%=componentId %>">
<%
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setI18N("GoToCurrentTopic", translation);

        //Display operations
        OperationPane operationPane = window.getOperationPane();
        if (SilverpeasRole.admin.isInRole(profile)){
          	if (kmeliaScc.isPdcUsed()) {
	        	operationPane.addOperation("useless", resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+kmeliaScc.getComponentId()+"','utilizationPdc1')");
                operationPane.addOperation("useless", resources.getString("GML.PDCPredefinePositions"), "javascript:onClick=openPredefinedPdCClassification(" + id + ");");
          	}
          	if (kmeliaScc.isContentEnabled()) {
	        	operationPane.addOperation(resources.getIcon("kmelia.modelUsed"), resources.getString("kmelia.ModelUsed"), "ModelUsed");
          	}
          	if (kmeliaScc.isWysiwygOnTopicsEnabled()) {
				operationPane.addOperation("useless", kmeliaScc.getString("TopicWysiwyg"), "javascript:onClick=topicWysiwyg('"+id+"')");
			}
          if (SilverpeasRole.admin.isInRole(profile)) {
            operationPane.addOperation("useless", resources.getString("GML.manageSubscriptions"), "ManageSubscriptions");
          }
          	if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportZipAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportTopic()");
          	}
          	if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportPdfAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ExportPDF"), "javascript:openExportPDFPopup()");
          	}
	        operationPane.addOperation(resources.getIcon("kmelia.sortPublications"), kmeliaScc.getString("kmelia.OrderPublications"), "ToOrderPublications");
			operationPane.addLine();
        }
        if (userCanCreatePublications) {
	        operationPane.addOperationOfCreation(resources.getIcon("kmelia.operation.addPubli"), kmeliaScc.getString("PubCreer"), "NewPublication");
	        if (kmeliaScc.isWizardEnabled()) {
	      		operationPane.addOperationOfCreation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
	        }
	        if (kmeliaScc.isImportFileAllowed()) {
	      		operationPane.addOperationOfCreation(resources.getIcon("kmelia.operation.importFile"), kmeliaScc.getString("kmelia.ImportFile"), "javascript:onClick=importFile()");
	        }
	        if (kmeliaScc.isImportFilesAllowed()) {
	        	operationPane.addOperationOfCreation(resources.getIcon("kmelia.operation.importFiles"), kmeliaScc.getString("kmelia.ImportFiles"), "javascript:onClick=importFiles()");
	        }
	        if (updateChain) {
	        	operationPane.addOperation(resources.getIcon("kmelia.updateByChain"), kmeliaScc.getString("kmelia.updateByChain"), "javascript:onClick=updateChain()");
	        }
	        operationPane.addOperation("useless", resources.getString("kmelia.operation.copyPublications"), "javascript:onclick=copyPublications()");
	        operationPane.addOperation("useless", resources.getString("kmelia.operation.cutPublications"), "javascript:onclick=cutPublications()");
	        operationPane.addOperation(resources.getIcon("kmelia.paste"), resources.getString("GML.paste"), "javascript:onClick=pasteFromOperations()");
	        operationPane.addOperation("useless", resources.getString("kmelia.operation.deletePublications"), "javascript:onclick=deletePublications()");
	        operationPane.addLine();
        }
                    	
    	if (!isGuest) {
    	  	operationPane.addOperation("useless", resources.getString("kmelia.operation.exportSelection"), "javascript:onclick=exportPublications()");
    		operationPane.addOperation("useless", resources.getString("SubscriptionsAdd"), "javascript:onClick=addSubscription()");
      		operationPane.addOperation("useless", resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(namePath))+"','','"+urlTopic+"')");
    	}
    	
    	if (userCanCreatePublications) {
      		operationPane.addLine();
          	operationPane.addOperation("useless", resources.getString("PubBasket"), "GoToBasket");
          	if (userCanValidatePublications) {
          		operationPane.addOperation("useless", resources.getString("ToValidate"), "ViewPublicationsToValidate");
          	}
  		}
      if (userCanSeeStats) {
        operationPane.addLine();
        operationPane.addOperation("useless", resources.getString("kmelia.operation.statistics"), "javascript:showStats();");
      }
      
    out.println(window.printBefore());
%>
<view:frame>
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
				<%
					  if (dragAndDropEnable && userCanCreatePublications) {
						%>
						<div id="DnD">
						<table width="98%" cellpadding="0" cellspacing="0"><tr><td align="right">
						<a href="javascript:showDnD()" id="dNdActionLabel"><%=resources.getString("GML.DragNDropExpand")%></a>
						</td></tr></table>
						<table width="100%" border="0" id="DropZone">
						<tr>
						<%
							boolean appletDisplayed = false;
							if (kmeliaScc.isDraftEnabled() && kmeliaScc.isPdcUsed() && kmeliaScc.isPDCClassifyingMandatory()) {
								//Do not display applet in normal mode.
								//Only display applet in draft mode
							} else {
								appletDisplayed = true;
						%>
								<td>
									<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding:0px; width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
								</td>
						<% } %>
						<% if (kmeliaScc.isDraftEnabled()) {
							if (appletDisplayed)
								out.println("<td width=\"5%\">&nbsp;</td>");
							%>
							<td>
								<div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; padding:0px; width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
							</td>
						<% } %>
						</tr></table>
						</div>
				<% }  %>
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
	<input type="hidden" name="Path" value=""/>
	<input type="hidden" name="ChildId"/>
	<input type="hidden" name="Status"/><input type="hidden" name="Recursive"/>
</form>

<form name="pubForm" action="ViewPublication" method="post">
	<input type="hidden" name="PubId"/>
</form>

<form name="fupload" action="fileUpload.jsp" method="post" enctype="multipart/form-data" accept-charset="UTF-8">
	<input type="hidden" name="Action" value="initial"/>
</form>

<form name="updateChain" action="UpdateChainInit">
</form>
</div>
<view:progressMessage/>
</body>
</html>