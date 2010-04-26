<%@ page import="java.text.ParseException"%>

<%!

TabbedPane displayTabs(SurveySessionController surveyScc, String surveyId, GraphicElementFactory gef, String action, String profile, 
ResourcesWrapper resources, boolean pollingStationMode, boolean participated) {
	TabbedPane tabbedPane = gef.getTabbedPane();
	String label = "";
	if (pollingStationMode)
		label = resources.getString("PollingStation");
	else
		label = resources.getString("Survey");
	
	boolean tabValid = action.equals("ViewCurrentQuestions") || action.equals("ViewResult") || action.equals("ViewSurvey");
	if (!participated) {
	 tabValid = action.equals("ViewCurrentQuestions") || action.equals("ViewSurvey");
	}
	
	tabbedPane.addTab(label, "surveyDetail.jsp?Action=ViewCurrentQuestions&Participated="+participated+"&SurveyId="+surveyId, tabValid, true);

  if ("admin".equals(profile) && !participated) {
      tabbedPane.addTab(resources.getString("survey.results"), "surveyDetail.jsp?Action=ViewResult&Participated="+participated+"&SurveyId="+surveyId, action.equals("ViewResult"), true);
  }
	
	tabbedPane.addTab(resources.getString("survey.Comments"), "surveyDetail.jsp?Action=ViewComments&Participated="+participated+"&SurveyId="+surveyId, action.equals("ViewComments"), true);
   
	if (("admin".equals(profile) || "publisher".equals(profile)) && surveyScc.isPdcUsed())
		tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositions.jsp?Action=ViewPdcPositions&Participated="+participated+"&SurveyId="+surveyId, action.equals("ViewPdcPositions"), true);

	return tabbedPane;
}

//Display the survey header
String displaySurveyHeader(QuestionContainerHeader surveyHeader, SurveySessionController surveyScc, ResourcesWrapper resources, GraphicElementFactory gef) throws ParseException {
        String title = Encode.javaStringToHtmlString(surveyHeader.getTitle());
        String description = Encode.javaStringToHtmlParagraphe(surveyHeader.getDescription());
        String creationDate = resources.getOutputDate(surveyHeader.getCreationDate());
        String beginDate = "&nbsp;";
        if (surveyHeader.getBeginDate() != null)
            beginDate = resources.getOutputDate(surveyHeader.getBeginDate());
        String endDate = "";
        if (surveyHeader.getEndDate() != null)
            endDate = resources.getOutputDate(surveyHeader.getEndDate());
        int nbVoters = surveyHeader.getNbVoters();
        int nbRegistered = surveyHeader.getNbRegistered();
        Board board = gef.getBoard();
        String r = "";
        r += board.printBefore();
        r += "<table border=\"0\" width=\"100%\">";
        r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("GML.name")+" :</td><td width=\"100%\">"+title+"</td></tr>";
        if (StringUtil.isDefined(description))
          		r += "<tr><td class=\"textePetitBold\" nowrap valign=\"top\">"+resources.getString("SurveyCreationDescription")+" :</td><td>"+description+"</td></tr>";
        r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyCreationDate")+" :</td><td>"+creationDate+"</td></tr>";
        r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyCreationBeginDate")+" :</td><td>"+beginDate+"</td></tr>";
        if (StringUtil.isDefined(endDate))
        		r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyCreationEndDate")+" :</td><td>"+endDate+"</td></tr>";

        if (surveyScc.isParticipationMultipleUsed())
		{
	          r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyNbParticipations")+" :</td><td>"+nbVoters+"</td></tr>";
		}
		else
		{
	          r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyNbVoters")+" :</td><td>"+nbVoters+"</td></tr>";
	          r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyNbRegistered")+" :</td><td>"+nbRegistered+"</td></tr>";
	          r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyParticipationRate")+" :</td><td><B>"+Math.round(((float)nbVoters*100)/((float)nbRegistered))+" %</B></td></tr>";
		}
        r += "</table>";
        r += board.printAfter();
        return r;
}

String displaySurvey(QuestionContainerDetail survey, GraphicElementFactory gef, String m_context, SurveySessionController surveyScc, 
ResourcesWrapper resources, ResourceLocator settings, String profile, boolean pollingStationMode, boolean participated) throws SurveyException, ParseException {

        String r = "";
        Question question = null;
        Collection answers = null;
        try{
            if (survey != null) {
                QuestionContainerHeader surveyHeader = survey.getHeader();
                Collection questions = survey.getQuestions();

				r += displayTabs(surveyScc, surveyHeader.getPK().getId(), gef, "ViewSurvey", profile, resources, pollingStationMode, participated).print();

                //Display the survey header
                r += displaySurveyHeader(surveyHeader, surveyScc, resources, gef);

                if (questions != null && questions.size()>0) {
                    //Display the questions
                    r += "<form name=\"survey\">";
                    r += "<input type=\"hidden\" name=\"Action\">";
                    r += "<input type=\"hidden\" name=\"NbQuestions\" value=\""+questions.size()+"\">";
                    r += "<input type=\"hidden\" name=\"SurveyId\" value=\""+surveyHeader.getPK().getId()+"\">";
                    Iterator itQ = questions.iterator();
                    int nbTotalQuestion = questions.size();
                    int i = 1;
                    while (itQ.hasNext()) {
                          question = (Question) itQ.next();
                          r += displayQuestion(question, i, i, nbTotalQuestion, m_context, settings, surveyScc, gef, resources);
                          i++;
                    }
                    r += "<table>";
                    Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.back"), "surveyList.jsp?Action=View", false);
                    Button voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote()", false);
                    r += "<tr><td align=\"center\"><table><tr><td>"+voteButton.print()+"</td><td>"+cancelButton.print()+"</td></tr></table></td></tr>";
                    r += "</table>";
                    r += "</form>";
                } else {
                    r += "<BR>"+resources.getString("SurveyWithNoQuestions")+"<BR><BR>";
                }
            } else {
                r += "<table><tr><td>"+resources.getString("SurveyUnavailable")+"</td></tr>";
            }
        }
        catch (Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurvey",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_SURVEY",e);            
        }

        return r;
}

