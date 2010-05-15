<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ include file="quizzUtils.jsp.inc" %>

<%
//R�cup�ration des param�tres
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
<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<Script language="javaScript1.2">
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
<BODY>
<%
          Vector questionsV = null;
          if (quizzId != null) {
              session.removeAttribute("currentQuizzId");
              session.removeAttribute("questionsVector");

              session.setAttribute("currentQuizzId", quizzId);
              
              quizz = quizzScc.getQuizzDetail(quizzId);
              Collection questions = quizz.getQuestions();
              //questions collection to questions vector
              questionsV = new Vector(questions);
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
          <Form name="questionForm" Action="questionCreatorBis.jsp" Method="POST" ENCTYPE="multipart/form-data">
          <input type="hidden" name="Action" value="CreateQuestion">
          </Form>
<%
          out.println(frame.printAfter());
          out.println(window.printAfter());
%>
</BODY>
</HTML>
<% } %>