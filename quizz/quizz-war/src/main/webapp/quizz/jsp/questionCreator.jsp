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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />
<jsp:useBean id="questionsVector" scope="session" class="java.util.ArrayList" />

<%@ include file="checkQuizz.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.stratelia.webactiv.quizz.multilang.quizz"/>

<%

//Retrieve parameter
String nextAction = "";
String m_context = GeneralPropertiesManager.getString("ApplicationURL");

int nbZone = 4; // number of field to control
List<ComponentInstLight> galleries = quizzScc.getGalleries();
if (galleries != null) {
	nbZone = nbZone + 2;
}

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
String ligne = m_context + "/util/icons/colorPix/1px.gif";

ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", quizzScc.getLanguage());
ResourceLocator quizzSettings = quizzScc.getSettings();

Button validateButton = null;
Button cancelButton = null;
Button finishButton = null;
ButtonPane buttonPane = null;

List<FileItem> items = FileUploadUtil.parseRequest(request);
boolean file = false;
int nb = 0;
int attachmentSuffix = 0;
String action = FileUploadUtil.getOldParameter(items, "Action", "");
String question = FileUploadUtil.getOldParameter(items, "question", "");
String clue =  FileUploadUtil.getOldParameter(items, "clue", "");
String penalty = FileUploadUtil.getOldParameter(items, "penalty", "");
String nbPointsMin = FileUploadUtil.getOldParameter(items, "nbPointsMin", "");
String nbPointsMax = FileUploadUtil.getOldParameter(items, "nbPointsMax", "");
String nbAnswers = FileUploadUtil.getOldParameter(items, "nbAnswers", "");
String style = FileUploadUtil.getOldParameter(items, "questionStyle", "");
QuestionForm form = new QuestionForm(file, attachmentSuffix);
List<Answer> answers = QuestionHelper.extractAnswer(items, form, quizzScc.getComponentId(), quizzSettings.getString("imagesSubDirectory"));
file = form.isFile();
attachmentSuffix = form.getAttachmentSuffix();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel />
<style type="text/css">
.thumbnailPreviewAndActions {
  display: none;
}
</style>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">

function sendData() {
    if (isCorrectForm()) {
        document.quizzForm.submit();
    }
}
function sendData2() {
    if (isCorrectForm2()) {
        document.quizzForm.submit();
    }
}

