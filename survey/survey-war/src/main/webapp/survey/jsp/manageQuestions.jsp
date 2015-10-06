<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="com.stratelia.webactiv.survey.control.FileHelper" %>
<%@ page import="org.silverpeas.util.*" %>
<%@ include file="checkSurvey.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="ctxPath" value="${pageContext.request.contextPath}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="isPolling" value="${requestScope['PollingStationMode']}" />
<c:set var="action" value="${requestScope['Action']}" />
<c:set var="questions" value="${requestScope['Questions']}" />
<c:set var="style" value="${requestScope['Style']}" />
<c:set var="questionId" value="${requestScope['QuestionId']}"/>
<c:set var="styles" value="${requestScope['QuestionStyles']}" />
<c:set var="uploadDirectory" value="${requestScope['ImageDirectory']}"/>
<c:set var="surveyName" value="${requestScope['SurveyName']}" />

<%--var="suggestion" ${requestScope['Suggestion']}" --%>

<%-- Initialize data for current display --%>
<c:set var="questionLabel" value="" />
<c:set var="answerType" value="" />
<c:set var="answerNb" value="0" />
<c:set var="answers" value="" />
<c:set var="suggestAnswer" value="" />
<c:set var="suggestion" value="false" />
<c:set var="nbQuestion" value="${fn:length(questions) + 1}" />
<c:if test="${not empty questionId}">
  <%-- Retrieve the question information --%>
  <c:forEach var="curQuestion" items="${questions}" varStatus="qIndex">
    <c:if test="${qIndex.index == questionId}">
      <c:set var="questionLabel" value="${curQuestion.label}" />
      <c:set var="answerType" value="${curQuestion.style}" />
      <c:set var="answers" value="${curQuestion.answers}" />
      <c:forEach var="answer" items="${answers}">
        <c:if test="${answer.opened}">
          <c:set var="suggestion" value="true" />
          <c:set var="suggestAnswer" value="${answer}" />
        </c:if>
        <c:if test="${!answer.opened}">
          <c:set var="answerNb" value="${answerNb + 1}" />
        </c:if>
      </c:forEach>
    </c:if>
  </c:forEach>
  <c:set var="nbQuestion" value="${questionId + 1}" />
</c:if>
<c:if test="${empty questionId}">
  <%-- New question form --%>
  <c:set var="answerNb" value="2" />
</c:if>
<c:set var="suggestionCheckLabel" value=""/>
<c:if test="${suggestion}">
  <c:set var="suggestionCheckLabel" value="checked"/>
</c:if>

<%
String m_context =
  ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
String px = m_context + "/util/icons/colorPix/1px.gif";

SettingBundle surveySettings =
  ResourceLocator.getSettingBundle("org.silverpeas.survey.surveySettings");

String nbMaxAnswers = surveySettings.getString("NbMaxAnswers");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
<view:looknfeel />
<script type="text/javascript" src="<c:out value="${ctxPath}" />/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<c:out value="${ctxPath}" />/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function sendData() {
  if (isCorrectForm()) {
    if (checkAnswers()) {
      if ($('input[name=suggestion]').is(':checked')) {
        $("#hiddenSuggestionAllowedId").val("1");
      }
      document.surveyForm.submit();
    }
  }
}

function cancelUpdate() {
  document.cancelForm.submit();
}

function backQuestions() {
  cancelUpdate();
}

