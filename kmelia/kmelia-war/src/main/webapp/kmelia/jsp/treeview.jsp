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
<%@page import="com.silverpeas.kmelia.SearchContext"%>
<%@page import="org.silverpeas.component.kmelia.KmeliaPublicationHelper"%>
<%@page import="com.stratelia.webactiv.SilverpeasRole"%>
<%@ page import="org.silverpeas.util.i18n.I18NHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkKmelia.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
String		rootId				= "0";

//R?cup?ration des param?tres
String 	profile			= (String) request.getAttribute("Profile");
String  translation 	= (String) request.getAttribute("Language");
boolean	isGuest			= ((Boolean) request.getAttribute("IsGuest")).booleanValue();
boolean displayNBPublis = ((Boolean) request.getAttribute("DisplayNBPublis")).booleanValue();
Boolean rightsOnTopics  = (Boolean) request.getAttribute("RightsOnTopicsEnabled");
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");
int		currentPageIndex = (Integer) request.getAttribute("PageIndex");

SearchContext searchContext = (SearchContext) request.getAttribute("SearchContext");
String query = "";
if (searchContext != null) {
  query = searchContext.getQuery();
}

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String id = (String) request.getAttribute("CurrentFolderId");
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

boolean userCanManageRoot = "admin".equalsIgnoreCase(profile);
boolean userCanManageTopics = rightsOnTopics.booleanValue() || "admin".equalsIgnoreCase(profile) || kmeliaScc.isTopicManagementDelegated();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.kmelia">
<head>
  <title></title>
  <view:looknfeel/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/browseBarComplete.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.jstree.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.cookie.js"></script>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript" src="<%=m_context%>/kmelia/jsp/javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js" />"></script>

<view:includePlugin name="popup"/>
<view:includePlugin name="preview"/>
<view:includePlugin name="rating" />

<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript" src="javaScript/searchInTopic.js"></script>
<script type="text/javascript" src="javaScript/publications.js"></script>
<script type="text/javascript">
function topicGoTo(id) {
    closeWindows();
    displayTopicContent(id);
    getTreeview().deselect_all();
    getTreeview().select_node($('#'+id));
}

