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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="com.stratelia.webactiv.survey.control.FileHelper" %>
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
<c:set var="suggestion" value="${requestScope['Suggestion']}" />
<c:set var="questions" value="${requestScope['Questions']}" />
<c:set var="style" value="${requestScope['Style']}" />

<%
  String action = "";
  String question = "";
  String nbAnswers = "";
  String answerInput = "";
  String suggestionAllowed = "";
  String suggestionCheck = "";
  String suggestion = "";
  //String qcmCheck = "";
  //String qcm = "0";
  //String openQuestionCheck = "";
  //String openQuestion = "0";
  String nextAction = "";
  String style = "";

  String m_context =
      GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

  //Icons
  String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
  String px = m_context + "/util/icons/colorPix/1px.gif";

  ResourceLocator surveySettings =
      new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", surveyScc.getLanguage());

  String nbMaxAnswers = surveySettings.getString("NbMaxAnswers");

  Button validateButton = null;
  Button cancelButton = null;
  Button finishButton = null;
  ButtonPane buttonPane = null;

  File dir = null;
  String logicalName = null;
  String type = null;
  String physicalName = null;
  String mimeType = null;
  boolean file = false;
  long size = 0;
  int nb = 0;
  int attachmentSuffix = 0;
  ArrayList imageList = new ArrayList();
  ArrayList answers = new ArrayList();
  Answer answer = null;
  List items = FileUploadUtil.parseRequest(request);
  Iterator itemIter = items.iterator();
  while (itemIter.hasNext()) {
    FileItem item = (FileItem) itemIter.next();
    if (item.isFormField()) {
      String mpName = item.getFieldName();
      if ("Action".equals(mpName)) {
        action = item.getString();
      } else if ("question".equals(mpName)) {
        question = item.getString(FileUploadUtil.DEFAULT_ENCODING);
      } else if ("nbAnswers".equals(mpName)) {
        nbAnswers = item.getString(FileUploadUtil.DEFAULT_ENCODING);
      } else if ("SuggestionAllowed".equals(mpName)) {
        suggestion = item.getString(FileUploadUtil.DEFAULT_ENCODING);
      } else if ("questionStyle".equals(mpName)) {
        style = item.getString(FileUploadUtil.DEFAULT_ENCODING);
      } else if (mpName.startsWith("answer")) {
        answerInput = item.getString(FileUploadUtil.DEFAULT_ENCODING);
        answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, false, null);
        answers.add(answer);
      } else if ("suggestionLabel".equals(mpName)) {
        answerInput = item.getString(FileUploadUtil.DEFAULT_ENCODING);
        answer = new Answer(null, null, answerInput, 0, 0, false, "", 0, true, null);
        answers.add(answer);
      } else if (mpName.startsWith("valueImageGallery")) {
        if (StringUtil.isDefined(item.getString(FileUploadUtil.DEFAULT_ENCODING))) {
          // traiter les images venant de la gallery si pas d'image externe
          if (!file){
            answer.setImage(item.getString(FileUploadUtil.DEFAULT_ENCODING));
          }
        }
      }
      //String value = paramPart.getStringValue();
    } else {
      // it's a file part
      if (FileHelper.isCorrectFile(item)) {
        // the part actually contained a file
        logicalName = item.getName();
        type = logicalName.substring(logicalName.indexOf(".") + 1, logicalName.length());
        physicalName =
            new Long(new Date().getTime()).toString() + attachmentSuffix + "." + type;
        attachmentSuffix = attachmentSuffix + 1;
        mimeType = item.getContentType();
        dir =
            new File(FileRepositoryManager.getAbsolutePath(surveyScc.getComponentId()) +
                surveySettings.getString("imagesSubDirectory") + File.separator + physicalName);
        FileUploadUtil.saveToFile(dir, item);
        size = item.getSize();
        if (size > 0) {
          answer.setImage(physicalName);
          file = true;
        }
      } else {
        // the field did not contain a file
        file = false;
      }
      out.flush();
    }
  }
%>
<html>
<head>
  <title></title>
<view:looknfeel />
<script type="text/javascript" src="<c:out value="${ctxPath}" />/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<c:out value="${ctxPath}" />/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<c:out value="${ctxPath}" />/util/javaScript/checkForm.js"></script>
<script type="text/javascript">

function sendData() {
    if (isCorrectForm()) {
        if (checkAnswers()) {
            if (document.surveyForm.suggestion.checked) {
                document.surveyForm.SuggestionAllowed.value = "1";
            }
            document.surveyForm.submit();
        }
    }
}

