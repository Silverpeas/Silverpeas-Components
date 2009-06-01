<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>
<%@ page import="com.stratelia.webactiv.quizz.QuizzException"%>

<%!
  String displayQuestionsUpdateView(Vector questions, GraphicElementFactory gef, String m_context, QuizzSessionController quizzScc,ResourceLocator settings, ResourcesWrapper resources) throws QuizzException {
        String questionUpSrc = "icons/questionUp.gif";
        String questionDownSrc = "icons/questionDown.gif";
        String questionDeleteSrc = "icons/questionDelete.gif";
        String questionUpdateSrc = "icons/questionUpdate.gif";
        String r = "";
        Question question = null;
        Collection answers = null;
        String operations = "";
        
        Board board = gef.getBoard();
        
		try{
			//Display the questions
			r += "<center>";
			//r += "<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"intfdcolor4\"><tr align=center><td>";
			r += board.printBefore();
			r += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"5\" width=\"98%\"><tr><td>";
			r += "<form name=\"quizz\" Action=\"questionsUpdate.jsp\" Method=\"Post\">";
			r += "<input type=\"hidden\" name=\"Action\" value=\"SubmitQuestions\">";
			Iterator itQ = questions.iterator();
			int i = 1;
			for (int j=0; j<questions.size(); j++) {
				  question = (Question) questions.get(j);
				  answers = question.getAnswers();

				  //check available operations to current question
				  operations = " ";
				  if (j!=0)
					  operations += "<a href=\"questionsUpdate.jsp?Action=UpQuestion&QId="+j+"\"><img src=\""+questionUpSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionUp")+"\" title=\""+resources.getString("QuestionUp")+"\"></a> ";
				  if (j+1!=questions.size())
					  operations += "<a href=\"questionsUpdate.jsp?Action=DownQuestion&QId="+j+"\"><img src=\""+questionDownSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionDown")+"\" title=\""+resources.getString("QuestionDown")+"\"></a> ";
				  operations += "<a href=\"questionsUpdate.jsp?Action=DeleteQuestion&QId="+j+"\"><img src=\""+questionDeleteSrc+"\" border=\"0\" alt=\""+resources.getString("GML.delete")+"\" title=\""+resources.getString("GML.delete")+"\"></a> ";
				  //operations += "<a href=\"questionsUpdate.jsp?Action=UpdateQuestion&QId="+j+"\"><img src=\""+questionUpdateSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionUpdate")+"\"></a>";

				  r += "<table border=\"0\" width=\"100%\">";
				  r += "<tr><td colspan=\"2\" align=\"left\"><B>&#149; <U>"+Encode.javaStringToHtmlString(question.getLabel())+"</U></B>"+operations+"<BR><BR></td></tr>";
				  if (question.isOpen()) {
						Iterator itA = answers.iterator();
						int isOpened = 0;
						r += "<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+i+"\" cols=\"60\" rows=\"4\"></textarea></td></tr>";
				  } 
				  else 
				  {
				  		String style = question.getStyle();
			        	if (style.equals("list"))
			        	{
			        		// liste déroulante
			             	String selectedStr = "";
			             	
			                r += "<tr><td><select id=\"answer_"+i+"\" name=\"answer_"+i+"\" >";
			                          			
			                Iterator itA = answers.iterator();
				            while (itA.hasNext()) 
				            {
				            	Answer answer = (Answer) itA.next();
			                    r += "<option value=\"\" "+selectedStr+">"+Encode.javaStringToHtmlString(answer.getLabel())+"</option>";
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
							while (itA.hasNext()) {
								Answer answer = (Answer) itA.next();
								if (answer.isOpened()) {
									isOpened = 1;
									r += "<tr><td align=\"left\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\">"+Encode.javaStringToHtmlString(answer.getLabel())+"<BR><input type=\"text\" size=\"20\" name=\"openedAnswer_"+i+"\"></td></tr>";
								} 
								else 
								{
									if (answer.getImage() == null) 
				                  	{
										  r += "<tr><td align=\"left\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\" width=\"100%\">"+Encode.javaStringToHtmlString(answer.getLabel())+"</td></tr>";
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
				                      		url = FileServer.getUrl(quizzScc.getSpaceId(), quizzScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
				                      	}
										  r += "<tr valign=middle><td align=\"left\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\" valign=top>"+Encode.javaStringToHtmlString(answer.getLabel())+"&nbsp;&nbsp;&nbsp;";
										  r += "<img src=\""+url+"\" border=\"0\" hspace=10 vspace=10 align=absmiddle></td><td>";
				                    }
	                    
	                    
								}
							}
						}
				  }
				  i++;
				  r += "</table>";
			}
			r += "</form></table>";
			r += board.printAfter();
			//r += "</td></tr></table>";
			//r += "<table>";
			Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false);
			Button voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:SendQuestions('"+questions.size()+"');", false);
			r += "<table><tr><td align=\"center\"><br><table border=\"0\"><tr><td>"+voteButton.print()+"</td><td>"+cancelButton.print()+"</td></tr></table></td></tr>";
			r += "</table>";
		} catch(Exception e){
			throw new QuizzException ("questionUtils_JSP.displayQuestionsUpdateView",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_UPDATEVIEW",e);
		}
        return r;
  }
%>