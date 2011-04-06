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
<%@page import="com.stratelia.silverpeas.util.SilverpeasSettings"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBarElement"%>

<%
String		rootId				= "0";
String		name				= "";
String		description			= "";

//R?cup?ration des param?tres
String 	profile			= (String) request.getAttribute("Profile");
List 	treeview 		= (List) request.getAttribute("Treeview");
String  translation 	= (String) request.getAttribute("Language");
boolean	isGuest			= ((Boolean) request.getAttribute("IsGuest")).booleanValue();
boolean displayNBPublis = ((Boolean) request.getAttribute("DisplayNBPublis")).booleanValue();
Boolean rightsOnTopics  = (Boolean) request.getAttribute("RightsOnTopicsEnabled");
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");

TopicDetail currentTopic 		= (TopicDetail) request.getAttribute("CurrentTopic");

String 		pathString 			= (String) request.getAttribute("PathString");

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String id = currentTopic.getNodeDetail().getNodePK().getId();
boolean useTreeview = (treeview != null);
String language = kmeliaScc.getLanguage();

NodeDetail nodeDetail = currentTopic.getNodeDetail();

List path = (List) kmeliaScc.getNodeBm().getPath(currentTopic.getNodePK());
//Icons
String subscriptionAddSrc	= m_context + "/util/icons/subscribeAdd.gif";
String favoriteAddSrc		= m_context + "/util/icons/addFavorit.gif";
String publicationAddSrc	= m_context + "/util/icons/publicationAdd.gif";
String pdcUtilizationSrc	= m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
String importFileSrc		= m_context + "/util/icons/importFile.gif";
String importFilesSrc		= m_context + "/util/icons/importFiles.gif";
String exportComponentSrc	= m_context + "/util/icons/exportComponent.gif";

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

boolean userCanManageRoot = "admin".equalsIgnoreCase(profile);
boolean userCanManageTopics = rightsOnTopics.booleanValue() || "admin".equalsIgnoreCase(profile) || kmeliaScc.isTopicManagementDelegated();

%>

<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<script type="text/javascript" src="<%=m_context%>/util/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/json/json-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/connection/connection-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/treeview/treeview-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/container/container_core-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/animation/animation-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/element/element-min.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/yui/resize/resize-min.js"></script>

<script type="text/javascript" src="<%=m_context%>/util/javaScript/jquery/jquery.cookie.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript" src="<%=m_context%>/kmelia/jsp/javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript" src="javaScript/searchInTopic.js"></script>

<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/yui/treeview/assets/skins/sam/treeview.css" />
<link rel="stylesheet" type="text/css" href="styleSheets/tree.css">
<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/yui/resize/assets/skins/sam/resize.css" />

<style type="text/css" >
/** resizable */
#pg {
    /*width: 950px;*/
    /*height: 600px;*/
    /*border: 1px solid red;*/
    margin-right: -70px;
    padding-right: 0px;
    float: left;
}
#pg .yui-g {
    /*height: 600px;*/
    width: 100%;
    overflow: hidden;
    /*border: 1px solid green;*/
    float: left;
}

#treeDiv1 {
	/*width: 20%;*/
	height : 500px;
	float: left;
	padding-right: 5px; /*do not forget to change end minus if this value change !*/
	overflow: hidden;
	border: 1px solid #F2F2F2;
}
#rightSide {
	float: left;
	margin-left: 30px;
	/*height: 100%;*/
	/*width: 675px;*/
	overflow: hidden;
	/*border: 1px solid blue;*/
}

.ygtvfocus {
	background-color: transparent;
	border: none;
}
.ygtvfocus .ygtvlabel, .ygtvfocus .ygtvlabel:link, .ygtvfocus .ygtvlabel:visited, .ygtvfocus .ygtvlabel:hover {
	background-color: transparent;
	/*font-weight: bold;*/
}

.ygtvfocus  a  {
	outline-style:none;
}

.ygtvcell .ygtvcontent .ygtvfocus {
	background-color: red;
}

.ygtvcell .ygtvtm .ygtvfocus {
	background-color: red;
}

.ygtv-highlight .ygtv-highlight0 .ygtvfocus .ygtvlabel,
.ygtv-highlight .ygtv-highlight1 .ygtvfocus .ygtvlabel,
.ygtv-highlight .ygtv-highlight2 .ygtvfocus .ygtvlabel {
	background-color: red;
}

