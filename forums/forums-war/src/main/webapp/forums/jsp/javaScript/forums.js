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

function notifyPopup2(context, compoId, users, groups)
{
  SP_openWindow(context + "/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=" + compoId
    + "&theTargetsUsers=" + users + "&theTargetsGroups=" + groups,
    "notifyUserPopup", "700", "400", "menubar=no,scrollbars=no,statusbar=no");
}