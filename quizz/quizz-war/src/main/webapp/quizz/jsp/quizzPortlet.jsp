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
String pixSrc = iconsPath + "/util/icons/colorPix/1px.gif";

String spaceId = quizzScc.getSpaceId();
String componentId = quizzScc.getComponentId();

%>

<HTML>
<HEAD>
	<TITLE>___/ Silverpeas - Corporate Portal Organizer \__________________________________________</TITLE>
  <%
  ResourceLocator settings = quizzScc.getSettings();
  String space = quizzScc.getSpaceLabel();
  String component = quizzScc.getComponentLabel();
  session.removeAttribute("currentQuizz");
  %>
<script language="JavaScript1.2">
function deleteQuizz(quizz_id)
{
	window.open("../../Rquizz/jsp/quizzAdmin.jsp?Space=<%=spaceId%>&Component=<%=componentId%>", "MyMain");
}

function goto_jsp(jsp)
{
	window.open(jsp,"MyMain");
}
</script>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor=#FFFFFF leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

  <%

  Collection quizzList = quizzScc.getAdminQuizzList();
  Iterator i = quizzList.iterator();
  %>
 <table cellpadding="0" cellspacing="0" border="0" width="100%">
    <tr class="intfdcolor51" height=15>
	  <td><img src="<%=pixSrc%>" width=35 height=1></td>
      <td width="40%"><span class="textePetitBold"><%=resources.getString("GML.name")%></span></td>
      <td width="60%"><span class="textePetitBold"><%=resources.getString("GML.description")%></span></td>
    </tr>
	<tr bgcolor=666666>
      <td colspan=3><img src="<%=pixSrc%>"></td>
    </tr>
    <tr>
  <%
  while (i.hasNext()) {
    QuestionContainerHeader quizzHeader = (QuestionContainerHeader) i.next();
%>

	<td><a href="#" onClick="goto_jsp('../../Rquizz/<%=spaceId%>_<%=componentId%>/palmaresAdmin.jsp?quizz_id=<%=quizzHeader.getPK().getId()%>')"><img src="icons/palmares_30x15.gif" border=0></a></td>
	<td><span class="textePetitBold"><a href="#" onClick="goto_jsp('../../Rquizz/<%=spaceId%>_<%=componentId%>/quizzQuestionsNew.jsp?QuizzId=<%=quizzHeader.getPK().getId()%>&Action=ViewQuizz')">
	<%=quizzHeader.getTitle()%></a></span></td>
	<td><%=quizzHeader.getDescription()%></td>
	</tr><tr>
	  <td colspan="3" bgcolor=CCCCCC><img src="<%=pixSrc%>"></td>
      </tr>
  <%   } %>
<!--  FIN TAG FORM-->
</table>
</BODY>
</HTML>