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
String		namePath			= "";
String		urlTopic			= "";

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

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", kmeliaScc.getLanguage());

//For Drag And Drop
boolean dragAndDropEnable = kmeliaScc.isDragAndDropEnable();

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

String userId = kmeliaScc.getUserId();

ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);

%>

<HTML>
<HEAD>
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

<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script src="<%=m_context%>/kmelia/jsp/javaScript/dragAndDrop.js" type="text/javascript"></script>

<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/yui/treeview/assets/skins/sam/treeview.css" />
<link rel="stylesheet" type="text/css" href="styleSheets/tree.css">
<link rel="stylesheet" type="text/css" href="<%=m_context%>/util/yui/resize/assets/skins/sam/resize.css" />

<style type="text/css" >
.tableFrame {
}
.hautFrame {
	/*width: 950px;*/
	/*border: 1px solid green;*/
}
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

#DropZone {
	padding-left: 5px;
	padding-right: 5px;
	margin-right: 5px;
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
#pubList {
	/*float: left;*/
	/*width: 100%;*/
	overflow: auto;
}

#DnD {
	/*float: left;*/
	/*width: 100%;*/
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

<script language="JavaScript1.2">

var subscriptionWindow = window;
var favoriteWindow = window;
var topicWindow = window;
var importFileWindow = window;
var importFilesWindow = window;
var exportComponentWindow = window;

function topicGoTo(id) {
    closeWindows();
    displayTopicContent(id);
}

