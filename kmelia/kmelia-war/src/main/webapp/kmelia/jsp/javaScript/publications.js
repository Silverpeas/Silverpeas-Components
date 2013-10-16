function exportPublications() {
	var selectedIds = getSelectedPublicationIds();
	var notSelectedIds = getNotSelectedPublicationIds();
	var uri = "ExportPublications?SelectedIds="+selectedIds+"&NotSelectedIds="+notSelectedIds;
	SP_openWindow(uri, "Export", '600', '300','scrollbars=yes, resizable, alwaysRaised');
	$("input:checked[name=C1]").removeAttr('checked').hide();
}

function showPublicationOperations(item) {
	//$(item).find(".unit-operation").show();
	$(item).find(".selection input").show();
	$(item).toggleClass("over-publication", true);
}

function hidePublicationOperations(item) {
	//$(item).find(".unit-operation").hide();
	var input = $(item).find(".selection input");
	if ($(input).is(':checked')) {
		// do not hide checkbox
	} else {
		input.hide();	
	}
	$(item).toggleClass("over-publication", false);
}

function showPublicationCheckedBoxes() {
	try {
		$("input:checked[name=C1]").show();
	} catch (e) {
		
	}
}

function getSelectedPublicationIds() {
	var selectedIds = "";
	 $("input:checked[name=C1]").each(function() {
		 var id = $(this).val();
		 selectedIds += id;
		 selectedIds += ",";
	});
	if (selectedIds.length > 0) {
		selectedIds = selectedIds.substring(0, selectedIds.length-1);
	}
	return selectedIds;
}

function getNotSelectedPublicationIds() {
	var notSelectedIds = "";
	 $("input:not(:checked)[name=C1]").each(function() {
		 var id = $(this).val();
		 notSelectedIds += id;
		 notSelectedIds += ",";
	});
	if (notSelectedIds.length > 0) {
		notSelectedIds = notSelectedIds.substring(0, notSelectedIds.length-1);
	}
	return notSelectedIds;
}

function sendPubId() {
	//do nothing
}

function startsWith(haystack, needle){
	return haystack.substr(0,needle.length)==needle?true:false;
}

function deletePublications() {
	var confirm = getString('kmelia.publications.trash.confirm');
	if (getCurrentNodeId() == "1") {
		confirm = getString('kmelia.publications.delete.confirm');
	}
	if (window.confirm(confirm)) {
		var componentId = getComponentId();
		var selectedPublicationIds = getSelectedPublicationIds();
		var notSelectedPublicationIds = getNotSelectedPublicationIds();
		var url = getWebContext()+'/KmeliaAJAXServlet';
		$.get(url, { SelectedIds:selectedPublicationIds,NotSelectedIds:notSelectedPublicationIds,ComponentId:componentId,Action:'DeletePublications'},
				function(data){
					if (startsWith(data, "ok")) {
						// fires event
						try {
							var nb = data.substring(3);
							displayPublications(getCurrentNodeId());
							if (getCurrentNodeId() == "1") {
								notySuccess(nb + ' ' + getString('kmelia.publications.delete.info'));
							} else {
								notySuccess(nb + ' ' + getString('kmelia.publications.trash.info'));
							}
							publicationsRemovedSuccessfully(nb);
						} catch (e) {
							writeInConsole(e);
						}
					} else {
						publicationsRemovedInError(data);
					}
				}, 'text');
	}
}

function publicationsRemovedInError(data) {
  	notyError(data);
}

function copyPublications() {
	var componentId = getComponentId();
	var selectedPublicationIds = getSelectedPublicationIds();
	var notSelectedPublicationIds = getNotSelectedPublicationIds();
	var url = getWebContext()+'/KmeliaAJAXServlet';
	$.get(url, { SelectedIds:selectedPublicationIds,NotSelectedIds:notSelectedPublicationIds,ComponentId:componentId,Action:'CopyPublications'},
			function(data){
				if (data == "ok") {
					// fires event
					// do nothing
				} else {
					notyError(data);
				}
			}, 'text');
}

function cutPublications() {
	var componentId = getComponentId();
	var selectedPublicationIds = getSelectedPublicationIds();
	var notSelectedPublicationIds = getNotSelectedPublicationIds();
	var url = getWebContext()+'/KmeliaAJAXServlet';
	$.get(url, { SelectedIds:selectedPublicationIds,NotSelectedIds:notSelectedPublicationIds,ComponentId:componentId,Action:'CutPublications'},
			function(data){
				if (data == "ok") {
					// fires event
					// do nothing
				} else {
					notyError(data);
				}
			}, 'text');
}