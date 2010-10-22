var subscriptionWindow = window;
var favoriteWindow = window;
var importFileWindow = window;
var importFilesWindow = window;
var exportComponentWindow = window;
var topicWindow = window;

function addFavorite(name,description,url)
{
	urlWindow = getWebContext()+"/RmyLinksPeas/jsp/CreateLinkFromComponent?Name="+name+"&Description="+description+"&Url="+url+"&Visible=true";
    windowName = "favoriteWindow";
	larg = "550";
	haut = "250";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!favoriteWindow.closed && favoriteWindow.name== "favoriteWindow")
        favoriteWindow.close();
    favoriteWindow = SP_openWindow(urlWindow, windowName, larg, haut, windowParams);
}

function addSubscription() {
    url = "subscriptionsManager.jsp?Action=AddSubscription&Id="+getCurrentNodeId();
    windowName = "subscriptionWindow";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
	larg = "550";
	haut = "350";
    if (!subscriptionWindow.closed && subscriptionWindow.name == "subscriptionWindow")
        subscriptionWindow.close();
	subscriptionWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
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

function exportPublications() {
	exportComponentWindow = SP_openWindow("exportTopic.jsp?TopicId="+getCurrentNodeId(),"exportComponentWindow",700,250,"scrollbars=yes, resizable=yes");
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function displayTopicDescription(id)
{
	//display rich description of topic
	var ieFix = new Date().getTime();
	var componentId = getComponentId();
	$.get(getWebContext()+'/KmeliaAJAXServlet', {Id:id,Action:'GetTopicWysiwyg',ComponentId:componentId,IEFix:ieFix},
			function(data){
				$("#topicDescription").html(data);
			},"html");
}

function refreshPublications()
{
	var nodeId = getCurrentNodeId();
	var ieFix = new Date().getTime();
	var componentId = getComponentId();
	$.get(getWebContext()+'/RAjaxPublicationsListServlet', {Id:nodeId,ComponentId:componentId,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function displayPublicationsToValidate()
{
	//display publications to validate
	var ieFix = new Date().getTime();
	var componentId = getComponentId();
	$.get(getWebContext()+'/RAjaxPublicationsListServlet', {ComponentId:componentId,ToValidate:1,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function closeWindows() {
	
	if(!subscriptionWindow.closed && subscriptionWindow.name=="subscriptionWindow") {
		subscriptionWindow.close();
	}

	if (!favoriteWindow.closed && favoriteWindow.name=="favoriteWindow") {
		favoriteWindow.close();
	}
	
	if (!topicWindow.closed && topicWindow.name=="topicWindow") {
		topicWindow.close();
	}
}

function publicationGoTo(id){
    closeWindows();
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function sortGoTo(selectedIndex) {
    closeWindows();
	if (selectedIndex != 0 && selectedIndex != 1) {
		var topicQuery = getSearchQuery();
		var sort = document.publicationsForm.sortBy[selectedIndex].value;
		var ieFix = new Date().getTime();
		var componentId = getComponentId();
		$.get(getWebContext()+'/RAjaxPublicationsListServlet', {Index:0,Sort:sort,ComponentId:componentId,Query:topicQuery,IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
		return;
	}
}