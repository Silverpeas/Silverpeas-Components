<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ include file="checkSurvey.jsp" %>
<%@ include file="surveyUtils.jsp.inc" %>

<%!

  String displayAlreadyVotes(QuestionContainerDetail survey, SurveySessionController surveyScc, GraphicElementFactory gef, ResourcesWrapper resources) throws SurveyException, ParseException {
        
    String r = "";
    String labelButton = resources.getString("Survey.revote");
    if (surveyScc.isPollingStationMode())
	    labelButton = resources.getString("PollingStation.revote");
    
    Board board = gef.getBoard();
        try{
            if (survey != null) 
            {
	            Collection votes = survey.getCurrentUserVotes();
	            if (votes != null) 
	            {
	                if (votes.size() > 0) 
	                {
	                    Iterator it = votes.iterator();
	                    if (it.hasNext()) 
	                    {
	                    	QuestionResult vote = (QuestionResult) it.next();
	                    	r += board.printBefore();
	                        r+= "<table border=\"0\" width=\"100%\">";
	                        r += "<tr><td align=center><span class=txtnav>"+resources.getString("YouHaveAlreadyParticipate")+" "+resources.getOutputDate(vote.getVoteDate())+"</span></td></tr>";
	                        //DLE
	                        if (surveyScc.isParticipationMultipleAllowedForUser())
	                        	r += "<tr><td align=center><a href=\"surveyDetail.jsp?Action=Vote&SurveyId="+survey.getHeader().getId()+"\">"+labelButton+"</a></td></tr>";
	                        r += "</table>";
	                        r += board.printAfter();
	                   }
	                }
	            } 
	            else 
                 r += "";
	        }
        }
        catch (Exception e){
            throw new  SurveyException("SurveyDetail_JSP.displayAlreadyVotes",SurveyException.WARNING,"Survey.EX_NO_VOTES_FOR_SURVEY",e);
        }
        return r;
  }
%>

<%
//Récupération des paramètres
String action = (String) request.getParameter("Action");
String surveyId = (String) request.getParameter("SurveyId");
String roundId = (String) request.getParameter("RoundId");
String profile = (String) request.getAttribute("Profile");
String choice = (String) request.getParameter("Choice");


boolean isParticipationMultipleUsed = surveyScc.isParticipationMultipleUsed();
boolean isParticipationMultipleAllowedForUser = surveyScc.isParticipationMultipleAllowedForUser();

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", surveyScc.getLanguage());
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
String alertSrc = m_context + "/util/icons/alert.gif";

QuestionContainerDetail survey = null;
boolean isClosed = false;
boolean inWait = false;

if (action.equals("PreviewSurvey") || action.equals("SubmitSurvey")) {
      survey = surveyScc.getSessionSurveyUnderConstruction();
} 
else if (action.equals("ViewComments"))
{
	survey = surveyScc.getSurvey(surveyId);
}
else if (action.equals("SubmitAndUpdateSurvey")) 
{
} 
else {
      if (roundId != null) {
            survey = surveyScc.getSessionSurvey();
      } else {
            survey = surveyScc.getSurvey(surveyId);
            surveyScc.setSessionSurvey(survey);
            roundId = "1";
      }
      boolean endDateReached = false;
      if (survey.getHeader().getEndDate() != null)
              endDateReached = (survey.getHeader().getEndDate().compareTo(resources.getDBDate(new Date())) < 0);
      if (endDateReached || survey.getHeader().isClosed())
              isClosed = true;
      if (survey.getHeader().getBeginDate() != null)
              inWait = (survey.getHeader().getBeginDate().compareTo(resources.getDBDate(new Date())) > 0);

			if (action.equals("Vote") && surveyScc.isParticipationMultipleAllowedForUser())
			{
				action = "ViewCurrentQuestions";
			}
			else if (action.equals("SendVote") && (surveyScc.isParticipationMultipleAllowedForUser() || surveyScc.isAnonymousModeAuthorized()))
			{
			}
			else if (action.equals("ViewCurrentQuestions") && surveyScc.isAnonymousModeAuthorized() && !surveyScc.hasAlreadyParticipated())
			{
			}
			else if (action.equals("RecordQuestionsResponses"))
			{
			}
			else
			{
	      		if ((isClosed || (survey.getCurrentUserVotes() != null && survey.getCurrentUserVotes().size() > 0 ) || inWait))
	     		{
	        		action = "ViewResult";
	      		}
			}
      if (action == null)
            action = "ViewSurvey";
}

