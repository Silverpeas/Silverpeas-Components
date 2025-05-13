/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

let favoriteWindow = window;
let importFileWindow = window;
let importFilesWindow = window;
let exportComponentWindow = window;


function addFavorite(name, description, url)
{
  postNewLink(name, url, description);
}

function importFile()
{
  const url = "importOneFile.jsp?Action=ImportFileForm&TopicId=" + getCurrentNodeId();
  const windowName = "importFileWindow";
  const windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
  const larg = "610";
  const haut = "370";
  if (!importFileWindow.closed && importFileWindow.name === "importFileWindow")
    importFileWindow.close();
  importFileWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function importFiles()
{
  const url = "importMultiFiles.jsp?Action=ImportFilesForm&TopicId=" + getCurrentNodeId();
  const windowName = "importFilesWindow";
  const windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars=1";
  const larg = "610";
  const haut = "460";
  if (!importFilesWindow.closed && importFilesWindow.name === "importFilesWindow")
    importFilesWindow.close();
  importFilesWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function openExportPDFPopup() {
  const chemin = "ExportAttachementsToPDF?TopicId=" + getCurrentNodeId();
  const largeur = "720";
  const hauteur = "300";
  SP_openWindow(chemin, "ExportWindow", largeur, hauteur, "scrollbars=yes, resizable=yes");
}

function openSPWindow(fonction, windowName) {
  window.pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400', 'scrollbars=yes, resizable, alwaysRaised');
}

function exportTopic() {
  window.exportComponentWindow = SP_openWindow("ExportTopic?TopicId=" + getCurrentNodeId(), "exportComponentWindow", 720, 420, "scrollbars=yes, resizable=yes");
}

function openPredefinedPdCClassification(nodeId) {
  let uri = getWebContext() + "/pdcPeas/jsp/predefinedClassification.jsp?componentId=" + getComponentId();
  if (nodeId != 0) {
    uri += "&nodeId=" + nodeId;
  }

  window.requestEditorDialog = jQuery.popup.load(uri);
  window.requestEditorDialog.show('free', {
    title: getString('GML.PDCPositionsPredefinition'),
    closeOnEscape: true,
    resizable: true,
    width: '800px'});
}

function displayTopicDescription(id) {
  //display rich description of topic
  const ieFix = new Date().getTime();
  const componentId = getComponentId();
  $.get(getWebContext() + '/KmeliaAJAXServlet', {Id: id, Action: 'GetTopicWysiwyg', ComponentId: componentId, IEFix: ieFix},
      function(data) {
        if (data && data.length > 0) {
          $("#topicDescription").show();
          $("#topicDescription").html(data);
          activateIDCards();
        } else {
          $("#topicDescription").html("");
          $("#topicDescription").hide();
        }
      }, "html");
}

function refreshPublications()
{
  const nodeId = getCurrentNodeId();
  const ieFix = new Date().getTime();
  const componentId = getComponentId();
  $.get(getWebContext() + '/RAjaxPublicationsListServlet',
      {Id : nodeId, ComponentId : componentId, IEFix : ieFix}, __updateDataAndUI, "html");
}

function validatePublicationClassification(s)
{
  const componentId = getComponentId();
  SP_openWindow(getWebContext() + '/Rkmelia/' + componentId + '/validateClassification?' + s, "Validation", '600', '400', 'scrollbars=yes, resizable, alwaysRaised');
}

function closeWindows() {
  if (!favoriteWindow.closed && favoriteWindow.name === "favoriteWindow") {
    favoriteWindow.close();
  }
}

function publicationGoTo(id) {
  closeWindows();
  document.pubForm.PubId.value = id;
  document.pubForm.submit();
}

const __updateDataAndUI = function(data) {
  updateHtmlContainingAngularDirectives($('#pubList'), data);
  activateUserZoom();
  showPublicationCheckedBoxes();
  setTimeout(checkMenuItemsAboutSelection, 0);
  spProgressMessage.hide();
}

const __updateDataAndUIWithError = function(data) {
  const $container = document.querySelector('#pubList');
  const $div = document.createElement('div');
  $div.classList.add('inlineMessage-nok');
  try {
    $div.innerHTML = data;
    sp.element.querySelectorAll('link', $div).forEach(function($link) {
      $link.remove();
    })
  } catch (e) {
    $div.innerText = sp.i18n.get('GML.error');
  } finally {
    $container.innerHTML = '';
    $container.appendChild($div);
    spProgressMessage.hide();
  }
}

function checkMenuItemsAboutSelection() {
  if ($("#pubList ul>li").length > 0 ){
    $("#menuitem-updatepubs").show();
    $("#menuitem-deletepubs").show();
  } else {
    $("#menuitem-updatepubs").hide();
    $("#menuitem-deletepubs").hide();
  }
}

function sortGoTo(selectedIndex) {
  closeWindows();
  if (selectedIndex !== 0 && selectedIndex !== 1) {
    const topicQuery = getSearchQuery();
    const sort = document.publicationsForm.sortBy[selectedIndex].value;
    const ieFix = new Date().getTime();
    const componentId = getComponentId();
    $.get(getWebContext() + '/RAjaxPublicationsListServlet',
        {Index : 0, Sort : sort, ComponentId : componentId, Query : topicQuery, IEFix : ieFix},
        __updateDataAndUI, "html");
    return;
  }
}

function resetSort() {
  jQuery.popup.confirm(getString('kmelia.sort.manual.reset.confirm'), function() {
    const ieFix = new Date().getTime();
    const componentId = getComponentId();
    $.get(getWebContext() + '/RAjaxPublicationsListServlet',
        {Index : 0, ResetManualSort : true, ComponentId : componentId, IEFix : ieFix},
        __updateDataAndUI, "html");
  });
}

function displayPath(id) {
  const url = getWebContext() + "/services/folders/" + getComponentId() + "/" + id + "/path?lang=" + getTranslation();
  $.getJSON(url, function(data) {
    //remove topic breadcrumb
    removeBreadCrumbElements();
    $(data).each(function(i, topic) {
      if (topic.id !== '0') {
        addBreadCrumbElement("javascript:topicGoTo(" + topic.id + ")", topic.text);
      }
    });
  });
}

function displayPublications(id) {
  //display publications of topic
  const pubIdToHighlight = getPubIdToHighlight();
  const ieFix = new Date().getTime();
  const componentId = getComponentId();
  const url = getWebContext() + "/RAjaxPublicationsListServlet";
  const timer = setTimeout(spProgressMessage.show, 700);
  return sp.ajaxRequest(url)
      .withParams({
        Id : id, ComponentId : componentId, PubIdToHighlight : pubIdToHighlight, IEFix : ieFix
      })
      .send()
      .then(function(request) {
        clearTimeout(timer);
        __updateDataAndUI(request.responseText);
      }, function(request) {
        clearTimeout(timer);
        __updateDataAndUIWithError(request.responseText);
      });
}
function displayOperations(id) {
  const ieFix = new Date().getTime();
  const componentId = getComponentId();
  const url = getWebContext() + "/KmeliaJSONServlet";
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

  let label;
  let url;
  let menuItem;
  let groupIndex = 0;
  let groupEmpty = true;
  let menuEmpty = true;
  let menuBarEmpty = true;
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
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.PDCParam'), {url: "javascript:onClick=openSPWindow('" + getWebContext() + "/RpdcUtilization/jsp/Main?ComponentId=" + getComponentId() + "','utilizationPdc1')"});
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

  if (op.exportApplication && id == "0") {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportComponent'),
        {url : "javascript:onClick=exportTopic()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.exportTopic && id != "0") {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportTopic'),
        {url : "javascript:onClick=exportTopic()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.exportPDFTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportPDFTopic'), {url: "javascript:openExportPDFPopup()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.exportPDFApplication) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.ExportPDFApplication'), {url: "javascript:openExportPDFPopup()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.addTopic) {
    label = getString('CreerSousTheme');
    url = "javascript:onclick=addNodeToCurrentNode()";
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
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.folder.copy'), {url: "javascript:onclick=copyCurrentNode()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.cutTopic) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.folder.cut'), {url: "javascript:onclick=cutCurrentNode()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.paste) {
    kmeliaWebService.getClipboardState().then(function(clipboardState) {
      let isClipboardEmpty = (clipboardState=='IS_EMPTY');
      menuItem = new YAHOO.widget.MenuItem(getString('GML.paste'), {url: "javascript:onclick=pasteFromOperations()", disabled: isClipboardEmpty});
      oMenu.addItem(menuItem, groupIndex);
      groupEmpty = false;
    });
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
    label = getString('PubCreer');
    url = "NewPublication";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.addPubli"], label);
    menuBarEmpty = false;
  }
  if (op.addFiles) {
    label = getString('kmelia.AddFile');
    url = "javascript:onclick=kmeliaFileAddingApp.addFiles()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.importFile"], label);
    menuBarEmpty = false;
  }
  if (op.importFile) {
    label = getString('kmelia.ImportFile');
    url = "javascript:onclick=importFile()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    addCreationItem(url, icons["operation.importFile"], label);
    menuBarEmpty = false;
  }
  if (op.importFiles) {
    label = getString('kmelia.ImportFiles');
    url = "javascript:onclick=importFiles()";
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
  if (op.selectAllPublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.publications.select'), {url: "javascript:onclick=selectAllPublications(true)", id: "menuitem-selectAllPubs"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.unselectAllPublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.publications.unselect'), {url: "javascript:onclick=selectAllPublications(false)", id: "menuitem-selectAllPubs"});
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

  if (op.updatePublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.updatePublications'), {url: "javascript:onclick=updatePublications()", id: "menuitem-updatepubs"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }
  if (op.deletePublications) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.deletePublications'), {url: "javascript:onclick=deletePublications()", id: "menuitem-deletepubs"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.putPublicationsInBasket)  {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.putPublicationsInBasket'), {
      url: "javascript:onclick=putPublicationsInBasket()"});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (!groupEmpty) {
    groupIndex++;
    groupEmpty = true;
    menuEmpty = false;
  }

  if (op.exportSelection) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.exportSelection'), {
      id: "operation-publications-select",
      url: "javascript:onclick=exportPublications()"
    });
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
  }

  if (op.subscriptions || op.topicSubscriptions) {
    let topicId = undefined;
    let subscribeLabel = undefined;
    let unsubscribeLabel = undefined;
    label = '<span id="subscriptionMenuLabel"></span>';
    url = "javascript:onclick=spSubManager.switchUserSubscription()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    if (op.topicSubscriptions) {
      subscribeLabel = getString('SubscriptionsAdd');
      unsubscribeLabel = getString('SubscriptionsRemove');
      topicId = op.context.nodeId;
    }
    window.SUBSCRIPTION_PROMISE.then(function() {
      const subscriptionResourceType = op.topicSubscriptions
          ? jQuery.subscription.subscriptionType.NODE
          : jQuery.subscription.subscriptionType.COMPONENT;
      window.spSubManager = new SilverpeasSubscriptionManager({
        componentInstanceId : op.context.componentId,
        subscriptionResourceType : subscriptionResourceType,
        resourceId : topicId,
        labels : {
          subscribe : subscribeLabel,
          unsubscribe : unsubscribeLabel
        },
        $menuLabel : undefined
      });
    });
  }

  if (op.favorites) {
    label = getString('FavoritesAdd1') + ' ' + getString('FavoritesAdd2');
    url = "javascript:onclick=addCurrentNodeAsFavorite()";
    menuItem = new YAHOO.widget.MenuItem(label, {url: url});
    oMenu.addItem(menuItem, groupIndex);
    groupEmpty = false;
    //addCreationItem(url, icons["operation.favorites"], label);
  }

  if (!groupEmpty) {
    groupIndex++;
    menuEmpty = false;
  }

  if (op.statistics) {
    menuItem = new YAHOO.widget.MenuItem(getString('kmelia.operation.statistics'), {url: "javascript:onclick=showStats()"});
    oMenu.addItem(menuItem, groupIndex);
    menuEmpty = false;
  }
  if (op.notify) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.notify'), {
      url: "javascript:onclick=notifyOnFolder('" + op.context.componentId + "', '" +
          op.context.nodeId + "')"
    });
    oMenu.addItem(menuItem, groupIndex);
  }

  if (op.mylinks) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.favorite.application.add'), {
      id: 'space-or-application-favorites-operation',
      url: "javascript:onclick=addAppAsFavorite()"
    });
    oMenu.addItem(menuItem, groupIndex);
  }

  if (op.responsibles) {
    menuItem = new YAHOO.widget.MenuItem(getString('GML.component.responsibles'), {
      id: 'space-or-component-responsibles-operation',
      url: "javascript:onclick=displayResponsibles()"
    });
    oMenu.addItem(menuItem, groupIndex);
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
    const creationItem = "<a href=\"" + url + "\" class=\"menubar-creation-actions-item\"><span><img src=\"" + icon + "\" alt=\"\"/>" + label + "</span></a>";
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

let currentNodeId;
let currentTopicName;
let currentTopicDescription;
let currentTopicStatus;
let currentTopicTranslations;

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
}

