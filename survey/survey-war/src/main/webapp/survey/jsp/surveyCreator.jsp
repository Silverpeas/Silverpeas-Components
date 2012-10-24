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
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkSurvey.jsp" %>

<%
//Retrieve parameter
String creationDate = "";
String nextAction = "";

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button validateButton = null;
Button cancelButton = null;
QuestionContainerDetail survey = null;

String action = request.getParameter("Action");
String surveyId = request.getParameter("SurveyId");
String title = request.getParameter("title");
String description = request.getParameter("description");
String beginDate = request.getParameter("beginDate");
String endDate = request.getParameter("endDate");
String nbQuestions = request.getParameter("nbQuestions");
String anonymousString = request.getParameter("anonymous");

//Anonymous mode -> force anonymous mode for each survey
if(surveyScc.isAnonymousModeEnabled()) {
	anonymousString = "true";
}

boolean anonymous = StringUtil.isDefined(anonymousString) && "true".equalsIgnoreCase(anonymousString);

//Mise a jour de l'espace
if (action == null) {
    action = "CreateSurvey";
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
<script type="text/javascript" language="javascript">
function sendData() {
  if (isCorrectForm()) {
    document.surveyForm.anonymous.disabled = false;
    <view:pdcPositions setIn="document.surveyForm.Positions.value"/>;
    //alert("Positions value before submit form=" + document.surveyForm.Positions.value);
    document.surveyForm.submit();
  }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.surveyForm.title.value);
     var description = document.surveyForm.description.value;
     var nbQuestions = document.surveyForm.nbQuestions.value;
     var beginDate = document.surveyForm.beginDate.value;
     var endDate = document.surveyForm.endDate.value;
     var beginDateOK = true;

     if (isWhitespace(title)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     if (!isValidTextArea(document.surveyForm.description)) {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationDescription")%>' <%=resources.getString("ContainsTooLargeText")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Characters")%>\n";
          errorNb++;
     }
     if (!isWhitespace(beginDate)) {
     	if (!isDateOK(beginDate, '<%=resources.getLanguage()%>')) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
           errorNb++;
           beginDateOK = false;
         }
       }
       if (!isWhitespace(endDate)) {
         if (!isDateOK(endDate, '<%=resources.getLanguage()%>')) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
           errorNb++;
         } else {
             if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
               if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, '<%=resources.getLanguage()%>')) {
                 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("MustContainsPostDateToBeginDate")%>\n";
                 errorNb++;
               }
             } else {
               if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
                 if (!isFuture(endDate, '<%=resources.getLanguage()%>')) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("MustContainsPostDate")%>\n";
                   errorNb++;
                 }
               }
             }
         }
       }
     if (isWhitespace(nbQuestions)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbQuestionPerPage")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     } else {
           if (isInteger(nbQuestions) == false) {
               errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbQuestionPerPage")%>' <%=resources.getString("GML.MustContainsFloat")%>\n";
               errorNb++;
           } else {
                if (nbQuestions <= 0) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationNbQuestionPerPage")%>' <%=resources.getString("MustContainsPositiveNumber")%>\n";
                   errorNb++;
                }
           }
     }
     
  	 <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
  	 
     switch(errorNb) {
        case 0 :
            result = true;
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            window.alert(errorMsg);
            result = false;
            break;
     }
     return result;
}

</script>
</head>
<body id="creation-page" class="survey">
<%
if (action.equals("CreateSurvey")) {
      cancelButton = gef.getFormButton(generalMessage.getString("GML.cancel"), "Main.jsp", false);
      validateButton = gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=sendData()", false);
      surveyScc.removeSessionSurveyUnderConstruction();
      title = "";
      description = "";
      creationDate = resources.getOutputDate(new Date());
      beginDate = resources.getInputDate(new Date());
      endDate = "";
      nbQuestions = "3";
      nextAction="SendNewSurvey";

      Window window = gef.getWindow();
      Frame frame = gef.getFrame();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(surveyScc.getSpaceLabel());
      browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
   	  browseBar.setExtraInformation(resources.getString("SurveyCreation"));

      out.println(window.printBefore());
      out.println(frame.printBefore());
%>
<form name="surveyForm" action="surveyCreator.jsp" method="post">
	<fieldset id="info" class="skinFieldset">
		<legend><%=resources.getString("survey.header.fieldset.info") %></legend>
		<div class="fields">
			<div class="field" id="nameArea">
				<label class="txtlibform" for="title"><%=resources.getString("GML.name")%></label>
				<div class="champs">
					<input type="text" name="title" size="60" maxlength="60" value="<%=EncodeHelper.javaStringToHtmlString(title)%>"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
				</div>
			</div>
			<div class="field" id="commentArea">
				<label class="txtlibform" for="description"><%=resources.getString("SurveyCreationDescription")%></label>
				<div class="champs">
					<textarea name="description" cols="80" rows="4"><%=description%></textarea>
				</div>
			</div>
			<div class="field" id="nbQuestionsArea">
				<label class="txtlibform" for="nbQuestions"><%=resources.getString("SurveyCreationNbQuestionPerPage")%></label>
				<div class="champs">
					<input type="text" name="nbQuestions" size="5" value="<%=nbQuestions%>" maxlength="2"/>
				</div>
			</div>
			<div class="field" id="anonymousArea">
				<%
			        //Mode anonyme -> force les votes à être tous anonymes
			        String anonymousDisabled = "";
			        String anonymousCheck = "";
			        if(surveyScc.isAnonymousModeEnabled()) {
			          anonymousCheck = "checked=\"checked\"";
			          anonymousDisabled = "disabled=\"disabled\"";
			        }
				%>
				<label class="txtlibform" for="anonymous"><%=resources.getString("survey.surveyAnonymous")%></label>
				<div class="champs">
					<input type="checkbox" name="anonymous" value="" <%=anonymousCheck%> <%=anonymousDisabled%>/>
				</div>
			</div>
			<input type="hidden" name="Action" value="<%=nextAction%>"/>
		</div>
	</fieldset>
	
	<fieldset id="dates" class="skinFieldset">
				<legend><%=resources.getString("survey.header.fieldset.period") %></legend>
				<div class="fields">
					<div class="field" id="beginArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("SurveyCreationBeginDate")%></label>
						<div class="champs">
							<input type="text" class="dateToPick" name="beginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
						</div>
					</div>
					<div class="field" id="endArea">
						<label for="beginDate" class="txtlibform"><%=resources.getString("SurveyCreationEndDate")%></label>
						<div class="champs">
							<input type="text" class="dateToPick" name="endDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
						</div>
					</div>
				</div>
		</fieldset>
		
	<input type="hidden" name="Positions" />
    <view:pdcNewContentClassification componentId="<%=componentId%>" />
    
    <div class="legend">
		<img src="<%=mandatoryField%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%>
	</div>
		
</form>
<%
    out.println(frame.printMiddle());
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(validateButton);
    buttonPane.addButton(cancelButton);
    buttonPane.setHorizontalPosition();
    out.println("<center>"+buttonPane.print()+"</center>");
    out.println(frame.printAfter());
    out.println(window.printAfter());
    out.println("</body></html>");
 } //End if action = ViewQuestion
if (action.equals("SendNewSurvey")) {
%>
<html>
<head>
<script language="javascript">
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