function reloadPage(id) {
	closeWindows();
    document.topicDetailForm.action = "GoToTopic";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function dirGoTo(id) {
    closeWindows();
    document.topicDetailForm.action = "GoToDirectory";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

function clipboardCopy() {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id=<%=id%>';
}

function clipboardCut() {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id=<%=id%>';
}

<% if (!profile.equals("user")) { %>

function updateChain()
{
    document.updateChain.submit();
}

function topicAdd(topicId, isLinked) {
	//alert("topicAdd : topicId = "+topicId);
	if (!topicWindow.closed && topicWindow.name== "topicAddWindow")
		topicWindow.close();
    var url = "ToAddTopic?Id="+topicId+"&Translation=<%=translation%>";
    if (isLinked)
    	url += "&IsLink=true";
	<% if (rightsOnTopics.booleanValue()) { %>
		location.href = url;
	<% } else { %>
		topicWindow = SP_openWindow(url, "topicWindow", "570", "350", "directories=0,menubar=0,toolbar=0, alwaysRaised");
	<% } %>
}

function topicUpdate(id)
{
	document.topicDetailForm.ChildId.value = id;
    if (!topicWindow.closed && topicWindow.name== "topicUpdateWindow")
    	topicWindow.close();

	<% if (rightsOnTopics.booleanValue()) { %>
		location.href = "ToUpdateTopic?Id="+id+"&Translation=<%=translation%>";
	<% } else { %>
		topicWindow = SP_openWindow("ToUpdateTopic?Id="+id+"&Translation=<%=translation%>", "topicWindow", "550", "350", "directories=0,menubar=0,toolbar=0,alwaysRaised");
	<% } %>
}

<% } %>

function showDnD()
{
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
	if (profile.equals("publisher") || profile.equals("writer")) { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeNormal_<%=language%>.html','<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&Draft=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } else { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeNormal_<%=language%>.html','<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&Draft=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } %>
}

function addFavorite(name,description,url)
{
	urlWindow = "<%=m_context%>/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
    windowName = "favoriteWindow";
	larg = "550";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!favoriteWindow.closed && favoriteWindow.name== "favoriteWindow")
        favoriteWindow.close();
    favoriteWindow = SP_openWindow(urlWindow, windowName, larg, haut, windowParams);
}

function addSubscription() {
	//alert(getCurrentNodeId());
    url = "subscriptionsManager.jsp?Action=AddSubscription&Id="+getCurrentNodeId();
    windowName = "subscriptionWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
	larg = "550";
	haut = "350";
    if (!subscriptionWindow.closed && subscriptionWindow.name == "subscriptionWindow")
        subscriptionWindow.close();
	subscriptionWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function publicationGoTo(id){
    closeWindows();
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}
function publicationGoToFromMain(id){
    closeWindows();
    document.pubForm.CheckPath.value = "1";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function fileUpload()
{
    document.fupload.submit();
}

function importFile()
{
    url = "importOneFile.jsp?Action=ImportFileForm&TopicId="+getCurrentNodeId();
    windowName = "importFileWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
		larg = "610";
		haut = "370";
		if (!importFileWindow.closed && importFileWindow.name=="importFileWindow")
			importFileWindow.close();
    importFileWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function importFiles()
{
    url = "importMultiFiles.jsp?Action=ImportFilesForm&TopicId="+getCurrentNodeId();
    windowName = "importFilesWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
		larg = "610";
		haut = "460";
		if (!importFilesWindow.closed && importFilesWindow.name=="importFilesWindow")
			importFilesWindow.close();
    importFilesWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function openExportPDFPopup() {

	chemin = "ExportAttachementsToPDF?TopicId="+getCurrentNodeId();
	largeur = "700";
	hauteur = "500";
	SP_openWindow(chemin, "ExportWindow", largeur, hauteur, "scrollbars=yes, resizable=yes");

}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function exportPublications()
{
	exportComponentWindow = SP_openWindow("exportTopic.jsp?TopicId="+getCurrentNodeId(),"exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

function closeWindows() {
	if(!subscriptionWindow.closed && subscriptionWindow.name=="subscriptionWindow")
		subscriptionWindow.close();

	if (!favoriteWindow.closed && favoriteWindow.name=="favoriteWindow")
		favoriteWindow.close();

	if (!topicWindow.closed && topicWindow.name=="topicWindow")
		topicWindow.close();
}

function uploadCompleted(s)
{
	refreshPublications();
	return true;
}

function doPagination(index)
{
	var paramToValidate = "0";
	if (getCurrentNodeId() == "tovalidate") {
		paramToValidate = "1";
	}
	var topicQuery = getSearchQuery();
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:index,ComponentId:'<%=componentId%>',ToValidate:paramToValidate,Query:topicQuery,IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
}

function sortGoTo(selectedIndex) {
    closeWindows();
	if (selectedIndex != 0 && selectedIndex != 1) {
		var topicQuery = getSearchQuery();
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:0,Sort:sort,ComponentId:'<%=componentId%>',Query:topicQuery,IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
		return;
	}
}

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
	  if( typeof( window.innerWidth ) == 'number' ) {
	    //Non-IE
	    myHeight = window.innerHeight;
	  } else if( document.documentElement && document.documentElement.clientHeight) {
	    //IE 6+ in 'standards compliant mode'
	    myHeight = document.documentElement.clientHeight;
	  } else if( document.body && document.body.clientHeight) {
	    //IE 4 compatible
	    myHeight = document.body.clientHeight;
	  }
	  return myHeight;
}

function searchInTopic()
{
	var topicQuery = getSearchQuery();
	if (topicQuery != "" && topicQuery.length > 1) {
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:0,Query:topicQuery,ComponentId:'<%=componentId%>',IEFix:ieFix},
				function(data){
					$('#pubList').html(data);
				},"html");
	}
	return;
}

function checkSubmitToSearch(ev)
{
	var touche = ev.keyCode;
	if (touche == 13) {
		searchInTopic();
	}
}

function getSearchQuery()
{
	var topicQuery = "";
	if (document.getElementById("topicQuery") != null) {
		topicQuery = document.getElementById("topicQuery").value;
	}
	return topicQuery;
}

function clearSearchQuery()
{
	try {
		if (document.getElementById("topicQuery") != null) {
			document.getElementById("topicQuery").value = "";
		}
	} catch (e) {
	}
}

</script>
</HEAD>
<BODY id="kmelia" onUnload="closeWindows()" class="yui-skin-sam">
<div id="<%=componentId %>">
<%
        namePath = "";

        urlTopic = nodeDetail.getLink();

        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setI18N("GoToCurrentTopic", translation);

        // cr?ation du nom pour les favoris
        namePath = spaceLabel + " > " + componentLabel;
         if (!pathString.equals(""))
        	namePath = namePath + " > " + pathString;

        //Display operations
        OperationPane operationPane = window.getOperationPane();
        operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+kmeliaScc.getComponentId()+"','utilizationPdc1')");
        operationPane.addOperation(resources.getIcon("kmelia.modelUsed"), resources.getString("kmelia.ModelUsed"), "ModelUsed");
        operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportPublications()");
        operationPane.addOperation(importFileSrc, kmeliaScc.getString("kmelia.ExportPDF"), "javascript:openExportPDFPopup()");
		operationPane.addLine();
        operationPane.addOperation(publicationAddSrc, kmeliaScc.getString("PubCreer"), "NewPublication");
      	operationPane.addOperation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
      	operationPane.addOperation(importFileSrc, kmeliaScc.getString("kmelia.ImportFile"), "javascript:onClick=importFile()");
        operationPane.addOperation(importFilesSrc, kmeliaScc.getString("kmelia.ImportFiles"), "javascript:onClick=importFiles()");
        operationPane.addOperation(resources.getIcon("kmelia.sortPublications"), kmeliaScc.getString("kmelia.OrderPublications"), "ToOrderPublications");
        operationPane.addOperation(resources.getIcon("kmelia.updateByChain"), kmeliaScc.getString("kmelia.updateByChain"), "javascript:onClick=updateChain()");
        operationPane.addOperation(resources.getIcon("kmelia.paste"), resources.getString("GML.paste"), "javascript:onClick=pasteFromOperations()");
    	operationPane.addLine();
    	operationPane.addOperation(subscriptionAddSrc, resources.getString("SubscriptionsAdd"), "javascript:onClick=addSubscription()");
      	operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(namePath))+"','"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(description))+"','"+urlTopic+"')");

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
					<div id="footer" class="txtBaseline">
					</div>
				</div>
			</div>
			</div>

	<%
		out.println(frame.printAfter());
		out.println(window.printAfter());
	%>

