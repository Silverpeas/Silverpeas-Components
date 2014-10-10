var subscriptionWindow = window;
var favoriteWindow = window;
var importFileWindow = window;
var importFilesWindow = window;
var exportComponentWindow = window;

function addFavorite(name, description, url)
{
  postNewLink(name, url, description);
}

function addSubscription() {
  url = "subscriptionsManager.jsp?Action=AddSubscription&Id=" + getCurrentNodeId();
  windowName = "subscriptionWindow";
  windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
  larg = "550";
  haut = "350";
  if (!subscriptionWindow.closed && subscriptionWindow.name === "subscriptionWindow")
    subscriptionWindow.close();
  subscriptionWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function importFile()
{
  url = "importOneFile.jsp?Action=ImportFileForm&TopicId=" + getCurrentNodeId();
  windowName = "importFileWindow";
  windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
  larg = "610";
  haut = "370";
  if (!importFileWindow.closed && importFileWindow.name === "importFileWindow")
    importFileWindow.close();
  importFileWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function importFiles()
{
  url = "importMultiFiles.jsp?Action=ImportFilesForm&TopicId=" + getCurrentNodeId();
  windowName = "importFilesWindow";
  windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
  larg = "610";
  haut = "460";
  if (!importFilesWindow.closed && importFilesWindow.name === "importFilesWindow")
    importFilesWindow.close();
  importFilesWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function openExportPDFPopup() {
  chemin = "ExportAttachementsToPDF?TopicId=" + getCurrentNodeId();
  largeur = "700";
  hauteur = "500";
  SP_openWindow(chemin, "ExportWindow", largeur, hauteur, "scrollbars=yes, resizable=yes");
}

function openSPWindow(fonction, windowName) {
  pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400', 'scrollbars=yes, resizable, alwaysRaised');
}

function exportTopic() {
  exportComponentWindow = SP_openWindow("ExportTopic?TopicId=" + getCurrentNodeId(), "exportComponentWindow", 700, 350, "scrollbars=yes, resizable=yes");
}

function openSPWindow(fonction, windowName) {
  pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400', 'scrollbars=yes, resizable, alwaysRaised');
}

function openPredefinedPdCClassification(nodeId) {
  var uri = getWebContext() + "/pdcPeas/jsp/predefinedClassification.jsp?componentId=" + getComponentId();
  if (nodeId != 0) {
    uri += "&nodeId=" + nodeId;
  }
  SP_openWindow(uri, "Classification", '650', '600', 'scrollbars=yes, resizable, alwaysRaised');
}

function displayTopicDescription(id) {
  //display rich description of topic
  var ieFix = new Date().getTime();
  var componentId = getComponentId();
  $.get(getWebContext() + '/KmeliaAJAXServlet', {Id: id, Action: 'GetTopicWysiwyg', ComponentId: componentId, IEFix: ieFix},
  function(data) {
    $("#topicDescription").html(data);
    activateIDCards();
  }, "html");
}

function refreshPublications()
{
  var nodeId = getCurrentNodeId();
  var ieFix = new Date().getTime();
  var componentId = getComponentId();
  $.get(getWebContext() + '/RAjaxPublicationsListServlet', {Id: nodeId, ComponentId: componentId, IEFix: ieFix},
  function(data) {
    //$('#pubList').html(data);
	updateHtmlContainingAngularDirectives($('#pubList'), data);
    activateUserZoom();
  }, "html");
}

function validatePublicationClassification(s)
{
  var componentId = getComponentId();
  SP_openWindow(getWebContext() + '/Rkmelia/' + componentId + '/validateClassification?' + s, "Validation", '600', '400', 'scrollbars=yes, resizable, alwaysRaised');
}

function closeWindows() {
  if (!subscriptionWindow.closed && subscriptionWindow.name === "subscriptionWindow") {
    subscriptionWindow.close();
  }

  if (!favoriteWindow.closed && favoriteWindow.name === "favoriteWindow") {
    favoriteWindow.close();
  }
}

function publicationGoTo(id) {
  closeWindows();
  document.pubForm.PubId.value = id;
  document.pubForm.submit();
}

function sortGoTo(selectedIndex) {
  closeWindows();
  if (selectedIndex !== 0 && selectedIndex !== 1) {
    var topicQuery = getSearchQuery();
    var sort = document.publicationsForm.sortBy[selectedIndex].value;
    var ieFix = new Date().getTime();
    var componentId = getComponentId();
    $.get(getWebContext() + '/RAjaxPublicationsListServlet', {Index: 0, Sort: sort, ComponentId: componentId, Query: topicQuery, IEFix: ieFix},
    function(data) {
      //$('#pubList').html(data);
      updateHtmlContainingAngularDirectives($('#pubList'), data);
      activateUserZoom();
    }, "html");
    return;
  }
}

function displayPath(id) {
  var url = getWebContext() + "/services/folders/" + getComponentId() + "/" + id + "/path?lang=" + getTranslation();
  $.getJSON(url, function(data) {
    //remove topic breadcrumb
    removeBreadCrumbElements();
    $(data).each(function(i, topic) {
      if (topic.attr["id"] != 0) {
        addBreadCrumbElement("javascript:topicGoTo(" + topic.attr["id"] + ")", topic.data);
      }
    });
  });
}

function displayPublications(id) {
  //display publications of topic
  var pubIdToHighlight = getPubIdToHighlight();
  var ieFix = new Date().getTime();
  var componentId = getComponentId();
  var url = getWebContext() + "/RAjaxPublicationsListServlet";
  $.get(url, {Id: id, ComponentId: componentId, PubIdToHighlight: pubIdToHighlight, IEFix: ieFix},
  function(data) {
    //$('#pubList').html(data);
	updateHtmlContainingAngularDirectives($('#pubList'), data);
    activateUserZoom();
  }, "html");
}

function displayOperations(id) {
  var ieFix = new Date().getTime();
  var componentId = getComponentId();
  var url = getWebContext() + "/KmeliaJSONServlet";
  $.get(url, {Id: id, Action: 'GetOperations', ComponentId: componentId, IEFix: ieFix},
  function(operations) {
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
    applyTokenSecurity();
  }, 'json');
}

function displayResponsibles() {
  displayComponentResponsibles(getCurrentUserId(), getComponentId());
}

function addAppAsFavorite() {
  addFavoriteApp(getComponentId());
}

function initOperations(id, op) {
  $("#menutoggle").css({'display': 'block'});

  oMenu.clearContent();
  $('#menubar-creation-actions').empty();

  var menuItem;
  var groupIndex = 0;
  var groupEmpty = true;
  var menuEmpty = true;
  var menuBarEmpty = true;

  if (op.emptyTrash) {
    menuItem = new YAHOO.widget.MenuItem(getString('EmptyBasket'), {url: "javascript:onClick=emptyTrash()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.admin) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.operations.setupComponent'), {url: getWebContext() + "/RjobStartPagePeas/jsp/SetupComponent?ComponentId=" + getComponentId()});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.pdc) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.PDCParam'), {url: "javascript:onClick=openSPWindow('" + getWebContext() + "/RpdcUtilization/jsp/Main?ComponentId=" + getComponentId() + "','utilizationPdc1')"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.predefinedPdcPositions) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.PDCPredefinePositions'), {url: "javascript:onClick=openPredefinedPdCClassification(" + id + ")"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.templates) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ModelUsed'), {url: "ModelUsed"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.manageSubscriptions) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.manageSubscriptions'), {url: "ManageSubscriptions"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.exporting) {
    if (id == "0") {
      menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportComponent'), {url: "javascript:onClick=exportTopic()"});
    } else {
      menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportTopic'), {url: "javascript:onClick=exportTopic()"});
    }
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.exportPDF) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportPDF'), {url: "javascript:openExportPDFPopup()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.addTopic) {
    var label = getString('CreerSousTheme');
    var url = "javascript:onclick=addNodeToCurrentNode()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.addTopic"], label);
    menuBarEmpty = false;
  }
  if (op.updateTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('ModifierSousTheme'), {url: "javascript:onclick=updateCurrentNode()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.deleteTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('SupprimerSousTheme'), {url: "javascript:onclick=deleteCurrentNode()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.sortSubTopics) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.SortTopics'), {url: "javascript:onclick=sortSubTopics()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.copyTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.copy'), {url: "javascript:onclick=copyCurrentNode()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.cutTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.cut'), {url: "javascript:onclick=cutCurrentNode()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.hideTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('TopicVisible2Invisible'), {url: "javascript:onclick=changeCurrentTopicStatus()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.showTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('TopicInvisible2Visible'), {url: "javascript:onclick=changeCurrentTopicStatus()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.wysiwygTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('TopicWysiwyg'), {url: "javascript:onclick=updateCurrentTopicWysiwyg()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.shareTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.shareTopic'), {url: "javascript:onclick=shareCurrentTopic()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.addPubli) {
    var label = getString('PubCreer');
    var url = "NewPublication";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.addPubli"], label);
    menuBarEmpty = false;
  }
  if (op.wizard) {
    var label = getString('kmelia.Wizard');
    var url = "WizardStart";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.wizard"], label);
    menuBarEmpty = false;
  }
  if (op.importFile) {
    var label = getString('kmelia.ImportFile');
    var url = "javascript:onclick=importFile()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.importFile"], label);
    menuBarEmpty = false;
  }
  if (op.importFiles) {
    var label = getString('kmelia.ImportFiles');
    var url = "javascript:onclick=importFiles()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.importFiles"], label);
    menuBarEmpty = false;
  }
  if (op.sortPublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.OrderPublications'), {url: "ToOrderPublications"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.updateChain) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.updateByChain'), {url: "javascript:onclick=updateChain()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.copyPublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.copyPublications'), {url: "javascript:onclick=copyPublications()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.cutPublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.cutPublications'), {url: "javascript:onclick=cutPublications()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.paste) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.paste'), {url: "javascript:onclick=pasteFromOperations()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.deletePublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.deletePublications'), {url: "javascript:onclick=deletePublications()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.exportSelection) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.exportSelection'), {url: "javascript:onclick=exportPublications()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.subscriptions) {
    var label = getString('SubscriptionsAdd');
    var url = "javascript:onclick=addSubscription()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    //addCreationItem(url, icons["operation.subscribe"], label);
  }
  if (op.favorites) {
    var label = getString('FavoritesAdd1') + ' ' + getString('FavoritesAdd2');
    var url = "javascript:onclick=addCurrentNodeAsFavorite()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    //addCreationItem(url, icons["operation.favorites"], label);
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.statistics) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.statistics'), {url: "javascript:onclick=showStats()"});
    oMenu.addItem(menuItem, groupIndex);
    menuEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.mylinks) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.favorite.application.add'), {
      classname: 'space-or-application-favorites-operation',
      url: "javascript:onclick=addAppAsFavorite()"
    });
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.responsibles) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.component.responsibles'), {
      classname: 'space-or-component-responsibles-operation',
      url: "javascript:onclick=displayResponsibles()"
    });
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  oMenu.render();

  if (menuEmpty) {
    $("#menutoggle").css({'display': 'none'});
  }
  if (menuBarEmpty) {
    $('#menubar-creation-actions').css({'display': 'none'});
  }
}

