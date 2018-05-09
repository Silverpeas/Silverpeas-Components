var currentMessageId = -1;
var parentMessageId = -1;

function replyMessage(messageId) {
  currentMessageId = messageId;
  $('div[id^="msgContent"]').hide();
  var $msgContent = $('#msgContent' + messageId).show();
  $('input[name="parentId"]').val(messageId);
  $('input[name="messageTitle"]').val('Re : ' + $msgContent.find('.txtnav').html());
  var $responseTable = $('#responseTable').show();
  if ($responseTable.length > 0) {
    initCKeditor(messageId);
  }
  $('#backButton').hide();
  scrollMessageList(messageId);
  document.location.href = "#msg" + messageId;
  callResizeFrame();
}

function cancelMessage() {
  sp.editor.wysiwyg.lastBackupManager.clear();
  var messageId = currentMessageId;
  currentMessageId = -1;
  $('div[id^="msgContent"]').show();
  var $responseTable = $('#responseTable').hide();
  if ($responseTable.length > 0) {
    resetText();
  }
  $('#backButton').show();
  scrollMessage(messageId);
  callResizeFrame();
}

function scrollMessageList(messageId, noMsgDivScroll) {

  // CSS
  $('[id^="msgLine"]').removeClass('msgLine');
  var msgContents = $('[id^="msgContent"]');
  msgContents.removeClass('msgContent');
  $('#msgLine' + parentMessageId).addClass('msgLine');
  var $msgLine = $('#msgLine' + messageId).addClass('msgLine');
  var $msgContent = $('#msgContent' + messageId);
  if (msgContents.length > 1) {
    $msgContent.addClass('msgContent');
  }

  // Focus
  if (!noMsgDivScroll) {
    scrollToItem($msgLine, $('#msgDiv')[0]);
  }
  scrollToItem($msgContent, document.body);
}

function scrollToItem($item, referenceItem) {
  if (!sp.element.isInView($item, true, referenceItem)) {
    sp.element.scrollTo($item, referenceItem);
  }
}

function scrollMessage(messageId) {
  if (currentMessageId != -1) {
    cancelMessage();
  }
  scrollMessageList(messageId, true);
}

function scrollTop() {
  sp.element.setScrollTo(0, document.body);
}