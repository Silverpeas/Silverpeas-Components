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

<jsp:useBean id="questionsVector" scope="session" class="java.util.ArrayList" />

<%@ include file="checkQuizz.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.stratelia.webactiv.quizz.multilang.quizz"/>
<%
String nextAction = "";

String m_context = GeneralPropertiesManager.getString("ApplicationURL");

int nbZone = 4; // nombre de zones � contr�ler
List<ComponentInstLight> galleries = quizzScc.getGalleries();
if (galleries != null) {
	nbZone = nbZone + 2;
}

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
String ligne = m_context + "/util/icons/colorPix/1px.gif";

ResourceLocator quizzSettings = quizzScc.getSettings();

Button validateButton = null;
Button cancelButton = null;
Button finishButton = null;
ButtonPane buttonPane = null;

List<FileItem> items = FileUploadUtil.parseRequest(request);
String action = FileUploadUtil.getOldParameter(items, "Action", "");
String question = FileUploadUtil.getOldParameter(items, "question", "");
String clue =  FileUploadUtil.getOldParameter(items, "clue", "");
String penalty = FileUploadUtil.getOldParameter(items, "penalty", "");
String nbPointsMin = FileUploadUtil.getOldParameter(items, "nbPointsMin", "");
String nbPointsMax = FileUploadUtil.getOldParameter(items, "nbPointsMax", "");
String nbAnswers = FileUploadUtil.getOldParameter(items, "nbAnswers", "");
String style = FileUploadUtil.getOldParameter(items, "questionStyle", "");
boolean file = false;
int nb = 0;
int attachmentSuffix = 0;
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
    if ((!isWhitespace(comment)) && (!isValidTextArea($("#comment"+i)))){
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
if (action.equals("SendNewQuestion")) {
  	  List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
      int questionNb = questionsV.size() + 1;
      int penaltyInt=0;
      int nbPointsMinInt=-1000;
      int nbPointsMaxInt=1000;
      if (!penalty.equals(""))
        penaltyInt=new Integer(penalty).intValue();
      if (!nbPointsMin.equals(""))
        nbPointsMinInt=new Integer(nbPointsMin).intValue();
      if (!nbPointsMax.equals(""))
        nbPointsMaxInt=new Integer(nbPointsMax).intValue();
      Question questionObject = new Question(null, null, question, null, clue, null, 0, style,penaltyInt,0,questionNb, nbPointsMinInt, nbPointsMaxInt);

      questionObject.setAnswers(answers);
      questionsV.add(questionObject);
      session.setAttribute("questionsVector", questionsV);
} //End if action = ViewResult
else if (action.equals("End")) {
      out.println("<body>");
      QuestionContainerDetail quizzDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
      //Vector 2 Collection
      List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
      quizzDetail.setQuestions(questionsV);
      out.println("</body></html>");
}
if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm"))) {
      out.println("<body>");
      List<Question> questionsV = (List<Question>) session.getAttribute("questionsVector");
      int questionNb = questionsV.size() + 1;
      cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
      buttonPane = gef.getButtonPane();
      if (action.equals("CreateQuestion")) {
            validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
            question = "";
            nbAnswers = "";
            penalty = "";
            clue = "";
            nbPointsMin ="";
            nbPointsMax ="";
            nextAction="SendQuestionForm";
            buttonPane.addButton(validateButton);
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
      Frame frame=gef.getFrame();
      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(quizzScc.getSpaceLabel());
      browseBar.setComponentName(quizzScc.getComponentLabel());
      browseBar.setExtraInformation(resources.getString("QuestionAdd"));

      out.println(window.printBefore());


      Board board = gef.getBoard();
%>
      <!--DEBUT CORPS -->
      <form name="quizzForm" action="questionCreatorBis.jsp" method="post" enctype="multipart/form-data">
<%         if (action.equals("SendQuestionForm")) { %>



<fieldset id="questionFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.question" /></legend>
  <div class="fields">
    <div class="field" id="questionArea"> 
      <label for="question" class="txtlibform"><fmt:message key="QuizzCreationQuestion" />&nbsp;<%=questionNb%></label>
      <div class="champs"><textarea name="question" cols="49" rows="3" readonly="readonly"><%=EncodeHelper.javaStringToHtmlString(question)%></textarea>&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/></div>
    </div>
    <div class="field" id="questionStyleArea">
      <label for="questionStyle" class="txtlibform"><fmt:message key="quizz.style" /></label>
      <div class="champs"><%=resources.getString("quizz."+style) %>
      </div>
    </div>
    <div class="field" id="nbAnswersArea">
      <label for="nbAnswers" class="txtlibform"><fmt:message key="QuizzCreationNbAnswers" /></label>
      <div class="champs">
        <input type="text" name="nbAnswers" value="<%=nbAnswers%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;&nbsp;&nbsp;<img border="0" src="<%=mandatoryField %>" width="5" height="5"/>
      </div>
    </div>

    <div class="field" id="nbPointsMinArea">
      <label for="nbPointsMin" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMin" /></label>
      <div class="champs">
        <input type="text" name="nbPointsMin" value="<%=nbPointsMin%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

    <div class="field" id="nbPointsMaxArea">
      <label for="nbPointsMax" class="txtlibform"><fmt:message key="QuizzCreationNbPointsMax" /></label>
      <div class="champs">
        <input type="text" name="nbPointsMax" value="<%=nbPointsMax%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
      </div>
    </div>

    <div class="field" id="clueArea">
      <label for="clue" class="txtlibform"><fmt:message key="QuizzClue" /></label>
      <div class="champs">
        <textarea name="clue" cols="49" rows="3" readonly="readonly"><%=EncodeHelper.javaStringToHtmlString(clue)%></textarea>
      </div>
    </div>

    <div class="field" id="penaltyArea">
      <label for="penalty" class="txtlibform"><fmt:message key="QuizzPenalty" /></label>
      <div class="champs">
        <input type="text" name="penalty" value="<%=penalty%>" size="5" maxlength="3" readonly="readonly"/>&nbsp;<%=resources.getString("QuizzNbPoints")%>
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
          <%if (galleries != null) {%>
        <span class="txtsublibform"> ou </span><input type="hidden" name="valueImageGallery<%= i %>" id="valueImageGallery<%= i %>"/>
         <select class="galleries" name="galleries" onchange="choixGallery(this, '<%= i %>');this.selectedIndex=0;"> 
           <option selected><%= resources.getString("survey.galleries") %></option>
<%
          for (ComponentInstLight gallery : galleries) { %>
             <option value="<%= gallery.getId() %>"><%= gallery.getLabel() %></option> 
<%        }
        } %>
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
              } else { %>


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

<div class="legend">
  <img border="0" src="<%=mandatoryField%>" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
</div>

    <input type="hidden" name="Action" value="<%=nextAction%>"/>
      </form>
      <!-- FIN CORPS -->
<%
      out.println(buttonPane.print());
      out.println(window.printAfter());
%>
</body></html>
<%
 } //End if action = ViewQuestion
if (action.equals("SendNewQuestion")) {
%>
<html>
<head>
<script language="Javascript">
    function goToQuestionsUpdate() {
        document.questionForm.submit();
    }
</script>
</head>
<body onload="goToQuestionsUpdate()">
<form name="questionForm" action="questionsUpdate.jsp" method="post">
<input type="hidden" name="Action" value="UpdateQuestions" />
</form>
</body>
</html>
<% } %>