function addCreationItem(url, icon, label) {
  if ($('#menubar-creation-actions').length > 0) {
    var creationItem = "<a href=\"" + url + "\" class=\"menubar-creation-actions-item\"><span><img src=\"" + icon + "\" alt=\"\"/>" + label + "</span></a>";
    $('#menubar-creation-actions').css({'display': 'block'});
    $('#menubar-creation-actions').append(creationItem);
  }
}

function hideOperations() {
  $("#menutoggle").css({'display': 'none'}); //hide operations
  if ($('#menubar-creation-actions').length > 0) {
    $('#menubar-creation-actions').empty();
    $('#menubar-creation-actions').css({'display': 'none'});
  }
}

var currentNodeId;
var currentTopicName;
var currentTopicDescription;
var currentTopicStatus;
var currentTopicTranslations;

function getCurrentNodeId() {
  return currentNodeId;
}

function setCurrentNodeId(id) {
  //alert("setCurrentNodeId : id = "+id);
  currentNodeId = id;
}

function getCurrentTopicStatus() {
  return currentTopicStatus;
}

function setCurrentTopicStatus(status) {
  currentTopicStatus = status;
}

function setCurrentTopicName(name) {
  currentTopicName = name;
  $("#addOrUpdateNode #folderName").html(name);
}

