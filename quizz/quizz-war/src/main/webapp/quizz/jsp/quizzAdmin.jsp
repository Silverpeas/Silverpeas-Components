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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.model.*"%>
<%@ page import="com.stratelia.webactiv.util.questionContainer.control.*"%>
<%@ page import="com.stratelia.webactiv.util.score.model.*"%>
<%@ page import="com.stratelia.webactiv.util.score.control.*"%>

<%@ include file="checkQuizz.jsp" %>

<jsp:useBean id="currentQuizz" scope="session" class="com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail" />

<%
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
String folderSrc = iconsPath + "/util/icons/delete.gif";
String linkIcon = iconsPath + "/util/icons/link.gif";

%>

<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
  <%
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel();
  session.removeAttribute("currentQuizz");

  String pdcUtilizationSrc  = m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";

  boolean isAdmin = false;

  if ("admin".equals(quizzScc.getUserRoleLevel())) {
      isAdmin = true;
  }
  %>
<script language="javascript" src="<%=m_context%>/util/javaScript/formUtil.js"></script>
<script language="JavaScript1.2">
function SP_openWindow(page,nom,largeur,hauteur,options) {
	var top=(screen.height-hauteur)/2;
	var left=(screen.width-largeur)/2;
	fenetre=window.open(page,nom,"top="+top+",left="+left+",width="+largeur+",height="+hauteur+","+options);
	fenetre.focus();
	return fenetre;
}

function openSPWindow(fonction, windowName){
    pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

function deleteQuizz(quizz_id)
{
  var rep = confirm('<%=resources.getString("QuizzDeleteThisQuizz")%>');
  if (rep==true)
    self.location="deleteQuizz.jsp?quizz_id="+quizz_id
}
</script>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

  <%
  //objet window
  Window window = gef.getWindow();
  window.setWidth("100%");
  

  //browse bar
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(space);
  browseBar.setComponentName(component);
  browseBar.setExtraInformation(resources.getString("QuizzList"));
  
  OperationPane operationPane = window.getOperationPane();
  if (isAdmin && quizzScc.isPdcUsed()) {
      operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDC"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId=" + quizzScc.getComponentId() + "','utilizationPdc1')");
      operationPane.addLine();
  }
  operationPane.addOperation(m_context + "/util/icons/quizz_to_add.gif",resources.getString("QuizzNewQuizz"), "quizzCreator.jsp");

  out.println(window.printBefore());

  Frame frame = gef.getFrame();
  
  //onglets
  TabbedPane tabbedPane1 = gef.getTabbedPane();
  tabbedPane1.addTab(resources.getString("QuizzOnglet1"),"quizzAdmin.jsp",true);
  tabbedPane1.addTab(resources.getString("QuizzSeeResult"),"quizzResultAdmin.jsp",false);
  
  out.println(tabbedPane1.print());
  out.println(frame.printBefore());

 //Tableau
  ArrayPane arrayPane = gef.getArrayPane("QuizzList", "quizzAdmin.jsp", request,session);

  ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
  arrayColumn0.setSortable(false);
  arrayPane.addArrayColumn(resources.getString("GML.name"));
  arrayPane.addArrayColumn(resources.getString("GML.description"));
  arrayPane.addArrayColumn(resources.getString("QuizzCreationDate"));
  arrayPane.addArrayColumn(resources.getString("GML.operation"));
  

  Collection quizzList = quizzScc.getAdminQuizzList();
  Iterator i = quizzList.iterator();
  while (i.hasNext()) {
    QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i.next();
    // gestion des permaliens sur les quizz
    String permalink = quizzHeader.getPermalink();
    String link = "&nbsp;<a href=\""+permalink+"\"><img src=\""+linkIcon+"\" border=\"0\" align=\"bottom\" alt=\""+resources.getString("quizz.CopyQuizzLink")+"\" title=\""+resources.getString("quizz.CopyQuizzLink")+"\"></a>";
    String name = "<a href=\"quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&Action=ViewQuizz"+"\">"+quizzHeader.getTitle()+"</a>";

	IconPane folderPane1 = gef.getIconPane();
	Icon folder1 = folderPane1.addIcon();
	folder1.setProperties(folderSrc, "", "javascript:deleteQuizz("+quizzHeader.getPK().getId()+");");
    ArrayLine arrayLine = arrayPane.addArrayLine();
    arrayLine.addArrayCellLink("<img src=\"icons/palmares_30x15.gif\" border=0>","palmaresAdmin.jsp?quizz_id="+quizzHeader.getPK().getId());
    //arrayLine.addArrayCellLink(quizzHeader.getTitle(),"quizzQuestionsNew.jsp?QuizzId="+quizzHeader.getPK().getId()+"&Action=ViewQuizz");
    arrayLine.addArrayCellText(name + link);
    arrayLine.addArrayCellText(Encode.javaStringToHtmlParagraphe(quizzHeader.getDescription()));
    
    Date creationDate = DateUtil.parse(quizzHeader.getCreationDate());
    ArrayCellText arrayCellText = arrayLine.addArrayCellText(resources.getOutputDate(creationDate));
    arrayCellText.setCompareOn(creationDate);
    
    arrayLine.addArrayCellIconPane(folderPane1);
  }
  out.println(arrayPane.print());
%>
  
<!--  FIN TAG FORM-->
<% out.println(frame.printMiddle());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</BODY>
</HTML>