String displayQuestions(QuestionContainerDetail survey, int roundId, GraphicElementFactory gef, String m_context, SurveySessionController surveyScc, 
ResourcesWrapper resources, ResourceLocator settings, String profile, boolean pollingStationMode, boolean participated) throws SurveyException, ParseException {
        Board board = gef.getBoard();
        String r = "";
        try{
            Question question = null;
            Frame frame = gef.getFrame();
			
            if (survey != null) {
                QuestionContainerHeader surveyHeader = survey.getHeader();
                int nbQuestionsPerPage = surveyHeader.getNbQuestionsPerPage();
                int end = nbQuestionsPerPage * roundId;
                int begin = end - nbQuestionsPerPage;
                Collection questions = survey.getQuestions();
                int nbQuestions = 0;
                int nbTotalQuestion = questions.size();

                if (end < questions.size())
                    nbQuestions = nbQuestionsPerPage;
                else if (end == questions.size())
                    nbQuestions = (questions.size() / roundId);
                else  //derniere page
                     nbQuestions = nbQuestionsPerPage - (end - questions.size());

                if (nbQuestionsPerPage >= questions.size())
                    nbQuestions = questions.size();

				r += displayTabs(surveyScc, surveyHeader.getPK().getId(), gef, "ViewSurvey", profile, resources, pollingStationMode, participated).print();
                r += frame.printBefore();
                r += "<center>";
                r += displaySurveyHeader(surveyHeader, surveyScc, resources, gef);

               if (questions != null && questions.size()>0) {
                    //Display the questions
                    r += "<form name=\"survey\" Action=\"surveyDetail.jsp\" Method=\"POST\">";
                    r += "<input type=\"hidden\" name=\"Action\">";
                    r += "<input type=\"hidden\" name=\"RoundId\">";
                    r += "<input type=\"hidden\" name=\"NbQuestions\" value=\""+nbQuestions+"\">";
                    r += "<input type=\"hidden\" name=\"SurveyId\" value=\""+surveyHeader.getPK().getId()+"\">";
                    Iterator itQ = questions.iterator();
                    int i = 1;
                    int j = 1;
                    while (itQ.hasNext()) {
                          question = (Question) itQ.next();
                          if ((i > begin) && (i <= end)) 
                          {
                              r += displayQuestion(question, j + (nbQuestionsPerPage * (roundId-1)), j, nbTotalQuestion, m_context, settings, surveyScc, gef, resources);
                              j++;
                          }
                          i++;
                    }
               
               		if (end >= questions.size())
               		{
	                    // affichage de la zone pour les commentaires
	                    r += "<br>";
	        			r += board.printBefore();
	        			r += "<table><tr><td align=\"left\" valign=top class=\"txtlibform\">"+resources.getString("survey.Comments")+" :</td>";
	        			r += "<td align=\"left\"><textarea name=\"Comment\" rows=\"4\" cols=\"60\"></textarea></td></tr>";
	        			r += "<tr><td align=\"left\" class=\"txtlibform\">"+resources.getString("survey.AnonymousComment")+" :</td>";
	        			r += "<td align=\"left\"><input type=\"checkbox\" name=\"anonymousComment\"></td></tr>";
	        			r += "</table>";
	        			r += board.printAfter();
	        			r += "<br>";
	        		}
        			
        			r += "</form>";
        			
                    Button cancelButton = null;
                    Button voteButton = null;
                    r += frame.printMiddle();
                    if ((begin <= 0) && (end < questions.size())) 
                    {
                          voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('"+(roundId+1)+"')", false);
                          r += "<center>"+voteButton.print()+"</center>";
                    } 
                    else if (end >= questions.size()) 
                    {
                          //cancelButton = (Button) gef.getFormButton(resources.getString("GML.back"), "javascript:onClick=sendVote('"+(roundId-1)+"')", false);
                          voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('end')", false);
                          r += "<center>"+voteButton.print()+"</center>";
                    } 
                    else 
                    {
                          //cancelButton = (Button) gef.getFormButton(resources.getString("GML.back"), "javascript:onClick=sendVote('"+(roundId-1)+"')", false);
                          voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('"+(roundId+1)+"')", false);
                          r += "<center>"+voteButton.print()+"</center>";
                    }
                } else {
                    r += "<BR>"+resources.getString("SurveyWithNoQuestions")+"<BR><BR>";
                    r += frame.printMiddle();
                }
                r += "</center>";

                
            } else {
                r += "<center>"+resources.getString("SurveyUnavailable")+"</center>";
            }
            r += frame.printAfter();
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displayQuestions",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_QUESTIONS",e);          
        }
        
        return r;
}

