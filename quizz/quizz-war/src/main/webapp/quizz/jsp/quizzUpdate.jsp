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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />
<jsp:useBean id="questionsVector" scope="session" class="java.util.Vector" />
<jsp:useBean id="questionsResponses" scope="session" class="java.util.Hashtable" />

<%@ include file="checkQuizz.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.stratelia.webactiv.quizz.multilang.quizz"/>

<%
//Retrieve parameters
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


action = (String) request.getParameter("Action");
quizzId = (String) request.getParameter("QuizzId");
title = (String) request.getParameter("title");
description = (String) request.getParameter("description");
beginDate = (String) request.getParameter("beginDate");
endDate = (String) request.getParameter("endDate");
nbQuestions = (String) request.getParameter("nbQuestions");
notice=(String) request.getParameter("notice");
nbAnswersNeeded = (String) request.getParameter("nbAnswersNeeded");
nbAnswersMax = (String) request.getParameter("nbAnswersMax");


String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

ResourceLocator settings = quizzScc.getSettings();

QuestionContainerDetail quizz = null;
if (action.equals("SendQuizzHeader")) {
    if (beginDate != null) {
        if (beginDate.length()>0)
          beginDate = resources.getDBDate(beginDate);
        else
          beginDate = null;
    }
    if (endDate != null) {
        if (endDate.length()>0)
          endDate = resources.getDBDate(endDate);
        else
          endDate = null;
    }

  /*if(nbAnswersMax == null || nbAnswersMax.trim().length() == 0) {
		nbAnswersMax = "0";
  }

  if(nbAnswersNeeded == null || nbAnswersNeeded.trim().length() == 0) {
		nbAnswersNeeded = "0";
  } */

    QuestionContainerHeader quizzHeader = new QuestionContainerHeader(null, title, description,notice, null, null, beginDate, endDate, false, 0, Integer.parseInt(nbQuestions), Integer.parseInt(nbAnswersMax), Integer.parseInt(nbAnswersNeeded),0, QuestionContainerHeader.IMMEDIATE_RESULTS, QuestionContainerHeader.TWICE_DISPLAY_RESULTS);
    quizzScc.updateQuizzHeader(quizzHeader, quizzId);
%>
<jsp:forward page="<%=quizzScc.getComponentUrl()+\"Main.jsp\"%>"/>
<%
      return;
}
if (action.equals("UpdateQuizzHeader")) {
  quizz = quizzScc.getQuizzDetail(quizzId);
  QuestionContainerHeader quizzHeader = quizz.getHeader();
  title = EncodeHelper.javaStringToHtmlString(quizzHeader.getTitle());
  description = quizzHeader.getDescription();
  notice = quizzHeader.getComment();
  creationDate = resources.getOutputDate(quizzHeader.getCreationDate());
  beginDate = "";
  if (quizzHeader.getBeginDate() != null) {
      beginDate = resources.getInputDate(quizzHeader.getBeginDate());
  }
  endDate = "";
  if (quizzHeader.getEndDate() != null) {
      endDate = resources.getInputDate(quizzHeader.getEndDate());
  }
  nbQuestions = Integer.toString(quizzHeader.getNbQuestionsPerPage());

	nbAnswersNeeded = Integer.toString(quizzHeader.getNbParticipationsBeforeSolution());
	nbAnswersMax = Integer.toString(quizzHeader.getNbMaxParticipations());
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function sendData() {
  if (isCorrectForm()) {
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
<body>
<%
  Window window = gef.getWindow();

  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(quizzScc.getSpaceLabel());
  browseBar.setComponentName(quizzScc.getComponentLabel());
  browseBar.setExtraInformation(resources.getString("QuizzUpdate"));

  out.println(window.printBefore());

  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resources.getString("GML.head"), "quizzUpdate.jsp?Action=UpdateQuizzHeader&QuizzId="+quizzId, action.equals("UpdateQuizzHeader"), false);
  tabbedPane.addTab(resources.getString("QuizzQuestions"), "questionsUpdate.jsp?Action=UpdateQuestions&QuizzId="+quizzId, action.equals("UpdateQuestions"), true);
  out.println(tabbedPane.print());
  Frame frame = gef.getFrame();
  Board board = gef.getBoard();

  out.println(frame.printBefore());
%>

<form name="quizzForm" action="quizzUpdate.jsp" method="post">

<input type="hidden" name="Action" value="SendQuizzHeader"/>
<input type="hidden" name="QuizzId" value="<%=quizzId%>"/>

<fieldset id="infoFieldset" class="skinFieldset">
  <legend><fmt:message key="quizz.header.fieldset.info" /></legend>
  <!-- SAISIE DU QUIZZ -->
  <div class="fields">
    <!-- Forum name -->
    <div class="field" id="titleArea">
      <label class="txtlibform" for="title"><fmt:message key="GML.name" /> </label>
      <div class="champs">
        <input type="text" name="title" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="<%=title%>" />&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
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
    <div class="field" id="creationArea">
      <label for="creationDate" class="txtlibform"><fmt:message key="QuizzCreationDate" /></label>
      <div class="champs"><%=creationDate%></div>
    </div>
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

</form>

<%
  out.println(frame.printMiddle());
%>
  <view:pdcClassification componentId="<%= quizzScc.getComponentId() %>" contentId="<%= quizzId %>" editable="true" />
<div class="legend">
  <img border="0" src="<%=mandatoryField%>" width="5" height="5"/> : <fmt:message key="GML.requiredField"/>
</div>

<%
  Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
  Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
  ButtonPane buttonPane = gef.getButtonPane();
  buttonPane.addButton(validateButton);
  buttonPane.addButton(cancelButton);
  buttonPane.setHorizontalPosition();
  out.println("<br><table width=\"100%\"><tr><td align=center>"+buttonPane.print()+"</td></tr></table>");
	out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>
<% } %>