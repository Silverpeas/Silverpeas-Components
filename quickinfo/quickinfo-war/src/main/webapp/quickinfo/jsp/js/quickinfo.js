function confirmDelete(id, msg) {
  if (window.confirm(msg)) {
    document.newsForm.action = "Remove";
    document.newsForm.Id.value = id;
    document.newsForm.submit();
  }
}