function exportPublications() {
	var selectedIds = getSelectedPublicationIds();
	var notSelectedIds = getNotSelectedPublicationIds();
	var uri = "ExportPublications?SelectedIds="+selectedIds+"&NotSelectedIds="+notSelectedIds;
	SP_openWindow(uri, "Export", '600', '300','scrollbars=yes, resizable, alwaysRaised');
	$("input:checked[name=C1]").removeAttr('checked');
}

function showPublicationOperations(item) {
	//$(item).find(".unit-operation").show();
	$(item).find(".selection input").show();
	$(item).toggleClass("over-publication", true);
}

function hidePublicationOperations(item) {
	//$(item).find(".unit-operation").hide();
	$(item).find(".selection input").hide();
	$(item).toggleClass("over-publication", false);
}

function getSelectedPublicationIds() {
	var selectedIds = "";
	 $("input:checked[name=C1]").each(function() {
		 var id = $(this).val();
		 selectedIds += id.split("/", 1)[0];
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
		 notSelectedIds += id.split("/", 1)[0];
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
