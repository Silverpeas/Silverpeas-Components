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

<%@ include file="checkQuizz.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel />
<script language="javascript">
<!--
function MM_openBrWindow(theURL,winName,features) { //v2.0
  window.open(theURL,winName,features);
}
//-->
</script>
</head>
<body>
<script language="javascript">
  function validate_form()
  {
    return;
  }
</script>

<%
  //get SessionController, Language & Settings
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel(); 
  String quizz_id = (String) request.getParameter("quizz_id");
  QuestionContainerDetail quizzDetail = quizzScc.getQuizzDetail(quizz_id);

  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");
  window.printBefore();

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzParticipate")+" "+quizzDetail.getHeader().getTitle());
  browseBar.setPath("<a href=\"Main.jsp\">"+resources.getString("QuizzList")+"</a>");

  //operation pane
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation("icons/imprim.gif",resources.getString("GML.print"),"javascript:window.print()");
  operationPane.addLine(); 

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  out.println(frame.printBefore());

  // Quizz Header %>
  <span class="titreFenetre"><br/>&nbsp;&nbsp;&nbsp;<%=quizzDetail.getHeader().getTitle()%></span>
  <blockquote> 
    <p><span class="sousTitreFenetre"><%=quizzDetail.getHeader().getDescription()%></span></p>
    <%
    if (quizzDetail.getHeader().getComment() != null) {
%>
      <p>
<%      
      out.println(resources.getString("QuizzNotice")+"&nbsp;&nbsp;");
      out.println(quizzDetail.getHeader().getComment()); %>
      <br/>
      <br/>
      </p>
    <% } %>
  </blockquote>
  <form>
  <table width="100%" border="0">
  <%  //Questions
    Collection<Question> quizzQuestions = quizzDetail.getQuestions();
    Iterator<Question> i = quizzQuestions.iterator();
    while (i.hasNext()) {
      Question quizzQuestion = (Question) i.next(); %>
      <tr><td class="intfdcolor4" nowrap width="41%"><span class="txtlibform">&nbsp;<img src="icons/1pxRouge.gif" width=5 height=5/>&nbsp;<%=quizzQuestion.getLabel()%>&nbsp;</td>
          <td class="intfdcolor4" align="center" nowrap><%=quizzQuestion.getNbPointsMax()%> pts</td>
          <td class="intfdcolor4" align="center" nowrap> <%
            if (quizzQuestion.getClue() != null){ %>
              <a href="#" onclick="MM_openBrWindow('quizzClue.jsp?quizz_id='+<%=quizzDetail.getHeader().getPK().getId()%>+'&question_id='+<%=quizzQuestion.getPK().getId()%>,'indice','width=570,height=220')"><%=resources.getString("QuizzSeeClue")%></a> (<%=resources.getString("QuizzPenalty")%> = <%=quizzQuestion.getCluePenalty()%> pts)
              <% } %>
          </td>
      </tr> <%
      //Answers
      Collection<Answer> questionAnswers = quizzQuestion.getAnswers();
      Iterator<Answer> j = questionAnswers.iterator();
      while (j.hasNext()) {
        Answer questionAnswer = (Answer) j.next(); %>
        <tr><td colspan="3"><table><tr>
            <% if (questionAnswer.getImage() != null) { %>
                <td><img src="icons/<%=questionAnswer.getImage()%>" align="left"/></td>
            <% } else { %>
                <td width=50>&nbsp;</td>
            <% } %>
            <% if (quizzQuestion.isQCM()) { %>
                <td><input type="checkbox" name="chk_<%=quizzQuestion.getPK().getId()%>_<%=j%>" value="on"/>&nbsp;<%=questionAnswer.getLabel()%></td>
            <% } else { %>
                <td><input type="radio" name="opt_question<%=quizzQuestion.getPK().getId()%>" value="<%=questionAnswer.getPK().getId()%>"/>&nbsp;<%=questionAnswer.getLabel()%>
                <% if (questionAnswer.isOpened()) { %>
                    <br/><textarea rows=5 cols=40 name="txa_question<%=quizzQuestion.getPK().getId()%>"></textarea><% } %>
                </td><% } %>
            </tr></table></td>
        </tr><%
      } %>
      <tr><td colspan="3"><hr noshade size=1 width=98% align=center></td></tr><%
    } %>
    <tr> 
      <td colspan="3"><span class="txtnote">(<img src="icons/1pxRouge.gif" width="5" height="5"/>&nbsp;=&nbsp;<%=resources.getString("GML.requiredField")%>)</td>
    </tr>
   </table>
     <%
      out.println(frame.printMiddle());
      ButtonPane buttonPane = gef.getButtonPane();
      buttonPane.addButton(gef.getFormButton(resources.getString("GML.validate"), "javascript:validate_form()", true));
      buttonPane.addButton(gef.getFormButton(resources.getString("GML.cancel"), "Main.jsp", false));
      out.println("<br>");
      out.println("<table width=\"100%\"><tr><td align=\"center\">"+buttonPane.print()+"</td></tr></table>");
      out.println("<br><br>");
      out.println(frame.printAfter());
      out.println(window.printAfter());
      %>
</form>
</body>
</html>