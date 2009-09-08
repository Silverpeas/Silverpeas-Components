<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="com.oreilly.servlet.multipart.*"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>

<%@ include file="checkSurvey.jsp" %>

<%
//Récupération des paramètres
String action = "";
String surveyId = "";
String title = "";
String description = "";
String creationDate = "";
String beginDate = "";
String endDate = "";
String nbQuestions = "";
boolean anonymous = true;
String anonymousString = "1";
String nextAction = "";

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

Button validateButton = null;
Button cancelButton = null;
QuestionContainerDetail survey = null;

action = (String) request.getParameter("Action");
surveyId = (String) request.getParameter("SurveyId");
title	 = (String) request.getParameter("title");
description = (String) request.getParameter("description");
beginDate = (String) request.getParameter("beginDate");
endDate = (String) request.getParameter("endDate");
nbQuestions = (String) request.getParameter("nbQuestions");
anonymousString = request.getParameter("anonymous");
anonymous = false;
if (StringUtil.isDefined(anonymousString)&& anonymousString.equals("true"))
	anonymous = true;

//Mise a jour de l'espace
if (action == null) {
    action = "CreateSurvey";
}

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript1.2">
function sendData() {
    if (isCorrectForm()) {
        document.surveyForm.submit();
    }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.surveyForm.title.value);
     var description = document.surveyForm.description.value;
     var nbQuestions = document.surveyForm.nbQuestions.value;
     var re = /(\d\d\/\d\d\/\d\d\d\d)/i;
     var beginDate = document.surveyForm.beginDate.value;
     var endDate = document.surveyForm.endDate.value;
     
     var yearBegin = extractYear(beginDate, '<%=surveyScc.getLanguage()%>'); 
     var monthBegin = extractMonth(beginDate, '<%=surveyScc.getLanguage()%>');
     var dayBegin = extractDay(beginDate, '<%=surveyScc.getLanguage()%>');
     
     var yearEnd = extractYear(endDate, '<%=surveyScc.getLanguage()%>'); 
     var monthEnd = extractMonth(endDate, '<%=surveyScc.getLanguage()%>');
     var dayEnd = extractDay(endDate, '<%=surveyScc.getLanguage()%>');

     var beginDateOK = true;

     if (isWhitespace(title)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++; 
     }
     if (!isValidTextArea(document.surveyForm.description)) {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationDescription")%>' <%=resources.getString("ContainsTooLargeText")%> <%=DBUtil.TextAreaLength%> <%=resources.getString("Characters")%>\n";
          errorNb++; 
     }
     if (isWhitespace(beginDate)) {
     } else {
           if (beginDate.replace(re, "OK") != "OK") {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                   errorNb++;
                   beginDateOK = false;
           } else {
                 if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationBeginDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                   errorNb++;
                   beginDateOK = false;
                 }
           }
     }
     if (isWhitespace(endDate)) {
     } else {
           if (endDate.replace(re, "OK") != "OK") {
                 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                 errorNb++;
           } else {
                 if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
                     errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                     errorNb++;
                 } else {
                     if ((isWhitespace(beginDate) == false) && (isWhitespace(endDate) == false)) {
                           if (beginDateOK && isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin) == false) {
                                  errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                                  errorNb++;
                           }
                     } else {
                           if ((isWhitespace(beginDate) == true) && (isWhitespace(endDate) == false)) {
                               if (isFutureDate(yearEnd, monthEnd, dayEnd) == false) {
                                      errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsCorrectDate")%>\n";
                                      errorNb++;
                               }
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
</HEAD>
<BODY>
<%
if (action.equals("SendNewSurvey")) {
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
      QuestionContainerHeader surveyHeader = new QuestionContainerHeader(null, title, description, null, null, beginDate, endDate, false, 0, new Integer(nbQuestions).intValue(), anonymous);
      QuestionContainerDetail surveyDetail = new QuestionContainerDetail();
      surveyDetail.setHeader(surveyHeader);
      surveyScc.setSessionSurveyUnderConstruction(surveyDetail);
} //End if action = SendNewSurvey

else if (action.equals("CreateSurvey")) {
      cancelButton = (Button) gef.getFormButton(generalMessage.getString("GML.cancel"), "Main.jsp", false);
      validateButton = (Button) gef.getFormButton(generalMessage.getString("GML.validate"), "javascript:onClick=sendData()", false);
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
      Board board = gef.getBoard();

      BrowseBar browseBar = window.getBrowseBar();
      browseBar.setDomainName(surveyScc.getSpaceLabel());
      browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
   	  browseBar.setExtraInformation(resources.getString("SurveyCreation"));

      out.println(window.printBefore());
      out.println(frame.printBefore());
      out.println(board.printBefore());
%>
      <center>
<table CELLPADDING=5 width="100%">
    <form name="surveyForm" Action="surveyCreator.jsp" method="POST">
    <tr><td class="txtlibform"><%=resources.getString("GML.name")%> :</td><td><input type="text" name="title" size="60" value="<%=Encode.javaStringToHtmlString(title)%>" maxlength="100">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td></tr>
   	<tr><td class="txtlibform" valign="top"><%=resources.getString("SurveyCreationDescription")%> :</td><td><textarea name="description" cols="50" rows="4"><%=description%></textarea></td></tr>
    <tr><td class="txtlibform"><%=resources.getString("SurveyCreationDate")%> :</td><td><%=creationDate%></td></tr>
    <tr><td class="txtlibform"><%=resources.getString("SurveyCreationBeginDate")%> :</td><td><input type="text" name="beginDate" size="12" value="<%=beginDate%>" maxlength="<%=DBUtil.DateFieldLength%>"></td></tr>
    <tr><td class="txtlibform"><%=resources.getString("SurveyCreationEndDate")%> :</td><td><input type="text" name="endDate" size="12" value="<%=endDate%>" maxlength="<%=DBUtil.DateFieldLength%>"></td></tr>
    <tr><td class="txtlibform"><%=resources.getString("SurveyCreationNbQuestionPerPage")%> :</td><td><input type="text" name="nbQuestions" size="5" value="<%=nbQuestions%>" maxLength="2">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td></tr>
	<tr><td class="txtlibform"><%=resources.getString("survey.surveyAnonymous")%> :</td>
    	<%
			String anonymousCheck = "";
	    	if (anonymous)
	        {
	        	anonymousCheck = "checked";
	        }
		%>
    	<td><input type="checkbox" name="anonymous" value="true" <%=anonymousCheck%>>
    	  <input type="hidden" name="anonymousString" value="<%=anonymousString%>"></td>
    </tr>    
    <tr><td colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> : <%=generalMessage.getString("GML.requiredField")%>)</td></tr>
    <tr><td><input type="hidden" name="Action" value="<%=nextAction%>"></td></tr>
    </form>
</table>
</center>
<%
	  out.println(board.printAfter());
      out.println(frame.printMiddle());
      ButtonPane buttonPane = gef.getButtonPane();
      buttonPane.addButton(validateButton);
      buttonPane.addButton(cancelButton);
      buttonPane.setHorizontalPosition();
      out.println("<BR><center>"+buttonPane.print()+"</center>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
      out.println("</BODY></HTML>");
 } //End if action = ViewQuestion
if (action.equals("SendNewSurvey")) {
%>
<HTML>
<HEAD>
<script language="Javascript">
    function goToQuestionCreator() {
        document.questionForm.submit();
    }
</script>
</HEAD>
<BODY onLoad="goToQuestionCreator()">
<Form name="questionForm" Action="questionCreator.jsp" Method="POST" ENCTYPE="multipart/form-data">
<input type="hidden" name="Action" value="FirstQuestion">
</Form>
</BODY>
</HTML>
<% } %>