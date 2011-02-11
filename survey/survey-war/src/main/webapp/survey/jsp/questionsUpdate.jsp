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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%--
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
--%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="ctxPath" value="${pageContext.request.contextPath}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<%--<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />--%>

<c:set var="action" value="${requestScope['Action']}" />
<c:set var="surveyName" value="${requestScope['SurveyName']}" />

<%
//Retrieve parameter
String action = (String) request.getParameter("Action");
String surveyId = (String) request.getParameter("SurveyId");
String surveyName = "";

String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String topicAddSrc = m_context + "/util/icons/folderAdd.gif";
String mandatoryField = m_context + "/util/icons/mandatoryField.gif";

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.survey.surveySettings", surveyScc.getLanguage());

QuestionContainerDetail survey = null;

%>
<html>
<head>
<view:looknfeel />
<script type="text/javascript">
function addQuestion() {
  document.questionForm.submit();
}
function updateQuestion(questionId) {
  $("#questionFormActionId").val("UpdateQuestion");
  $("#questionFormQuestionId").val(questionId);
  //alert("value= " +$("#questionFormActionId").val());
  document.questionForm.submit();
}
</script>
</head>
<body>
<%
  List questionsV = surveyScc.getSessionQuestions();
  surveyId = surveyScc.getSessionSurveyId();

  Window window = gef.getWindow();
%>
<%-- //TODO add the operation Pane only if there is no question (depends on vote or survey) --%>
<view:operationPane>
  <view:operation altText="<%=resources.getString("QuestionAdd")%>" icon="icons/questionAdd.gif" action="javaScript:addQuestion();"></view:operation>
</view:operationPane>
<fmt:message var="extraInfoBB" key="SurveyUpdate"/>
<c:set var="extraInfoBB" value="${extraInfoBB} '${surveyName}'" />
<view:browseBar extraInformations="${extraInfoBB}">
</view:browseBar>

<view:window>
<%          

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resources.getString("GML.head"), "surveyUpdate.jsp?Action=UpdateSurveyHeader&SurveyId="+surveyId, "UpdateSurveyHeader".equals(action), true);
tabbedPane.addTab(resources.getString("SurveyQuestions"), "questionsUpdate.jsp?Action=UpdateQuestions&SurveyId="+surveyId, "UpdateQuestions".equals(action), false);
out.println(tabbedPane.print());

//out.println(displayQuestionsUpdateView(surveyScc, questionsV, gef, m_context, settings, resources));

