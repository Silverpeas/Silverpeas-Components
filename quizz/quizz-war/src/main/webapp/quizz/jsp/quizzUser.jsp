<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.text.ParsePosition"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>
<%@ page import="com.stratelia.webactiv.quizz.control.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.score.model.*"%>
<%@ page import="com.stratelia.webactiv.util.score.control.*"%>
<%@ page import="com.stratelia.webactiv.quizz.QuizzException"%>

<%@ include file="checkQuizz.jsp" %>

<jsp:useBean id="currentQuizz" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />
<jsp:useBean id="currentParticipationId" scope="session" class="java.lang.String" />

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
	}catch (Exception e){
		throw new QuizzException ("quizzUser_JSP.displayCredits",QuizzException.WARNING,"Quizz.EX_CANNOT_DISPLAY_CREDITS",e);
	}

	return Html_display;
}
%>

<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String linkIcon = iconsPath + "/util/icons/link.gif";
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
  session.removeAttribute("currentQuizz");
  session.removeAttribute("questionsResponses");

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
  tabbedPane1.addTab(resources.getString("QuizzOnglet1"),"Main.jsp",true);
  tabbedPane1.addTab(resources.getString("QuizzSeeResult"),"quizzResultUser.jsp",false);
  
  out.println(tabbedPane1.print());
  out.println(frame.printBefore());

 //Tableau
  ArrayPane arrayPane = gef.getArrayPane("QuizzList","Main.jsp",request,session);

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
    //  gestion des permaliens sur les quizz
    String permalink = quizzHeader.getPermalink();
    String link = "&nbsp;<a href=\""+permalink+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("quizz.CopyQuizzLink")+"\" title=\""+resources.getString("quizz.CopyQuizzLink")+"\"></a>";

    ArrayCellText arrayCellText2 = null;
    if (nb_user_votes >= nb_max_participations)
      arrayCellText2 = arrayLine.addArrayCellText(quizzHeader.getTitle() + link);
    else
      arrayCellText2 = arrayLine.addArrayCellText("<A HREF=quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&ParticipationId="+nb_user_votes+"&Action=ViewCurrentQuestions>"+quizzHeader.getTitle()+"</A>" + link);

    arrayCellText2.setCompareOn(quizzHeader.getTitle().toLowerCase());
    arrayLine.addArrayCellText(Encode.javaStringToHtmlParagraphe(quizzHeader.getDescription()));
    arrayLine.addArrayCellText(displayCredits(nb_max_participations, nb_user_votes));
    
    Date creationDate = DateUtil.parse(quizzHeader.getCreationDate());
    ArrayCellText arrayCellText3 = arrayLine.addArrayCellText(resources.getOutputDate(creationDate));
    arrayCellText3.setCompareOn(creationDate);
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
</BODY>
</HTML>