/**
	Display question
**/
String displayQuestion(Question question, int i, int nbQuestionInPage, int nbTotalQuestion, String m_context, ResourceLocator settings, SurveySessionController surveyScc, GraphicElementFactory gef, ResourcesWrapper resources) 
{
        Collection answers = question.getAnswers();
        String r = "";
        Board board = gef.getBoard();
        r += "<br>";
        r += board.printBefore();
        r += "<table border=\"0\" cellpadding=\"2\" width=\"100%\">";
        if (!surveyScc.isPollingStationMode())
	        r += "<tr><td align=\"center\" colspan=\"2\"><b>"+resources.getString("SurveyCreationQuestion")+" n&deg;"+i+ " / " + nbTotalQuestion +"</b><BR></td></tr>";
        
        r += "<tr><td colspan=\"2\"><img src=\""+m_context+"/util/icons/mandatoryField.gif\" width=\"5\">&nbsp;&nbsp;<B><U>"+Encode.javaStringToHtmlString(question.getLabel())+"</U></B><BR/></td></tr>";
		
		// traitement du type de question
        String style = question.getStyle();
                         
        //if (question.isOpen())
        if (style.equals("open")) 
        {
              Iterator itA = answers.iterator();
              while (itA.hasNext()) 
              {
                  Answer answer = (Answer) itA.next();
                  String inputValue = answer.getPK().getId()+","+question.getPK().getId();
                  r += "<input type=\"hidden\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\">";
                  r += "<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+nbQuestionInPage+"\" cols=\"60\" rows=\"4\"></textarea></td></tr>";
              }
        } 
        else
        {
        	if (style.equals("list"))
            {
             	// liste déroulante
             	String selectedStr = "";
             	
                r += "<tr><td><select id=\"answer_"+nbQuestionInPage+"\" name=\"answer_"+nbQuestionInPage+"\" onchange=\"selectAnswer(this.value);\">";
                          			
                Iterator itA = answers.iterator();
	            while (itA.hasNext()) 
	            {
	            	Answer answer = (Answer) itA.next();
	            	String inputValue = answer.getPK().getId()+","+question.getPK().getId();
                    r += "<option value=\""+inputValue+"\" "+selectedStr+">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
	            }
	            r += "</td></tr>";
            }
            else
            {
	           String inputType = "radio";
	           String selectedStr = "";
	           //if (question.isQCM())
	           if (style.equals("checkbox"))
	           {
                    inputType = "checkbox";
                    selectedStr = "";
               }
               Iterator itA = answers.iterator();
               int isOpened = 0;
               int answerNb = 0;
               while (itA.hasNext()) 
               {
                  Answer answer = (Answer) itA.next();
                  String inputValue = answer.getPK().getId()+","+question.getPK().getId();
                  if (answer.isOpened()) 
                  {
                      	isOpened = 1;
                      	String label = "";
                      	if (StringUtil.isDefined(settings.getString("SurveyCreationDefaultSuggestionLabel")))
                      		label = settings.getString("SurveyCreationDefaultSuggestionLabel");
                      	r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR><input type=\"text\" size=\"40\" maxlength=\""+DBUtil.TextFieldLength+"\" name=\"openedAnswer_"+nbQuestionInPage+"\" value=\""+label+"\" onFocus=\"checkButton(document.survey.answer_"+nbQuestionInPage+"["+answerNb+"])\"></td></tr>";
                  } 
                  else 
                  {
	                  	if (answer.getImage() == null) 
	                  	{
	                    	r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\" "+selectedStr+"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</td></tr>";
	                    } 
	                    else 
	                    {
	                      	String imageUrl = answer.getImage();
	                      	String url = "";
	                      	if (imageUrl.startsWith("/"))
	                      	{
	                      		url = imageUrl+"&Size=266x150";
	                      	}
	                      	else
	                      	{
	                      		url = FileServer.getUrl(answer.getPK().getSpace(), answer.getPK().getComponentName(), imageUrl, imageUrl, "image/gif", settings.getString("imagesSubDirectory"));
	                      	}
	                        r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\" "+selectedStr+"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
	                        r += "<img src=\""+url+"\" border=\"0\"></td><td>";
                    	}
                  }
                  answerNb++;
              }	// {while}
        	}
        }
        r += "</table>";
        r += board.printAfter();
        return r;
}

