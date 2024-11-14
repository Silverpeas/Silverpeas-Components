function exportPublications() {
  const selectedIds = getSelectedPublicationIds();
  const notSelectedIds = getNotSelectedPublicationIds();
  const uri = "ExportPublications?SelectedIds=" + selectedIds + "&NotSelectedIds=" + notSelectedIds;
  SP_openWindow(uri, "Export", '720', '420', 'scrollbars=yes, resizable, alwaysRaised');
  $("input:checked[name=C1]").removeAttr('checked').hide();
}

function showPublicationOperations(item) {
  //$(item).find(".unit-operation").show();
  $(item).find(".add-to-basket-selection").show();
  $(item).find(".selection input").show();
  $(item).toggleClass("over-publication", true);
}

function hidePublicationOperations(item) {
  //$(item).find(".unit-operation").hide();
  $(item).find(".add-to-basket-selection").hide();
  const input = $(item).find(".selection input");
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
  let selectedIds = "";
  $("input:checked[name=C1]").each(function() {
    const id = $(this).val();
    selectedIds += id;
    selectedIds += ",";
  });
  if (selectedIds.length > 0) {
    selectedIds = selectedIds.substring(0, selectedIds.length - 1);
  }
  return selectedIds;
}

function getNotSelectedPublicationIds() {
  let notSelectedIds = "";
  $("input:not(:checked)[name=C1]").each(function() {
    const id = $(this).val();
    notSelectedIds += id;
    notSelectedIds += ",";
  });
  if (notSelectedIds.length > 0) {
    notSelectedIds = notSelectedIds.substring(0, notSelectedIds.length - 1);
  }
  return notSelectedIds;
}

function sendPubId() {
  //do nothing
}

function startsWith(haystack, needle) {
  return haystack.substr(0, needle.length) === needle ? true : false;
}

