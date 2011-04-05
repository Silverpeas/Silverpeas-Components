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

function displayPath(id) {
    var sUrl = getWebContext()+"/KmeliaJSONServlet?Action=GetPath&ComponentId="+getComponentId()+"&Language="+getLanguage()+"&Id="+id+"&IEFix="+new Date().getTime();
    $.getJSON(sUrl, function(data){
    	//remove topic breadcrumb
        removeBreadCrumbElements();
    	$(data.reverse()).each(function(i, topic) {
			if (topic.id != 0) {
            	addBreadCrumbElement("javascript:topicGoTo("+topic.id+")", topic.name);
            }
		});
	});
}

function displayPublications(id) {
	//display publications of topic
	var pubIdToHighlight = getPubIdToHighlight();
	var ieFix = new Date().getTime();
	var componentId = getComponentId();
	var url = getWebContext()+"/RAjaxPublicationsListServlet";
	$.get(url, {Id:id,ComponentId:componentId,PubIdToHighlight:pubIdToHighlight,IEFix:ieFix},
			function(data){
				$('#pubList').html(data);
			},"html");
}

function displayOperations(id) {
	var ieFix = new Date().getTime();
	var componentId = getComponentId();
	var url = getWebContext()+"/KmeliaJSONServlet";
	$.get(url, { Id:id,Action:'GetOperations',ComponentId:componentId,IEFix:ieFix},
			function(operations){
				//display dNd according rights
				checkDnD(id, operations);
				initOperations(id, operations);
				try {
					if (operations.addTopic) {
						showRightClickHelp();
					}
				} catch (e) {
					// right click could not be supported by calling page
				}
			}, 'json');
}

function initOperations(id, op) {
	$("#menutoggle").css({'display':'block'});
	
	oMenu.clearContent();
	
	var menuItem;
	var groupIndex = 0;
	var groupEmpty = true;
	
	if (op.emptyTrash) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.emptyTrash"], {url: "javascript:onClick=emptyTrash()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (op.pdc) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.pdc"], {url: "javascript:onClick=openSPWindow('"+getWebContext()+"/RpdcUtilization/jsp/Main?ComponentId="+getComponentId()+"','utilizationPdc1')"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (op.templates) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.templates"], {url: "ModelUsed"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (op.exporting) {
		if (id == "0") {
			menuItem = new YAHOO.widget.MenuItem(labels["operation.exportComponent"], {url: "javascript:onClick=exportPublications()"});
		} else {
			menuItem = new YAHOO.widget.MenuItem(labels["operation.exportTopic"], {url: "javascript:onClick=exportPublications()"});
		}
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (op.exportPDF) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.exportPDF"], {url: "javascript:openExportPDFPopup()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (!groupEmpty) {
		groupIndex++;
		groupEmpty = true;
	}
	
	if (op.addTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.addTopic"], {url: "javascript:onclick=addNodeToCurrentNode()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.updateTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.updateTopic"], {url: "javascript:onclick=updateCurrentNode()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.deleteTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.deleteTopic"], {url: "javascript:onclick=deleteCurrentNode()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.sortSubTopics) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.sortTopics"], {url: "javascript:onclick=sortSubTopics()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.copyTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.copy"], {url: "javascript:onclick=copyCurrentNode()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.cutTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.cut"], {url: "javascript:onclick=cutCurrentNode()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.hideTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.visible2invisible"], {url: "javascript:onclick=changeCurrentTopicStatus()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.showTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.invisible2visible"], {url: "javascript:onclick=changeCurrentTopicStatus()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.wysiwygTopic) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.wysiwygTopic"], {url: "javascript:onclick=updateCurrentTopicWysiwyg()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (!groupEmpty) {
		groupIndex++;
		groupEmpty = true;
	}
	
	if (op.addPubli) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.addPubli"], {url: "NewPublication"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.wizard) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.wizard"], {url: "WizardStart"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.importFile) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.importFile"], {url: "javascript:onclick=importFile()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.importFiles) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.importFiles"], {url: "javascript:onclick=importFiles()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.sortPublications) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.sortPublis"], {url: "ToOrderPublications"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.updateChain) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.updateChain"], {url: "javascript:onclick=updateChain()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	if (op.paste) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.paste"], {url: "javascript:onclick=pasteFromOperations()"});
		oMenu.addItem(menuItem, groupIndex);
		groupEmpty = false;
	}
	
	if (!groupEmpty) {
		groupIndex++;
		groupEmpty = true;
	}
	
	if (op.subscriptions) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.subscribe"], {url: "javascript:onclick=addSubscription()"});
		oMenu.addItem(menuItem, groupIndex);
	}
	if (op.favorites) {
		menuItem = new YAHOO.widget.MenuItem(labels["operation.favorites"], {url: "javascript:onclick=addCurrentNodeAsFavorite()"});
		oMenu.addItem(menuItem, groupIndex);
	}
			
	oMenu.render();
}

var currentNodeId;
var currentTopicDescription;
var currentTopicName;

function getCurrentNodeId() {
	return currentNodeId;
}

function setCurrentNodeId(id) {
	//alert("setCurrentNodeId : id = "+id);
	currentNodeId = id;
}

function setCurrentTopicName(name) {
	currentTopicName = name;
}
function setCurrentTopicDescription(desc) {
	currentTopicDescription = desc;
}