/** operations */
.operationHidden {
	display: none;
}

.operationVisible {
	display: block;
}

#footer {
	text-align: center;
}

#ygtv0 {
	overflow: auto;
	height: 500px;
}

.icon-basket { display:block; height: 22px; padding-left: 18px; padding-top: 2px; background: transparent url(icons/treeview/basket.jpg) no-repeat -1px 0px; }
.icon-tovalidate { display:block; height: 19px; padding-left: 18px; padding-top: 3px; background: transparent url(<%=m_context%>/util/icons/ok_alpha.gif) no-repeat 0px 2px;}

.invisibleTopic {
	color: #BBB;
	cursor: pointer;
	margin-left: 2px;
}
</style>

<script type="text/javascript">
function topicGoTo(id) {
    closeWindows();
    displayTopicContent(id);
}

function clipboardCopy() {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id=<%=id%>';
}

function clipboardCut() {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id=<%=id%>';
}

function showDnD()
{
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
	if (profile.equals("publisher") || profile.equals("writer")) { %>
		showHideDragDrop('<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeNormal_<%=language%>.html','<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&Draft=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } else { %>
		showHideDragDrop('<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeNormal_<%=language%>.html','<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&Draft=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
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

function getWidth() {
	  var myWidth = 0;
	  if( typeof( window.innerWidth ) == 'number' ) {
	    //Non-IE
	    myWidth = window.innerWidth;
	  } else if( document.documentElement && document.documentElement.clientWidth ) {
	    //IE 6+ in 'standards compliant mode'
	    myWidth = document.documentElement.clientWidth;
	  } else if( document.body && document.body.clientWidth ) {
	    //IE 4 compatible
	    myWidth = document.body.clientWidth;
	  }
	  return myWidth;
}

function getHeight() {
	  var myHeight = 0;
	  if( typeof( window.innerHeight ) == 'number' ) {
	    //Non-IE
	    myHeight = window.innerHeight;
	  } else if( document.documentElement && document.documentElement.clientHeight) {
	    //IE 6+ in 'standards compliant mode'
	    myHeight = document.documentElement.clientHeight;
	  } else if( document.body && document.body.clientHeight) {
	    //IE 4 compatible
	    myHeight = document.body.clientHeight;
	  }
	  return (myHeight -20);
}

</script>
</head>
<body id="kmelia" onUnload="closeWindows()" class="yui-skin-sam">
<div id="<%=componentId %>">
<%
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setI18N("GoToCurrentTopic", translation);

        //Display operations - following lines are mandatory to init menu correctly
        OperationPane operationPane = window.getOperationPane();
      	operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addCurrentNodeAsFavorite()");

    //Instanciation du cadre avec le view generator
	Frame frame = gef.getFrame();

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
			<div id="pg">
			<div class="yui-g">
				<div id="treeDiv1"></div>
				<div id="rightSide">
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
									<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
								</td>
						<% } %>
						<% if (kmeliaScc.isDraftEnabled()) {
							if (appletDisplayed)
								out.println("<td width=\"5%\">&nbsp;</td>");
							%>
							<td>
								<div id="DragAndDropDraft" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
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
				</div>
			</div>
			</div>

	<%
		out.println(frame.printAfter());
		out.println(window.printAfter());
	%>

<form name="topicDetailForm" method="POST">
	<input type="hidden" name="Id" value="<%=id%>"/>
	<input type="hidden" name="Path" value="<%=EncodeHelper.javaStringToHtmlString(pathString)%>"/>
	<input type="hidden" name="ChildId"/>
	<input type="hidden" name="Status"/>
	<input type="hidden" name="Recursive"/>
</form>

<form name="pubForm" action="ViewPublication" method="POST">
	<input type="hidden" name="PubId"/>
	<input type="hidden" name="CheckPath"/>
</form>

<form name="fupload" action="fileUpload.jsp" method="POST" enctype="multipart/form-data" accept-charset="UTF-8">
	<input type="hidden" name="Action" value="initial"/>
</form>

<form name="updateChain" action="UpdateChainInit">
</form>
<script type="text/javascript">
//Declarations
var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;

var oTreeView;
var root;
var currentNodeIndex;
var basketNode;
var toValidateNode;
function initTree(id)
{
	//create a new tree:
    oTreeView = new YAHOO.widget.TreeView("treeDiv1");

    //turn dynamic loading on for entire tree:
    oTreeView.setDynamicLoad(loadNodeData, 0);
    //oTreeView.singleNodeHighlight = true;

    root = new YAHOO.widget.TextNode({"id":"0","role":"<%=kmeliaScc.getUserTopicProfile("0")%>"}, oTreeView.getRoot(), true);
	root.labelElId = "0";
	root.label = "<%=EncodeHelper.javaStringToJsString(componentLabel)%>";
    root.href = "javascript:displayTopicContent(0)";

	//render tree with these toplevel nodes; all descendants of these nodes
	//will be generated as needed by the dynamic loader.
	oTreeView.render();

	setCurrentNodeId(id);

	//let the time to tree to be loaded !
	setTimeout("displayTopicContent("+id+")", 500);

	<% if (SilverpeasSettings.readBoolean(settings, "DisplayDnDOnLoad", false)) { %>
		showDnD();
	<% } %>

	oTreeView.subscribe("expandComplete", function(node) {
		//highlight node
		if (node.data.id == id)
		{
			//currentNodeIndex = node.index;
			$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'bold'});
		}

     });

	oTreeView.subscribe('clickEvent',function(oArgs) {
		//alert('Click on node: ' + oArgs.node.label+", id = "+oArgs.node.data.id+", index = "+oArgs.node.index);

		$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'normal'});

		setCurrentNodeId(oArgs.node.data.id);
		currentNodeIndex = oArgs.node.index;

		$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'bold'});

		displayTopicContent(getCurrentNodeId());
	});

	//display topic's name editor
	oTreeView.subscribe('dblClickEvent', oTreeView.onEventEditNode);

	//Save new topic's name
	oTreeView.subscribe('editorSaveEvent', function (oArgs) {
		$.post('<%=m_context%>/KmeliaAJAXServlet', {Id:oArgs.node.data.id,ComponentId:'<%=componentId%>',Action:'Rename',Name:oArgs.newValue},
				function(data){
					if (data == "ok")
					{
						//do nothing
					}
					else
					{
						alert(data);
					}
				});
	});

}