function isCorrectForm()
{
   var errorMsg = "";
   var errorNb = 0;
   var question = stripInitialWhitespace(document.quizzForm.question.value);
   var nbAnswers = document.quizzForm.nbAnswers.value;
   var clue = document.quizzForm.clue.value;
   var penalty = document.quizzForm.penalty.value;
   var nbPointsMin = document.quizzForm.nbPointsMin.value;
   var nbPointsMax = document.quizzForm.nbPointsMax.value;

   if (isWhitespace(nbAnswers))
   {
           errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswers")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
   }
   if (document.quizzForm.questionStyle.options[document.quizzForm.questionStyle.selectedIndex].value=="null") {
   	//choisir au moins un style
    	errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("quizz.style")%>' <%=resources.getString("GML.MustBeFilled")%> \n";
    	errorNb++;
   }
   else
   {
      if (isInteger(nbAnswers)==false)
      {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswers")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
          errorNb++;
      }
      else
      {
          if (nbAnswers <= 0)
          {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswers")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
              errorNb++;
           }
      }
   }
  if (!isWhitespace(penalty))
  {
      if (isInteger(penalty)==false)
      {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzPenalty")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
          errorNb++;
      }
      else
      {
          if (penalty <= 0)
          {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzPenalty")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
              errorNb++;
          }
      }
      if (isWhitespace(clue))
      {
         errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzClue")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
         errorNb++;
      }
  }
 if (!isWhitespace(clue))
  {
      if (!isValidTextArea(document.quizzForm.clue))
      {
         errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzClue")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Caracters")%>\n";
         errorNb++;
      }
      if (isWhitespace(penalty))
      {
         errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzPenalty")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
         errorNb++;
      }
  }

  if (!isWhitespace(nbPointsMax))
  {
      if (isSignedInteger(nbPointsMax)==false)
      {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
          errorNb++;
      }
      else
      {
          if (nbPointsMax <= 0)
          {
              errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
              errorNb++;
          }
      }
  }
  if (!isWhitespace(nbPointsMin))
  {
      if (isSignedInteger(nbPointsMin)==false)
      {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMin")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
          errorNb++;
      }
      else
      {
              if (parseInt(nbPointsMin, 10) >= parseInt(nbPointsMax, 10))
              {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPointsMin")%>' <%=resources.getString("MustContainsStrictlyInfNumber")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>'\n";
                errorNb++;
              }
      }
  }

   if (isWhitespace(question)) {
         errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationQuestion")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
         errorNb++;
   }
   switch(errorNb) {
      case 0 :
          result = true;
          break;
      case 1 :
          errorMsg = "<%=resources.getString("GML.ThisFormContain")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
      default :
          errorMsg = "<%=resources.getString("GML.ThisFormContain")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
   }
   return result;
}
function isCorrectForm2()
{
     var errorMsg = "";
     var errorNb = 0;
     var nb = Number(document.quizzForm.nbAnswers.value);
     var nbPointsMax = Number(document.quizzForm.nbPointsMax.value);
     var nbPointsMin = Number(document.quizzForm.nbPointsMin.value);
     for (var i = 0; i < nb; i++)
     {
       var answer = $("#answer"+i).val(); // document.quizzForm.elements[<%=nbZone%>*i+7].value
       var nbPoints = $("#nbPoints"+i).val(); //document.quizzForm.elements[<%=nbZone%>*i+8].value;
       var comment = $("#comment"+i).val(); //document.quizzForm.elements[<%=nbZone%>*i+9].value;

         if (isWhitespace(nbPoints))
         {
                 errorMsg +="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("GML.MustBeFilled")%>\n";
                 errorNb++;
         }
         else
         {
            if (isSignedInteger(nbPoints)==false)
            {
                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("GML.MustContainsFloat")%>\n";
                errorNb++;
            }
	    else
	    {
		if((document.quizzForm.nbPointsMax.value!='')&&(parseInt(nbPoints, 10) > parseInt(nbPointsMax, 10)))
		{
	                errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("MustContainsInfNumber")%> '<%=resources.getString("QuizzCreationNbPointsMax")%>'\n";
			errorNb++;
		}
		else
		{
			if((document.quizzForm.nbPointsMin.value!='')&&(parseInt(nbPoints, 10) < parseInt(nbPointsMin, 10)))
			{
				errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzNbPoints")%> "+String(i+1)+"' <%=resources.getString("MustContainsSupNumber")%> '<%=resources.getString("QuizzCreationNbPointsMin")%>'\n";
				errorNb++;
			}
		}

	    }
         }
         if (isWhitespace(answer)) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationAnswerNb")%> "+String(i+1)+"' <%=resources.getString("GML.MustBeFilled")%>\n";
               errorNb++;
         }
         if ((!isWhitespace(comment)) && (!isValidTextArea(document.quizzForm.elements[<%=nbZone%>*i+9])))
          {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationAnswerComment")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Caracters")%>\n";
               errorNb++;
          }
   }
  switch(errorNb) {
      case 0 :
          result = true;
          break;
      case 1 :
          errorMsg = "<%=resources.getString("GML.ThisFormContain")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
      default :
          errorMsg = "<%=resources.getString("GML.ThisFormContain")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
          window.alert(errorMsg);
          result = false;
          break;
  }
  return result;
}
function goToEnd() {
    document.quizzForm.Action.value = "End";
    document.quizzForm.submit();
}
function confirmCancel()
{
	if (confirm('<%=resources.getString("ConfirmCancel")%>'))
		self.location="Main.jsp";
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
    url = "<%=m_context%>/gallery/jsp/wysiwygBrowser.jsp?ComponentId="+componentId+"&Language=<%=quizzScc.getLanguage()%>";
    windowName = "galleryWindow";
    larg = "820";
    haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    if (!galleryWindow.closed && galleryWindow.name=="galleryWindow")
      galleryWindow.close();
    galleryWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
  }
}

