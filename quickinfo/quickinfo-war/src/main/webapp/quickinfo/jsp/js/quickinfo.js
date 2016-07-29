function confirmDelete(id, msg) {
  jQuery.popup.confirm(msg, function() {
    document.newsForm.action = "Remove";
    document.newsForm.Id.value = id;
    document.newsForm.submit();
  });
}