function setCurrentTopicDescription(desc) {
  currentTopicDescription = desc;
  $("#addOrUpdateNode #folderDescription").html(desc);
}
function setCurrentTopicTranslations(trans) {
  currentTopicTranslations = trans;
}

var translations;
function storeTranslations(trans) {
  translations = trans;
  var select = $('select[name="I18NLanguage"]');
  select.attr("onchange", "showTranslation(this.value.substring(0,2))");
  if (translations != null && translations.length > 1) {
    //display delete operation
    if ($("#deleteTranslation").length === 0) {
      var img = '<img src="' + getWebContext() + '/util/icons/delete.gif" title="' + getString('GML.translationRemove') + '" alt="' + getString('GML.translationRemove') + '"/>';
      $("<a id=\"deleteTranslation\" href=\"javascript:document.getElementById('TranslationRemoveIt').value='true';document.topicForm.submit();\">" + img + "</a>").insertAfter(select);
    }
  } else {
    //remove delete operation
    $("#deleteTranslation").remove();
  }
  showTranslation($('select[name="I18NLanguage"] option:selected').val().substring(0, 2));
  return false;
}

function showTranslation(lang) {
  var found;
  var i = 0;
  while (!found && i < translations.length) {
    if (translations[i].language === lang) {
      found = true;
      $("#addOrUpdateNode #folderName").val(translations[i].name);
      $("#addOrUpdateNode #folderDescription").val(translations[i].description);
      $('select[name="I18NLanguage"] option:selected').val(translations[i].language + "_" + translations[i].id);
    }
    i++;
  }
  if (!found) {
    $("#addOrUpdateNode #folderName").val("");
    $("#addOrUpdateNode #folderDescription").val("");
  }
}

