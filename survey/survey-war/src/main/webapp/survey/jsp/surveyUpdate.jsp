<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkSurvey.jsp" %>
<%@ include file="surveyUtils.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%
	if (! surveyScc.isPollingStationMode()) {
%>
<fmt:message var="surveyConfirmUpdateLabel" key="survey.confirmUpdateSurvey" />
<% 
	} else { 
%>
  <fmt:message var="surveyConfirmUpdateLabel" key="survey.confirmUpdatePoll"/>
<%
	}
%>

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
String nbVotersString = request.getParameter("nbVoters");
int nbVoters = 0;
if(nbVotersString != null) {
  nbVoters = Integer.parseInt(nbVotersString);
}

String appName = "survey";
if (surveyScc.isPollingStationMode()) {
  nbQuestions = "1";
  appName = "pollingStation";
}
String anonymousString = request.getParameter("anonymous");

//Anonymous mode -> force all the survey to be anonymous
if(surveyScc.isAnonymousModeEnabled()) {
	anonymousString = "on";
}

boolean anonymous = StringUtil.isDefined(anonymousString) && "on".equalsIgnoreCase(anonymousString);

String resultMode = request.getParameter("resultMode");
String resultView = request.getParameter("resultView");

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

//Icons
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
      
      int resultModeInt = QuestionContainerHeader.IMMEDIATE_RESULTS;
      int resultViewInt = QuestionContainerHeader.TWICE_DISPLAY_RESULTS;
      
      if (! surveyScc.isPollingStationMode()) { 
	      resultModeInt = Integer.parseInt(resultMode);
	      resultViewInt = Integer.parseInt(resultView);
	      if(resultModeInt == QuestionContainerHeader.IMMEDIATE_RESULTS) {
	        resultViewInt = QuestionContainerHeader.TWICE_DISPLAY_RESULTS;
	      } else if (resultModeInt == QuestionContainerHeader.DELAYED_RESULTS) {
	        resultViewInt = QuestionContainerHeader.NOTHING_DISPLAY_RESULTS;
	      }
      }
      QuestionContainerHeader surveyHeader = new QuestionContainerHeader(null, title, description, null, null, beginDate, endDate, false, nbVoters, new Integer(nbQuestions).intValue(), anonymous, resultModeInt, resultViewInt);
      surveyScc.updateSurveyHeader(surveyHeader, surveyId);
      action = "UpdateSurveyHeader";
      request.setAttribute("UpdateSucceed", "true");
}
if ("UpdateSurveyHeader".equals(action))
{
         survey = surveyScc.getSurvey(surveyId);
         QuestionContainerHeader surveyHeader = survey.getHeader();
         title = WebEncodeHelper.javaStringToHtmlString(surveyHeader.getTitle());
         description = WebEncodeHelper.javaStringToHtmlString(surveyHeader.getDescription());
         creationDate = resources.getOutputDate(surveyHeader.getCreationDate());
         beginDate = "";
         if (surveyHeader.getBeginDate() != null)
             beginDate = resources.getInputDate(surveyHeader.getBeginDate());
         endDate = "";
         if (surveyHeader.getEndDate() != null)
             endDate = resources.getInputDate(surveyHeader.getEndDate());
         if (!surveyScc.isPollingStationMode()) {
         	nbQuestions = new Integer(surveyHeader.getNbQuestionsPerPage()).toString();
         }
         anonymous = surveyHeader.isAnonymous();

          //Mode anonyme -> force les enquetes a etre toutes anonymes
          if(surveyScc.isAnonymousModeEnabled()) {
            anonymous = true;
          }

          resultMode = Integer.toString(surveyHeader.getResultMode());
          resultView = Integer.toString(surveyHeader.getResultView());
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/dateUtils.js"></script>
<script type="text/javascript">
function sendData() {
    ifCorrectFormExecute(function() {
		  document.surveyForm.anonymous.disabled = false;
      document.surveyForm.submit();
    });
}

function ifCorrectFormExecute(callback) {
     var errorMsg = "";
     var errorNb = 0;
     var title = stripInitialWhitespace(document.surveyForm.title.value);
     var beginDate = document.surveyForm.beginDate.value;
     var endDate = document.surveyForm.endDate.value;
     var beginDateOK = true;
     
     if (isWhitespace(title)) {
           errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("GML.name")%>' <%=resources.getString("GML.MustBeFilled")%>\n";
           errorNb++;
     }
     <% if (!surveyScc.isPollingStationMode()) { %>
     if (!isValidTextArea(document.surveyForm.description)) {
          errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationDescription")%>' <%=resources.getString("ContainsTooLargeText")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Characters")%>\n";
          errorNb++;
     }
     <% } %>
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
                 errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsPostDateTo")%> '<%=resources.getString("SurveyCreationBeginDate")%>'\n";
                 errorNb++;
               }
             } else {
               if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
                 if (!isFuture(endDate, '<%=resources.getLanguage()%>')) {
                   errorMsg+="  - <%=resources.getString("GML.theField")%> '<%=resources.getString("SurveyCreationEndDate")%>' <%=resources.getString("GML.MustContainsPostDate")%>\n";
                   errorNb++;
                 }
               }
             }
         }
       }
     <% if (!surveyScc.isPollingStationMode()) { %>
     var nbQuestions = document.surveyForm.nbQuestions.value;
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
     <% } %>
     
  	<view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>
  
     switch(errorNb) {
       case 0 :
            callback.call(this);
            break;
        case 1 :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> 1 <%=resources.getString("GML.error")%> : \n" + errorMsg;
            jQuery.popup.error(errorMsg);
            break;
        default :
            errorMsg = "<%=resources.getString("GML.ThisFormContains")%> " + errorNb + " <%=resources.getString("GML.errors")%> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
     }
}