function checkAnswers() {
  var errorMsg = "";
  var errorNb = 0;
  var answerEmpty = false;
  var imageEmpty = false;
  var fieldsEmpty = "";
  if ($("#questionStyle").val() != "open") {
         
    for (var i=0; i<document.surveyForm.length; i++) 
    {
      inputName = document.surveyForm.elements[i].name.substring(0, 5);
      if (inputName == "answe" ) {
        if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
          answerEmpty = true;
        }
      }
    
      if (inputName == "image")
      {
        if (answerEmpty == true) {
          if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
            imageEmpty = true;
          }
        }
        answerEmpty = false;
      }
    
      if (inputName == "value")
      {
        if (imageEmpty == true) {
          if (isWhitespace(stripInitialWhitespace(document.surveyForm.elements[i].value))) {
            fieldsEmpty += (parseInt(document.surveyForm.elements[i].name.substring(17, document.surveyForm.elements[i].name.length))+1)+",";
            errorNb++;
          }
        }
        imageEmpty = false;
      }
    }
  }
 <c:if test="${!suggestion && action == 'SendQuestionForm'}">
  if (isWhitespace(stripInitialWhitespace(document.surveyForm.suggestionLabel.value))) {
       errorNb++;
  }
 </c:if>
  switch(errorNb) {
    case 0 :
        result = true;
        break;
    default :
        fields = fieldsEmpty.split(",");
        for (var i=0; i < fields.length-1; i++) {
          errorMsg += "<fmt:message key="SurveyCreationAnswerNb" /> "+fields[i]+" \n";
        }
    <c:if test="${!suggestion && action == 'SendQuestionForm'}">
        if (isWhitespace(stripInitialWhitespace($("#suggestionId").val()))) {
          errorMsg += "<fmt:message key="OtherAnswer" /> \n";
        }
    </c:if>
        window.alert("<fmt:message key="EmptyAnswerNotAllowed" /> \n" + errorMsg);
        result = false;
        break;
  }
  return result;
}

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;
  var question = stripInitialWhitespace(document.surveyForm.question.value);
  var nbAnswers = document.surveyForm.nbAnswers.value;
  if (isWhitespace(question)) {
    errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="SurveyCreationQuestion"/>' <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }
  if (document.surveyForm.questionStyle.options[document.surveyForm.questionStyle.selectedIndex].value=="null") {
    //choisir au moins un style
    errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="survey.style"/>' <fmt:message key="GML.MustBeFilled"/> \n";
    errorNb++;
  }
  else
  {
    if (document.surveyForm.questionStyle.options[document.surveyForm.questionStyle.selectedIndex].value != "open") {
        //Closed Question
        if (isWhitespace(nbAnswers)) {
           errorMsg +="  - <fmt:message key="GML.theField"/> '<fmt:message key="SurveyCreationNbPossibleAnswer"/>' <fmt:message key="GML.MustBeFilled"/>\n";
           errorNb++;
        } else {
               if (isInteger(nbAnswers)==false) {
                 errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="SurveyCreationNbPossibleAnswer"/>' <fmt:message key="GML.MustContainsFloat"/>\n";
                 errorNb++;
               } else {
                    if (document.surveyForm.suggestion.checked) {
                        //nb min answers = 1
                        if (nbAnswers <= 0) {
                           errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="SurveyCreationNbPossibleAnswer"/>' <fmt:message key="MustContainsNumberGreaterThan"/> 1\n";
                           errorNb++;
                        }
                    } else {
                        //nb min answers = 2
                        if (nbAnswers <= 1) {
                           errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="SurveyCreationNbPossibleAnswer"/>' <fmt:message key="MustContainsNumberGreaterThan"/> 2\n";
                           errorNb++;
                        }
                    }
                    if (nbAnswers > <%=nbMaxAnswers%>) {
                       errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="SurveyCreationNbPossibleAnswer"/>' <fmt:message key="MustContainsNumberLessThan"/> <%=nbMaxAnswers%>\n";
                         errorNb++;
                    }
               }
          }
     }
  }
  switch(errorNb) {
    case 0 :
        result = true;
        break;
    case 1 :
        errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
    default :
        errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
  }
  return result;
}

function goToEnd() {
  document.surveyForm.Action.value = "End";
  document.surveyForm.submit();
}

var galleryWindow = window;
var currentAnswer;

function choixGallery(liste, idAnswer)
{
  currentAnswer = idAnswer;
  index = liste.selectedIndex;
  var componentId = liste.options[index].value;
  if (index != 0)
  {
    url = "<c:out value="${ctxPath}"/>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=<%=surveyScc.getLanguage()%>";
    windowName = "galleryWindow";
    larg = "820";
    haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!galleryWindow.closed && galleryWindow.name=="galleryWindow") {
      galleryWindow.close();
    }
    galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
  }
}

function deleteImage(idImage)
{
  document.getElementById('imageGallery'+idImage).innerHTML = "";
  document.getElementById('valueImageGallery'+idImage).value = "";
}

function choixImageInGallery(url)
{

  deleteImage(currentAnswer);
	
  var newLink = document.createElement("img");
  newLink.setAttribute("src", url);
  newLink.setAttribute("height", "40px");
  newLink.setAttribute("align", "top");
  
  var newLabel = document.createTextNode("<fmt:message key="survey.imageGallery"/>");
  newLink.appendChild(newLabel);
  
  var removeLink =  document.createElement("a");
  removeLink.setAttribute("href", "javascript:deleteImage('"+currentAnswer+"')");
  var removeIcon = document.createElement("img");
  removeIcon.setAttribute("src", '<c:out value="${ctxPath}"/>/util/icons/delete.gif');
  removeIcon.setAttribute("border", "0");
  removeIcon.setAttribute("align", "top");
  removeIcon.setAttribute("alt", "<fmt:message key="GML.delete"/>");
  removeIcon.setAttribute("title", "<fmt:message key="GML.delete"/>");
  
  removeLink.appendChild(removeIcon);
  
  document.getElementById('imageGallery'+currentAnswer).appendChild(newLink);
  document.getElementById('imageGallery'+currentAnswer).appendChild(removeLink);
  
  document.getElementById('valueImageGallery'+currentAnswer).value = url;
}