if (action.equals("SendVote"))
{
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
            String[] selectedAnswers = (String[]) request.getParameterValues("answer_"+i);
            if (selectedAnswers != null) {
                String questionId = selectedAnswers[0].substring(selectedAnswers[0].indexOf(",")+1, selectedAnswers[0].length());
                for (int j = 0; j < selectedAnswers.length; j++) {
                      String answerId = selectedAnswers[j].substring(0, selectedAnswers[j].indexOf(","));
                      v.add(answerId);
                }
                String openedAnswer = (String) request.getParameter("openedAnswer_"+i);
                v.add("OA"+openedAnswer);
                hash.put(questionId, v);
            }
        }
        surveyScc.recordReply(surveyId, hash, comment, iAC);

        surveyScc.removeSessionResponses();
        
        //Record participation in cookie
		if (surveyScc.isAnonymousModeAuthorized())
        {
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
            String[] selectedAnswers = (String[]) request.getParameterValues("answer_"+i);
            if (selectedAnswers != null) {
                String questionId = selectedAnswers[0].substring(selectedAnswers[0].indexOf(",")+1, selectedAnswers[0].length());
                for (int j = 0; j < selectedAnswers.length; j++) {
                      String answerId = selectedAnswers[j].substring(0, selectedAnswers[j].indexOf(","));
                      v.add(answerId);
                }
                String openedAnswer = (String) request.getParameter("openedAnswer_"+i);
                v.add("OA"+openedAnswer);
                hash.put(questionId, v);
            }
        }
        surveyScc.setSessionResponses(hash);
        action = "ViewCurrentQuestions";
} 

if (action.equals("SubmitSurvey")) {
        QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
        //Vector 2 Collection
        Vector questionsV = surveyScc.getSessionQuestions();
        ArrayList q = new ArrayList();
        for (int j = 0; j < questionsV.size(); j++) {
              q.add((Question) questionsV.get(j));
        }
        surveyDetail.setQuestions(q);
        surveyScc.createSurvey(surveyDetail);
        surveyScc.removeSessionSurveyUnderConstruction();
        %>
        <jsp:forward page="<%=surveyScc.getComponentUrl()+\"Main.jsp\"%>"/>
        <%
        return;
} 
else if (action.equals("SubmitAndUpdateSurvey")) {
        QuestionContainerDetail surveyDetail = surveyScc.getSessionSurveyUnderConstruction();
        //Vector 2 Collection
        Vector questionsV = surveyScc.getSessionQuestions();
        ArrayList q = new ArrayList();
        for (int j = 0; j < questionsV.size(); j++) {
              q.add((Question) questionsV.get(j));
        }
        surveyDetail.setQuestions(q);
        surveyId = surveyScc.createSurvey(surveyDetail).getId();
        surveyScc.removeSessionSurveyUnderConstruction();
} 
else if (action.equals("PreviewSurvey")) {
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println(gef.getLookStyleSheet());
        out.println("</HEAD>");
        out.println("<BODY>");

        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(surveyScc.getSpaceLabel());
        browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
        browseBar.setExtraInformation(resources.getString("GML.preview"));

        String surveyPart = displaySurveyPreview(survey, gef, m_context, surveyScc, resources, settings);

		// notification
		//OperationPane operationPane = window.getOperationPane();
        //String url = "ToAlertUser?SurveyId=" + surveyId;
        //operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:onClick=goToNotify('"+url+"')");
         
        window.addBody(surveyPart);
        out.println(window.print());
}
if (action.equals("ViewSurvey")) {
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println(gef.getLookStyleSheet());
        out.println("</HEAD>");
        out.println("<BODY>");

        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(surveyScc.getSpaceLabel());
        browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
        browseBar.setExtraInformation(survey.getHeader().getTitle());

        String surveyPart = displaySurvey(survey, gef, m_context, surveyScc, resources, settings, profile, pollingStationMode);

		// notification
		OperationPane operationPane = window.getOperationPane();
        String url = "ToAlertUser?SurveyId=" + surveyId;
        operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:onClick=goToNotify('"+url+"')");

        window.addBody(surveyPart);
        out.println(window.print());
} //End if action = ViewSurvey

else if (action.equals("ViewComments")) {
%>
    <HTML>
    <HEAD>
    <TITLE></TITLE>
    
    <%
    out.println(gef.getLookStyleSheet()); 
    %>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
    </HEAD>
    <BODY>
<%
    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(surveyScc.getSpaceLabel());
    browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
    browseBar.setExtraInformation(survey.getHeader().getTitle());

    String surveyPart = displaySurveyComments(surveyScc, survey, gef, resources, profile, pollingStationMode);

    window.addBody(surveyPart);
    out.println(window.print());
}
// end if action = ViewComments

