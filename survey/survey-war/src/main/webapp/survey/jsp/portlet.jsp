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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
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
<%@ page import="org.silverpeas.util.*" %>

<%@ include file="checkSurvey.jsp" %>

<%!
String lockSrc = "";
String unlockSrc = "";
String surveyDeleteSrc = "";
String surveyUpdateSrc = "";
String addSurveySrc = "";

%>

<% 
//R�cup�ration des param�tres
String action = (String) request.getParameter("Action");
String language = (String) request.getParameter("Language");
String space = (String) request.getParameter("Space");
String profile = (String) request.getParameter("Profile");

String action_prev = "ViewOpenedSurveys";
if (action != null) {
    action_prev = action;
}

String iconsPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");

//Icons
lockSrc = iconsPath + "/util/icons/lock.gif";
unlockSrc = iconsPath + "/util/icons/unlock.gif";
surveyDeleteSrc = iconsPath + "/util/icons/delete.gif";
surveyUpdateSrc = iconsPath + "/util/icons/update.gif";
addSurveySrc = iconsPath + "/util/icons/create-action/add-survey.png";

//Mise a jour de l'espace
if (action == null) {
    action = "ViewOpenedSurveys";
}
if (action.equals("DeleteSurvey")) {
    String surveyId = (String) request.getParameter("SurveyId");
    surveyScc.deleteSurvey(surveyId);
    action = "View";
} else if (action.equals("CloseSurvey")) {
    String surveyId = (String) request.getParameter("SurveyId");
    surveyScc.closeSurvey(surveyId);
    action = "ViewClosedSurveys";
} else if (action.equals("OpenSurvey")) {
    String surveyId = (String) request.getParameter("SurveyId");
    surveyScc.openSurvey(surveyId);
    action = "ViewOpenedSurveys";
}
if (action.equals("ViewOpenedSurveys")) {
    surveyScc.setViewType(SurveySessionController.OPENED_SURVEYS_VIEW);
    action = "View";
} else if (action.equals("ViewClosedSurveys")) {
    surveyScc.setViewType(SurveySessionController.CLOSED_SURVEYS_VIEW);
    action = "View";
} else if (action.equals("ViewInWaitSurveys")) {
    surveyScc.setViewType(SurveySessionController.INWAIT_SURVEYS_VIEW);
    action = "View";
}

surveyScc.removeSessionSurveyUnderConstruction();
surveyScc.removeSessionSurvey();
surveyScc.removeSessionResponses();

LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.survey.multilang.surveyBundle", surveyScc.getLanguage());

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<script language="JavaScript1.2">
function viewOpenedSurveys() {
  document.surveysForm.Action.value = "ViewOpenedSurveys";
  document.surveysForm.submit();
}

function viewClosedSurveys() {
  document.surveysForm.Action.value = "ViewClosedSurveys";
  document.surveysForm.submit();
}

function viewInWaitSurveys() {
  document.surveysForm.Action.value = "ViewInWaitSurveys";
  document.surveysForm.submit();
}

function createSurvey() {
  document.newSurveyForm.Action.value = "CreateSurvey";
  document.newSurveyForm.submit();
}

function deleteSurvey(surveyId, name) {
  document.surveysForm.Action.value = "<%=action_prev%>";
  document.surveysForm.submit();
}

function goto_jsp(jsp, param)
{
	param = param.substring(1);
	window.open("../../Rsurvey/<%=spaceId%>_<%=componentId%>/"+jsp+"?"+param,"MyMain");
}

</script>
</head>
<body marginheight="2" marginwidth="2" leftmargin="2" topmargin="2">
<% 
	  int view = surveyScc.getViewType();
	  Collection surveys = surveyScc.getSurveys();
	  //R�cup�ration du tableau dans le haut du cadre

	Iterator i = surveys.iterator();
	int index = 0;
	%>
	
  <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr class="intfdcolor51" height=15>
      <td><span class="textePetitBold"><%=message.getString("GML.name")%></span></td>
      <td><span class="textePetitBold"><%=message.getString("SurveyNbVoters")%></span></td>
    </tr>
	<tr bgcolor=666666>
      <td colspan=2><img src="" width=1></td>
    </tr>
    <tr> 
      <%
	
       while (i.hasNext()) {
		 QuestionContainerHeader survey = (QuestionContainerHeader) i.next();
		if (survey.getTitle() != null)
		{
%>
      <td valign=top height=15><span class=textePetitBold><a href="javascript:goto_jsp('surveyDetail.jsp','&Action=ViewCurrentQuestions&SurveyId=<%=survey.getPK().getId()%>')"><%=survey.getTitle()%></span></td>

      <td width="40%" align=center><%=survey.getNbVoters()%></td>
	  </tr><tr>
	  <td colspan="2" bgcolor=CCCCCC><img src="" width=1></td>
      </tr>
<%
		} else {
%>
      <td>&nbsp;</td>
	  </tr><tr>
	  <td colspan="2" bgcolor=CCCCCC><img src="" width=1></td>
      </tr>
      <%
		index++;
		}
	}
  %>
    
  </table>

<form name="surveysForm" action="../../Rsurvey/jsp/surveyList.jsp?Space=<%=spaceId%>&Component=<%=componentId%>&Profile=<%=profile%>" method="post" target="MyMain">
<input type="hidden" name="Action" value="">
<input type="hidden" name="SurveyId" value="">
</form>

<form name="newSurveyForm" action="../../Rsurvey/jsp/surveyCreator.jsp?Space=<%=spaceId%>&Component=<%=componentId%>&Profile=<%=profile%>" method="post" target="MyMain">
<input type="hidden" name="Action" value="">
</form>

</body>
</html>