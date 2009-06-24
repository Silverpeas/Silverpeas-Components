<%@ include file="checkKmelia.jsp" %>
<%@ include file="topicReport.jsp.inc" %>
<%@ include file="publicationsList.jsp.inc" %>

<%!
  //Icons
  String folderSrc;
  String topicAddSrc;
  String topicBasketSrc;
  String topicDZSrc;
  String subscriptionAddSrc;
  String subscriptionSrc;
  String favoriteAddSrc;
  String favoriteSrc;
  String publicationAddSrc;
  String publicationSrc;
  String fullStarSrc;
  String emptyStarSrc;
  String topicSrc;
  String topicUpdateSrc;
  String topicDeleteSrc;
  String topicInfoSrc;
  String topicAccueilInfoSrc;
  String topicUpSrc;
  String topicDownSrc;
  String topicVisibleSrc;
  String topicInvisibleSrc;
  String pubToValidateSrc;
  String pdcUtilizationSrc;
  String emptyBasketSrc;
%>

<%
String		rootId				= "0";
String		name				= "";
String		description			= "";
String		alertType			= "";
String		fatherId			= "";
String		namePath			= "";
String		urlTopic			= "";

//Récupération des paramètres
String 	sort			= request.getParameter("Sort");
String 	childId			= request.getParameter("ChildId");
String 	profile			= (String) request.getAttribute("Profile");
List 	treeview 		= (List) request.getAttribute("Treeview");
String  translation 	= (String) request.getAttribute("Language");
boolean	isGuest			= ((Boolean) request.getAttribute("IsGuest")).booleanValue();

Boolean displayNbPublis = (Boolean) request.getAttribute("DisplayNBPublis");
Boolean rightsOnTopics  = (Boolean) request.getAttribute("RightsOnTopicsEnabled");

TopicDetail currentTopic 		= (TopicDetail) request.getAttribute("CurrentTopic");
String 		pathString 			= (String) request.getAttribute("PathString");
String 		linkedPathString 	= (String) request.getAttribute("LinkedPathString");

String		pubIdToHighlight	= (String) request.getAttribute("PubIdToHighlight"); //used when we have found publication from search (only toolbox) 

String id = currentTopic.getNodeDetail().getNodePK().getId();
boolean useTreeview = (treeview != null);
String language = kmeliaScc.getLanguage();

NodeDetail nodeDetail = currentTopic.getNodeDetail();

//Icons
folderSrc			= m_context + "/util/icons/component/kmeliaSmall.gif";
topicAddSrc			= m_context + "/util/icons/folderAddBig.gif";
topicAccueilInfoSrc = m_context + "/util/icons/folderInfoBig.gif";
topicBasketSrc		= m_context + "/util/icons/pubTrash.gif";
emptyBasketSrc		= m_context + "/util/icons/basketDelete.gif";
topicDZSrc			= m_context + "/util/icons/kmelia_declassified.gif";
subscriptionAddSrc	= m_context + "/util/icons/subscribeAdd.gif";
subscriptionSrc		= m_context + "/util/icons/subscribe.gif";
favoriteAddSrc		= m_context + "/util/icons/addFavorit.gif";
favoriteSrc			= m_context + "/util/icons/favorite.gif";
publicationAddSrc	= m_context + "/util/icons/publicationAdd.gif";
publicationSrc		= m_context + "/util/icons/publication.gif";
fullStarSrc			= m_context + "/util/icons/starFilled.gif";
emptyStarSrc		= m_context + "/util/icons/starEmpty.gif";
topicSrc			= m_context + "/util/icons/component/kmeliaSmall.gif";
topicUpdateSrc		= m_context + "/util/icons/update.gif";
topicDeleteSrc		= m_context + "/util/icons/delete.gif";
topicInfoSrc		= m_context + "/util/icons/info.gif";
topicUpSrc			= m_context + "/util/icons/arrow/arrowUp.gif";
topicDownSrc		= m_context + "/util/icons/arrow/arrowDown.gif";
topicVisibleSrc		= m_context + "/util/icons/visible.gif";
topicInvisibleSrc	= m_context + "/util/icons/masque.gif";
pubToValidateSrc	= m_context + "/util/icons/publicationstoValidate.gif";
pdcUtilizationSrc	= m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
String importFileSrc	= m_context + "/util/icons/importFile.gif";
String importFilesSrc	= m_context + "/util/icons/importFiles.gif";
String exportComponentSrc	= m_context + "/util/icons/exportComponent.gif";