/**
 * This method display the answer given the answer type
 * @param value : the answer type
 */
function showQuestionOptions(value) {
  //alert('showQuestionOptions call value=' + value);
  if (value == "open" || value == "null") {
    $("tr[id*=answerNotOpen]").hide();
    $("#suggestionTRId").hide();
  } else {
    if (value == "list") {
      $("tr[id*=answerNotOpen]").show();
      $("tr[id*=answerNotOpenImage]").hide();
      $("tr[id*=answerNotOpenGallery]").hide();
      updateQuestionForm();
    } else {
      $("tr[id*=answerNotOpen]").show();
      updateQuestionForm();
    }
  }
}

// Global variable declaration
var nbAnswerForm = <c:out value="${answerNb}"/>;

$(document).ready(function() {
  $( "#dialog" ).dialog({
    autoOpen: false/*,
    show: "blind",
    hide: "explode"*/
  });
});


function windowAlert(message) {
  $("#dialogId").html(message);
  $("#dialogId").dialog("open");
  return false;
}

function updateQuestionForm() {
  if ($('input[name=suggestion]').is(':checked')) {
    $("#suggestionTRId").show();
  } else {
    $("#suggestionTRId").hide();
  }
}

/**
 * This method add or remove answer respecting the number of answer to display
 * @param nbAnswer: the number of answer to diplay
 */
function udpateListAnswer(nbAnswer) {
  //alert("Nombre de reponse = " + nbAnswer + " compare to " + nbAnswerForm);
  var curNbAnswer = parseInt(nbAnswer);
  if (isCorrectForm()) {
    
    if (curNbAnswer > nbAnswerForm) {
      // Add new answer form
      for (var cptAnswer = nbAnswerForm; cptAnswer < nbAnswer; cptAnswer++) {
        var insertId = parseInt(cptAnswer) - 1;
        //alert("Add new answer cptAnswer= " + cptAnswer + "insertAfterId = answerNotOpenGallery" + insertId);
        //$(getHTMLAnswer(cptAnswer)).insertAfter($("#answerNotOpenGallery" + insertId));
        insertHTMLAnswer(cptAnswer);
      }
    } else if (curNbAnswer < nbAnswerForm) {
      // Remove answer form
      for (var cptAnswer = nbAnswerForm; cptAnswer >= nbAnswer; cptAnswer--) {
        //alert("removing element id= " + cptAnswer);
        $("#answerNotOpenGallery" + cptAnswer).remove();
        $("#answerNotOpenImage" + cptAnswer).remove();
        $("#answerNotOpenAnswerNb" + cptAnswer).remove();
      }
    }
    //Update global variable
    nbAnswerForm = parseInt(nbAnswer);
    //alert("valeur courante de nbAnswerForm = " + nbAnswerForm);
    //Display data
    showQuestionOptions($("#questionStyle").val());
  } else {
    $("#nbAnswersId").val(nbAnswerForm);
  }
}
/**
 * insertHTMLAnswer
 * method used to insert element in the right place 
 * because there is a problem adding element that already has been removed
 */
