<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="java.util.*"%>
<%@ include file="checkQuestionReply.jsp" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:setLocale value="{sessionScope.SilverSessionController.favoriteLanguage}" />

<%
	// recuperation des parametres
	String		profil		= (String) request.getAttribute("Flag");
	String		userId		= (String) request.getAttribute("UserId");
	Collection 	questions 	= (Collection) request.getAttribute("questions");
	String		questionId	= (String) request.getAttribute("QuestionId");  // question en cours e ouvrir
	Collection	categories	= (Collection) request.getAttribute("Categories");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><fmt:message key="GML.popupTitle"/></title>
  <view:looknfeel />
  <link rel="stylesheet" type="text/css" href="css/question-reply-css.jsp" />
<script type="text/javascript">
<!-- 
$(document).ready(function() {  
  var etat = new Array();

  $('.question').live('click', function(objectEvent) {
      question = this.id;
      id = question.substring(1);
      answersUrl = '<c:url value="/services/questionreply/${pageScope.componentId}/replies/question/"/>' + id;
      typeLien = question.substring(0,1);
      if (typeLien!="l" && !$(objectEvent.target).hasClass('actionQuestion')) {
        $('#' + this.id + ' .answers').hide();
        if(etat[id] != "open"){
          $('#a'+id).show();
          etat[id] = "open";
          var found = $('#a'+id + '>ul>li');
          if (found.length == 0) {  
            $.getJSON(answersUrl,function(data) {
              $('#a'+id + ' > ul').html('');
              $.each(data, function(key, answer) {
                $('#a'+ id + ' > ul').append(displayAnswer(answer));
              });
            });
          }
        } else {
          $('#a'+id).hide();
          etat[id] = "close";
		} 
      }
    }, function() {}
  );
    
  $('.categoryTitle').click(function() {
      category = this.id;
      id = category.substring(1);
      questionUrl = '<c:url value="/services/questionreply/${pageScope.componentId}/questions/category/"/>' + id;
      typeLien = category.substring(0,1);	
      if (typeLien!="l") {
        $('.category').removeClass('select');
        $('.questions').hide();
        var found = $('#qc'+id + '>li');
        if (found.length == 0) {  
          $.getJSON(questionUrl,function(data) {
            $('#qc'+id).html('');
            $.each(data, function(key, question) {
              answersDiv = $('<div>').addClass('answers').attr('id', 'a' + question.id)
              answersDiv.append($('<p>').text(question.content));
              answersDiv.append($('<ul>'));
              answersDiv.hide();            
              $('#qc'+id).append($('<li>').append(displayQuestion(question)).append(answersDiv));
            });
          });
        }
        $('#qc'+id).show();        	   
        $(this).parent().addClass('select');			
      }
    }, function() {}
  );  
    
  $('.questions').hide();
  $("ul li:first-child .questions").show();
  $("ul li:first-child").addClass('select');
  $("ul li:first-child .categoryTitle").trigger($.Event("click"));

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
    questionDiv = $('<div>').attr('id', 'q' + questionToBeDisplayed.id).addClass('question');
    questionTitleDiv = $('<div>').addClass('questionTitle');
    questionTitle = $('<h4>');
    questionTitleLink = $('<a>').addClass('question').attr('id', 'l' + questionToBeDisplayed.id).attr('href', '#').attr('title', '<fmt:message key="questionReply.open"/>').text(questionToBeDisplayed.title);
    questionTitle.append(questionTitleLink);
    questionTitleDiv.append(questionTitle);
    questionHyperlink = $('<a>').addClass('hyperlink').attr('href', '<c:url value="/Question/" />' + questionToBeDisplayed.id).attr('title', '<fmt:message key="questionReply.CopyQuestionLink"/>');
    hyperlinkImg = $('<img>').addClass('actionQuestion').attr('src', '<c:url value="${hyperlinkIcon}"/>').attr('alt', '<fmt:message key="questionReply.CopyQuestionLink"/>').attr('border', '0');
    questionHyperlink.append(hyperlinkImg);    
    questionTitleDiv.append(questionHyperlink);
    switch(questionToBeDisplayed.status) {
      case 0 :
        questionStatusImg = $('<img>').addClass('status').attr('alt',  '<fmt:message key="questionReply.encours" />').attr('title',  '<fmt:message key="questionReply.encours" />').attr('src', '<c:url value="${newIcon}" />');
        questionTitleDiv.append(questionStatusImg);
        break;
      case 1 :
        questionStatusImg = $('<img>').addClass('status').attr('alt',  '<fmt:message key="questionReply.waiting" />').attr('title',  '<fmt:message key="questionReply.waiting" />').attr('src', '<c:url value="${waitingIcon}" />');
        questionTitleDiv.append(questionStatusImg);
        break;
      case 2 :
        questionStatusImg = $('<img>').addClass('status').attr('alt',  '<fmt:message key="questionReply.close" />').attr('title',  '<fmt:message key="questionReply.close" />').attr('src', '<c:url value="${closeIcon}" />');
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
      replyQuestionLink = $('<a>').addClass('reply').attr('title', '<fmt:message key="questionReply.ajoutR"/>').attr('href', 'CreateRQuery?QuestionId=' + questionToBeDisplayed.id);
      replyQuestionImg = $('<img>').addClass('actionQuestion').attr('alt', '<fmt:message key="questionReply.ajoutR"/>').attr('src', '<c:url value="${addReplyIcon}" />' );
      replyQuestionLink.append(replyQuestionImg);
      actionDiv.append(replyQuestionLink);
    }
    if(questionToBeDisplayed.reopenable){
      reopenQuestionLink = $('<a>').addClass('open').attr('title', '<fmt:message key="questionReply.open"/>').attr('href', 'javascript:openQ(\'' + questionToBeDisplayed.id + '\')');
      reopenQuestionImg = $('<img>').addClass('actionQuestion').attr('alt', '<fmt:message key="questionReply.open"/>').attr('src', '<c:url value="${openIcon}" />' );
      reopenQuestionLink.append(reopenQuestionImg);
      actionDiv.append(reopenQuestionLink);
    }
    if(questionToBeDisplayed.updatable){
      updateQuestionLink = $('<a>').addClass('update').attr('title', '<fmt:message key="questionReply.modifQ"/>').attr('href', 'UpdateQ?QuestionId=' + questionToBeDisplayed.id);
      updateQuestionImg = $('<img>').addClass('actionQuestion').attr('alt', '<fmt:message key="questionReply.modifQ"/>').attr('src', '<c:url value="${updateIcon}" />' );
      updateQuestionLink.append(updateQuestionImg);
      actionDiv.append(updateQuestionLink);
      
      deleteQuestionLink = $('<a>').addClass('delete').attr('title', '<fmt:message key="questionReply.delQ"/>').attr('href', 'javascript:deleteConfirm(\'' + questionToBeDisplayed.id + '\')');
      deleteQuestionImg = $('<img>').attr('alt', '<fmt:message key="questionReply.delQ"/>').attr('src', '<c:url value="${deleteIcon}" />' );
      deleteQuestionLink.append(deleteQuestionImg);
      actionDiv.append(deleteQuestionLink);
    }
    
    actionDiv.append($('<input>').addClass('checkbox').attr('name', 'checkedQuestion').attr('value', questionToBeDisplayed.id).attr('type', 'checkbox'));
    actionDiv.append($('<input>').attr('name', 'status').attr('value', questionToBeDisplayed.status).attr('type', 'hidden'));
    questionDiv.append(actionDiv);
    return questionDiv;
  }
  <fmt:message key="questionReply.minicone" bundle="${icons}" var="publicAnswerIcon"/>
  <fmt:message key="questionReply.miniconeReponse" bundle="${icons}" var="privateAnswerIcon"/>
  function displayAnswer(answer) {
    answerBlock = $('<li>').addClass('answer tableBoard');
    answerTitle = $('<h5>').addClass('answerTitle').text(answer.title);
    if(answer.publicReply) {
      answerTitle.append($('<img>').addClass('status').attr('alt','<fmt:message key="questionReply.Rpublique" />').attr('title','<fmt:message key="questionReply.Rpublique" />').attr('src', '<c:url value="${publicAnswerIcon}" />'));
    } else {
      answerTitle.append($('<img>').addClass('status').attr('alt','<fmt:message key="questionReply.Rprivee" />').attr('title','<fmt:message key="questionReply.Rprivee" />').attr('src', '<c:url value="${privateAnswerIcon}" />'));
    }
    actionDiv = $('<div>').addClass('action');    
    if(!answer.readonly){
      updateAnswerLink = $('<a>').attr('title', '<fmt:message key="questionReply.modifR" />').attr('href', 'UpdateR?replyId=' + answer.id + '&QuestionId=' + answer.questionId);
      updateAnswerImg = $('<img>').attr('alt', '<fmt:message key="questionReply.modifR" />').attr('src', '<c:url value="${updateIcon}" />');
      updateAnswerLink.append(updateAnswerImg);
      actionDiv.append(updateAnswerLink);
      
      deleteAnswerLink = $('<a>').attr('title', '<fmt:message key="questionReply.delR" />').attr('href', 'javascript:deleteConfirmR(\'' + answer.id + '\', \'' + answer.questionId + '\')');
      deleteAnswerImg = $('<img>').attr('alt', '<fmt:message key="questionReply.delR" />').attr('src', '<c:url value="${deleteIcon}" />');
      deleteAnswerLink.append(deleteAnswerImg);
      actionDiv.append(deleteAnswerLink);
    }
    answerTitle.append(actionDiv);
    answerBlock.append(answerTitle);
    answerContentDiv = $('<div>').addClass('answerContent');
    answerAttachmentDiv = $('<div>').addClass('answerAttachment');
    if(answer.attachments != null && answer.attachments.length > 0) {
      answerAttachmentDiv.load('<c:url value="/attachment/jsp/displayAttachments.jsp?Context=Images&ComponentId=${pageScope.componentId}" />&Id=' + answer.id);
      answerContentDiv.append(answerAttachmentDiv);
    }
    answerContentDiv.append(answer.content);
    answerBlock.append(answerContentDiv);
    answerAuthorBlock = $('<span>').addClass('answerAuthor txtBaseline').text(answer.creatorName);
    answerDateBlock = $('<span>').addClass('answerDate').text(' - ' + answer.creationDate);
    answerAuthorBlock.append(answerDateBlock);
    answerBlock.append(answerAuthorBlock);
    return answerBlock;
  }
-->
</script>
<script type="text/javascript">

function openSPWindow(fonction, windowName)
{
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

// supprimer une question
function deleteConfirm(id)
{
	//confirmation de suppression de la question
	if(window.confirm('<fmt:message key="MessageSuppressionQ" />'))
	{
			document.QForm.action = "DeleteQ";
			document.QForm.Id.value = id;
			document.QForm.submit();
	}
}

// clore une question
function closeQ(id)
{
	//confirmation de cloture de la question
	if(window.confirm('<fmt:message key="MessageCloseQ" />'))
	{
			document.QForm.action = "CloseQ";
			document.QForm.Id.value = id;
			document.QForm.submit();
	}
}

//reouvrir une question
function openQ(id)
{
	//confirmation de l'ouverture de la question
	if(window.confirm("<%=resource.getString("MessageOpenQ")%>"))
	{
      document.QForm.action = "OpenQ";
      document.QForm.Id.value = id;
      document.QForm.submit();
	}
}

// supprimer toutes les questions selectionnees
function DeletesAdmin()
{
	if (existSelected())
	{
		if (existStatusError('2', '0'))
			alert("<%=resource.getString("questionReply.delStatusErr")%>");
		else
		{
			if (window.confirm("<%=resource.getString("MessageSuppressionsQ")%>"))
			{
				document.forms[0].action = "<%=routerUrl%>DeleteQuestions";
				document.forms[0].submit();
			}
		}
	}
}

// clore toutes les questions selectionnees
function Closes()
{
	if (existSelect())
	{
		if (existStatusError('1'))
			alert("<%=resource.getString("questionReply.closeStatusErr")%>");
		else
		{
			if (window.confirm("<%=resource.getString("MessageClosesQ")%>"))
			{
				document.forms[0].action = "<%=routerUrl%>CloseQuestions";
				document.forms[0].submit();
			}
		}
	}
}

// controler si toutes les cases cochees sont valides pour l'operation demandee
function existStatusError(status)
{
	var err = false;
	if (document.forms[0].status != null)
	{
		if (document.forms[0].status.length != null)
		{
			var i = 0;
			while (i < document.forms[0].status.length)
			{
				 var statusQ = document.forms[0].status[i].value;
				 if ((document.forms[0].checkedQuestion[i] != null)&&(document.forms[0].checkedQuestion[i].checked))
				 {
					if (statusQ != status)
					{
						err = true;
						document.forms[0].checkedQuestion[i].checked = false;
					}
				 }
				i++;
			}
		}
	}
	return err;
}

function existStatusError(status1, status2)
{
	var err = false;
	if (document.forms[0].status != null)
	{
		if (document.forms[0].status.length != null)
		{
			var i = 0;
			while (i < document.forms[0].status.length)
			{
				 var statusQ = document.forms[0].status[i].value;
				 if ((document.forms[0].checkedQuestion[i] != null)&&(document.forms[0].checkedQuestion[i].checked))
				 {
					if (statusQ != status1 && statusQ != status2)
					{
						err = true;
						document.forms[0].checkedQuestion[i].checked = false;
					}
				 }
				i++;
			}
		}
	}
	return err;
}

// recherche s'il y a des questions selectionnees
function existSelect()
{
	if (document.forms[0].checkedQuestion != null)
	{
		if (document.forms[0].checkedQuestion.length != null)
		{
			var i = 0;
			while (i < document.forms[0].checkedQuestion.length)
			{
				 if (document.forms[0].checkedQuestion[i].checked)
					return true;
				i ++;
			}
		}
		else
		{
			 if (document.forms[0].checkedQuestion.checked)
				return true;

		}
	}
	return false;
}

// supprimer une reponse
function deleteConfirmR(replyId, questionId)
{
  //confirmation de suppression de la question
  if(window.confirm("<%=resource.getString("MessageSuppressionR")%>")) {
    document.RForm.action = "DeleteR";
    document.RForm.replyId.value = replyId;
    document.RForm.QuestionId.value = questionId;
    document.RForm.submit();
  }
}

function confirmDeleteCategory(categoryId) {
	if (confirm("<%=resource.getString("questionReply.confirmDeleteCategory")%>")) {
		window.location.href=("DeleteCategory?CategoryId=" + categoryId + "");
	}
}
<fmt:message key="GML.subscribe" var="labelSubscribe"/>
<fmt:message key="GML.unsubscribe" var="labelUnsubscribe"/>
function successUnsubscribe() {
   $("#yui-gen1").empty().append($('<a>').addClass('yuimenuitemlabel').attr('href', 
   "javascript:subscribe();").attr('title', 
   '<view:encodeJs string="${labelUnsubscribe}" />').append('<view:encodeJs string="${labelSubscribe}" />') );
}

function successSubscribe() {
   $("#yui-gen1").empty().append($('<a>').addClass('yuimenuitemlabel').attr(
   'href', "javascript:unsubscribe();").attr('title', 
   '<view:encodeJs string="${labelUnsubscribe}" />').append('<view:encodeJs string="${labelUnsubscribe}" />') );
}

function unsubscribe() {
  $.post('<c:url value="/services/unsubscribe/${pageScope.componentId}" />',successUnsubscribe(), 'json');
}

function subscribe() {
  $.post('<c:url value="/services/subscribe/${pageScope.componentId}" />', successSubscribe(), 'json');
}
</script>
</head>
<body>
<%
  browseBar.setDomainName(spaceLabel);
  browseBar.setPath("");
  
  if (profil.equals("admin")) {
    // gestion du plan de classement
    operationPane.addOperation(resource.getIcon("questionReply.pdcUtilizationSrc"), resource.
            getString("GML.PDCParam"),
            "javascript:onClick=openSPWindow('" + m_context + "/RpdcUtilization/jsp/Main?ComponentId=" + componentId + "','utilizationPdc1')");
    operationPane.addLine();
    // creation des categories
    operationPane.addOperation(resource.getIcon("questionReply.createCategory"), resource.getString(
            "questionReply.createCategory"), "NewCategory");
    operationPane.addLine();
  }
  if (!profil.equals("user")) {
    operationPane.addOperation(resource.getIcon("questionReply.addQ"), resource.getString(
            "questionReply.addQ"), "CreateQQuery");
  }
  if (profil.equals("admin") || profil.equals("writer")) {
    operationPane.addOperation(resource.getIcon("questionReply.addQR"), resource.getString(
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
  
  if(((Boolean)request.getAttribute("userAlreadySubscribed"))) {
    operationPane.addOperation(resource.getIcon("GML.unsubscribe"), resource.getString(
          "GML.unsubscribe"), "javascript:unsubscribe();");
  }else {
    operationPane.addOperation(resource.getIcon("GML.subscribe"), resource.getString(
          "GML.subscribe"), "javascript:subscribe();");
  }
            
  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<form method="post" action="">
  <ul>
    <fmt:message key="questionReply.updateCategory" bundle="${icons}" var="updateCategoryIcon"/>
    <fmt:message key="questionReply.deleteCategory" bundle="${icons}" var="deleteCategoryIcon"/>
    <c:forEach items="${requestScope.Categories}" var="category">
      <li class="category">
        <div class="categoryTitle" id="c<c:out value='${category.id}'/>">
          <h3><a class="categoryTitle"  id="lc<c:out value='${category.id}'/>" title="<fmt:message key="questionReply.openCategory"/>" href="#"><c:out value='${category.name}'/></a></h3>
          <div class="action">
            <c:if test="${'admin' eq requestScope.Flag}">
            <a title="<fmt:message key="questionReply.updateCategory"/>" href="EditCategory?CategoryId=<c:out value='${category.id}'/>"> 
              <img src="<c:url value="${updateCategoryIcon}"/>" alt="<fmt:message key="questionReply.updateCategory"/>"/></a>
            <a title="<fmt:message key="questionReply.deleteCategory"/>" href="javascript:confirmDeleteCategory('<c:out value='${category.id}'/>');">
              <img src="<c:url value="${deleteCategoryIcon}"/>" alt="<fmt:message key="questionReply.deleteCategory"/>"/></a>
            </c:if>
          </div>
        </div>
        <ul class="questions" id="qc<c:out value='${category.id}'/>" ></ul>
    </li>
    </c:forEach>
    <li class="category">
        <div class="categoryTitle" id="cnull">
          <h3><a class="categoryTitle"  id="lcnull" title="<fmt:message key="questionReply.openCategory"/>" href="#"><fmt:message key="questionReply.noCategory"/></a></h3>
          <div class="action">            
          </div>
        </div>
        <ul class="questions" id="qcnull" ></ul>
    </li>

</form>
  
<form name="QForm" action="" method="post">
  <input type="hidden" name="Id" />
</form>
  
<form id ="RForm" name="RForm" action="" method="post">
  <input type="hidden" name="replyId" />
  <input type="hidden" name="QuestionId" />
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>