function deleteImage(idImage) {
  $("#thumbnailPreviewAndActions"+idImage).css("display", "none");
  $("#valueImageGallery"+idImage).attr("value", "");
}

function choixImageInGallery(url) {
  $("#thumbnailPreviewAndActions"+currentAnswer).css("display", "block");
  $("#thumbnailActions"+currentAnswer).css("display", "block");
  $("#thumbnail"+currentAnswer).attr("src", url);
  $("#valueImageGallery"+currentAnswer).attr("value", url);
}
</script>
</head>
<%

if (action.equals("FirstQuestion")) {
      session.setAttribute("questionsVector", new ArrayList<Question>(10));
      action = "CreateQuestion";
}
if (action.equals("SendNewQuestion")) {
  	  List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
      int questionNb = questionsV.size() + 1;
      int penaltyInt=0;
      int nbPointsMinInt=-1000;
      int nbPointsMaxInt=1000;
      if (!penalty.equals(""))
        penaltyInt= Integer.parseInt(penalty);
      if (!nbPointsMin.equals(""))
        nbPointsMinInt=Integer.parseInt(nbPointsMin);
      if (!nbPointsMax.equals(""))
        nbPointsMaxInt=Integer.parseInt(nbPointsMax);
      Question questionObject = new Question(null, null, question, null, clue, null, 0, style,penaltyInt,0,questionNb, nbPointsMinInt, nbPointsMaxInt);

      questionObject.setAnswers(answers);
      questionsV.add(questionObject);
      action = "CreateQuestion";
} //End if action = ViewResult
else if (action.equals("End")) {
      out.println("<body>");
      QuestionContainerDetail questionContainerDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
      //Vector 2 Collection
      List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
      questionContainerDetail.setQuestions(questionsV);
      out.println("</body></html>");
}
if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm"))) {
      out.println("<body>");
      List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
      int questionNb = questionsV.size() + 1;
      cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "javascript:confirmCancel();", false);
      buttonPane = gef.getButtonPane();
      if (action.equals("CreateQuestion")) {
            validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            finishButton = gef.getFormButton(resources.getString("Finish"), "javascript:onClick=goToEnd()", false);
            question = "";
            nbAnswers = "";
            penalty = "";
            clue = "";
            nbPointsMin ="";
            nbPointsMax ="";
            nextAction="SendQuestionForm";
            buttonPane.addButton(validateButton);
            if (questionsV.size() != 0) {
                buttonPane.addButton(finishButton);
            }
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      } else if (action.equals("SendQuestionForm")) {
            validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData2()", false);
            nextAction="SendNewQuestion";
            buttonPane.addButton(validateButton);
            buttonPane.addButton(cancelButton);
            buttonPane.setHorizontalPosition();
      }

      Window window = gef.getWindow();
      Frame frame = gef.getFrame();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(quizzScc.getSpaceLabel());
      browseBar.setComponentName(quizzScc.getComponentLabel());
      browseBar.setExtraInformation(resources.getString("QuizzCreation"));

      out.println(window.printBefore());

      out.println(frame.printBefore());
      QuestionContainerDetail questionDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
      QuestionContainerHeader questionContainerHeader = questionDetail.getHeader();
      String beginDate = "&nbsp;";
      if (questionContainerHeader.getBeginDate() != null) {
        beginDate = resources.getOutputDate(questionContainerHeader.getBeginDate());
      }
      String endDate = "&nbsp;";
      if (questionContainerHeader.getEndDate() != null) {
        endDate = resources.getOutputDate(questionContainerHeader.getEndDate());
      }      
%>
<c:set var="quizDetail" value="${sessionScope['quizzUnderConstruction']}" />
<!--DEBUT CORPS -->
<form name="quizzForm" action="questionCreator.jsp" method="post" enctype="multipart/form-data">

