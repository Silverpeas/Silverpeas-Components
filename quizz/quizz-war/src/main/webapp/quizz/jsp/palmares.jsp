<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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

<%@ include file="checkQuizz.jsp" %>

<%!
    String displayCredits(int nb_max_user_votes , int nb_user_votes) throws QuizzException
    {
	String Html_display = null;
	try {
		if (nb_user_votes >= nb_max_user_votes)
			Html_display = "<img src=\"icons/feuRouge.gif\">&nbsp;";
		else
			Html_display = "<img src=\"icons/feuVert.gif\">&nbsp;";
		for (int i=0; i<nb_max_user_votes; i++)
		{
			if (i < (nb_max_user_votes - nb_user_votes))
				Html_display += "<img src=\"icons/creditOn.gif\">";
			else
			Html_display += "<img src=\"icons/creditOff.gif\">";
		}
	}catch( Exception e){
		throw new QuizzException ("palmares_JSP.displayCredits",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_PALMARES",e);
	}

	return Html_display;
}
%>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>

<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
  <%
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel(); 

  String currentQuizzTitle="";
  int currentQuizzPoints=0;

  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");
  
  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzList"));
  
  out.println(window.printBefore());

  Frame frame = gef.getFrame();
  
  //onglets
  TabbedPane tabbedPane1 = gef.getTabbedPane();
  tabbedPane1.addTab(resources.getString("QuizzOnglet1"),"Main.jsp",true);
  tabbedPane1.addTab(resources.getString("QuizzSeeResult"),"quizzResultUser.jsp",false);
  
  out.println(tabbedPane1.print());
  out.println(frame.printBefore());

 //Tableau
  ArrayPane arrayPane = gef.getArrayPane("QuizzList","palmares.jsp?quizz_id="+request.getParameter("quizz_id"),request,session);

  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);

  arrayPane.addArrayColumn(resources.getString("GML.name"));
  arrayPane.addArrayColumn(resources.getString("GML.description"));
  arrayPane.addArrayColumn(resources.getString("QuizzCredits"));
  arrayPane.addArrayColumn(resources.getString("QuizzCreationDate"));

  Collection quizzList = quizzScc.getUserQuizzList();
  Iterator i = quizzList.iterator();
  while (i.hasNext()) {
    QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i.next();
    int nb_max_participations = quizzHeader.getNbMaxParticipations();
    Collection scoreDetails = quizzHeader.getScores(); 
    int nb_user_votes = 0;
    if (scoreDetails != null)
      nb_user_votes = scoreDetails.size();
    ArrayLine arrayLine = arrayPane.addArrayLine();
    arrayLine.addArrayCellLink("<img src=\"icons/palmares_30x15.gif\" border=0>","palmares.jsp?quizz_id="+quizzHeader.getPK().getId());
    ArrayCellText arrayCellText2 = null;
    if (nb_user_votes >= nb_max_participations)
      arrayCellText2 = arrayLine.addArrayCellText(quizzHeader.getTitle());
    else
      arrayCellText2 = arrayLine.addArrayCellText("<A HREF=quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&ParticipationId="+nb_user_votes+"&Action=ViewCurrentQuestions>"+quizzHeader.getTitle()+"</A>");
    arrayCellText2.setCompareOn((String) (quizzHeader.getTitle()).toLowerCase());
    ArrayCellText arrayCellText3 = arrayLine.addArrayCellText(Encode.javaStringToHtmlString(quizzHeader.getDescription()));
    ArrayCellText arrayCellText4 = arrayLine.addArrayCellText(displayCredits(nb_max_participations, nb_user_votes));
    
	Date creationDate = DateUtil.parse(quizzHeader.getCreationDate());
    ArrayCellText arrayCellText5 = arrayLine.addArrayCellText(resources.getOutputDate(creationDate));
    arrayCellText5.setCompareOn(creationDate);
    
    if (quizzHeader.getPK().getId().equals(request.getParameter("quizz_id")))
    {
      currentQuizzTitle=quizzHeader.getTitle();
      currentQuizzPoints=quizzHeader.getNbMaxPoints(); 
    }
  }
  out.println(arrayPane.print());
%>
  <blockquote> <img src="icons/feuVert.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzParticipateYes")%><br>
      <img src="icons/feuRouge.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzParticipateNo")%><br>
      <img src="icons/creditOff.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzCreditOver")%><br>
      <img src="icons/creditOn.gif" width="10" height="10" align="absmiddle">&nbsp;<%=resources.getString("QuizzCreditAvailable")%>
  </blockquote>