function setCurrentTopicDescription(desc) {
  currentTopicDescription = desc;
}
function setCurrentTopicTranslations(trans) {
  currentTopicTranslations = trans;
}

let translations;

function storeTranslations(trans) {
  translations = trans;
  const select = $('select[name="I18NLanguage"]');
  select.attr("onchange", "showTranslation(this.value.substring(0,2))");
  if (translations != null && translations.length > 1) {
    //display delete operation
    if ($("#deleteTranslation").length === 0) {
      const img = '<img src="' + getWebContext() + '/util/icons/delete.gif" title="' + getString('GML.translationRemove') + '" alt="' + getString('GML.translationRemove') + '"/>';
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
  let found;
  let i = 0;
  while (!found && i < translations.length) {
    if (translations[i].language === lang) {
      found = true;
      setDataInFolderDialog(translations[i].name, translations[i].description);
      $('select[name="I18NLanguage"] option:selected').val(translations[i].language + "_" + translations[i].id);
    }
    i++;
  }
  if (!found) {
    setDataInFolderDialog("", "");
    $('select[name="I18NLanguage"] option:selected').val(lang + "_-1");
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
  if (id !== "0" && id !== "1" && id !== getToValidateFolderId() && id !== getNonVisiblePubsFolderId()) {
    $("#footer").css({'visibility': 'visible'});
    const url = getWebContext() + "/services/folders/" + getComponentId() + "/" + id;
    $.getJSON(url, function(topic) {
      const name = topic.text;
      const desc = topic.attr["description"];
      const date = $.datepicker.formatDate(getDateFormat(), new Date(topic.attr["creationDate"]));
      const creator = topic.attr["creator"].fullName;
      $("#footer").html(getString('kmelia.topic.info') + ' ' + creator + ' - ' + date + ' - <a class="sp-permalink" id="topicPermalink" href="#"><img src="' + icons["permalink"] + '"/></a>');
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
  }
}

function deleteFolder(nodeId, nodeLabel) {
  const label = getString('ConfirmDeleteTopic') + " '" + nodeLabel + "' ?";
  jQuery.popup.confirm(label, function() {
    const componentId = getComponentId();
    const url = getWebContext() + '/KmeliaAJAXServlet';
    $.post(url, {Id: nodeId, ComponentId: componentId, Action: 'Delete'},
        function(data) {
          if (data !== null && data.length > 0 && !isNaN(data)) {
            // fires event
            try {
              nodeDeleted(nodeId);
            } catch (e) {
              writeInConsole(e);
            }
            // go to parent node
            displayTopicContent(data);
          } else {
            notyError(data);
          }
        }, 'text');
    return true;
  });
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

function applyWithNodePath(path, operation) {
  const url = getWebContext() + "/services/folders/" + getComponentId() + "/" + path + "/path?lang=" + getTranslation() + "&IEFix=" + new Date().getTime();
  $.getJSON(url, function(data) {
    operation(data);
  });
}

function applyWithNode(nodeId, operation) {
  const url = getWebContext() + "/services/folders/" + getComponentId() + "/" + nodeId + "?lang=" + getTranslation() + "&IEFix=" + new Date().getTime();
  $.getJSON(url, function(data) {
    operation(data);
  });
}

function topicAdd(topicId, isLinked) {
  const translation = getTranslation();
  const rightsOnTopic = params["rightsOnTopic"];
  let url = "ToAddTopic?Id=" + topicId + "&Translation=" + translation;
  if (isLinked) {
    url += "&IsLink=true";
  }
  if (rightsOnTopic) {
    location.href = url;
  } else {
    document.topicForm.action = "AddTopic";
    setDataInFolderDialog("", "");
    $("#addOrUpdateNode #parentId").val(topicId);
    translations = null;
    //remove delete operation
    $("#deleteTranslation").remove();

    // display path of parent
    applyWithNodePath(topicId, function(data) {
      //remove topic breadcrumb
      $("#addOrUpdateNode #path").html("");
      $(data).each(function(i, topic) {
        let item = " > " + topic.text;
        if (topic.id == 0) {
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
    setDataInFolderDialog(currentTopicName, currentTopicDescription);
  }
  topicUpdate(getCurrentNodeId());
}

function topicUpdate(id) {
  const translation = getTranslation();
  const rightsOnTopic = params["rightsOnTopic"];
  if (rightsOnTopic) {
    location.href = "ToModifyTopic?Id=" + id + "&Translation=" + translation;
  } else {
    document.topicForm.action = "UpdateTopic";
    $("#addOrUpdateNode #topicId").val(id);

    // display path of parent
    const url = getWebContext() + "/services/folders/" + getComponentId() + "/" + id + "/path?lang=" + getTranslation() + "&IEFix=" + new Date().getTime();
    $.getJSON(url, function(data) {
      //remove topic breadcrumb
      $("#addOrUpdateNode #path").html("");
      $(data).each(function(i, topic) {
        let item = " > " + topic.text;
        if (topic.id == 0) {
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
  let errorMsg = "";
  let errorNb = 0;
  const title = stripInitialWhitespace(document.topicForm.Name.value);
  if (isWhitespace(title)) {
    errorMsg += "  - '" + getString('TopicTitle') + "' " + getString('GML.MustBeFilled') + "\n";
    errorNb++;
  }
  let result = false;
  switch (errorNb) {
    case 0 :
      result = true;
      break;
    case 1 :
      errorMsg = getString('GML.ThisFormContains') + " 1 " + getString('GML.error') + " : \n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = getString('GML.ThisFormContains') + " " + errorNb + " " + getString('GML.errors') + " :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
  if (result) {
    document.topicForm.submit();
  }
}

function emptyTrash() {
  jQuery.popup.confirm(getString('ConfirmFlushTrashBean'), function() {
    spProgressMessage.show();
    const componentId = getComponentId();
    const url = getWebContext() + '/KmeliaAJAXServlet';
    $.post(url, {ComponentId: componentId, Action: 'EmptyTrash'},
        function(data) {
          spProgressMessage.hide();
          if (data === "ok") {
            displayTopicContent("1");
          } else {
            notyError(data);
          }
        }, 'text');
    return true;
  });
}

function checkDnD(id, operations) {
  if (operations.addPubli == true) {
    activateDragAndDrop();
  } else {
    muteDragAndDrop();
  }
}

function addCurrentNodeAsFavorite() {
  const path = $("#breadCrumb").text();
  let description = "";
  let url = getComponentPermalink();
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
  const sharingObject = {
    componentId : getComponentId(),
    type : "Node",
    id : getCurrentNodeId(),
    name : $("#" + getCurrentNodeId()).find('a:first').text()
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
  checkOnPaste(id);
}

function pasteDone(id) {
  reloadPage(id);
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

function publicationGoToFromMain(id) {
  closeWindows();
  document.pubForm.CheckPath.value = "1";
  document.pubForm.PubId.value = id;
  document.pubForm.submit();
}

function fileUpload() {
  document.fupload.submit();
}

function doPagination(index, nbItemsPerPage) {
  const topicQuery = getSearchQuery();
  const ieFix = new Date().getTime();
  const componentId = getComponentId();
  const selectedPublicationIds = getSelectedPublicationIds();
  const notSelectedPublicationIds = getNotSelectedPublicationIds();
  const url = getWebContext() + '/RAjaxPublicationsListServlet';
  $.get(url, {Index: index, NbItemsPerPage: nbItemsPerPage, ComponentId: componentId, Query: topicQuery, SelectedPubIds: selectedPublicationIds, NotSelectedPubIds: notSelectedPublicationIds, IEFix: ieFix},
      function(data) {
        __updateDataAndUI(data);
        location.href = "#pubList";
      }, "html");
}

function showStats() {
  spProgressMessage.show();
  location.href = "statistics?componentId=" + getComponentId() + "&topicId=" + getCurrentNodeId();
}

function changeStatus(nodeId, currentStatus) {
  closeWindows();
  let newStatus = "Visible";
  if (currentStatus === "Visible") {
    newStatus = "Invisible";
  }

  let title = getString('TopicVisible2Invisible');
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
          notyError(data);
        }
      }, 'text');
}

function movePublication(id, sourceId, targetId) {
  const params = {
    "dnd" : true,
    "pubId" : id,
    "sourceId" : sourceId,
    "targetId" : targetId
  };
  displayPasteDialog(params, function() {
    sendMovePublication(params);
  });
}

function sendMovePublication(params, extraParams) {
  const pubId = params.pubId;
  const sourceId = params.sourceId;
  const targetId = params.targetId;
  kmeliaWebService.movePublication(pubId, sourceId, targetId, extraParams).then(function(result) {
    if (result === "ok") {
      try {
        publicationMovedSuccessfully(pubId, targetId);
      } catch (e) {
        writeInConsole(e);
      }
    } else {
      publicationMovedInError(pubId, result);
    }
  });
}

function setDataInFolderDialog(name, desc) {
  $("#addOrUpdateNode #folderName").val(name.unescapeHTML());
  $("#addOrUpdateNode #folderDescription").val(desc.unescapeHTML());
}

function notifyOnFolder(componentId, folderId) {
  sp.messager.open(componentId, {nodeId: folderId});
}

