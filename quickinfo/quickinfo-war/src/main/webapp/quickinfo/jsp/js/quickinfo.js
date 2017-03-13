function confirmDelete(id, componentId, msg, onSuccess) {
  jQuery.popup.confirm(msg, function() {
    var params = {
      method : 'DELETE',
      url : webContext+"/services/news/"+componentId+"/"+id
    };
    silverpeasAjax(params).then(function(request) {
      var callback = window[onSuccess];
      callback.call(undefined, id);
    });
  });
}