<FORM NAME="topicDetailForm" METHOD="POST">
	<input type="hidden" name="Id" value="<%=id%>">
	<input type="hidden" name="Path" value="<%=EncodeHelper.javaStringToHtmlString(pathString)%>">
	<input type="hidden" name="ChildId">
	<input type="hidden" name="Status"><input type="hidden" name="Recursive">
</FORM>

<FORM NAME="pubForm" action="ViewPublication" METHOD="POST">
	<input type="hidden" name="PubId">
	<input type="hidden" name="CheckPath" value="1">
</FORM>

<FORM NAME="fupload" ACTION="fileUpload.jsp" METHOD="POST" enctype="multipart/form-data" accept-charset="UTF-8">
	<input type="hidden" name="Action" value="initial">
</FORM>

<form name="frm_report" action="GoToTopic">
  	<input type="hidden" name="Id" value="<%=id%>">
</form>
<form name="updateChain" action="UpdateChainInit">
</form>
<script type="text/javascript">

//Declarations
var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;

var oTreeView;
var root;
var currentNodeId;
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

function getCurrentNodeId()
{
	return currentNodeId;
}

function setCurrentNodeId(id)
{
	//alert("setCurrentNodeId : id = "+id);
	currentNodeId = id;
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
	function addNode()
	{
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

	/*
	    Deletes the TextNode that was the target of the "contextmenu"
	    event that triggered the display of the ContextMenu instance.
	*/
	function deleteNode()
	{
		var nodeId = oCurrentTextNode.labelElId;
		if(window.confirm("<%=kmeliaScc.getString("ConfirmDeleteTopic")%> '" + oCurrentTextNode.data.name + "' ?"))
		{
			$.get('<%=m_context%>/KmeliaAJAXServlet', { Id:nodeId,ComponentId:'<%=componentId%>',Action:'Delete'},
					function(data){
						if (data == "ok")
						{
							oTreeView.removeNode(oCurrentTextNode);
			                oTreeView.draw();
						}
						else
						{
							alert(data);
						}
					});
		}
	}

	function emptyTrash()
	{
		if(window.confirm("<%=kmeliaScc.getString("ConfirmFlushTrashBean")%>"))
		{
			$.progressMessage();
			$.get('<%=m_context%>/KmeliaAJAXServlet', {ComponentId:'<%=componentId%>',Action:'EmptyTrash'},
					function(data){
						$.closeProgressMessage();
						if (data == "ok")
						{
							displayPublications("1");
							//alert("Corbeille vid?e avec succ?s !");
						}
						else
						{
							alert(data);
						}
					});
		}
	}

	function copyNode()
	{
		top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id='+oCurrentTextNode.labelElId;
	}

	var nodeToCut;
	function cutNode()
	{
		nodeToCut = oCurrentTextNode.labelElId;
		top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id='+oCurrentTextNode.labelElId;
	}

	function topicWysiwyg()
	{
		closeWindows();
		document.topicDetailForm.action = "ToTopicWysiwyg";
		document.topicDetailForm.ChildId.value = oCurrentTextNode.labelElId;
		document.topicDetailForm.submit();
	}

	function changeTopicStatus()
	{
		closeWindows();
		var nodeId = oCurrentTextNode.labelElId;
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
				});
	}

	function pasteFromOperations()
	{
		//alert("paste : currentNodeId = "+getCurrentNodeId());
		pasteNode(getCurrentNodeId());
	}

	function pasteFromTree()
	{
		pasteNode(oCurrentTextNode.labelElId);
	}

	function pasteNode(id)
	{
		$.progressMessage();

		//alert("pasteNode : id = "+id);
		//prepare URL for XHR request:
        var sUrl = "<%=m_context%>/KmeliaJSONServlet?Action=Paste&ComponentId=<%=componentId%>&Language=<%=language%>&Id="+id+"&IEFix="+new Date().getTime();

        //prepare our callback object
        var callback = {

            //if our XHR call is successful, we want to make use
            //of the returned data and create child nodes.
            success: function(oResponse) {
                var messages = [];
                // Use the JSON Utility to parse the data returned from the server
                try {
                    messages = YAHOO.lang.JSON.parse(oResponse.responseText);
                }
                catch (x) {
                    alert("JSON Parse failed!");
                    return;
                }

                /*var path = "";
                // The returned data was parsed into an array of objects.
                for (var i = 0; i < messages.length ; i++) {
                    var m = messages[i];
                    var oChildNode = new YAHOO.widget.TextNode(m.name, oCurrentTextNode, false);
                    oChildNode.labelElId = m.id;
                    //oChildNode.href = "javascript:displayTopicContent("+m.id+")";
                    oChildNode.editable = true;
                    if (m.id == nodeToCut)
                    {
                    	var node = oTreeView.getNodeByProperty("labelElId", m.id);
                    	oTreeView.removeNode(node);
                    }
                }
                oCurrentTextNode.refresh();
				oCurrentTextNode.expand();
				oTreeView.draw();

				displayPublications(oCurrentTextNode.labelElId);*/

				reloadPage(id);

				$.closeProgressMessage();
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

	function sortTopics()
	{
		closeWindows();
		SP_openWindow("ToOrderTopics?Id="+oCurrentTextNode.labelElId, "topicAddWindow", "600", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised,resizable");
	}

	/*
    "contextmenu" event handler for the element(s) that
    triggered the display of the ContextMenu instance - used
    to set a reference to the TextNode instance that triggered
    the display of the ContextMenu instance.
	*/
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
			if (oCurrentTextNode.labelElId == "basket" || oCurrentTextNode.labelElId == "tovalidate")
			{
				//do not show the menu
				oContextMenu.cfg.setProperty("visible", false);
			}
			else
			{
			    //get profile to display more or less context actions
				$.getJSON("<%=m_context%>/KmeliaJSONServlet?Id="+oCurrentTextNode.labelElId+"&Action=GetTopic&ComponentId=<%=componentId%>&Language=<%=language%>&IEFix="+new Date().getTime(),
						function(data){
							try
							{
								var profile = data[0].role;
								var parentProfile =  oCurrentTextNode.parent.data.role;
								if (profile == "admin")
								{
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
									if (parentProfile != "admin")
									{
										//do not show the menu
										oContextMenu.cfg.setProperty("visible", false);
									}
									else
									{
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
	}

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

	    <% if (!"admin".equals(kmeliaScc.getUserTopicProfile("0"))) { %>
	    	this.cancel();
	    <% } %>
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
		            { text: "<%=resources.getString("SupprimerSousTheme")%>", onclick: { fn: deleteNode } },
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

	/*
	     Subscribe to the "contextmenu" event for the element(s)
	     specified as the "trigger" for the ContextMenu instance.
	*/
	oBasketContextMenu.subscribe("triggerContextMenu", onTriggerContextMenu);
	oValidateContextMenu.subscribe("triggerContextMenu", onTriggerContextMenu);
	oContextMenu.subscribe("triggerContextMenu", onTriggerContextMenu);
	oRootContextMenu.subscribe("triggerContextMenu", onTriggerRootContextMenu);

	YAHOO.util.Event.addListener("mytreecontextmenu", "mouseout", oContextMenu.hide);

	function displayTopicContent(id)
	{
		var node = oTreeView.getNodeByProperty("labelElId", id);
		//alert(node.data.name);

		clearSearchQuery();

		if (id != "1" && id != "tovalidate")
		{
			//highlight current topic
			$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'normal'});

			try
			{
				setCurrentNodeId(node.data.id);
				currentNodeIndex = node.index;
				$("#ygtvcontentel"+currentNodeIndex).css({'font-weight':'bold'});
			}
			catch (e)
			{
				//TO FIX
			}
		}

		if (id == "tovalidate" || id == "1")
		{
			$("#DnD").css({'display':'none'}); //hide dropzone
			$("#menutoggle").css({'display':'none'}); //hide operations
			$("#footer").css({'visibility':'hidden'}); //hide footer
			$("#searchZone").css({'display':'none'}); //hide search

			if (id == "tovalidate")
			{
				displayPublicationsToValidate();

				//update breadcrumb
                removeBreadCrumbElements();
                addBreadCrumbElement("#", "<%=resources.getString("ToValidate")%>");
			}
			else
			{
				displayPublications(id);
				displayPath(id);
			}
		}
		else
		{
			displayPublications(id);
			displayPath(id);
			getProfile(id);
			if (id != "0" || <%=kmeliaScc.getNbPublicationsOnRoot() == 0%>) {
				$("#searchZone").css({'display':'block'});
			} else if (<%=kmeliaScc.getNbPublicationsOnRoot() != 0%>) {
				$("#searchZone").css({'display':'none'}); //hide search
			}
		}

		//display topic information
		if (id != "0" && id != "1" && id != "tovalidate")
		{
			$("#footer").css({'visibility':'visible'});
			$("#footer").html("<%=EncodeHelper.javaStringToJsString(resources.getString("kmelia.topic.info"))%>"+node.data.creatorName+" - "+node.data.date+" - <a id=\"topicPermalink\" href=\"#\"><img src=\"<%=resources.getIcon("kmelia.link")%>\"/></a>");
			$("#footer #topicPermalink").attr("href", "<%=m_context%>/Topic/"+id+"?ComponentId=<%=componentId%>");
		}
		else
		{
			$("#footer").css({'visibility':'hidden'});
		}

		//display topic rich description
		displayTopicDescription(id);
	}

	function checkDnD(id, profile)
	{
		var displayIt = true;
		if (id == "0" && <%=kmeliaScc.getNbPublicationsOnRoot() != 0 && kmeliaScc.isTreeStructure()%>)
		{
			displayIt = false;
		}
		else if (id == "1")
		{
			displayIt = false;
		}
		else
		{
			displayIt = (profile == "admin" || profile == "publisher" || profile == "writer");
		}
		//alert("checkDnD : "+displayIt);
		if (displayIt)
		{
			$("#DnD").css({'display':'block'});
		}
		else
		{
			$("#DnD").css({'display':'none'});
		}
	}

	function checkOperations(id, profile)
	{
		if (id == "1")
		{
			$("#menutoggle").css({'display':'none'});
		}
		else
		{
			$("#menutoggle").css({'display':'block'});
		}

		oMenu.getItem(0,0).cfg.setProperty("classname", "operationHidden"); //pdc
		if (id == "0" && <%=kmeliaScc.isPdcUsed()%> && profile == "admin")
			oMenu.getItem(0,0).cfg.setProperty("classname", "operationVisible");

		oMenu.getItem(1,0).cfg.setProperty("classname", "operationHidden"); //templates
		if (<%=kmeliaScc.isContentEnabled()%> && profile == "admin")
			oMenu.getItem(1,0).cfg.setProperty("classname", "operationVisible");

		oMenu.getItem(2,0).cfg.setProperty("classname", "operationHidden"); //export
		if (<%=kmeliaScc.isExportComponentAllowed()%> && <%=kmeliaScc.isExportZipAllowed()%> && profile == "admin")
		{
			oMenu.getItem(2,0).cfg.setProperty("classname", "operationVisible");
			if (id == "0")
				oMenu.getItem(2,0).cfg.setProperty("text", "<%=resources.getString("kmelia.ExportComponent")%>");
			else
				oMenu.getItem(2,0).cfg.setProperty("text", "<%=resources.getString("kmelia.ExportTopic")%>");
		}

		oMenu.getItem(3,0).cfg.setProperty("classname", "operationHidden"); //export PDF
	 	if (<%=kmeliaScc.isExportComponentAllowed()%> && <%=kmeliaScc.isExportPdfAllowed()%> && (profile == "admin" || profile == "publisher"))
			oMenu.getItem(3,0).cfg.setProperty("classname", "operationVisible");

	 	oMenu.getItem(0,1).cfg.setProperty("classname", "operationHidden"); //add publi
	 	oMenu.getItem(1,1).cfg.setProperty("classname", "operationHidden"); //wizard
	 	oMenu.getItem(2,1).cfg.setProperty("classname", "operationHidden"); //import file
	 	oMenu.getItem(3,1).cfg.setProperty("classname", "operationHidden"); //import files
	 	oMenu.getItem(4,1).cfg.setProperty("classname", "operationHidden"); //sort publis
	 	oMenu.getItem(5,1).cfg.setProperty("classname", "operationHidden"); //update chain
	 	oMenu.getItem(6,1).cfg.setProperty("classname", "operationHidden"); //paste


	 	if ((id != "0" && id != "1") || (id == "0" && (<%=kmeliaScc.getNbPublicationsOnRoot() == 0%> || <%=!kmeliaScc.isTreeStructure()%>)))
	 	{
		 	if (profile != "user")
		 	{
				oMenu.getItem(0,1).cfg.setProperty("classname", "operationVisible");
				if (<%=kmeliaScc.isWizardEnabled()%>)
					oMenu.getItem(1,1).cfg.setProperty("classname", "operationVisible");
				if (<%=kmeliaScc.isImportFileAllowed()%>)
					oMenu.getItem(2,1).cfg.setProperty("classname", "operationVisible");
				if (<%=kmeliaScc.isImportFilesAllowed()%>)
					oMenu.getItem(3,1).cfg.setProperty("classname", "operationVisible");
				oMenu.getItem(6,1).cfg.setProperty("classname", "operationVisible"); //paste
		 	}
		 	if (profile == "admin")
		 	{
		 		oMenu.getItem(4,1).cfg.setProperty("classname", "operationVisible"); //sort publis

		 	}
		var node = oTreeView.getNodeByProperty("labelElId", id);
		if (node.data.updateChain)
		{
			oMenu.getItem(5,1).cfg.setProperty("classname", "operationVisible"); //update chain

		}
		oMenu.getItem(0,2).cfg.setProperty("classname", "operationVisible"); //subscriptions
		oMenu.getItem(1,2).cfg.setProperty("classname", "operationVisible"); //favorites
	 	}
	}

	function getProfile(id)
	{
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/KmeliaAJAXServlet', { Id:id,Action:'GetProfile',ComponentId:'<%=componentId%>',IEFix:ieFix},
				function(data){
					//display dNd according rights
					checkDnD(id, data);
					checkOperations(id, data);
				});
	}

	function displayPublications(id)
	{
		//display publications of topic
		var pubIdToHighlight = "<%=pubIdToHighlight%>";
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Id:id,ComponentId:'<%=componentId%>',PubIdToHighlight:pubIdToHighlight,IEFix:ieFix},
				function(data){
					$('#pubList').html(data);
				},"html");
	}

	function displayPublicationsToValidate()
	{
		//display publications to validate
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {ComponentId:'<%=componentId%>',ToValidate:1,IEFix:ieFix},
				function(data){
					$('#pubList').html(data);
				},"html");
	}

	function refreshPublications()
	{
		var nodeId = getCurrentNodeId();
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Id:nodeId,ComponentId:'<%=componentId%>',IEFix:ieFix},
				function(data){
					$('#pubList').html(data);
				},"html");
	}

	function displayTopicDescription(id)
	{
		//display rich description of topic
		var ieFix = new Date().getTime();
		$.get('<%=m_context%>/KmeliaAJAXServlet', {Id:id,Action:'GetTopicWysiwyg',ComponentId:'<%=componentId%>',IEFix:ieFix},
				function(data){
					$("#topicDescription").html(data);
				},"html");
	}

	function displayPath(id)
	{
		//prepare URL for XHR request:
        var sUrl = "<%=m_context%>/KmeliaJSONServlet?Action=GetPath&ComponentId=<%=componentId%>&Language=<%=language%>&Id="+id+"&IEFix="+new Date().getTime();

        //prepare our callback object
        var callback = {

            //if our XHR call is successful, we want to make use
            //of the returned data and create child nodes.
            success: function(oResponse) {
                var messages = [];
                // Use the JSON Utility to parse the data returned from the server
                try {
                    messages = YAHOO.lang.JSON.parse(oResponse.responseText);
                }
                catch (x) {
                    alert("JSON Parse failed!");
                    return;
                }

                //remove topic breadcrumb
                removeBreadCrumbElements();

                // The returned data was parsed into an array of objects.
                for (var i = messages.length-1; i >= 0 ; i--) {
                    var m = messages[i];
                    if (m.id != 0) {
                    	addBreadCrumbElement("javascript:topicGoTo("+m.id+")", m.name);
                    }
                }
                //alert(path);
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
</script>
<script>
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
    });
})();
</script>
</div>
<view:progressMessage/>
</BODY>
</HTML>