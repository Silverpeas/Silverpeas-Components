function searchInTopic() {
  var topicQuery = getSearchQuery();
  if (topicQuery != "" && topicQuery.length > 1) {
		$.progressMessage();
		var ieFix = new Date().getTime();
		var componentId = getComponentId();
		$.get(getWebContext()+'/RAjaxPublicationsListServlet', {Index:0,Query:topicQuery,ComponentId:componentId,IEFix:ieFix},
				function(data){
					$.closeProgressMessage();
					$('#pubList').html(data);
				},"html");
	}
	return;
}

function checkSubmitToSearch(ev) {
	var touche = ev.keyCode;
	if (touche == 13) {
		searchInTopic();
	}
}

function getSearchQuery() {
	var topicQuery = "";
	if (document.getElementById("topicQuery") != null) {
		topicQuery = $("#topicQuery").val();
	}
	return topicQuery;
}

function clearSearchQuery() {
  try {
    if (document.getElementById("topicQuery") != null) {
      $("#topicQuery").val("");
    }
  } catch (e) {
  }
}