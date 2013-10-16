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