else if (action.equals("ViewCurrentQuestions")) {
%>
        <HTML>
        <HEAD>
        <TITLE></TITLE>
            
        <%
        out.println(gef.getLookStyleSheet()); 
        %>
        <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
        <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
        <script language="JavaScript1.2">
        function sendVote(roundId) {
          if (isCorrectForm()) {
        	  
        	 try 
        	 {
        		 if (document.survey.anonymousComment.checked)
        			 x = 1;
        		 else
        			 x = 0; 
        		 document.survey.Comment.value = document.survey.Comment.value;
        		 document.survey.anonymousComment.value = x;
        	 } catch (e)
        	 {
        		 //la zone commentaire n'est pas affichée pour la page courante
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
            		// on ne contrôle que les zones réponses
            		endName = name.substr(7);
            		if (first)
            		{
            			indice = endName;
            			first = false;
            		}
            		if (endName != indice)
            		{
            			// on a déjà contrôlé une question qui n'a pas de réponse, pas la peine de continuer
            			if (!ok)
            				return false;
            			
            			ok = false;
            		}
		           	if (document.survey.elements[i].type == "radio" || document.survey.elements[i].type == "checkbox") 
		           	{
		           		// contrôle des boutons radio et des cases à cocher (pas de contrôle pour les questions ouvertes)
			           	if (document.survey.elements[i].checked)
			           	{
			           		// une case est cochée, valider la question
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
                            errorMsg+=" <%=resources.getString("GML.theField")%> <%=resources.getString("ContainsTooLargeText")%> <%=DBUtil.TextAreaLength%> <%=resources.getString("Characters")%>";
                            errorNb++;
                      }
                  }
             }
             // contrôle que toutes les questions aient au moins une réponse validée
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

        </script>
        </HEAD>
        
        <BODY>
<%

        Window window = gef.getWindow();
        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(surveyScc.getSpaceLabel());
        browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
        browseBar.setExtraInformation(survey.getHeader().getTitle());

        String surveyPart = displayQuestions(survey, new Integer(roundId).intValue(), gef, m_context, surveyScc, resources, settings, profile, pollingStationMode);

		// notification
   	    OperationPane operationPane = window.getOperationPane();
        String url = "ToAlertUser?SurveyId=" + surveyId;
        operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:onClick=goToNotify('"+url+"')");

        window.addBody(surveyPart);
        out.println(window.print());
} 
else if (action.equals("ViewResult")) {
	String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

         <HTML>
         <HEAD>
         <TITLE></TITLE>
         <style>


			
			body {
				margin:20px 0 0 20px;
			}
		
			/* tableau Doodle */
			.questionResults th {
				border-top:#CCC 1px solid;
			}
			
			.questionResults th, .questionResults td, .questionResults tr {
				padding:5px;
				font-size:10px;
			}
			
			.questionResults .questionResults-top th {
				background-color:#E4E4E4;
				width:150px;
			}
			
			.questionResults .questionResults-top .questionResults-vide, .questionResults tbody .questionResults-vide {
				background-color:#FFFFFF;
				border:none;
				padding:3px;
			}
			
			.questionResults .displayUserName , .questionResults .displayUserName a {
				/*display:block;*/
				background-color:#E4E4E4;
				margin-right:5px;
				font-size:10px;
				font-weight:bold;
			}
			
			.questionResults tbody tr td {
				background-color:#FFFFFF;
				min-height:33px;
			}
			
			.questionResults tbody .questionResults-Oui {
				background-color:#D5FAC5;
				text-align:center;
				font-size:10px;
			}
			
			.questionResults tbody .questionResults-Non {
				background-color:#FECBCB;
				text-align:center;
				font-size:10px;
			}
			
			.questionResults .labelAnswer {
				text-align:right;
				width:50%;
			}		
		</style>
<%
        out.println(gef.getLookStyleSheet());
%>
		 <script type="text/javascript" src="<%=iconsPath%>/util/javaScript/animation.js"></script>
         <script language="JavaScript1.2">
        
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
        
        function viewUsers(id)
        {
        	url = "ViewListResult?AnswerId="+id;
 		    windowName = "usersWindow";
 		    larg = "550";
 		    haut = "250";
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
 		    haut = "250";
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
 				   
     	</script>
         </HEAD>
         <BODY>
<%     
        survey = surveyScc.getSurvey(surveyId);

        Window window = gef.getWindow();
        Frame frame = gef.getFrame();

        BrowseBar browseBar = window.getBrowseBar();
        browseBar.setDomainName(surveyScc.getSpaceLabel());
        browseBar.setComponentName(surveyScc.getComponentLabel(),"surveyList.jsp?Action=View");
        browseBar.setExtraInformation(survey.getHeader().getTitle());

        //notification
		OperationPane operationPane = window.getOperationPane();
        String url = "ToAlertUser?SurveyId=" + surveyId;
        operationPane.addOperation(alertSrc, resources.getString("GML.notify"), "javaScript:onClick=goToNotify('"+url+"')");

        String alreadyVotes = displayAlreadyVotes(survey, surveyScc, gef, resources);
        if (!StringUtil.isDefined(choice))
        	choice = "D";
        String surveyPart = displaySurveyResult(choice, survey, gef, m_context, surveyScc, resources, isClosed, settings, frame);
        window.addBody(displayTabs(surveyScc, survey.getHeader().getPK().getId(), gef, action, profile, resources, pollingStationMode).print()+frame.printBefore()+"<center>"+alreadyVotes+"</center><BR>"+surveyPart);

        out.println(window.print());
} 
else if (action.equals("SubmitAndUpdateSurvey")) { %>
        <HTML>
        <HEAD>
        <script language="Javascript">
            function Replace() {
              location.replace("surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId=<%=surveyId%>");
            }
        </script>
        </HEAD>
        <BODY onLoad="Replace()">
<% } %>

</BODY>
</HTML>