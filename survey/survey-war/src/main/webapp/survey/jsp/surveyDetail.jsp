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
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>

<%@ include file="checkSurvey.jsp"%>
<%@ include file="surveyUtils.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="ctxPath" value="${pageContext.request.contextPath}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="profile" value="${requestScope['Profile']}"/>
<%
  String action = request.getParameter("Action");
  String surveyId = request.getParameter("SurveyId");
  String roundId = request.getParameter("RoundId");
  String profile = (String) request.getAttribute("Profile");
  String choice = request.getParameter("Choice");
  if (!StringUtil.isDefined(choice)) {
    choice = "D";
  }

  boolean participated = true;
  if (StringUtil.isDefined(request.getParameter("Participated"))) {
    participated = request.getParameter("Participated").equalsIgnoreCase("true");
  }

  boolean isParticipationMultipleUsed = surveyScc.isParticipationMultipleUsed();
  boolean isParticipationMultipleAllowedForUser =
      surveyScc.isParticipationMultipleAllowedForUser();

  ResourceLocator settings =
      new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", surveyScc
          .getLanguage());
  String m_context =
      GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");



  //Icons
  String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
  String alertSrc = m_context + "/util/icons/alert.gif";
  String exportSrc = m_context + "/util/icons/export.gif";
  String copySrc = m_context + "util/icons/copy.gif";
%>
<c:url value="/util/icons/alert.gif" var="alertSrc"></c:url>
<c:url value="/util/icons/export.gif" var="exportSrc"></c:url>
<c:url value="/util/icons/publish.gif" var="publishSrc"></c:url>
<c:url value="/util/icons/copy.gif" var="copySrc"></c:url>