function showDnD() {
	<%
	long maximumFileSize = FileRepositoryManager.getUploadMaximumFileSize();
	if (profile.equals("publisher") || profile.equals("writer")) { %>
		showHideDragDrop('<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?NextView=Rkmelia/jsp/validateImportedFilesClassification.jsp&UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeNormal_<%=language%>.html','<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?NextView=Rkmelia/jsp/validateImportedFilesClassification.jsp&UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&Draft=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } else { %>
		showHideDragDrop('<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?NextView=Rkmelia/jsp/validateImportedFilesClassification.jsp&UserId=<%=userId%>&ComponentId=<%=componentId%>&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeNormal_<%=language%>.html','<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?NextView=Rkmelia/jsp/validateImportedFilesClassification.jsp&UserId=<%=userId%>&ComponentId=<%=componentId%>&Draft=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } %>
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

function getComponentLabel() {
	return "<%=EncodeHelper.javaStringToJsString(EncodeHelper.javaStringToHtmlString(
      componentLabel))%>";
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
</script>
</head>
<body id="kmelia" onunload="closeWindows()" class="yui-skin-sam">
<div compile-directive style="display: none"></div>
<div id="<%=componentId %>" class="<%=profile%>">
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setI18N("GoToCurrentTopic", translation);

    //Display operations - following lines are mandatory to init menu correctly
    OperationPane operationPane = window.getOperationPane();
   	operationPane.addOperation("useless", resources.getString("FavoritesAdd1")+" "+resources.getString("FavoritesAdd2"), "javaScript:addCurrentNodeAsFavorite()");

    out.println(window.printBefore());
%>
	<view:frame>
	
       
         <div class="wrap">
            <div class="resizable resizable1"><div id="treeDiv1"></div></div>
				<div id="rightSide" class="resizable resizable2">
					<% if (displaySearch.booleanValue()) {
						Button searchButton = gef.getFormButton(resources.getString("GML.search"), "javascript:onClick=searchInTopic();", false); %>
						<div id="searchZone">
							<view:board>
								<table id="searchLine">
									<tr><td><div id="searchLabel"><%=resources.getString("kmelia.SearchInTopics") %></div>&nbsp;<input type="text" id="topicQuery" size="50" value="<%=query %>" onkeydown="checkSubmitToSearch(event)"/></td><td><%=searchButton.print() %></td></tr>
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
				<% } %>
					<div id="pubList">
					<br/>
					<view:board>
					<br/><center><%=resources.getString("kmelia.inProgressPublications") %><br/><br/><img src="<%=resources.getIcon("kmelia.progress") %>"/></center><br/>
					</view:board>
					</div>
					<div id="footer" class="txtBaseline"></div>
				</div>
			</div>
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
function getComponentPermalink() {
	return "<%=URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentId, false)%>";
}

function deleteNode(nodeId, nodeLabel) {
	// removing nb items displayed in nodeLabel
	if (params["nbPublisDisplayed"]) {
		var idx = nodeLabel.lastIndexOf('(');
		if (idx > 1) {
			nodeLabel = nodeLabel.substring(0, idx-1);
		}
	}
	deleteFolder(nodeId, nodeLabel);
}

function getNodeTitle(nodeId) {
	var nodeTitle = getTreeview().get_text("#"+nodeId);
	var idx = nodeTitle.lastIndexOf('(');
	if (idx > 1) {
		nodeTitle = nodeTitle.substring(0, idx-1);
	}
	return nodeTitle;
}

function getNbPublis(nodeId) {
	var nodeLabel = getTreeview().get_text("#"+nodeId);
	var idx = nodeLabel.lastIndexOf('(');
	var nbPublis = nodeLabel.substring(idx+1, nodeLabel.length-1);
	return eval(nbPublis);
}

function addNbPublis(nodeId, nb) {
	var previousNbPublis = getNbPublis(nodeId);
	var nodeTitle = getNodeTitle(nodeId);
	var nbPublis = eval(previousNbPublis+nb);
	getTreeview().rename_node("#"+nodeId, nodeTitle+" ("+nbPublis+")");
}

function nodeDeleted(nodeId) {
	if (params["nbPublisDisplayed"]) {
		// change nb publications on each parent of deleted node (except root)
		var nbPublisRemoved = getNbPublis(nodeId);
		var path = getTreeview().get_path("#"+nodeId, true);
		for (i=0; i<path.length; i++) {
			var elementId = path[i];
			if (elementId != "0" && elementId != nodeId) {
				addNbPublis(elementId, 0-nbPublisRemoved);
			}
		}
		// add nb of removed publis in bin
		addNbPublis("1", nbPublisRemoved);
	}
	getTreeview().delete_node("#"+nodeId);
}

function resetNbPublis(nodeId) {
	var nodeTitle = getNodeTitle(nodeId);
	getTreeview().rename_node("#"+nodeId, nodeTitle+" (0)");
}

function emptyTrash() {
	if(window.confirm("<%=kmeliaScc.getString("ConfirmFlushTrashBean")%>")) {
		$.progressMessage();
		$.post('<%=m_context%>/KmeliaAJAXServlet', {ComponentId:'<%=componentId%>',Action:'EmptyTrash'},
				function(data){
					$.closeProgressMessage();
					if (data == "ok") {
						if (params["nbPublisDisplayed"]) {
							// remove nb publis to root
							var nbPublisDeleted = getNbPublis("1");
							addNbPublis("0", 0-nbPublisDeleted);
							// set nb publis on bin to 0
							resetNbPublis("1");
						}
						displayTopicContent("1");
					} else {
						alert(data);
					}
				}, 'text');
	}
}

function copyNode(id)	{
	top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+id;
}

function copyCurrentNode()	{
	copyNode(getCurrentNodeId());
}

function cutNode(id) {
	top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+id;
}

function cutCurrentNode() {
	cutNode(getCurrentNodeId());
}

function changeCurrentTopicStatus() {
	var node = getTreeview()._get_node("#"+getCurrentNodeId());
	changeStatus(getCurrentNodeId(), node.attr("status"));
}

function updateUIStatus(nodeId, newStatus, recursive) {
	// updating data stored in treeview
	var node = getTreeview()._get_node("#"+nodeId);
	node.attr("status", newStatus);

	//changing label style according to topic's new status
	node.removeClass("Visible Invisible");
	node.addClass(newStatus);

	//
	if (recursive == "1") {
		var children = getTreeview()._get_children("#"+nodeId);
		for (var i=0; i<children.length; i++) {
			try {
				updateUIStatus(children[i].id, newStatus, recursive);
			} catch (e) {
			}
		}
	}

	if (nodeId == getCurrentNodeId()) {
		// refreshing operations of current folder
		displayOperations(nodeId);
	}
}

function displayTopicContent(id) {

	if (id != searchFolderId) {
		// search session is over
		searchInProgress = false;
	}

	setCurrentNodeId(id);

	if (!searchInProgress) {
		clearSearchQuery();
	}

	if (id == getToValidateFolderId() || id == "1") {
		$("#DnD").css({'display':'none'}); //hide dropzone
		$("#footer").css({'visibility':'hidden'}); //hide footer
		$("#searchZone").css({'display':'none'}); //hide search

		if (id == getToValidateFolderId())	{
			hideOperations();
			displayPublications(id);

			//update breadcrumb
            removeBreadCrumbElements();
            addBreadCrumbElement("#", "<%=resources.getString("ToValidate")%>");
		} else {
			displayOperations(id);
			displayPublications(id);
			displayPath(id);
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
	}

	//display topic information
	displayTopicInformation(id);

	//display topic rich description
	displayTopicDescription(id);
}

var rightClickHelpAlreadyShown = false;
function showRightClickHelp() {
	var rightClickCookieName = "Silverpeas_GED_RightClickHelp";
	var rightClickCookieValue = $.cookie(rightClickCookieName);
	if (!rightClickHelpAlreadyShown && "IKnowIt" != rightClickCookieValue) {
		rightClickHelpAlreadyShown = true;
		$( "#rightClick-message" ).dialog({
			modal: true,
			resizable: false,
			width: 400,
			dialogClass: 'help-modal-message',
			buttons: {
				"<%=resources.getString("kmelia.help.rightclick.buttons.ok") %>": function() {
					$.cookie(rightClickCookieName, "IKnowIt", { expires: 3650, path: '/' });
					$( this ).dialog( "close" );
				},
				"<%=resources.getString("kmelia.help.rightclick.buttons.remind") %>": function() {
					$( this ).dialog( "close" );
				}
			}
		});
	}
}

function getTreeview() {
	return $.jstree._reference("#treeDiv1");
}

function customMenu(node) {
	<% if (!userCanManageTopics) { %>
		//user can not manage folders
		return false;
	<% } %>

	var nodeType = node.attr("rel");
	var nodeId = node.attr("id");
	var userRole = '<%=profile%>';
	if (params["rightsOnTopic"]) {
		userRole = node.attr("role");
	}
	if (nodeType == getToValidateFolderId()) {
    	return false;
    } else if (nodeType == "bin") {
    	var binItems = {
       			emptyItem: {
       	            label: "<%=resources.getString("EmptyBasket")%>",
       	            action: function () {
       	            	emptyTrash();
       	            }
	        }
        	};
        return binItems;
    }

    // The default set of all items
    var items = {
    	addItem: {
        	label: "<%=resources.getString("CreerSousTheme")%>",
            action: function (obj) {
            	topicAdd(obj.attr("id"), false);
            }
        },
    	editItem: {
            label: "<%=resources.getString("ModifierSousTheme")%>",
            action: function (obj) {
            	var url = getWebContext()+"/services/folders/"+getComponentId()+"/"+obj.attr("id");
        		$.getJSON(url, function(topic){
        					var name = topic.data;
        					var desc = topic.attr["description"];
        					if (params["i18n"]) {
        						storeTranslations(topic.translations);
        					} else {
        						$("#addOrUpdateNode #folderName").val(name);
								$("#addOrUpdateNode #folderDescription").val(desc);
        					}
        					topicUpdate(topic.attr["id"]);
        				});
            }
        },
        deleteItem: {
            label: "<%=resources.getString("SupprimerSousTheme")%>",
            action: function (obj) {
            	deleteNode(obj.attr("id"), getTreeview().get_text(obj));
            }
        },
        sortItem: {
            label: "<%=resources.getString("kmelia.SortTopics")%>",
            action: function (obj) {
            	closeWindows();
        		SP_openWindow("ToOrderTopics?Id="+obj.attr("id"), "topicAddWindow", "600", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised,resizable");
            },
        	"separator_after" : true
        },
        <% if (kmeliaScc.isPdcUsed()) { %>
        pdcItem: {
        	label: "<%=resources.getString("GML.PDCPredefinePositions")%>",
            action: function (obj) {
            	openPredefinedPdCClassification(obj.attr("id"));
            },
        	"separator_after" : true
        },
        <% } %>
        copyItem: {
            label: "<%=resources.getString("GML.copy")%>",
            action: function (obj) {
            	copyNode(obj.attr("id"));
            }
        },
        cutItem: {
            label: "<%=resources.getString("GML.cut")%>",
            action: function (obj) {
            	cutNode(obj.attr("id"));
            }
        },
        pasteItem: {
            label: "<%=resources.getString("GML.paste")%>",
            action: function (obj) {
            	pasteNode(obj.attr("id"));
            },
        	"separator_after" : true
        }
        <% if (kmeliaScc.isOrientedWebContent() || kmeliaScc.isWysiwygOnTopicsEnabled()) { %>
        ,
        wysiwygItem: {
            label: "<%=resources.getString("TopicWysiwyg")%>",
            action: function (obj) {
            	updateTopicWysiwyg(obj.attr("id"));
            }
        }
        <% } %>
        <% if (kmeliaScc.isOrientedWebContent()) { %>
        ,
        statusItem: {
            label: "<%=resources.getString("TopicVisible2Invisible")%>",
            action: function (obj) {
            	changeStatus(obj.attr("id"), obj.attr("status"));
            }
        }
    	<% } %>
    };

    if (nodeType == "root") {
    	<% if(!userCanManageRoot) { %>
    		// user can not manage root folder
    		return false;
    	<% } %>
    	delete items.editItem;
        delete items.deleteItem;
        delete items.copyItem;
        delete items.cutItem;
        delete items.statusItem;
    }

    if (items.statusItem) {
    	if (node.attr("status") == "Invisible") {
    		items.statusItem.label = "<%=resources.getString("TopicInvisible2Visible")%>";
    	}
    }

    if (userRole == "admin") {
    	// all actions are allowed
    } else if (userRole == "user") {
		var parentProfile =  getTreeview()._get_parent(node).attr("role");
		if (parentProfile != "admin") {
			//do not show the menu
			return;
		} else {
			items.addItem._disabled = true;
			items.sortItem._disabled = true;
			if (items.pdcItem) {
				items.pdcItem._disabled = true;
			}
			items.copyItem._disabled = true;
			items.cutItem._disabled = true;
			items.pasteItem._disabled = true;
			if (items.wysiwygItem) {
            	items.wysiwygItem._disabled = true;
            }
            if (items.statusItem) {
            	items.statusItem._disabled = true;
            }
		}
	} else {
		var isTopicManagementDelegated = <%=kmeliaScc.isTopicManagementDelegated()%>;
		var userId = "<%=kmeliaScc.getUserId()%>";
		var creatorId = node.attr("creatorId");
		if (isTopicManagementDelegated && userRole != "admin") {
			if (creatorId != userId) {
				//do not show the menu
				return;
			} else if (creatorId == userId) {
				if (items.pdcItem) {
					items.pdcItem._disabled = true;
				}
				items.copyItem._disabled = true;
				items.cutItem._disabled = true;
				items.pasteItem._disabled = true;
                if (items.wysiwygItem) {
                	items.wysiwygItem._disabled = true;
                }
                if (items.statusItem) {
                	items.statusItem._disabled = true;
                }
			}
		} else {
			if (userRole != "admin") {
				//do not show the menu
				return;
			}
		}
	}
    return items;
}

function spreadNbItems(children) {
	if (children) {
		for(var i = 0; i < children.length; i++) {
			var child = children[i];
			child.attr['title'] = child.attr['description'];
			<% if (kmeliaScc.isOrientedWebContent()) { %>
				child.attr['class'] = child.attr['status'];
			<% } %>
			if (child.attr['nbItems']) {
				child.data = child.data + " ("+child.attr['nbItems']+")";
				spreadNbItems(child.children);
			}
		}
	}
}

function getUserProfile(id) {
	var componentId = getComponentId();
	var result = "";
    $.ajax({
      url: getWebContext()+'/KmeliaAJAXServlet',
      data : {Id:id,Action:'GetProfile',ComponentId:componentId},
      type : 'GET',
      dataType : 'text',
      cache : false,
      async : false,
      success : function(data, status, jqXHR) {
        result = data;
      },
      error : function(jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
      }
    });
    return result;
}

function publicationMovedInError(id, data) {
	var pubName = getPublicationName(id);
  notyError("<%=resources.getString("kmelia.drag.publication.error1")%>" + pubName + "<%=resources.getString("kmelia.drag.publication.error2")%>" + "<br/><br/>" + data);
}

function getPublicationName(id) {
	return $("#pubList #pub-"+id).html();
}

function extractPublicationId(id) {
	return id.substring(4, id.length);
}

function publicationMovedSuccessfully(id, targetId) {
	var pubName = getPublicationName(id);
  notySuccess("<%=resources.getString("kmelia.drag.publication.success1")%>" + pubName + "<%=resources.getString("kmelia.drag.publication.success2")%>");

	if (params["nbPublisDisplayed"]) {
		// add one publi to target node and its parents
		var path = getTreeview().get_path("#"+targetId, true);
		for (i=0; i<path.length; i++) {
			var elementId = path[i];
			if (elementId != "0") {
				addNbPublis(elementId, 1);
			}
		}

		// remove one publi to current node and its parents
		var path = getTreeview().get_path("#"+getCurrentNodeId(), true);
		for (i=0; i<path.length; i++) {
			var elementId = path[i];
			if (elementId != "0") {
				addNbPublis(elementId, -1);
			}
		}
	}

	try {
		// remove one publi to publications header
		var previousNb = $("#pubsHeader #pubsCounter span").html();
		if (previousNb == 1) {
			$("#pubsHeader #pubsCounter").html("<%=resources.getString("GML.publications")%>");
			$("#pubsHeader #pubsSort").hide();
			$("#pubList ul").html("<%=resources.getString("PubAucune")%>")
		} else {
			$("#pubsHeader #pubsCounter span").html(eval(previousNb-1));
		}
	} catch (e) {

	}

	// remove publication from publications list
	$("#pubList #pub-"+id).closest("li").fadeOut('500', function() {
		$(this).remove();
	});
}

function publicationsRemovedSuccessfully(nb) {
	if (params["nbPublisDisplayed"]) {
		if (getCurrentNodeId() == "1") {
			// publications are definitively removed
			// remove nb publis from trash and root folders
			addNbPublis("1", 0-eval(nb));
			addNbPublis("0", 0-eval(nb));
		} else {
			// publications goes to trash
			// remove nb publi to current node and its parents except root
			var path = getTreeview().get_path("#"+getCurrentNodeId(), true);
			for (i=0; i<path.length; i++) {
				var elementId = path[i];
				if (elementId != "0") {
					addNbPublis(elementId, 0-eval(nb));
				}
			}

			// add nb publi to trash
			addNbPublis("1", eval(nb));
		}
	}
}

function getString(key) {
	return $.i18n.prop(key)
}

$(document).ready(
	function () {
		//build the tree
		$("#treeDiv1").bind("loaded.jstree", function (event, data) {
			//alert("TREE IS LOADED");
		}).bind("select_node.jstree", function (e, data) {
    		// data.inst is the instance which triggered this event
    		var nodeId = $(data.rslt.obj).attr('id');

    		// open topic in treeview
    		var idSelector = "#"+nodeId;
    		$.jstree._reference(idSelector).open_node(idSelector);

		// display topic content in right panel
    		displayTopicContent(nodeId);
    	})
    	.jstree({
    	"core" : {
        html_titles: true
    			//"load_open" : true
    	},
    	"ui" :{
            "select_limit" : 1,
          	"initially_select" : "#<%=id%>",
          	"select_prev_on_delete" : false
        },
	"json_data" : {
			"ajax" : {
				"url": function (node) {
					var nodeId = "";
					var url = "<%=m_context%>/services/folders/<%=componentId%>/<%=id%>/treeview?lang="+getTranslation()+"&IEFix="+new Date().getTime();
					if (node != -1) {
						url = "<%=m_context%>/services/folders/<%=componentId%>/"+node.attr("id")+"/children?lang="+getTranslation()+"&IEFix="+new Date().getTime();
					}
					return url;
				},
				success: function(n) {
					if (n) {
						if (n.length) {
							// this is subfolders
							spreadNbItems(n);
						} else {
							if (n.data) {
								// this is the root
								n.data = "<%=EncodeHelper.javaStringToHtmlString(componentLabel)%>";
								if (n.attr['nbItems']) {
									n.data = n.data + " ("+n.attr['nbItems']+")";
								}
								<% if (kmeliaScc.isOrientedWebContent()) { %>
									n.attr['class'] = n.attr['status'];
								<% } %>
								spreadNbItems(n.children);
							}
						}
					}
				    return n;
				}
			}
		},
		// Using types - most of the time this is an overkill
		// read the docs carefully to decide whether you need types
		"types" : {
			// I set both options to -2, as I do not need depth and children count checking
			// Those two checks may slow jstree a lot, so use only when needed
			"max_depth" : -2,
			"max_children" : -2,
			// I want only `drive` nodes to be root nodes
			// This will prevent moving or creating any other type as a root node
			"valid_children" : [ "root" ],
			"types" : {
				// The `root` node
				"root" : {
					"valid_children" : [ "bin", getToValidateFolderId() ],
					// those prevent the functions with the same name to be used on `root` nodes
					// internally the `before` event is used
					"start_drag" : false,
					"move_node" : false,
					"delete_node" : false,
					"remove" : false
				},
				// The `bin` node
				"bin" : {
					// can have files and folders inside, but NOT other `drive` nodes
					"valid_children" : "none",
					"icon" : {
						"image" : "icons/treeview/basket.jpg"
					},
					// those prevent the functions with the same name to be used on `bin` nodes
					// internally the `before` event is used
					"start_drag" : false,
					"move_node" : false,
					"delete_node" : false,
					"remove" : false
				},
				// The `to validate` node
				"<%=KmeliaHelper.SPECIALFOLDER_TOVALIDATE%>" : {
					// can have files and folders inside, but NOT other `drive` nodes
					"valid_children" : "none",
					"icon" : {
						"image" : "<%=m_context%>/util/icons/ok_alpha.gif"
					},
					// those prevent the functions with the same name to be used on `tovalidate` nodes
					// internally the `before` event is used
					"start_drag" : false,
					"move_node" : false,
					"delete_node" : false,
					"remove" : false
				},
				"folder" : {
					"valid_children" : [ "folder" ],
					// those prevent the functions with the same name to be used on `root` nodes
					// internally the `before` event is used
					"start_drag" : false,
					"move_node" : false,
					"delete_node" : false,
					"remove" : false
				}
			}
		},
		"themes" : {
			"theme" : "default",
			"dots" : false,
			"icons" : true
		},
		"contextmenu" : {
			"show_at_node" : false,
			"items" : customMenu
		},
		"dnd" : {
			"drop_finish" : function () {
				alert("drop_finish");
			},
			"drag_check" : function (data) {
				var targetId = data.r.attr("id");
				var targetType = data.r.attr("rel");
				if (targetId == getCurrentNodeId()) {
					return false;
				} else if (targetType == getToValidateFolderId()) {
					return false;
				} else if (targetType == "root") {
					if (<%=KmeliaPublicationHelper.isPublicationsOnRootAllowed(componentId)%>) {
						var profile = getUserProfile(targetId);
						//writeInConsole("drag_check : current user is "+profile+" in root");
						if (profile != "<%=SilverpeasRole.user.toString()%>") {
							return {
								after : false,
								before : false,
								inside : true
							};
						}
					}
				} else if (targetId != "treeDiv1"){
					var profile = getUserProfile(targetId);
					//writeInConsole("drag_check : current user is "+profile+" in folder #"+targetId);
					if (profile != "<%=SilverpeasRole.user.toString()%>") {
						return {
							after : false,
							before : false,
							inside : true
						};
					}
				}
				return false;
			},
			"drag_finish" : function (data) {
				var pubId = extractPublicationId(data.o.id);
				var targetId = data.r.attr("id");

				// store new parent of publication
				movePublication(pubId, getCurrentNodeId(), targetId);
			}
		},
		// the `plugins` array allows you to configure the active plugins on this instance
		"plugins" : ["themes","json_data","ui","types","crrm","contextmenu","dnd"]
    });

    // init splitter
    $(".resizable1").resizable({
      autoHide: true,
      handles: 'e',
      maxWidth: 500,
      resize: function (e, ui) {
        var parent = ui.element.parent();
        var remainingSpace = parent.width() - ui.element.outerWidth();
        var divTwo = ui.element.next();
        var divTwoWidth = (remainingSpace - (divTwo.outerWidth() - divTwo.width())) / parent.width() * 100 + "%";
        divTwo.width(divTwoWidth);
      },
      stop: function (e, ui) {
        var parent = ui.element.parent();
        ui.element.css({
          width: ui.element.width() / parent.width() * 100 + "%"
        });
      }
    });
	
	$.i18n.properties({
        name: 'kmeliaBundle',
        path: webContext + '/services/bundles/com/silverpeas/kmelia/multilang/',
        language: '<%=language%>',
        mode: 'map'
    });

	<% if (displaySearch.booleanValue()) { %>
		document.getElementById("topicQuery").focus();
	<% } %>

	<% if (KmeliaHelper.ROLE_ADMIN.equals(profile)) { %>
		//Right-click concerns only admins
		showRightClickHelp();
	<% } %>

	<% if (settings.getBoolean("DisplayDnDOnLoad", false)) { %>
		showDnD();
	<% } %>
	}
);
</script>
</div>
<div id="visibleInvisible-message" style="display: none;">
	<p>
	</p>
</div>
<div id="rightClick-message" title="<%=resources.getString("kmelia.help.rightclick.title") %>" style="display: none;">
	<p>
    <%=resources.getStringWithParam("kmelia.help.rightclick.content", EncodeHelper.javaStringToHtmlString(componentLabel)) %>
	</p>
</div>
<div id="addOrUpdateNode" style="display: none;">
	<form name="topicForm" action="AddTopic" method="post">
    <input type="hidden" id="<%=I18NHelper.HTMLHiddenRemovedTranslationMode %>" name="<%=I18NHelper.HTMLHiddenRemovedTranslationMode %>" value="false"/>
            <table cellpadding="5" width="100%">
              <tr><td class="txtlibform"><fmt:message key="TopicPath"/> :</td>
                <td valign="top" id="path"></td>
              </tr>
              <%=I18NHelper.getFormLine(resources, null, kmeliaScc.getLanguage())%>
              <tr>
                <td class="txtlibform"><fmt:message key="TopicTitle"/> :</td>
                <td><input type="text" name="Name" id="folderName" size="60" maxlength="60"/>
                <input type="hidden" name="ParentId" id="parentId"/>
                <input type="hidden" name="ChildId" id="topicId"/>&nbsp;<img border="0" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5" alt=""/></td>
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

<%@ include file="../../sharing/jsp/createTicketPopin.jsp" %>
<view:progressMessage/>
<script type="text/javascript">
/* declare the module myapp and its dependencies (here in the silverpeas module) */
var myapp = angular.module('silverpeas.kmelia', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>