<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.info" /></legend>
  <!-- SAISIE DU QUIZZ -->
  <div class="fields">
    <!-- Forum name -->
    <div class="field" id="titleArea">
      <label class="txtlibform" for="title"><fmt:message key="GML.name" /> </label>
      <div class="champs">
        <input type="text" name="title" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="${quizDetail.title}" disabled="disabled" />
      </div>
    </div>

    <div class="field" id="nbQuestionsArea">
      <label class="txtlibform" for="nbQuestions"><fmt:message key="QuizzCreationNbQuestionPerPage" /> </label>
      <div class="champs">
        <input type="text" name="nbQuestions" size="5" maxlength="3" value="${quizDetail.header.nbQuestionsPerPage}" disabled="disabled" />
      </div>
    </div>
     
    <div class="field" id="nbAnswersMaxArea">
      <label class="txtlibform" for="nbAnswersMax"><fmt:message key="QuizzCreationNbPossibleAnswer" /> </label>
      <div class="champs">
        <input type="text" name="nbAnswersMax" size="5" maxlength="3" value="${quizDetail.header.nbMaxParticipations}" disabled="disabled"/>
      </div>
    </div>
    
    <div class="field" id="nbAnswersNeededArea">
      <label class="txtlibform" for="nbAnswersNeeded"><fmt:message key="QuizzCreationNbAnswerNeeded" /> </label>
      <div class="champs">
        <input type="text" name="nbAnswersNeeded" size="5" maxlength="3" value="${quizDetail.header.nbParticipationsBeforeSolution}" disabled="disabled"/>
      </div>
    </div>

    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="description" cols="49" rows="3" disabled="disabled">${quizDetail.header.description}</textarea>
      </div>
    </div>

    <div class="field" id="noticeArea">
      <label class="txtlibform" for="notice"><fmt:message key="QuizzCreationNotice" /> </label>
      <div class="champs">
        <textarea name="notice" cols="49" rows="3" disabled="disabled">${quizDetail.header.comment}</textarea>
      </div>
    </div>
    
  </div>
</fieldset>

<fieldset id="datesFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.period" /></legend>
  <div class="fields">
    <div class="field" id="beginArea">
      <label for="beginDate" class="txtlibform"><fmt:message key="QuizzCreationBeginDate" /></label>
      <div class="champs">
        <input type="text" class="dateToPick" name="beginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>" disabled="disabled"/>
      </div>
    </div>
    <div class="field" id="endArea">
      <label for="endDate" class="txtlibform"><fmt:message key="QuizzCreationEndDate" /></label>
      <div class="champs">
        <input type="text" class="dateToPick" name="endDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>" disabled="disabled"/>
      </div>
    </div>
  </div>  
</fieldset>
      
<%    
      Board board = gef.getBoard();
%>