function getDateFormat() {
  if (getLanguage() === "fr") {
    return "dd/mm/yy";
  } else if (getLanguage() === "de") {
    return "dd.mm.yy";
  }
  return "mm/dd/yy";
}

function displayTopicInformation(id) {
  if (id !== "0" && id !== "1" && id !== getToValidateFolderId()) {
    $("#footer").css({'visibility': 'visible'});
    var url = getWebContext() + "/services/folders/" + getComponentId() + "/" + id;
    $.getJSON(url, function(topic) {
      var name = topic.data;
      var desc = topic.attr["description"];
      var date = $.datepicker.formatDate(getDateFormat(), new Date(topic.attr["creationDate"]));
      ;
      var creator = topic.attr["creator"].fullName;
      $("#footer").html(getString('kmelia.topic.info') + ' ' + creator + ' - ' + date + ' - <a id="topicPermalink" href="#"><img src="' + icons["permalink"] + '"/></a>');
      $("#footer #topicPermalink").attr("href", getWebContext() + "/Topic/" + id + "?ComponentId=" + getComponentId());
      setCurrentTopicName(name);
      setCurrentTopicDescription(desc);
      setCurrentTopicStatus(topic.attr["status"]);
      if (params["i18n"]) {
        setCurrentTopicTranslations(topic.translations);
      }
      activateUserZoom();
    });
  } else {
    $("#footer").css({'visibility': 'hidden'});
  }
}

function writeInConsole(text) {
  if (typeof console !== 'undefined') {
    console.log(text);
  } else {
    alert(text);
  }
}

function deleteFolder(nodeId, nodeLabel) {
  if (window.confirm(getString('ConfirmDeleteTopic') + " '" + nodeLabel + "' ?")) {
    var componentId = getComponentId();
    var url = getWebContext() + '/KmeliaAJAXServlet';
    $.post(url, {Id: nodeId, ComponentId: componentId, Action: 'Delete'},
    function(data) {
      if ((data - 0) == data && data.length > 0) {
        // fires event
        try {
          nodeDeleted(nodeId);
        } catch (e) {
          writeInConsole(e);
        }
        // go to parent node
        displayTopicContent(data);
      } else {
        alert(data);
      }
    }, 'text');
  }
}

function deleteCurrentNode() {
  deleteFolder(getCurrentNodeId(), currentTopicName);
}

function sortSubTopics() {
  closeWindows();
  SP_openWindow("ToOrderTopics?Id=" + getCurrentNodeId(), "topicAddWindow", "600", "500", "directories=0,menubar=0,toolbar=0,scrollbars=1,alwaysRaised,resizable");
}

function addNodeToCurrentNode() {
  topicAdd(getCurrentNodeId(), false);
}