function checkAnswers() {
     var errorMsg = "";
     var errorNb = 0;
     var answerEmpty = false;
     var imageEmpty = false;
     var fieldsEmpty = "";
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
 <c:if test="${suggestion != '0' && action == 'SendQuestionForm'}">
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
            <c:if test="${suggestion != '0' && action == 'SendQuestionForm'}">
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
	     } else {
	          document.surveyForm.Action.value = "SendNewQuestion";
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
		if (!galleryWindow.closed && galleryWindow.name=="galleryWindow")
			galleryWindow.close();
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
	var newLink = document.createElement("a");
	newLink.setAttribute("href", url);
	newLink.setAttribute("target", "_blank");

	var newLabel = document.createTextNode("<fmt:message key="survey.imageGallery"/>");
	newLink.appendChild(newLabel);

	var removeLink =  document.createElement("a");
	removeLink.setAttribute("href", "javascript:deleteImage('"+currentAnswer+"')");
	var removeIcon = document.createElement("img");
	removeIcon.setAttribute("src", "icons/questionDelete.gif");
	removeIcon.setAttribute("border", "0");
	removeIcon.setAttribute("align", "absmiddle");
	removeIcon.setAttribute("alt", "<fmt:message key="GML.delete"/>");
	removeIcon.setAttribute("title", "<fmt:message key="GML.delete"/>");

	removeLink.appendChild(removeIcon);

	document.getElementById('imageGallery'+currentAnswer).appendChild(newLink);
	document.getElementById('imageGallery'+currentAnswer).appendChild(removeLink);

	document.getElementById('valueImageGallery'+currentAnswer).value = url;
}

function showQuestionOptions(value)
{
  if (value != "open" && value!= "null") {
    $("#trNbQuestions").show();
    $("#trSuggestion").show();
  } else {
    $("#trNbQuestions").hide();
    $("#trSuggestion").hide();
  }
}

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

</script>
</head>
Style=<c:out value="${style}"/><br/>
NbAnswers= <c:out value="${requestScope['NbAnswers']}" /><br/>
Suggestion= <c:out value="${suggestion}" />
<c:choose>
  <c:when test="${action == 'SendNewQuestion'}">
  
  </c:when>
  <c:when test="${action == 'End'}">
<body>
</body>
  </c:when>
  <c:when test="${action == 'CreateQuestion' || action == 'SendQuestionForm' || ation == 'UpdateQuestion'}">
    
    <c:set var="nbQuestion" value="1" />
    <c:set var="questionLabel" value=""/>
    <c:set var="nextAction" value="SendQuestionForm" />
    <c:choose>
      <c:when test="${action == 'CreateQuestion'}">
        <c:set var="nbQuestion" value="${fn:length(questions) + 1}" />
      </c:when>
      <c:when test="${action == 'SendQuestionForm'}">
        <c:set var="nbQuestion" value="${fn:length(questions) + 1}" />
        <c:set var="nextAction" value="SendNewQuestion" />
      </c:when>
      <c:when test="${action == 'UpdateQuestion'}">
        <c:set var="nbQuestion" value="${requestScope['QuestionId'] + 1}" />
      </c:when>
    </c:choose>