<%
  QuestionContainerDetail survey = null;
  boolean isClosed = false;
  boolean inWait = false;

  if (action.equals("PreviewSurvey") || action.equals("SubmitSurvey")) {
    survey = surveyScc.getSessionSurveyUnderConstruction();
  } else if (action.equals("ViewComments")) {
    survey = surveyScc.getSurvey(surveyId);
  } else if (action.equals("SubmitAndUpdateSurvey")) {
  } else {
    if (roundId != null) {
      survey = surveyScc.getSessionSurvey();
    } else {
      survey = surveyScc.getSurvey(surveyId);
      surveyScc.setSessionSurvey(survey);
      roundId = "1";
    }
    boolean endDateReached = false;
    if (survey.getHeader().getEndDate() != null)
      endDateReached =
          (survey.getHeader().getEndDate().compareTo(resources.getDBDate(new Date())) < 0);
    if (endDateReached || survey.getHeader().isClosed())
      isClosed = true;
    if (survey.getHeader().getBeginDate() != null)
      inWait =
          (survey.getHeader().getBeginDate().compareTo(resources.getDBDate(new Date())) > 0);

    if (action.equals("Vote") && surveyScc.isParticipationMultipleAllowedForUser()) {
      action = "ViewCurrentQuestions";
    } else if (action.equals("SendVote") &&
        (surveyScc.isParticipationMultipleAllowedForUser() || surveyScc
            .isAnonymousModeAuthorized())) {
    } else if (action.equals("ViewCurrentQuestions") && surveyScc.isAnonymousModeAuthorized() &&
        !surveyScc.hasAlreadyParticipated()) {
    } else if (action.equals("RecordQuestionsResponses")) {
    } else {
      if ((isClosed ||
          (survey.getCurrentUserVotes() != null && survey.getCurrentUserVotes().size() > 0) || inWait)) {
        action = "ViewResult";
      }
    }
    if (action == null)
      action = "ViewSurvey";
  }

  if (action.equals("SendVote")) {
    int nbQuestions = new Integer((String) request.getParameter("NbQuestions")).intValue();
    String comment = (String) request.getParameter("Comment");
    String isAnonymousComment = (String) request.getParameter("anonymousComment");
    Hashtable hash = surveyScc.getSessionResponses();
    if (hash == null)
      hash = new Hashtable();

    boolean iAC = false;
    if (isAnonymousComment != null && isAnonymousComment.equals("1"))
      iAC = true;

    for (int i = 1; i <= nbQuestions; i++) {
      Vector v = new Vector(5, 2);
      String[] selectedAnswers = (String[]) request.getParameterValues("answer_" + i);
      if (selectedAnswers != null) {
        String questionId =
            selectedAnswers[0].substring(selectedAnswers[0].indexOf(",") + 1,
                selectedAnswers[0].length());
        for (int j = 0; j < selectedAnswers.length; j++) {
          String answerId = selectedAnswers[j].substring(0, selectedAnswers[j].indexOf(","));
          v.add(answerId);
        }
        String openedAnswer = (String) request.getParameter("openedAnswer_" + i);
        v.add("OA" + openedAnswer);
        hash.put(questionId, v);
      }
    }
    surveyScc.recordReply(surveyId, hash, comment, iAC);

    surveyScc.removeSessionResponses();

    //Record participation in cookie
    if (surveyScc.isAnonymousModeAuthorized()) {
%>
<script language="javascript">
  location.href = "/RecordParticipation?cid=<%=surveyScc.getComponentId()%>&sid=<%=surveyId%>&duration=<%=settings.getString("cookieDuration")%>";
</script>
<%
  }
    action = "ViewResult";
  }

  if (action.equals("RecordQuestionsResponses")) {
    int nbQuestions = new Integer((String) request.getParameter("NbQuestions")).intValue();
    Hashtable hash = surveyScc.getSessionResponses();
    if (hash == null)
      hash = new Hashtable();

    for (int i = 1; i <= nbQuestions; i++) {
      Vector v = new Vector(5, 2);
      String[] selectedAnswers = (String[]) request.getParameterValues("answer_" + i);
      if (selectedAnswers != null) {
        String questionId =
            selectedAnswers[0].substring(selectedAnswers[0].indexOf(",") + 1,
                selectedAnswers[0].length());
        for (int j = 0; j < selectedAnswers.length; j++) {
          String answerId = selectedAnswers[j].substring(0, selectedAnswers[j].indexOf(","));
          v.add(answerId);
        }
        String openedAnswer = (String) request.getParameter("openedAnswer_" + i);
        v.add("OA" + openedAnswer);
        hash.put(questionId, v);
      }
    }
    surveyScc.setSessionResponses(hash);
    action = "ViewCurrentQuestions";
  }

  if (action.equals("SubmitSurvey")) {
    QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
    //Vector 2 Collection
    List questionsV = surveyScc.getSessionQuestions();
    ArrayList q = new ArrayList();
    for (int j = 0; j < questionsV.size(); j++) {
      q.add((Question) questionsV.get(j));
    }
    surveyDetail.setQuestions(q);
    surveyScc.createSurvey(surveyDetail);
    surveyScc.removeSessionSurveyUnderConstruction();
%>
<jsp:forward page="<%=surveyScc.getComponentUrl()+\"Main.jsp\"%>" />
<%
  return;
  } else if (action.equals("SubmitAndUpdateSurvey")) {
    QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
    //Vector 2 Collection
    List questionsV = surveyScc.getSessionQuestions();
    ArrayList q = new ArrayList();
    for (int j = 0; j < questionsV.size(); j++) {
      q.add((Question) questionsV.get(j));
    }
    surveyDetail.setQuestions(q);
    surveyId = surveyScc.createSurvey(surveyDetail).getId();
    surveyScc.removeSessionSurveyUnderConstruction();
  } else if (action.equals("PreviewSurvey")) {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
</head>
<body>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(surveyScc.getSpaceLabel());
    browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp?Action=View");
    browseBar.setExtraInformation(resources.getString("survey.preview"));

    String surveyPart =
        displaySurveyPreview(survey, gef, m_context, surveyScc, resources, settings);

    window.addBody(surveyPart);
    out.println(window.print());
  }
  if (action.equals("ViewSurvey")) {
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
</head>
<body>
    <%

    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(surveyScc.getSpaceLabel());
    browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp?Action=View");
    browseBar.setExtraInformation(survey.getHeader().getTitle());

    String surveyPart =
        displaySurvey(survey, gef, m_context, surveyScc, resources, settings, profile,
            pollingStationMode, participated);

    // notification
    OperationPane operationPane = window.getOperationPane();
    String url = "ToAlertUser?SurveyId=" + surveyId;
    operationPane.addOperation(alertSrc, resources.getString("GML.notify"),
        "javaScript:onClick=goToNotify('" + url + "')");

    window.addBody(surveyPart);
    %>
    <view:pdcClassification componentId="<%= componentId %>" contentId="<%= surveyId %>" />
    <%
    out.println(window.print());
  } //End if action = ViewSurvey

  else if (action.equals("ViewComments")) {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
</head>
<body>
<%
  Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(surveyScc.getSpaceLabel());
    browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp?Action=View");
    browseBar.setExtraInformation(survey.getHeader().getTitle());

    String surveyPart =
        displaySurveyComments(surveyScc, survey, gef, resources, profile, pollingStationMode, participated);

    window.addBody(surveyPart);
    out.println(window.print());
  }
  // end if action = ViewComments

  else if (action.equals("ViewCurrentQuestions")) {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel />

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function sendVote(roundId) {
  if (isCorrectForm()) {

	 try
	 {
 document.survey.anonymousComment.disabled = false;
		 if (document.survey.anonymousComment.checked)
			 x = 1;
		 else
			 x = 0;
		 document.survey.Comment.value = document.survey.Comment.value;
		 document.survey.anonymousComment.value = x;
	 } catch (e)
	 {
		 //la zone commentaire n'est pas affich?e pour la page courante
	 }

	 if (roundId == "end") {
          document.survey.Action.value="SendVote";
          document.survey.submit();
	 } else {
         document.survey.RoundId.value = roundId;
          document.survey.Action.value="RecordQuestionsResponses";
          document.survey.submit();
    }
  }
}

function checkRadioAndCheckboxes()
{
	var ok = false;
	var first = true;
	var name = "";
	var indice = 1;
    for (var i=0; i<document.survey.length; i++)
    {
    	// on passe sur toutes les zones du formulaire
    	name = document.survey.elements[i].name;
    	startName = name.substr(0,7);
    	//alert(document.survey.elements[i].type+", "+name+", ok = "+ok+", indice = "+indice);
    	if (startName == "answer_")
    	{
    		// on ne contr?le que les zones r?ponses
    		endName = name.substr(7);
    		if (first)
    		{
    			indice = endName;
    			first = false;
    		}
    		if (endName != indice)
    		{
    			// on a d?j? contr?l? une question qui n'a pas de r?ponse, pas la peine de continuer
    			if (!ok)
    				return false;

    			ok = false;
    		}
       	if (document.survey.elements[i].type == "radio" || document.survey.elements[i].type == "checkbox")
       	{
       		// contr?le des boutons radio et des cases ? cocher (pas de contr?le pour les questions ouvertes)
         	if (document.survey.elements[i].checked)
         	{
         		// une case est coch?e, valider la question
         		ok = true;
         	}
         	indice = endName;
       	}
       	else
       	{
       		ok = true;
       	}
    	} // fin : if (startName == "answer_")
    }
    return ok;
}

function isCorrectForm() {
     var errorMsg = "";
     var errorNb = 0;

     ok = 1;
     for (var i=0; i<document.survey.length; i++) {
          if (document.survey.elements[i].type == "textarea") {
              if (!isValidTextArea(document.survey.elements[i])) {
                    ok = 0;
                    document.survey.elements[i].select();
                    errorMsg+=" <%=resources.getString("GML.theField")%> <%=resources.getString("ContainsTooLargeText")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Characters")%>";
                    errorNb++;
              }
          }
     }
     // contr?le que toutes les questions aient au moins une r?ponse valid?e
   if (!checkRadioAndCheckboxes())
   {
    	 errorMsg+="<%=resources.getString("survey.NoResponse")%>";
       errorNb++;
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

function checkButton(input) {
    if (!input.checked)
        input.click();
}

var notifyWindow = window;

function goToNotify(url)
{
	windowName = "notifyWindow";
	larg = "740";
	haut = "600";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
        notifyWindow.close();
    notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function clipboardCopy() {
    top.IdleFrame.location.href = '../..<%=surveyScc.getComponentUrl()%>copy?Id=<%=survey.getHeader().getId()%>';
}

</script>
</head>

<body>
<view:window>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(surveyScc.getSpaceLabel());
    browseBar.setComponentName(surveyScc.getComponentLabel(), "surveyList.jsp?Action=View");
    browseBar.setExtraInformation(survey.getHeader().getTitle());
  
    participated = false;
    String surveyPart =
        displayQuestions(survey, Integer.parseInt(roundId), gef, m_context, surveyScc,
            resources, settings, profile, pollingStationMode, participated);
  
    // notification
    OperationPane operationPane = window.getOperationPane();
    String url = "ToAlertUser?SurveyId=" + surveyId;
    operationPane.addOperation(alertSrc, resources.getString("GML.notify"),
        "javaScript:onClick=goToNotify('" + url + "')");
  
    // copier
    operationPane.addOperation(copySrc, resources.getString("GML.copy"),
        "javaScript:onClick=clipboardCopy()");
  
    out.println(surveyPart);
%>
<view:pdcClassification componentId="<%= componentId %>" contentId="<%= surveyId %>" />
</view:window>
<%
  } else if (action.equals("ViewResult")) {
    String iconsPath =
        GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    int resultView = survey.getHeader().getResultView();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel />
<view:includePlugin name="popup"/>
<script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

 		function viewSuggestions(id) {
 		    url = "surveySuggestions.jsp?QuestionId="+id;
 		    windowName = "sugg";
 		    larg = "550";
 		    haut = "250";
 		    windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 		    suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
 		    suggestions.focus();
 		}

 		var notifyWindow = window;
 		var usersWindow = window;
 	  var exportWindow = window;

 		function goToNotify(url)
    {
 			  windowName = "notifyWindow";
        larg = "740";
        haut = "600";
        windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
        if (!notifyWindow.closed && notifyWindow.name == "notifyWindow")
                notifyWindow.close();
            notifyWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
    }

 		function Export(url)
    {
 			  windowName = "exportWindow";
        larg = "740";
        haut = "600";
        windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
        if (!exportWindow.closed && exportWindow.name == "exportWindow")
                exportWindow.close();
            exportWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
    }

    function viewUsers(id)
    {
    	  url = "ViewListResult?AnswerId="+id;
 		    windowName = "usersWindow";
 		    larg = "550";
 		    haut = "370";
 		    windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 		    if (!usersWindow.closed && usersWindow.name == "usersWindow")
                usersWindow.close();
 		    usersWindow = SP_openWindow(url, windowName, larg , haut, windowParams);
 		    usersWindow.focus();
    }

    function viewAllUsers(id)
    {
       	url = "ViewAllUsers?SurveyId="+id;
 		    windowName = "usersWindow";
 		    larg = "550";
 		    haut = "370";
 		    windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 		    if (!usersWindow.closed && usersWindow.name == "usersWindow")
                usersWindow.close();
 		    usersWindow = SP_openWindow(url, windowName, larg , haut, windowParams);
 		    usersWindow.focus();
    }

    function viewResultByUser(userId, userName)
    {
       	url = "UserResult?UserId="+userId+"&UserName="+userName;
 		    windowName = "resultByUser";
 		    larg = "700";
 		    haut = "500";
 		    windowParams = "directories=0,menubar=0,toolbar=0,resizable=1,scrollbars=1,alwaysRaised";
 		    suggestions = SP_openWindow(url, windowName, larg , haut, windowParams);
 		    suggestions.focus();
    }

    function clipboardCopy() {
        top.IdleFrame.location.href = '../..<%=surveyScc.getComponentUrl()%>copy?Id=<%=survey.getHeader().getId()%>';
    }
    
    function changeScope(mode, participated, surveyId) {
    	if(mode == 'classic') {
    		location.href="surveyDetail.jsp?Action=ViewResult&Participated="+participated+"&SurveyId="+surveyId+"&Choice=C";
    	} else if (mode == 'detail') {
    		location.href="surveyDetail.jsp?Action=ViewResult&Participated="+participated+"&SurveyId="+surveyId+"&Choice=D";
    	}
    }
    
    function isCorrectForm() {
    	  var errorMsg = "";
    	  var errorNb = 0;
    	                                      
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

    	function sendData() {
    	  document.publishResultForm.submit();
    	}
    
    function showDialog(title) {
	 	  $("#publishResultDialog").popup({
	      title: title,
	      callback: function() {
	        if (isCorrectForm()) {
	          sendData();
	        }
	        return isCorrect;
	      }
	    });
    }
    
    function PublishResult(title) {
    	  /*var name = $("#categ-"+id+" .categ-title").text();
    	  var desc = $("#categ-"+id+" .categ-desc").text();
    	  
    	  $("#categoryManager #CategoryId").val(id);
    	  $("#categoryManager #Name").val(name);
    	  $("#categoryManager #Description").val(desc);*/
    	  
    	  showDialog(title)
    }

     	</script>
</head>
<body id="survey-result-<%=choice%>">
<view:browseBar extraInformations="<%=survey.getHeader().getTitle()%>" componentId="<%=surveyScc.getComponentId()%>">
</view:browseBar>
<view:operationPane>
  <fmt:message key="GML.notify" var="notifyUserMsg" />
  <c:set var="notifyUserAction">javaScript:onClick=goToNotify('ToAlertUser?SurveyId=<%=surveyId%>');</c:set>
  <view:operation altText="${notifyUserMsg}" icon="${alertSrc}" action="${notifyUserAction}" />
  
  <%
  if (survey.getHeader().getResultMode() == QuestionContainerHeader.DELAYED_RESULTS &&
      (SilverpeasRole.admin.toString().equals(profile) || 
      SilverpeasRole.publisher.toString().equals(profile))) {
  %>
    <fmt:message key="survey.publishResult" var="publishMsg" />
    <c:set var="publishAction">javaScript:onClick=PublishResult('${publishMsg}');</c:set>
    <view:operation altText="${publishMsg}" icon="${publishSrc}" action="${publishAction}" />
  <%
  }
  %>

  <%
  if (SilverpeasRole.admin.toString().equals(profile) ||
      SilverpeasRole.publisher.toString().equals(profile)) {
  %>
    <fmt:message key="GML.export" var="exportMsg" />
    <c:set var="exportAction">javaScript:onClick=Export('ExportCSV?SurveyId=<%=surveyId%>');</c:set>
    <view:operation altText="${exportMsg}" icon="${exportSrc}" action="${exportAction}" />
  <%
  }
  %>
  <fmt:message key="GML.copy" var="copyMsg" />
  <view:operation altText="${copyMsg}" icon="${copySrc}" action="javaScript:onClick=clipboardCopy();" />
  
</view:operationPane>
<view:window>
<view:frame>
<%
Frame frame = gef.getFrame();
String surveyPart =
    displaySurveyResult(choice, survey, gef, m_context, surveyScc, resources, isClosed,
        settings, frame, participated, profile);
out.println(displayTabs(surveyScc, survey.getHeader().getPK().getId(), gef, action,
    profile, resources, pollingStationMode, participated).print() +
    frame.printBefore());
out.println(surveyPart);

%>

<view:pdcClassification componentId="<%= componentId %>" contentId="<%= surveyId %>" />
</view:frame>
</view:window>

<div id="publishResultDialog" style="display: none;">
  <form name="publishResultForm" action="PublishResult" method="post">
  <div id="view-publishResultDialog">
    <fmt:message key="survey.resultView" var="resultViewMsg" />
    <fmt:message key="survey.C" var="classicMsg" />
    <fmt:message key="survey.D" var="detailedMsg" />
    <fmt:message key="survey.resultView.C" var="classicDescMsg" />
    <fmt:message key="survey.resultView.D" var="detailedDescMsg" />
    <fmt:message key="survey.synthesisFile" var="synthesisFileMsg" />
    <fmt:message key="survey.notifications" var="notificationMsg" />
    <fmt:message key="survey.noNotification" var="noNotificationMsg" />
    <fmt:message key="survey.notificationParticipants" var="notificationParticipantsMsg" />
    <label class="label-ui-dialog" for="view">${resultViewMsg}</label>
    <%
    String checked = "";
    if(QuestionContainerHeader.CLASSIC_DISPLAY_RESULTS == resultView || 
        QuestionContainerHeader.TWICE_DISPLAY_RESULTS == resultView) {
      checked = "checked=\"checked\"";
    }
    %>
    <span class="champs-ui-dialog"><input name="checkedView" type="checkbox" value="C" <%=checked%>/><b>${classicMsg}</b><br />${classicDescMsg}</span>
    <% 
    checked = "";
    if(QuestionContainerHeader.DETAILED_DISPLAY_RESULTS == resultView || 
        QuestionContainerHeader.TWICE_DISPLAY_RESULTS == resultView) {
      checked = "checked=\"checked\"";
    }
    %>
    <span class="champs-ui-dialog"><input name="checkedView" type="checkbox" value="D" <%=checked%>/><b>${detailedMsg}</b><br />${detailedDescMsg}</span>
  </div>
  <div id="synthesisFile-publishResultDialog">
    <label class="label-ui-dialog" for="synthesisFile">${synthesisFileMsg}</label>
    <span class="champs-ui-dialog"><input name="synthesisFile" type="file" /></span>
  </div>
  <div id="notification-publishResultDialog"> 
  <label class="label-ui-dialog" for="notification">${notificationMsg}</label>
  <span class="champs-ui-dialog">
    <select name="notification">
      <option value="0">${noNotificationMsg}</option>
      <option value="1">${notificationParticipantsMsg}</option>
    </select>
  </span>
  </div>
  </form>
</div>

<%

  } else if (action.equals("SubmitAndUpdateSurvey")) {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script language="Javascript">
function Replace() {
  location.replace("surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId=<%=surveyId%>");
}
</script>
</head>
<body onload="Replace()">
<%
  }
%>

</body>
</html>