function topicAdd(topicId, isLinked) {
  var translation = getTranslation();
  var rightsOnTopic = params["rightsOnTopic"];
  var url = "ToAddTopic?Id=" + topicId + "&Translation=" + translation;
  if (isLinked) {
    url += "&IsLink=true";
  }
  if (rightsOnTopic) {
    location.href = url;
  } else {
    document.topicForm.action = "AddTopic";
    $("#addOrUpdateNode #folderName").val("");
    $("#addOrUpdateNode #folderDescription").val("");
    $("#addOrUpdateNode #parentId").val(topicId);
    translations = null;
    //remove delete operation
    $("#deleteTranslation").remove();

    // display path of parent
    var url = getWebContext() + "/services/folders/" + getComponentId() + "/" + topicId + "/path?lang=" + getTranslation() + "&IEFix=" + new Date().getTime();
    $.getJSON(url, function(data) {
      //remove topic breadcrumb
      $("#addOrUpdateNode #path").html("");
      $(data).each(function(i, topic) {
        var item = " > " + topic.data;
        if (topic.attr["id"] == 0) {
          item = getComponentLabel();
        }
        $("#addOrUpdateNode #path").html($("#addOrUpdateNode #path").html() + item);
      });
    });

    // open modal dialog
    $("#addOrUpdateNode").dialog({
      modal: true,
      resizable: false,
      title: getString('CreerSousTheme'),
      width: 600,
      buttons: {
        "OK": function() {
          submitTopic();
        },
        "Annuler": function() {
          $(this).dialog("close");
        }
      }
    });
  }
}

function updateCurrentNode() {
  if (params["i18n"]) {
    storeTranslations(currentTopicTranslations);
  } else {
    $("#addOrUpdateNode #folderName").val(currentTopicName);
    $("#addOrUpdateNode #folderDescription").val(currentTopicDescription);
  }
  topicUpdate(getCurrentNodeId());
}

function topicUpdate(id) {
  var translation = getTranslation();
  var rightsOnTopic = params["rightsOnTopic"];
  if (rightsOnTopic) {
    location.href = "ToUpdateTopic?Id=" + id + "&Translation=" + translation;
  } else {
    document.topicForm.action = "UpdateTopic";
    $("#addOrUpdateNode #topicId").val(id);

    // display path of parent
    var url = getWebContext() + "/services/folders/" + getComponentId() + "/" + id + "/path?lang=" + getTranslation() + "&IEFix=" + new Date().getTime();
    $.getJSON(url, function(data) {
      //remove topic breadcrumb
      $("#addOrUpdateNode #path").html("");
      $(data).each(function(i, topic) {
        var item = " > " + topic.data;
        if (topic.attr["id"] == 0) {
          item = getComponentLabel();
        } else {
          if (i === data.length - 1) {
            item = "";
          }
        }
        $("#addOrUpdateNode #path").html($("#addOrUpdateNode #path").html() + item);
      });
    });

    // open modal dialog
    $("#addOrUpdateNode").dialog({
      modal: true,
      resizable: false,
      title: getString('ModifierSousTheme'),
      width: 600,
      buttons: {
        "OK": function() {
          submitTopic();
        },
        "Annuler": function() {
          $(this).dialog("close");
        }
      }
    });
  }
}

function submitTopic() {
  var errorMsg = "";
  var errorNb = 0;
  var title = stripInitialWhitespace(document.topicForm.Name.value);
  if (isWhitespace(title)) {
    errorMsg += "  - '" + getString('TopicTitle') + "' " + getString('GML.MustBeFilled') + "\n";
    errorNb++;
  }
  switch (errorNb) {
    case 0 :
      result = true;
      break;
    case 1 :
      errorMsg = getString('GML.ThisFormContains') + " 1 " + getString('GML.error') + " : \n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
    default :
      errorMsg = getString('GML.ThisFormContains') + " " + errorNb + " " + getString('GML.errors') + " :\n" + errorMsg;
      window.alert(errorMsg);
      result = false;
      break;
  }
  if (result) {
    document.topicForm.submit();
  }
}