<% if (action.equals("SendQuestionForm")) {
%>
  <input type="hidden" name="question" value="<%=EncodeHelper.javaStringToHtmlString(question) %>" />
  <input type="hidden" name="questionStyle" value="<%=style %>" />
  <input type="hidden" name="nbAnswers" value="<%=nbAnswers%>"/>
  <input type="hidden" name="nbPointsMin" value="<%=nbPointsMin%>"/>
  <input type="hidden" name="nbPointsMax" value="<%=nbPointsMax%>"/>
  <input type="hidden" name="clue" value="<%=EncodeHelper.javaStringToHtmlString(clue)%>"/>
  <input type="hidden" name="penalty" value="<%=penalty%>"/>


<fieldset id="questionFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.question" /></legend>
  <div class="fields">
    <div class="field" id="questionArea"> 
      <label for="question" class="txtlibform"><fmt:message key="QuizzCreationQuestion" />&nbsp;<%=questionNb%></label>
      <div class="champs"><textarea name="questionBis" cols="49" rows="3" disabled="disabled"><%=EncodeHelper.javaStringToHtmlString(question)%></textarea>&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/></div>
    </div>
    <div class="field" id="questionStyleArea">
      <label for="questionStyle" class="txtlibform"><fmt:message key="quizz.style" /></label>
      <div class="champs"><%=resources.getString("quizz."+style) %>
      </div>
    </div>
    <div class="field" id="nbAnswersArea">
      <label for="nbAnswers" class="txtlibform"><fmt:message key="QuizzCreationNbAnswers" /></label>
      <div class="champs">
        <input type="text" name="nbAnswersBis" value="<%=nbAnswers%>" size="5" maxlength="3" disabled="disabled"/>&nbsp;&nbsp;&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/>
      </div>
    </div>

    <div class="field" id="nbPointsMinArea">
      <label for="nbPointsMin" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMin" /></label>
      <div class="champs">
        <input type="text" name="nbPointsMinBis" value="<%=nbPointsMin%>" size="5" maxlength="3" disabled="disabled"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

    <div class="field" id="nbPointsMaxArea">
      <label for="nbPointsMax" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMax" /></label>
      <div class="champs">
        <input type="text" name="nbPointsMaxBis" value="<%=nbPointsMax%>" size="5" maxlength="3" disabled="disabled"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

    <div class="field" id="clueArea">
      <label for="clue" class="txtlibform"><fmt:message key="QuizzClue" /></label>
      <div class="champs">
        <textarea name="clueBis" cols="49" rows="3" disabled="disabled"><%=EncodeHelper.javaStringToHtmlString(clue)%></textarea>
      </div>
    </div>

    <div class="field" id="penaltyArea">
      <label for="penalty" class="txtlibform"><fmt:message key="QuizzPenalty" /></label>
      <div class="champs">
        <input type="text" name="penaltyBis" value="<%=penalty%>" size="5" maxlength="3" disabled="disabled"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

  </div>  
</fieldset>


<fieldset id="answersFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.answers" /></legend>
  <div class="fields">

<%
            nb = Integer.parseInt(nbAnswers);
            String inputName = "";
            int j=0;
            for (int i = 0; i < nb; i++) {
                j = i + 1;
                inputName = "answer"+i;
%>

    <div class="field">
      <label for="<%=inputName%>" class="txtlibform"><fmt:message key="QuizzCreationAnswerNb" />&nbsp;<%=(i+1)%></label>
      <div class="champs">
        <textarea name="<%=inputName%>" id="<%=inputName%>" cols="49" rows="3"></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
        <div class="points">
          <input type="text" name="nbPoints<%=i%>" id="nbPoints<%=i%>" value="" size="5" maxlength="3" />&nbsp;<fmt:message key="QuizzNbPoints"/>&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/>
        </div>
      </div>
    </div>
  
    <div class="field">
      <label for="comment<%=i%>" class="txtlibform"><fmt:message key="QuizzCreationAnswerComment" />&nbsp;<%=(i+1)%></label>
      <div class="champs">
        <textarea name="comment<%=i%>" id="comment<%=i%>" cols="49" rows="3"></textarea>
      </div>
    </div>
<%  
  if (!style.equals("list")) {
%>    
    <div class="field fieldImage">
      <label for="image<%= i %>" class="txtlibform"><fmt:message key="QuizzCreationAnswerImage" />&nbsp;<%=(i+1)%></label>
      <div class="champs">
      <div class="thumbnailPreviewAndActions" id="thumbnailPreviewAndActions<%= i %>">
        <div class="thumbnailPreview">
        <img alt="" class="thumbnail" id="thumbnail<%= i %>" src="null" />
        </div>
        <div class="thumbnailActions" id="thumbnailActions<%= i %>">
        <a href="javascript:deleteImage(<%=i%>)"><img title="<fmt:message key="quizz.answer.image.delete"/>" alt="<fmt:message key="quizz.answer.image.delete"/>" src="/silverpeas/util/icons/cross.png" /> <fmt:message key="quizz.answer.image.delete"/></a>
        </div>
        </div>
    
        <div class="thumbnailInputs">
        <img title="<%=resources.getString("survey.answer.image.select")%>" alt="<%=resources.getString("survey.answer.image.select")%>" src="/silverpeas/util/icons/images.png" /> <input type="file" id="thumbnailFile" size="40" name="image<%=i%>" />
        <span class="txtsublibform"> ou </span><input type="hidden" name="valueImageGallery<%= i %>" id="valueImageGallery<%= i %>"/>
         <select class="galleries" name="galleries" onchange="choixGallery(this, '<%= i %>');this.selectedIndex=0;"> 
           <option selected><%= resources.getString("survey.galleries") %></option>
<%
          for (int k = 0; k < galleries.size(); k++) {
            ComponentInstLight gallery = galleries.get(k); %>
             <option value="<%= gallery.getId() %>"><%= gallery.getLabel() %></option> 
<%        } %>
          </select>
        </div>
      </div>
    </div>
<%
            }
          }
%>
    </div>
  </fieldset>
        
<%
      } else {
%>

<fieldset id="questionFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.question" /></legend>
  <div class="fields">
    <div class="field" id="questionArea"> 
      <label for="question" class="txtlibform"><fmt:message key="QuizzCreationQuestion" />&nbsp;<%=questionNb%></label>
      <div class="champs"><textarea name="question" cols="49" rows="3"><%=EncodeHelper.javaStringToHtmlString(question)%></textarea>&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/></div>
    </div>
    <div class="field" id="questionStyleArea">
      <label for="questionStyle" class="txtlibform"><fmt:message key="quizz.style" /></label>
      <div class="champs">
        <select id="questionStyle" name="questionStyle" > 
          <option selected value="null"><fmt:message key="quizz.style" /></option> 
          <option value="radio"><fmt:message key="quizz.radio" /></option> 
          <option value="checkbox"><fmt:message key="quizz.checkbox" /></option> 
          <option value="list"><fmt:message key="quizz.list" /></option> 
        </select>&nbsp;&nbsp;&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="nbAnswersArea">
      <label for="nbAnswers" class="txtlibform"><fmt:message key="QuizzCreationNbAnswers" /></label>
      <div class="champs">
        <input type="text" name="nbAnswers" value="<%=nbAnswers%>" size="5" maxlength="3"/>&nbsp;&nbsp;&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/>
      </div>
    </div>

    <div class="field" id="nbPointsMinArea">
      <label for="nbPointsMin" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMin" /></label>
      <div class="champs">
        <input type="text" name="nbPointsMin" value="<%=nbPointsMin%>" size="5" maxlength="3" />&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

    <div class="field" id="nbPointsMaxArea">
      <label for="nbPointsMax" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMax" /></label>
      <div class="champs">
        <input type="text" name="nbPointsMax" value="<%=nbPointsMax%>" size="5" maxlength="3"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

    <div class="field" id="clueArea">
      <label for="clue" class="txtlibform"><fmt:message key="QuizzClue" /></label>
      <div class="champs">
        <textarea name="clue" cols="49" rows="3"><%=EncodeHelper.javaStringToHtmlString(clue)%></textarea>
      </div>
    </div>

    <div class="field" id="penaltyArea">
      <label for="penalty" class="txtlibform"><fmt:message key="QuizzPenalty" /></label>
      <div class="champs">
        <input type="text" name="penalty" value="<%=penalty%>" size="5" maxlength="3"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

  </div>  
</fieldset>

<%
   }
%>
		<input type="hidden" name="Action" value="<%=nextAction%>"/>
<div class="legend">
  <img border="0" src="<%=mandatoryField%>" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
</div>
      </form>
      <!-- FIN CORPS -->
<%
    out.println(frame.printMiddle());
    out.println("<br><center>"+buttonPane.print());
    out.println(frame.printAfter());
  
    out.println(window.printAfter());
    out.println("</body></html>");
 } //End if action = ViewQuestion
if (action.equals("End")) {
%>
<html>
<head>
<script language="Javascript">
function goToQuizzPreview() {
  document.questionForm.submit();
}
</script>
</head>
<body onload="goToQuizzPreview()">
<form name="questionForm" action="quizzQuestionsNew.jsp" method="post">
<input type="hidden" name="Action" value="PreviewQuizz"/>
</form>
</body>
</html>
<% } %>