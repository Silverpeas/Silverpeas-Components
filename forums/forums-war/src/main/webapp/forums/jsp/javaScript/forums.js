function resizeFrame()
{
  if (parent.resizeForum)
  {
    parent.resizeForum();
  }
}

function isEmpty(str) {
  return (!str || 0 === str.length);
}

function isBlank(str) {
  return (!str || /^\s*$/.test(str));
}

function isTextFilled()
{
  //var text = FCKeditorAPI.GetInstance("messageText").GetHTML();
  var text = $("#cke_messageText iframe").contents().find("body").text();
  if (!isEmpty(text) && !isBlank(text)) {
    //alert('text has been filled !!! text=' + text);
    return true;
  }
  //alert("text is empty !!!");
  return false;
}

function resetText()
{
  //FCKeditorAPI.GetInstance("messageText").SetHTML("");
  $("#cke_messageText iframe").contents().find("body").text("");
}

function notifyForumPopup(users)
{
  sp.messager.open(null, {recipientUsers: users, recipientEdition: false});
}