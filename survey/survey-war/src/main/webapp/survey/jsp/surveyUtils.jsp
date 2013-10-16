<%@page import="com.silverpeas.util.StringUtil"%>
<%@page import="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerHeader"%>
<%@ page import="java.text.ParseException"%>
<%@ page import="com.stratelia.webactiv.SilverpeasRole"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>

<%!

TabbedPane displayTabs(SurveySessionController surveyScc, String surveyId, GraphicElementFactory gef, String action, String profile,
ResourcesWrapper resources, boolean pollingStationMode, boolean participated) {
	TabbedPane tabbedPane = gef.getTabbedPane();
	String label = "";
	if (pollingStationMode) {
		label = resources.getString("PollingStation");
	} else {
		label = resources.getString("Survey");
  }
	boolean tabValid = action.equals("ViewCurrentQuestions") || action.equals("ViewResult") || action.equals("ViewSurvey");
	if (!participated) {
	 tabValid = action.equals("ViewCurrentQuestions") || action.equals("ViewSurvey");
	}

	tabbedPane.addTab(label, "surveyDetail.jsp?Action=ViewCurrentQuestions&Participated="+participated+"&SurveyId="+surveyId, tabValid, true);

  if ((SilverpeasRole.admin.toString().equals(profile) ||
      SilverpeasRole.publisher.toString().equals(profile)) &&
      !participated) {
      tabbedPane.addTab(resources.getString("survey.results"), "surveyDetail.jsp?Action=ViewResult&Participated="+participated+"&SurveyId="+surveyId, action.equals("ViewResult"), true);
  }

	tabbedPane.addTab(resources.getString("survey.Comments"), "surveyDetail.jsp?Action=ViewComments&Participated="+participated+"&SurveyId="+surveyId, action.equals("ViewComments"), true);

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
                    Button cancelButton = gef.getFormButton(resources.getString("GML.back"), "surveyList.jsp?Action=View", false);
                    Button voteButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote()", false);
                    r += "<tr><td align=\"center\"><table><tr><td>"+voteButton.print()+"</td><td>"+cancelButton.print()+"</td></tr></table></td></tr>";
                    r += "</table>";
                    r += "</form>";
                } else {
                    r += "<br>"+resources.getString("SurveyWithNoQuestions")+"<br><br>";
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

                if (end < questions.size()) {
                    nbQuestions = nbQuestionsPerPage;
                } else if (end == questions.size()) {
                    nbQuestions = (questions.size() / roundId);
                } else { //derniere page
                     nbQuestions = nbQuestionsPerPage - (end - questions.size());
                }

                if (nbQuestionsPerPage >= questions.size()) {
                    nbQuestions = questions.size();
                }
				r += displayTabs(surveyScc, surveyHeader.getPK().getId(), gef, "ViewSurvey", profile, resources, pollingStationMode, participated).print();
                r += frame.printBefore();
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
						//Mode anonyme ou enquete anonyme -> force les commentaires a�etre tous anonymes
						String anonymousCommentCheck = "";
						String anonymousCommentDisabled = "";
						if(surveyScc.isAnonymousModeEnabled() || surveyHeader.isAnonymous()) {
							anonymousCommentCheck = "checked";
							anonymousCommentDisabled = "disabled";
						}

	                    // affichage de la zone pour les commentaires
	                    r += "<br>";
	        			r += board.printBefore();
	        			r += "<table><tr><td align=\"left\" valign=top class=\"txtlibform\">"+resources.getString("survey.Comments")+" :</td>";
	        			r += "<td align=\"left\"><textarea name=\"Comment\" rows=\"4\" cols=\"60\"></textarea></td></tr>";
	        			r += "<tr><td align=\"left\" class=\"txtlibform\">"+resources.getString("survey.AnonymousComment")+" :</td>";
	        			r += "<td align=\"left\"><input type=\"checkbox\" name=\"anonymousComment\" "+anonymousCommentCheck+" "+anonymousCommentDisabled+"></td></tr>";
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
                          voteButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('"+(roundId+1)+"')", false);
                          r += "<center>"+voteButton.print()+"</center>";
                    }
                    else if (end >= questions.size())
                    {
                          voteButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('end')", false);
                          r += "<center>"+voteButton.print()+"</center>";
                    }
                    else
                    {
                          voteButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('"+(roundId+1)+"')", false);
                          r += "<center>"+voteButton.print()+"</center>";
                    }
                } else {
                    r += "<br/>"+resources.getString("SurveyWithNoQuestions")+"<br/><br/>";
                    r += frame.printMiddle();
                }

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

		String openAnswerId="";
		Iterator itA = answers.iterator();
		while (itA.hasNext())
	        {
			Answer answer = (Answer) itA.next();
			if (answer.isOpened()) {
				openAnswerId = answer.getPK().getId()+","+question.getPK().getId();
			}
		}

                r += "<tr><td><select id=\"answer_"+nbQuestionInPage+"\" name=\"answer_"+nbQuestionInPage+"\" onchange=\"if(this.value=='"+openAnswerId+"'){document.getElementById('openanswer"+nbQuestionInPage+"').style.display='block'}else{document.getElementById('openanswer"+nbQuestionInPage+"').style.display='none'};\">";

                itA = answers.iterator();
	            while (itA.hasNext())
	            {
	            	Answer answer = (Answer) itA.next();
			String inputValue = answer.getPK().getId()+","+question.getPK().getId();
			r += "<option value=\""+inputValue+"\" "+selectedStr+">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
	            }
		    r += "<input type=\"text\" name=\"openedAnswer_"+nbQuestionInPage+"\" id=\"openanswer"+nbQuestionInPage+"\" value=\"\" style=\"display: none\"/>";
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
                      	r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\"></td><td align=\"left\" width=\"100%\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"<BR><input type=\"text\" size=\"40\" maxlength=\""+DBUtil.getTextFieldLength()+"\" name=\"openedAnswer_"+nbQuestionInPage+"\" value=\""+label+"\" onFocus=\"checkButton(document.survey.answer_"+nbQuestionInPage+"["+answerNb+"])\"></td></tr>";
                  }
                  else
                  {
	                  	if (answer.getImage() == null)
	                  	{
	                    	r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\" "+selectedStr+"></td><td align=\"left\" width=\"100%\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</td></tr>";
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
	                      		url = FileServerUtils.getUrl(answer.getPK().getComponentName(), imageUrl, imageUrl, "image/gif", settings.getString("imagesSubDirectory"));
	                      	}
	                        r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+nbQuestionInPage+"\" value=\""+inputValue+"\" "+selectedStr+"></td><td align=\"left\" width=\"100%\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"<BR>";
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

// Previsualisation
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
                          //r+="<table cellpadding=0 cellspacing=2 border=0 width=\"98%\" CLASS=intfdcolor><tr><td class=intfdcolor4 nowrap>";
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
                          			// drop down list
                          			r += "<tr><td><select id=\"answers\" name=\"answers\" onchange=\"if(this.value=='openanswer_"+i+"'){document.getElementById('openanswer"+i+"').style.display='block'}else{document.getElementById('openanswer"+i+"').style.display='none'};\">";

                          			Iterator itA = answers.iterator();
	                                int isOpened = 0;
	                                while (itA.hasNext())
	                                {
	                                    Answer answer = (Answer) itA.next();
					    if (answer.isOpened()) {
						r += "<option name=\"openanswer_"+i+"\" value=\"openanswer_"+i+"\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
					    } else {
                   				r += "<option name=\"answer_"+i+"\" value=\"\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
					    }
   	                                }
					r += "<input type=\"text\" id=\"openanswer"+i+"\" name=\"answer_"+i+"\" value=\"\" style=\"display:none\"/>";
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
	                                            	url = FileServerUtils.getUrl(surveyScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
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
                          r += board.printAfter();
                          if (itQ.hasNext())
                              r += "<BR>";
                    } // {while}
                    r += "</form>";
                }
                else
                {
                     r += "<br/>"+resources.getString("SurveyWithNoQuestions")+"<br/><br/>";
                }
                r += frame.printMiddle();
                Button cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "surveyList.jsp?Action=View", false);
                Button updateButton = gef.getFormButton(resources.getString("GML.modify"), "surveyDetail.jsp?Action=SubmitAndUpdateSurvey", false);
                Button voteButton = gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=document.survey.submit();", false);
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

  String displayQuestionsUpdateView(SurveySessionController surveyScc, List questions, GraphicElementFactory gef, String m_context, ResourceLocator settings, ResourcesWrapper resources) throws SurveyException
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
                       			r += "<tr><td><select id=\"answers\" name=\"answers\" onchange=\"if(this.value=='openanswer_"+i+"'){document.getElementById('openanswer"+i+"').style.display='block'}else{document.getElementById('openanswer"+i+"').style.display='none'};\">";

                       			Iterator itA = answers.iterator();
	                                while (itA.hasNext())
	                                {
	                                    Answer answer = (Answer) itA.next();
					    if (answer.isOpened()) {
						r += "<option name=\"openanswer_"+i+"\" value=\"openanswer_"+i+"\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
					    } else {
                   				r += "<option name=\"answer_"+i+"\" value=\"\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
					    }
   	                                }
					r += "<input type=\"text\" id=\"openanswer"+i+"\" name=\"answer_"+i+"\" value=\"\" style=\"display:none\"/>";
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
	                                            url = FileServerUtils.getUrl(surveyScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
	                                        }
	                                        r += "<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
	                                        r += "<img src=\""+url+"\" border=\"0\"></td><td>";
                                    	}
                                	}
                            	}
                            }
                      }
                      i++;
                      r += "</table>";
                      r += board.printAfter();
                      if (j<questions.size()-1)
                            r += "<br/>";
                }
                r += "</form>";
            }
            else
            {
                r += "<br/>"+settings.getString("SurveyWithNoQuestions", "")+"<br/><br/>";
            }
            r += frame.printMiddle();
            Button voteButton = gef.getFormButton(resources.getString("GML.validate"), "questionsUpdate.jsp?Action=SendQuestions", false);
            r += "<center>"+voteButton.print()+"</center>";
            r += frame.printAfter();
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displayQuestionsUpdateView",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_UPDATEVIEW",e);
        }

       return r;
  }

