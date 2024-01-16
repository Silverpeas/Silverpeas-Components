<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkQuestionReply.jsp" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:setLocale value="{sessionScope.SilverSessionController.favoriteLanguage}" />

<%
	// recuperation des parametres
	String		profil		= (String) request.getAttribute("Flag");
	boolean		pdcUsed		= (Boolean) request.getAttribute("PDCUsed");

	String attachmentsProfile = profil;
	if ("publisher".equals(profil)) {
	  // This is done to avoid operations on files to unauthorized profiles
    attachmentsProfile = "reader";
  }
%>

<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.questionReply">
<head>
  <title><fmt:message key="GML.popupTitle"/></title>
  <view:looknfeel />
  <view:includePlugin name="preview" />
  <view:includePlugin name="toggle"/>
  <view:includePlugin name="subscription"/>
<script type="text/javascript">
  <!--

var $firstCategoryClickEventProcessedPromise;
var searchScope = false;
var categoryIdsToDisplay = [];

var etat = new Array();
function bindQuestionsEvent() {
  var $questions = $('.questionTitle');
  $questions.off('click');
  $questions.on('click', function(event) {
    question = this.id;
    id = question.substring(1);
    answersUrl = '<c:url value="/services/questionreply/${pageScope.componentId}/replies/question/"/>' + id;
    typeLien = question.substring(0,1);
    if (typeLien!="l" && !$(event.target).hasClass('actionQuestion')) {
      $('#' + this.id + ' .answers').hide();
      if(etat[id] != "open"){
        $('#a'+id).show();
        etat[id] = "open";
        var found = $('#a'+id + '>ul>li');
        if (found.length == 0) {
          $.ajax({
            url: answersUrl,
            type: "GET",
            contentType: "application/json",
            dataType: "json",
            cache: false,
            success: function(data) {
              $('#a'+id + ' > ul').html('');
              $.each(data, function(key, answer) {
                $('#a'+ id + ' > ul').append(displayAnswer(answer));
              });
              $('html, body').animate({scrollTop:$("#q" + id).parent().offset().top}, 500);
            }
          });
        }
      } else {
        $('#a'+id).hide();
        etat[id] = "close";
      }
      return false;
    }
  });
}

function bindCategoryEvent() {
   $('.categoryTitle').on('click', function() {
      category = this.id;
      id = category.substring(1);
      if ($(this).hasClass('category-open')) {
        $(this).removeClass('category-open').addClass('category-closed');
        $("#qc"+id).hide();
      } else {
        $(this).removeClass('category-closed').addClass('category-open');
        openCategory(id);
      }
      return false;
    });
}

function openCategory(id, exclusive) {
  if (id.length > 0) {
    questionUrl = '<c:url value="/services/questionreply/${pageScope.componentId}/questions/category/"/>' + id;

    $('.category').removeClass('select');
    var $internalPromise = $.Deferred();
    var found = $('#qc'+id + '>li');
    if (found.length == 0 && !searchScope) {
      // This category has not been loaded yet
      $.ajax({
        url: questionUrl,
        type: "GET",
        contentType: "application/json",
        dataType: "json",
        cache: false,
        success: function(data) {
          $('#qc'+id).html('');
          if (data.length > 0) {
            $.each(data, function(key, question) {
              $('#qc' + id).append(displayQuestion(question));
            });
          }
          $internalPromise.resolve(true);
        }
      });
    } else {
      //$('#qc' + id).append("<div class=\"inlineMessage\">No result for this category !</div>");
      $internalPromise.resolve(false);
    }
    $internalPromise.then(function(isQuestionBindingRequired) {
      $('#qc' + id + " .inlineMessage.no-result").remove();
      var found = $('#qc'+id + '>li');
      if (found.length == 0 && !searchScope) {
        $('#qc' + id).append("<div class=\"inlineMessage empty-category\"><fmt:message key="questionReply.category.empty"/></div>");
      } else {
        // search case
        var allHidden = true;
        $.each(found, function(key, li) {
          allHidden = allHidden && $(li).css("display") === "none";
        });
        if (allHidden) {
          $('#qc' + id).append("<div class=\"inlineMessage no-result\"><fmt:message key="questionReply.category.noResult"/></div>");
        }
      }
      $('#qc' + id).show();
      $(this).parent().addClass('select');
      if (isQuestionBindingRequired) {
        bindQuestionsEvent();
      }
      $firstCategoryClickEventProcessedPromise.resolve();
    });
  }

  if (exclusive) {
    // hide all other categories
    $(".category").each(function() {
      var cId = $(this).attr('id');
      if (cId !== "category-"+id) {
        $(this).hide();
      } else {
        $(this).show();
      }
    });

    $("#filter a").removeClass('active');
    $("#filter a#"+id).addClass('active');
  }
}