function deletePublications() {
  let confirm = getString('kmelia.publications.trash.confirm');
  if (getCurrentNodeId() === "1") {
    confirm = getString('kmelia.publications.delete.confirm');
  }
  jQuery.popup.confirm(confirm, function() {
    const componentId = getComponentId();
    const selectedPublicationIds = getSelectedPublicationIds();
    const notSelectedPublicationIds = getNotSelectedPublicationIds();
    const url = getWebContext() + '/KmeliaAJAXServlet';
    $.post(url, {SelectedIds: selectedPublicationIds, NotSelectedIds: notSelectedPublicationIds, ComponentId: componentId, Action: 'DeletePublications'},
    function(data) {
      if (startsWith(data, "ok")) {
        // fires event
        try {
          const nb = data.substring(3);
          displayPublications(getCurrentNodeId());
          if (getCurrentNodeId() === "1") {
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
  });
}

function publicationsRemovedInError(data) {
  notyError(data);
}

function copyPublications() {
  const componentId = getComponentId();
  const selectedPublicationIds = getSelectedPublicationIds();
  const notSelectedPublicationIds = getNotSelectedPublicationIds();
  const url = getWebContext() + '/KmeliaAJAXServlet';
  $.post(url, {SelectedIds: selectedPublicationIds, NotSelectedIds: notSelectedPublicationIds, ComponentId: componentId, Action: 'CopyPublications'},
  function(data) {
    if (data === "ok") {
      // fires event
      // do nothing
    } else {
      notyError(data);
    }
  }, 'text');
}

function cutPublications() {
  const componentId = getComponentId();
  const selectedPublicationIds = getSelectedPublicationIds();
  const notSelectedPublicationIds = getNotSelectedPublicationIds();
  const url = getWebContext() + '/KmeliaAJAXServlet';
  $.post(url, {SelectedIds: selectedPublicationIds, NotSelectedIds: notSelectedPublicationIds, ComponentId: componentId, Action: 'CutPublications'},
  function(data) {
    if (data === "ok") {
      // fires event
      // do nothing
    } else {
      notyError(data);
    }
  }, 'text');
}

function putPublicationInBasket(contributionId) {
  const basketManager = new BasketManager();
  basketManager.putContributionInBasket(contributionId);
}

function putPublicationsInBasket() {
  let decodePubId = function(id) {
    let pubIds = id.split('-');
    return pubIds[1] + ':Publication:' + pubIds[0];
  };
  let selectedPublicationIds = getSelectedPublicationIds().split(',').map(decodePubId);
  let notSelectedPublicationIds = getNotSelectedPublicationIds().split(',').map(decodePubId);
  const basketManager = new BasketManager();
  basketManager.putContributionsInBasket(selectedPublicationIds, notSelectedPublicationIds);
}

function updatePublications() {
  const componentId = getComponentId();
  const selectedPublicationIds = getSelectedPublicationIds();
  const notSelectedPublicationIds = getNotSelectedPublicationIds();
  const url = getWebContext() + '/Rkmelia/' + componentId + '/ToUpdatePublications';
  const formRequest = sp.formRequest(url).byPostMethod();
  formRequest.withParam("SelectedIds", selectedPublicationIds);
  formRequest.withParam("NotSelectedIds", notSelectedPublicationIds);
  formRequest.submit();
}

function selectAllPublications(select) {
  const componentId = getComponentId();
  const url = getWebContext() + '/KmeliaAJAXServlet';
  $.post(url, {ComponentId: componentId, Action: 'SELECTALLPUBLICATIONS', Selected: select},
      function(data) {
        if (data === "ok") {
          // select (or unselect) all checkboxes
          $("#pubList .selection input:checkbox").prop("checked", select);

          // revert action in menu
          if (select) {
            showPublicationCheckedBoxes();
            $("#menuitem-selectAllPubs a").text(getString('kmelia.operation.publications.unselect'));
            $("#menuitem-selectAllPubs a").attr("href", "javascript:onclick=selectAllPublications(false)");
          } else {
            $("#menuitem-selectAllPubs a").text(getString('kmelia.operation.publications.select'));
            $("#menuitem-selectAllPubs a").attr("href", "javascript:onclick=selectAllPublications(true)");
          }
        } else {
          notyError(data);
        }
      }, 'text');
}

(function($) {
  window.kmeliaWebService = new function() {
    const __serviceUrl = webContext + '/KmeliaAJAXServlet';
    const __computeRequestParams = function(action, params) {
      const componentId = getComponentId();
      if (!componentId) {
        throw new Error("component id must exist")
      }
      return extendsObject({}, params, {Action : action, ComponentId : componentId});
    };
    const __getBySyncRequest = function(action, params) {
      let result = "";
      $.ajax({
        url : __serviceUrl,
        data : __computeRequestParams(action, params),
        type : 'GET',
        dataType : 'text',
        cache : false,
        async : false,
        success : function(data, status, jqXHR) {
          result = data;
        },
        error : function(jqXHR, textStatus, errorThrown) {
          sp.log.error(errorThrown);
        }
      });
      return result;
    };
    const __ajaxRequest = function(action, params) {
      return sp.ajaxRequest(__serviceUrl).withParams(__computeRequestParams(action, params));
    };
    const __asText = function(request) {
      return request.responseText;
    };

    /**
     * Gets the clipboard state about kmelia instances.
     * @returns {*}
     */
    this.getClipboardState = function() {
      return __ajaxRequest('GetClipboardState').send().then(__asText);
    };

    /**
     * Gets the user profile about a folder represented by the given identifier.
     * @param folderId identifier of a folder.
     * @returns {string}
     */
    this.getUserProfileSynchronously = function(folderId) {
      return __getBySyncRequest('GetProfile', {Id : folderId});
    };

    /**
     * Gets the authorizations about the manipulation of a publication represented by the given
     * identifier. An optional identifier of topic can be found if the verification is about an
     * other folder than the current one.
     * @param pubId identifier of a publication.
     * @param topicId (optional) an identifier of folder. If none, the current folder os taken into
     *     account by services.
     * @returns {any}
     */
    this.getPublicationUserAuthorizationsSynchronously = function(pubId, topicId) {
      const _params = {pubId : pubId, nodeId : topicId};
      const authorizations = __getBySyncRequest('GetPublicationAuthorizations', _params);
      return extendsObject({
        canBeCut : false,
        canBeDeleted : false
      }, JSON.parse(authorizations));
    };

    /**
     * Moves the publication represented by the given identifier from a folder to another one.
     * Some additional parameters can be given to services in order to set validators, state, etc.
     * @param pubId the identifier of the publication to move.
     * @param sourceNodeId the identifier of the source folder.
     * @param targetNodeId the identifier of the target folder.
     * @param extraParams additional parameters.
     * @returns {*}
     */
    this.movePublication = function(pubId, sourceNodeId, targetNodeId, extraParams) {
      const _params = extendsObject({}, extraParams, {
        Id : pubId, SourceNodeId : sourceNodeId, TargetNodeId : targetNodeId
      });
      return __ajaxRequest('MovePublication', _params).byPostMethod().send().then(__asText);
    };

    /**
     * Pastes publication copies and/or cuts into folder represented by given identifier.
     * Some additional parameters can be given to services in order to set validators, state, etc.
     * @param folderId identifier of a target folder.
     * @param extraParams additional parameters.
     * @returns {*}
     */
    this.pastePublications = function(folderId, extraParams) {
      const _params = extendsObject({}, extraParams, {Id : folderId});
      return __ajaxRequest('Paste', _params).byPostMethod().send().then(__asText);
    };
  };
})(jQuery);