if (id == null) {
	id = rootId;
}

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.kmelia.settings.kmeliaSettings", kmeliaScc.getLanguage());

//For Drag And Drop
boolean dragAndDropEnable = kmeliaScc.isDragAndDropEnable();

String sURI = request.getRequestURI();
String sRequestURL = HttpUtils.getRequestURL(request).toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
String userId = kmeliaScc.getUserId();

ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
String pathInstallerJre = generalSettings.getString("pathInstallerJre");
if (pathInstallerJre != null && !pathInstallerJre.startsWith("http"))
	pathInstallerJre = m_sAbsolute+pathInstallerJre;

//Example: http://myserver
String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);

%>


<%@page import="com.stratelia.silverpeas.util.SilverpeasSettings"%><HTML>
<HEAD>
<TITLE></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/ajax/ricoAjax.js"></script>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>

<% if (useTreeview) { %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/treeview/TreeView.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/treeview/TreeViewElements.js"></script>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/treeview.css">
<% } %>
<style>
#DropZone {
	padding-left: 5px;
	padding-right: 5px;
	/*margin-left: 5px;*/
	margin-right: 5px;
}
</style>

<script language="JavaScript1.2">

var subscriptionWindow = window;
var favoriteWindow = window;
var topicUpdateWindow = window;
var topicAddWindow = window;
var importFileWindow = window;
var importFilesWindow = window;
var exportComponentWindow = window;

