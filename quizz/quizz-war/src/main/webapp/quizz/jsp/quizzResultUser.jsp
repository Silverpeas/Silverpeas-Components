<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@ include file="checkQuizz.jsp" %>

<%!
String displayCredits(int nb_max_user_votes , int nb_user_votes)  throws QuizzException{
	String Html_display = null;
	try{
	  if (nb_user_votes == nb_max_user_votes)
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
		throw new QuizzException ("quizzResultUser_JSP.displayCredits",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_CREDITS",e);
	}
	return Html_display;
}
%>
<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>
<html>
<head>
<title>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</title>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
  <%
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel();

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
  tabbedPane1.addTab(resources.getString("QuizzOnglet1"),"Main.jsp",false);
  tabbedPane1.addTab(resources.getString("QuizzSeeResult"),"quizzResultUser.jsp",true);
  
  out.println(tabbedPane1.print());
  out.println(frame.printBefore());

 //Tableau
  ArrayPane arrayPane = gef.getArrayPane("QuizzResult","",request,session);
  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);
  arrayPane.addArrayColumn(resources.getString("GML.name"));
  arrayPane.addArrayColumn(resources.getString("ScoreDate"));
  arrayPane.addArrayColumn(resources.getString("QuizzCredits"));
  arrayPane.addArrayColumn(resources.getString("ScoreLib"));
  arrayPane.addArrayColumn(resources.getString("ScorePosition"));

  Collection<QuestionContainerHeader> quizzList = quizzScc.getUserResults();
  Iterator<QuestionContainerHeader> i = quizzList.iterator();
  while (i.hasNext()) {
    QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i.next();
    int nb_max_participations = quizzHeader.getNbMaxParticipations();
    Collection<ScoreDetail> scoreDetails = quizzHeader.getScores();
    int nb_user_votes = 0;
    if (scoreDetails != null)
    {
      nb_user_votes = scoreDetails.size();
      Iterator<ScoreDetail> j = scoreDetails.iterator();
      while (j.hasNext()) {
        ScoreDetail scoreDetail = (ScoreDetail) j.next();
    	ArrayLine arrayLine = arrayPane.addArrayLine();
    	arrayLine.addArrayCellLink("<img src=\"icons/palmares_30x15.gif\" border=0>","palmares.jsp?quizz_id="+quizzHeader.getPK().getId());
        arrayLine.addArrayCellText(quizzHeader.getTitle());
        
        Date participationDate = DateUtil.parse(scoreDetail.getParticipationDate());
		ArrayCellLink cell = arrayLine.addArrayCellLink(resources.getOutputDate(participationDate),"quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&Action=ViewResult&Page=1"+"&UserId="+scoreDetail.getUserId()+"&ParticipationId="+new Integer(scoreDetail.getParticipationId()).toString());
        
        arrayLine.addArrayCellText(displayCredits(nb_max_participations, nb_user_votes));
        ArrayCellText arrayCellText1 = arrayLine.addArrayCellText(new Integer(scoreDetail.getScore()).toString()+"/"+quizzHeader.getNbMaxPoints());
        arrayCellText1.setCompareOn(new Integer(scoreDetail.getScore()));
        ArrayCellText arrayCellText3 = arrayLine.addArrayCellText(new Integer(scoreDetail.getPosition()).toString());
		arrayCellText3.setCompareOn(new Integer(scoreDetail.getPosition()));

      }
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
<% out.println(frame.printMiddle());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>