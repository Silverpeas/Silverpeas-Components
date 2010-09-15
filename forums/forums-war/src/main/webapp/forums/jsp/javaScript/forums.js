function resizeFrame()
{
  if (parent.resizeForum)
  {
    parent.resizeForum();
  }
}

function isTextFilled()
{
  var text = FCKeditorAPI.GetInstance("messageText").GetHTML();
  var start = 0
  var end = text.indexOf("<");
  var s;
  var index;
  while (start != -1 && end != -1)
  {
    if (end > start)
    {
      s = text.substring(start + 1, end);
      index = s.indexOf(" ");
      while (index != -1)
      {
        s = s.substring(0, index) + s.substring(index + 1);
        index = s.indexOf(" ");
      }
      index = s.indexOf("&nbsp;");
      while (index != -1)
      {
        s = s.substring(0, index) + s.substring(index + 6);
        index = s.indexOf("&nbsp;");
      }
      if (s != "")
      {
        return true;
      }
    }
    start = text.indexOf(">", end);
    end = text.indexOf("<", start);
  }
  return false;
}

function resetText()
{
  FCKeditorAPI.GetInstance("messageText").SetHTML("");
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