function topicGoTo(id) {
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

function clipboardPaste() {
    top.IdleFrame.document.location.replace('../..<%=URLManager.getURL(URLManager.CMP_CLIPBOARD)%>paste?compR=Rkmelia&SpaceFrom=<%=kmeliaScc.getSpaceId()%>&ComponentFrom=<%=kmeliaScc.getComponentId()%>&JSPPage=<%=response.encodeURL(URLEncoder.encode("GoToTopic?Id="+id))%>&TargetFrame=MyMain&message=REFRESH');
}

function clipboardCopy() {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>copy?Object=Node&Id=<%=id%>';
}

function clipboardCut() {
    top.IdleFrame.location.href = '../..<%=kmeliaScc.getComponentUrl()%>cut?Object=Node&Id=<%=id%>';
}

<% if (!profile.equals("user")) { %>
function publicationGoToValidate(id){
    closeWindows();
    document.pubForm.Action.value = "ValidateView";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function goToValidate() {
    closeWindows();
    document.topicDetailForm.action = "ViewPublicationsToValidate";
    document.topicDetailForm.submit();
}

function topicAdd(isLinked) {
	if (!topicAddWindow.closed && topicAddWindow.name== "topicAddWindow")
        topicAddWindow.close();
    var url = "ToAddTopic?Translation=<%=translation%>";
    if (isLinked)
    	url += "&IsLink=true";
	<% if (rightsOnTopics.booleanValue()) { %>
		location.href = url;
	<% } else { %>
	    topicAddWindow = SP_openWindow(url, "topicAddWindow", "570", "350", "directories=0,menubar=0,toolbar=0, alwaysRaised");
	<% } %>
}

function topicUpdate(id) 
{	
	document.topicDetailForm.ChildId.value = id;
    if (!topicUpdateWindow.closed && topicUpdateWindow.name== "topicUpdateWindow")
        topicUpdateWindow.close();
    
	<% if (rightsOnTopics.booleanValue()) { %>
		location.href = "ToUpdateTopic?Id="+id+"&Translation=<%=translation%>";
	<% } else { %>
	    topicUpdateWindow = SP_openWindow("ToUpdateTopic?Id="+id+"&Translation=<%=translation%>", "topicUpdateWindow", "550", "350", "directories=0,menubar=0,toolbar=0,alwaysRaised");
	<% } %>
}

function topicDeleteConfirm(childId, name) {
    if(window.confirm("<%=kmeliaScc.getString("ConfirmDeleteTopic")%> '" + name + "' ?")){
          document.topicDetailForm.action = "DeleteTopic";
          document.topicDetailForm.Id.value = childId;
          document.topicDetailForm.submit();
    }
}

function flushTrashCan() {
    if(window.confirm("<%=kmeliaScc.getString("ConfirmFlushTrashBean")%>")){
          document.topicDetailForm.action = "FlushTrashCan";
          document.topicDetailForm.submit();
    }
}

function publicationAdd(){
    closeWindows();
    document.pubForm.action = "NewPublication";
    document.pubForm.submit();
}

function topicWysiwyg(topicId) {
	closeWindows();
	document.topicDetailForm.action = "ToTopicWysiwyg";
	document.topicDetailForm.ChildId.value = topicId;
	document.topicDetailForm.submit();
}

function topicDown(topicId) {
	closeWindows();
	document.topicDetailForm.action = "TopicDown";
	document.topicDetailForm.ChildId.value = topicId;
	document.topicDetailForm.submit();
}

function topicUp(topicId) {
	closeWindows();
	document.topicDetailForm.action = "TopicUp";
	document.topicDetailForm.ChildId.value = topicId;
	document.topicDetailForm.submit();
}

function changeTopicStatus(topicId, status) {
	closeWindows();
	document.topicDetailForm.action = "ChangeTopicStatus";
	document.topicDetailForm.ChildId.value = topicId;
	document.topicDetailForm.Status.value = status;
	document.topicDetailForm.Recursive.value = "0";

	visible2InvisibleRecursive = '<%=kmeliaScc.getString("TopicVisible2InvisibleRecursive")%>';
	invisible2VisibleRecursive = '<%=kmeliaScc.getString("TopicInvisible2VisibleRecursive")%>';

	if (status == 'Invisible')
	{
		question = visible2InvisibleRecursive;
	}
	else
	{
		question = invisible2VisibleRecursive;
	}

	if(window.confirm(question)){
		document.topicDetailForm.Recursive.value = "1";
	}
	document.topicDetailForm.submit();
}

function showDnD()
{
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize 		= uploadSettings.getString("MaximumFileSize", "10000000");
	String maxFileSizeForApplet = maximumFileSize.substring(0, maximumFileSize.length()-3);
	if (profile.equals("publisher") || profile.equals("writer")) { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&TopicId=<%=id%>&IgnoreFolders=1','<%=httpServerBase%>/weblib/dragAnddrop/ModeNormal_<%=language%>.html','<%=httpServerBase%>/weblib/dragAnddrop/raduploadMulti.properties','<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&TopicId=<%=id%>&IgnoreFolders=1&Draft=1','<%=httpServerBase%>/weblib/dragAnddrop/ModeDraft_<%=language%>.html','<%=maxFileSizeForApplet%>','<%=pathInstallerJre%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } else { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&TopicId=<%=id%>','<%=httpServerBase%>/weblib/dragAnddrop/ModeNormal_<%=language%>.html','<%=httpServerBase%>/weblib/dragAnddrop/raduploadMulti.properties','<%=httpServerBase+m_context%>/RImportDragAndDrop/jsp/Drop?UserId=<%=userId%>&ComponentId=<%=componentId%>&TopicId=<%=id%>&Draft=1','<%=httpServerBase%>/weblib/dragAnddrop/ModeDraft_<%=language%>.html','<%=maxFileSizeForApplet%>','<%=pathInstallerJre%>','<%=resources.getString("GML.DragNDropExpand")%>','<%=resources.getString("GML.DragNDropCollapse")%>');
	<% } %>
}

<% } %>

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

function goToSubscriptions() {
    url = "subscriptionsManager.jsp?Action=View";
    windowName = "subscriptionWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
	larg = "550";
	haut = "350";
    if (!subscriptionWindow.closed && subscriptionWindow.name == "subscriptionWindow")
        subscriptionWindow.close();
    subscriptionWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function addSubscription(id) {
    url = "subscriptionsManager.jsp?Action=AddSubscription&Id="+id;
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

function importFile(topicId)
{
    url = "importOneFile.jsp?Action=ImportFileForm&TopicId="+topicId;
    windowName = "importFileWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
		larg = "610";
		haut = "370";
		if (!importFileWindow.closed && importFileWindow.name=="importFileWindow")
			importFileWindow.close();
    importFileWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function importFiles(topicId)
{
    url = "importMultiFiles.jsp?Action=ImportFilesForm&TopicId="+topicId;
    windowName = "importFilesWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
		larg = "610";
		haut = "460";
		if (!importFilesWindow.closed && importFilesWindow.name=="importFilesWindow")
			importFilesWindow.close();
    importFilesWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function openExportPDFPopup(topicId) {
	
	chemin = "ExportAttachementsToPDF?TopicId=" + topicId;
	largeur = "700";
	hauteur = "500";
	SP_openWindow(chemin, "ExportWindow", largeur, hauteur, "scrollbars=yes, resizable=yes");
	
}
	
function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function exportComponent()
{
	exportComponentWindow = SP_openWindow("exportTopic.jsp","exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

function exportTopic(topicId)
{
	exportComponentWindow = SP_openWindow("exportTopic.jsp?TopicId="+topicId,"exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

function closeWindows() {
	if(!subscriptionWindow.closed && subscriptionWindow.name=="subscriptionWindow")
		subscriptionWindow.close();

	if (!favoriteWindow.closed && favoriteWindow.name=="favoriteWindow")
		favoriteWindow.close();

	if (!topicAddWindow.closed && topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();

	if (!topicUpdateWindow.closed && topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
}

function uploadCompleted(s)
{
	location.href="<%=m_context%><%=kmeliaScc.getComponentUrl()%>GoToTopic?Id=<%=id%>";
	return true;
}

function doPagination(index)
{
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>","Index="+index);
	return;
}

function sortGoTo(selectedIndex) {
    closeWindows();
	if (selectedIndex != 0 && selectedIndex != 1) {
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>","Index=0","Sort="+sort);
		return;
	}
}

function init()
{
	ajaxEngine.registerRequest('refreshPubList', '<%=m_context%>/RAjaxPublicationsListServlet/dummy');
	
	ajaxEngine.registerAjaxElement('pubList');
	
	ajaxEngine.sendRequest('refreshPubList','ElementId=pubList',"ComponentId=<%=componentId%>","PubIdToHighLight=<%=pubIdToHighlight%>");

	<% if (SilverpeasSettings.readBoolean(settings, "DisplayDnDOnLoad", false)) { %>
		showDnD();
	<% } %>
}
</script>
<script src="<%=m_context%>/kmelia/jsp/javaScript/dragAndDrop.js" type="text/javascript"></script>
</HEAD>
<BODY onUnload="closeWindows()" onLoad="init()">
<%
        namePath = "";
        if (!id.equals("0"))
        {
		      name = nodeDetail.getName(translation);
        	description = nodeDetail.getDescription(translation);
        }
        urlTopic = nodeDetail.getLink();
          
        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(kmeliaScc.getSpaceLabel());
        browseBar.setComponentName(kmeliaScc.getComponentLabel(), "Main");
        browseBar.setPath(linkedPathString);
        browseBar.setI18N("GoToTopic?Id="+id, translation);
        
        // création du nom pour les favoris
        namePath = spaceLabel + " > " + componentLabel;
         if (!pathString.equals(""))
        	namePath = namePath + " > " + pathString;

        //Display operations by profile
        OperationPane operationPane = window.getOperationPane();
        if (profile.equals("admin")) {
        	if (id.equals("1")) {
            	operationPane.addOperation(emptyBasketSrc, kmeliaScc.getString("EmptyBasket"), "javascript:onClick=flushTrashCan()");
            } else {
				if (id.equals("0") && kmeliaScc.isPdcUsed()) {
					operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+kmeliaScc.getComponentId()+"','utilizationPdc1')");
				}
				if (id.equals("0") && kmeliaScc.isContentEnabled()) {
					operationPane.addOperation(resources.getIcon("kmelia.modelUsed"), resources.getString("kmelia.ModelUsed"), "ModelUsed");
					operationPane.addLine();
				}
				if (id.equals("0") && kmeliaScc.isOrientedWebContent()) {
					operationPane.addOperation(topicAccueilInfoSrc, kmeliaScc.getString("TopicWysiwyg"), "javascript:onClick=topicWysiwyg('"+id+"')");
					operationPane.addLine();
				}
				if (kmeliaScc.isExportComponentAllowed() && (kmeliaScc.isExportZipAllowed() || kmeliaScc.isExportPdfAllowed()))
				{
					if (id.equals("0") && kmeliaScc.isExportZipAllowed())
					{
						// on est à l'accueil, exportation du composant entier
						operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmelia.ExportComponent"), "javascript:onClick=exportComponent()");
					}
					else {
						if (kmeliaScc.isExportZipAllowed())
							operationPane.addOperation(exportComponentSrc, kmeliaScc.getString("kmelia.ExportTopic"), "javascript:onClick=exportTopic("+id+")");
					}
					
					if (kmeliaScc.isExportPdfAllowed())
						operationPane.addOperation(importFileSrc, kmeliaScc.getString("kmelia.ExportPDF"), "javascript:openExportPDFPopup("+id+")");
					
					operationPane.addLine();
				}
				
				if (kmeliaScc.isTreeStructure()) {
					operationPane.addOperation(topicAddSrc, kmeliaScc.getString("CreerSousTheme"), "javascript:onClick=topicAdd(false)");
					if (kmeliaScc.isFoldersLinkedEnabled())
		            {
		            	operationPane.addOperation(resources.getIcon("kmelia.folderLinkedAdd"), resources.getString("kmelia.FolderAddLink"), "javascript:onClick=topicAdd(true)");
		            }
					operationPane.addLine();
				}
		        if (!id.equals("0") || (id.equals("0") && (kmeliaScc.getNbPublicationsOnRoot() == 0 || !kmeliaScc.isTreeStructure()) )) {
		        	operationPane.addOperation(publicationAddSrc, kmeliaScc.getString("PubCreer"), "javascript:onClick=publicationAdd()");
		        	if (kmeliaScc.isWizardEnabled())
		        	{
		        		// ajout assistant de publication
		        		operationPane.addOperation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
		        	}
		        	if (kmeliaScc.isImportFileAllowed())
						operationPane.addOperation(importFileSrc, kmeliaScc.getString("kmelia.ImportFile"), "javascript:onClick=importFile("+id+")");
		            if (kmeliaScc.isImportFilesAllowed())
						operationPane.addOperation(importFilesSrc, kmeliaScc.getString("kmelia.ImportFiles"), "javascript:onClick=importFiles("+id+")");
		            if (currentTopic.getPublicationDetails().size() > 1)
		            	operationPane.addOperation(resources.getIcon("kmelia.sortPublications"), kmeliaScc.getString("kmelia.OrderPublications"), "ToOrderPublications");
		            operationPane.addLine();
		 		}
		        if (!id.equals("1"))
		        {
		        	if (!id.equals("0"))
		        	{
		        		operationPane.addOperation(resources.getIcon("kmelia.copy"), resources.getString("GML.copy"), "javascript:onClick=clipboardCopy()");
	            		operationPane.addOperation(resources.getIcon("kmelia.cut"), resources.getString("GML.cut"), "javascript:onClick=clipboardCut()");
		        	}
	            	operationPane.addOperation(resources.getIcon("kmelia.paste"), resources.getString("GML.paste"), "javascript:onClick=clipboardPaste()");
	            	operationPane.addLine();
		        }
		        
		        operationPane.addOperation(subscriptionAddSrc, resources.getString("SubscriptionsAdd"), "javascript:onClick=addSubscription('"+id+"')");
		        operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(namePath))+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(description))+"','"+urlTopic+"')");
		        if (!toolboxMode && id.equals("0")) {
		        	operationPane.addLine();
		            operationPane.addOperation(topicBasketSrc, resources.getString("PubBasket"), "javascript:onClick=topicGoTo('1')");
		            operationPane.addOperation(pubToValidateSrc, resources.getString("ToValidate"), "javascript:onClick=goToValidate()");
		    	}
      		}
      	}
      	else if (profile.equals("publisher") || profile.equals("writer")) 
      	{
			if (id.equals("1")) {
				operationPane.addOperation(emptyBasketSrc, resources.getString("EmptyBasket"), "javascript:onClick=flushTrashCan()");
			} else 
			{
				if (kmeliaScc.isTreeStructure() && kmeliaScc.isTopicManagementDelegated())
				{
					operationPane.addOperation(topicAddSrc, resources.getString("CreerSousTheme"), "javascript:onClick=topicAdd()");
					operationPane.addLine();
				}
				if (!id.equals("0") || (id.equals("0") && (kmeliaScc.getNbPublicationsOnRoot() == 0 || !kmeliaScc.isTreeStructure())))
				{
			    	operationPane.addOperation(publicationAddSrc, resources.getString("PubCreer"), "javascript:onClick=publicationAdd()");
			    	if (kmeliaScc.isWizardEnabled())
			    	{
			    		//ajout assistant de publication
			    		operationPane.addOperation(resources.getIcon("kmelia.wizard"), resources.getString("kmelia.Wizard"), "WizardStart");
			    	}
			    	if (kmeliaScc.isImportFileAllowed())
				    	operationPane.addOperation(importFileSrc, resources.getString("kmelia.ImportFile"), "javascript:onClick=importFile("+id+")");
					if (kmeliaScc.isImportFilesAllowed())
						operationPane.addOperation(importFilesSrc, resources.getString("kmelia.ImportFiles"), "javascript:onClick=importFiles("+id+")");
					operationPane.addLine();
				    operationPane.addOperation(resources.getIcon("kmelia.paste"), resources.getString("GML.paste"), "javascript:onClick=clipboardPaste()");
			    }
				if (kmeliaScc.isExportComponentAllowed() && kmeliaScc.isExportPdfAllowed())
				{
					if (!id.equals("0"))
						operationPane.addOperation(importFileSrc, kmeliaScc.getString("kmelia.ExportPDF"), "javascript:openExportPDFPopup("+id+")");
					operationPane.addLine();
				}
				operationPane.addOperation(subscriptionAddSrc, kmeliaScc.getString("SubscriptionsAdd"), "javascript:onClick=addSubscription('"+id+"')");
				operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(namePath))+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(description))+"','"+urlTopic+"')");
				if (!toolboxMode && id.equals("0")) {
					if (!kmeliaScc.isSuppressionOnlyForAdmin())
					{
						operationPane.addLine();
						operationPane.addOperation(topicBasketSrc, resources.getString("PubBasket"), "javascript:onClick=topicGoTo('1')");
					}
				    if (profile.equals("publisher")) {
				    	operationPane.addLine();
				        operationPane.addOperation(pubToValidateSrc, resources.getString("ToValidate"), "javascript:onClick=goToValidate()");
				    }
				}
			}
      }
      else if (!isGuest)
      {
          operationPane.addOperation(subscriptionAddSrc, resources.getString("SubscriptionsAdd")+" '"+Encode.javaStringToHtmlString(name)+"'", "javascript:onClick=addSubscription('"+id+"')");
          operationPane.addLine();
          operationPane.addOperation(favoriteAddSrc, resources.getString("FavoritesAdd1")+" "+kmeliaScc.getString("FavoritesAdd2"), "javaScript:addFavorite('"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(namePath))+"','"+Encode.javaStringToHtmlString(Encode.javaStringToJsString(description))+"','"+urlTopic+"')");
      }
																				
    //Instanciation du cadre avec le view generator
	Frame frame = gef.getFrame();

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
	<% if (useTreeview) { %>
		<table width="98%" border="0"><tr><td valign="top"><%@ include file="treeview.jsp.inc" %></td><td valign="top" width="100%">
	<% } %>
<%
	boolean subTopicsVisible = currentTopic.getNodeDetail().getChildrenNumber() > 0;
	if (!id.equals("1") && !id.equals("2") && kmeliaScc.isTreeStructure()) {
        if (profile.equals("admin") || (kmeliaScc.isTopicManagementDelegated() && (profile.equals("publisher") || profile.equals("writer"))))
          displaySessionTopicsToAdmin(kmeliaScc, topicSrc, topicUpdateSrc, topicDeleteSrc, topicInfoSrc, topicUpSrc, topicDownSrc, topicVisibleSrc, topicInvisibleSrc, gef, profile, request, session, resources, out);
        else
        {
        	if (!useTreeview)
        		displaySessionTopicsToUsers(kmeliaScc, currentTopic, gef, request, session, resources, out);
        	else
        		subTopicsVisible = false;
        }
    }
				if (!profile.equals("user") && dragAndDropEnable && !id.equals("1") && ((id.equals("0") && kmeliaScc.getNbPublicationsOnRoot() == 0) || Integer.parseInt(id) > 2))
				{ 
					if (subTopicsVisible)
			  			out.println("<br/>");
					%>
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
					<% //End of if
				}
				
			//Get only publications from the Home topic but not in the basket
			if (id.equals("0") && kmeliaScc.getNbPublicationsOnRoot() != 0 && kmeliaScc.isTreeStructure())
			{
				ArrayList publicationsToDisplay = new ArrayList();
				Iterator iterator =	currentTopic.getPublicationDetails().iterator();
			    UserPublication userPub;
			    while (iterator.hasNext())
			    {
	                userPub = (UserPublication) iterator.next();
	                if (!kmeliaScc.isPublicationDeleted(userPub.getPublication().getPK().getId()))
	                	publicationsToDisplay.add(userPub);
			    } 
			    displayLastPublications(publicationsToDisplay, (currentTopic.getNodeDetail().getChildrenNumber() > 0), pathString, kmeliaScc.getString("PublicationsLast"), kmeliaScc, settings, resources, out);
			} else {
				 Board board = gef.getBoard();
				 out.println("<div id=\"pubList\">");
				 out.println("<br/>");
				 out.println(board.printBefore());
				 out.println("<br/><center>"+resources.getString("kmelia.inProgressPublications")+"<br/><br/><img src=\""+resources.getIcon("kmelia.progress")+"\"/></center><br/>");
				 out.println(board.printAfter());
			     out.println("</div>");
			}
%>
	<% if (useTreeview) { %>
		</td></tr></table>
	<% } %>

	<%
		out.println(frame.printAfter());
		out.println(window.printAfter());
	%>

<FORM NAME="topicDetailForm" METHOD="POST">
	<input type="hidden" name="Id" value="<%=id%>">
	<input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(pathString)%>">
	<input type="hidden" name="ChildId">
	<input type="hidden" name="Status"><input type="hidden" name="Recursive">
</FORM>

<FORM NAME="pubForm" action="ViewPublication" METHOD="POST">
	<input type="hidden" name="Action">
	<input type="hidden" name="PubId">
	<input type="hidden" name="Path">
	<input type="hidden" name="CheckPath">
</FORM>

<FORM NAME="fupload" ACTION="fileUpload.jsp" METHOD="POST"  enctype="multipart/form-data">
	<input type="hidden" name="Action" value="initial">
</FORM>

<form name="frm_report" action="GoToTopic">
  	<input type="hidden" name="Id" value="<%=id%>">
</form>

</BODY>
</HTML>