<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkQuizz.jsp" %>
<%
String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
%>

<html>
<head>
	<title>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</title>
<view:looknfeel/>
</head>
<body>

<%!
Vector infos(JspWriter out, Collection Questions, String questionId)  throws QuizzException {
    String clue = null;
    String label = null;
    Iterator i = Questions.iterator();
    Vector infos = new Vector();
    int questionNum = 0;
	try{
		int questionInt=new Integer(questionId).intValue();
		while ((i.hasNext()) && (questionNum < questionInt)) {
			Question quizzQuestion = (Question) i.next();
			questionNum++;
			if (questionNum == questionInt)
			{
			  clue = Encode.javaStringToHtmlParagraphe(quizzQuestion.getClue());
			  label = quizzQuestion.getLabel();
			  infos.add(label);
			  infos.add(clue);
			}
		}
	} catch (Exception e){
			throw new QuizzException ("questionCluePreview_JSP.infos",QuizzException.WARNING,"Quizz.EX_CANNOT_OBTAIN_QUESTIONS_INFOS",e);
	}

    return infos;
}
%>

<%
//Retrieve parameters
  String question_id = (String) request.getParameter("question_id");
  QuestionContainerDetail quizzDetail = (QuestionContainerDetail) session.getAttribute("quizzUnderConstruction");
  

  //get SessionController, Language & Settings
  SettingBundle settings = quizzScc.getSettings();
  
  //Get Space, component, and current quizz_id 
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel(); 

    //Questions
  Collection<Question> quizzQuestions = quizzDetail.getQuestions();
  Vector infos = infos(out, quizzQuestions, question_id);

  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");
  window.printBefore();

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(quizzDetail.getHeader().getTitle());

  out.println(window.printBefore());
  Frame frame = gef.getFrame();
  String title = resources.getString("QuizzClue")+":"+"&nbsp;"+infos.get(0)+"<br><br>";
  //title += "<a href=\"javascript:window.close()\"><div align=right><img align=middle src=\"icons/windowClose.gif\" width=16 height=14 border=0></a></div>";
  frame.addTitle(title);
  out.println(frame.printBefore());
%>
<center>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
<tr> 
	<td nowrap>
		<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
    <tr>
      <td width="30"></td>
      <td class=textePetitBold><%=infos.get(1)%></td>
      <td align="right"><img src="icons/silverProf_rvb.gif" width="69" height="70"> 
      </td>
    </tr>
  </table></td></tr></table>
  <br></center>
<%
  Button closeButton = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton(closeButton);
  out.println(closeButton.print());
%>

<%
  out.println(frame.printMiddle());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>