String displaySurveyResultOfUser(String userName, String userId, Collection resultsByUser,
  QuestionContainerDetail survey, GraphicElementFactory gef, String m_context, SurveySessionController surveyScc,
  ResourcesWrapper resources, ResourceLocator settings, String profile) throws SurveyException, ParseException {
	   
  Board board = gef.getBoard();
  String r = "";

  // rechercher le commentaire de l'utilisateur
  Comment userCommentDetail = null;
  String userComment = "";
  Collection<Comment> comments = survey.getComments();
  for (Comment comment : comments) {
    if (userId.equals(comment.getUserId())) {
      userComment = comment.getComment();
      userCommentDetail = comment;
    }
  }

  try {
    if (survey != null) {
	    Collection<Question> questions = survey.getQuestions();
	    
       	r += board.printBefore();
       	r += "<table border=\"0\" cellspacing=\"5\" cellpadding=\"5\" width=\"100%\">";
       	r += " <tr><td class=\"textePetitBold\" nowrap>"+resources.getString("survey.participationOf")+" : </td><td width=\"90%\">"+Encode.javaStringToHtmlString(userName)+"</td></tr>";
        if (!userComment.equals("") && userCommentDetail != null && !userCommentDetail.isAnonymous()) {
          r += " <tr><td class=\"textePetitBold\" nowrap valign=\"top\">"+resources.getString("survey.Comment")+" : </td><td width=\"90%\">"+Encode.javaStringToHtmlParagraphe(userComment)+"</td></tr>";
        }
        r += "</table>";
        r += board.printAfter();
        r += "<br/>";
      
      if (SilverpeasRole.admin.toString().equals(profile) ||
          SilverpeasRole.publisher.toString().equals(profile)) {
        
        r += "<div class=\"surveyResult\">";
			   
	      if (questions != null && !questions.isEmpty()) {
	        r += board.printBefore();
	        r += "<table border=\"0\" cellspacing=\"1\" width=\"100%\" class=\"questionResults\" >";
	        r += "<thead>";
	        int i=1;
	        for (Question question : questions) {
	          Collection answers = question.getAnswers();
	          if (!surveyScc.isPollingStationMode()) {
	            r += "<tr><th align=\"center\" colspan=\"2\"><b>"+resources.getString("SurveyCreationQuestion")+" n&deg;"+i+"</b></th></tr>";
	          }
	          r += "<tr><th colspan=\"2\" align=\"left\"><img src=\""+m_context+"/util/icons/mandatoryField.gif\" width=\"5\"/>&nbsp;&nbsp;<b><u>"+Encode.javaStringToHtmlString(question.getLabel())+"</u></b></th></tr>";
	          r += " </thead>";
	          r += "<tbody>";
	
	          String style = question.getStyle();
              // Display result for each user
              if (style.equals("open")) {
                r += displayOpenAnswersToQuestionByUser(userId, false, question.getPK().getId(), surveyScc);
              } else {
             	r += displaySurveyResultChartByUser(resultsByUser, false, answers, m_context, settings);
              }
	          r += "</td></tr>";
	          r += "<tr class=\"questionResults-top\"><td class=\"questionResults-vide\" colspan=\"2\">&nbsp;</td></tr>";
	          i++;
	        } //end while
	        r += " </tbody></table>";
	        r += board.printAfter();
	      } else {//questions.size == 0
	        r += "<br/>"+resources.getString("SurveyWithNoQuestions")+"<br/><br/>";
	      }
      }
      r += "</div>";
    } else {//survey == null
      r += "<center>"+resources.getString("SurveyUnavailable")+"</center>";
    }
  } catch( Exception e){
    throw new  SurveyException("SurveyUtils_JSP.displaySurveyResult",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_CHART",e);
  }
  return r;
}

String displaySurveyResult(String choice, QuestionContainerDetail survey, GraphicElementFactory gef, String m_context,
    SurveySessionController surveyScc, ResourcesWrapper resources, boolean isClosed,
    ResourceLocator settings, boolean participated, String profile, HttpServletRequest request) throws SurveyException, ParseException {
  Board board = gef.getBoard();
  String r = "";

  try {
    if (survey != null) {
	    QuestionContainerHeader surveyHeader = survey.getHeader();
	    Collection<Question> questions = survey.getQuestions();
	    Collection<QuestionResult> votes = survey.getCurrentUserVotes();

	    //Display the survey header
     	String surveyId = surveyHeader.getPK().getId();
     	String title = surveyHeader.getTitle();
     	String description = surveyHeader.getDescription();
     	String creationDate = resources.getOutputDate(surveyHeader.getCreationDate());
     	String beginDate = "&nbsp;";
    	if (surveyHeader.getBeginDate() != null) {
    	  beginDate = resources.getOutputDate(surveyHeader.getBeginDate());
    	}
     	String endDate = "&nbsp;";
     	if (surveyHeader.getEndDate() != null) {
        endDate = resources.getOutputDate(surveyHeader.getEndDate());
     	} else {
     	  endDate = "";
     	}
     	int nbVoters = surveyHeader.getNbVoters();
     	int nbRegistered = surveyHeader.getNbRegistered();
     	int participationRate = Math.round(((float)nbVoters*100)/((float)nbRegistered));
     	boolean anonymous = surveyHeader.isAnonymous();

     	//Mode anonyme -> force les enquetes a�etre toutes anonymes
     	if(surveyScc.isAnonymousModeEnabled()) {
     	  anonymous = true;
     	}
     	
     	int resultMode = surveyHeader.getResultMode();
     	int resultView = surveyHeader.getResultView();
        
        if (resultMode == QuestionContainerHeader.DELAYED_RESULTS) {
	        
	        r += "<div class=\"rightContent\">";
	        
	        List<SimpleDocument> listDocument = surveyScc.getAllSynthesisFile(surveyId);
	        if(listDocument != null && !listDocument.isEmpty()) {
	          r += "<div class=\"attachments bgDegradeGris\">";
	          r += "  <div class=\"bgDegradeGris header\">";
	          r += "    <h4 class=\"clean\">"+resources.getString("survey.synthesisFile")+"</h4>";
	          r += "  </div>";
	          r += "  <ul id=\"attachmentList\">";
	        
	          for(SimpleDocument simpleDocument : listDocument) {
	            String url = m_context +  simpleDocument.getAttachmentURL();
	            String permalink = m_context + "/File/"+simpleDocument.getId();
	            String dateDocument = resources.getOutputDate(simpleDocument.getCreated());
	            if(simpleDocument.getUpdated() != null) {
	              dateDocument = resources.getOutputDate(simpleDocument.getUpdated());
	            }
	            
	            r += "    <li class=\"attachmentListItem\">";
			        r += "       <span class=\"lineMain\">";
			        r += "        <img class=\"icon\" src=\""+simpleDocument.getDisplayIcon()+"\">";
			        r += "        <a target=\"_blank\" href=\""+url+"\">"+simpleDocument.getFilename()+"</a>";
			        r += "       </span>";
			        r += "       <span class=\"lineSize\">";
			        r += "        <a href=\""+permalink+"\"><img border=\"0\" title=\""+resources.getString("survey.attachmentPermalink")+"\" alt=\""+resources.getString("survey.attachmentPermalink")+"\" src=\""+m_context+"/util/icons/link.gif\"></a>";
			        r +=            FileRepositoryManager.formatFileSize(simpleDocument.getSize())+" - "+dateDocument;
			        r += "       </span>";
			        r += "    </li>";
	          }
	        
	          r += "  </ul>";
	          r += "</div>";
	        }
	        r += "<div class=\"bgDegradeGris\" id=\"surveyInfoPublication\">";
	        r += "  <p id=\"permalinkInfo\">";
	        r += "    <a title=\""+resources.getString("survey.CopySurveyLink")+"\" href=\""+m_context+"/Survey/"+surveyId+"\">";
	        r += "      <img border=\"0\" alt=\""+resources.getString("survey.CopySurveyLink")+"\" src=\""+m_context+"/util/icons/link.gif\">";
	        r += "    </a>"+resources.getString("GML.permalink")+"<br/>";
	        r += "    <input type=\"text\" value=\""+URLManager.getServerURL(request)+surveyHeader.getPermalink()+"\" onFocus=\"select();\" class=\"inputPermalink\">";
	        r += "  </p>";
	        r += "</div>";
	        r += "</div>";
        }
        
        r += "<div class=\"principalContent\">";
        r += " <h2 class=\"eventName\">"+Encode.javaStringToHtmlString(title)+"</h2>";
        r += " <div class=\"eventInfo\">";
        r += "   <div class=\"surveyDate\">";
        r += "     <div class=\"bloc\">";
        r += "       <span class=\"eventBeginDate\">"+resources.getString("SurveyCreationDate")+" : ";
        r += "       "+creationDate;
        r += "       </span>";
        r += "       <span class=\"eventBeginDate\">"+resources.getString("SurveyCreationBeginDate"); 
        r += "       "+beginDate;
        r += "       </span>";
        if (StringUtil.isDefined(endDate)) {
          r += "      <span class=\"eventEndDate\">"+resources.getString("SurveyCreationEndDate");
          r += "      "+endDate;
          r += "      </span>";
        }
        r += "     </div>";
        r += "   </div>";
        r += "   <div class=\"surveyParticipation\">";
        r += "     <div class=\"bloc\">";
        r += "       <span>"+resources.getString("survey.participation")+" : "+nbVoters;
        if (!surveyScc.isParticipationMultipleUsed()) {
          r += "/"+nbRegistered;
        }
        if (!anonymous &&
            ((SilverpeasRole.admin.toString().equals(profile) ||
            SilverpeasRole.publisher.toString().equals(profile)) ||
            (resultMode == QuestionContainerHeader.IMMEDIATE_RESULTS ||
            (resultMode == QuestionContainerHeader.DELAYED_RESULTS &&
            (resultView == QuestionContainerHeader.DETAILED_DISPLAY_RESULTS || 
            resultView == QuestionContainerHeader.TWICE_DISPLAY_RESULTS))))) {
          // affichage de l'icone des users
          r += "       <a href=\"javaScript:onClick=viewAllUsers('"+surveyId+"');\"><img src=\"icons/info.gif\" border=\"0\" align=\"absmiddle\" width=\"15\" height=\"15\"></a>";
        }
        r += "       </span>";
        if (!surveyScc.isParticipationMultipleUsed()) {
          r += "       <span>"+resources.getString("survey.thatToSay");
          if (isClosed) {
            r += "       "+(100-participationRate)+"&nbsp;%&nbsp;"+resources.getString("survey.abstentionRate");
          } else {
            r += "       "+participationRate+"&nbsp;%&nbsp;"+resources.getString("GML.preview.dialog.title.of")+" "+resources.getString("survey.participation");
          }
          r += "       </span>";
        }
        r += "     </div>";
        r += "   </div>";
        r += "   <div class=\"surveyUserParticipation\">";
        
        if (votes != null) {
          if (votes.size() > 0) {
            Iterator<QuestionResult> it = votes.iterator();
            if (it.hasNext()) {
              QuestionResult vote = (QuestionResult) it.next();
              r += "     <div class=\"bloc\">";
              r += "       <span>"+resources.getString("YouHaveAlreadyParticipate");
              r += "       "+resources.getOutputDate(vote.getVoteDate())+"</span>";
                      
              if (surveyScc.isParticipationMultipleAllowedForUser()) {
                String labelButton = resources.getString("Survey.revote");
                if (surveyScc.isPollingStationMode()) {
                  labelButton = resources.getString("PollingStation.revote");
                }
                r += "       <span><a href=\"surveyDetail.jsp?Action=Vote&SurveyId="+
                         survey.getHeader().getId() + "\">" + labelButton + "</a></span>";
              }
              r += "     </div>";
            }
          }
        }
        
        r += "   </div>";
        r += "   <br clear=\"left\">&nbsp;";
        r += " </div>";
        if (StringUtil.isDefined(description)) {
        	r += " <div class=\"surveyDesc\">"+Encode.javaStringToHtmlParagraphe(description)+"</div>";
        }
        r += "</div>";
      
      if ((SilverpeasRole.admin.toString().equals(profile) ||
          SilverpeasRole.publisher.toString().equals(profile)) ||
          resultMode == QuestionContainerHeader.IMMEDIATE_RESULTS ||
           (resultMode == QuestionContainerHeader.DELAYED_RESULTS &&
           resultView != QuestionContainerHeader.NOTHING_DISPLAY_RESULTS)) {
        
        r += "<div class=\"surveyResult\">";
			   
		  if (!anonymous) {
				  // l'enquete n'est pas anonyme, proposer le choix d'affichage
	        r += "   <div class=\"sousNavBulle\">";
	        r += "    <p>"+resources.getString("survey.results")+" "+resources.getString("survey.choice")+" : ";
	        if((SilverpeasRole.admin.toString().equals(profile) ||
              SilverpeasRole.publisher.toString().equals(profile)) ||
              resultView == QuestionContainerHeader.TWICE_DISPLAY_RESULTS) {
            String active = "";
            if (choice.equals("C")) {
              active = "active";
            }
            r += "    <a onClick=\"changeScope('classic', '"+participated+"', '"+surveyId+"')\" href=\"#\" class=\""+active+"\" id=\"scope-classic\">"+resources.getString("survey.C")+"</a>";
            active = "";
            if (choice.equals("D")) {
             active = "active";
            }
            r += "    <a onClick=\"changeScope('detail', '"+participated+"', '"+surveyId+"')\" href=\"#\" class=\""+active+"\" id=\"scope-detail\">"+resources.getString("survey.D")+"</a>";
          } else if((SilverpeasRole.admin.toString().equals(profile) ||
	            SilverpeasRole.publisher.toString().equals(profile)) ||
	            resultView == QuestionContainerHeader.CLASSIC_DISPLAY_RESULTS) {
	          choice = "C";
	          r += "    <a onClick=\"changeScope('classic', '"+participated+"', '"+surveyId+"')\" href=\"#\" class=\"active\" id=\"scope-classic\">"+resources.getString("survey.C")+"</a>";
	      } else if((SilverpeasRole.admin.toString().equals(profile) ||
	            SilverpeasRole.publisher.toString().equals(profile)) ||
	            resultView == QuestionContainerHeader.DETAILED_DISPLAY_RESULTS) {
	          choice = "D";
	          r += "    <a onClick=\"changeScope('detail', '"+participated+"', '"+surveyId+"')\" href=\"#\" class=\"active\" id=\"scope-detail\">"+resources.getString("survey.D")+"</a>";
	        }
	        r += "    </p>";
	        r += "   </div>";
	      }
			   
	      if (questions != null && !questions.isEmpty()) {
	        r += board.printBefore();
	        r += "<table border=\"0\" cellspacing=\"1\" width=\"100%\" class=\"questionResults\">";
	        int i=1;
	        for (Question question : questions) {
	          Collection answers = question.getAnswers();
	          String questionDecorator = "";
	          if (!surveyScc.isPollingStationMode()) {
	            questionDecorator = resources.getString("SurveyCreationQuestion")+" n&deg;"+i+"&nbsp;-&nbsp;";
	          }
	          r += "<tr><th colspan=\"2\" align=\"left\"><img src=\""+m_context+"/util/icons/mandatoryField.gif\" width=\"5\">&nbsp;&nbsp;"+questionDecorator+Encode.javaStringToHtmlString(question.getLabel())+"</th></tr>";
	
	            String style = question.getStyle();
	            if (!anonymous && choice.equals("D")) {
	              // display not anonymous result
	           	  if (style.equals("open")) {
	           	   r += displayOpenAnswersToQuestionNotAnonymous(question.getPK().getId(), surveyScc);
	              } else {
	                r += displaySurveyResultChartNotAnonymous(question, answers, m_context, settings, surveyScc);
	              }
	            } else {
	              // traitement de l'affichage des questions ouvertes
	              if (style.equals("open")) {
	                r += displayOpenAnswersToQuestion(anonymous, question.getPK().getId(), surveyScc);
	              } else {
	                int nbUsers = surveyScc.getUserByQuestion(new ForeignPK(question.getPK())).size();
	                r += displaySurveyResultChart(anonymous, answers, m_context, settings, nbUsers);
	              }
	            }
	          r += "</td></tr>";
	          r += "<tr><td class=\"questionSeparator\" colspan=\"2\">&nbsp;</td></tr>";
	          i++;
	        } //end while
	        r += "</table>";
	        r += board.printAfter();
	      } else {//questions.size == 0
	        r += "<br/>"+resources.getString("SurveyWithNoQuestions")+"<br/><br/>";
	      }
	      r += "</div>";
      } else {
        r += "<div class=\"inlineMessage\">"+resources.getString("survey.result.thankyou")+"</div>";
      }
    } else {//survey == null
      r += "<center>"+resources.getString("SurveyUnavailable")+"</center>";
    }
  } catch( Exception e){
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
                r += "<tr><td colspan=\"2\" align=\"left\">&#149; "+answer+"<br></td></tr>";
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
                 r += "<tr><td class=\"displayUserName\" width=\"40%\"><a href=\"javaScript:onClick=viewResultByUser('"+userId+"','"+userName+"');\">"+Encode.javaStringToHtmlString(userName)+"</a></td><td class=\"freeAnswer\">"+answer+"</td></tr>";
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
                    		  	url = FileServerUtils.getUrl(answer.getPK().getComponentName(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
                    		}
                            r += "<tr><td class=\"labelAnswer\" >"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
                            r += "<img src=\""+url+"\" border=\"0\"></td><td width=\"60%\">";
                        }
                    }
                    r += "<img src=\"icons/square.gif\" border=\"0\" width=\""+nbSquareForThisAnswer+"\" height=\"5\">";
                    r += "&nbsp;"+percentageForThisAnswer+"%";
                    if (!anonymous && nbSquareForThisAnswer != 0)
                    {
                    	// l'enquête n'est pas anonyme et le % de réponse n'est pas nulle : afficher l'icone pour visualiser les users
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
                Map answerValues = new HashMap();
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
		              		r += "<td class=\"questionResults-Non\">&nbsp;</td>";
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
                    		  	url = FileServerUtils.getUrl(answer.getPK().getComponentName(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
                    		}
                            r += "<tr><td class=\"labelAnswer\" >"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
                            r += "<img src=\""+url+"\" border=\"0\" width=\"60%\"/></td><td>";
                        }
                        // mettre en valeur cette réponse si c'est le choix de l'utilisateur
                        if (resultsByUser.contains(answer.getPK().getId()))
	  	                	r += "<img src=\""+m_context+"/util/icons/finishedTask.gif\" border=\"0\" valign=\"center\" width=\"15\" height=\"15\"/>";

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
                                  if (comment.isAnonymous() || surveyScc.isAnonymousModeEnabled()) {
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
            }
        }
        catch( Exception e){
            throw new  SurveyException("SurveyUtils_JSP.displaySurveyComments",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_RESULT",e);
        }
       return r;
  }

%>
