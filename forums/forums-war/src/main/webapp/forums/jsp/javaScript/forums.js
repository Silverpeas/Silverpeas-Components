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

var NOTATIONS_COUNT = 5;
var NOTATION_PREFIX = "notationImg";
var notationFlags = new Array();
var readOnly = true;
var currentNote = -1;
var userNote = -1;

function notationOver(image)
{
  var limit = getNotationIndex(image);
  notationFlags[limit - 1] = true;
  var i;
  for (i = 1; i <= NOTATIONS_COUNT; i++) {
    document.getElementById(NOTATION_PREFIX + i).className = "notation_" + (i <= limit ? "on" : "off");
  }
}

function notationOut(image)
{
  var limit = getNotationIndex(image);
  notationFlags[limit - 1] = false;
  var i;
  limit = (inOnNotation() ? limit : currentNote);
  for (i = 1; i <= NOTATIONS_COUNT; i++) {
    document.getElementById(NOTATION_PREFIX + i).className = "notation_" + (i <= limit ? "on" : "off");
  }
}

function inOnNotation()
{
  var i;
  for (i = 0; i < NOTATIONS_COUNT; i++) {
    if (notationFlags[i]) {
      return true;
    }
  }
  return false;
}

function getNotationIndex(image)
{
  return parseInt(image.id.substring(NOTATION_PREFIX.length));
}

function notifyPopup2(context, compoId, users, groups)
{
  SP_openWindow(context + "/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=" + compoId
    + "&theTargetsUsers=" + users + "&theTargetsGroups=" + groups,
    "notifyUserPopup", "700", "400", "menubar=no,scrollbars=no,statusbar=no");
}