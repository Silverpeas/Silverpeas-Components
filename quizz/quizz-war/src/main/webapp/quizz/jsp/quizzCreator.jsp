<%@ page import="org.silverpeas.util.EncodeHelper" %>
<%@ page import="org.silverpeas.util.DBUtil" %>
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

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.questionContainer.model.QuestionContainerDetail" />

<%@ include file="checkQuizz.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.quizz.multilang.quizz"/>

<%
//Retrieve parameter
String action = "";
String quizzId = "";
String title = "";
String description = "";
String creationDate = "";
String beginDate = "";
String endDate = "";
String nbQuestions = "";
String notice="";
String nbAnswersNeeded = "1";
String nbAnswersMax = "1";
String nextAction = "";

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

String space = quizzScc.getSpaceLabel();
String component = quizzScc.getComponentLabel();

Button validateButton = null;
Button cancelButton = null;
//QuestionContainerDetail questionContainerDetail = null;

action = request.getParameter("Action");
quizzId =  request.getParameter("QuizzId");
title = request.getParameter("title");
description = request.getParameter("description");
beginDate = request.getParameter("beginDate");
endDate = request.getParameter("endDate");
nbQuestions = request.getParameter("nbQuestions");
notice= request.getParameter("notice");
nbAnswersNeeded = request.getParameter("nbAnswersNeeded");
nbAnswersMax = request.getParameter("nbAnswersMax");
//Mise a jour de l'espace
if (action == null) {
	action = "CreateQuizz";
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">
function sendData() {
  if (isCorrectForm()) {
    <view:pdcPositions setIn="document.quizzForm.Positions.value"/>;    
    document.quizzForm.submit();
  }
}

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;
  var title = stripInitialWhitespace(document.quizzForm.title.value);
  var nbQuestions = document.quizzForm.nbQuestions.value;
  var notice= document.quizzForm.notice.value;
  var description= document.quizzForm.description.value;
  var nbAnswersNeeded =  document.quizzForm.nbAnswersNeeded.value;
  var nbAnswersMax =  document.quizzForm.nbAnswersMax.value;
  var beginDate = document.quizzForm.beginDate.value;
  var endDate = document.quizzForm.endDate.value;
  
  if (isWhitespace(title)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
  }
  if (isWhitespace(description)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
  }
  else
  {
    if (!isValidTextArea(document.quizzForm.description)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.description")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Caracters")%>\n";
       errorNb++;
    }
  }
  if (!isValidTextArea(document.quizzForm.notice)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNotice")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Caracters")%>\n";
       errorNb++;
  }
  
  if (!isWhitespace(beginDate)) {
     if (!isDateOK(beginDate, '<%=resources.getLanguage()%>')) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
       errorNb++;
     }
  }
  if (!isWhitespace(endDate)) {
   if (!isDateOK(endDate, '<%=resources.getLanguage()%>')) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
           errorNb++;
  } else {
       if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
    	   if (!isDate1AfterDate2(endDate, beginDate, '<%=resources.getLanguage()%>')) {
                    errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationEndDate")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
                    errorNb++;
             }
       }
  }
  }
  if (isWhitespace(nbQuestions)) {
       errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbQuestionPerPage")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
       errorNb++;
  } else {
       if (isInteger(nbQuestions) == false) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbQuestionPerPage")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
           errorNb++;
       } else {
            if (nbQuestions <= 0) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbQuestionPerPage")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
               errorNb++;
            }
       }
  }
  if (!isWhitespace(nbAnswersMax)) {
       if (isInteger(nbAnswersMax) == false) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPossibleAnswer")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
           errorNb++;
       }
       else {
            if (nbAnswersMax <= 0) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbPossibleAnswer")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
               errorNb++;
            }
       }
  }
  if (!isWhitespace(nbAnswersNeeded)) {
      if (isInteger(nbAnswersNeeded) == false) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswerNeeded")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
           errorNb++;
       } else {
            if (nbAnswersNeeded <= 0) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswerNeeded")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
               errorNb++;
            }
            else
            {
                if (Number(nbAnswersNeeded) > Number(nbAnswersMax))
                {
                  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("QuizzCreationNbAnswerNeeded")%>' <%=resources.getString("MustContainsInfNumber")%> '<%=resources.getString("QuizzCreationNbPossibleAnswer")%>'\n";
                  errorNb++;
                }
            }
       }
  }

  <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
  
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