$(document).ready(function() {
  $firstCategoryClickEventProcessedPromise = $.Deferred();
  bindCategoryEvent();
  bindQuestionsEvent();
  $('.questions').hide();
  <c:choose>
    <c:when test="${param.categoryId != null}">
      $("#qc<c:out value="${param.categoryId}"/>").show();
      $("#c<c:out value="${param.categoryId}"/>").parent().addClass('select');
      $("#c<c:out value="${param.categoryId}"/>").trigger($.Event("click"));
    </c:when>
    <c:otherwise>
      $("ul li:first-child .questions").show();
      $("ul li:first-child").addClass('select');
      $("ul li:first-child .categoryTitle").trigger($.Event("click"));
    </c:otherwise>
  </c:choose>
  <c:if test="${param.questionId != null}">
    $firstCategoryClickEventProcessedPromise.then(function(){
      $('#q<c:out value="${param.questionId}"/>').trigger($.Event('click'));
    });
  </c:if>

  $('.category').hover(function() {
      $(this).addClass('hover');
    }, function() {
      $(this).removeClass('hover');
    }
  );

});
  <fmt:message key="questionReply.link" bundle="${icons}" var="hyperlinkIcon"/>
  <fmt:message key="questionReply.open" bundle="${icons}" var="openIcon"/>
  <fmt:message key="questionReply.update" bundle="${icons}" var="updateIcon"/>
  <fmt:message key="questionReply.delete" bundle="${icons}" var="deleteIcon"/>
  <fmt:message key="questionReply.encours" bundle="${icons}" var="newIcon"/>
  <fmt:message key="questionReply.waiting" bundle="${icons}" var="waitingIcon"/>
  <fmt:message key="questionReply.close" bundle="${icons}" var="closeIcon"/>
  <fmt:message key="questionReply.miniconeReponse" bundle="${icons}" var="addReplyIcon"/>
  function displayQuestion(questionToBeDisplayed) {
    var questionId = questionToBeDisplayed.id;
    questionDiv = $('<div>').addClass('question');

    answersDiv = $('<div>').addClass('answers').attr('id', 'a' + questionId);
    if (StringUtil.isDefined(questionToBeDisplayed.content)) {
      answersDiv.append($('<p>').text(questionToBeDisplayed.content));
    }
    answersDiv.append($('<ul>'));
    answersDiv.hide();

    var li = $('<li>').attr('id', 'question-' + questionId).addClass('questionLI');
    li.append(questionDiv);
    li.append(answersDiv);

    questionTitleDiv = $('<div>').attr('id', 'q' + questionId).addClass('questionTitle');
    questionTitle = $('<h4>');
    questionTitleLink = $('<a>').addClass('question').attr('id', 'l' + questionId).attr('href', '#'+questionId).attr('title', '<fmt:message key="questionReply.open"/>').text(questionToBeDisplayed.title);
    questionTitle.append(questionTitleLink);
    questionTitleDiv.append(questionTitle);
    questionHyperlink = $('<a>').addClass('permalink').addClass('sp-permalink').attr('href', '<c:url value="/Question/" />' + questionId).attr('title', '<fmt:message key="questionReply.CopyQuestionLink"/>');
    hyperlinkImg = $('<img>').attr('src', '<c:url value="${hyperlinkIcon}"/>').attr('alt', '<fmt:message key="questionReply.CopyQuestionLink"/>').attr('border', '0');
    questionHyperlink.append(hyperlinkImg);
    questionTitleDiv.append(questionHyperlink);
    switch(questionToBeDisplayed.status) {
      case 0 :
        questionStatusImg = $('<img>').addClass('status').attr('alt',  '<fmt:message key="questionReply.encours" />').attr('title',  '<fmt:message key="questionReply.encours" />').attr('src', '<c:url value="${newIcon}" />').attr('border', '0' );
        questionTitleDiv.append(questionStatusImg);
        break;
      case 1 :
        questionStatusImg = $('<img>').addClass('status').attr('alt',  '<fmt:message key="questionReply.waiting" />').attr('title',  '<fmt:message key="questionReply.waiting" />').attr('src', '<c:url value="${waitingIcon}" />').attr('border', '0' );
        questionTitleDiv.append(questionStatusImg);
        break;
      case 2 :
        questionStatusImg = $('<img>').addClass('status').attr('alt',  '<fmt:message key="questionReply.close" />').attr('title',  '<fmt:message key="questionReply.close" />').attr('src', '<c:url value="${closeIcon}" />').attr('border', '0' );
        questionTitleDiv.append(questionStatusImg);
        break;
    }

    questionAuthor = $('<span>').addClass('questionAuthor').addClass('txtBaseline').text(questionToBeDisplayed.creator.fullName + ' ');
    questionCreationDate = $('<span>').addClass('questionDate').text('- ' + questionToBeDisplayed.creationDate);
    questionAuthor.append(questionCreationDate);
    questionTitleDiv.append(questionAuthor);
    questionDiv.append(questionTitleDiv);
    actionDiv = $('<div>').addClass('action');
    if(questionToBeDisplayed.replyable){
      replyQuestionLink = $('<a>').addClass('reply').attr('title', '<fmt:message key="questionReply.ajoutR"/>').attr('href', 'CreateRQuery?QuestionId=' + questionId);
      replyQuestionImg = $('<img>').addClass('actionQuestion').attr('alt', '<fmt:message key="questionReply.ajoutR"/>').attr('src', '<c:url value="${addReplyIcon}" />' ).attr('border', '0' );
      replyQuestionLink.append(replyQuestionImg);
      actionDiv.append(replyQuestionLink);
    }
    if(questionToBeDisplayed.reopenable){
      reopenQuestionLink = $('<a>').addClass('open').attr('title', '<fmt:message key="questionReply.open"/>').attr('href', 'javascript:openQ(\'' + questionId + '\')');
      reopenQuestionImg = $('<img>').addClass('actionQuestion').attr('alt', '<fmt:message key="questionReply.open"/>').attr('src', '<c:url value="${openIcon}" />' ).attr('border', '0' );
      reopenQuestionLink.append(reopenQuestionImg);
      actionDiv.append(reopenQuestionLink);
    }
    if(questionToBeDisplayed.updatable){
      updateQuestionLink = $('<a>').addClass('update').attr('title', '<fmt:message key="questionReply.modifQ"/>').attr('href', 'UpdateQ?QuestionId=' + questionId);
      updateQuestionImg = $('<img>').addClass('actionQuestion').attr('alt', '<fmt:message key="questionReply.modifQ"/>').attr('src', '<c:url value="${updateIcon}" />' ).attr('border', '0' );
      updateQuestionLink.append(updateQuestionImg);
      actionDiv.append(updateQuestionLink);

      deleteQuestionLink = $('<a>').addClass('delete').attr('title', '<fmt:message key="questionReply.delQ"/>').attr('href', 'javascript:deleteConfirm(\'' + questionId + '\')');
      deleteQuestionImg = $('<img>').attr('alt', '<fmt:message key="questionReply.delQ"/>').attr('src', '<c:url value="${deleteIcon}" />' ).attr('border', '0' );
      deleteQuestionLink.append(deleteQuestionImg);
      actionDiv.append(deleteQuestionLink);
    }
    <c:if test="${'user' != requestScope.Flag}">
      actionDiv.append($('<input>').addClass('checkbox').attr('name', 'checkedQuestion').attr('value', questionId).attr('type', 'checkbox').prop('value', questionId));
      actionDiv.append($('<input>').attr('name', 'status').attr('value', questionToBeDisplayed.status).attr('type', 'hidden'));
      questionDiv.append(actionDiv);
    </c:if>
    return li;
  }

  <fmt:message key="questionReply.minicone" bundle="${icons}" var="publicAnswerIcon"/>
  <fmt:message key="questionReply.miniconeReponse" bundle="${icons}" var="privateAnswerIcon"/>
  function displayAnswer(answer) {
    answerBlock = $('<li>').addClass('answer');
    answerTitle = $('<h5>').addClass('answerTitle').text(answer.title);
    if(answer.publicReply) {
      answerTitle.append($('<img>').addClass('status').attr('alt','<fmt:message key="questionReply.Rpublique" />').attr('title','<fmt:message key="questionReply.Rpublique" />').attr('src', '<c:url value="${publicAnswerIcon}" />').attr('border', '0' ));
    } else {
      answerTitle.append($('<img>').addClass('status').attr('alt','<fmt:message key="questionReply.Rprivee" />').attr('title','<fmt:message key="questionReply.Rprivee" />').attr('src', '<c:url value="${privateAnswerIcon}" />').attr('border', '0' ));
    }
    actionDiv = $('<div>').addClass('action');
    if(!answer.readOnly){
      updateAnswerLink = $('<a>').attr('title', '<fmt:message key="questionReply.modifR" />').attr('href', 'UpdateR?replyId=' + answer.id + '&QuestionId=' + answer.questionId);
      updateAnswerImg = $('<img>').attr('alt', '<fmt:message key="questionReply.modifR" />').attr('src', '<c:url value="${updateIcon}" />').attr('border', '0' );
      updateAnswerLink.append(updateAnswerImg);
      actionDiv.append(updateAnswerLink);

      deleteAnswerLink = $('<a>').attr('title', '<fmt:message key="questionReply.delR" />').attr('href', 'javascript:deleteConfirmR(\'' + answer.id + '\', \'' + answer.questionId + '\')');
      deleteAnswerImg = $('<img>').attr('alt', '<fmt:message key="questionReply.delR" />').attr('src', '<c:url value="${deleteIcon}" />').attr('border', '0' );
      deleteAnswerLink.append(deleteAnswerImg);
      actionDiv.append(deleteAnswerLink);
    }
    answerTitle.append(actionDiv);
    answerBlock.append(answerTitle);
    answerContentDiv = $('<div>').addClass('answerContent');
    answerAttachmentDiv = $('<div>').addClass('answerAttachment');
    var hasAttachments = answer.attachments != null && answer.attachments.length > 0;
    if(hasAttachments) {
      answerAttachmentDiv.load('<c:url value="/attachment/jsp/displayAttachedFiles.jsp?Context=attachment&ComponentId=${pageScope.componentId}" />&Profile=<%=attachmentsProfile%>&Id=' + answer.id);
      answerContentDiv.append(answerAttachmentDiv);
    }
    answerContentDiv.append(answer.content);
    if (hasAttachments) {
      displayAttachmentsAsContent(answerContentDiv, answer.id, 'Answer');
    }
    answerContentDiv.append("<br clear=\"right\"/>");
    answerBlock.append(answerContentDiv);
    answerAuthorBlock = $('<span>').addClass('answerAuthor txtBaseline').text(answer.creatorName);
    answerDateBlock = $('<span>').addClass('answerDate').text(' - ' + answer.creationDate);
    answerAuthorBlock.append(answerDateBlock);
    answerBlock.append(answerAuthorBlock);
    return answerBlock;
  }

  function displayAttachmentsAsContent($rootContainer, resourceId, resourceType) {
      new AttachmentsAsContentViewer({
        parentContainer : $rootContainer,
        highestUserRole : '<%=attachmentsProfile%>',
        componentInstanceId : '${pageScope.componentId}',
        resourceId : resourceId,
        resourceType : resourceType
      });
  }