function emptyTrash() {
  if (window.confirm(getString('ConfirmFlushTrashBean'))) {
    $.progressMessage();
    var componentId = getComponentId();
    var url = getWebContext() + '/KmeliaAJAXServlet';
    $.post(url, {ComponentId: componentId, Action: 'EmptyTrash'},
    function(data) {
      $.closeProgressMessage();
      if (data === "ok") {
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
    $("#DnD").css({'display': 'block'});
  } else {
    $("#DnD").css({'display': 'none'});
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
  addFavorite(path, description, url);
}

function updateCurrentTopicWysiwyg() {
  updateTopicWysiwyg(getCurrentNodeId());
}

function shareCurrentTopic() {
  var sharingObject = {
      componentId: getComponentId(),
      type       : "Node",
      id         : getCurrentNodeId(),
      name   : $("#" + getCurrentNodeId()).find('a:first').text()
  };
  createSharingTicketPopup(sharingObject);
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

  var url = getWebContext() + '/KmeliaAJAXServlet';
  $.post(url, {ComponentId: getComponentId(), Action: 'Paste', Id: id, IEFix: new Date().getTime()},
  function(data) {
    $.closeProgressMessage();
    if (data === "ok") {
      reloadPage(id);
    } else {
      notyError(data);
    }
  }, 'text');
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

function publicationGoToFromMain(id) {
  closeWindows();
  document.pubForm.CheckPath.value = "1";
  document.pubForm.PubId.value = id;
  document.pubForm.submit();
}

function fileUpload() {
  document.fupload.submit();
}

function doPagination(index) {
  var topicQuery = getSearchQuery();
  var ieFix = new Date().getTime();
  var componentId = getComponentId();
  var selectedPublicationIds = getSelectedPublicationIds();
  var notSelectedPublicationIds = getNotSelectedPublicationIds();
  var url = getWebContext() + '/RAjaxPublicationsListServlet';
  $.get(url, {Index: index, ComponentId: componentId, Query: topicQuery, SelectedPubIds: selectedPublicationIds, NotSelectedPubIds: notSelectedPublicationIds, IEFix: ieFix},
  function(data) {
    //$('#pubList').html(data);
	updateHtmlContainingAngularDirectives($('#pubList'), data);
    activateUserZoom();
    showPublicationCheckedBoxes();
    location.href = "#pubList";
  }, "html");
}

function showStats() {
  //alert("Current componentId = " + getComponentId() + " and topicId=" + getCurrentNodeId());
  //TODO call how to update HTML from this page with new Ajax call
  //var url = getWebContext() + "/Rkmelia/statistic?componentId=" + getComponentId() + "&topicId=" + getCurrentNodeId();
  var url = "statistics?componentId=" + getComponentId() + "&topicId=" + getCurrentNodeId();
  //alert("loading url " + url);
  location.href = url;
}

function changeStatus(nodeId, currentStatus) {
  closeWindows();
  var newStatus = "Visible";
  if (currentStatus === "Visible") {
    newStatus = "Invisible";
  }

  var title = getString('TopicVisible2Invisible');
  if (newStatus === 'Invisible') {
    $("#visibleInvisible-message p").html(getString('TopicVisible2InvisibleRecursive'));
  } else {
    $("#visibleInvisible-message p").html(getString('TopicInvisible2VisibleRecursive'));
    title = getString('TopicInvisible2Visible');
  }

  $("#visibleInvisible-message").dialog({
    modal: true,
    resizable: false,
    width: 400,
    title: title,
    buttons: [{
        text: getString('GML.yes'),
        click: function() {
          _updateTopicStatus(nodeId, newStatus, '1');
          $(this).dialog("close");
        }
      }, {
        text: getString('kmelia.folder.onlythisfolder'),
        click: function() {
          _updateTopicStatus(nodeId, newStatus, '0');
          $(this).dialog("close");
        }
      }, {
        text: getString('GML.cancel'),
        click: function() {
          $(this).dialog("close");
        }
      }]
  });
}

function _updateTopicStatus(nodeId, status, recursive) {
  $.post(getWebContext() + '/KmeliaAJAXServlet', {ComponentId: getComponentId(), Action: 'UpdateTopicStatus', Id: nodeId, Status: status, Recursive: recursive},
  function(data) {
    if (data === "ok") {
      updateUIStatus(nodeId, status, recursive);
    } else {
      alert(data);
    }
  }, 'text');
}

function movePublication(id, sourceId, targetId) {
  var componentId = getComponentId();
  var url = getWebContext() + '/KmeliaAJAXServlet';
  $.post(url, {Id: id, SourceNodeId: sourceId, TargetNodeId: targetId, ComponentId: componentId, Action: 'MovePublication'},
  function(data) {
    //data = "erreur en votre faveur zlekfj kjf kjh kjsdh fkjshdjfkhsdjkhf fjkshd kjfhsd kjfhsdkjhf"
    if (data === "ok") {
      // fires event
      try {
        publicationMovedSuccessfully(id, targetId);
      } catch (e) {
        writeInConsole(e);
      }
    } else {
      publicationMovedInError(id, data);
    }
  }, 'text');
}
