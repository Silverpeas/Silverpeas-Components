<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

var etat = new Array();
function bindQuestionsEvent() {
  $('.questionTitle').on('click', function(event) {
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
      questionUrl = '<c:url value="/services/questionreply/${pageScope.componentId}/questions/category/"/>' + id;
      typeLien = category.substring(0,1);	
      if (typeLien!="l") {
        $('.category').removeClass('select');
        $('.questions').hide();
        $('#qc' + id + ' .answers').hide();
        $.each(etat, function(index) { 
          etat[index] = 'close';
        });
        var found = $('#qc'+id + '>li');
        if (found.length == 0) {  
		
			   $.ajax({
					url: questionUrl,
					type: "GET",
					contentType: "application/json",
					dataType: "json",
					cache: false,
					success: function(data) {
            $('#qc'+id).html('');
            $.each(data, function(key, question) {
              answersDiv = $('<div>').addClass('answers').attr('id', 'a' + question.id)
              answersDiv.append($('<p>').text(question.content));
              answersDiv.append($('<ul>'));
              answersDiv.hide();            
              $('#qc'+id).append($('<li>').append(displayQuestion(question)).append(answersDiv));
            });
						$('.questionTitle').off('click');
						bindQuestionsEvent();
					}
          });
		
		
        
        }
        $('#qc'+id).show();        	   
        $(this).parent().addClass('select');			
      }
    });
}
    
$(document).ready(function() {
  bindCategoryEvent();
  bindQuestionsEvent();
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
    questionDiv = $('<div>').addClass('question');
    questionTitleDiv = $('<div>').attr('id', 'q' + questionToBeDisplayed.id).addClass('questionTitle');
    questionTitle = $('<h4>');
    questionTitleLink = $('<a>').addClass('question').attr('id', 'l' + questionToBeDisplayed.id).attr('href', '#'+questionToBeDisplayed.id).attr('title', '<fmt:message key="questionReply.open"/>').text(questionToBeDisplayed.title);
    questionTitle.append(questionTitleLink);
    questionTitleDiv.append(questionTitle);
    questionHyperlink = $('<a>').addClass('permalink').attr('href', '<c:url value="/Question/" />' + questionToBeDisplayed.id).attr('title', '<fmt:message key="questionReply.CopyQuestionLink"/>');
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
    return questionDiv;
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
</head>
<body id="portlet-faq">
  <view:window browseBarVisible="false">
  <ul>
    <c:forEach items="${requestScope.Categories}" var="category">
      <li class="category">
        <div class="categoryTitle" id="c<c:out value='${category.id}'/>">
          <h3><a class="categoryTitle"  id="lc<c:out value='${category.id}'/>" title="<fmt:message key="questionReply.openCategory"/>" href="#"><c:out value='${category.name}'/></a></h3>
          <div class="action">          
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
  </ul>
  </view:window>
</body>
</html>