<%-- ADD browsebar --%>  
<fmt:message key="QuestionAdd" var="questionAddLabel" />
<view:browseBar extraInformations="${questionAddLabel}">
</view:browseBar>
  <view:window>
    <view:frame>
   
   
   
   <center>
  <view:board>
      <!--DEBUT CORPS -->
      <form name="surveyForm" action="questionCreatorBis.jsp" method="post" enctype="multipart/form-data">
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr><td class="txtlibform" width="30%"><fmt:message key="SurveyCreationQuestion"/> <c:out value="${nbQuestion}" /> :</td>
          <td width="70%">
            <input type="text" name="question" value="<view:encodeHtml string="<%=question%>" />" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
          </td>
        </tr>
    <%
      String inputName = "";
            %>
  <c:choose>
    <c:when test="${action == 'SendQuestionForm'}">
      <c:choose>
        <c:when test="${not(style == 'open')}">
        
          <c:set var="nbAnswer" value="${requestScope['NbAnswers']}"/>
        <tr>
          <td class="txtlibform" valign="top"><fmt:message key="survey.style" /> :</td>
          <td>
            <c:set var="mykey" value="survey.${style}"></c:set>
            <fmt:message key="${mykey}" />
            <select style="visibility: hidden;" id="questionStyle" name="questionStyle">
              <option selected><c:out value="style"/></option>
            </select>
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="SurveyCreationNbPossibleAnswer" /> :</td>
          <td>
            <input type="text" name="nbAnswers" value="<c:out value="${nbAnswer}"/>" size="3" disabled="disabled" />
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="SuggestionAllowed" /> :</td>
          <td><input type="checkbox" name="suggestion" value="" <%=suggestionCheck%> disabled /></td>
        </tr>
  
  <c:forEach var="cptAnswer" begin="0" end="${nbAnswer - 1}" step="1" varStatus="index">
    <c:set var="inputName" value="answer${cptAnswer}" />

        <tr>
          <td colspan="2" align="center">
            <table cellpadding="0" cellspacing="5" width="100%">
              <tr>
                <td class="intfdcolor"><img src="<%= px%>" border="0"></td>
              </tr>
        </table>
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerNb" />&nbsp;<c:out value="${cptAnswer + 1}"/> :</td>
          <td><input type="text" name="<c:out value="${inputName}"/>" value="" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>"></td>
        </tr>
    <c:if test="${not (style == 'list')}">
        <tr>
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerImage"/>&nbsp;<c:out value="${cptAnswer + 1}"/> :</td>
          <td><input type="file" name="image<c:out value="${cptAnswer}"/>" size="60"></td>
        </tr>

        <!-- zone to display link on image -->
        <tr>
          <td></td>
          <td><span id="imageGallery<c:out value="${cptAnswer}"/>"></span>
            <input type="hidden" id="valueImageGallery<c:out value="${cptAnswer}"/>" name="valueImageGallery<c:out value="${cptAnswer}"/>" />
            <c:set var="gallery" value="${requestScope['Gallery']}" />
            <c:if test="${not empty(gallery)}">
              <select id="galleries" name="galleries" onchange="choixGallery(this, '<c:out value="${cptAnswer}"/>');this.selectedIndex=0;">
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

      <c:if test="${not (suggestion == '0')}">
        <tr>
              <td colspan="2" align="center">
                <table cellpadding="0" cellspacing="5" width="100%">
                  <tr>
                    <td class="intfdcolor"><img src="<%=px%>" border="0"></td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td class="txtlibform"><fmt:message key="OtherAnswer"/>&nbsp;:</td>
              <td>
                <input type="text" name="suggestionLabel" id="suggestionId" value="<fmt:message key="SurveyCreationDefaultSuggestionLabel" />" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>" />
              </td>
            </tr>
      </c:if>        
        </c:when>
        <c:otherwise>
            <input type="hidden" name="style" value="<c:out value="${style}"/>" />
        </c:otherwise>
      </c:choose>

          <tr>
            <td>(<img border="0" src="<%= mandatoryField%>" width="5" height="5">&nbsp;:&nbsp;<fmt:message key="GML.requiredField"/>)</td>
          </tr>

    </c:when>
    <c:otherwise>
    <%-- create a new question form here --%>
      <tr>
            <td class="txtlibform" valign="top"><fmt:message key="survey.style" /> :</td>
            <td>
              <select id="questionStyle" name="questionStyle" onchange="showQuestionOptions(this.value);">
                <option selected value="null"><fmt:message key="survey.style" /></option>
            <option value="open"><fmt:message key="survey.open" /></option>
            <option value="radio"><fmt:message key="survey.radio" /></option>
            <option value="checkbox"><fmt:message key="survey.checkbox"/></option>
            <option value="list"><fmt:message key="survey.list"/></option>
          </select>
        </td>
          </tr>
      <tr id="trNbQuestions" style="display:none;">
            <td class="txtlibform"><fmt:message key="SurveyCreationNbPossibleAnswer" /> :</td>
            <td><input type="text" name="nbAnswers" value="<%=nbAnswers %>" size="3" maxlength="2">&nbsp;<img border=0 src="<%=mandatoryField%>" width="5" height="5"></td>
          </tr>
          <tr id="trSuggestion" style="display:none;">
            <td class="txtlibform"><fmt:message key="SuggestionAllowed"/> :</td>
            <td><input type="checkbox" name="suggestion" value="" <%=suggestionCheck%>></td>
          </tr>
      <tr>
            <td><input type="hidden" name="answer0"></td>
          </tr>
          <tr>
            <td>(<img border="0" src="<%= mandatoryField%>" width="5" height="5">&nbsp;:&nbsp;<fmt:message key="GML.requiredField"/>)</td>
          </tr>    
    
    </c:otherwise>
  </c:choose>

        <tr>
          <td>
            <input type="hidden" name="Action" value="<c:out value="${nextAction}" />">
            <input type="hidden" name="SuggestionAllowed" value="0">
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
    <view:button label="${cancelButtonLabel}" action="javascript:onClick=history.back();" disabled="false"></view:button>
    <view:button label="${validateButtonLabel}" action="javascript:sendData();" disabled="false"></view:button>
  </view:buttonPane>
