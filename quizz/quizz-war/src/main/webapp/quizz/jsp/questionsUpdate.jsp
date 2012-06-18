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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<jsp:useBean id="quizzUnderConstruction" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />
<jsp:useBean id="questionsVector" scope="session" class="java.util.Vector" />
<jsp:useBean id="questionsResponses" scope="session" class="java.util.Hashtable" />
<jsp:useBean id="currentQuizzId" scope="session" class="java.lang.String" />

<%@ include file="checkQuizz.jsp" %>

<%!
  String displayQuestionsUpdateView(Vector questions, GraphicElementFactory gef, String m_context, QuizzSessionController quizzScc,ResourceLocator settings, ResourcesWrapper resources) throws QuizzException {
        String questionUpSrc = "icons/questionUp.gif";
        String questionDownSrc = "icons/questionDown.gif";
        String questionDeleteSrc = "icons/questionDelete.gif";
        String questionUpdateSrc = "icons/questionUpdate.gif";
        String r = "";
        Question question = null;
        Collection<Answer> answers = null;
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
          if (j!=0) {
            operations += "<a href=\"questionsUpdate.jsp?Action=UpQuestion&QId="+j+"\"><img src=\""+questionUpSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionUp")+"\" title=\""+resources.getString("QuestionUp")+"\"></a> ";
          }
          if (j+1!=questions.size()) {
            operations += "<a href=\"questionsUpdate.jsp?Action=DownQuestion&QId="+j+"\"><img src=\""+questionDownSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionDown")+"\" title=\""+resources.getString("QuestionDown")+"\"></a> ";
          }
          operations += "<a href=\"questionsUpdate.jsp?Action=DeleteQuestion&QId="+j+"\"><img src=\""+questionDeleteSrc+"\" border=\"0\" alt=\""+resources.getString("GML.delete")+"\" title=\""+resources.getString("GML.delete")+"\"></a> ";
          //operations += "<a href=\"questionsUpdate.jsp?Action=UpdateQuestion&QId="+j+"\"><img src=\""+questionUpdateSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionUpdate")+"\"></a>";

          r += "<table border=\"0\" width=\"100%\">";
          r += "<tr><td colspan=\"2\" align=\"left\"><B>&#149; <U>"+EncodeHelper.javaStringToHtmlString(question.getLabel())+"</U></B>"+operations+"<BR><BR></td></tr>";
          if (question.isOpen()) {
            Iterator<Answer> itA = answers.iterator();
            int isOpened = 0;
            r += "<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+i+"\" cols=\"60\" rows=\"4\"></textarea></td></tr>";
          } 
          else 
          {
              String style = question.getStyle();
                if (style.equals("list"))
                {
                  // drop down list
                    String selectedStr = "";
                    
                      r += "<tr><td><select id=\"answer_"+i+"\" name=\"answer_"+i+"\" >";
                                      
                      Iterator<Answer> itA = answers.iterator();
                    while (itA.hasNext()) 
                    {
                      Answer answer = (Answer) itA.next();
                          r += "<option value=\"\" "+selectedStr+">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</option>";
                    }
                    r += "</td></tr>";
                }
                else
                {
              String inputType = "radio";
              if (style.equals("checkbox")) {
                  inputType = "checkbox";
              }
              Iterator<Answer> itA = answers.iterator();
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



<%
//Retrieve parameters
String action = (String) request.getParameter("Action");
String quizzId = (String) request.getParameter("QuizzId");

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

ResourceLocator settings = quizzScc.getSettings();

QuestionContainerDetail quizz = null;

if (action.equals("UpQuestion")) {
      int qId = new Integer((String) request.getParameter("QId")).intValue();
      Vector qV = (Vector) session.getAttribute("questionsVector");
      Question q1 = (Question) qV.get(qId);
      Question q2 = (Question) qV.get(qId-1);
      qV.set(qId-1, q1);
      qV.set(qId, q2);
      session.setAttribute("questionsVector", qV);
      action = "UpdateQuestions";
} else if (action.equals("DownQuestion")) {
      int qId = new Integer((String) request.getParameter("QId")).intValue();
      Vector qV = (Vector) session.getAttribute("questionsVector");
      Question q1 = (Question) qV.get(qId);
      Question q2 = (Question) qV.get(qId+1);
      qV.set(qId+1, q1);
      qV.set(qId, q2);
      session.setAttribute("questionsVector", qV);
      action = "UpdateQuestions";
} else if (action.equals("DeleteQuestion")) {
      int qId = new Integer((String) request.getParameter("QId")).intValue();
      Vector qV = (Vector) session.getAttribute("questionsVector");
      qV.remove(qId);
      session.setAttribute("questionsVector", qV);
      action = "UpdateQuestions";
}
if (action.equals("SendQuestions")) {
      Vector qV = (Vector) session.getAttribute("questionsVector");
      quizzScc.updateQuestions(qV, (String) session.getAttribute("currentQuizzId"));
%>
	<jsp:forward page="<%=quizzScc.getComponentUrl()+\"Main.jsp\"%>"/>
<%
	return;
}
if (action.equals("UpdateQuestions")) {
%>
<html>
<head>
	<title>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<script language="javascript">
function addQuestion() {
    document.questionForm.submit();
}
function SendQuestions(nb)
{
	if (Number(nb) > 0)
		self.location = "questionsUpdate.jsp?Action=SendQuestions";
	else
			alert('<%=resources.getString("MustContainsAQuestion")%>');
}
</script>
<body>
<%
  Vector<Question> questionsV = null;
  if (quizzId != null) {
    session.removeAttribute("currentQuizzId");
    session.removeAttribute("questionsVector");

    session.setAttribute("currentQuizzId", quizzId);
    
    quizz = quizzScc.getQuizzDetail(quizzId);
    Collection<Question> questions = quizz.getQuestions();
    //questions collection to questions vector
    questionsV = new Vector<Question>(questions);
    session.setAttribute("questionsVector", questionsV);
  }
  questionsV = (Vector) session.getAttribute("questionsVector");
  quizzId = (String) session.getAttribute("currentQuizzId");
  
  Window window = gef.getWindow();
  Frame frame=gef.getFrame();
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(quizzScc.getSpaceLabel());
  browseBar.setComponentName(quizzScc.getComponentLabel());
  browseBar.setExtraInformation(resources.getString("QuizzUpdate"));

  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(m_context + "/util/icons/quizz_to_addQuestion.gif", resources.getString("QuestionAdd"), "javaScript:addQuestion()");
  
  out.println(window.printBefore());

  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resources.getString("GML.head"), "quizzUpdate.jsp?Action=UpdateQuizzHeader&QuizzId="+quizzId, action.equals("UpdateQuizzHeader"), true);
  tabbedPane.addTab(resources.getString("QuizzQuestions"), "questionsUpdate.jsp?Action=UpdateQuestions&QuizzId="+quizzId, action.equals("UpdateQuestions"), false);
  out.println(tabbedPane.print());
 
  out.println(frame.printBefore());

  out.println(displayQuestionsUpdateView(questionsV, gef, m_context, quizzScc, settings, resources));
%>
          <form name="questionForm" action="questionCreatorBis.jsp" method="post" enctype="multipart/form-data">
          <input type="hidden" name="Action" value="CreateQuestion">
          </form>
<%
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>
<% } %>