-->
</script>
<script type="text/javascript">
function openSPWindow(fonction, windowName) {
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

// supprimer une question
function deleteConfirm(id) {
	//confirmation de suppression de la question
  var label = "<fmt:message key="MessageSuppressionQ" />";
  jQuery.popup.confirm(label, function() {
    document.QForm.action = "DeleteQ";
    document.QForm.Id.value = id;
    document.QForm.submit();
	});
}

// clore une question
function closeQ(id) {
	//confirmation de cloture de la question
  var label = "<fmt:message key="MessageCloseQ" />";
  jQuery.popup.confirm(label, function() {
    document.QForm.action = "CloseQ";
    document.QForm.Id.value = id;
    document.QForm.submit();
	});
}

//reouvrir une question
function openQ(id) {
	//confirmation de l'ouverture de la question
  var label = "<fmt:message key="MessageOpenQ" />";
  jQuery.popup.confirm(label, function() {
    document.QForm.action = "OpenQ";
    document.QForm.Id.value = id;
    document.QForm.submit();
	});
}

// supprimer toutes les questions selectionnees
function DeletesAdmin() {
	if (existSelected()) {
		if (existStatusError('2', '0')) {
			notyError("<%=resource.getString("questionReply.delStatusErr")%>");
    } else {
      var label = "<fmt:message key="MessageSuppressionsQ" />";
      jQuery.popup.confirm(label, function() {
				document.mainForm.action = "<%=routerUrl%>DeleteQuestions";
				document.mainForm.submit();
			});
		}
	}
}

// clore toutes les questions selectionnees
function Closes() {
	if (existSelected()) {
		if (existStatusError('1')) {
			notyError("<%=resource.getString("questionReply.closeStatusErr")%>");
    } else {
      var label = "<fmt:message key="MessageClosesQ" />";
      jQuery.popup.confirm(label, function() {
				document.mainForm.action = "<%=routerUrl%>CloseQuestions";
				document.mainForm.submit();
			});
		}
	}
}

// controler si toutes les cases cochees sont valides pour l'operation demandee
function existStatusError(status) {
	var err = false;
	if (document.mainForm.status != null) {
		if (document.mainForm.status.length != null) {
			var i = 0;
			while (i < document.mainForm.status.length) {
        var statusQ = document.mainForm.status[i].value;
        if ((document.mainForm.checkedQuestion[i] != null)&&(document.mainForm.checkedQuestion[i].checked)) {
          if (statusQ != status) {
						err = true;
						document.mainForm.checkedQuestion[i].checked = false;
					}
        }
				i++;
			}
		}
	}
	return err;
}

function existStatusError(status1, status2) {
	var err = false;
	if (document.mainForm.status != null) {
		if (document.mainForm.status.length != null) {
			var i = 0;
			while (i < document.mainForm.status.length) {
        var statusQ = document.mainForm.status[i].value;
        if ((document.mainForm.checkedQuestion[i] != null)&&(document.mainForm.checkedQuestion[i].checked)) {
					if (statusQ != status1 && statusQ != status2) {
						err = true;
						document.mainForm.checkedQuestion[i].checked = false;
					}
        }
				i++;
			}
		}
	}
	return err;
}

// supprimer une reponse
function deleteConfirmR(replyId, questionId) {
  //confirmation de suppression de la question
  var label = "<fmt:message key="MessageSuppressionR" />";
  jQuery.popup.confirm(label, function() {
    document.RForm.action = "DeleteR";
    document.RForm.replyId.value = replyId;
    document.RForm.QuestionId.value = questionId;
    document.RForm.submit();
  });
}

function confirmDeleteCategory(categoryId) {
  var label = "<fmt:message key="questionReply.confirmDeleteCategory" />";
  jQuery.popup.confirm(label, function() {
		window.location.href=("DeleteCategory?CategoryId=" + categoryId + "");
	});
}

SUBSCRIPTION_PROMISE.then(function() {
  window.spSubManager = new SilverpeasSubscriptionManager('${pageScope.componentId}');
});

function addCategory() {
  document.categoryForm.action = "CreateCategory";
  // open modal dialog
  $('#addOrUpdateCategory').popup('validation', {
    title : '<fmt:message key="questionReply.createCategory"/>',
    width : '600px',
    callback : function() {
      ifCorrectFormExecute(function() {
        document.categoryForm.submit();
      });
      return true;
    }
  });
}

function updateCategory(id) {
  document.categoryForm.action = "UpdateCategory";
  document.categoryForm.CategoryId.value = id;
  $("#addOrUpdateCategory #categoryName").val($("#c"+id+" h3").text());
  $("#addOrUpdateCategory #categoryDescription").val($('#c'+id+" .categoryDescription").text());
  // open modal dialog
  $('#addOrUpdateCategory').popup('validation', {
    title : '<fmt:message key="questionReply.updateCategory"/>',
    width : '600px',
    callback : function() {
      ifCorrectFormExecute(function() {
        document.categoryForm.submit();
      });
      return true;
    }
  });
}

function ifCorrectFormExecute(callback) {
  var errorMsg = "";
  var errorNb = 0;
  var name = stripInitialWhitespace(document.categoryForm.Name.value);

  if (name == "") {
    errorMsg+="  - '<fmt:message key="GML.title"/>' <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }

  switch(errorNb) {
    case 0 :
      callback.call(this);
      break;
    case 1 :
      errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
}

function getComponentId() {
  return "${pageScope.componentId}";
}

function search() {
  var query = $('#query').val();
  if (query.length > 0) {
    var queryDescription = {
      appId : getComponentId(),
      query : query
    };

    sp.search.on(queryDescription).then(function(results) {
      // limit results only to 'Question' (main entry)
      var questionIds = [];
      results.forEach(function(result) {
        if (result.type === "Question") {
          questionIds.push(result.id);
        }
      });
      displaySearchResults(questionIds);
    });
  } else {
    location.href = "Main";
  }
}

function displaySearchResults(resultQuestionIds) {
  searchScope = true;
  categoryIdsToDisplay = [];
  $("#filter a").removeClass("active");
  $("#filter-all").addClass("active");
  $(".inlineMessage.no-result").remove();
  if (resultQuestionIds.length == 0) {
    $(".category").hide();
    $("#noResult").show();
  } else if (resultQuestionIds.length > 0) {
    $("#noResult").hide();

    var $internalPromise = $.Deferred();

    // hide displayed questions which are not part of results
    $(".questionLI").each(function() {
      aQuestionId = this.id.substring("question-".length);
      index = resultQuestionIds.indexOf(aQuestionId);
      if (index == -1) {
        $(this).hide();
      } else {
        $(this).show();
        $(this).parents('li.category').show();
        var categId = $(this).parents('li.category').attr('id');
        categoryIdsToDisplay.push(categId);
        resultQuestionIds.splice(index, 1);
      }
    });

    // display questions
    if (resultQuestionIds.length > 0) {
      var param = "";
      $.each(resultQuestionIds, function(i, questionId) {
        if (i != 0) {
          param += "&";
        }
        param += "ids="+questionId;
      });

      questionUrl = '<c:url value="/services/questionreply/${pageScope.componentId}/questions?"/>' + param;
      $.ajax({
        url: questionUrl,
        type: "GET",
        contentType: "application/json",
        dataType: "json",
        cache: false,
        success: function(questions) {
          $.each(questions, function(key, question) {
            if (question.categoryId) {
              categoryId = question.categoryId;
            } else {
              categoryId = "null";
            }
            categoryIdsToDisplay.push("category-"+categoryId);
            $('#qc' + categoryId).show();
            $('#qc' + categoryId).append(displayQuestion(question));
          });
          $internalPromise.resolve();
        }
      });
    } else {
      $internalPromise.resolve();
    }

    $internalPromise.then(function() {
      bindQuestionsEvent();
      showRelevantCategories();
    });
  }
}

// hide empty categories, show the other ones
function showRelevantCategories() {
  $(".category").each(function() {
    var cId = $(this).attr('id');
    if (categoryIdsToDisplay.indexOf(cId) == -1) {
      $(this).hide();
    } else {
      $(this).show();
    }
  });
}

function filterAll() {
  if (searchScope) {
    showRelevantCategories();
  } else {
    location.href = "Main";
  }
}
</script>
</head>
<body class="<%=profil%>">
<%
  if (profil.equals("admin")) {
    if (pdcUsed) {
	    // gestion du plan de classement
	    operationPane.addOperation(resource.getIcon("questionReply.pdcUtilizationSrc"), resource.
	            getString("GML.PDCParam"),
	            "javascript:onClick=openSPWindow('" + m_context + "/RpdcUtilization/jsp/Main?ComponentId=" + componentId + "','utilizationPdc1')");
	    operationPane.addLine();
    }
    // creation des categories
    operationPane.addOperationOfCreation(resource.getIcon("questionReply.createCategory"), resource.getString(
            "questionReply.createCategory"), "javascript:onclick=addCategory()");
    operationPane.addLine();
  }
  if (!profil.equals("user")) {
    operationPane.addOperationOfCreation(resource.getIcon("questionReply.addQ"), resource.getString(
            "questionReply.addQ"), "CreateQQuery");
  }
  if (profil.equals("admin") || profil.equals("writer")) {
    operationPane.addOperationOfCreation(resource.getIcon("questionReply.addQR"), resource.getString(
            "questionReply.addQR"), "CreateQueryQR");
    operationPane.addLine();
    operationPane.addOperation(resource.getIcon("questionReply.delQ"), resource.getString(
            "questionReply.delQs"), "javascript:onClick=DeletesAdmin();");
    operationPane.addOperation(resource.getIcon("questionReply.cloreQ"), resource.getString(
            "questionReply.cloreQs"), "javascript:onClick=Closes();");
  }
  operationPane.addLine();
  operationPane.addOperation(resource.getIcon("questionReply.export"), resource.getString(
          "questionReply.export"), "javascript:onClick=openSPWindow('Export','export')");

  operationPane.addLine();
  operationPane.addOperation(resource.getIcon("GML.unsubscribe"),
      "<span id='subscriptionMenuLabel'></span>",
      "javascript:spSubManager.switchUserSubscription()");

  out.println(window.printBefore());
%>
<view:frame>
<view:componentInstanceIntro componentId="<%=componentId%>" language="<%=language%>"/>
<view:areaOfOperationOfCreation/>

  <div class="container-filter">
    <div id="search" class="content-container-filter ">
      <form method="post" action="javascript:search()" name="searchForm">
        <label for="query"><fmt:message key="GML.search"/> </label>
        <input size="40" maxlength="60" value="" type="text" id="query" name="query"/>
        <a class="sp_button" href="javascript:search()"><fmt:message key="GML.ok"/></a>
      </form>
    </div>
    <div id="filter" class="listing-filter content-container-filter "> <fmt:message key="questionReply.filters"/>
      <c:forEach items="${requestScope.Categories}" var="category">
        <a id="${category.id}" title="${category.description}" href="#" onclick="javascript:openCategory('${category.id}', true)">${category.name}</a>
      </c:forEach>
      <a id="null" href="#" onclick="javascript:openCategory('null', true)"><fmt:message key="questionReply.noCategory"/></a>
      - <a id="filter-all" class="active" href="#" onclick="filterAll()"><fmt:message key="GML.allFP"/></a> </div>
  </div>

  <form method="post" action="" name="mainForm">
  <ul>
    <fmt:message key="questionReply.updateCategory" bundle="${icons}" var="updateCategoryIcon"/>
    <fmt:message key="questionReply.deleteCategory" bundle="${icons}" var="deleteCategoryIcon"/>
    <c:forEach items="${requestScope.Categories}" var="category">
      <li class="category" id="category-${category.id}">
        <div class="categoryTitle" id="c<c:out value='${category.id}'/>">
          <h3><c:out value='${category.name}'/></h3>
          <p>
            <c:if test="${not empty category.description}"> : </c:if>
            <span class="categoryDescription"><c:out value='${category.description}'/></span>
          </p>
          <div class="action">
            <c:if test="${'admin' eq requestScope.Flag}">
            <a title="<fmt:message key="questionReply.updateCategory"/>" href="#" onclick="javascript:updateCategory('${category.id}')">
              <img src="<c:url value="${updateCategoryIcon}"/>" alt="<fmt:message key="questionReply.updateCategory"/>" border="0"/></a>
            <a title="<fmt:message key="questionReply.deleteCategory"/>" href="#" onclick="javascript:confirmDeleteCategory('<c:out value='${category.id}'/>');">
              <img src="<c:url value="${deleteCategoryIcon}"/>" alt="<fmt:message key="questionReply.deleteCategory"/>" border="0"/></a>
            </c:if>
          </div>
        </div>
        <ul class="questions" id="qc<c:out value='${category.id}'/>" ></ul>
    </li>
    </c:forEach>
    <li class="category" id="category-null">
      <div class="categoryTitle" id="cnull">
        <h3><fmt:message key="questionReply.noCategory"/></h3>
      </div>
      <ul class="questions" id="qcnull" ></ul>
    </li>
</ul>
</form>

  <div id="noResult" class="inlineMessage" style="display: none">
    <fmt:message key="questionReply.noResult"/>
  </div>

<c:url var="mandatoryFieldUrl" value="/util/icons/mandatoryField.gif"/>
<div id="addOrUpdateCategory" style="display: none;">
  <form name="categoryForm" action="CreateCategory" method="post">
    <table cellpadding="5" width="100%">
      <tr>
        <td class="txtlibform"><fmt:message key="GML.title"/> :</td>
        <td><input type="text" name="Name" id="categoryName" size="60" maxlength="150"/>
          &nbsp;<img border="0" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
      </tr>
      <tr>
        <td class="txtlibform"><fmt:message key="GML.description" /> :</td>
        <td><input type="text" name="Description" id="categoryDescription" size="60" maxlength="150"/></td>
      </tr>
      <tr>
        <td colspan="2"><img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField"/></td>
      </tr>
    </table>
    <input type="hidden" name="CategoryId"/>
  </form>
</div>

<form name="QForm" action="" method="post">
  <input type="hidden" name="Id" />
</form>

<form id="RForm" name="RForm" action="" method="post">
  <input type="hidden" name="replyId" />
  <input type="hidden" name="QuestionId" />
</form>
</view:frame>
<%
out.println(window.printAfter());
%>
<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.questionReply', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>