function getComponentPermalink() {
	return "<%=URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentId)%>";
}

function getCurrentNode() {
	return getNode(getCurrentNodeId());	
}

function getNode(id) {
	return oTreeView.getNodeByProperty("labelElId", id);
}

function loadNodeData(node, fnLoadComplete)  {

    //We'll load node data based on what we get back when we
    //use Connection Manager topass the text label of the
    //expanding node to the Yahoo!
    //Search "related suggestions" API.  Here, we're at the
    //first part of the request -- we'll make the request to the
    //server.  In our success handler, we'll build our new children
    //and then return fnLoadComplete back to the tree.

    //prepare URL for XHR request:
    var sUrl = "<%=m_context%>/KmeliaJSONServlet?Action=GetSubTopics&ComponentId=<%=componentId%>&Language=<%=language%>&IEFix="+new Date().getTime()+"&Id="+node.labelElId;

    //prepare our callback object
    var callback = {

        //if our XHR call is successful, we want to make use
        //of the returned data and create child nodes.
        success: function(oResponse) {
            YAHOO.log("XHR transaction was successful.", "info", "example");
            //YAHOO.log(oResponse.responseText);

            var messages = [];

            // Use the JSON Utility to parse the data returned from the server
            try {
                messages = YAHOO.lang.JSON.parse(oResponse.responseText);
            }
            catch (x) {
                alert("JSON Parse failed!");
                return;
            }

            var tempNode;
            var basketHere = false;
            var nbItemsInBasket = 0;
            var nbItemsToValidate = 0;
            <%
            String ids = "(";
            for (int n=0; n<path.size(); n++)
            {
              NodeDetail node = (NodeDetail) path.get(n);
              if (n!=0)
                ids += " || ";
              ids += "m.id == "+node.getId();
            }
            ids += ")";
            %>
            // The returned data was parsed into an array of objects.
            var nbPublisOnRoot = 0;
            for (var i = 0, len = messages.length; i < len; ++i) {
                var m = messages[i];
                if (m.id == "0" && m.nbObjects != -1)
                {
                	root.label = root.label + " ("+m.nbObjects+")";
                }
                if (m.id == "1")
                {
                    basketHere = true;
                    nbItemsInBasket = m.nbObjects;
                    //alert("basket = "+m.nbObjects);
                }
                else if (m.id == "tovalidate")
                {
                	nbItemsToValidate = m.nbObjects;
                }
                if (m.id != "1" && m.id != "2" && m.id != "tovalidate")
                {
                    tempNode = new YAHOO.widget.TextNode(m, node, <%=ids%>);
                    tempNode.labelElId = m.id;
                    if (m.nbObjects != -1)
                    {
                    	tempNode.label = m.name + " ("+m.nbObjects+")";
                    }
                    else
                    {
                    	tempNode.label = m.name;
                    }
                    tempNode.title = m.description;
                    if (node.data.role == "admin" && <%=kmeliaScc.getSettings().getBoolean("TreeNodeEditable", false)%>)
                    {
                        //node's label is only editable if user is admin on parent node
                    	tempNode.editable = true;
                    }
					<% if (kmeliaScc.isOrientedWebContent()) { %>
						if (m.status == "Invisible") {
							tempNode.labelStyle = "invisibleTopic";
						}
					<% } %>
                }
                if (m.level == "2")
                {
                	if ((m.id != "1" && m.id != "tovalidate") || <%="admin".equals(kmeliaScc.getUserTopicProfile("0"))%>)
                	{
                    	nbPublisOnRoot += m.nbObjects;
                    }
                }
            }
            if (basketHere)
            {
                if (<%=!toolboxMode%> && <%="admin".equals(profile) || "publisher".equals(profile)%>)
                {
					//add "To validate"
					toValidateNode = new YAHOO.widget.TextNode({"id":"tovalidate"}, root, false, true);
					toValidateNode.labelElId = "tovalidate";
					toValidateNode.label = "<%=resources.getString("ToValidateShort")%>";
					toValidateNode.title = "<%=resources.getString("ToValidate")%>";
					if (nbItemsToValidate != -1 && <%=displayNBPublis%>)
	   				{
	   					toValidateNode.label = toValidateNode.label + " ("+nbItemsToValidate+")";
	   				}
					toValidateNode.href = "javascript:displayTopicContent('tovalidate')";
					toValidateNode.isLeaf = true;
					toValidateNode.hasIcon = true;
					toValidateNode.labelStyle = "icon-tovalidate";
                }

                if (<%="admin".equals(profile) || "publisher".equals(profile) || "writer".equals(profile)%>) {
	            	//add basket
		   			basketNode = new YAHOO.widget.TextNode({"id":"1"}, root, false, true);
		   			basketNode.labelElId = "basket";
		   			basketNode.label = "<%=resources.getString("kmelia.basket")%>";
		   			if (nbItemsInBasket != -1) {
		   				basketNode.label = basketNode.label + " ("+nbItemsInBasket+")";
		   			}
		   			basketNode.href = "javascript:displayTopicContent(1)";
		   			basketNode.isLeaf = true;
		   			basketNode.hasIcon = true;
					basketNode.labelStyle = "icon-basket";
                }
            }

            if (nbPublisOnRoot > 0)
            {
            	root.label = root.label + " ("+nbPublisOnRoot+")";
            	$("#"+root.labelElId).html("<%=EncodeHelper.javaStringToJsString(componentLabel)%> ("+nbPublisOnRoot+")");
            	//root.refresh();
            }

            //When we're done creating child nodes, we execute the node's
            //loadComplete callback method which comes in via the argument
            //in the response object (we could also access it at node.loadComplete,
            //if necessary):
            oResponse.argument.fnLoadComplete();
        },

        //if our XHR call is not successful, we want to
        //fire the TreeView callback and let the Tree
        //proceed with its business.
        failure: function(oResponse) {
            YAHOO.log("Failed to process XHR transaction.", "info", "example");
            oResponse.argument.fnLoadComplete();
        },

        //our handlers for the XHR response will need the same
        //argument information we got to loadNodeData, so
        //we'll pass those along:
        argument: {
            "node": node,
            "fnLoadComplete": fnLoadComplete
        },

        //timeout -- if more than 7 seconds go by, we'll abort
        //the transaction and assume there are no children:
        timeout: 7000
    };

  	//With our callback object ready, it's now time to
    //make our XHR call using Connection Manager's
    //asyncRequest method:
    YAHOO.util.Connect.asyncRequest('GET', sUrl, callback);
}

	var oCurrentTextNode = null;
	
	/*
	     Adds a new TextNode as a child of the TextNode instance
	     that was the target of the "contextmenu" event that
	     triggered the display of the ContextMenu instance.
	*/
	function addNode() {
		topicAdd(oCurrentTextNode.labelElId, false);
	}

	/*
	     Edits the label of the TextNode that was the target of the
	     "contextmenu" event that triggered the display of the
	     ContextMenu instance.
	*/
	function editNodeLabel() {
		topicUpdate(oCurrentTextNode.labelElId);
	}

	function updateCurrentNode() {
		topicUpdate(getCurrentNodeId());
	}

	function deleteNode(nodeId, nodeLabel) {
		if(window.confirm("<%=kmeliaScc.getString("ConfirmDeleteTopic")%> '" + nodeLabel + "' ?")) {
			$.get('<%=m_context%>/KmeliaAJAXServlet', { Id:nodeId,ComponentId:'<%=componentId%>',Action:'Delete'},
					function(data){
						if ((data - 0) == data && data.length > 0) {
							var node = oTreeView.getNodeByProperty("labelElId", nodeIdToDelete);
							// go to parent node
							displayTopicContent(node.parent.data.id);
							// remove node from treeview
							oTreeView.removeNode(node);
			                oTreeView.draw();
						} else {
							alert(data);
						}
					}, 'text');
		}
	}
	
	var nodeIdToDelete;
	function deleteCurrentNode() {
		nodeIdToDelete = getCurrentNodeId();
		var node = oTreeView.getNodeByProperty("labelElId", getCurrentNodeId());
		deleteNode(nodeIdToDelete, node.data.name);
	}
	
	/*
    	Deletes the TextNode that was the target of the "contextmenu"
    	event that triggered the display of the ContextMenu instance.
	*/
	function deleteNodeFromTreeview() {
		nodeIdToDelete = oCurrentTextNode.labelElId;
		deleteNode(nodeIdToDelete, oCurrentTextNode.data.name);
	}

	function emptyTrash()
	{
		if(window.confirm("<%=kmeliaScc.getString("ConfirmFlushTrashBean")%>"))
		{
			$.progressMessage();
			$.get('<%=m_context%>/KmeliaAJAXServlet', {ComponentId:'<%=componentId%>',Action:'EmptyTrash'},
					function(data){
						$.closeProgressMessage();
						if (data == "ok") {
							var basketNode = oTreeView.getNodeByProperty("labelElId", "basket");
							basketNode.label = "<%=resources.getString("kmelia.basket")%> (0)";
							displayTopicContent("1");
							oTreeView.draw();
						} else {
							alert(data);
						}
					}, 'text');
		}
	}

	function copyNode()	{
		top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+oCurrentTextNode.labelElId;
	}

	function copyCurrentNode()	{
		top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+getCurrentNodeId();
	}

	var nodeToCut;
	function cutNode() {
		nodeToCut = oCurrentTextNode.labelElId;
		top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+nodeToCut;
	}

	function cutCurrentNode() {
		nodeToCut = getCurrentNodeId();
		top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+nodeToCut;
	}

	function topicWysiwyg() {
		updateTopicWysiwyg(oCurrentTextNode.labelElId);
	}

	function changeTopicStatus() {
		changeStatus(oCurrentTextNode.labelElId);
	}

	function changeCurrentTopicStatus() {
		changeStatus(getCurrentNodeId());
	}

	function changeStatus(nodeId)
	{
		closeWindows();
		var node = oTreeView.getNodeByProperty("labelElId", nodeId);
		var currentStatus = oCurrentTextNode.data.status;
		var newStatus = "Visible";
		if (currentStatus == "Visible")
			newStatus = "Invisible";

		if (newStatus == 'Invisible')
		{
			question = '<%=kmeliaScc.getString("TopicVisible2InvisibleRecursive")%>';
		}
		else
		{
			question = '<%=kmeliaScc.getString("TopicInvisible2VisibleRecursive")%>';
		}

		var recursive = "0";
		if(window.confirm(question)){
			recursive = "1";
		}

		$.get('<%=m_context%>/KmeliaAJAXServlet', {ComponentId:'<%=componentId%>',Action:'UpdateTopicStatus',Id:nodeId,Status:newStatus,Recursive:recursive},
				function(data){
					if (data == "ok")
					{
						oCurrentTextNode.data.status = newStatus;

						//changing label style according to topic's new status
						if (newStatus == "Invisible") {
							oCurrentTextNode.labelStyle = "invisibleTopic";
						} else {
							oCurrentTextNode.labelStyle = "ygtvlabel";
						}
						oTreeView.draw();
					}
					else
					{
						alert(data);
					}
				}, 'text');
	}

	function pasteFromTree()
	{
		pasteNode(oCurrentTextNode.labelElId);
	}

	function sortTopics() {
		closeWindows();
		SP_openWindow("ToOrderTopics?Id="+oCurrentTextNode.labelElId, "topicAddWindow", "600", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised,resizable");
	}

	/*
    "contextmenu" event handler for the element(s) that
    triggered the display of the ContextMenu instance - used
    to set a reference to the TextNode instance that triggered
    the display of the ContextMenu instance.
	*/
	<% if (userCanManageTopics) { %>
		function onTriggerContextMenu(p_oEvent)
		{
	    	//alert("onTriggerContextMenu : enter");
		    var oTarget = this.contextEventTarget;
	
		    /*
		         Get the TextNode instance that that triggered the
		         display of the ContextMenu instance.
		    */
		    oCurrentTextNode = oTreeView.getNodeByElement(oTarget);
		    if (!oCurrentTextNode) {
		        // Cancel the display of the ContextMenu instance.
		        this.cancel();
		    }
	
		    if (oCurrentTextNode)
		    {
			    //get profile to display more or less context actions
				$.getJSON("<%=m_context%>/KmeliaJSONServlet?Id="+oCurrentTextNode.labelElId+"&Action=GetTopic&ComponentId=<%=componentId%>&Language=<%=language%>&IEFix="+new Date().getTime(),
						function(data){
							try
							{
								var profile = data[0].role;
								if (profile == "admin")
								{
									//oContextMenu.cfg.setProperty("visible", true);
									//all actions are enabled
									oContextMenu.getItem(0).cfg.setProperty("disabled", false);
									oContextMenu.getItem(1).cfg.setProperty("disabled", false);
									oContextMenu.getItem(2).cfg.setProperty("disabled", false);
									oContextMenu.getItem(3).cfg.setProperty("disabled", false);
	
									oContextMenu.getItem(0,1).cfg.setProperty("disabled", false);
									oContextMenu.getItem(1,1).cfg.setProperty("disabled", false);
									oContextMenu.getItem(2,1).cfg.setProperty("disabled", false);
								}
								else if (profile == "user")
								{
									var parentProfile =  oCurrentTextNode.parent.data.role;
									if (parentProfile != "admin")
									{
										//do not show the menu
										oContextMenu.cfg.setProperty("visible", false);
									}
									else
									{
										//oContextMenu.cfg.setProperty("visible", true);
										oContextMenu.getItem(0).cfg.setProperty("disabled", true);
										oContextMenu.getItem(1).cfg.setProperty("disabled", false);
										oContextMenu.getItem(2).cfg.setProperty("disabled", false);
										oContextMenu.getItem(3).cfg.setProperty("disabled", true);
	
										oContextMenu.getItem(0,1).cfg.setProperty("disabled", true);
										oContextMenu.getItem(1,1).cfg.setProperty("disabled", true);
										oContextMenu.getItem(2,1).cfg.setProperty("disabled", true);
									}
								}
								else
								{
									var isTopicManagementDelegated = <%=kmeliaScc.isTopicManagementDelegated()%>;
									var userId = "<%=kmeliaScc.getUserId()%>";
									var creatorId = data[0].creatorId;
									if (isTopicManagementDelegated && profile != "admin")
									{
										if (creatorId != userId)
										{
											//do not show the menu
											oContextMenu.cfg.setProperty("visible", false);
										}
										else if (creatorId == userId)
										{
											//oContextMenu.cfg.setProperty("visible", true);
											oContextMenu.getItem(0,1).cfg.setProperty("disabled", true);
											oContextMenu.getItem(1,1).cfg.setProperty("disabled", true);
											oContextMenu.getItem(2,1).cfg.setProperty("disabled", true);
	
											oContextMenu.getItem(0,2).cfg.setProperty("disabled", true);
											oContextMenu.getItem(1,2).cfg.setProperty("disabled", true);
										}
									}
									else
									{
										if (profile != "admin")
										{
											//do not show the menu
											oContextMenu.cfg.setProperty("visible", false);
										}
									}
								}
	
								<% if (kmeliaScc.isOrientedWebContent()) { %>
									if (data[0].status == "Invisible")
									{
										oContextMenu.getItem(1,2).cfg.setProperty("text", "<%=kmeliaScc.getString("TopicInvisible2Visible")%>");
									}
									else
									{
										oContextMenu.getItem(1,2).cfg.setProperty("text", "<%=kmeliaScc.getString("TopicVisible2Invisible")%>");
									}
								<% } %>
							} catch (e) {
								//do nothing
								//alert(e);
							}
						});
		    }
		}

		/*
		    Instantiate a ContextMenu:  The first argument passed to the constructor
		    is the id for the Menu element to be created, the second is an
		    object literal of configuration properties.
		*/
		var oContextMenu = new YAHOO.widget.ContextMenu(
		    "mytreecontextmenu",
		    {
		        trigger: "treeDiv1",
		        hideDelay: 100,
		        effect: {
	                effect: YAHOO.widget.ContainerEffect.FADE,
	                duration: 0.30
	            },
		        lazyload: true,
		        itemdata: [
			        [
			            { text: "<%=resources.getString("CreerSousTheme")%>", onclick: { fn: addNode } },
			            { text: "<%=resources.getString("ModifierSousTheme")%>", onclick: { fn: editNodeLabel } },
			            { text: "<%=resources.getString("SupprimerSousTheme")%>", onclick: { fn: deleteNodeFromTreeview } },
			            { text: "<%=resources.getString("kmelia.SortTopics")%>", onclick: { fn: sortTopics } }
			        ],
		            [
			            { text: "<%=resources.getString("GML.copy")%>", onclick: { fn: copyNode } },
		            	{ text: "<%=resources.getString("GML.cut")%>", onclick: { fn: cutNode } },
		            	{ text: "<%=resources.getString("GML.paste")%>", onclick: { fn: pasteFromTree } }
		    		],
		    		[
			    		<% if (kmeliaScc.isOrientedWebContent()) { %>
			            	{ text: "<%=kmeliaScc.getString("TopicWysiwyg")%>", onclick: { fn: topicWysiwyg } },
			            	{ text: "<%=kmeliaScc.getString("TopicVisible2Invisible")%>", onclick: { fn: changeTopicStatus } }
			            <% } else if (kmeliaScc.isWysiwygOnTopicsEnabled()) { %>
			            	{ text: "<%=kmeliaScc.getString("TopicWysiwyg")%>", onclick: { fn: topicWysiwyg } }
			            <% } %>
		    		]
		    	]
		    }
		);

		oContextMenu.subscribe("triggerContextMenu", onTriggerContextMenu);
		YAHOO.util.Event.addListener("mytreecontextmenu", "mouseout", oContextMenu.hide);
	<% } %>

	var oBasketContextMenu = new YAHOO.widget.ContextMenu(
		    "basketcontextmenu",
		    {
		        trigger: "basket",
		        hideDelay: 100,
		        effect: {
	                effect: YAHOO.widget.ContainerEffect.FADE,
	                duration: 0.30
	            },
		        lazyload: true,
		        itemdata: [
		            { text: "<%=resources.getString("EmptyBasket")%>", onclick: { fn: emptyTrash } }
		        ]
		    }
		);

	var oValidateContextMenu = new YAHOO.widget.ContextMenu(
		    "tovalidatecontextmenu",
		    {
		        trigger: "tovalidate",
		        lazyload: true
		    }
		);

	<% if (userCanManageRoot) { %>
  	function onTriggerRootContextMenu(p_oEvent)
	{
		//alert("onTriggerContextMenu : enter");
	    var oTarget = this.contextEventTarget;
	
	    /*
	         Get the TextNode instance that that triggered the
	         display of the ContextMenu instance.
	    */
	    oCurrentTextNode = oTreeView.getNodeByElement(oTarget);
	    if (!oCurrentTextNode) {
	        // Cancel the display of the ContextMenu instance.
	        this.cancel();
	    }
	}

	var oRootContextMenu = new YAHOO.widget.ContextMenu(
		    "rootcontextmenu",
		    {
		    	trigger: "ygtvtableel1",
			    hideDelay: 100,
	        	effect: {
                	effect: YAHOO.widget.ContainerEffect.FADE,
                	duration: 0.30
            	},
		        lazyload: true,
		        itemdata: [
		   		        [
		   		            { text: "<%=resources.getString("CreerSousTheme")%>", onclick: { fn: addNode } },
		   		            { text: "<%=resources.getString("kmelia.SortTopics")%>", onclick: { fn: sortTopics } }
		   		        ],
		   	            [
		   		            { text: "<%=resources.getString("GML.paste")%>", onclick: { fn: pasteFromTree } }
		   	    		]
		   	    		<% if (kmeliaScc.isOrientedWebContent() || kmeliaScc.isWysiwygOnTopicsEnabled()) { %>
		   	    		,[
		            		{ text: "<%=kmeliaScc.getString("TopicWysiwyg")%>", onclick: { fn: topicWysiwyg } }
		   	    		]
		            	<% } %>
		    	]
		    }
		);

	oRootContextMenu.subscribe("triggerContextMenu", onTriggerRootContextMenu);
	<% } %>

	function displayTopicContent(id)
	{
		clearSearchQuery();

		if (id != "1" && id != "tovalidate")
		{
			//highlight current topic
			$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'normal'});

			try {
				var node = oTreeView.getNodeByProperty("labelElId", id);
				setCurrentNodeId(node.data.id);
				currentNodeIndex = node.index;
				$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'bold'});
			} catch (e) {
				//TO FIX
			}
		}

		if (id == "tovalidate" || id == "1") {
			$("#DnD").css({'display':'none'}); //hide dropzone
			$("#footer").css({'visibility':'hidden'}); //hide footer
			$("#searchZone").css({'display':'none'}); //hide search

			if (id == "tovalidate")	{
				$("#menutoggle").css({'display':'none'}); //hide operations
				displayPublicationsToValidate();

				//update breadcrumb
                removeBreadCrumbElements();
                addBreadCrumbElement("#", "<%=resources.getString("ToValidate")%>");
			} else {
				displayOperations(id);
				displayPublications(id);
				displayPath(id);
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
(function() {
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        col1 = null
        col2 = null;

  	//Add an onDOMReady handler to build the tree and the resize bar when the document is ready
    Event.onDOMReady(function() {

        //build the tree
    	initTree('<%=id%>');

    	//build resize bar
        var size = getWidth() - 57;
        col1 = Dom.get('treeDiv1');
        col2 = Dom.get('rightSide');
        var max = (size - 150);
        var resize = new YAHOO.util.Resize('treeDiv1', {
            handles: ['r'],
            minWidth: 150,
            maxWidth: max
        });
        resize.on('resize', function(ev) {
            var w = ev.width;
            Dom.setStyle(col2, 'width', (size - w - 41) + 'px');
        });
        resize.resize(null, 250, 250, 0, 0, true);

        //Resize height of treeview according to window height
        Dom.setStyle(col1, 'height', getHeight()-120 + 'px');

        var col3 = Dom.get('ygtv0');
        Dom.setStyle(col3, 'height', getHeight()-120 + 'px');

        <% if (displaySearch.booleanValue()) { %>
    		document.getElementById("topicQuery").focus();
    	<% } %>

    	<% if (KmeliaHelper.ROLE_ADMIN.equals(profile)) { %>
			//Right-click concerns only admins
	    	showRightClickHelp();
		<% } %>
    });
})();
</script>
</div>
<div id="rightClick-message" title="<%=resources.getString("kmelia.help.rightclick.title") %>" style="display: none;">
	<p>
		<%=resources.getStringWithParam("kmelia.help.rightclick.content", componentLabel) %>
	</p>
</div>
<view:progressMessage/>
</body>
</html>