</script>
</head>
<body bgcolor="#FFFFFF">
<%
if (action.equals("CreateQuizz")) {
  cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
  validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
  session.removeAttribute("quizzUnderConstruction");
  quizzScc.setPositions(null);
  title = "";
  creationDate = resources.getOutputDate(new Date());
  beginDate = creationDate;
  endDate = "";
  nbQuestions = "1";
  notice="";
  description="";
  nbAnswersNeeded = "1";
  nbAnswersMax = "1";
  nextAction="SendNewQuizz";

  Window window = gef.getWindow();
  Frame frame =gef.getFrame();
  Board board = gef.getBoard();

  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzCreation"));

  out.println(window.printBefore());
  out.println(frame.printBefore());
%>
<form name="quizzForm" action="quizzCreator.jsp" method="post">
  <input type="hidden" name="Action" value="<%=nextAction%>" />
  <input type="hidden" name="Positions" />

<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.info" /></legend>
  <!-- SAISIE DU QUIZZ -->
  <div class="fields">
    <!-- Forum name -->
    <div class="field" id="titleArea">
      <label class="txtlibform" for="title"><fmt:message key="GML.name" /> </label>
      <div class="champs">
        <input type="text" name="title" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=EncodeHelper.javaStringToHtmlString(title)%>" />&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="nbQuestionsArea">
      <label class="txtlibform" for="nbQuestions"><fmt:message key="QuizzCreationNbQuestionPerPage" /> </label>
      <div class="champs">
        <input type="text" name="nbQuestions" size="5" maxlength="3" value="<%=nbQuestions%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    
    <div class="field" id="nbAnswersMaxArea">
      <label class="txtlibform" for="nbAnswersMax"><fmt:message key="QuizzCreationNbPossibleAnswer" /> </label>
      <div class="champs">
        <input type="text" name="nbAnswersMax" size="5" maxlength="3" value="<%=nbAnswersMax%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    
    <div class="field" id="nbAnswersNeededArea">
      <label class="txtlibform" for="nbAnswersNeeded"><fmt:message key="QuizzCreationNbAnswerNeeded" /> </label>
      <div class="champs">
        <input type="text" name="nbAnswersNeeded" size="5" maxlength="3" value="<%=nbAnswersNeeded%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>

    <div class="field" id="descriptionArea">
      <label class="txtlibform" for="description"><fmt:message key="GML.description" /> </label>
      <div class="champs">
        <textarea name="description" cols="49" rows="3"><%=EncodeHelper.javaStringToHtmlString(description)%></textarea>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>

    <div class="field" id="noticeArea">
      <label class="txtlibform" for="notice"><fmt:message key="QuizzCreationNotice" /> </label>
      <div class="champs">
        <textarea name="notice" cols="49" rows="3"><%=EncodeHelper.javaStringToHtmlString(notice)%></textarea>
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
        <input type="text" class="dateToPick" name="beginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
      </div>
    </div>
    <div class="field" id="endArea">
      <label for="endDate" class="txtlibform"><fmt:message key="QuizzCreationEndDate" /></label>
      <div class="champs">
        <input type="text" class="dateToPick" name="endDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
      </div>
    </div>
  </div>  
</fieldset>

  <view:pdcNewContentClassification componentId="<%=quizzScc.getComponentId()%>" />

<div class="legend">
  <img border="0" src="<%=mandatoryField%>" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
</div>
 
</form>

<%

  out.println(frame.printMiddle());
%>
<br/>
<center>
<%
  ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(validateButton);
  buttonPane.addButton(cancelButton);
  buttonPane.setHorizontalPosition();
  out.println(buttonPane.print());
%>
</center>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
  out.println("</body></html>");
 } //End if action = ViewQuestion
if (action.equals("SendNewQuizz")) {
%>
<html>
<head>
<script language="Javascript">
function goToQuestionCreator() {
  document.questionForm.submit();
}
</script>
</head>
<body onload="goToQuestionCreator()">
<form name="questionForm" action="questionCreator.jsp" method="post" enctype="multipart/form-data">
<input type="hidden" name="Action" value="FirstQuestion"/>
</form>
</body>
</html>
<% } %>