function insertHTMLAnswer(answerId) {
  var insertAfterIndex = answerId - 1;
  var addIndex = parseInt(answerId) + 1;

  var htmlAnswer= '<tr id="answerNotOpenAnswerNb' + answerId + '">';
  htmlAnswer += '  <td class="txtlibform"><fmt:message key="SurveyCreationAnswerNb" />&nbsp;' + (addIndex) + ' :</td>';
  htmlAnswer += '  <td><input type="text" name="<c:out value="answer' + answerId + '"/>" value="" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>"></td>';
  htmlAnswer += '</tr>';
  $(htmlAnswer).insertAfter($("#answerNotOpenGallery" + insertAfterIndex));
  

  htmlAnswer = '<tr id="answerNotOpenImage' + answerId + '">';
  htmlAnswer +=  '<td class="txtlibform"><fmt:message key="SurveyCreationAnswerImage"/>&nbsp;' + (addIndex) + ' :</td>';
  htmlAnswer +=  '<td><input type="file" name="image' + answerId + '" size="60"></td>';
  htmlAnswer +=  '</tr>';
  $(htmlAnswer).insertAfter($("#answerNotOpenAnswerNb" + answerId));

  htmlAnswer =  '<tr id="answerNotOpenGallery' + answerId + '">';
  htmlAnswer +=  '<td></td>';
  htmlAnswer +=  '<td><span id="imageGallery' + answerId + '"></span>';
  htmlAnswer +=  '<input type="hidden" id="valueImageGallery' + answerId + '" name="valueImageGallery' + answerId + '" />';
  <c:set var="gallery" value="${requestScope['Gallery']}" />
      <c:if test="${not empty(gallery)}">
  htmlAnswer +=  '<select id="galleries" name="galleries" onchange="javascript:choixGallery(this, \'' + answerId + '\');this.selectedIndex=0;">';
  htmlAnswer +=  '<option selected><fmt:message key="survey.galleries" /></option>';
        <c:forEach var="curGal" items="${gallery}" varStatus="galIndex">
  htmlAnswer +=  '<option value="<c:out value="${curGal.id}"/>"><c:out value="${curGal.label}"/></option>'; 
        </c:forEach>
  htmlAnswer +=  '      </select>';
      </c:if>
  htmlAnswer +=  '  </td>';
  htmlAnswer +=  '</tr>';
  $(htmlAnswer).insertAfter($("#answerNotOpenImage" + answerId));
}

</script>
</head>
<body>
<%-- ADD browsebar --%>  
<fmt:message key="QuestionAdd" var="browseBarLabel" />
<c:set var="addQuestion" value="true"/> 
<c:set var="nextAction" value="SendNewQuestion" /> 
<c:if test="${not empty questionId}">
  <fmt:message key="QuestionUpdate" var="browseBarLabel" />
  <c:set var="addQuestion" value="false"/> 
  <c:set var="nextAction" value="SendUpdateQuestion" /> 
</c:if>

<%-- Add browsebar label --%>
<fmt:message var="bbElt" key="SurveyUpdate"/>
<c:set var="bbElt" value="${bbElt} '${surveyName}'" />
<view:browseBar extraInformations="${browseBarLabel}">
  <view:browseBarElt label="${bbElt}" link="javascript:backQuestions();"></view:browseBarElt>
</view:browseBar>

<view:window>
  <view:frame>
   <center>
  <view:board>

<c:choose>
  <c:when test="${requestScope['UpdateSucceed']}">
    <div class="inlineMessage-ok">
      <fmt:message key="survey.update.succeed" />
    </div><br clear="all"/>
  </c:when>
</c:choose>
  
    <!-- SURVEY FORM BEGIN -->
    <form name="surveyForm" action="manageQuestions.jsp" method="post" enctype="multipart/form-data">
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr><%-- Question LABEL --%>
          <td class="txtlibform" width="30%"><fmt:message key="SurveyCreationQuestion"/> <c:out value="${nbQuestion}" /> :</td>
          <td width="70%">
            <input type="text" name="question" value="<view:encodeHtml string="${questionLabel}" />" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
          </td>
        </tr>

        <tr><%-- Question STYLE --%>
          <td class="txtlibform" valign="top"><fmt:message key="survey.style" /> :</td>
          <td>
            <select id="questionStyle" name="questionStyle" onchange="javascript:showQuestionOptions(this.value);">
              <option value="null"></option>
    <c:forEach var="curStyle" items="${styles}" >
      <c:set var="styleSelected" value=""/>
      <c:if test="${curStyle == answerType}">
        <c:set var="styleSelected" value="selected"/>
      </c:if>
      <fmt:message var="styleLabel" key="survey.${curStyle}"></fmt:message>
              <option <c:out value="${styleSelected}"/> value="<c:out value="${curStyle}" />"><c:out value="${styleLabel}"/></option>
    </c:forEach>
            </select>
          </td>
        </tr>