</center>
   
   
    
    </view:frame>
  </view:window>
<body>


</body>  
  
  
  </c:when>
  
</c:choose>
</html>
<%
  if (action.equals("SendNewQuestion")) {
    Question questionObject = new Question(null, null, question, "", "", null, style, 0);
    questionObject.setAnswers(answers);
    List questionsV = surveyScc.getSessionQuestions();
    questionsV.add(questionObject);
    surveyScc.setSessionQuestions(questionsV);
  } //End if action = ViewResult
  else if (action.equals("End")) {
    out.println("<body>");
    QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
    //Vector 2 Collection
    List questionsV = surveyScc.getSessionQuestions();
    surveyDetail.setQuestions(questionsV);
    out.println("</body></html>");
  }
  if ((action.equals("CreateQuestion")) || (action.equals("SendQuestionForm")) || "UpdateQuestion".equals(action)) {
    out.println("<body>");
    List questionsV = surveyScc.getSessionQuestions();
    int questionNb = questionsV.size() + 1;
    cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=history.back();", false);
    buttonPane = gef.getButtonPane();
    if (action.equals("CreateQuestion")) {
      validateButton =
          (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
      question = "";
      nbAnswers = "";
      suggestion = "";
      nextAction = "SendQuestionForm";
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
      buttonPane.setHorizontalPosition();
    } else if (action.equals("SendQuestionForm")) {
      validateButton =
          (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
      if (suggestion.equals("0")) {
        suggestionCheck = "";
      } else {
        suggestionCheck = "checked";
      }
      nextAction = "SendNewQuestion";
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
      buttonPane.setHorizontalPosition();
    }

    Window window = gef.getWindow();
    Frame frame = gef.getFrame();

    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(surveyScc.getSpaceLabel());
    browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp?Action=View");
    browseBar.setExtraInformation(resources.getString("QuestionAdd"));

    out.println(window.printBefore());
    out.println(frame.printBefore());
%>
<center>
  <view:board>
      <!--DEBUT CORPS -->
      <form name="surveyForm" action="questionCreatorBis.jsp" method="post" enctype="multipart/form-data">
      <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr><td class="txtlibform" width="30%"><fmt:message key="SurveyCreationQuestion"/> <%=questionNb%> :</td>
          <td width="70%">
            <input type="text" name="question" value="<view:encodeHtml string="<%=question%>" />" size="60" maxlength="<%=DBUtil.getTextFieldLength()%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
          </td>
        </tr>
		<%
		  String inputName = "";
		    if (action.equals("SendQuestionForm")) {
		      if (!style.equals("open")) {
            %>
        <tr>
          <td class="txtlibform" valign="top"><fmt:message key="survey.style" /> :</td>
          <td>
            <c:set var="mykey">survey.<%=style%></c:set>
            <fmt:message key="${mykey}" />
            <select style="visibility: hidden;" id="questionStyle" name="questionStyle">
              <option selected><%=style%></option>
            </select>
		  </td>
        </tr>
		<tr>
          <td class="txtlibform"><fmt:message key="SurveyCreationNbPossibleAnswer" /> :</td>
          <td>
            <input type="text" name="nbAnswers" value="<%=nbAnswers %>" size="3" disabled="disabled" />
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="SuggestionAllowed" /> :</td>
          <td><input type="checkbox" name="suggestion" value="" <%=suggestionCheck%> disabled /></td>
        </tr>
<%
		        nb = Integer.parseInt(nbAnswers);
		        inputName = "";
		        int j = 0;
		        for (int i = 0; i < nb; i++) {
		          j = i + 1;
		          inputName = "answer" + i;
              %>
        <tr>
          <td colspan="2" align="center">
            <table cellpadding="0" cellspacing="5" width="100%">
              <tr>
                <td class="intfdcolor"><img src="<%= px%>" border="0"></td>
              </tr>
		    </table>
          </td>
        </tr>
        <tr>
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerNb" />&nbsp;<%= j%> :</td>
          <td><input type="text" name="<%=inputName %>" value="" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>"></td>
        </tr>
             <%
		          if (!style.equals("list")) {
             %>
		<tr>
          <td class="txtlibform"><fmt:message key="SurveyCreationAnswerImage"/>&nbsp;<%= j%> :</td>
          <td><input type="file" name="image<%=i%>" size="60"></td>
        </tr>

        <!-- zone to display link on image -->
        <tr>
          <td></td>
          <td><span id="imageGallery<%=i%>"></span>
            <input type="hidden" id="valueImageGallery<%=i %>" name="valueImageGallery<%=i %>" />
            <c:set var="gallery" value="${requestScope['Gallery']}" />
            <c:if test="${not empty(gallery)}">
              <select id="galleries" name="galleries" onchange="choixGallery(this, '<%=i %>');this.selectedIndex=0;">
                <option selected><fmt:message key="survey.galleries" /></option>
              <c:forEach var="curGal" items="${gallery}" varStatus="galIndex">
                 <option value="<c:out value="${curGal.id}"/>"><c:out value="${curGal.label}"/></option> 
              </c:forEach>
              </select>
            </c:if>
          </td>
        </tr>
                <% 
		          }
		        }
                %>
      <c:if test="${not (suggestion == '0')}">
		    <tr>
              <td colspan="2" align="center">
                <table cellpadding="0" cellspacing="5" width="100%">
                  <tr>
                    <td class="intfdcolor"><img src="<%=px%>" border="0"></td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td class="txtlibform"><fmt:message key="OtherAnswer"/>&nbsp;:</td>
              <td>
                <input type="text" name="suggestionLabel" id="suggestionId" value="<fmt:message key="SurveyCreationDefaultSuggestionLabel" />" size="60" maxlength="<%=DBUtil.getTextFieldLength() %>" />
              </td>
            </tr>
      </c:if>
          <%            
		      } else {
		        out.println("<input type=\"hidden\" name=\"style\" value=" + style + " >");
		      }
          %>
          <tr>
            <td>(<img border="0" src="<%= mandatoryField%>" width="5" height="5">&nbsp;:&nbsp;<fmt:message key="GML.requiredField"/>)</td>
          </tr>
           <%
		    } else {
		      // liste déroulante des choix possible
          %>
		  <tr>
            <td class="txtlibform" valign="top"><fmt:message key="survey.style" /> :</td>
            <td>
              <select id="questionStyle" name="questionStyle" onchange="javascript:showQuestionOptions(this.value);">
                <option selected value="null"><fmt:message key="survey.style" /></option>
		      	<option value="open"><fmt:message key="survey.open" /></option>
		      	<option value="radio"><fmt:message key="survey.radio" /></option>
		        <option value="checkbox"><fmt:message key="survey.checkbox"/></option>
		        <option value="list"><fmt:message key="survey.list"/></option>
		      </select>
		    </td>
          </tr>
		  <tr id="trNbQuestions" style="display:none;">
            <td class="txtlibform"><fmt:message key="SurveyCreationNbPossibleAnswer" /> :</td>
            <td><input type="text" name="nbAnswers" value="<%=nbAnswers %>" size="3" maxlength="2">&nbsp;<img border=0 src="<%=mandatoryField%>" width="5" height="5"></td>
          </tr>
          <tr id="trSuggestion" style="display:none;">
            <td class="txtlibform"><fmt:message key="SuggestionAllowed"/> :</td>
            <td><input type="checkbox" name="suggestion" value="" <%=suggestionCheck%>></td>
          </tr>
		  <tr>
            <td><input type="hidden" name="answer0"></td>
          </tr>
          <tr>
            <td>(<img border="0" src="<%= mandatoryField%>" width="5" height="5">&nbsp;:&nbsp;<fmt:message key="GML.requiredField"/>)</td>
          </tr>
              <%
		    }
		%>
        <tr>
          <td>
            <input type="hidden" name="Action" value="<%=nextAction%>">
            <input type="hidden" name="SuggestionAllowed" value="0">
          </td>
        </tr>
      </table>
      </form>
      <!-- FIN CORPS -->
    </view:board>
</center>

<%
    out.println(frame.printMiddle());
    out.println("<br><center>" + buttonPane.print() + "</center>");
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>

<div id="dialogId" title="">

</div>
</body>
</html>

<%    
  } //End if action = ViewQuestion
%>