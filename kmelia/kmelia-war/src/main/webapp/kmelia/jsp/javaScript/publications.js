function exportPublications() {
  var selectedIds = getSelectedPublicationIds();
  var notSelectedIds = getNotSelectedPublicationIds();
  var uri = "ExportPublications?SelectedIds=" + selectedIds + "&NotSelectedIds=" + notSelectedIds;
  SP_openWindow(uri, "Export", '600', '300', 'scrollbars=yes, resizable, alwaysRaised');
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
    selectedIds = selectedIds.substring(0, selectedIds.length - 1);
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
  var confirm = getString('kmelia.publications.trash.confirm');
  if (getCurrentNodeId() === "1") {
    confirm = getString('kmelia.publications.delete.confirm');
  }
  jQuery.popup.confirm(confirm, function() {
    var componentId = getComponentId();
    var selectedPublicationIds = getSelectedPublicationIds();
    var notSelectedPublicationIds = getNotSelectedPublicationIds();
    var url = getWebContext() + '/KmeliaAJAXServlet';
    $.post(url, {SelectedIds: selectedPublicationIds, NotSelectedIds: notSelectedPublicationIds, ComponentId: componentId, Action: 'DeletePublications'},
    function(data) {
      if (startsWith(data, "ok")) {
        // fires event
        try {
          var nb = data.substring(3);
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
  var componentId = getComponentId();
  var selectedPublicationIds = getSelectedPublicationIds();
  var notSelectedPublicationIds = getNotSelectedPublicationIds();
  var url = getWebContext() + '/KmeliaAJAXServlet';
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
  var componentId = getComponentId();
  var selectedPublicationIds = getSelectedPublicationIds();
  var notSelectedPublicationIds = getNotSelectedPublicationIds();
  var url = getWebContext() + '/KmeliaAJAXServlet';
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

function putPublicationsInBasket() {
  let selectedPublicationIds = getSelectedPublicationIds();
  const isPublicationToAdd = selectedPublicationIds.trim().length > 0;
  let notSelectedPublicationIds = getNotSelectedPublicationIds();
  let decodePubId = function(id) {
    let pubIds = id.split('-');
    return pubIds[1] + ':Publication:' + pubIds[0];
  };
  let basket = new BasketService();
  let deletePromises = [];

  // remove from the basket the unselected publications
  if (notSelectedPublicationIds.trim().length > 0) {
    let arrayOfNonSelectedPubIds = notSelectedPublicationIds.split(',');
    deletePromises.push(basket.getBasketSelectionElements(BasketService.Context.transfert).then(function(elts) {
      const entriesToDelete = elts.filter(function(elt) {
        return arrayOfNonSelectedPubIds.filter(function(id) {
          return elt.getId() === decodePubId(id.trim());
        }).length > 0;
      })
      .map(function(elt) {
        return {
          item : {
            id : elt.getId()
          }
        }
      });
      return basket.deleteEntries(entriesToDelete, isPublicationToAdd);
    }));
  }

  // put into the basket the selected publications
  if(isPublicationToAdd) {
    const entriesToAdd = selectedPublicationIds.split(',').map(function(id) {
      let pubId = decodePubId(id.trim());
      return {
        context : {
          reason : BasketService.Context.transfert
        }, item : {
          id : pubId, type : 'Publication'
        }
      };
    });
    sp.promise.whenAllResolved(deletePromises).then(function() {
      basket.putNewEntries(entriesToAdd);
    })
  }
}

function updatePublications() {
  var componentId = getComponentId();
  var selectedPublicationIds = getSelectedPublicationIds();
  var notSelectedPublicationIds = getNotSelectedPublicationIds();
  var url = getWebContext() + '/Rkmelia/' + componentId + '/ToUpdatePublications';
  var formRequest = sp.formRequest(url).byPostMethod();
  formRequest.withParam("SelectedIds", selectedPublicationIds);
  formRequest.withParam("NotSelectedIds", notSelectedPublicationIds);
  formRequest.submit();
}

function selectAllPublications(select) {
  var componentId = getComponentId();
  var url = getWebContext() + '/KmeliaAJAXServlet';
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
    var __serviceUrl = webContext + '/KmeliaAJAXServlet';
    var __computeRequestParams = function(action, params) {
      var componentId = getComponentId();
      if (!componentId) {
        throw new Error("component id must exist")
      }
      return extendsObject({}, params, {Action : action, ComponentId : componentId});
    };
    var __getBySyncRequest = function(action, params) {
      var result = "";
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
    var __ajaxRequest = function(action, params) {
      return sp.ajaxRequest(__serviceUrl).withParams(__computeRequestParams(action, params));
    };
    var __asText = function(request) {
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
      var _params = {pubId : pubId, nodeId : topicId};
      var authorizations = __getBySyncRequest('GetPublicationAuthorizations', _params);
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
      var _params = extendsObject({}, extraParams, {
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
      var _params = extendsObject({}, extraParams, {Id : folderId});
      return __ajaxRequest('Paste', _params).byPostMethod().send().then(__asText);
    };
  };
})(jQuery);
