<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page import="com.stratelia.webactiv.SilverpeasRole"%>

<%
String		rootId				= "0";
String		description			= "";
String		namePath			= "";
String		urlTopic			= "";

//R?cup?ration des param?tres
String 	profile			= (String) request.getAttribute("Profile");
String  translation 	= (String) request.getAttribute("Language");
boolean	isGuest			= ((Boolean) request.getAttribute("IsGuest")).booleanValue();
Boolean displaySearch	= (Boolean) request.getAttribute("DisplaySearch");
boolean updateChain		= ((Boolean) request.getAttribute("HaveDescriptor")).booleanValue();

TopicDetail currentTopic 		= (TopicDetail) request.getAttribute("CurrentTopic");

String 		pathString 			= (String) request.getAttribute("PathString");

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox)

String id = currentTopic.getNodeDetail().getNodePK().getId();
String language = kmeliaScc.getLanguage();

NodeDetail nodeDetail = currentTopic.getNodeDetail();

List path = (List) kmeliaScc.getNodeBm().getPath(currentTopic.getNodePK());

if (id == null) {
	id = rootId;
}

//For Drag And Drop
boolean dragAndDropEnable = kmeliaScc.isDragAndDropEnable();

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

String userId = kmeliaScc.getUserId();

ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();

boolean userCanCreatePublications = SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile) || SilverpeasRole.writer.isInRole(profile);
boolean userCanValidatePublications = SilverpeasRole.admin.isInRole(profile) || SilverpeasRole.publisher.isInRole(profile);

%>

<HTML>
<HEAD>
<meta http-equiv="content-type" content="text/html; charset=utf-8">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript" src="javaScript/dragAndDrop.js"></script>
<script type="text/javascript" src="javaScript/navigation.js"></script>
<script type="text/javascript" src="javaScript/searchInTopic.js"></script>
<script type="text/javascript">
<% if (!profile.equals("user")) { %>
function updateChain()
{
    document.updateChain.submit();
}
<% } %>

function getCurrentNodeId() {
	return "0";
}

function getWebContext() {
	return "<%=m_context%>";
}

function getComponentId() {
	return "<%=componentId%>";
}

function showDnD()
{
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
	%>
	showHideDragDrop('<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeNormal_<%=language%>.html','<%=URLManager.getFullApplicationURL(request)%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&Draft=1&SessionId=<%=session.getId()%>','<%=URLManager.getFullApplicationURL(request)%>/upload/ModeDraft_<%=language%>.html','<%=resources.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
}

function fileUpload()
{
    document.fupload.submit();
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

function topicWysiwyg()
{
	closeWindows();
	document.topicDetailForm.action = "ToTopicWysiwyg";
	document.topicDetailForm.ChildId.value = "0";
	document.topicDetailForm.submit();
}

function pasteFromOperations() {
	$.progressMessage();
	var ieFix = new Date().getTime();
	$.get('<%=m_context%>/KmeliaJSONServlet', {Action:'Paste',ComponentId:'<%=componentId%>',Language:'<%=language%>',IEFix:ieFix},
			function(data){
				displayPublications("0");
				$.closeProgressMessage();
			},"json");
}

$(document).ready(function() {
	displayPublications("<%=id%>");
	displayTopicDescription("0");
});
</script>
</HEAD>
<BODY id="kmelia" onUnload="closeWindows()" class="yui-skin-sam">
<div id="<%=componentId %>">
<%
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
        if (SilverpeasRole.admin.isInRole(profile)){
          	if (kmeliaScc.isPdcUsed()) {
	        	operationPane.addOperation("useless", resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+kmeliaScc.getComponentId()+"','utilizationPdc1')");
          	}
          	if (kmeliaScc.isContentEnabled()) {
	        	operationPane.addOperation(resources.getIcon("kmelia.modelUsed"), resources.getString("kmelia.ModelUsed"), "ModelUsed");
          	}
          	if (kmeliaScc.isWysiwygOnTopicsEnabled()) {
				operationPane.addOperation("useless", kmeliaScc.getString("TopicWysiwyg"), "javascript:onClick=topicWysiwyg('"+id+"')");
			}
          	if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportZipAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportPublications()");
          	}
          	if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportPdfAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ExportPDF"), "javascript:openExportPDFPopup()");
          	}
	        operationPane.addOperation(resources.getIcon("kmelia.sortPublications"), kmeliaScc.getString("kmelia.OrderPublications"), "ToOrderPublications");
			operationPane.addLine();
        }
        if (userCanCreatePublications) {
	        operationPane.addOperation("useless", kmeliaScc.getString("PubCreer"), "NewPublication");
	        if (kmeliaScc.isWizardEnabled()) {
	      		operationPane.addOperation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
	        }
	        if (kmeliaScc.isImportFileAllowed()) {
	      		operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ImportFile"), "javascript:onClick=importFile()");
	        }
	        if (kmeliaScc.isImportFilesAllowed()) {
	        	operationPane.addOperation("useless", kmeliaScc.getString("kmelia.ImportFiles"), "javascript:onClick=importFiles()");
	        }
	        if (updateChain) {
	        	operationPane.addOperation(resources.getIcon("kmelia.updateByChain"), kmeliaScc.getString("kmelia.updateByChain"), "javascript:onClick=updateChain()");
	        }
	        operationPane.addOperation(resources.getIcon("kmelia.paste"), resources.getString("GML.paste"), "javascript:onClick=pasteFromOperations()");
	        operationPane.addLine();
        }
                    	
    	if (!isGuest) {
    		operationPane.addOperation("useless", resources.getString("SubscriptionsAdd"), "javascript:onClick=addSubscription()");
      		operationPane.addOperation("useless", resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(namePath))+"','"+EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToJsString(description))+"','"+urlTopic+"')");
    	}
    	
    	if (userCanCreatePublications) {
      		operationPane.addLine();
          	operationPane.addOperation("useless", resources.getString("PubBasket"), "GoToBasket");
          	if (userCanValidatePublications) {
          		operationPane.addOperation("useless", resources.getString("ToValidate"), "ViewPublicationsToValidate");
          	}
  		}

    //Instanciation du cadre avec le view generator
	Frame frame = gef.getFrame();

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
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
					  if (dragAndDropEnable && userCanCreatePublications)
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

<form name="updateChain" action="UpdateChainInit">
</form>
</div>
<view:progressMessage/>
</BODY>
</HTML>