String questionUpSrc = "icons/arrowUp.gif";
String questionDownSrc = "icons/arrowDown.gif";
String questionDeleteSrc = m_context + "/util/icons/delete.gif";
String questionUpdateSrc = m_context + "/util/icons/update.gif";
Question question = null;
Collection answers = null;
String operations = "";
Board board = gef.getBoard();
try
{
    Frame frame = gef.getFrame();
    out.println(frame.printBefore());
    %>
    <center>
    <%
    if (questionsV != null && questionsV.size() > 0)
    {
        //Display the questions
      %>
<form name="survey" action="questionsUpdate.jsp" method="post" />
  <input type="hidden" name="Action" value="SubmitQuestions" />
        <%
        Iterator itQ = questionsV.iterator();
        int i = 1;
        for (int j=0; j<questionsV.size(); j++)
        {
              question = (Question) questionsV.get(j);
              answers = question.getAnswers();

              //check available operations to current question
              operations = " ";
              if (j!=0) {
                  operations += "<a href=\"questionsUpdate.jsp?Action=UpQuestion&QId="+j+"\"><img src=\""+questionUpSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionUp")+"\" title=\""+resources.getString("QuestionUp")+"\" align=\"absmiddle\"></a> ";
              }
              if (j+1!=questionsV.size()) {
                  operations += "<a href=\"questionsUpdate.jsp?Action=DownQuestion&QId="+j+"\"><img src=\""+questionDownSrc+"\" border=\"0\" alt=\""+resources.getString("QuestionDown")+"\" title=\""+resources.getString("QuestionDown")+"\" align=\"absmiddle\"></a> ";
              }
              operations += "<a href=\"javascript:updateQuestion('"+j+"');\"><img src=\""+questionUpdateSrc+"\" border=\"0\" alt=\""+surveyScc.getString("survey.update")+"\" title=\""+surveyScc.getString("survey.update")+"\"></a> ";
              operations += "<a href=\"questionsUpdate.jsp?Action=DeleteQuestion&QId="+j+"\"><img src=\""+questionDeleteSrc+"\" border=\"0\" alt=\""+resources.getString("GML.delete")+"\" title=\""+resources.getString("GML.delete")+"\"></a> ";

              out.println(board.printBefore());
              %>
              <table border="0" width="100%">
                <tr>
                  <td colspan="2" align="left"><b>&#149; <u><%=EncodeHelper.javaStringToHtmlString(question.getLabel())%></u></b>
                    <div id="surveyOperationId"><%=operations%></div><br/>
                  </td>
                </tr>
              <%
              // Switch on question type
              String style = question.getStyle();

              //if (question.isOpen())
              if (style.equals("open"))
              {
              		// Open question
                    Iterator itA = answers.iterator();
                    int isOpened = 0;
                    out.println("<tr><td colspan=\"2\"><textarea name=\"openedAnswer_"+i+"\" cols=\"60\" rows=\"4\"></textarea></td></tr>");
              }
              else
              {
               		if (style.equals("list"))
               		{
               			// drop down list
               			out.println("<tr><td><select id=\"answers\" name=\"answers\" onchange=\"if(this.value=='openanswer_"+i+"'){document.getElementById('openanswer"+i+"').style.display='block'}else{document.getElementById('openanswer"+i+"').style.display='none'};\">");

               			Iterator itA = answers.iterator();
                        while (itA.hasNext())
                        {
                            Answer answer = (Answer) itA.next();
                      	    if (answer.isOpened()) {
                                out.println("<option name=\"openanswer_"+i+"\" value=\"openanswer_"+i+"\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</option>");
                      	    } else {
                      	      out.println("<option name=\"answer_"+i+"\" value=\"\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</option>");
                      	    }
                        }
                        out.println("<input type=\"text\" id=\"openanswer"+i+"\" name=\"answer_"+i+"\" value=\"\" style=\"display:none\"/>");
                        out.println("</td></tr>");
                	}
                  	else
                  	{
                    	String inputType = "radio";
                    	if (style.equals("checkbox")) {
                          inputType = "checkbox";
                        }
                     	Iterator itA = answers.iterator();
                    	int isOpened = 0;
                    	while (itA.hasNext())
                    	{
                        	Answer answer = (Answer) itA.next();
                        	if (answer.isOpened())
                        	{
                            	isOpened = 1;
                            	out.println("<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"<BR><input type=\"text\" size=\"20\" name=\"openedAnswer_"+i+"\"></td></tr>");
                        	}
                        	else
                        	{
                            	if (answer.getImage() == null) {
                                  	out.println("<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\" width=\"100%\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"</td></tr>");
                            	} else {
                                  	String url = "";
        	                      	if (answer.getImage().startsWith("/"))
        	                      	{
        	                      		url = answer.getImage()+"&Size=266x150";
        	                      	}
        	                      	else
        	                      	{
                                        url = FileServerUtils.getUrl(surveyScc.getSpaceId(), surveyScc.getComponentId(), answer.getImage(), answer.getImage(), "image/gif", settings.getString("imagesSubDirectory"));
                                    }
                                    out.println("<tr><td width=\"40px\" align=\"center\"><input type=\""+inputType+"\" name=\"answer_"+i+"\" value=\"\" checked></td><td align=\"left\">"+EncodeHelper.javaStringToHtmlString(answer.getLabel())+"<br>");
                                    out.println("<img src=\""+url+"\" border=\"0\"></td><td>");
                              	}
                        	}
                    	} // {while}
                  }
              }
              i++;
              %>
              </table>
              <%
              out.println(board.printAfter());
              if (j<questionsV.size()-1) {
                out.println("<br>");
              }
          } // {for}
%>
</form>
<% } else { %>
        <br><fmt:message key="SurveyWithNoQuestions" /><br><br>
<% } %>
    </center>
    <%
    out.println(frame.printMiddle());
    Button voteButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "questionsUpdate.jsp?Action=SendQuestions", false);
    out.println("<center>"+voteButton.print()+"</center>");
    out.println(frame.printAfter());
} catch( Exception e){
    throw new SurveyException("SurveyUtils_JSP.displayQuestionsUpdateView",SurveyException.WARNING,"Survey.EX_CANNOT_DISPLAY_UPDATEVIEW",e);
}
          
%>
    <!-- questionCreatorBis.jsp -->
    <form name="questionForm" action="manageQuestions.jsp" method="post" enctype="multipart/form-data">
      <input type="hidden" name="Action" value="CreateQuestion" id="questionFormActionId" />
      <input type="hidden" name="QuestionId" value="" id="questionFormQuestionId" />
    </form>
</view:window>
</body>
</html>
