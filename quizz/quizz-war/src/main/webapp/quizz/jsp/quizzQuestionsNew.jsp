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
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />
<jsp:useBean id="questionsVector" scope="session" class="java.util.ArrayList" />
<jsp:useBean id="questionsResponses" scope="session" class="java.util.HashMap" />

<%@ include file="checkQuizz.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>


<%
String    profile     = (String) request.getAttribute("Profile");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

String exportSrc = m_context + "/util/icons/export.gif";
String copySrc = m_context + "util/icons/copy.gif";
%>

<%!
//Display the quizz header
String displayQuizzHeader(QuizzSessionController quizzScc, QuestionContainerHeader quizzHeader, ResourcesWrapper resources, GraphicElementFactory gef) {
  String title = quizzHeader.getTitle();
  String description = quizzHeader.getDescription();
  String comment = quizzHeader.getComment();

  Board board = gef.getBoard();
  String r = "";
  r += board.printBefore();
  r += "<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\" width=\"100%\">";
  r += "<tr><td>";
  r += "<span class=\"titreFenetre\">"+EncodeHelper.javaStringToHtmlString(title)+"</span>";
  r += "<blockquote>";
  r += "<span class=\"sousTitreFenetre\">"+EncodeHelper.javaStringToHtmlParagraphe(description)+"</span>";
  if (StringUtil.isDefined(comment)) {
    r += "<br><br><span class=txttitrecol>"+resources.getString("QuizzNotice")+"</span>&nbsp;&nbsp;";
    r += EncodeHelper.javaStringToHtmlParagraphe(comment)+"";
  }
  r += "</blockquote>";
  r += "</td></tr></table>";
  r += board.printAfter();
  return r;
}

