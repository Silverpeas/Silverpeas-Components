function searchInTopic() {

  let extraFormSearch = document.querySelector("#extraFormSearch");
  let serializedExtraFormSearch = "";
  if (extraFormSearch) {
    serializedExtraFormSearch = $(extraFormSearch).serialize();
  }

  var topicQuery = getSearchQuery();
  $.progressMessage();
  var ieFix = new Date().getTime();
  var componentId = getComponentId();
  $.get(getWebContext()+'/RAjaxPublicationsListServlet?'+serializedExtraFormSearch, {Index:0,Query:topicQuery,ComponentId:componentId,IEFix:ieFix},
      __updateDataAndUI,"html");
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
    document.getElementById("extraFormSearch").reset();
  } catch (e) {
  }
}