<!--  FIN TAG FORM-->
<% 
out.println(frame.printMiddle());
out.println("<br>&nbsp;&nbsp;<span class=sousTitreFenetre>"+resources.getString("QuizzSeeResult")+"&nbsp;:&nbsp;"+currentQuizzTitle+"<br></span>");
%>
<!-- Palmares du quizz -->
<%
if (quizzScc.getIsAllowedTopScores())
{
    out.println("<br>&nbsp;&nbsp;<span class=textePetitBold2>"+resources.getString("QuizzPalmares")+"&nbsp;"+quizzScc.getNbTopScores()+"<br></span>");
    ArrayPane arrayPane2 = gef.getArrayPane("QuizzResult","palmares.jsp?quizz_id="+request.getParameter("quizz_id"),request,session);
    arrayPane2.addArrayColumn(resources.getString("ScorePosition"));
    arrayPane2.addArrayColumn(resources.getString("GML.user"));
    arrayPane2.addArrayColumn(resources.getString("ScoreDate"));
    arrayPane2.addArrayColumn(resources.getString("ScoreLib"));

  Collection scoreList = quizzScc.getUserPalmares(request.getParameter("quizz_id"));
  if (scoreList != null)
  {
    Iterator j = scoreList.iterator();
    while (j.hasNext()) {
      ScoreDetail scoreDetail = (ScoreDetail) j.next();
      UserDetail userDetail=quizzScc.getUserDetail(scoreDetail.getUserId());
      String firstName = "";
      String lastName = resources.getString("UserUnknown");
      if (userDetail != null)
      {
	      firstName = userDetail.getFirstName();
	      lastName = userDetail.getLastName();
      }
      ArrayLine arrayLine = arrayPane2.addArrayLine();
      ArrayCellText arrayCellText3 = arrayLine.addArrayCellText(new Integer(scoreDetail.getPosition()).toString());
      arrayCellText3.setCompareOn(new Integer(scoreDetail.getPosition()));
      arrayLine.addArrayCellText(lastName + " " + firstName);
      
      Date participationDate = DateUtil.parse(scoreDetail.getParticipationDate());
      ArrayCellText arrayCellText = arrayLine.addArrayCellText(resources.getOutputDate(participationDate));
      arrayCellText.setCompareOn(participationDate);
      
      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(new Integer(scoreDetail.getScore()).toString()+"/"+currentQuizzPoints);
      arrayCellText1.setCompareOn(new Integer(scoreDetail.getScore()));
    }
  }
  out.println(arrayPane2.print());
}
%>
<!-- fin palmares -->  
<!-- Résultats du user -->
<%
    out.println("<br>&nbsp;&nbsp;<span class=textePetitBold2>"+resources.getString("QuizzUserParticipations")+"<br></span>");
    ArrayPane arrayPane3 = gef.getArrayPane("QuizzResult2","palmares.jsp?quizz_id="+request.getParameter("quizz_id"),request,session);
    arrayPane3.addArrayColumn(resources.getString("ScorePosition"));
    arrayPane3.addArrayColumn(resources.getString("ScoreDate"));
    arrayPane3.addArrayColumn(resources.getString("ScoreLib"));

  Collection userScoreList = quizzScc.getUserScoresByFatherId(request.getParameter("quizz_id"));
  if (userScoreList != null)
  {
    Iterator j = userScoreList.iterator();
    while (j.hasNext()) {
      ScoreDetail scoreDetail = (ScoreDetail) j.next();
      ArrayLine arrayLine = arrayPane3.addArrayLine();
      arrayLine.addArrayCellText(new Integer(scoreDetail.getPosition()).toString());
      
      Date participationDate = DateUtil.parse(scoreDetail.getParticipationDate());
      arrayLine.addArrayCellLink(resources.getOutputDate(participationDate),"quizzQuestionsNew.jsp?QuizzId="+request.getParameter("quizz_id")+"&Action=ViewResult&Page=0"+"&UserId="+scoreDetail.getUserId()+"&ParticipationId="+new Integer(scoreDetail.getParticipationId()).toString());
      
      ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(new Integer(scoreDetail.getScore()).toString()+"/"+currentQuizzPoints);
      arrayCellText1.setCompareOn(new Integer(scoreDetail.getScore()));
    }
  }
  out.println(arrayPane3.print());
%>
<!-- fin résultats user -->  
<!-- moyenne -->
<div align="right">
  <table width="100%" border="0" cellspacing="0" cellpadding="5">
    <tr>
      <td width="100%" align="left" valign="middle" class="textePetitBold2"><%=resources.getString("QuizzNbVoters")%>&nbsp;:&nbsp;
        <%=quizzScc.getNbVoters(request.getParameter("quizz_id"))%>
      </td>
      <td width="100%" align="right" valign="middle"  class="textePetitBold2"><%=resources.getString("QuizzAverage")%>&nbsp;:&nbsp;</td>
      <td align="right"> 
        <table border="0" cellspacing="0" cellpadding="0">
          <tr> 
            <td rowspan="3"><img src="icons/tableProf_1.gif" width="7" height="70"></td>
            <td><img src="icons/tableProf_2.gif" width="55" height="6"></td>
            <td rowspan="3"><img src="icons/tableProf_4.gif" width="79" height="70"></td>
          </tr>
          <tr> 
            <td height="42" bgcolor="#387B80"><span class="titreFenetre2"><div align="center">
<%
float average=quizzScc.getAveragePoints(request.getParameter("quizz_id"));
int averageInt=Math.round(average);
if (averageInt==average)
  out.println(averageInt);
else
  out.println(average);
%>           
              <br>
              <img src="icons/1pxBlanc.gif" width="30" height="1"><br>
              <%=currentQuizzPoints%></div></span>
           </td>
          </tr>
          <tr> 
            <td><img src="icons/tableProf_3.gif" width="55" height="22"></td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</div>
<!-- fin moyenne -->

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>