String displayQuizz(QuestionContainerDetail quizz, GraphicElementFactory gef, String m_context, QuizzSessionController quizzScc, ResourcesWrapper resources, ResourceLocator settings, JspWriter out) throws QuizzException {
  String r = "";
  Question question = null;
  Collection<Answer> answers = null;
	try{
		if (quizz != null) {
			QuestionContainerHeader quizzHeader = quizz.getHeader();
			Collection<Question> questions = quizz.getQuestions();
			//Display the quizz header
			r += displayQuizzHeader(quizzScc, quizzHeader, resources, gef);

			//Display the questions
			r += "<form name=\"quizz\" Action=\"quizzQuestionsNew.jsp\" Method=\"Post\">";
			r += "<input type=\"hidden\" name=\"Action\">";
			r += "<input type=\"hidden\" name=\"NbQuestions\" value=\""+questions.size()+"\">";
			r += "<input type=\"hidden\" name=\"QuizzId\" value=\""+quizzHeader.getPK().getId()+"\">";

			Iterator<Question> itQ = questions.iterator();
			int i = 1;
			while (itQ.hasNext()) {
				  question = (Question) itQ.next();
				  r += displayQuestion(quizzScc, question, i, m_context, settings, gef, resources);
				  i++;

			}
		} else {
			r += "<table><tr><td>"+resources.getString("QuizzUnavailable")+"</td></tr>";
		}
	} catch (Exception e){
		throw new QuizzException ("quizzQuestionNew_JSP.displayQuizz",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_QUIZZ",e);
	}

  return r;
}

List<String> displayQuestions(QuestionContainerDetail quizz, int roundId,GraphicElementFactory gef, String m_context,QuizzSessionController quizzScc, ResourcesWrapper resources, ResourceLocator settings, Frame frame, JspWriter out) throws QuizzException {
  String r = "";
  String s = "";
  Question question = null;
  List<String> displayQuestions = new ArrayList<String>();
	try{
		if (quizz != null) {
			QuestionContainerHeader quizzHeader = quizz.getHeader();
			int nbQuestionsPerPage = quizzHeader.getNbQuestionsPerPage();
			int end = nbQuestionsPerPage * roundId;
			int begin = end - nbQuestionsPerPage;
			Collection<Question> questions = quizz.getQuestions();
			int nbQuestions = 0;
			if (end <= questions.size()) {
				nbQuestions = nbQuestionsPerPage;
			} else {
				nbQuestions = questions.size() % nbQuestionsPerPage;
      }
			r += displayQuizzHeader(quizzScc, quizzHeader, resources, gef);

			//Display the questions
			r += "<form name=\"quizz\" Action=\"quizzQuestionsNew.jsp\" Method=\"Post\">";
			r += "<input type=\"hidden\" name=\"Action\">";
			r += "<input type=\"hidden\" name=\"RoundId\">";
			r += "<input type=\"hidden\" name=\"NbQuestions\" value=\""+nbQuestions+"\">";
			r += "<input type=\"hidden\" name=\"QuizzId\" value=\""+quizzHeader.getPK().getId()+"\">";
			Iterator<Question> itQ = questions.iterator();
			int i = 1;
			int j = 1;
			while (itQ.hasNext()) {
				  question = (Question) itQ.next();
				  if ((i > begin) && (i <= end)) {
					  r += displayQuestion(quizzScc, question, j, m_context, settings, gef, resources);
					  j++;
				  }
				  i++;
			}
			s += "<table width=\"98%\" border=\"0\" cellspacing=\"2\" cellpadding=\"5\">";
			s += "<tr><td nowrap>";
			s += "<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\" width=\"100%\">";
			s += "<tr><td>";

			Button cancelButton = null;
			Button voteButton = null;
			if ((begin <= 0) && (end < questions.size())) {
				  voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('"+(roundId+1)+"')", false);
				  s += "<tr><td align=\"center\"><table><tr><td class=intfdcolor51 align=center>"+voteButton.print()+"</td></tr></table></td></tr>";
			} else if (end >= questions.size()) {
				  voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('end')", false);
				  s += "<tr><td align=\"center\"><table><tr><td class=intfdcolor51 align=center>"+voteButton.print()+"</td></tr></table></td></tr>";
			} else {
				  voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=sendVote('"+(roundId+1)+"')", false);
				  s += "<tr><td align=\"center\"><table><tr><td class=intfdcolor51 align=center>"+voteButton.print()+"</td></tr></table></td></tr>";
			}
			 s += "</table>";
			 s += "</td></tr>";
			 s += "</table>";
			 s += "</form>";
		} else {
			r += "<table><tr><td>"+resources.getString("QuizzUnavailable")+"</td></tr></table>";
		}
		displayQuestions.add(r);
		displayQuestions.add(s);
	} catch (Exception e){
		throw new QuizzException ("quizzQuestionNew_JSP.displayQuestion",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_QUESTIONS",e);
	}

  return displayQuestions;
}

String displayQuestion(QuizzSessionController quizzScc, Question question, int i, String m_context, ResourceLocator settings, GraphicElementFactory gef, ResourcesWrapper resources) {
  Collection<Answer> answers = question.getAnswers();
  Board board = gef.getBoard();
  String r = "";
  r += "<br>";
  r += board.printBefore();
  r += "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\">";
  r += "<tr><td nowrap>";
  r += "<span class=txtlibform>&nbsp;<b>&#149;</b>&nbsp;"+EncodeHelper.javaStringToHtmlString(question.getLabel())+"&nbsp;</span>";
  r += " - "+question.getNbPointsMax()+" pts</td>";
  r += "<td nowrap align=\"right\">";
  r += "<input type=\"hidden\" name=\"cluePenalty_"+i+"\" value=\"0\">";

  if (question.getClue() != null && !question.getClue().equals(""))
	{
			r += "<a href=\"#\" onClick=\"view_clue("+i+","+question.getFatherId()+","+question.getPK().getId()+","+question.getCluePenalty()+")\">"+resources.getString("QuizzSeeClue")+"</a>";
			r += " (" + resources.getString("QuizzPenalty") + "=" + question.getCluePenalty() + " pts)";
	}
	else
		r += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
  r += "</table>";
  r += board.printAfter();
  r += "<br>";
  r += board.printBefore();
  r += "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\" >";

  if (question.isOpen()) {
        Iterator<Answer> itA = answers.iterator();
        while (itA.hasNext()) {
            Answer answer = (Answer) itA.next();
            String inputValue = answer.getPK().getId()+","+question.getPK().getId();
            r += "<input type=\"hidden\" name=\"answer_"+i+"\" value=\""+inputValue+"\">";
            r += "<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+i+"\" cols=\"20\" rows=\"3\"></textarea></td></tr>";
        }
  }
  else
  {
  	String style = question.getStyle();
  	if (style.equals("list"))
  	{
  		// liste d�roulante
       	String selectedStr = "";

          r += "<tr><td><select id=\"answer_"+i+"\" name=\"answer_"+i+"\" >";

          Iterator<Answer> itA = answers.iterator();
        while (itA.hasNext())
        {
        	Answer answer = (Answer) itA.next();
        	String inputValue = answer.getPK().getId()+","+question.getPK().getId();
              r += "<option value=\""+inputValue+"\" "+selectedStr+">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</option>";
        }
        r += "</td></tr>";
  	}
  	else
   	{
        String inputType = "radio";
        if (style.equals("checkbox"))
        	inputType = "checkbox";
          Iterator<Answer> itA = answers.iterator();
          int isOpened = 0;
          while (itA.hasNext())
          {
              Answer answer = (Answer) itA.next();
              r += "<tr><td colspan=\"2\"><table width=\"98%\"><tr>";
              String inputValue = answer.getPK().getId()+","+question.getPK().getId();

              // traitement de l'affichage des images
              if (answer.getImage() == null)
	      {
            	    r += "<td align=\"left\">&nbsp;</td>";
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
		        	url = FileServerUtils.getUrl(answer.getPK().getComponentName(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
		        }
		        r += "<td width=\"50%\"><img src=\""+url+"\" align=\"left\"></td>";
	      }
              if (answer.isOpened()) {
                  isOpened = 1;
                  r += "<td align=\"left\" width=\"1%\">";
                  r += "<input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\""+inputValue+"\"></td>";
                  r += "<td align=\"left\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"";
                  r += "<input type=\"text\" size=\"20\" name=\"openedAnswer_"+i+"\"></td></tr>";
                  r += "<tr><td colspan=2><br></td></tr></table></td></tr>";
              } else {
                  r += "<td align=\"left\" width=\"1%\">";
                  r += "<input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\""+inputValue+"\"></td>";
                  r += "<td width=\"100%\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</td>";
              }
              r += "</tr></table></td></tr>";
          }
  	}
  }
  r += "</table>";
  r += board.printAfter();
  return r;
}


List<String> displayQuestionResult(QuestionContainerDetail quizz, Question question, int i, String m_context, ResourceLocator settings, QuizzSessionController quizzScc, boolean solutionAllowed, ResourcesWrapper resources) {

  Collection<Answer> answers = question.getAnswers();
  Collection<QuestionResult> questionResults = question.getQuestionResults();
  List<String> displayQuestionResult = new ArrayList<String>();
  String r = "";
  // User answers
  int questionUserScore = 0;
  Iterator<QuestionResult> itB = questionResults.iterator();
  List<String> qrUser = new ArrayList<String>();
  try {
		while (itB.hasNext()) {
			QuestionResult questionResult = (QuestionResult) itB.next();
			questionUserScore += questionResult.getNbPoints();
			if (question.isOpen()) {
				qrUser.add(questionResult.getOpenedAnswer());
			} else {
				qrUser.add(questionResult.getAnswerPK().getId());
			}
		}
		if (question.getNbPointsMax() < questionUserScore) {
			questionUserScore = question.getNbPointsMax();
		} else if (question.getNbPointsMin() > questionUserScore) {
			questionUserScore = question.getNbPointsMin();
		}
		r = "<br><table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr align=center><td><table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\">";
		r += "<tr><td nowrap width=\"41%\"><span class=txtlibform>&nbsp;<b>&#149</b>&nbsp;"+Encode.javaStringToHtmlString(question.getLabel())+"&nbsp;</span></td>";
		r += "<td class=\"quizzscore\" width=25% align=\"center\">"+resources.getString("ScoreLib")+" : "+questionUserScore+"/"+question.getNbPointsMax()+" pts</td>";
		r += "<td align=\"center\" nowrap>";
		r += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		r += "</td></tr>";
		if (question.isOpen()) {
			Iterator<Answer> itA = answers.iterator();
			while (itA.hasNext()) {
				Answer answer = (Answer) itA.next();
				String inputValue = answer.getPK().getId()+","+question.getPK().getId();
				r += "<input type=\"hidden\" name=\"answer_"+i+"\" value=\""+inputValue+"\">";
				if (solutionAllowed) {
					r += "<tr><td><textarea disabled name=\"openedAnswer_"+i+"\" cols=\"40\" rows=\"3\">"+qrUser.get(0)+"</textarea></td>";
					r += "&nbsp;&nbsp;<td><textarea disabled name=\"openedAnswer_"+i+"\" cols=\"40\" rows=\"3\">"+answer.getLabel()+"</textarea></td></tr>";
				}
				else
					r += "<tr><td colspan=\"2\"><textarea disabled name=\"openedAnswer_"+i+"\" cols=\"40\" rows=\"3\">"+qrUser.get(0)+"</textarea></td></tr>";
			}
		} else {
			String style = question.getStyle();

			// on est dans le cas de l'affichage,
			// alors on pr�sente les listes d�roulante sous forme de bouton radio
			// pour faire apparaitre le choix de l'utilisateur et les commentaires
			String inputType = "radio";
			//if (question.isQCM())
			if (style.equals("checkbox"))
                  inputType = "checkbox";
			Iterator<Answer> itA = answers.iterator();
			int isOpened = 0;
			while (itA.hasNext()) {
				Answer answer = (Answer) itA.next();
				String inputStatus = "";
				r += "<tr><td colspan=\"3\"><table><tr>";
				String inputValue = answer.getPK().getId()+","+question.getPK().getId();

				// traitement de l'affichage des images
                  if (answer.getImage() == null)
			      {
                	  r += "<td width=50>&nbsp;</td>";
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
				        	url = FileServerUtils.getUrl(answer.getPK().getComponentName(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
				        }
				        r += "<td><img src=\""+url+"\" align=\"left\"></td>";
			      }

				if (answer.isOpened()) {
					isOpened = 1;
					r += "<td width=\"40px\" align=\"center\"><input disabled type=\""+inputType+"\" name=\"answer_"+i+"\" value=\""+inputValue+"\"></td><td align=\"left\">";
					if (answer.isSolution() && solutionAllowed)
						r += "<b class=textePetitBoldVert>"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</b>";
					else
						r += EncodeHelper.javaStringToHtmlString(answer.getLabel());
					r += "<BR><input disabled type=\"text\" size=\"20\" name=\"openedAnswer_"+i+"\"></td></tr>";
				} else {
					inputStatus = "";
					if (qrUser!=null){
						for (int cpt=0; cpt<qrUser.size(); cpt++){
							if (qrUser.get(cpt).equals(answer.getPK().getId())) {
								inputStatus = "checked";
							}
						}
					}
					r += "<td width=\"40px\" align=\"center\"><input disabled type=\""+inputType+"\" name=\"answer_"+i+"\" value=\""+inputValue+"\" "+inputStatus+"></td><td align=\"left\">";
					if (answer.isSolution() && solutionAllowed)
						r += "<b class=textePetitBoldVert>"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</b><BR>";
					else
						r += EncodeHelper.javaStringToHtmlString(answer.getLabel())+"<BR>";
					r += "</td>";
					if (solutionAllowed){
   						r += "</tr><tr>";
						r += "<td align=\"left\" colspan=2>";
						r += "<b>"+EncodeHelper.javaStringToHtmlParagraphe(answer.getComment())+"</b></td>";
					}
				}
				r += "</tr></table></td></tr>";
			}
		}
		r += "</table></td></tr></table>";
	} catch (NumberFormatException e) {
		SilverTrace.info("Quizz","quizzQuestionsNew_JSP.displayQuestionResult","Quizz.EX_BAD_NUMBER_FORMAT");
	}
	displayQuestionResult.add(r);
	displayQuestionResult.add(Integer.toString(questionUserScore));
	displayQuestionResult.add(Integer.toString(question.getNbPointsMax()));

	return displayQuestionResult;
}

String displayQuizzHeaderPreview(QuizzSessionController quizzScc, QuestionContainerHeader quizzHeader, ResourcesWrapper resources, GraphicElementFactory gef) {
  Board board = gef.getBoard();
	String title = EncodeHelper.javaStringToHtmlString(quizzHeader.getTitle());
  String description = EncodeHelper.javaStringToHtmlParagraphe(quizzHeader.getDescription());
  String comment = EncodeHelper.javaStringToHtmlParagraphe(quizzHeader.getComment());
  String r = "";
  r += board.printBefore();
  r += "<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\" width=\"98%\">";
  r += "<tr><td>";
  r += "<span class=\"TitreFenetre\">"+title+"</span>";
  r += "<blockquote>";
  r += "<span class=\"sousTitreFenetre\">"+description+"</span>";
  if (comment != null) {
    r += "<br><br><span class=txttitrecol>"+comment+"</span>";
  }
  r += "</blockquote>";
  r += "</td></tr></table>";
  r += board.printAfter();

  return r;
}



String displayQuizzPreview(QuestionContainerDetail quizz, GraphicElementFactory gef, String m_context,QuizzSessionController quizzScc, ResourcesWrapper resources, ResourceLocator settings) throws QuizzException {
  String r = "";
  Question question = null;
  try{
		if (quizz != null) {
			QuestionContainerHeader quizzHeader = quizz.getHeader();
			Collection<Question> questions = quizz.getQuestions();

			//Display the quizz header
			r += displayQuizzHeaderPreview(quizzScc, quizzHeader,resources, gef);

			//Display the questions
			r += "<form name=\"quizz\" Action=\"quizzQuestionsNew.jsp\" Method=\"Post\">";
			r += "<input type=\"hidden\" name=\"Action\" value=\"SubmitQuizz\">";
			Iterator<Question> itQ = questions.iterator();
			int i = 1;
			while (itQ.hasNext()) {
				  question = (Question) itQ.next();
				  r += displayQuestionPreview(question, i, m_context, quizzScc,settings, gef, resources);
				  i++;
			}
			r += "</form>";

		} else {
			r += "<table><tr><td>"+resources.getString("QuizzUnavailable")+"</td></tr></table>";
		}
	} catch(Exception e){
		throw new QuizzException ("quizzQuestionNew_JSP.displayQuizzPreview",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_QUIZZPREVIEW",e);
	}

	return r;
}


String displayQuestionPreview(Question question, int i, String m_context, QuizzSessionController quizzScc,ResourceLocator settings, GraphicElementFactory gef, ResourcesWrapper resources) {
        Collection<Answer> answers = question.getAnswers();
        Board board = gef.getBoard();
        String r = "<br>";
        r += board.printBefore();
        r += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\"><tr><td nowrap><span class=\"txtlibform\">&nbsp;<b>&#149;</b>&nbsp;"+Encode.javaStringToHtmlString(question.getLabel())+"&nbsp;</span>";
        r += " - "+question.getNbPointsMax()+" pts</td>";
        r += "<td align=\"right\" nowrap>";
        if ((question.getClue() != null)&&(!question.getClue().equals("")))
        {
          r += "<a href=\"#\" onClick=\"view_cluePreview('"+i+"')\">"+resources.getString("QuizzSeeClue")+"</a>";
          r += " (" + resources.getString("QuizzPenalty") + "=" + question.getCluePenalty() + " pts)";
        }
        else
          r += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        r += "</td></tr></table>";
        r += "</table><br>";
        r += board.printBefore();
        r += "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\" >";

        String style = question.getStyle();
        if (style.equals("list"))
		{
        	r += "<tr><td colspan=\"2\">";
            r += "<select id=\"answers\" name=\"answers\" >";

        	Iterator<Answer> itA = answers.iterator();
        	while (itA.hasNext())
	        {
	            Answer answer = (Answer) itA.next();
	           	r += "<option name=\"answer_"+i+"\" value=\"\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</option>";
	        }

         	r += "</td></tr>";
		}
		else
		{
	        String inputType = "radio";
	        if (style.equals("checkbox"))
	        	inputType = "checkbox";
	        Iterator<Answer> itA = answers.iterator();
	        while (itA.hasNext())
	        {
	            Answer answer = (Answer) itA.next();
	            r += "<tr><td colspan=\"2\"><table width=\"98%\"><tr>";

	            // traitement de l'affichage des images
	            if (answer.getImage() == null)
			      {
	            	r += "<td align=\"left\">&nbsp;</td>";
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
				        	url = FileServerUtils.getUrl(quizzScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
				        }
				        r += "<td width=\"50%\"><img src=\""+url+"\" align=\"left\"></td>";
			      }
	            r += "<td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\"></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR>";
	            r += "</tr></table>";
	        }
		}
        r += board.printAfter();
        r += board.printAfter();
        return r;
}

String displayQuizzResult(QuestionContainerDetail quizz, GraphicElementFactory gef, String m_context,QuizzSessionController quizzScc, ResourcesWrapper resources, ResourceLocator settings, int nb_user_votes) throws QuizzException {
        String r = "";
        List<String> function = null;
        int quizzUserScore = 0;
        int quizzScoreMax = 0;

        Question question = null;
        Collection answers = null;
        try{
			if (quizz != null) {
				QuestionContainerHeader quizzHeader = quizz.getHeader();
				//Display the quizz header
				r += displayQuizzHeader(quizzScc, quizzHeader, resources, gef);

				boolean solutionAllowed = false;
				if (nb_user_votes >= quizzHeader.getNbParticipationsBeforeSolution())
				  solutionAllowed = true;

				Collection<Question> questions = quizz.getQuestions();
				//Display the quizz header
				String quizzId = quizzHeader.getPK().getId();
				String title = quizzHeader.getTitle();

				//Display the questions
				r += "<form name=\"quizz\" Action=\"quizzQuestionsNew.jsp\" Method=\"Post\">";
				r += "<input type=\"hidden\" name=\"Action\">";
				r += "<input type=\"hidden\" name=\"QuizzId\" value=\""+quizzHeader.getPK().getId()+"\">";
				Iterator<Question> itQ = questions.iterator();
				int i = 1;
				while (itQ.hasNext()) {
					  question = (Question) itQ.next();
					  function = displayQuestionResult(quizz, question, i, m_context, settings, quizzScc, solutionAllowed, resources);
					  r += function.get(0);
					  quizzUserScore += new Integer((String) function.get(1)).intValue();
					  quizzScoreMax += new Integer((String) function.get(2)).intValue();
					  i++;
				}

				//Score total:
				float winRate=((new Float(quizzUserScore).floatValue())/(new Float(quizzScoreMax).floatValue()))*100;
				String winRateImg="tableProf_4.gif";
				String winRate1 = settings.getString("winRate1");
				String winRate2 = settings.getString("winRate2");
				String winRate3 = settings.getString("winRate3");
				String winRate4 = settings.getString("winRate4");
				String winRateImg1 = settings.getString("winRateImg1");
				String winRateImg2 = settings.getString("winRateImg2");
				String winRateImg3 = settings.getString("winRateImg3");
				String winRateImg4 = settings.getString("winRateImg4");

				if (winRate<=new Integer(winRate1).intValue()) {
				  winRateImg=winRateImg1;
				} else if (winRate<=new Integer(winRate2).intValue()) {
				  winRateImg=winRateImg2;
			  } else if (winRate<=new Integer(winRate3).intValue()) {
			    winRateImg=winRateImg3;
        } else if (winRate<=new Integer(winRate4).intValue()) {
          winRateImg=winRateImg4;
        }

				r += "<table width=100% border=0 cellspacing=0 cellpadding=5><tr><td width=50% align=right valign=top class=\"txtnav\">"+resources.getString("TotalScoreLib")+" :</td>";
				r += "<td width=\"50%\"><table border=0 cellspacing=0 cellpadding=0><tr>";
				r += "<td rowspan=3><img src=\"icons/tableProf_1.gif\" width=7 height=70></td><td><img src=\"icons/tableProf_2.gif\" width=34 height=6></td>";
				r += "<td rowspan=3><img src=\"icons/"+winRateImg+"\" width=79 height=70></td></tr>";
				r += "<tr><td height=42 bgcolor=#387B80><span class=\"titreFenetre2\">"+quizzUserScore+"<br><img src=\"icons/1pxBlanc.gif\" width=30 height=1><br>"+quizzScoreMax+"</span></td></tr>";
				r += "<tr><td><img src=\"icons/tableProf_3.gif\" width=34 height=22></td></tr></table></td></tr></table><br>";
			} else {
		        r += "<table><tr><td>"+resources.getString("QuizzUnavailable")+"</td></tr>";
			}
		}catch( Exception e){
			throw new QuizzException ("quizzQuestionNew_JSP.displayQuizzResult",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_RESULT",e);
		}

		return r;
}

  String displayQuizzResultAdmin(QuestionContainerDetail quizz, GraphicElementFactory gef, String m_context,QuizzSessionController quizzScc, ResourcesWrapper resources, ResourceLocator settings, int nb_user_votes) throws QuizzException {
        String r = "";
        List<String> function = null;
        int quizzUserScore = 0;
        int quizzScoreMax = 0;

        Question question = null;
        Collection answers = null;
		try{
			if (quizz != null) {
				QuestionContainerHeader quizzHeader = quizz.getHeader();
				r += displayQuizzHeader(quizzScc,quizzHeader,resources, gef);
				boolean solutionAllowed = true;
				Collection<Question> questions = quizz.getQuestions();
				String quizzId = quizzHeader.getPK().getId();
				String title = quizzHeader.getTitle();
				r += "<form name=\"quizz\">";
				r += "<input type=\"hidden\" name=\"Action\">";
				r += "<input type=\"hidden\" name=\"QuizzId\" value=\""+quizzHeader.getPK().getId()+"\">";
				Iterator<Question> itQ = questions.iterator();
				int i = 1;
				while (itQ.hasNext()) {
					  question = (Question) itQ.next();
					  function = displayQuestionResult(quizz, question, i, m_context, settings, quizzScc, solutionAllowed, resources);
					  r += function.get(0);
					  quizzUserScore += new Integer((String) function.get(1)).intValue();
					  quizzScoreMax += new Integer((String) function.get(2)).intValue();
					  i++;
				}
					float winRate=((new Float(quizzUserScore).floatValue())/(new Float(quizzScoreMax).floatValue()))*100;
				String winRateImg="tableProf_4.gif";
				String winRate1 = settings.getString("winRate1");
				String winRate2 = settings.getString("winRate2");
				String winRate3 = settings.getString("winRate3");
				String winRate4 = settings.getString("winRate4");
				String winRateImg1 = settings.getString("winRateImg1");
				String winRateImg2 = settings.getString("winRateImg2");
				String winRateImg3 = settings.getString("winRateImg3");
				String winRateImg4 = settings.getString("winRateImg4");

				if (winRate<=new Integer(winRate1).intValue())
					winRateImg=winRateImg1;
				else if (winRate<=new Integer(winRate2).intValue())
					winRateImg=winRateImg2;
				else if (winRate<=new Integer(winRate3).intValue())
					winRateImg=winRateImg3;
				else if (winRate<=new Integer(winRate4).intValue())
					winRateImg=winRateImg4;

				r += "<table width=100% border=0 cellspacing=0 cellpadding=5><tr><td width=50% align=right valign=top class=\"txtnav\">"+resources.getString("TotalScoreLib")+" :</td>";
				r += "<td width=\"50%\"><table border=0 cellspacing=0 cellpadding=0><tr>";
				r += "<td rowspan=3><img src=\"icons/tableProf_1.gif\" width=7 height=70></td><td><img src=\"icons/tableProf_2.gif\" width=34 height=6></td>";
				r += "<td rowspan=3><img src=\"icons/"+winRateImg+"\" width=79 height=70></td></tr>";
				r += "<tr><td height=42 bgcolor=#387B80><span class=\"titreFenetre2\">"+quizzUserScore+"<br><img src=\"icons/1pxBlanc.gif\" width=30 height=1><br>"+quizzScoreMax+"</span></td></tr>";
				r += "<tr><td><img src=\"icons/tableProf_3.gif\" width=34 height=22></td></tr></table></td></tr></table><br>";
			} else {
				r += "<table><tr><td>"+resources.getString("QuizzUnavailable")+"</td></tr>";
			}
		} catch(Exception e){
			throw new QuizzException ("quizzQuestionNew_JSP.displayQuizzResultAdmin",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_RESULT",e);
		}

		return r;
}

%>

<%

//Retrieve parameter
String action = request.getParameter("Action");
String quizzId = request.getParameter("QuizzId");
String participationIdSTR =  request.getParameter("ParticipationId");
String userId = request.getParameter("UserId");
String roundId = request.getParameter("RoundId");
String origin = request.getParameter("Page");
if (origin==null) origin="";
int participationId = 0;

if (roundId == null) {
  roundId = "1";
}

if (participationIdSTR != null) {
  session.setAttribute("currentParticipationId", participationIdSTR);
}
ResourceLocator settings = quizzScc.getSettings();

//Icons
String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
String ligne = m_context + "/util/icons/colorPix/1px.gif";

//Html
List<String> html_string = null;

QuestionContainerDetail quizz = null;

boolean isClosed = false;

if (action.equals("PreviewQuizz") || action.equals("SubmitQuizz")) {
      quizz = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
}
else {
	if (quizzId != null && quizzId.length() != 0) {
		quizz = quizzScc.getQuizzDetail(quizzId);
		session.setAttribute("currentQuizz", quizz);
	}
	else
	{
      quizz = (QuestionContainerDetail) session.getAttribute("currentQuizz");
      if (quizz == null) {
          quizz = quizzScc.getQuizzDetail(quizzId);
          session.setAttribute("currentQuizz", quizz);
          roundId = "1";
      }
	}

  boolean endDateReached = false;
  if (quizz.getHeader().getEndDate() != null) {
          endDateReached = (quizz.getHeader().getEndDate().compareTo(resources.getDBDate(new Date())) < 0);
  }
  if (endDateReached || quizz.getHeader().isClosed())
        isClosed = true;
  if (action == null) {
          action = "ViewQuizz";
  }
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
<link type="text/css" href="<%=m_context%>/util/styleSheets/fieldset.css" rel="stylesheet" />
<view:looknfeel />
<style type="text/css">
.thumbnailPreviewAndActions {
  display: none;
}
</style>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
<!--
function confirmCancel()
{
	if (confirm('<%=resources.getString("ConfirmCancel")%>'))
		self.location="Main.jsp";
}

function update_suggestion(quizz_id)
{
    errorMsg="";
   document.quizz.Action.value="UpdateSuggestion";
   if (!isWhitespace(document.quizz.txa_suggestion.value)) {
      if (!isValidTextArea(document.quizz.txa_suggestion)) {
            errorMsg = "<%=resources.getString("GML.ThisFormContain")%> 1 <%=resources.getString("GML.error")%> : \n";
            errorMsg += "  - <%=resources.getString("GML.theField")%> '<%=resources.getString("EducationSuggestion")%>' <%=resources.getString("MustContainsLessCar")%> <%=DBUtil.getTextAreaLength()%> <%=resources.getString("Caracters")%>\n";
            window.alert(errorMsg);
            return;
      }
      else
           document.quizz.submit();
   }
   document.quizz.submit();
}
function view_cluePreview(question_id)
{
  largeur = "600";
  hauteur = "230";

  SP_openWindow('quizzCluePreview.jsp?question_id='+question_id,'indice',largeur,hauteur,'scrollbars=yes,resizable=yes');
}

function view_clue(i, quizz_id, question_id, cluePenalty)
{
  objet = eval("document.quizz.cluePenalty_"+i);
  if (objet.value == 0)
    objet.value = cluePenalty;
  largeur = "600";
  hauteur = "230";

 SP_openWindow('quizzClue.jsp?quizz_id='+quizz_id+'&question_id='+question_id,'indice',largeur,hauteur,'scrollbars=yes,resizable=yes');
}

// PopUp center
function SP_openWindow(page,nom,largeur,hauteur,options) {
var top=(screen.height-hauteur)/2;
var left=(screen.width-largeur)/2;
	if (window.fenetre != null){
  	window.fenetre.close();}
fenetre=window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
return fenetre;
}

function MM_openBrWindow(theURL,winName,features) { //v2.0
  window.open(theURL,winName,features);
}

function sendVote(roundId) {
    if (roundId == "end") {
          document.quizz.Action.value="SendVote";
          document.quizz.submit();
    } else {
          document.quizz.RoundId.value = roundId;
          document.quizz.Action.value="RecordQuestionsResponses";
          document.quizz.submit();
    }
}

var exportWindow = window;

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

function clipboardCopy(id) {
  top.IdleFrame.location.href = '../..<%=quizzScc.getComponentUrl()%>copy?Id='+id;
}

//-->
</script>
</head>
<%

if (action.equals("RecordQuestionsResponses")) {
  int nbQuestions = Integer.parseInt(request.getParameter("NbQuestions"));
  int cluePenalty  = 0;
  Map hash = (Map) session.getAttribute("questionsResponses");
  if (hash == null)
        hash = new HashMap();

  for (int i = 1; i <= nbQuestions; i++) {
      List v = new ArrayList(5);
      cluePenalty = Integer.parseInt(request.getParameter("cluePenalty_"+i));
      v.add("PC"+cluePenalty);
      String[] selectedAnswers = (String[]) request.getParameterValues("answer_"+i);
      if (selectedAnswers != null) {
        String questionId = selectedAnswers[0].substring(selectedAnswers[0].indexOf(",")+1, selectedAnswers[0].length());
        for (int j = 0; j < selectedAnswers.length; j++) {
              String answerId = selectedAnswers[j].substring(0, selectedAnswers[j].indexOf(","));
              v.add(answerId);
        }
        String openedAnswer = request.getParameter("openedAnswer_"+i);
        v.add("OA"+openedAnswer);
        //if (hash.containsKey(questionId))
        hash.put(questionId, v);
      }
  }
  session.setAttribute("questionsResponses", hash);
  action = "ViewCurrentQuestions";
} else if (action.equals("SendVote")) {
  int nbQuestions = Integer.parseInt(request.getParameter("NbQuestions"));
  int cluePenalty  = 0;
  Map hash = (Map) session.getAttribute("questionsResponses");
  if (hash == null)
      hash = new HashMap();

  for (int i = 1; i <= nbQuestions; i++) {
      List v = new ArrayList(5);
      cluePenalty = Integer.parseInt(request.getParameter("cluePenalty_"+i));
      v.add("PC"+cluePenalty);
      String[] selectedAnswers = (String[]) request.getParameterValues("answer_"+i);
      if (selectedAnswers != null) {
        String questionId = selectedAnswers[0].substring(selectedAnswers[0].indexOf(",")+1, selectedAnswers[0].length());
        for (int j = 0; j < selectedAnswers.length; j++) {
              String answerId = selectedAnswers[j].substring(0, selectedAnswers[j].indexOf(","));
              v.add(answerId);
        }
        String openedAnswer = request.getParameter("openedAnswer_"+i);
        v.add("OA"+openedAnswer);
        hash.put(questionId, v);
     }
  }
  quizzScc.recordReply(quizzId, hash);
  action = "ViewResult";
} //End if action = ViewResult
if (action.equals("SubmitQuizz")) {
  QuestionContainerDetail quizzDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
  //Vector 2 Collection
  List questionsV = (List) session.getAttribute("questionsVector");
  List<Question> q = new ArrayList<Question>();
  for (int j = 0; j < questionsV.size(); j++) {
        q.add((Question) questionsV.get(j));
  }
  quizzDetail.setQuestions(q);
  quizzScc.createQuizz(quizzDetail);
  session.removeAttribute("quizzUnderConstruction");
  quizzScc.setPositions(null);
  %>
  <jsp:forward page='<%=quizzScc.getComponentUrl()+"Main.jsp"%>'/>
  <%
  return;
} else if (action.equals("PreviewQuizz")) {
    out.println("<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor=\"#FFFFFF\">");

    Window window = gef.getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    browseBar.setDomainName(quizzScc.getSpaceLabel());
    browseBar.setComponentName(quizzScc.getComponentLabel());
    browseBar.setExtraInformation(resources.getString("quizz.preview"));

    out.println(window.printBefore());
    Frame frame = gef.getFrame();
    out.println(frame.printBefore());
    String quizzPart = displayQuizzPreview(quizz, gef, m_context, quizzScc, resources, settings);
    out.println(quizzPart);
    out.println(frame.printMiddle());
    out.println("<table width=\"100%\">");
    Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:confirmCancel();", false);
    Button voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=document.quizz.submit();", false);
    out.println("<tr><td align=\"center\"><table><tr align=center><td align=center>"+voteButton.print()+"</td><td align=center>"+cancelButton.print()+"</td></tr></table></td></tr>");
    out.println("</table>");
    out.println("<br><br>"+frame.printAfter());
    out.println(window.printAfter());
}
if (action.equals("ViewQuizz")) {
  out.println("<body bgcolor=\"#FFFFFF\">");
  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(quizzScc.getSpaceLabel());
  browseBar.setComponentName(quizzScc.getComponentLabel());
  browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");
  browseBar.setExtraInformation(quizz.getHeader().getTitle());

  //operation pane
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(m_context + "/util/icons/quizz_print.gif",resources.getString("GML.print"),"javascript:window.print()");
  if (quizzScc.getNbVoters(quizz.getHeader().getPK().getId())==0||"admin".equals(quizzScc.getUserRoleLevel()))
  {
  	operationPane.addLine();
	  operationPane.addOperation(m_context + "/util/icons/quizz_to_edit.gif",resources.getString("QuestionUpdate"),"quizzUpdate.jsp?Action=UpdateQuizzHeader&QuizzId="+quizz.getHeader().getPK().getId());
  }

  if (profile.equals("admin")) {
    // export csv
    String url = "ExportCSV?QuizzId=" + quizz.getHeader().getPK().getId();
    operationPane.addOperation(exportSrc, resources.getString("GML.export"), "javaScript:onClick=Export('"+url+"')");
  }

  // copier
  operationPane.addOperation(copySrc, resources.getString("GML.copy"), "javaScript:onClick=clipboardCopy('"+quizz.getHeader().getPK().getId()+"')");

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());
  String quizzPart = displayQuizz(quizz,gef, m_context, quizzScc, resources, settings, out);
  out.println(quizzPart);
%>
  <view:pdcClassification componentId="<%= quizzScc.getComponentId() %>" contentId="<%= quizzId %>" />
<%
  out.println(frame.printMiddle());
  out.println(frame.printAfter());
  out.println(window.printAfter());
} //End if action = ViewQuizz
else if (action.equals("ViewCurrentQuestions")) {
  out.println("<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor=\"#FFFFFF\">");

  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(quizzScc.getSpaceLabel());
  browseBar.setComponentName(quizzScc.getComponentLabel());
  browseBar.setPath("<a href=\"Main.jsp\">" + resources.getString("QuizzList") + "</a>");
  browseBar.setExtraInformation(resources.getString("QuizzParticipate") + " " + quizz.getHeader().getTitle());

  //operation pane
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(m_context + "/util/icons/quizz_print.gif",resources.getString("GML.print"),"javascript:window.print()");

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());

  html_string = displayQuestions(quizz, Integer.parseInt(roundId), gef, m_context, quizzScc, resources, settings, frame, out);
  out.println(html_string.get(0));
  out.println(frame.printMiddle());
  out.println(html_string.get(1));
  out.println(frame.printAfter());
  out.println(window.printAfter());
}
else if (action.equals("ViewResult")) {
  out.println("<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor=\"#FFFFFF\">");
  String participation=(String) session.getAttribute("currentParticipationId");
  int nb_user_votes = 0;
  Collection<ScoreDetail> userScores = quizzScc.getUserScoresByFatherId(quizzId);
  if (userScores != null)
  nb_user_votes = userScores.size();

  if ((participation!=null)&&(!participation.equals(""))) {
    participationId = Integer.parseInt((String) session.getAttribute("currentParticipationId"));
  } else {
	  participationId=nb_user_votes;
  }

  if (userId == null) {
    participationId += 1;
    quizz = quizzScc.getQuestionContainerForCurrentUserByParticipationId(quizzId, participationId);
  }
  else
  {
      quizz = quizzScc.getQuestionContainerByParticipationId(quizzId, userId, participationId);
  }

  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(quizzScc.getSpaceLabel());
  browseBar.setComponentName(quizzScc.getComponentLabel());
  browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");
  browseBar.setExtraInformation(resources.getString("QuizzSeeResults"));

  //operation pane
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(m_context + "/util/icons/quizz_print.gif",resources.getString("GML.print"),"javascript:window.print()");

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());
  String quizzPart = displayQuizzResult(quizz, gef, m_context, quizzScc, resources, settings, nb_user_votes);
  ScoreDetail userScoreDetail = null;
  //Suggestion pedagogique
  if (userId == null) {
    userScoreDetail= quizzScc.getCurrentUserScoreByFatherIdAndParticipationId(quizzId, participationId);
  } else
    userScoreDetail= quizzScc.getUserScoreByFatherIdAndParticipationId(quizzId, userId, participationId);

  if ((userScoreDetail.getSuggestion()!=null) && (!userScoreDetail.getSuggestion().equals(""))) {
    quizzPart += "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr align=center><td><table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\"><tr><td valign=\"top\"><img src=\"icons/silverProf_SuggPedago.gif\" align=\"left\"><span class=\"txtnav\">";
    quizzPart += resources.getString("EducationSuggestion") + " :</span><br>";
    quizzPart += EncodeHelper.javaStringToHtmlString(userScoreDetail.getSuggestion())+"</td></tr></table></td></tr></table>";
  }

  out.println(quizzPart);
  out.println(frame.printMiddle());

  Button cancelButton = gef.getFormButton(resources.getString("GML.back"), "Main.jsp", false);
  if (origin.equals("1")) {
    cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "quizzResultUser.jsp", false);
  } else if (origin.equals("0")) {
    cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "palmares.jsp?quizz_id="+quizzId, false);
  }
  out.println("<table width=100% border=\"0\">");
  out.println("<tr><td align=\"center\">"+cancelButton.print()+"</td></tr>");
  out.println("</table>");
  out.println(frame.printAfter());
  out.println(window.printAfter());
}
else if (action.equals("ViewResultAdmin")) {
  out.println("<body>");
  String participation= (String) session.getAttribute("currentParticipationId");
  if ((participation!=null)&&(!participation.equals("")))
    participationId = new Integer((String) session.getAttribute("currentParticipationId")).intValue();
  if (userId == null) {
    participationId += 1;
    quizz = quizzScc.getQuestionContainerForCurrentUserByParticipationId(quizzId, participationId);
  } else{
       participationId = new Integer((String) session.getAttribute("currentParticipationId")).intValue();
       quizz = quizzScc.getQuestionContainerByParticipationId(quizzId, userId, participationId);
  }
  int nb_user_votes = 0;
  Collection<ScoreDetail> userScores = quizzScc.getUserScoresByFatherId(quizzId);
  if (userScores != null) {
    nb_user_votes = userScores.size();
  }

  Window window = gef.getWindow();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(quizzScc.getSpaceLabel());
  browseBar.setComponentName(quizzScc.getComponentLabel());
  browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");
  browseBar.setExtraInformation(resources.getString("QuizzSeeResults"));

  //operation pane
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(m_context + "/util/icons/quizz_print.gif",resources.getString("GML.print"),"javascript:window.print()");

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());
  String quizzPart = displayQuizzResultAdmin(quizz, gef, m_context, quizzScc, resources, settings, nb_user_votes);
  ScoreDetail userScoreDetail = null;
  //Suggestion pedagogique
  if (userId == null) {
    userScoreDetail= quizzScc.getCurrentUserScoreByFatherIdAndParticipationId(quizzId, participationId);
  } else {
    userScoreDetail= quizzScc.getUserScoreByFatherIdAndParticipationId(quizzId, userId, participationId);
  }

  quizzPart += "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr align=center><td><table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" class=\"contourintfdcolor\" width=\"100%\"><tr><td valign=\"top\"><img src=\"icons/silverProf_SuggPedago.gif\" align=\"left\"><span class=\"txtnav\">";
  quizzPart += resources.getString("EducationSuggestion") + " :</span><br>";
  quizzPart += "<textarea name=\"txa_suggestion\" cols=\"120\" rows=\"4\">";
  if (userScoreDetail.getSuggestion()!=null) {
    quizzPart += userScoreDetail.getSuggestion();
  }
  quizzPart += "</textarea>";
  quizzPart += "</td></tr></table></td></tr></table>";
  quizzPart += "<input type=\"hidden\" name=\"UserId\" value=\""+userId+"\">";
  quizzPart += "<input type=\"hidden\" name=\"Page\" value=\""+origin+"\">";

  out.println(quizzPart);
  out.println(frame.printMiddle());
  Button cancelButton= gef.getFormButton(resources.getString("GML.back"), "quizzAdmin.jsp", false);
  if (origin.equals("1")) {
    cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "quizzResultAdmin.jsp", false);
  } else if (origin.equals("0")) {
    cancelButton = gef.getFormButton(resources.getString("GML.cancel"), "palmaresAdmin.jsp?quizz_id="+quizzId, false);
  }
  Button voteButton = gef.getFormButton(resources.getString("QuestionUpdate"), "javascript:update_suggestion("+quizz.getHeader().getPK().getId()+")", false);
  out.println("<table width=100% border=\"0\">");
  out.println("<tr><td align=\"center\"><table><tr><td align=center>"+voteButton.print()+"</td><td align=center>"+cancelButton.print()+"</td></tr></table></td></tr>");
  out.println("</table>");
  out.println(frame.printAfter());
  out.println(window.printAfter());
}
else if (action.equals("UpdateSuggestion")) {
  String suggestion = request.getParameter("txa_suggestion");
  participationId = new Integer((String) session.getAttribute("currentParticipationId")).intValue();
  quizz = quizzScc.getQuestionContainerByParticipationId(quizzId, userId, participationId);
  ScoreDetail userScoreDetail = null;
  //Suggestion pedagogique
  userScoreDetail = quizzScc.getUserScoreByFatherIdAndParticipationId(quizzId, userId, participationId);
  userScoreDetail.setSuggestion(suggestion);
  quizzScc.updateScore(userScoreDetail);

  if (origin.equals("0"))
  {
%>
          <script type="text/javascript">
          <!--
          self.location="palmaresAdmin.jsp?quizz_id=<%=quizzId%>";
          //-->
          </script>
<%
  } else if (origin.equals("1")) {
%>
<script type="text/javascript">
<!--
self.location="quizzResultAdmin.jsp";
//-->
</script>
<%
  }
}
%>
</body>
</html>
