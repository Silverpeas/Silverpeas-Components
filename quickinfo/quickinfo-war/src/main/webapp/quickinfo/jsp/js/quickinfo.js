function confirmDelete(id, componentId, msg, onSuccess) {
  jQuery.popup.confirm(msg, function() {
    var params = {
      method : 'DELETE',
      url : webContext+"/services/news/"+componentId+"/"+id
    };
    silverpeasAjax(params).then(function(request) {
      if (typeof onSuccess === 'function') {
        onSuccess.call(undefined, id);
      }
    });
  });
}