function displayTopicInformation(id) {
	if (id != "0" && id != "1" && id != "tovalidate") {
		$("#footer").css({'visibility':'visible'});
		var url = getWebContext()+"/KmeliaJSONServlet?Id="+id+"&Action=GetTopic&ComponentId="+getComponentId()+"&Language="+getLanguage()+"&IEFix="+new Date().getTime();
		$.getJSON(url, function(topic){
					$("#footer").html(labels["topic.info"]+topic[0].creatorName+' - '+topic[0].date+' - <a id="topicPermalink" href="#"><img src="'+icons["permalink"]+'"/></a>');
					$("#footer #topicPermalink").attr("href", getWebContext()+"/Topic/"+id+"?ComponentId="+getComponentId());
					setCurrentTopicName(topic[0].name);
					setCurrentTopicDescription(topic[0].description);
				});
	} else {
		$("#footer").css({'visibility':'hidden'});
	}
}

function deleteNode(nodeId, nodeLabel) {
	if(window.confirm(labels["ConfirmDeleteTopic"]+ " '" + nodeLabel + "' ?")) {
		var componentId = getComponentId();
		var url = getWebContext()+'/KmeliaAJAXServlet';
		$.get(url, { Id:nodeId,ComponentId:componentId,Action:'Delete'},
				function(data){
					if ((data - 0) == data && data.length > 0) {
						// go to parent node
						displayTopicContent(data);
					} else {
						alert(data);
					}
				}, 'text');
	}
}

function deleteCurrentNode() {
	deleteNode(getCurrentNodeId(), currentTopicName);
}

function sortSubTopics() {
	closeWindows();
	SP_openWindow("ToOrderTopics?Id="+getCurrentNodeId(), "topicAddWindow", "600", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised,resizable");
}

function addNodeToCurrentNode() {
	topicAdd(getCurrentNodeId(), false);
}

function topicAdd(topicId, isLinked) {
	//alert("topicAdd : topicId = "+topicId);
	var translation = getTranslation();
	var rightsOnTopic = params["rightsOnTopic"];
	if (!topicWindow.closed && topicWindow.name== "topicAddWindow") {
		topicWindow.close();
	}
    var url = "ToAddTopic?Id="+topicId+"&Translation="+translation;
    if (isLinked) {
    	url += "&IsLink=true";
    }
	if (rightsOnTopic) {
		location.href = url;
	} else {
		topicWindow = SP_openWindow(url, "topicWindow", "570", "350", "directories=0,menubar=0,toolbar=0, alwaysRaised");
	}
}

function updateCurrentNode() {
	topicUpdate(getCurrentNodeId());
}

function topicUpdate(id) {
	document.topicDetailForm.ChildId.value = id;
    if (!topicWindow.closed && topicWindow.name== "topicUpdateWindow") {
    	topicWindow.close();
    }

    var translation = getTranslation();
	var rightsOnTopic = params["rightsOnTopic"];
	if (rightsOnTopic) {
		location.href = "ToUpdateTopic?Id="+id+"&Translation="+translation;
	} else {
		topicWindow = SP_openWindow("ToUpdateTopic?Id="+id+"&Translation="+translation, "topicWindow", "550", "350", "directories=0,menubar=0,toolbar=0,alwaysRaised");
	}
}

function emptyTrash() {
	if(window.confirm(labels["ConfirmFlushTrashBean"]))	{
		$.progressMessage();
		var componentId = getComponentId();
		var url = getWebContext()+'/KmeliaAJAXServlet';
		$.get(url, {ComponentId:componentId,Action:'EmptyTrash'},
				function(data){
					$.closeProgressMessage();
					if (data == "ok") {
						displayTopicContent("1");
					} else {
						alert(data);
					}
				}, 'text');
	}
}

function checkDnD(id, operations) {
	//alert("checkDnD : "+displayIt);
	if (operations.addPubli == true) {
		$("#DnD").css({'display':'block'});
	} else {
		$("#DnD").css({'display':'none'});
	}
}

function addCurrentNodeAsFavorite() {
	var path = $("#breadCrumb").text();
	var description = "";
	var url = getComponentPermalink();
	if (getCurrentNodeId() != "0") {
		url = $("#topicPermalink").attr("href");
		description = currentTopicDescription;
	}
	addFavorite(encodeURI(path), encodeURI(description), url);
}

function updateCurrentTopicWysiwyg() {
	updateTopicWysiwyg(getCurrentNodeId());
}

function updateTopicWysiwyg(id) {
	closeWindows();
	document.topicDetailForm.action = "ToTopicWysiwyg";
	document.topicDetailForm.ChildId.value = id;
	document.topicDetailForm.submit();
}

function pasteFromOperations() {
	pasteNode(getCurrentNodeId());
}

function pasteNode(id) {
	$.progressMessage();

	//alert("pasteNode : id = "+id);
	var url = getWebContext()+"/KmeliaJSONServlet?Action=Paste&ComponentId="+getComponentId()+"&Language="+getLanguage()+"&Id="+id+"&IEFix="+new Date().getTime();
	
	$.getJSON(url, function(nodes){
		reloadPage(id);
		$.closeProgressMessage();
	});
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

function updateChain() {
    document.updateChain.submit();
}

function publicationGoToFromMain(id){
    closeWindows();
    document.pubForm.CheckPath.value = "1";
    document.pubForm.PubId.value = id;
    document.pubForm.submit();
}

function fileUpload() {
    document.fupload.submit();
}

function doPagination(index) {
	var paramToValidate = "0";
	if (getCurrentNodeId() == "tovalidate") {
		paramToValidate = "1";
	}
	var topicQuery = getSearchQuery();
	var ieFix = new Date().getTime();
	var componentId = getComponentId();
	$.get('<%=m_context%>/RAjaxPublicationsListServlet', {Index:index,ComponentId:componentId,ToValidate:paramToValidate,Query:topicQuery,IEFix:ieFix},
							function(data){
								$('#pubList').html(data);
							},"html");
}