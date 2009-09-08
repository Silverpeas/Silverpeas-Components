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

<%@ include file="checkSurvey.jsp" %>

<%!
String lockSrc = "";
String unlockSrc = "";
String surveyDeleteSrc = "";
String surveyUpdateSrc = "";
String addSurveySrc = "";

%>

<% 
//Récupération des paramètres
String action = (String) request.getParameter("Action");
String language = (String) request.getParameter("Language");
String space = (String) request.getParameter("Space");
String profile = (String) request.getParameter("Profile");

String action_prev = "ViewOpenedSurveys";
if (action != null) {
    action_prev = action;
}

String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
lockSrc = iconsPath + "/util/icons/lock.gif";
unlockSrc = iconsPath + "/util/icons/unlock.gif";
surveyDeleteSrc = iconsPath + "/util/icons/delete.gif";
surveyUpdateSrc = iconsPath + "/util/icons/update.gif";
addSurveySrc = iconsPath + "/util/icons/survey_to_add.gif";

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

ResourceLocator message = new ResourceLocator("com.stratelia.webactiv.survey.multilang.surveyBundle", surveyScc.getLanguage());

%>
<HTML>
<HEAD>
<TITLE></TITLE>
<% out.println(gef.getLookStyleSheet()); %>
<SCRIPT Language="JavaScript1.2">
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

</SCRIPT>
</HEAD>
<BODY marginheight=2 marginwidth=2 leftmargin=2 topmargin=2>
<% 
	  int view = surveyScc.getViewType();
	  Collection surveys = surveyScc.getSurveys();
	  //Récupération du tableau dans le haut du cadre

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

<FORM NAME="surveysForm" ACTION="../../Rsurvey/jsp/surveyList.jsp?Space=<%=spaceId%>&Component=<%=componentId%>&Profile=<%=profile%>" METHOD="POST" target="MyMain">
<input type="hidden" name="Action" value="">
<input type="hidden" name="SurveyId" value="">
</FORM>

<FORM NAME="newSurveyForm" ACTION="../../Rsurvey/jsp/surveyCreator.jsp?Space=<%=spaceId%>&Component=<%=componentId%>&Profile=<%=profile%>" METHOD="POST" target="MyMain">
<input type="hidden" name="Action" value="">
</FORM>

</BODY>
</HTML>