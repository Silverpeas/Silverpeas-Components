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

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>

<%@ include file="checkSurvey.jsp" %>
<%@ include file="surveyUtils.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
//Retrieve parameter
String action = request.getParameter("Action");
String surveyId = request.getParameter("SurveyId");

String title = request.getParameter("title");
String description = request.getParameter("description");
String creationDate = "";
String beginDate = request.getParameter("beginDate");
String endDate = request.getParameter("endDate");
String nbQuestions = request.getParameter("nbQuestions");
String anonymousString = request.getParameter("anonymous");

//Anonymous mode -> force all the survey to be anonymous
if(surveyScc.isAnonymousModeEnabled()) {
	anonymousString = "true";
}

boolean anonymous = StringUtil.isDefined(anonymousString) && "true".equalsIgnoreCase(anonymousString);

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

QuestionContainerDetail survey = null;
if ("SendSurveyHeader".equals(action)) {
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
      surveyScc.updateSurveyHeader(surveyHeader, surveyId);
      action = "UpdateSurveyHeader";
      request.setAttribute("UpdateSucceed", "true");
}
if ("UpdateSurveyHeader".equals(action))
{
          survey = surveyScc.getSurvey(surveyId);
          QuestionContainerHeader surveyHeader = survey.getHeader();
          title = Encode.javaStringToHtmlString(surveyHeader.getTitle());
          description = Encode.javaStringToHtmlString(surveyHeader.getDescription());
          creationDate = resources.getOutputDate(surveyHeader.getCreationDate());
          beginDate = "";
          if (surveyHeader.getBeginDate() != null)
              beginDate = resources.getInputDate(surveyHeader.getBeginDate());
          endDate = "";
          if (surveyHeader.getEndDate() != null)
              endDate = resources.getInputDate(surveyHeader.getEndDate());
          nbQuestions = new Integer(surveyHeader.getNbQuestionsPerPage()).toString();
          anonymous = surveyHeader.isAnonymous();

          //Mode anonyme -> force les enquetes a etre toutes anonymes
		  if(surveyScc.isAnonymousModeEnabled()) {
			anonymous = true;
		  }

          anonymousString = "0";
          if (anonymous) {
          	anonymousString = "1";
		  }

%>
<html>
<head>
<title></title>
<view:looknfeel />
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function sendData() {
    if (isCorrectForm()) {
		document.surveyForm.anonymous.disabled = false;
        document.surveyForm.submit();
    }
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.surveyForm.title.value);
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
<body>
<%
        Window window = gef.getWindow();
        Frame frame = gef.getFrame();
        Board board = gef.getBoard();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(surveyScc.getSpaceLabel());
        browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
        browseBar.setExtraInformation(resources.getString("SurveyUpdate")+" '"+survey.getHeader().getTitle()+"'");

        out.println(window.printBefore());

        TabbedPane tabbedPane = gef.getTabbedPane();
        tabbedPane.addTab(resources.getString("GML.head"), "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId="+surveyId, action.equals("UpdateSurveyHeader"), false);
        String surveyTabPanelLabel = resources.getString("SurveyQuestions");
        if (surveyScc.isPollingStationMode()) {
          surveyTabPanelLabel = resources.getString("SurveyQuestion");
        }
        tabbedPane.addTab(surveyTabPanelLabel, "questionsUpdate.jsp?Action=UpdateQuestions&SurveyId="+surveyId, action.equals("UpdateQuestions"), true);
        out.println(tabbedPane.print());
        %>
<c:choose>
  <c:when test="${requestScope['UpdateSucceed']}">
    <div class="inlineMessage inlineMessage-ok">
      <fmt:message key="survey.update.header.succeed" />
    </div><br clear="all"/>
  </c:when>
</c:choose>
<%

        out.println(frame.printBefore());
%>
<view:board>

<center>
<form name="surveyForm" action="surveyUpdate.jsp" method="post">
<table cellpadding="5" width="100%">
    <tr>
      <td class="txtlibform"><%=resources.getString("GML.name")%> :</td>
      <td>
        <input type="text" name="title" size="60" value="<%=title%>" maxlength="100">
        &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
      </td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resources.getString("SurveyCreationDescription")%> :</td>
      <td>
        <textarea name="description" cols="50" rows="4"><%=description%></textarea>
      </td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resources.getString("SurveyCreationDate")%> :</td>
      <td><%=creationDate%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resources.getString("SurveyCreationBeginDate")%> :</td>
      <td>
        <input type="text" class="dateToPick" name="beginDate" size="14" value="<%=beginDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
      </td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resources.getString("SurveyCreationEndDate")%>
        :</td>
      <td>
        <input type="text" class="dateToPick" name="endDate" size="14" value="<%=endDate%>" maxlength="<%=DBUtil.getDateFieldLength()%>"/>
      </td>
    </tr>
    <%
    String anonymousCheck = "";
    if (anonymous) {
      anonymousCheck = "checked";
    }

    //Mode anonyme -> force les enquetes a etre toutes anonymes
    String anonymousDisabled = "";
    if(surveyScc.isAnonymousModeEnabled()) {
      anonymousDisabled = "disabled";
    }
  
    String anonymousLabel = resources.getString("survey.surveyAnonymous");
    String displayVote = "";
    if (surveyScc.isPollingStationMode()) {
      anonymousLabel = resources.getString("survey.pollAnonymous");
      displayVote="display:none;";
    }
  
    %>
    <tr style="<%=displayVote%>">
      <td class="txtlibform"><%=resources.getString("SurveyCreationNbQuestionPerPage")%>
        :</td>
      <td>
        <input type="text" name="nbQuestions" size="5" value="<%=nbQuestions%>" maxlength="2">
        &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
      </td>
    </tr>
    <tr>
    	<td class="txtlibform"><%=anonymousLabel%> :</td>
    	<td>
    	  <input type="checkbox" name="anonymous" value="true" <%=anonymousCheck%> <%=anonymousDisabled%>>
    	  <input type="hidden" name="anonymousString" value="<%=anonymousString%>">
    	</td>
    </tr>
    <tr>
      <td colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">
        : <%=generalMessage.getString("GML.requiredField")%>) </td>
    </tr>
    <tr>
      <td>
        <input type="hidden" name="Action" value="SendSurveyHeader">
        <input type="hidden" name="NextAction">
        <input type="hidden" name="SurveyId" value="<%=surveyId%>">
      </td>
    </tr>
</table>
</form>
</center>

<view:pdcClassification componentId="<%= componentId %>" contentId="<%= surveyId %>" editable="true" />

</view:board>
<%
        out.println(frame.printMiddle());
        Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(validateButton);
        buttonPane.setHorizontalPosition();
        out.println("<br/><center>"+buttonPane.print()+"</center>");
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>
<% } %>