<c:choose>
<c:when test="${not empty answerType && answerType != 'open'}">
  <%-- Displaying answers --%>
        <tr id="answerNotOpenPA">
          <td class="txtlibform"><fmt:message key="SurveyCreationNbPossibleAnswer" /> :</td>
          <td>
            <input type="text" name="nbAnswers" id="nbAnswersId" value="<c:out value="${answerNb}"/>" size="3" onchange="javascript:udpateListAnswer(this.value);" />
          </td>
        </tr>
        <tr id="answerNotOpenSA">
          <td class="txtlibform"><fmt:message key="SuggestionAllowed" /> :</td>
          <td><input type="checkbox" name="suggestion" value="" <c:out value="${suggestionCheckLabel}"/> onchange="javascript:updateQuestionForm();"/></td>
        </tr>
        
    <c:forEach var="answer" items="${answers}" varStatus="answerStatus">
      <c:if test="${!answer.opened}">

        <tr id="answerNotOpenAnswerNb<c:out value="${answerStatus.index}"/>">
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerNb" />&nbsp;<c:out value="${answerStatus.index + 1}"/> :</td>
          <td><input type="text" name="<c:out value="answer${answerStatus.index}"/>" value="<c:out value="${answer.label}"/>" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>"></td>
        </tr>

    <c:set var="displayList" value="none"/>
    <c:if test="${answerType != 'list'}">
      <c:set var="displayList" value=""/>
    </c:if>

        <tr id="answerNotOpenImage<c:out value="${answerStatus.index}"/>" style="display:<c:out value="${displayList}"/>;">
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerImage"/>&nbsp;<c:out value="${answerStatus.index + 1}"/> :</td>
          <td><input type="file" name="image<c:out value="${answerStatus.index}"/>" size="60"></td>
        </tr>

        <!-- zone to display link on image -->
        <tr id="answerNotOpenGallery<c:out value="${answerStatus.index}"/>" style="display:<c:out value="${displayList}"/>;">
          <td></td>
          <%-- Check if an image already exist in the answer --%>
<c:set var="imageUrl" value=""/>
<c:choose>
  <c:when test="${not empty answer.image}">
    <c:choose>
      <c:when test="${fn:startsWith(answer.image, '/')}">
        <c:set var="imageUrl" value="${answer.image}"/>
      </c:when>
      <c:otherwise>
        <c:set var="imageUrl" value="${uploadDirectory}"/>
        <c:set var="imageUrl" value="${fn:replace(imageUrl, 'REPLACE_FILE_NAME', answer.image)}"/>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise></c:otherwise>
</c:choose>

          <td>
            <span id="imageGallery<c:out value="${answerStatus.index}"/>">
            <c:if test="${not empty imageUrl}">
              <img src="<c:out value="${imageUrl}"/>" border="0" height="40px" align="top"/>
              <a href="javascript:deleteImage('<c:out value="${answerStatus.index}"/>')"><img border="0" src="<c:out value="${ctxPath}"/>/util/icons/delete.gif" align="top" alt="<fmt:message key="GML.delete"/>" title="<fmt:message key="GML.delete"/>"></a>
            </c:if>
            </span>
            <input type="hidden" id="valueImageGallery<c:out value="${answerStatus.index}"/>" name="valueImageGallery<c:out value="${answerStatus.index}"/>" value="<c:out value="${answer.image}"/>" />
            <c:set var="gallery" value="${requestScope['Gallery']}" />
            <c:if test="${not empty(gallery)}">
              <select id="galleries" name="galleries" onchange="javascript:choixGallery(this, '<c:out value="${answerStatus.index}"/>');this.selectedIndex=0;">
                <option selected><fmt:message key="survey.galleries" /></option>
              <c:forEach var="curGal" items="${gallery}" varStatus="galIndex">
                 <option value="<c:out value="${curGal.id}"/>"><c:out value="${curGal.label}"/></option> 
              </c:forEach>
              </select>
            </c:if>
          </td>
        </tr>
                  
      </c:if>          
    </c:forEach>