// Pr�visualisation
  String displaySurveyPreview(QuestionContainerDetail survey, GraphicElementFactory gef, String m_context, SurveySessionController surveyScc, ResourcesWrapper resources, ResourceLocator settings) throws SurveyException, ParseException 
  {
        String r = "";
        try
        {
            Question question = null;
            Collection answers = null;
            Frame frame = gef.getFrame();
			Board board = gef.getBoard();
            if (survey != null) 
            {
                QuestionContainerHeader surveyHeader = survey.getHeader();
                Collection questions = survey.getQuestions();

                r += frame.printBefore();

                //Display the survey header
                String title = surveyHeader.getTitle();
                String description = surveyHeader.getDescription();
                String creationDate = resources.getOutputDate(new Date());
                String beginDate = "&nbsp";
                if (surveyHeader.getBeginDate() != null)
                    beginDate = resources.getOutputDate(surveyHeader.getBeginDate());
                String endDate = "";
                if (surveyHeader.getEndDate() != null)
                    endDate = resources.getOutputDate(surveyHeader.getEndDate());
                r += "<center>";
                r += board.printBefore();
                  r += "<table border=\"0\" cellpadding=\"2\" width=\"100%\">";
                  r += "<tr><td class=\"textePetitBold\">"+resources.getString("GML.name")+" :</td><td>"+Encode.javaStringToHtmlString(title)+"</td></tr>";
                  if (StringUtil.isDefined(description))	
                  		r += "<tr><td class=\"textePetitBold\" valign=\"top\">"+resources.getString("SurveyCreationDescription")+" :</td><td>"+Encode.javaStringToHtmlParagraphe(description)+"</td></tr>";
                  r += "<tr><td class=\"textePetitBold\">"+resources.getString("SurveyCreationDate")+" :</td><td>"+creationDate+"</td></tr>";
                  r += "<tr><td class=\"textePetitBold\">"+resources.getString("SurveyCreationBeginDate")+" :</td><td>"+beginDate+"</td></tr>";
                  if (StringUtil.isDefined(endDate))	
                  		r += "<tr><td class=\"textePetitBold\">"+resources.getString("SurveyCreationEndDate")+" :</td><td>"+endDate+"</td></tr>";
                r += "</table>";
                r += board.printAfter();
                r += "<BR>";

                if (questions != null && questions.size()>0) 
                {
                    //Display the questions
                    r += "<form name=\"survey\" Action=\"surveyDetail.jsp\" Method=\"Post\">";
                    r += "<input type=\"hidden\" name=\"Action\" value=\"SubmitSurvey\">";
                    Iterator itQ = questions.iterator();
                    int i = 1;
                    while (itQ.hasNext()) 
                    {
                          question = (Question) itQ.next();
                          answers = question.getAnswers();
                          //r+="<table CELLPADDING=0 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><tr><td CLASS=intfdcolor4 NOWRAP>";
                          r += board.printBefore();
                          r += "<table border=\"0\" width=\"100%\">";
                          r += "<tr><td colspan=\"2\"><B><U>"+Encode.javaStringToHtmlString(question.getLabel())+"</U></B><BR/></td></tr>";
                          
                          // traitement du type de question
                          String style = question.getStyle();
                          
                          //if (question.isOpen())
                          if (style.equals("open")) 
                          {
                          		// question ouverte
                                Iterator itA = answers.iterator();
                                int isOpened = 0;
                                r += "<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+i+"\" cols=\"60\" rows=\"4\"></textarea></td></tr>";
                          } 
                          else 
                          {
                          		if (style.equals("list"))
                          		{
                          			// liste d�roulante
                          			r += "<tr><td><select id=\"answers\" name=\"answers\" onchange=\"selectAnswer(this.value);\">";
                          			
                          			Iterator itA = answers.iterator();
	                                int isOpened = 0;
	                                while (itA.hasNext()) 
	                                {
	                                    Answer answer = (Answer) itA.next();
                   						r += "<option name=\"answer_"+i+"\" value=\"\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
   	                                }
	                                r += "</td></tr>";
                          		}
                          		else
                          		{
	                                String inputType = "radio";
	                                //if (question.isQCM())
	                                if (style.equals("checkbox"))
	                                      inputType = "checkbox";
	                                Iterator itA = answers.iterator();
	                                int isOpened = 0;
	                                while (itA.hasNext()) 
	                                {
	                                    Answer answer = (Answer) itA.next();
	                                    if (answer.isOpened()) 
	                                    {
	                                        isOpened = 1;
	                                        r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR><input type=\"text\" size=\"20\" name=\"openedAnswer_"+i+"\"></td></tr>";
	                                    } 
	                                    else 
	                                    {
	                                        if (answer.getImage() == null)
	                                              r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</td></tr>";
	                                        else 
	                                        {
	                                        	String url = "";
						                      	if (answer.getImage().startsWith("/"))
						                      	{
						                      		url = answer.getImage()+"&Size=266x150";
						                      	}
						                      	else
						                      	{
	                                            	url = FileServer.getUrl(surveyScc.getSpaceId(), surveyScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
	                                            }
	                                            r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
	                                            r += "<img src=\""+url+"\" border=\"0\"></td><td>";
	                                        }
	                                    }
	                                } // {while}
                                }
                          }
                          i++;
                          r += "</table>";
                          //r += "</td></tr></table>";
                          r += board.printAfter();
                          if (itQ.hasNext())
                              r += "<BR>";
                    } // {while}
                    r += "</form>";
                } 
                else 
                {
                     r += "<BR>"+resources.getString("SurveyWithNoQuestions")+"<BR><BR>";
                }
                r += "</center>";
                r += frame.printMiddle();
                Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "surveyList.jsp?Action=View", false);
                Button updateButton = (Button) gef.getFormButton(resources.getString("GML.modify"), "surveyDetail.jsp?Action=SubmitAndUpdateSurvey", false);
                Button voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=document.survey.submit();", false);
                ButtonPane buttonPane = gef.getButtonPane();
                buttonPane.addButton(voteButton);
                buttonPane.addButton(updateButton);
                buttonPane.addButton(cancelButton);
                r += "<center>"+buttonPane.print()+"</center>";
                r += frame.printAfter();
            } 
            else 
            {
                r += "<table><tr><td>"+resources.getString("SurveyUnavailable")+"</td></tr>";
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyPreview",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_PREVIEW",e);            
        }
        
       return r;
  }

  String displayQuestionsUpdateView(SurveySessionController surveyScc, Vector questions, GraphicElementFactory gef, String m_context, ResourceLocator settings, ResourcesWrapper resources) throws SurveyException 
  {
        String questionUpSrc = "icons/arrowUp.gif";
        String questionDownSrc = "icons/arrowDown.gif"; 
        String questionDeleteSrc = "icons/questionDelete.gif";
        String questionUpdateSrc = "icons/questionUpdate.gif";
        String r = "";
        Question question = null;
        Collection answers = null;
        String operations = "";
		Board board = gef.getBoard();
        try
        { 
            Frame frame = gef.getFrame();
            r += frame.printBefore();
            r += "<center>";

            if (questions != null && questions.size()>0) 
            {
                //Display the questions
                r += "<form name=\"survey\" Action=\"questionsUpdate.jsp\" Method=\"Post\">";
                r += "<input type=\"hidden\" name=\"Action\" value=\"SubmitQuestions\">";
                Iterator itQ = questions.iterator();
                int i = 1;
                for (int j=0; j<questions.size(); j++) 
                {
                      question = (Question) questions.get(j);
                      answers = question.getAnswers();

                      //check available operations to current question
                      operations = " ";
                      if (j!=0)
                          operations += "<a href=\"questionsUpdate.jsp?Action=UpQuestion&QId="+j+"\"><img src=\""+questionUpSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionUp")+"\" title=\""+resources.getString("QuestionUp")+"\" align=\"absmiddle\"></a> ";
                      if (j+1!=questions.size())
                          operations += "<a href=\"questionsUpdate.jsp?Action=DownQuestion&QId="+j+"\"><img src=\""+questionDownSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionDown")+"\" title=\""+resources.getString("QuestionDown")+"\" align=\"absmiddle\"></a> ";
                      operations += "<a href=\"questionsUpdate.jsp?Action=DeleteQuestion&QId="+j+"\"><img src=\""+questionDeleteSrc+"\" border=\"0\" alt=\""+resources.getString("GML.delete")+"\" title=\""+resources.getString("GML.delete")+"\" align=\"absmiddle\"></a> ";

                      r += board.printBefore();
                      r += "<table border=\"0\" width=\"100%\">";
                      r += "<tr><td colspan=\"2\" align=\"left\"><B>&#149; <U>"+Encode.javaStringToHtmlString(question.getLabel())+"</U></B>"+operations+"<BR/></td></tr>";
                      
                      // traitement du type de question
                      String style = question.getStyle();
                          
                      //if (question.isOpen())
                      if (style.equals("open")) 
                      {
                      		// question ouverte
                            Iterator itA = answers.iterator();
                            int isOpened = 0;
                            r += "<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+i+"\" cols=\"60\" rows=\"4\"></textarea></td></tr>";
                      } 
                      else 
                      {
                       		if (style.equals("list"))
                       		{
                       			// liste d�roulante
                       			r += "<tr><td><select id=\"answers\" name=\"answers\" onchange=\"selectAnswer(this.value);\">";
                          			
                       			Iterator itA = answers.iterator();
	                            int isOpened = 0;
	                            while (itA.hasNext()) 
	                            {
	                                Answer answer = (Answer) itA.next();
 	            					
                    				r += "<option name=\"answer_"+i+"\" value=\"\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
	                            }
	                            r += "</td></tr>";
                        	}
                          	else
                          	{
                            	String inputType = "radio";
                            	if (style.equals("checkbox"))
	                                inputType = "checkbox";
                             	Iterator itA = answers.iterator();
                            	int isOpened = 0;
                            	while (itA.hasNext()) 
                            	{
                                	Answer answer = (Answer) itA.next();
                                	if (answer.isOpened()) 
                                	{
                                    	isOpened = 1;
                                    	r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR><input type=\"text\" size=\"20\" name=\"openedAnswer_"+i+"\"></td></tr>";
                                	} 
                                	else 
                                	{
                                    	if (answer.getImage() == null)
                                          	r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</td></tr>";
                                    	else 
                                    	{
	                                    	String url = "";
					                      	if (answer.getImage().startsWith("/"))
					                      	{
					                      		url = answer.getImage()+"&Size=266x150";
					                      	}
					                      	else
					                      	{
	                                            url = FileServer.getUrl(surveyScc.getSpaceId(), surveyScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
	                                        }
	                                        r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
	                                        r += "<img src=\""+url+"\" border=\"0\"></td><td>";
                                    	}
                                	}
                            	} // {while}
                            }
                      }
                      i++;
                      r += "</table>";
                      r += board.printAfter();
                      if (j<questions.size()-1)
                            r += "<BR>";
                } // {for}
                r += "</form>";
            } 
            else 
            {
                r += "<BR>"+settings.getString("SurveyWithNoQuestions")+"<BR><BR>";
            }
            r += "</center>";
            r += frame.printMiddle();
            Button voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "questionsUpdate.jsp?Action=SendQuestions", false);
            r += "<center>"+voteButton.print()+"</center>";
            r += frame.printAfter();
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displayQuestionsUpdateView",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_UPDATEVIEW",e);           
        }

       return r;
  }

  String displaySurveyResult(String choice, QuestionContainerDetail survey, GraphicElementFactory gef, String m_context, 
  SurveySessionController surveyScc, ResourcesWrapper resources, boolean isClosed, 
  ResourceLocator settings, Frame frame, boolean participated) throws SurveyException, ParseException {
		return displaySurveyResult("unknown", "unknown", "all", null, choice, survey, gef, m_context, surveyScc, resources, isClosed, 
		settings, frame, participated);
  }
	
	  String displaySurveyResult(String userName, String userId, String styleView, Collection resultsByUser, String choice, 
  QuestionContainerDetail survey, GraphicElementFactory gef, String m_context, SurveySessionController surveyScc, 
  ResourcesWrapper resources, boolean isClosed, ResourceLocator settings, Frame frame) throws SurveyException, ParseException {
	   return  displaySurveyResult(userName, userId, styleView, resultsByUser, choice, survey, gef, m_context, surveyScc, 
      resources, isClosed, settings, frame, true);
	
	}
	
  String displaySurveyResult(String userName, String userId, String styleView, Collection resultsByUser, String choice, 
  QuestionContainerDetail survey, GraphicElementFactory gef, String m_context, SurveySessionController surveyScc, 
  ResourcesWrapper resources, boolean isClosed, ResourceLocator settings, Frame frame, boolean participated) throws SurveyException, ParseException {
        Board board = gef.getBoard();
        String r = "";
        
        // rechercher le commentaire de l'utilisateur
        String userComment = "";
        Collection comments = survey.getComments();
        Iterator it = comments.iterator();
        while (it.hasNext()) {
          Comment comment = (Comment) it.next();
          if (userId.equals(comment.getUserId())) {
            userComment = comment.getComment();
          }
        }
        
        try{
            if (survey != null) {
                QuestionContainerHeader surveyHeader = survey.getHeader();
                Collection questions = survey.getQuestions();

				//Display the survey header
                	String surveyId = surveyHeader.getPK().getId();
                	String title = surveyHeader.getTitle();
                	String description = surveyHeader.getDescription();
                	String creationDate = resources.getOutputDate(surveyHeader.getCreationDate());
                	String beginDate = "&nbsp;";
               		if (surveyHeader.getBeginDate() != null)
                    	beginDate = resources.getOutputDate(surveyHeader.getBeginDate());
                	String endDate = "&nbsp;";
                	if (surveyHeader.getEndDate() != null)
                    	endDate = resources.getOutputDate(surveyHeader.getEndDate());
                	else
                    	endDate = "";
                	int nbVoters = surveyHeader.getNbVoters();
                	int nbRegistered = surveyHeader.getNbRegistered();
                	int participationRate = Math.round(((float)nbVoters*100)/((float)nbRegistered));
                	boolean anonymous = surveyHeader.isAnonymous();
				
				if (!styleView.equals("user"))
				{
                 	r += "<center>";
                	r += board.printBefore();
                	r += "<table cellspacing=\"3\" cellpadding=\"0\" border=\"0\" width=\"100%\">"; 
                	r += " <tr><td class=\"textePetitBold\" nowrap>"+resources.getString("GML.name")+" :</td><td width=\"90%\">"+Encode.javaStringToHtmlString(title)+"</td></tr>";
                	if (StringUtil.isDefined(description))
                		r += "<tr><td class=\"textePetitBold\" nowrap valign=\"top\">"+resources.getString("SurveyCreationDescription")+" :</td><td>"+Encode.javaStringToHtmlParagraphe(description)+"</td></tr>";
                	r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyCreationDate")+" :</td><td>"+creationDate+"</td></tr>";
                	r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyCreationBeginDate")+" :</td><td>"+beginDate+"</td></tr>";
                	if (StringUtil.isDefined(endDate))
                		r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("SurveyCreationEndDate")+" :</td><td>"+endDate+"</td></tr>";	
	                if (!anonymous && !styleView.equals("user"))
				  	{
				  		// l'enquête n'est pas anonyme, proposer le choix d'affichage
			           	String selectedStr = "";
		                r += "<tr><td class=\"textePetitBold\" nowrap>"+resources.getString("survey.choice")+" :</td>";
	                   	if (choice.equals("D"))
	                		choice = "C";
	                	else
	                		choice = "D";
	                    r += "<td>";
	                    r += "<a href=\"surveyDetail.jsp?Action=ViewResult&Participated="+participated+"&SurveyId="+surveyId+"&Choice="+choice+"\">";
	                    r += "Afficher la vue "+resources.getString("survey."+choice);
	                    r +="</a></td></tr>";
					}
	                r += "</table>";
	                r += board.printAfter();
	                r += "<BR>";
                
	                r += board.printBefore();
	                
	                r += "<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\" width=\"100%\" align=center>";
	                r += "<TR><TD align=\"center\" class=\"intfdcolor\" height=\"1\" colspan=\"3\"></TD></TR>";
					if (surveyScc.isParticipationMultipleUsed())
					{
		                r += "<tr><td align=\"center\"><b>"+resources.getString("SurveyNbParticipations")+"</b></td>";
		            }	
		            else
		            {
		                r += "<tr><td align=\"center\"><b>"+resources.getString("SurveyNbVoters")+"</b></td>";
		            }    
					if (!surveyScc.isParticipationMultipleUsed())
	                {
	                	r += "<td align=\"center\"><b>"+resources.getString("SurveyNbRegistered")+"</b></td>";
		                if (isClosed)
		                	r += "<td align=\"center\" ><b>"+resources.getString("SurveyAbstentionRate")+"</b></td></tr>";
		                else 
		                	r += "<td align=\"center\"><b>"+resources.getString("SurveyParticipationRate")+"</b></td></tr>";
	                }
	                r += "<TR><TD align=\"center\" class=\"intfdcolor\" height=\"1\" colspan=\"3\"></TD></TR>";
	                r += "<tr><td align=\"center\">"+nbVoters;
	                if (!anonymous)
		                {
		                	// affichage de l'icone des users
		                	r += "<a href=\"javaScript:onClick=viewAllUsers('"+surveyId+"');\"><img src=\"icons/info.gif\" border=\"0\" align=\"absmiddle\" width=\"15\" height=\"15\"></a>";
		
		                }
		            r += "</td>";
	                if (!surveyScc.isParticipationMultipleUsed())
	                {
		                r += "<td align=\"center\">"+nbRegistered+"</td>";
		                if (isClosed)
		                 	r += "<td align=\"center\" >"+(100-participationRate)+"&nbsp;%</td></tr>";
		                else 
		                 	r += "<td align=\"center\">"+participationRate+"&nbsp;%</td></tr>";
	                }
	                r += "<TR><TD align=\"center\" class=\"intfdcolor\" height=\"1\" colspan=\"3\"></TD></TR>";
	                r += "</table>";
	                r += board.printAfter();
	                r += "<BR>";
	            }	
	            else
	            {
	            	r += "<center>";
                	r += board.printBefore();
                	r += "<table border=\"0\" width=\"100%\">"; 
                	r += " <tr><td class=\"textePetitBold\" nowrap>"+resources.getString("survey.participation")+" : </td><td width=\"90%\">"+Encode.javaStringToHtmlString(userName)+"</td></tr>";
	                if (!userComment.equals("")) {
	                   r += " <tr><td class=\"textePetitBold\" nowrap>"+resources.getString("survey.Comment")+" : </td><td width=\"90%\">"+Encode.javaStringToHtmlString(userComment)+"</td></tr>";
	                } 
	                r += "</table>";
	                r += board.printAfter();
	                r += "<br/>";
	            }		
               
                if (questions != null && questions.size()>0) {
                    r += board.printBefore();
                    r += "<table border=\"0\" cellspacing=\"1\" width=\"100%\" class=\"questionResults\" >";
                    r += "<thead>";
                    Iterator itQ = questions.iterator();
                    int i=1;
                    while (itQ.hasNext()) 
                    {
                          Question question = (Question) itQ.next();
                          Collection answers = question.getAnswers();
                          if (!surveyScc.isPollingStationMode())
								r += "<tr><th align=\"center\" colspan=\"2\" ><b>"+resources.getString("SurveyCreationQuestion")+" n&deg;"+i+"</b></th></tr>";
                          r += "<tr><th colspan=\"2\" align=\"left\"><img src=\""+m_context+"/util/icons/mandatoryField.gif\" width=5>&nbsp;&nbsp;<B><U>"+Encode.javaStringToHtmlString(question.getLabel())+"</U></B></th></tr>";
                          r += " </thead>";
                          r += "<tbody>";
                           
                          String style = question.getStyle(); 
                          if (styleView.equals("user"))
                          {
                          		// affichage des résults par user
                          		if (style.equals("open")) 
		                        {
		                        	r += displayOpenAnswersToQuestionByUser(userId, false, question.getPK().getId(), surveyScc);
		                        } 
		                        else 
		                        {
		                        	r += displaySurveyResultChartByUser(resultsByUser, false, answers, m_context, settings);
		                        }
                          }
                          else
                          {       
	                          if (!anonymous && choice.equals("D"))
	                          {
	 	                      	  // affichage des résultat de façon nominative
		                      	  if (style.equals("open")) 
		                          {
		                              r += displayOpenAnswersToQuestionNotAnonymous(question.getPK().getId(), surveyScc);
		                          } 
		                          else 
		                          {
		                              r += displaySurveyResultChartNotAnonymous(question, answers, m_context, settings, surveyScc);
		                          }
		                      }
		                      else
		                      {
		                          // traitement de l'affichage des questions ouvertes
		                      	  //if (question.isOpen())
		                      	  if (style.equals("open")) 
		                          {
		                              r += displayOpenAnswersToQuestion(anonymous, question.getPK().getId(), surveyScc);
		                          } else 
		                          {
		                          	  int nbUsers = surveyScc.getUserByQuestion(new ForeignPK(question.getPK())).size();
		                              r += displaySurveyResultChart(anonymous, answers, m_context, settings, nbUsers);
		                          }
		                      }
		                  }    
	                      r += "</td></tr>";
	                      r += " <tr class=\"questionResults-top\"><td class=\"questionResults-vide\" colspan=\"2\">&nbsp;</td></tr>";
	                      i++;
                     }
                     r += " </tbody></table>";
                     r += board.printAfter();
                 } else {
                     r += "<BR>"+resources.getString("SurveyWithNoQuestions")+"<BR><BR>";
                 }                
                 r += frame.printAfter();
	        } else {
	             r += "<center>"+resources.getString("SurveyUnavailable")+"</center>";
	        }
	        r += "</center>";
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyResult",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_CHART",e);           
        }

    return r;
}

  String displayOpenAnswersToQuestion(boolean anonymous, String questionId, SurveySessionController surveyScc) throws SurveyException {
        String r = "";
        try{
            //fetch the answers to this open question
            Collection openAnswers = surveyScc.getSuggestions(questionId);
            Iterator it = openAnswers.iterator();
            String answer = "";
            while (it.hasNext()) {
                QuestionResult qR = (QuestionResult) it.next();
                answer = Encode.javaStringToHtmlParagraphe(qR.getOpenedAnswer());
                if (!StringUtil.isDefined(answer))
                    answer = surveyScc.getString("NoResponse");
                r += "<tr><td colspan=\"2\" align=\"left\">&#149; "+answer+"<BR></td></tr>";
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displayOpenAnswersToQuestion",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_ANSWER",e);         
        }
        return r;
  }
  
  String displayOpenAnswersToQuestionNotAnonymous(String questionId, SurveySessionController surveyScc) throws SurveyException {
        String r = "";
        try{
            //fetch the answers to this open question
            Collection openAnswers = surveyScc.getSuggestions(questionId);
            Iterator it = openAnswers.iterator();
            String answer = "";
            while (it.hasNext()) {
                QuestionResult qR = (QuestionResult) it.next();
                answer = Encode.javaStringToHtmlParagraphe(qR.getOpenedAnswer());
                if (!StringUtil.isDefined(answer))
                    answer = surveyScc.getString("NoResponse");
                String userId = qR.getUserId();
                UserDetail userDetail = surveyScc.getUserDetail(userId);
                String userName = userDetail.getDisplayedName();
                 r += "<tr><td class=\"displayUserName\"><a href=\"javaScript:onClick=viewResultByUser('"+userId+"','"+userName+"');\">"+Encode.javaStringToHtmlString(userName)+"</a></td><td align=\"left\" >"+answer+"<BR></td></tr>";
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displayOpenAnswersToQuestion",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_ANSWER",e);         
        }
        return r;
  }
  
  String displayOpenAnswersToQuestionByUser(String userId, boolean anonymous, String questionId, SurveySessionController surveyScc) throws SurveyException {
        String r = "";
        try{
            //fetch the answers to this open question
            Collection openAnswers = surveyScc.getSuggestions(questionId);
            Iterator it = openAnswers.iterator();
            String answer = "";
            while (it.hasNext()) {
                QuestionResult qR = (QuestionResult) it.next();
                answer = Encode.javaStringToHtmlParagraphe(qR.getOpenedAnswer());
                if (!StringUtil.isDefined(answer))
                    answer = surveyScc.getString("NoResponse");
                String questionUserId = qR.getUserId();
                if (userId.equals(questionUserId))
                	r += "<tr><td colspan=\"2\" align=\"left\">&#149; "+answer+"<BR></td></tr>";
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displayOpenAnswersToQuestion",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_ANSWER",e);         
        }
        return r;
  }

  String displaySurveyResultChart(boolean anonymous, Collection answers, String m_context, ResourceLocator settings, int nbUsers) throws SurveyException 
  {
        String r = "";
        try
        {
            if (answers != null) 
            {
                Iterator itA = answers.iterator();
                int nbVoters = 0;
                while (itA.hasNext()) 
                {
                    Answer answer = (Answer) itA.next();
                    nbVoters += answer.getNbVoters();
                }
                itA = answers.iterator();
                int percentageForThisAnswer = 0;
                int nbSquareForThisAnswer = 0;
                while (itA.hasNext()) 
                {
                    Answer answer = (Answer) itA.next();
                    nbSquareForThisAnswer = 0;
                    percentageForThisAnswer = 0;
                    
                    if (nbUsers > 0) 
	                {
	                    percentageForThisAnswer = Math.round((answer.getNbVoters()*100f)/nbUsers);
	                    nbSquareForThisAnswer = (answer.getNbVoters() * 100) / nbUsers;
	                }
                    if (answer.isOpened()) 
                    {
                        if (answer.getNbVoters() == 0)
                            r += "<tr><td align=\"left\" nowrap >"+Encode.javaStringToHtmlString(answer.getLabel())+"</td><td>";
                        else
                        	r += "<tr><td align=\"left\" nowrap ><A href=\"javaScript:onClick=viewSuggestions('"+answer.getQuestionPK().getId()+"');\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</a></td><td>";
                    } 
                    else 
                    {
                        if (answer.getImage() == null)
                              r += "<tr><td class=\"labelAnswer\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</td><td>";
                        else 
                        {
                        	String url = "";
	                      	if (answer.getImage().startsWith("/"))
	                      	{
	                      		url = answer.getImage()+"&Size=266x150";
	                      	}
	                      	else
	                      	{
                    		  	url = FileServer.getUrl(answer.getPK().getSpace(), answer.getPK().getComponentName(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
                    		}
                            r += "<tr><td class=\"labelAnswer\" >"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
                            r += "<img src=\""+url+"\" border=\"0\"></td><td width=\"60%\">";
                        }
                    }
                    r += "<img src=\"icons/square.gif\" border=\"0\" width=\""+nbSquareForThisAnswer+"\" height=\"5\">";
                    r += "&nbsp;"+percentageForThisAnswer+"%";
                    if (!anonymous && nbSquareForThisAnswer != 0)
                    {
                    	// l'enqu�te n'est pas anonyme et le % de r�ponse n'est pas nul : afficher l'icone pour visualiser les users
                       	r += "<a href=\"javaScript:onClick=viewUsers('"+answer.getPK().getId()+"');\"><img src=\"icons/info.gif\" border=\"0\" align=\"absmiddle\" width=\"15\" height=\"15\"></a>";
                    }
                    r += "</td>";
                } // {while}
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyResultChart",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_RESULT",e);         
        }
        return r;
  }
  
  String displaySurveyResultChartNotAnonymous(Question question, Collection answers, String m_context, ResourceLocator settings, SurveySessionController surveyScc) throws SurveyException 
  {
        String r = "";
        try
        {
            if (answers != null) 
            {
                Iterator itA = answers.iterator();
                Collection users = new ArrayList();
                r += "<tr><td colspan=\"2\"><table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\">"; 
                r+= " <thead> <tr class=\"questionResults-top\"> <th class=\"questionResults-vide\"></th>";
                Hashtable answerValues = new Hashtable();
                int rang = 0;
                while (itA.hasNext()) 
                {
                	rang = rang + 1;
                    Answer answer = (Answer) itA.next();
                    // affichage de la ligne des differentes réponses possibles
                    r += "<th> "+Encode.javaStringToHtmlString(answer.getLabel())+" </th>";
                    answerValues.put(answer.getPK().getId(), new Integer(rang));
                }
                r += "</tr> </thead>";
                r += "<tbody>";
                
                users = surveyScc.getUserByQuestion(new ForeignPK(question.getPK()));
                String saveUser = "";
                Iterator itU = users.iterator();
                while (itU.hasNext())
                {
                 	String user = (String) itU.next();
                 	String userId = user.split("/")[0];
                	String userName = user.split("/")[1];
                	int position = 1;
                	if (!saveUser.equals(userName))
                	{
	                	r += "<tr><td align=\"left\" width=\"40%\" class=\"displayUserName\"><a href=\"javaScript:onClick=viewResultByUser('"+userId+"','"+userName+"');\">"+Encode.javaStringToHtmlString(userName)+"</a></td>";
	                	// rechercher les réponses pour cet utilisateur
	                	String value;
	                	Collection results = surveyScc.getResultByUser(userId, new ForeignPK(question.getPK()));
	                	Iterator it = results.iterator();
	                	position = 1;
	                	while (it.hasNext())
	                	{
	                		QuestionResult qr = (QuestionResult) it.next();
	                		value = qr.getAnswerPK().getId();
	                		
	                		Integer n = (Integer) answerValues.get(value);
	                		int valueColonne = n.intValue();
	                		// décaller pour se trouver dans la bonne colonne
	                		while (position <= valueColonne)
	                		{
		                		if (valueColonne == position)
	              				{
	              					// on est sur la bonne colonne
	              					r += "<td class=\"questionResults-Oui\"> X </td>";
	              				}
	              				else
	              				{
	              					// on décale 
	              					r += "<td class=\"questionResults-Non\">&nbsp;</td>";
	              				}
	              				position = position + 1;
	              			}
	                		saveUser = userName;
	                	}
	                	// completer la ligne avec des cases à "vide"
		              	while (position <= rang)
		              	{
		              		r += "<td class=\"questionResults-Non\"> </td>";
		              		position = position + 1;
		              	}
                	}
                	
                }
                r += " </tbody>";
                r += "<tr><td></td>";
                itA = answers.iterator();
                int percentageForThisAnswer = 0;
                while (itA.hasNext()) 
                {
	                percentageForThisAnswer = 0;
	                Answer answer = (Answer) itA.next();
	                if (!users.isEmpty()) 
	                {
	                    percentageForThisAnswer = Math.round((answer.getNbVoters()*100f)/users.size());
	                }
	                // afficher le %
	                r += "<td align=\"center\">"+percentageForThisAnswer+"%</td>";
	            }
	            r += "</tr>";
	            r += "</table></td></tr>";
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyResultChart",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_RESULT",e);         
        }
        return r;
  }
  
  String displaySurveyResultChartByUser(Collection resultsByUser, boolean anonymous, Collection answers, String m_context, ResourceLocator settings) throws SurveyException 
  {
        String r = "";
        try
        {
            if (answers != null) 
            {
                Iterator itA = answers.iterator();
                int nbVoters = 0;
                while (itA.hasNext()) 
                {
                    Answer answer = (Answer) itA.next();
                    nbVoters += answer.getNbVoters();
                }
                itA = answers.iterator();
                while (itA.hasNext()) 
                {
                    Answer answer = (Answer) itA.next();
                    if (answer.isOpened()) 
                    {
                        if (answer.getNbVoters() == 0)
                            r += "<tr><td class=\"labelAnswer\" >"+Encode.javaStringToHtmlString(answer.getLabel())+"</td><td>";
                        else
                        	r += "<tr><td class=\"labelAnswer\" ><A href=\"javaScript:onClick=viewSuggestions('"+answer.getQuestionPK().getId()+"');\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</a></td><td>";
                    } 
                    else 
                    {
                        if (answer.getImage() == null)
                              r += "<tr><td class=\"labelAnswer\" >"+Encode.javaStringToHtmlString(answer.getLabel())+"</td><td>";
                        else 
                        {
                        	String url = "";
	                      	if (answer.getImage().startsWith("/"))
	                      	{
	                      		url = answer.getImage()+"&Size=266x150";
	                      	}
	                      	else
	                      	{
                    		  	url = FileServer.getUrl(answer.getPK().getSpace(), answer.getPK().getComponentName(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
                    		}
                            r += "<tr><td class=\"labelAnswer\" >"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
                            r += "<img src=\""+url+"\" border=\"0\" width=\"60%\"></td><td>";
                        }
                        // mettre en valeur cette réponse si c'est le choix de l'utilisateur
                        if (resultsByUser.contains(answer.getPK().getId()))
	  	                	r += "<img src=\""+m_context+"/util/icons/finishedTask.gif\" border=\"0\" valign=\"center\" width=\"15\" height=\"15\">";
                        
                    }
                    r += "</td>";
                } // {while}
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyResultChart",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_RESULT",e);         
        }
        return r;
  }
  
  String displaySurveyComments(SurveySessionController surveyScc, QuestionContainerDetail survey, GraphicElementFactory gef, 
  ResourcesWrapper resources, String profile, boolean pollingStationMode, boolean participated) throws SurveyException {
		Board board = gef.getBoard();
		Frame frame = gef.getFrame();
        String r = "";
        boolean oneComment = false;
        boolean first = true;
        QuestionContainerHeader surveyHeader = survey.getHeader();
        r += displayTabs(surveyScc, surveyHeader.getPK().getId(), gef, "ViewComments", profile, resources, pollingStationMode, participated).print();
        r += frame.printBefore();
        try
        {
            if (survey != null) 
            {
                Collection comments = survey.getComments();
                r += "<center>";
                r += board.printBefore();
                r += "<TABLE border=0 cellspacing=0 cellpadding=5 width=\"98%\" align=center>";
                if (comments != null) {
                    if (comments.size() > 0) {
                        Iterator it = comments.iterator();
                        while (it.hasNext()) {
                          String userName = "";
                          UserDetail user = null;
                          Comment comment = (Comment) it.next();
                          if (comment.getComment() != null && comment.getComment().trim().length() > 0) {
                                  if (first)
                                        r += "<tr><td class=txtnav><B>"+resources.getString("survey.Comments")+"</B><BR></td></tr>";
                                  r += "<tr>";
                                  r += "<td valign=\"top\"><blockquote>";
                                  if (comment.isAnonymous()) {
                                      userName = resources.getString("survey.AnonymousComment");
                                  } else {
                                      user = surveyScc.getUserDetail(comment.getUserId());
                                      if (user != null)
                                        userName = user.getFirstName() + " " + user.getLastName();
                                      else
                                        userName = resources.getString("UnknownUser");
                                  }
                                  r += "<p>&#149; <B>"+userName+"</B> - "+resources.getOutputDate(comment.getDate())+"<br>";
                                    r +="&nbsp;&nbsp;"+ Encode.javaStringToHtmlParagraphe(comment.getComment())+"</p>";
                                r += "</blockquote></td>";
                              r += "</tr>";
                              oneComment = true;
                              first = false;
                          }
                       }
                    }
                    if (!oneComment)
                        r += "<tr><td align=center class=\"txttitrecol\"><B>"+resources.getString("survey.NoComment")+"</B></td></tr>";
                } else {
                     r += "<tr><td align=center class=\"txttitrecol\"><B>"+resources.getString("survey.NoComment")+"</B></td></tr>";
                }
                r += "</table>";
                r += board.printAfter();
                r += frame.printAfter();
                r += "</center>";
            }
        } 
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyComments",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_RESULT",e);         
        }
       return r;
  }

%>