function updateQuestions(surveyId, name, nbVotes)
{
  var voteNumbers = parseInt(nbVotes);
  if(voteNumbers == 0) {
    reallyUpdateQuestions(surveyId);
  } else {
    var label = "<view:encodeJs string="${surveyConfirmUpdateLabel}" /> '" + name + "' ?";
    jQuery.popup.confirm(label, function() {
      reallyUpdateQuestions(surveyId);
    });
  }
}

function reallyUpdateQuestions(surveyId) {
  document.surveyForm.action = "QuestionsUpdate";
  document.surveyForm.Action.value = "UpdateQuestions";
  document.surveyForm.SurveyId.value = surveyId;
  document.surveyForm.submit();
}

</script>
</head>
<body class="<%=appName%>">
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
        tabbedPane.addTab(resources.getString("GML.head"), "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId="+surveyId, action.equals("UpdateSurveyHeader"), true);
        String surveyTabPanelLabel = resources.getString("SurveyQuestions");
        if (surveyScc.isPollingStationMode()) {
          surveyTabPanelLabel = resources.getString("SurveyQuestion");
        }
        tabbedPane.addTab(surveyTabPanelLabel, "javascript:updateQuestions('"+surveyId+"', '" +
            WebEncodeHelper.javaStringToHtmlString(WebEncodeHelper.javaStringToJsString(surveyHeader.getTitle())) + "', '" +
        	surveyHeader.getNbVoters() + "')", action.equals("UpdateQuestions"), true);
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

<form name="surveyForm" action="surveyUpdate.jsp" method="post">
<fieldset id="info" class="skinFieldset">
	<legend><%=resources.getString("survey.header.fieldset.info") %></legend>
	<div class="fields">
		<div class="field" id="nameArea">
			<label class="txtlibform" for="title"><%=resources.getString("GML.name")%></label>
			<div class="champs">
				<input type="text" name="title" size="60" maxlength="60" value="<%=title%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
			</div>
		</div>
		<% if (!surveyScc.isPollingStationMode()) { %>
		<div class="field" id="questionArea">
			<label class="txtlibform"><%=resources.getString("SurveyCreationDescription")%></label>
			<div class="champs">
				<textarea name="description" cols="80" rows="4"><%=description%></textarea>
			</div>
		</div>
		<% } %>
		<%
		    
		%>
		<% if (!surveyScc.isPollingStationMode()) { %>
		<div class="field" id="nbQuestionsArea">
			<label class="txtlibform"><%=resources.getString("SurveyCreationNbQuestionPerPage")%></label>
			<div class="champs">
				<input type="text" name="nbQuestions" size="5" value="<%=nbQuestions%>" maxlength="2"/>&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"/>
			</div>
		</div>
		<% } %>
		
		<div class="field" id="anonymousArea">
			<%
				String anonymousLabel = resources.getString("survey.surveyAnonymous");
	    		if (surveyScc.isPollingStationMode()) {
	    			anonymousLabel = resources.getString("survey.pollAnonymous");
	      		}
	    
		        //Mode anonyme -> force les votes à être tous anonymes
		        String anonymousDisabled = "";
		        String anonymousCheck = "";
		        if (anonymous) {
	  		      	anonymousCheck = "checked=\"checked\"";
	  		  	}
		        if(surveyScc.isAnonymousModeEnabled()) {
		          anonymousCheck = "checked=\"checked\"";
		          anonymousDisabled = "disabled=\"disabled\"";
		        }
			%>
			<label class="txtlibform"><%=anonymousLabel%></label>
			<div class="champs">
				<input type="checkbox" name="anonymous" <%=anonymousCheck%> <%=anonymousDisabled%>/>
			</div>
		</div>
		<% if (! surveyScc.isPollingStationMode()) { %>
		<div class="field" id="resultModeArea">
        <label class="txtlibform" for="resultMode"><%=resources.getString("survey.creation.resultMode")%></label>
        <div class="champs">
          <select id="resultMode" name="resultMode">
            <%
            String selectedStr = "";
            if (Integer.parseInt(resultMode) == QuestionContainerHeader.IMMEDIATE_RESULTS) {
              selectedStr = "selected";
            }
            %>
              <option <%=selectedStr%> value="<%=QuestionContainerHeader.IMMEDIATE_RESULTS%>"><%=resources.getString("survey.creation.resultMode.1")%></option>
            <%
            selectedStr = "";
            if (Integer.parseInt(resultMode) == QuestionContainerHeader.DELAYED_RESULTS) {
              selectedStr = "selected";
            }
            %>
              <option <%=selectedStr%> value="<%=QuestionContainerHeader.DELAYED_RESULTS%>"><%=resources.getString("survey.creation.resultMode.2")%></option>
          </select>
        </div>
    </div>
    <%} %>
	<input type="hidden" name="Action" value="SendSurveyHeader"/>
    <input type="hidden" name="NextAction"/>
    <input type="hidden" name="SurveyId" value="<%=surveyId%>"/>
    <input type="hidden" name="resultView" value="<%=resultView%>"/>
    <input type="hidden" name="nbVoters" value="<%=surveyHeader.getNbVoters()%>"/>
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

<view:pdcClassification componentId="<%= componentId %>" contentId="<%= surveyId %>" editable="true" />

<div class="legend">
	<img src="<%=mandatoryField%>" width="5" height="5"/> : <%=resources.getString("GML.requiredField")%>
</div>

</form>

<%
        out.println(frame.printMiddle());
        Button validateButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendData()", false);
        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(validateButton);
        buttonPane.setHorizontalPosition();
        out.println("<center>"+buttonPane.print()+"</center>");
        out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>
<% } %>