</c:when>
<c:otherwise>
        <tr id="answerNotOpenPA" style="display:none;">
          <td class="txtlibform"><fmt:message key="SurveyCreationNbPossibleAnswer" /> :</td>
          <td>
            <input type="text" name="nbAnswers" id="nbAnswersId" value="<c:out value="${answerNb}"/>" size="3" onchange="javascript:udpateListAnswer(this.value);" />
          </td>
        </tr>
        <tr id="answerNotOpenSA" style="display:none;">
          <td class="txtlibform"><fmt:message key="SuggestionAllowed" /> :</td>
          <td><input type="checkbox" name="suggestion" value="" <c:out value="${suggestionCheckLabel}"/>  onchange="javascript:updateQuestionForm();"/></td>
        </tr>
        <tr id="answerNotOpenAnswerNb0" style="display:none;">
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerNb" />&nbsp;1 :</td>
          <td><input type="text" name="answer0" value="" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>"></td>
        </tr>
        <tr id="answerNotOpenImage0" style="display:none;">
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerImage"/>&nbsp;1 :</td>
          <td><input type="file" name="image0" size="60"></td>
        </tr>
        <!-- zone to display link on image -->
        <tr id="answerNotOpenGallery0" style="display:none;">
          <td></td>
          <td><span id="imageGallery0"></span>
            <input type="hidden" id="valueImageGallery0" name="valueImageGallery0" />
            <c:set var="gallery" value="${requestScope['Gallery']}" />
            <c:if test="${not empty(gallery)}">
              <select id="galleries" name="galleries" onchange="javascript:choixGallery(this, '0');this.selectedIndex=0;">
                <option selected><fmt:message key="survey.galleries" /></option>
              <c:forEach var="curGal" items="${gallery}" varStatus="galIndex">
                 <option value="<c:out value="${curGal.id}"/>"><c:out value="${curGal.label}"/></option> 
              </c:forEach>
              </select>
            </c:if>
          </td>
        </tr>
        <tr id="answerNotOpenAnswerNb1" style="display:none;">
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerNb" />&nbsp;2 :</td>
          <td><input type="text" name="answer1" value="" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>"></td>
        </tr>
        <tr id="answerNotOpenImage1" style="display:none;">
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerImage"/>&nbsp;2 :</td>
          <td><input type="file" name="image1" size="60"></td>
        </tr>
        <!-- zone to display link on image -->
        <tr id="answerNotOpenGallery1" style="display:none;">
          <td></td>
          <td><span id="imageGallery1"></span>
            <input type="hidden" id="valueImageGallery1" name="valueImageGallery1" />
            <c:set var="gallery" value="${requestScope['Gallery']}" />
            <c:if test="${not empty(gallery)}">
              <select id="galleries" name="galleries" onchange="javascript:choixGallery(this, '1');this.selectedIndex=0;">
                <option selected><fmt:message key="survey.galleries" /></option>
              <c:forEach var="curGal" items="${gallery}" varStatus="galIndex">
                 <option value="<c:out value="${curGal.id}"/>"><c:out value="${curGal.label}"/></option> 
              </c:forEach>
              </select>
            </c:if>
          </td>
        </tr>
</c:otherwise>
</c:choose>

<c:set var="displaySuggestion" value="none" />
<c:if test="${suggestion}">
  <c:set var="displaySuggestion" value="" />
</c:if>
        <tr>
          <td colspan="2" align="center">
            <table cellpadding="0" cellspacing="5" width="100%">
              <tr>
                <td class="intfdcolor"><img src="<%=px%>" border="0"></td>
              </tr>
            </table>
          </td>
        </tr>

<%-- Check if suggestion answer is empty --%>
<fmt:message var="suggestionLabel" key="SurveyCreationDefaultSuggestionLabel" />
<c:if test="${not empty suggestAnswer}">
  <c:set var="suggestionLabel" value="${suggestAnswer.label}"/>
</c:if>
        
        <tr id="suggestionTRId" style="display:<c:out value="${displaySuggestion}"/>">
          <td class="txtlibform"><fmt:message key="OtherAnswer"/>&nbsp;:</td>
          <td>
            <input type="text" name="suggestionLabel" id="suggestionId" value="<c:out value="${suggestionLabel}"/>" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>" />
          </td>
        </tr>

</div>



        <tr>
          <td>
            <input type="hidden" name="Action" value="<c:out value="${nextAction}" />">
            <input type="hidden" name="SuggestionAllowed" value="0" id="hiddenSuggestionAllowedId">
            <input type="hidden" name="QuestionId" value="<c:out value="${questionId}" />">
          </td>
        </tr>
      </table>
      </form>
      <!-- FIN CORPS -->
    </view:board>
</center>
<br/>
<center><%-- Add button pane --%>
  <fmt:message key="GML.cancel" var="cancelButtonLabel"></fmt:message>
  <fmt:message key="GML.validate" var="validateButtonLabel"></fmt:message>
  <view:buttonPane>
    <view:button label="${validateButtonLabel}" action="javascript:sendData();" disabled="false"></view:button>
    <view:button label="${cancelButtonLabel}" action="javascript:onClick=cancelUpdate();" disabled="false"></view:button>
  </view:buttonPane>
</center>
   
   
    
  </view:frame>
</view:window>

<form name="cancelForm" action="questionsUpdate.jsp" method="get" >
</form>


</body>
</html>